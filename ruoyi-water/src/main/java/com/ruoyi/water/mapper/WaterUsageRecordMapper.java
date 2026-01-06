package com.ruoyi.water.mapper;

import com.ruoyi.water.domain.WaterUsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用水记录 Mapper 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Mapper
public interface WaterUsageRecordMapper {

    /**
     * 查询用水记录列表
     *
     * @param waterUsageRecord 查询条件
     * @return 用水记录列表
     */
    List<WaterUsageRecord> selectWaterUsageRecordList(WaterUsageRecord waterUsageRecord);

    /**
     * 根据记录ID查询用水记录
     *
     * @param recordId 记录ID
     * @return 用水记录
     */
    WaterUsageRecord selectWaterUsageRecordById(Long recordId);

    /**
     * 根据用户ID查询用水记录列表
     *
     * @param userId 用户ID
     * @return 用水记录列表
     */
    List<WaterUsageRecord> selectWaterUsageRecordByUserId(Long userId);

    /**
     * 新增用水记录
     *
     * @param waterUsageRecord 用水记录
     * @return 影响行数
     */
    int insertWaterUsageRecord(WaterUsageRecord waterUsageRecord);

    /**
     * 修改用水记录
     *
     * @param waterUsageRecord 用水记录
     * @return 影响行数
     */
    int updateWaterUsageRecord(WaterUsageRecord waterUsageRecord);

    /**
     * 删除用水记录（逻辑删除）
     *
     * @param recordId 记录ID
     * @return 影响行数
     */
    int deleteWaterUsageRecordById(Long recordId);

    /**
     * 批量删除用水记录（逻辑删除）
     *
     * @param recordIds 记录ID数组
     * @return 影响行数
     */
    int deleteWaterUsageRecordByIds(Long[] recordIds);

    /**
     * 根据用户ID和统计周期查询用水统计数据
     *
     * @param userId 用户ID
     * @param statPeriod 统计周期（如：2024-01）
     * @return 统计数据
     */
    Map<String, Object> selectUsageStatisticsByUserAndPeriod(@Param("userId") Long userId, @Param("statPeriod") String statPeriod);

    /**
     * 根据统计周期查询所有用户的用水统计数据
     *
     * @param statPeriod 统计周期（如：2024-01）
     * @return 统计数据列表
     */
    List<Map<String, Object>> selectAllUsageStatisticsByPeriod(@Param("statPeriod") String statPeriod);

    /**
     * 查询用水类型分布统计
     *
     * @param userId 用户ID（可选）
     * @return 类型分布统计
     */
    List<Map<String, Object>> selectUsageTypeStatistics(@Param("userId") Long userId);

    /**
     * 查询时段分布统计
     *
     * @param userId 用户ID（可选）
     * @return 时段分布统计
     */
    List<Map<String, Object>> selectTimePeriodStatistics(@Param("userId") Long userId);

    /**
     * 查询全局用水概览统计
     *
     * @return 概览统计（总用户数、总记录数、总用水量）
     */
    Map<String, Object> selectGlobalOverview();

    /**
     * 查询用户个人用水概览统计
     *
     * @param userId 用户ID
     * @return 概览统计（记录数、总用水量、平均用水量）
     */
    Map<String, Object> selectUserOverview(@Param("userId") Long userId);

    /**
     * 查询近N天用水趋势
     *
     * @param userId 用户ID（可选，为空则查全局）
     * @param days   天数
     * @return 每日用水量趋势
     */
    List<Map<String, Object>> selectDailyTrend(@Param("userId") Long userId, @Param("days") int days);
}

