package com.ruoyi.water.mapper;

import com.ruoyi.water.domain.WaterHousehold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 住户信息 Mapper 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Mapper
public interface WaterHouseholdMapper {

    /**
     * 查询住户信息列表
     *
     * @param waterHousehold 查询条件
     * @return 住户信息列表
     */
    List<WaterHousehold> selectWaterHouseholdList(WaterHousehold waterHousehold);

    /**
     * 根据住户ID查询住户信息
     *
     * @param householdId 住户ID
     * @return 住户信息
     */
    WaterHousehold selectWaterHouseholdById(Long householdId);

    /**
     * 根据用户ID查询住户信息
     *
     * @param userId 用户ID
     * @return 住户信息
     */
    WaterHousehold selectWaterHouseholdByUserId(Long userId);

    /**
     * 新增住户信息
     *
     * @param waterHousehold 住户信息
     * @return 影响行数
     */
    int insertWaterHousehold(WaterHousehold waterHousehold);

    /**
     * 修改住户信息
     *
     * @param waterHousehold 住户信息
     * @return 影响行数
     */
    int updateWaterHousehold(WaterHousehold waterHousehold);

    /**
     * 删除住户信息
     *
     * @param householdId 住户ID
     * @return 影响行数
     */
    int deleteWaterHouseholdById(Long householdId);

    /**
     * 批量删除住户信息
     *
     * @param householdIds 住户ID数组
     * @return 影响行数
     */
    int deleteWaterHouseholdByIds(Long[] householdIds);

    /**
     * 更新住户状态
     *
     * @param householdId 住户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("householdId") Long householdId, @Param("status") Integer status);
}

