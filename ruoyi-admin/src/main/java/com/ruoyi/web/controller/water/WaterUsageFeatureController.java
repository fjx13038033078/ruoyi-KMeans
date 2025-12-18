package com.ruoyi.web.controller.water;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.water.domain.WaterUsageFeature;
import com.ruoyi.water.service.WaterUsageFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用水特征分析（K-means聚类） Controller
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@RestController
@RequestMapping("/water/feature")
@RequiredArgsConstructor
public class WaterUsageFeatureController extends BaseController {

    private final WaterUsageFeatureService waterUsageFeatureService;

    /**
     * 查询用水特征列表（业务操作员查看）
     *
     * @param waterUsageFeature 查询条件
     * @return 用水特征列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(WaterUsageFeature waterUsageFeature) {
        startPage();
        List<WaterUsageFeature> list = waterUsageFeatureService.selectWaterUsageFeatureList(waterUsageFeature);
        return getDataTable(list);
    }

    /**
     * 获取用水特征详情
     *
     * @param featureId 特征ID
     * @return 用水特征
     */
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Long featureId) {
        return AjaxResult.success(waterUsageFeatureService.selectWaterUsageFeatureById(featureId));
    }

    /**
     * 根据统计周期查询所有用水特征
     *
     * @param statPeriod 统计周期（如：2024-01）
     * @return 用水特征列表
     */
    @GetMapping("/listByPeriod")
    public AjaxResult listByPeriod(@RequestParam String statPeriod) {
        List<WaterUsageFeature> list = waterUsageFeatureService.selectWaterUsageFeatureByPeriod(statPeriod);
        return AjaxResult.success(list);
    }

    /**
     * 获取当前用户的用水特征
     *
     * @param statPeriod 统计周期
     * @return 用水特征
     */
    @GetMapping("/myFeature")
    public AjaxResult getMyFeature(@RequestParam String statPeriod) {
        Long userId = SecurityUtils.getUserId();
        WaterUsageFeature feature = waterUsageFeatureService.selectWaterUsageFeatureByUserAndPeriod(userId, statPeriod);
        return AjaxResult.success(feature);
    }

    /**
     * 生成用水特征数据（从用水记录表聚合）
     *
     * @param statPeriod 统计周期（如：2024-01）
     * @return 操作结果
     */
    @GetMapping("/generate")
    public AjaxResult generateFeatureData(@RequestParam String statPeriod) {
        boolean result = waterUsageFeatureService.generateFeatureData(statPeriod);
        if (result) {
            return AjaxResult.success("用水特征数据生成成功");
        } else {
            return AjaxResult.error("用水特征数据生成失败，可能没有该周期的用水记录");
        }
    }

    /**
     * 执行K-means聚类分析
     *
     * @param statPeriod 统计周期
     * @param k 聚类数量（默认为3）
     * @return 操作结果
     */
    @GetMapping("/cluster")
    public AjaxResult executeKMeansClustering(@RequestParam String statPeriod,
                                               @RequestParam(defaultValue = "3") Integer k) {
        boolean result = waterUsageFeatureService.executeKMeansClustering(statPeriod, k);
        if (result) {
            return AjaxResult.success("K-means聚类分析执行成功");
        } else {
            return AjaxResult.error("K-means聚类分析执行失败，可能特征数据不足");
        }
    }

    /**
     * 根据聚类ID查询用水特征列表
     *
     * @param clusterId 聚类簇ID
     * @param statPeriod 统计周期
     * @return 用水特征列表
     */
    @GetMapping("/listByCluster")
    public AjaxResult listByClusterId(@RequestParam Integer clusterId,
                                       @RequestParam String statPeriod) {
        List<WaterUsageFeature> list = waterUsageFeatureService.selectWaterUsageFeatureByClusterId(clusterId, statPeriod);
        return AjaxResult.success(list);
    }

    /**
     * 查询聚类统计信息（各聚类的平均特征）
     *
     * @param statPeriod 统计周期
     * @return 聚类统计列表
     */
    @GetMapping("/clusterStatistics")
    public AjaxResult getClusterStatistics(@RequestParam String statPeriod) {
        List<WaterUsageFeature> list = waterUsageFeatureService.selectClusterStatistics(statPeriod);
        return AjaxResult.success(list);
    }

    /**
     * 删除用水特征
     *
     * @param featureId 特征ID
     * @return 操作结果
     */
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam Long featureId) {
        return toAjax(waterUsageFeatureService.deleteWaterUsageFeatureById(featureId));
    }

    /**
     * 批量删除用水特征
     *
     * @param featureIds 特征ID数组
     * @return 操作结果
     */
    @PostMapping("/deleteBatch")
    public AjaxResult deleteBatch(@RequestBody Long[] featureIds) {
        return toAjax(waterUsageFeatureService.deleteWaterUsageFeatureByIds(featureIds));
    }
}

