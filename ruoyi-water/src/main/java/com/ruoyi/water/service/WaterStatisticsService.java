package com.ruoyi.water.service;

import java.util.Map;

/**
 * 用水统计 Service 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public interface WaterStatisticsService {

    /**
     * 获取全局统计数据（管理员/业务操作员）
     *
     * @return 统计数据
     */
    Map<String, Object> getGlobalStatistics();

    /**
     * 获取用户个人统计数据（普通用户）
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    Map<String, Object> getUserStatistics(Long userId);
}

