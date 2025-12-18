package com.ruoyi.water.service.impl;

import com.ruoyi.water.domain.WaterUsageFeature;
import com.ruoyi.water.mapper.WaterUsageFeatureMapper;
import com.ruoyi.water.mapper.WaterUsageRecordMapper;
import com.ruoyi.water.service.WaterUsageFeatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 用水特征 Service 实现类（K-means聚类分析用）
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterUsageFeatureServiceImpl implements WaterUsageFeatureService {

    private final WaterUsageFeatureMapper waterUsageFeatureMapper;
    private final WaterUsageRecordMapper waterUsageRecordMapper;

    /**
     * 聚类标签映射
     */
    private static final String[] CLUSTER_LABELS = {"节约型", "正常型", "高耗型", "夜间活跃型", "波动型"};

    @Override
    public List<WaterUsageFeature> selectWaterUsageFeatureList(WaterUsageFeature waterUsageFeature) {
        return waterUsageFeatureMapper.selectWaterUsageFeatureList(waterUsageFeature);
    }

    @Override
    public WaterUsageFeature selectWaterUsageFeatureById(Long featureId) {
        return waterUsageFeatureMapper.selectWaterUsageFeatureById(featureId);
    }

    @Override
    public WaterUsageFeature selectWaterUsageFeatureByUserAndPeriod(Long userId, String statPeriod) {
        return waterUsageFeatureMapper.selectWaterUsageFeatureByUserAndPeriod(userId, statPeriod);
    }

    @Override
    public List<WaterUsageFeature> selectWaterUsageFeatureByPeriod(String statPeriod) {
        return waterUsageFeatureMapper.selectWaterUsageFeatureByPeriod(statPeriod);
    }

    @Override
    public boolean insertWaterUsageFeature(WaterUsageFeature waterUsageFeature) {
        int rows = waterUsageFeatureMapper.insertWaterUsageFeature(waterUsageFeature);
        return rows > 0;
    }

    @Override
    public boolean updateWaterUsageFeature(WaterUsageFeature waterUsageFeature) {
        int rows = waterUsageFeatureMapper.updateWaterUsageFeature(waterUsageFeature);
        return rows > 0;
    }

    @Override
    public boolean deleteWaterUsageFeatureById(Long featureId) {
        int rows = waterUsageFeatureMapper.deleteWaterUsageFeatureById(featureId);
        return rows > 0;
    }

    @Override
    public boolean deleteWaterUsageFeatureByIds(Long[] featureIds) {
        int rows = waterUsageFeatureMapper.deleteWaterUsageFeatureByIds(featureIds);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateFeatureData(String statPeriod) {
        log.info("开始生成用水特征数据，统计周期：{}", statPeriod);

        // 从用水记录表聚合统计数据
        List<Map<String, Object>> statisticsList = waterUsageRecordMapper.selectAllUsageStatisticsByPeriod(statPeriod);

        if (statisticsList == null || statisticsList.isEmpty()) {
            log.warn("统计周期 {} 没有用水记录数据", statPeriod);
            return false;
        }

        for (Map<String, Object> stats : statisticsList) {
            Long userId = ((Number) stats.get("user_id")).longValue();

            // 检查是否已存在该用户该周期的特征数据
            WaterUsageFeature existingFeature = waterUsageFeatureMapper.selectWaterUsageFeatureByUserAndPeriod(userId, statPeriod);

            WaterUsageFeature feature = new WaterUsageFeature();
            feature.setUserId(userId);
            feature.setStatPeriod(statPeriod);
            feature.setTotalUsage(getBigDecimalValue(stats.get("total_usage")));
            feature.setAvgDailyUsage(getBigDecimalValue(stats.get("avg_daily_usage")));
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
        }

        log.info("用水特征数据生成完成，共处理 {} 条记录", statisticsList.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeKMeansClustering(String statPeriod, int k) {
        log.info("开始执行K-means聚类分析，统计周期：{}，K值：{}", statPeriod, k);

        // 获取该周期的所有特征数据
        List<WaterUsageFeature> featureList = waterUsageFeatureMapper.selectWaterUsageFeatureByPeriod(statPeriod);

        if (featureList == null || featureList.size() < k) {
            log.warn("特征数据不足，无法进行K-means聚类，数据量：{}，K值：{}", featureList != null ? featureList.size() : 0, k);
            return false;
        }

        // 提取特征向量
        double[][] features = extractFeatures(featureList);

        // 执行K-means聚类
        int[] clusterAssignments = kMeans(features, k, 100);

        // 计算每个簇的平均日用水量，用于确定簇标签
        Map<Integer, Double> clusterAvgUsage = new HashMap<>();
        Map<Integer, Integer> clusterCount = new HashMap<>();
        for (int i = 0; i < clusterAssignments.length; i++) {
            int cluster = clusterAssignments[i];
            double avgUsage = featureList.get(i).getAvgDailyUsage().doubleValue();
            clusterAvgUsage.merge(cluster, avgUsage, Double::sum);
            clusterCount.merge(cluster, 1, Integer::sum);
        }

        // 计算每个簇的平均用水量
        List<Map.Entry<Integer, Double>> sortedClusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            double avg = clusterAvgUsage.getOrDefault(i, 0.0) / clusterCount.getOrDefault(i, 1);
            sortedClusters.add(new AbstractMap.SimpleEntry<>(i, avg));
        }
        sortedClusters.sort(Comparator.comparingDouble(Map.Entry::getValue));

        // 根据用水量排序分配标签
        Map<Integer, String> clusterLabelMap = new HashMap<>();
        for (int i = 0; i < sortedClusters.size(); i++) {
            int clusterId = sortedClusters.get(i).getKey();
            String label = i < CLUSTER_LABELS.length ? CLUSTER_LABELS[i] : "类型" + (i + 1);
            clusterLabelMap.put(clusterId, label);
        }

        // 更新聚类结果
        for (int i = 0; i < featureList.size(); i++) {
            WaterUsageFeature feature = featureList.get(i);
            int clusterId = clusterAssignments[i];
            feature.setClusterId(clusterId);
            feature.setClusterLabel(clusterLabelMap.get(clusterId));
            waterUsageFeatureMapper.updateWaterUsageFeature(feature);
        }

        log.info("K-means聚类分析完成，共处理 {} 条记录", featureList.size());
        return true;
    }

    @Override
    public List<WaterUsageFeature> selectWaterUsageFeatureByClusterId(Integer clusterId, String statPeriod) {
        return waterUsageFeatureMapper.selectWaterUsageFeatureByClusterId(clusterId, statPeriod);
    }

    @Override
    public List<WaterUsageFeature> selectClusterStatistics(String statPeriod) {
        return waterUsageFeatureMapper.selectClusterStatistics(statPeriod);
    }

    /**
     * 提取特征向量
     */
    private double[][] extractFeatures(List<WaterUsageFeature> featureList) {
        double[][] features = new double[featureList.size()][6];
        for (int i = 0; i < featureList.size(); i++) {
            WaterUsageFeature f = featureList.get(i);
            features[i][0] = f.getAvgDailyUsage() != null ? f.getAvgDailyUsage().doubleValue() : 0;
            features[i][1] = f.getDawnRatio() != null ? f.getDawnRatio().doubleValue() : 0;
            features[i][2] = f.getMorningRatio() != null ? f.getMorningRatio().doubleValue() : 0;
            features[i][3] = f.getAfternoonRatio() != null ? f.getAfternoonRatio().doubleValue() : 0;
            features[i][4] = f.getEveningRatio() != null ? f.getEveningRatio().doubleValue() : 0;
            features[i][5] = f.getWeekendRatio() != null ? f.getWeekendRatio().doubleValue() : 0;
        }
        // 标准化特征
        return normalizeFeatures(features);
    }

    /**
     * 特征标准化（Z-score标准化）
     */
    private double[][] normalizeFeatures(double[][] features) {
        int n = features.length;
        int m = features[0].length;

        for (int j = 0; j < m; j++) {
            double mean = 0;
            for (int i = 0; i < n; i++) {
                mean += features[i][j];
            }
            mean /= n;

            double std = 0;
            for (int i = 0; i < n; i++) {
                std += Math.pow(features[i][j] - mean, 2);
            }
            std = Math.sqrt(std / n);

            if (std > 0) {
                for (int i = 0; i < n; i++) {
                    features[i][j] = (features[i][j] - mean) / std;
                }
            }
        }
        return features;
    }

    /**
     * K-means聚类算法实现
     */
    private int[] kMeans(double[][] data, int k, int maxIterations) {
        int n = data.length;
        int m = data[0].length;
        Random random = new Random(42);

        // 随机初始化聚类中心
        double[][] centroids = new double[k][m];
        Set<Integer> selectedIndices = new HashSet<>();
        for (int i = 0; i < k; i++) {
            int index;
            do {
                index = random.nextInt(n);
            } while (selectedIndices.contains(index));
            selectedIndices.add(index);
            centroids[i] = Arrays.copyOf(data[index], m);
        }

        int[] assignments = new int[n];
        Arrays.fill(assignments, -1);

        for (int iter = 0; iter < maxIterations; iter++) {
            boolean changed = false;

            // 分配每个点到最近的聚类中心
            for (int i = 0; i < n; i++) {
                int nearestCluster = 0;
                double minDistance = Double.MAX_VALUE;

                for (int j = 0; j < k; j++) {
                    double distance = euclideanDistance(data[i], centroids[j]);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestCluster = j;
                    }
                }

                if (assignments[i] != nearestCluster) {
                    assignments[i] = nearestCluster;
                    changed = true;
                }
            }

            // 如果没有变化，提前结束
            if (!changed) {
                break;
            }

            // 更新聚类中心
            double[][] newCentroids = new double[k][m];
            int[] counts = new int[k];

            for (int i = 0; i < n; i++) {
                int cluster = assignments[i];
                counts[cluster]++;
                for (int j = 0; j < m; j++) {
                    newCentroids[cluster][j] += data[i][j];
                }
            }

            for (int i = 0; i < k; i++) {
                if (counts[i] > 0) {
                    for (int j = 0; j < m; j++) {
                        centroids[i][j] = newCentroids[i][j] / counts[i];
                    }
                }
            }
        }

        return assignments;
    }

    /**
     * 计算欧几里得距离
     */
    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * 安全获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
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
        return Integer.parseInt(value.toString());
    }
}

