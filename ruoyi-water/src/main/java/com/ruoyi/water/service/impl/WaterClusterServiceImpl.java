package com.ruoyi.water.service.impl;

import com.ruoyi.common.kmeans.*;
import com.ruoyi.water.domain.WaterAnomalyRecord;
import com.ruoyi.water.domain.WaterUsageFeature;
import com.ruoyi.water.mapper.WaterAnomalyRecordMapper;
import com.ruoyi.water.mapper.WaterUsageFeatureMapper;
import com.ruoyi.water.mapper.WaterUsageRecordMapper;
import com.ruoyi.water.service.WaterClusterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用水聚类分析 Service 实现类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterClusterServiceImpl implements WaterClusterService {

    private final WaterUsageFeatureMapper waterUsageFeatureMapper;
    private final WaterUsageRecordMapper waterUsageRecordMapper;
    private final WaterAnomalyRecordMapper waterAnomalyRecordMapper;

    /**
     * 聚类配置（可通过接口动态调整）
     */
    private KMeansConfig clusterConfig = new KMeansConfig(3, 100);

    /**
     * 异常检测器
     */
    private AnomalyDetector anomalyDetector = new AnomalyDetector();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateFeatureData(String statPeriod) {
        log.info("开始生成用水特征数据，统计周期：{}", statPeriod);

        // 从用水记录表聚合统计数据
        List<Map<String, Object>> statisticsList = waterUsageRecordMapper.selectAllUsageStatisticsByPeriod(statPeriod);

        if (statisticsList == null || statisticsList.isEmpty()) {
            log.warn("统计周期 {} 没有用水记录数据", statPeriod);
            return 0;
        }

        int count = 0;
        for (Map<String, Object> stats : statisticsList) {
            Long userId = ((Number) stats.get("user_id")).longValue();

            // 检查是否已存在该用户该周期的特征数据
            WaterUsageFeature existingFeature = waterUsageFeatureMapper.selectWaterUsageFeatureByUserAndPeriod(userId, statPeriod);

            WaterUsageFeature feature = new WaterUsageFeature();
            feature.setUserId(userId);
            feature.setStatPeriod(statPeriod);
            feature.setTotalUsage(getBigDecimalValue(stats.get("total_usage")));
            feature.setAvgDailyUsage(calculateAvgDailyUsage(stats, statPeriod));
            feature.setUsageCount(getIntValue(stats.get("usage_count")));
            feature.setDawnRatio(getBigDecimalValue(stats.get("dawn_ratio")));
            feature.setMorningRatio(getBigDecimalValue(stats.get("morning_ratio")));
            feature.setAfternoonRatio(getBigDecimalValue(stats.get("afternoon_ratio")));
            feature.setEveningRatio(getBigDecimalValue(stats.get("evening_ratio")));
            feature.setWeekendRatio(getBigDecimalValue(stats.get("weekend_ratio")));

            if (existingFeature != null) {
                feature.setFeatureId(existingFeature.getFeatureId());
                waterUsageFeatureMapper.updateWaterUsageFeature(feature);
            } else {
                waterUsageFeatureMapper.insertWaterUsageFeature(feature);
            }
            count++;
        }

        log.info("用水特征数据生成完成，共处理 {} 条记录", count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClusterResult executeKMeansClustering(String statPeriod, KMeansConfig config) {
        log.info("开始执行K-means聚类分析，统计周期：{}，配置：{}", statPeriod, config);

        // 获取该周期的所有特征数据
        List<WaterUsageFeature> featureList = waterUsageFeatureMapper.selectWaterUsageFeatureByPeriod(statPeriod);

        if (featureList == null || featureList.size() < config.getK()) {
            log.warn("特征数据不足，无法进行K-means聚类，数据量：{}，K值：{}",
                    featureList != null ? featureList.size() : 0, config.getK());
            return null;
        }

        // 构建数据点列表
        List<DataPoint> dataPoints = new ArrayList<>();
        for (WaterUsageFeature f : featureList) {
            double[] features = new double[]{
                    f.getAvgDailyUsage() != null ? f.getAvgDailyUsage().doubleValue() : 0,
                    f.getDawnRatio() != null ? f.getDawnRatio().doubleValue() : 0,
                    f.getMorningRatio() != null ? f.getMorningRatio().doubleValue() : 0,
                    f.getAfternoonRatio() != null ? f.getAfternoonRatio().doubleValue() : 0,
                    f.getEveningRatio() != null ? f.getEveningRatio().doubleValue() : 0,
                    f.getWeekendRatio() != null ? f.getWeekendRatio().doubleValue() : 0
            };
            DataPoint point = new DataPoint(f.getUserId(), features, f.getUserName());
            dataPoints.add(point);
        }

        // 执行K-means聚类
        ClusterResult result = KMeansAlgorithm.cluster(dataPoints, config);

        // 更新特征表中的聚类结果
        String[] labels = result.getClusterLabels();
        for (int i = 0; i < featureList.size(); i++) {
            WaterUsageFeature feature = featureList.get(i);
            DataPoint point = dataPoints.get(i);
            feature.setClusterId(point.getClusterId());
            feature.setClusterLabel(labels != null && point.getClusterId() < labels.length ?
                    labels[point.getClusterId()] : "类型" + (point.getClusterId() + 1));
            waterUsageFeatureMapper.updateWaterUsageFeature(feature);
        }

        log.info("K-means聚类分析完成，迭代次数：{}，SSE：{}，轮廓系数：{}",
                result.getIterations(), result.getSse(), result.getSilhouetteScore());

        return result;
    }

    @Override
    public ClusterResult executeKMeansClustering(String statPeriod, int k) {
        KMeansConfig config = new KMeansConfig(k, clusterConfig.getMaxIterations());
        config.setNormalize(clusterConfig.isNormalize());
        config.setConvergenceThreshold(clusterConfig.getConvergenceThreshold());
        config.setClusterLabels(clusterConfig.getClusterLabels());
        return executeKMeansClustering(statPeriod, config);
    }

    @Override
    public KMeansConfig getClusterConfig() {
        return clusterConfig;
    }

    @Override
    public void updateClusterConfig(KMeansConfig config) {
        if (config != null) {
            this.clusterConfig = config;
            log.info("聚类配置已更新：{}", config);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeAnomalyDetection(String statPeriod) {
        log.info("开始执行异常检测，统计周期：{}", statPeriod);

        // 获取该周期的所有特征数据
        List<WaterUsageFeature> featureList = waterUsageFeatureMapper.selectWaterUsageFeatureByPeriod(statPeriod);

        if (featureList == null || featureList.isEmpty()) {
            log.warn("统计周期 {} 没有特征数据", statPeriod);
            return 0;
        }

        // 转换为异常检测器需要的数据格式
        List<AnomalyDetector.WaterFeatureData> featureDataList = new ArrayList<>();
        for (WaterUsageFeature f : featureList) {
            AnomalyDetector.WaterFeatureData data = new AnomalyDetector.WaterFeatureData();
            data.setUserId(f.getUserId());
            data.setUserName(f.getUserName());
            data.setStatPeriod(f.getStatPeriod());
            data.setTotalUsage(f.getTotalUsage() != null ? f.getTotalUsage().doubleValue() : 0);
            data.setAvgDailyUsage(f.getAvgDailyUsage() != null ? f.getAvgDailyUsage().doubleValue() : 0);
            data.setUsageCount(f.getUsageCount());
            data.setDawnRatio(f.getDawnRatio() != null ? f.getDawnRatio().doubleValue() : 0);
            data.setMorningRatio(f.getMorningRatio() != null ? f.getMorningRatio().doubleValue() : 0);
            data.setAfternoonRatio(f.getAfternoonRatio() != null ? f.getAfternoonRatio().doubleValue() : 0);
            data.setEveningRatio(f.getEveningRatio() != null ? f.getEveningRatio().doubleValue() : 0);
            data.setWeekendRatio(f.getWeekendRatio() != null ? f.getWeekendRatio().doubleValue() : 0);
            featureDataList.add(data);
        }

        // 执行异常检测
        List<AnomalyDetector.AnomalyResult> anomalies = anomalyDetector.detectBatch(featureDataList);

        if (anomalies.isEmpty()) {
            log.info("未检测到异常");
            // 删除该周期的待处理异常记录（因为重新检测后没有异常了）
            waterAnomalyRecordMapper.deletePendingByStatPeriod(statPeriod);
            return 0;
        }

        // 只删除该周期的待处理异常记录，保留已处理和已忽略的记录
        waterAnomalyRecordMapper.deletePendingByStatPeriod(statPeriod);

        // 保存异常记录（只保存不存在的记录，避免重复）
        List<WaterAnomalyRecord> records = new ArrayList<>();
        for (AnomalyDetector.AnomalyResult anomaly : anomalies) {
            // 检查该异常是否已存在（已处理或已忽略的记录）
            int exists = waterAnomalyRecordMapper.checkAnomalyExists(
                    anomaly.getUserId(), anomaly.getStatPeriod(), anomaly.getRuleCode());
            if (exists > 0) {
                // 已存在（已处理或已忽略），跳过
                continue;
            }

            WaterAnomalyRecord record = new WaterAnomalyRecord();
            record.setUserId(anomaly.getUserId());
            record.setStatPeriod(anomaly.getStatPeriod());
            record.setRuleCode(anomaly.getRuleCode());
            record.setRuleName(anomaly.getRuleName());
            record.setAnomalyDescription(anomaly.getAnomalyDescription());
            record.setFeatureField(anomaly.getFeatureField());
            record.setActualValue(BigDecimal.valueOf(anomaly.getActualValue()));
            record.setThresholdValue(anomaly.getThresholdValue() != null ?
                    BigDecimal.valueOf(anomaly.getThresholdValue()) : null);
            record.setSeverity(anomaly.getSeverity());
            record.setStatus(0); // 未处理
            records.add(record);
        }

        if (!records.isEmpty()) {
            waterAnomalyRecordMapper.batchInsertWaterAnomalyRecord(records);
        }

        log.info("异常检测完成，新增 {} 条异常记录", records.size());
        return records.size();
    }

    @Override
    public List<AnomalyRule> getAnomalyRules() {
        return anomalyDetector.getRules();
    }

    @Override
    public void updateAnomalyRules(List<AnomalyRule> rules) {
        anomalyDetector.setRules(rules);
        log.info("异常检测规则已更新，共 {} 条规则", rules.size());
    }

    @Override
    public void addAnomalyRule(AnomalyRule rule) {
        anomalyDetector.addRule(rule);
        log.info("已添加异常检测规则：{}", rule.getRuleCode());
    }

    @Override
    public void removeAnomalyRule(String ruleCode) {
        anomalyDetector.removeRule(ruleCode);
        log.info("已删除异常检测规则：{}", ruleCode);
    }

    @Override
    public List<WaterAnomalyRecord> selectAnomalyRecordList(WaterAnomalyRecord waterAnomalyRecord) {
        return waterAnomalyRecordMapper.selectWaterAnomalyRecordList(waterAnomalyRecord);
    }

    @Override
    public boolean handleAnomalyRecord(Long anomalyId, Integer status, String handleBy, String handleRemark) {
        int rows = waterAnomalyRecordMapper.updateStatus(anomalyId, status, handleBy, handleRemark);
        return rows > 0;
    }

    @Override
    public List<WaterUsageFeature> getClusterResultByPeriod(String statPeriod) {
        return waterUsageFeatureMapper.selectWaterUsageFeatureByPeriod(statPeriod);
    }

    @Override
    public List<WaterUsageFeature> getClusterStatistics(String statPeriod) {
        return waterUsageFeatureMapper.selectClusterStatistics(statPeriod);
    }

    /**
     * 计算日均用水量
     */
    private BigDecimal calculateAvgDailyUsage(Map<String, Object> stats, String statPeriod) {
        BigDecimal totalUsage = getBigDecimalValue(stats.get("total_usage"));
        // 简单按30天计算，实际可根据月份天数调整
        if (totalUsage.compareTo(BigDecimal.ZERO) > 0) {
            return totalUsage.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 安全获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(4, RoundingMode.HALF_UP);
        }
        try {
            return new BigDecimal(value.toString()).setScale(4, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 安全获取Integer值
     */
    private Integer getIntValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

