package com.ruoyi.web.controller.water;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.kmeans.AnomalyRule;
import com.ruoyi.common.kmeans.ClusterResult;
import com.ruoyi.common.kmeans.KMeansConfig;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.water.domain.WaterAnomalyRecord;
import com.ruoyi.water.domain.WaterUsageFeature;
import com.ruoyi.water.service.WaterClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用水聚类分析 Controller
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@RestController
@RequestMapping("/water/cluster")
@RequiredArgsConstructor
public class WaterClusterController extends BaseController {

    private final WaterClusterService waterClusterService;

    // ==================== 特征数据生成 ====================

    /**
     * 生成用水特征数据
     *
     * @param statPeriod 统计周期（如：2024-01）
     * @return 生成结果
     */
    @GetMapping("/generateFeature")
    public AjaxResult generateFeatureData(@RequestParam String statPeriod) {
        int count = waterClusterService.generateFeatureData(statPeriod);
        if (count > 0) {
            return AjaxResult.success("特征数据生成成功，共生成 " + count + " 条记录");
        } else {
            return AjaxResult.error("特征数据生成失败，该周期没有用水记录数据");
        }
    }

    // ==================== K-Means聚类分析 ====================

    /**
     * 执行K-means聚类分析
     *
     * @param statPeriod 统计周期
     * @param k          聚类数量（默认3）
     * @return 聚类结果
     */
    @GetMapping("/execute")
    public AjaxResult executeKMeansClustering(@RequestParam String statPeriod,
                                               @RequestParam(defaultValue = "3") Integer k) {
        ClusterResult result = waterClusterService.executeKMeansClustering(statPeriod, k);
        if (result != null) {
            return AjaxResult.success("聚类分析执行成功", result);
        } else {
            return AjaxResult.error("聚类分析执行失败，特征数据不足");
        }
    }

    /**
     * 执行K-means聚类分析（使用自定义配置）
     *
     * @param statPeriod 统计周期
     * @param config     聚类配置
     * @return 聚类结果
     */
    @PostMapping("/executeWithConfig")
    public AjaxResult executeWithConfig(@RequestParam String statPeriod,
                                         @RequestBody KMeansConfig config) {
        ClusterResult result = waterClusterService.executeKMeansClustering(statPeriod, config);
        if (result != null) {
            return AjaxResult.success("聚类分析执行成功", result);
        } else {
            return AjaxResult.error("聚类分析执行失败，特征数据不足");
        }
    }

    /**
     * 获取聚类配置
     *
     * @return 当前聚类配置
     */
    @GetMapping("/config")
    public AjaxResult getClusterConfig() {
        return AjaxResult.success(waterClusterService.getClusterConfig());
    }

    /**
     * 更新聚类配置
     *
     * @param config 新的聚类配置
     * @return 操作结果
     */
    @PostMapping("/config")
    public AjaxResult updateClusterConfig(@RequestBody KMeansConfig config) {
        waterClusterService.updateClusterConfig(config);
        return AjaxResult.success("聚类配置更新成功");
    }

    /**
     * 获取聚类结果
     *
     * @param statPeriod 统计周期
     * @return 聚类结果列表
     */
    @GetMapping("/result")
    public TableDataInfo getClusterResult(@RequestParam String statPeriod) {
        startPage();
        List<WaterUsageFeature> list = waterClusterService.getClusterResultByPeriod(statPeriod);
        return getDataTable(list);
    }

    /**
     * 获取聚类统计信息
     *
     * @param statPeriod 统计周期
     * @return 聚类统计列表
     */
    @GetMapping("/statistics")
    public AjaxResult getClusterStatistics(@RequestParam String statPeriod) {
        List<WaterUsageFeature> list = waterClusterService.getClusterStatistics(statPeriod);
        return AjaxResult.success(list);
    }

    // ==================== 异常检测 ====================

    /**
     * 执行异常检测
     *
     * @param statPeriod 统计周期
     * @return 检测结果
     */
    @GetMapping("/detectAnomaly")
    public AjaxResult executeAnomalyDetection(@RequestParam String statPeriod) {
        int count = waterClusterService.executeAnomalyDetection(statPeriod);
        return AjaxResult.success("异常检测完成，检测到 " + count + " 条异常");
    }

    /**
     * 获取异常检测规则列表
     *
     * @return 规则列表
     */
    @GetMapping("/rules")
    public AjaxResult getAnomalyRules() {
        return AjaxResult.success(waterClusterService.getAnomalyRules());
    }

    /**
     * 更新异常检测规则列表
     *
     * @param rules 规则列表
     * @return 操作结果
     */
    @PostMapping("/rules")
    public AjaxResult updateAnomalyRules(@RequestBody List<AnomalyRule> rules) {
        waterClusterService.updateAnomalyRules(rules);
        return AjaxResult.success("异常检测规则更新成功");
    }

    /**
     * 添加异常检测规则
     *
     * @param rule 规则
     * @return 操作结果
     */
    @PostMapping("/rules/add")
    public AjaxResult addAnomalyRule(@RequestBody AnomalyRule rule) {
        waterClusterService.addAnomalyRule(rule);
        return AjaxResult.success("异常检测规则添加成功");
    }

    /**
     * 删除异常检测规则
     *
     * @param ruleCode 规则编码
     * @return 操作结果
     */
    @GetMapping("/rules/delete")
    public AjaxResult removeAnomalyRule(@RequestParam String ruleCode) {
        waterClusterService.removeAnomalyRule(ruleCode);
        return AjaxResult.success("异常检测规则删除成功");
    }

    // ==================== 异常记录管理 ====================

    /**
     * 查询异常记录列表
     *
     * @param waterAnomalyRecord 查询条件
     * @return 异常记录列表（分页）
     */
    @GetMapping("/anomaly/list")
    public TableDataInfo listAnomalyRecords(WaterAnomalyRecord waterAnomalyRecord) {
        startPage();
        List<WaterAnomalyRecord> list = waterClusterService.selectAnomalyRecordList(waterAnomalyRecord);
        return getDataTable(list);
    }

    /**
     * 处理异常记录
     *
     * @param anomalyId    异常记录ID
     * @param status       处理状态（1-已处理,2-已忽略）
     * @param handleRemark 处理意见
     * @return 操作结果
     */
    @GetMapping("/anomaly/handle")
    public AjaxResult handleAnomalyRecord(@RequestParam Long anomalyId,
                                           @RequestParam Integer status,
                                           @RequestParam(required = false) String handleRemark) {
        String handleBy = SecurityUtils.getUsername();
        boolean result = waterClusterService.handleAnomalyRecord(anomalyId, status, handleBy, handleRemark);
        return toAjax(result);
    }

    // ==================== 一键分析 ====================

    /**
     * 一键分析（生成特征 + 聚类 + 异常检测）
     *
     * @param statPeriod 统计周期
     * @param k          聚类数量（默认3）
     * @return 分析结果
     */
    @GetMapping("/analyzeAll")
    public AjaxResult analyzeAll(@RequestParam String statPeriod,
                                  @RequestParam(defaultValue = "3") Integer k) {
        StringBuilder resultMsg = new StringBuilder();

        // 1. 生成特征数据
        int featureCount = waterClusterService.generateFeatureData(statPeriod);
        resultMsg.append("特征数据生成完成，共 ").append(featureCount).append(" 条；");

        if (featureCount == 0) {
            return AjaxResult.error("分析失败：该周期没有用水记录数据");
        }

        // 2. 执行聚类分析
        ClusterResult clusterResult = waterClusterService.executeKMeansClustering(statPeriod, k);
        if (clusterResult != null) {
            resultMsg.append("聚类分析完成，K=").append(k)
                    .append("，迭代").append(clusterResult.getIterations()).append("次；");
        } else {
            resultMsg.append("聚类分析跳过（数据不足）；");
        }

        // 3. 执行异常检测
        int anomalyCount = waterClusterService.executeAnomalyDetection(statPeriod);
        resultMsg.append("异常检测完成，发现 ").append(anomalyCount).append(" 条异常。");

        return AjaxResult.success(resultMsg.toString());
    }
}

