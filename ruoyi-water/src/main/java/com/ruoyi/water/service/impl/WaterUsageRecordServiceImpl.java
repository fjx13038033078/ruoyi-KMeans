package com.ruoyi.water.service.impl;

import com.ruoyi.water.domain.WaterUsageRecord;
import com.ruoyi.water.mapper.WaterUsageRecordMapper;
import com.ruoyi.water.service.WaterUsageRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用水记录 Service 实现类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterUsageRecordServiceImpl implements WaterUsageRecordService {

    private final WaterUsageRecordMapper waterUsageRecordMapper;

    @Override
    public List<WaterUsageRecord> selectWaterUsageRecordList(WaterUsageRecord waterUsageRecord) {
        return waterUsageRecordMapper.selectWaterUsageRecordList(waterUsageRecord);
    }

    @Override
    public WaterUsageRecord selectWaterUsageRecordById(Long recordId) {
        return waterUsageRecordMapper.selectWaterUsageRecordById(recordId);
    }

    @Override
    public List<WaterUsageRecord> selectWaterUsageRecordByUserId(Long userId) {
        return waterUsageRecordMapper.selectWaterUsageRecordByUserId(userId);
    }

    @Override
    public boolean insertWaterUsageRecord(WaterUsageRecord waterUsageRecord) {
        int rows = waterUsageRecordMapper.insertWaterUsageRecord(waterUsageRecord);
        return rows > 0;
    }

    @Override
    public boolean updateWaterUsageRecord(WaterUsageRecord waterUsageRecord) {
        int rows = waterUsageRecordMapper.updateWaterUsageRecord(waterUsageRecord);
        return rows > 0;
    }

    @Override
    public boolean deleteWaterUsageRecordById(Long recordId) {
        int rows = waterUsageRecordMapper.deleteWaterUsageRecordById(recordId);
        return rows > 0;
    }

    @Override
    public boolean deleteWaterUsageRecordByIds(Long[] recordIds) {
        int rows = waterUsageRecordMapper.deleteWaterUsageRecordByIds(recordIds);
        return rows > 0;
    }

    @Override
    public List<Map<String, Object>> selectUsageTypeStatistics(Long userId) {
        return waterUsageRecordMapper.selectUsageTypeStatistics(userId);
    }

    @Override
    public List<Map<String, Object>> selectTimePeriodStatistics(Long userId) {
        return waterUsageRecordMapper.selectTimePeriodStatistics(userId);
    }
}

