package com.ruoyi.water.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用水记录实体类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@TableName("water_usage_record")
public class WaterUsageRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

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
     * 用水日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private LocalDate usageDate;

    /**
     * 用水量（立方米）
     */
    private BigDecimal usageAmount;

    /**
     * 用水类型：1-饮用,2-烹饪,3-洗浴,4-洗衣,5-冲厕,6-其他
     */
    private Integer usageType;

    /**
     * 用水时段：1-凌晨(0-6),2-上午(6-12),3-下午(12-18),4-晚间(18-24)
     */
    private Integer timePeriod;

    /**
     * 是否周末：0-否,1-是
     */
    private Integer isWeekend;

    /**
     * 删除标志：0-存在,2-删除
     */
    private Integer delFlag;
}
