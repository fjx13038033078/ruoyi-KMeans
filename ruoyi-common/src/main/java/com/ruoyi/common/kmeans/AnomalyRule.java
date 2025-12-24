package com.ruoyi.common.kmeans;

import java.io.Serializable;

/**
 * 异常检测规则定义
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public class AnomalyRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        /**
         * 阈值规则：值超过阈值则异常
         */
        THRESHOLD,
        /**
         * 倍数规则：值超过平均值的N倍则异常
         */
        MULTIPLIER,
        /**
         * 范围规则：值不在指定范围内则异常
         */
        RANGE
    }

    /**
     * 规则编码（唯一标识）
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则类型
     */
    private RuleType ruleType;

    /**
     * 检测的特征字段名
     */
    private String featureField;

    /**
     * 阈值（对于THRESHOLD和MULTIPLIER类型）
     */
    private Double threshold;

    /**
     * 最小值（对于RANGE类型）
     */
    private Double minValue;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 权重（用于综合评分）
     */
    private double weight = 1.0;

    public AnomalyRule() {
    }

    public AnomalyRule(String ruleCode, String ruleName, String description,
                       RuleType ruleType, String featureField, Double threshold, Double minValue) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.description = description;
        this.ruleType = ruleType;
        this.featureField = featureField;
        this.threshold = threshold;
        this.minValue = minValue;
    }

    // Getters and Setters

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getFeatureField() {
        return featureField;
    }

    public void setFeatureField(String featureField) {
        this.featureField = featureField;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "AnomalyRule{" +
                "ruleCode='" + ruleCode + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType=" + ruleType +
                ", featureField='" + featureField + '\'' +
                ", threshold=" + threshold +
                ", enabled=" + enabled +
                '}';
    }
}

