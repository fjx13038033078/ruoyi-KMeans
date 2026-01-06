package com.ruoyi.water.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.water.domain.WaterAnomalyRecord;
import com.ruoyi.water.mapper.WaterAnomalyRecordMapper;
import com.ruoyi.water.service.WaterAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用水告警 Service 实现类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Service
@RequiredArgsConstructor
public class WaterAlertServiceImpl implements WaterAlertService {

    private final WaterAnomalyRecordMapper waterAnomalyRecordMapper;

    @Override
    public List<WaterAnomalyRecord> selectMyAlertList(WaterAnomalyRecord waterAnomalyRecord) {
        // 强制设置为当前用户ID
        waterAnomalyRecord.setUserId(SecurityUtils.getUserId());
        return waterAnomalyRecordMapper.selectMyAlertList(waterAnomalyRecord);
    }

    @Override
    public int getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        return waterAnomalyRecordMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long anomalyId) {
        Long userId = SecurityUtils.getUserId();
        return waterAnomalyRecordMapper.markAsRead(anomalyId, userId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead() {
        Long userId = SecurityUtils.getUserId();
        return waterAnomalyRecordMapper.batchMarkAsRead(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitFeedback(Long anomalyId, Integer userFeedback, String feedbackRemark) {
        Long userId = SecurityUtils.getUserId();
        return waterAnomalyRecordMapper.submitFeedback(anomalyId, userId, userFeedback, feedbackRemark) > 0;
    }

    @Override
    public WaterAnomalyRecord getAlertDetail(Long anomalyId) {
        WaterAnomalyRecord record = waterAnomalyRecordMapper.selectWaterAnomalyRecordById(anomalyId);
        // 验证是否是当前用户的告警
        if (record != null && record.getUserId().equals(SecurityUtils.getUserId())) {
            // 自动标记为已读
            if (record.getIsRead() == null || record.getIsRead() == 0) {
                markAsRead(anomalyId);
                record.setIsRead(1);
            }
            return record;
        }
        return null;
    }
}

