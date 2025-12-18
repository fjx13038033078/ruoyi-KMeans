package com.ruoyi.web.controller.water;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.water.domain.WaterHousehold;
import com.ruoyi.water.service.WaterHouseholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住户信息管理 Controller
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@RestController
@RequestMapping("/water/household")
@RequiredArgsConstructor
public class WaterHouseholdController extends BaseController {

    private final WaterHouseholdService waterHouseholdService;

    /**
     * 查询住户信息列表
     *
     * @param waterHousehold 查询条件
     * @return 住户信息列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(WaterHousehold waterHousehold) {
        startPage();
        List<WaterHousehold> list = waterHouseholdService.selectWaterHouseholdList(waterHousehold);
        return getDataTable(list);
    }

    /**
     * 获取住户信息详情
     *
     * @param householdId 住户ID
     * @return 住户信息
     */
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Long householdId) {
        return AjaxResult.success(waterHouseholdService.selectWaterHouseholdById(householdId));
    }

    /**
     * 获取当前用户的住户信息
     *
     * @return 住户信息
     */
    @GetMapping("/myInfo")
    public AjaxResult getMyHouseholdInfo() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(waterHouseholdService.selectWaterHouseholdByUserId(userId));
    }

    /**
     * 新增住户信息
     *
     * @param waterHousehold 住户信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody WaterHousehold waterHousehold) {
        waterHousehold.setCreateBy(SecurityUtils.getUsername());
        return toAjax(waterHouseholdService.insertWaterHousehold(waterHousehold));
    }

    /**
     * 修改住户信息
     *
     * @param waterHousehold 住户信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody WaterHousehold waterHousehold) {
        waterHousehold.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(waterHouseholdService.updateWaterHousehold(waterHousehold));
    }

    /**
     * 删除住户信息
     *
     * @param householdId 住户ID
     * @return 操作结果
     */
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam Long householdId) {
        return toAjax(waterHouseholdService.deleteWaterHouseholdById(householdId));
    }

    /**
     * 批量删除住户信息
     *
     * @param householdIds 住户ID数组
     * @return 操作结果
     */
    @PostMapping("/deleteBatch")
    public AjaxResult deleteBatch(@RequestBody Long[] householdIds) {
        return toAjax(waterHouseholdService.deleteWaterHouseholdByIds(householdIds));
    }

    /**
     * 更新住户状态
     *
     * @param householdId 住户ID
     * @param status 状态
     * @return 操作结果
     */
    @GetMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestParam Long householdId, @RequestParam Integer status) {
        return toAjax(waterHouseholdService.updateStatus(householdId, status));
    }
}

