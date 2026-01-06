package com.ruoyi.water.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 用水异常记录实体类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@TableName("water_anomaly_record")
public class WaterAnomalyRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 异常记录ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long anomalyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称（不存在于数据库，用于显示）
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 统计周期（如：2024-01）
     */
    private String statPeriod;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 异常描述
     */
    private String anomalyDescription;

    /**
     * 检测的特征字段
     */
    private String featureField;

    /**
     * 实际值
     */
    private BigDecimal actualValue;

    /**
     * 阈值
     */
    private BigDecimal thresholdValue;

    /**
     * 严重程度（1-5级）
     */
    private Integer severity;

    /**
     * 处理状态：0-未处理,1-已处理,2-已忽略
     */
    private Integer status;

    /**
     * 用户是否已读：0-未读,1-已读
     */
    private Integer isRead;

    /**
     * 用户反馈：0-未反馈,1-确认属实,2-认为误报
     */
    private Integer userFeedback;

    /**
     * 用户反馈时间
     */
    private java.util.Date feedbackTime;

    /**
     * 用户反馈说明
     */
    private String feedbackRemark;

    /**
     * 处理人
     */
    private String handleBy;

    /**
     * 处理意见
     */
    private String handleRemark;
}

