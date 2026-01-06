package com.ruoyi.water.service;

import com.ruoyi.water.domain.WaterAnomalyRecord;

import java.util.List;

/**
 * 用水告警 Service 接口
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public interface WaterAlertService {

    /**
     * 查询当前用户的告警列表
     *
     * @param waterAnomalyRecord 查询条件
     * @return 告警列表
     */
    List<WaterAnomalyRecord> selectMyAlertList(WaterAnomalyRecord waterAnomalyRecord);

    /**
     * 获取当前用户未读告警数量
     *
     * @return 未读数量
     */
    int getUnreadCount();

    /**
     * 标记告警为已读
     *
     * @param anomalyId 告警ID
     * @return 是否成功
     */
    boolean markAsRead(Long anomalyId);

    /**
     * 全部标记为已读
     *
     * @return 标记的数量
     */
    int markAllAsRead();

    /**
     * 提交用户反馈
     *
     * @param anomalyId      告警ID
     * @param userFeedback   用户反馈（1-确认属实，2-认为误报）
     * @param feedbackRemark 反馈说明
     * @return 是否成功
     */
    boolean submitFeedback(Long anomalyId, Integer userFeedback, String feedbackRemark);

    /**
     * 查询告警详情
     *
     * @param anomalyId 告警ID
     * @return 告警详情
     */
    WaterAnomalyRecord getAlertDetail(Long anomalyId);
}

