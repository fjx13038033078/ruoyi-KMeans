package com.ruoyi.water.service;

import com.ruoyi.water.domain.WaterUsageFeature;

import java.util.List;

/**
 * 用水特征 Service 接口（K-means聚类分析用）
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public interface WaterUsageFeatureService {

    /**
     * 查询用水特征列表
     *
     * @param waterUsageFeature 查询条件
     * @return 用水特征列表
     */
    List<WaterUsageFeature> selectWaterUsageFeatureList(WaterUsageFeature waterUsageFeature);

    /**
     * 根据特征ID查询用水特征
     *
     * @param featureId 特征ID
     * @return 用水特征
     */
    WaterUsageFeature selectWaterUsageFeatureById(Long featureId);

    /**
     * 根据用户ID和统计周期查询用水特征
     *
     * @param userId 用户ID
     * @param statPeriod 统计周期
     * @return 用水特征
     */
    WaterUsageFeature selectWaterUsageFeatureByUserAndPeriod(Long userId, String statPeriod);

    /**
     * 根据统计周期查询所有用水特征
     *
     * @param statPeriod 统计周期
     * @return 用水特征列表
     */
    List<WaterUsageFeature> selectWaterUsageFeatureByPeriod(String statPeriod);

    /**
     * 新增用水特征
     *
     * @param waterUsageFeature 用水特征
     * @return 是否成功
     */
    boolean insertWaterUsageFeature(WaterUsageFeature waterUsageFeature);

    /**
     * 修改用水特征
     *
     * @param waterUsageFeature 用水特征
     * @return 是否成功
     */
    boolean updateWaterUsageFeature(WaterUsageFeature waterUsageFeature);

    /**
     * 删除用水特征
     *
     * @param featureId 特征ID
     * @return 是否成功
     */
    boolean deleteWaterUsageFeatureById(Long featureId);

    /**
     * 批量删除用水特征
     *
     * @param featureIds 特征ID数组
     * @return 是否成功
     */
    boolean deleteWaterUsageFeatureByIds(Long[] featureIds);

    /**
     * 生成用水特征数据（从用水记录表聚合）
     *
     * @param statPeriod 统计周期（如：2024-01）
     * @return 是否成功
     */
    boolean generateFeatureData(String statPeriod);

    /**
     * 执行K-means聚类分析
     *
     * @param statPeriod 统计周期
     * @param k 聚类数量
     * @return 是否成功
     */
    boolean executeKMeansClustering(String statPeriod, int k);

    /**
     * 根据聚类ID查询用水特征列表
     *
     * @param clusterId 聚类簇ID
     * @param statPeriod 统计周期
     * @return 用水特征列表
     */
    List<WaterUsageFeature> selectWaterUsageFeatureByClusterId(Integer clusterId, String statPeriod);

    /**
     * 查询聚类统计信息
     *
     * @param statPeriod 统计周期
     * @return 聚类统计列表
     */
    List<WaterUsageFeature> selectClusterStatistics(String statPeriod);
}

