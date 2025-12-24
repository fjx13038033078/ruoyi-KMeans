package com.ruoyi.common.kmeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用水异常检测器
 * 基于规则的异常检测算法
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public class AnomalyDetector {

    /**
     * 异常检测规则列表
     */
    private List<AnomalyRule> rules;

    public AnomalyDetector() {
        this.rules = new ArrayList<>();
        // 初始化默认规则
        initDefaultRules();
    }

    public AnomalyDetector(List<AnomalyRule> rules) {
        this.rules = rules != null ? rules : new ArrayList<>();
    }

    /**
     * 初始化默认异常检测规则
     */
    private void initDefaultRules() {
        // 规则1：凌晨用水异常 - 凌晨时段用水占比超过30%
        rules.add(new AnomalyRule(
                "NIGHT_USAGE_ABNORMAL",
                "凌晨用水异常",
                "凌晨(0-6点)用水占比超过阈值，可能存在漏水或异常用水行为",
                AnomalyRule.RuleType.THRESHOLD,
                "dawnRatio",
                30.0,
                null
        ));

        // 规则2：用水量异常高 - 日均用水量超过平均值的2倍
        rules.add(new AnomalyRule(
                "HIGH_USAGE",
                "用水量异常高",
                "日均用水量显著高于平均水平，可能存在浪费或漏水",
                AnomalyRule.RuleType.MULTIPLIER,
                "avgDailyUsage",
                2.0,
                null
        ));

        // 规则3：周末用水异常 - 周末用水占比超过50%
        rules.add(new AnomalyRule(
                "WEEKEND_ABNORMAL",
                "周末用水偏高",
                "周末用水占比过高，用水习惯可能异常",
                AnomalyRule.RuleType.THRESHOLD,
                "weekendRatio",
                50.0,
                null
        ));
    }

    /**
     * 检测单个用户的用水异常
     *
     * @param feature  用水特征数据
     * @param avgStats 全局平均统计数据（用于倍数规则）
     * @return 检测到的异常列表
     */
    public List<AnomalyResult> detect(WaterFeatureData feature, GlobalStats avgStats) {
        List<AnomalyResult> anomalies = new ArrayList<>();

        for (AnomalyRule rule : rules) {
            if (!rule.isEnabled()) {
                continue;
            }

            AnomalyResult result = checkRule(rule, feature, avgStats);
            if (result != null) {
                anomalies.add(result);
            }
        }

        return anomalies;
    }

    /**
     * 批量检测用水异常
     *
     * @param features 用水特征数据列表
     * @return 所有检测到的异常列表
     */
    public List<AnomalyResult> detectBatch(List<WaterFeatureData> features) {
        List<AnomalyResult> allAnomalies = new ArrayList<>();

        // 计算全局统计数据
        GlobalStats avgStats = calculateGlobalStats(features);

        for (WaterFeatureData feature : features) {
            List<AnomalyResult> anomalies = detect(feature, avgStats);
            allAnomalies.addAll(anomalies);
        }

        return allAnomalies;
    }

    /**
     * 检查单条规则
     */
    private AnomalyResult checkRule(AnomalyRule rule, WaterFeatureData feature, GlobalStats avgStats) {
        Double value = getFeatureValue(feature, rule.getFeatureField());
        if (value == null) {
            return null;
        }

        boolean isAnomaly = false;
        String description = "";

        switch (rule.getRuleType()) {
            case THRESHOLD:
                // 阈值规则：值超过阈值则异常
                if (value > rule.getThreshold()) {
                    isAnomaly = true;
                    description = String.format("%s: %.2f%% > %.2f%%",
                            rule.getRuleName(), value, rule.getThreshold());
                }
                break;

            case MULTIPLIER:
                // 倍数规则：值超过平均值的N倍则异常
                Double avgValue = getAvgValue(avgStats, rule.getFeatureField());
                if (avgValue != null && avgValue > 0 && value > avgValue * rule.getThreshold()) {
                    isAnomaly = true;
                    description = String.format("%s: %.4f > 平均值%.4f的%.1f倍",
                            rule.getRuleName(), value, avgValue, rule.getThreshold());
                }
                break;

            case RANGE:
                // 范围规则：值不在指定范围内则异常
                if (rule.getMinValue() != null && value < rule.getMinValue()) {
                    isAnomaly = true;
                    description = String.format("%s: %.2f < 最小值%.2f",
                            rule.getRuleName(), value, rule.getMinValue());
                } else if (rule.getThreshold() != null && value > rule.getThreshold()) {
                    isAnomaly = true;
                    description = String.format("%s: %.2f > 最大值%.2f",
                            rule.getRuleName(), value, rule.getThreshold());
                }
                break;
        }

        if (isAnomaly) {
            AnomalyResult result = new AnomalyResult();
            result.setUserId(feature.getUserId());
            result.setUserName(feature.getUserName());
            result.setStatPeriod(feature.getStatPeriod());
            result.setRuleCode(rule.getRuleCode());
            result.setRuleName(rule.getRuleName());
            result.setAnomalyDescription(description);
            result.setFeatureField(rule.getFeatureField());
            result.setActualValue(value);
            result.setThresholdValue(rule.getThreshold());
            result.setSeverity(calculateSeverity(value, rule));
            return result;
        }

        return null;
    }

    /**
     * 获取特征值
     */
    private Double getFeatureValue(WaterFeatureData feature, String fieldName) {
        switch (fieldName) {
            case "avgDailyUsage":
                return feature.getAvgDailyUsage();
            case "totalUsage":
                return feature.getTotalUsage();
            case "dawnRatio":
                return feature.getDawnRatio();
            case "morningRatio":
                return feature.getMorningRatio();
            case "afternoonRatio":
                return feature.getAfternoonRatio();
            case "eveningRatio":
                return feature.getEveningRatio();
            case "weekendRatio":
                return feature.getWeekendRatio();
            case "usageCount":
                return feature.getUsageCount() != null ? feature.getUsageCount().doubleValue() : null;
            default:
                return null;
        }
    }

    /**
     * 获取全局平均值
     */
    private Double getAvgValue(GlobalStats stats, String fieldName) {
        if (stats == null) return null;
        switch (fieldName) {
            case "avgDailyUsage":
                return stats.getAvgDailyUsage();
            case "totalUsage":
                return stats.getAvgTotalUsage();
            case "dawnRatio":
                return stats.getAvgDawnRatio();
            case "weekendRatio":
                return stats.getAvgWeekendRatio();
            default:
                return null;
        }
    }

    /**
     * 计算严重程度（1-5级）
     */
    private int calculateSeverity(Double actualValue, AnomalyRule rule) {
        if (rule.getThreshold() == null || rule.getThreshold() == 0) {
            return 1;
        }
        double ratio = actualValue / rule.getThreshold();
        if (ratio > 3) return 5;
        if (ratio > 2.5) return 4;
        if (ratio > 2) return 3;
        if (ratio > 1.5) return 2;
        return 1;
    }

    /**
     * 计算全局统计数据
     */
    private GlobalStats calculateGlobalStats(List<WaterFeatureData> features) {
        if (features == null || features.isEmpty()) {
            return new GlobalStats();
        }

        GlobalStats stats = new GlobalStats();
        double sumDailyUsage = 0, sumTotalUsage = 0, sumDawnRatio = 0, sumWeekendRatio = 0;
        int count = features.size();

        for (WaterFeatureData f : features) {
            sumDailyUsage += f.getAvgDailyUsage() != null ? f.getAvgDailyUsage() : 0;
            sumTotalUsage += f.getTotalUsage() != null ? f.getTotalUsage() : 0;
            sumDawnRatio += f.getDawnRatio() != null ? f.getDawnRatio() : 0;
            sumWeekendRatio += f.getWeekendRatio() != null ? f.getWeekendRatio() : 0;
        }

        stats.setAvgDailyUsage(sumDailyUsage / count);
        stats.setAvgTotalUsage(sumTotalUsage / count);
        stats.setAvgDawnRatio(sumDawnRatio / count);
        stats.setAvgWeekendRatio(sumWeekendRatio / count);

        return stats;
    }

    // Getters and Setters

    public List<AnomalyRule> getRules() {
        return rules;
    }

    public void setRules(List<AnomalyRule> rules) {
        this.rules = rules;
    }

    public void addRule(AnomalyRule rule) {
        this.rules.add(rule);
    }

    public void removeRule(String ruleCode) {
        this.rules.removeIf(r -> r.getRuleCode().equals(ruleCode));
    }

    /**
     * 用水特征数据（用于异常检测的输入）
     */
    public static class WaterFeatureData implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String userName;
        private String statPeriod;
        private Double totalUsage;
        private Double avgDailyUsage;
        private Integer usageCount;
        private Double dawnRatio;
        private Double morningRatio;
        private Double afternoonRatio;
        private Double eveningRatio;
        private Double weekendRatio;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getStatPeriod() { return statPeriod; }
        public void setStatPeriod(String statPeriod) { this.statPeriod = statPeriod; }
        public Double getTotalUsage() { return totalUsage; }
        public void setTotalUsage(Double totalUsage) { this.totalUsage = totalUsage; }
        public Double getAvgDailyUsage() { return avgDailyUsage; }
        public void setAvgDailyUsage(Double avgDailyUsage) { this.avgDailyUsage = avgDailyUsage; }
        public Integer getUsageCount() { return usageCount; }
        public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
        public Double getDawnRatio() { return dawnRatio; }
        public void setDawnRatio(Double dawnRatio) { this.dawnRatio = dawnRatio; }
        public Double getMorningRatio() { return morningRatio; }
        public void setMorningRatio(Double morningRatio) { this.morningRatio = morningRatio; }
        public Double getAfternoonRatio() { return afternoonRatio; }
        public void setAfternoonRatio(Double afternoonRatio) { this.afternoonRatio = afternoonRatio; }
        public Double getEveningRatio() { return eveningRatio; }
        public void setEveningRatio(Double eveningRatio) { this.eveningRatio = eveningRatio; }
        public Double getWeekendRatio() { return weekendRatio; }
        public void setWeekendRatio(Double weekendRatio) { this.weekendRatio = weekendRatio; }
    }

    /**
     * 全局统计数据
     */
    public static class GlobalStats implements Serializable {
        private static final long serialVersionUID = 1L;

        private Double avgDailyUsage;
        private Double avgTotalUsage;
        private Double avgDawnRatio;
        private Double avgWeekendRatio;

        public Double getAvgDailyUsage() { return avgDailyUsage; }
        public void setAvgDailyUsage(Double avgDailyUsage) { this.avgDailyUsage = avgDailyUsage; }
        public Double getAvgTotalUsage() { return avgTotalUsage; }
        public void setAvgTotalUsage(Double avgTotalUsage) { this.avgTotalUsage = avgTotalUsage; }
        public Double getAvgDawnRatio() { return avgDawnRatio; }
        public void setAvgDawnRatio(Double avgDawnRatio) { this.avgDawnRatio = avgDawnRatio; }
        public Double getAvgWeekendRatio() { return avgWeekendRatio; }
        public void setAvgWeekendRatio(Double avgWeekendRatio) { this.avgWeekendRatio = avgWeekendRatio; }
    }

    /**
     * 异常检测结果
     */
    public static class AnomalyResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String userName;
        private String statPeriod;
        private String ruleCode;
        private String ruleName;
        private String anomalyDescription;
        private String featureField;
        private Double actualValue;
        private Double thresholdValue;
        private int severity; // 严重程度 1-5

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getStatPeriod() { return statPeriod; }
        public void setStatPeriod(String statPeriod) { this.statPeriod = statPeriod; }
        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getAnomalyDescription() { return anomalyDescription; }
        public void setAnomalyDescription(String anomalyDescription) { this.anomalyDescription = anomalyDescription; }
        public String getFeatureField() { return featureField; }
        public void setFeatureField(String featureField) { this.featureField = featureField; }
        public Double getActualValue() { return actualValue; }
        public void setActualValue(Double actualValue) { this.actualValue = actualValue; }
        public Double getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }
        public int getSeverity() { return severity; }
        public void setSeverity(int severity) { this.severity = severity; }
    }
}

