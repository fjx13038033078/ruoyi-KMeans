package com.ruoyi.water.mapper;

import com.ruoyi.water.domain.WaterUsageFeature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用水特征 Mapper 接口（K-means聚类分析用）
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Mapper
public interface WaterUsageFeatureMapper {

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
    WaterUsageFeature selectWaterUsageFeatureByUserAndPeriod(@Param("userId") Long userId, @Param("statPeriod") String statPeriod);

    /**
     * 根据统计周期查询所有用水特征
     *
     * @param statPeriod 统计周期
     * @return 用水特征列表
     */
    List<WaterUsageFeature> selectWaterUsageFeatureByPeriod(@Param("statPeriod") String statPeriod);

    /**
     * 新增用水特征
     *
     * @param waterUsageFeature 用水特征
     * @return 影响行数
     */
    int insertWaterUsageFeature(WaterUsageFeature waterUsageFeature);

    /**
     * 修改用水特征
     *
     * @param waterUsageFeature 用水特征
     * @return 影响行数
     */
    int updateWaterUsageFeature(WaterUsageFeature waterUsageFeature);

    /**
     * 删除用水特征
     *
     * @param featureId 特征ID
     * @return 影响行数
     */
    int deleteWaterUsageFeatureById(Long featureId);

    /**
     * 批量删除用水特征
     *
     * @param featureIds 特征ID数组
     * @return 影响行数
     */
    int deleteWaterUsageFeatureByIds(Long[] featureIds);

    /**
     * 批量更新聚类结果
     *
     * @param featureList 用水特征列表（包含聚类结果）
     * @return 影响行数
     */
    int batchUpdateClusterResult(@Param("list") List<WaterUsageFeature> featureList);

    /**
     * 根据聚类ID查询用水特征列表
     *
     * @param clusterId 聚类簇ID
     * @param statPeriod 统计周期
     * @return 用水特征列表
     */
    List<WaterUsageFeature> selectWaterUsageFeatureByClusterId(@Param("clusterId") Integer clusterId, @Param("statPeriod") String statPeriod);

    /**
     * 查询聚类统计信息
     *
     * @param statPeriod 统计周期
     * @return 聚类统计列表
     */
    List<WaterUsageFeature> selectClusterStatistics(@Param("statPeriod") String statPeriod);
}

