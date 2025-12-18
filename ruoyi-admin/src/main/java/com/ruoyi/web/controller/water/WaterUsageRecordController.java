package com.ruoyi.web.controller.water;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.water.domain.WaterUsageRecord;
import com.ruoyi.water.service.WaterUsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用水记录管理 Controller
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@RestController
@RequestMapping("/water/record")
@RequiredArgsConstructor
public class WaterUsageRecordController extends BaseController {

    private final WaterUsageRecordService waterUsageRecordService;

    /**
     * 查询用水记录列表（业务操作员查看所有）
     *
     * @param waterUsageRecord 查询条件
     * @return 用水记录列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(WaterUsageRecord waterUsageRecord) {
        startPage();
        List<WaterUsageRecord> list = waterUsageRecordService.selectWaterUsageRecordList(waterUsageRecord);
        return getDataTable(list);
    }

    /**
     * 获取用水记录详情
     *
     * @param recordId 记录ID
     * @return 用水记录
     */
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Long recordId) {
        return AjaxResult.success(waterUsageRecordService.selectWaterUsageRecordById(recordId));
    }

    /**
     * 获取当前用户的用水记录列表
     *
     * @param waterUsageRecord 查询条件
     * @return 用水记录列表（分页）
     */
    @GetMapping("/myRecords")
    public TableDataInfo getMyRecords(WaterUsageRecord waterUsageRecord) {
        startPage();
        // 强制设置为当前登录用户的ID，确保只能查询自己的记录
        waterUsageRecord.setUserId(SecurityUtils.getUserId());
        List<WaterUsageRecord> list = waterUsageRecordService.selectWaterUsageRecordList(waterUsageRecord);
        return getDataTable(list);
    }

    /**
     * 新增用水记录（普通用户录入自己的用水记录）
     *
     * @param waterUsageRecord 用水记录
     * @return 操作结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody WaterUsageRecord waterUsageRecord) {
        // 设置当前用户ID
        waterUsageRecord.setUserId(SecurityUtils.getUserId());
        waterUsageRecord.setCreateBy(SecurityUtils.getUsername());
        return toAjax(waterUsageRecordService.insertWaterUsageRecord(waterUsageRecord));
    }

    /**
     * 修改用水记录
     *
     * @param waterUsageRecord 用水记录
     * @return 操作结果
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody WaterUsageRecord waterUsageRecord) {
        waterUsageRecord.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(waterUsageRecordService.updateWaterUsageRecord(waterUsageRecord));
    }

    /**
     * 删除用水记录
     *
     * @param recordId 记录ID
     * @return 操作结果
     */
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam Long recordId) {
        return toAjax(waterUsageRecordService.deleteWaterUsageRecordById(recordId));
    }

    /**
     * 批量删除用水记录
     *
     * @param recordIds 记录ID数组
     * @return 操作结果
     */
    @PostMapping("/deleteBatch")
    public AjaxResult deleteBatch(@RequestBody Long[] recordIds) {
        return toAjax(waterUsageRecordService.deleteWaterUsageRecordByIds(recordIds));
    }

    /**
     * 查询用水类型分布统计
     *
     * @param userId 用户ID（可选，不传则统计所有用户）
     * @return 类型分布统计
     */
    @GetMapping("/typeStatistics")
    public AjaxResult getUsageTypeStatistics(@RequestParam(required = false) Long userId) {
        List<Map<String, Object>> statistics = waterUsageRecordService.selectUsageTypeStatistics(userId);
        return AjaxResult.success(statistics);
    }

    /**
     * 查询时段分布统计
     *
     * @param userId 用户ID（可选，不传则统计所有用户）
     * @return 时段分布统计
     */
    @GetMapping("/timePeriodStatistics")
    public AjaxResult getTimePeriodStatistics(@RequestParam(required = false) Long userId) {
        List<Map<String, Object>> statistics = waterUsageRecordService.selectTimePeriodStatistics(userId);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取当前用户的用水类型分布统计
     *
     * @return 类型分布统计
     */
    @GetMapping("/myTypeStatistics")
    public AjaxResult getMyUsageTypeStatistics() {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> statistics = waterUsageRecordService.selectUsageTypeStatistics(userId);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取当前用户的时段分布统计
     *
     * @return 时段分布统计
     */
    @GetMapping("/myTimePeriodStatistics")
    public AjaxResult getMyTimePeriodStatistics() {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> statistics = waterUsageRecordService.selectTimePeriodStatistics(userId);
        return AjaxResult.success(statistics);
    }
}

