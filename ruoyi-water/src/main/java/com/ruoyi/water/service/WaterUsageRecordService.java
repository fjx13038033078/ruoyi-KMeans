package com.ruoyi.water.service;

import com.ruoyi.water.domain.WaterUsageRecord;

import java.util.List;
import java.util.Map;

/**
 * 用水记录 Service 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public interface WaterUsageRecordService {

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
     * @return 是否成功
     */
    boolean insertWaterUsageRecord(WaterUsageRecord waterUsageRecord);

    /**
     * 修改用水记录
     *
     * @param waterUsageRecord 用水记录
     * @return 是否成功
     */
    boolean updateWaterUsageRecord(WaterUsageRecord waterUsageRecord);

    /**
     * 删除用水记录
     *
     * @param recordId 记录ID
     * @return 是否成功
     */
    boolean deleteWaterUsageRecordById(Long recordId);

    /**
     * 批量删除用水记录
     *
     * @param recordIds 记录ID数组
     * @return 是否成功
     */
    boolean deleteWaterUsageRecordByIds(Long[] recordIds);

    /**
     * 查询用水类型分布统计
     *
     * @param userId 用户ID（可选）
     * @return 类型分布统计
     */
    List<Map<String, Object>> selectUsageTypeStatistics(Long userId);

    /**
     * 查询时段分布统计
     *
     * @param userId 用户ID（可选）
     * @return 时段分布统计
     */
    List<Map<String, Object>> selectTimePeriodStatistics(Long userId);
}

