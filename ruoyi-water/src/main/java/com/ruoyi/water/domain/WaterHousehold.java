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

/**
 * 住户信息实体类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@TableName("water_household")
public class WaterHousehold extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 住户ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long householdId;

    /**
     * 用户ID，关联sys_user表
     */
    private Long userId;

    /**
     * 用户名称（不存在于数据库，用于显示）
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 住址
     */
    private String address;

    /**
     * 所属小区
     */
    private String community;

    /**
     * 家庭人数
     */
    private Integer familySize;

    /**
     * 水表编号
     */
    private String meterNo;

    /**
     * 状态：0-正常,1-停用
     */
    private Integer status;
}
