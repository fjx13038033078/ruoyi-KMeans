package com.ruoyi.water.service.impl;

import com.ruoyi.water.mapper.WaterAnomalyRecordMapper;
import com.ruoyi.water.mapper.WaterHouseholdMapper;
import com.ruoyi.water.mapper.WaterUsageRecordMapper;
import com.ruoyi.water.service.WaterStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用水统计 Service 实现类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Service
@RequiredArgsConstructor
public class WaterStatisticsServiceImpl implements WaterStatisticsService {

    private final WaterUsageRecordMapper waterUsageRecordMapper;
    private final WaterHouseholdMapper waterHouseholdMapper;
    private final WaterAnomalyRecordMapper waterAnomalyRecordMapper;

    @Override
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 用水概览
        Map<String, Object> overview = waterUsageRecordMapper.selectGlobalOverview();
        result.put("overview", overview != null ? overview : new HashMap<>());

        // 2. 用水类型分布
        List<Map<String, Object>> typeDistribution = waterUsageRecordMapper.selectUsageTypeStatistics(null);
        result.put("typeDistribution", typeDistribution);

        // 3. 用水时段分布
        List<Map<String, Object>> periodDistribution = waterUsageRecordMapper.selectTimePeriodStatistics(null);
        result.put("periodDistribution", periodDistribution);

        // 4. 异常统计
        Map<String, Object> anomalyStats = waterAnomalyRecordMapper.selectGlobalAnomalyStats();
        result.put("anomalyStats", anomalyStats != null ? anomalyStats : new HashMap<>());

        // 5. 近7天用水趋势
        List<Map<String, Object>> dailyTrend = waterUsageRecordMapper.selectDailyTrend(null, 7);
        result.put("dailyTrend", dailyTrend);

        // 6. 住户数量
        int householdCount = waterHouseholdMapper.selectHouseholdCount();
        result.put("householdCount", householdCount);

        return result;
    }

    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 个人用水概览
        Map<String, Object> overview = waterUsageRecordMapper.selectUserOverview(userId);
        result.put("overview", overview != null ? overview : new HashMap<>());

        // 2. 个人用水类型分布
        List<Map<String, Object>> typeDistribution = waterUsageRecordMapper.selectUsageTypeStatistics(userId);
        result.put("typeDistribution", typeDistribution);

        // 3. 个人用水时段分布
        List<Map<String, Object>> periodDistribution = waterUsageRecordMapper.selectTimePeriodStatistics(userId);
        result.put("periodDistribution", periodDistribution);

        // 4. 个人告警统计
        Map<String, Object> alertStats = waterAnomalyRecordMapper.selectUserAlertStats(userId);
        result.put("alertStats", alertStats != null ? alertStats : new HashMap<>());

        // 5. 个人近7天用水趋势
        List<Map<String, Object>> dailyTrend = waterUsageRecordMapper.selectDailyTrend(userId, 7);
        result.put("dailyTrend", dailyTrend);

        return result;
    }
}

