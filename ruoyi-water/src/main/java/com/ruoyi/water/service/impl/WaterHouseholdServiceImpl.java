package com.ruoyi.water.service.impl;

import com.ruoyi.water.domain.WaterHousehold;
import com.ruoyi.water.mapper.WaterHouseholdMapper;
import com.ruoyi.water.service.WaterHouseholdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 住户信息 Service 实现类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaterHouseholdServiceImpl implements WaterHouseholdService {

    private final WaterHouseholdMapper waterHouseholdMapper;

    @Override
    public List<WaterHousehold> selectWaterHouseholdList(WaterHousehold waterHousehold) {
        return waterHouseholdMapper.selectWaterHouseholdList(waterHousehold);
    }

    @Override
    public WaterHousehold selectWaterHouseholdById(Long householdId) {
        return waterHouseholdMapper.selectWaterHouseholdById(householdId);
    }

    @Override
    public WaterHousehold selectWaterHouseholdByUserId(Long userId) {
        return waterHouseholdMapper.selectWaterHouseholdByUserId(userId);
    }

    @Override
    public boolean insertWaterHousehold(WaterHousehold waterHousehold) {
        int rows = waterHouseholdMapper.insertWaterHousehold(waterHousehold);
        return rows > 0;
    }

    @Override
    public boolean updateWaterHousehold(WaterHousehold waterHousehold) {
        int rows = waterHouseholdMapper.updateWaterHousehold(waterHousehold);
        return rows > 0;
    }

    @Override
    public boolean deleteWaterHouseholdById(Long householdId) {
        int rows = waterHouseholdMapper.deleteWaterHouseholdById(householdId);
        return rows > 0;
    }

    @Override
    public boolean deleteWaterHouseholdByIds(Long[] householdIds) {
        int rows = waterHouseholdMapper.deleteWaterHouseholdByIds(householdIds);
        return rows > 0;
    }

    @Override
    public boolean updateStatus(Long householdId, Integer status) {
        int rows = waterHouseholdMapper.updateStatus(householdId, status);
        return rows > 0;
    }
}

