package com.ruoyi.water.service;

import com.ruoyi.common.kmeans.AnomalyDetector;
import com.ruoyi.common.kmeans.AnomalyRule;
import com.ruoyi.common.kmeans.ClusterResult;
import com.ruoyi.common.kmeans.KMeansConfig;
import com.ruoyi.water.domain.WaterAnomalyRecord;
import com.ruoyi.water.domain.WaterUsageFeature;

import java.util.List;

/**
 * 用水聚类分析 Service 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public interface WaterClusterService {

    /**
     * 生成用水特征数据（从用水记录表聚合）
     *
     * @param statPeriod 统计周期（如：2024-01）
     * @return 生成的特征数量
     */
    int generateFeatureData(String statPeriod);

    /**
     * 执行K-means聚类分析
     *
     * @param statPeriod 统计周期
     * @param config     聚类配置
     * @return 聚类结果
     */
    ClusterResult executeKMeansClustering(String statPeriod, KMeansConfig config);

    /**
     * 执行K-means聚类分析（使用默认配置）
     *
     * @param statPeriod 统计周期
     * @param k          聚类数量
     * @return 聚类结果
     */
    ClusterResult executeKMeansClustering(String statPeriod, int k);

    /**
     * 获取聚类配置
     *
     * @return 当前聚类配置
     */
    KMeansConfig getClusterConfig();

    /**
     * 更新聚类配置
     *
     * @param config 新的聚类配置
     */
    void updateClusterConfig(KMeansConfig config);

    /**
     * 执行异常检测
     *
     * @param statPeriod 统计周期
     * @return 检测到的异常数量
     */
    int executeAnomalyDetection(String statPeriod);

    /**
     * 获取异常检测规则列表
     *
     * @return 规则列表
     */
    List<AnomalyRule> getAnomalyRules();

    /**
     * 更新异常检测规则
     *
     * @param rules 规则列表
     */
    void updateAnomalyRules(List<AnomalyRule> rules);

    /**
     * 添加异常检测规则
     *
     * @param rule 规则
     */
    void addAnomalyRule(AnomalyRule rule);

    /**
     * 删除异常检测规则
     *
     * @param ruleCode 规则编码
     */
    void removeAnomalyRule(String ruleCode);

    /**
     * 查询异常记录列表
     *
     * @param waterAnomalyRecord 查询条件
     * @return 异常记录列表
     */
    List<WaterAnomalyRecord> selectAnomalyRecordList(WaterAnomalyRecord waterAnomalyRecord);

    /**
     * 处理异常记录
     *
     * @param anomalyId    异常记录ID
     * @param status       处理状态
     * @param handleBy     处理人
     * @param handleRemark 处理意见
     * @return 是否成功
     */
    boolean handleAnomalyRecord(Long anomalyId, Integer status, String handleBy, String handleRemark);

    /**
     * 根据统计周期查询聚类结果
     *
     * @param statPeriod 统计周期
     * @return 用水特征列表（包含聚类结果）
     */
    List<WaterUsageFeature> getClusterResultByPeriod(String statPeriod);

    /**
     * 查询聚类统计信息
     *
     * @param statPeriod 统计周期
     * @return 聚类统计列表
     */
    List<WaterUsageFeature> getClusterStatistics(String statPeriod);
}

