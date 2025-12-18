package com.ruoyi.water.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用水特征实体类（K-means聚类分析用）
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@TableName("water_usage_feature")
public class WaterUsageFeature implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 特征ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long featureId;

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
     * 总用水量（立方米）
     */
    private BigDecimal totalUsage;

    /**
     * 日均用水量（立方米）
     */
    private BigDecimal avgDailyUsage;

    /**
     * 用水次数
     */
    private Integer usageCount;

    /**
     * 凌晨用水占比(%)
     */
    private BigDecimal dawnRatio;

    /**
     * 上午用水占比(%)
     */
    private BigDecimal morningRatio;

    /**
     * 下午用水占比(%)
     */
    private BigDecimal afternoonRatio;

    /**
     * 晚间用水占比(%)
     */
    private BigDecimal eveningRatio;

    /**
     * 周末用水占比(%)
     */
    private BigDecimal weekendRatio;

    /**
     * 聚类簇ID
     */
    private Integer clusterId;

    /**
     * 聚类标签（如：节约型、正常型、高耗型）
     */
    private String clusterLabel;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}

