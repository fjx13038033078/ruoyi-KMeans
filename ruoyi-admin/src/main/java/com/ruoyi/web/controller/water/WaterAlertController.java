package com.ruoyi.web.controller.water;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.water.domain.WaterAnomalyRecord;
import com.ruoyi.water.service.WaterAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用水告警 Controller（普通用户使用）
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@RestController
@RequestMapping("/water/alert")
@RequiredArgsConstructor
public class WaterAlertController extends BaseController {

    private final WaterAlertService waterAlertService;

    /**
     * 查询我的告警列表
     *
     * @param waterAnomalyRecord 查询条件
     * @return 告警列表（分页）
     */
    @GetMapping("/myList")
    public TableDataInfo myList(WaterAnomalyRecord waterAnomalyRecord) {
        startPage();
        List<WaterAnomalyRecord> list = waterAlertService.selectMyAlertList(waterAnomalyRecord);
        return getDataTable(list);
    }

    /**
     * 获取未读告警数量
     *
     * @return 未读数量
     */
    @GetMapping("/unreadCount")
    public AjaxResult getUnreadCount() {
        int count = waterAlertService.getUnreadCount();
        return AjaxResult.success(count);
    }

    /**
     * 获取告警详情
     *
     * @param anomalyId 告警ID
     * @return 告警详情
     */
    @GetMapping("/detail/{anomalyId}")
    public AjaxResult getDetail(@PathVariable Long anomalyId) {
        WaterAnomalyRecord record = waterAlertService.getAlertDetail(anomalyId);
        if (record == null) {
            return AjaxResult.error("告警不存在或无权查看");
        }
        return AjaxResult.success(record);
    }

    /**
     * 标记告警为已读
     *
     * @param anomalyId 告警ID
     * @return 操作结果
     */
    @PostMapping("/read/{anomalyId}")
    public AjaxResult markAsRead(@PathVariable Long anomalyId) {
        return toAjax(waterAlertService.markAsRead(anomalyId));
    }

    /**
     * 全部标记为已读
     *
     * @return 操作结果
     */
    @PostMapping("/readAll")
    public AjaxResult markAllAsRead() {
        int count = waterAlertService.markAllAsRead();
        return AjaxResult.success("已将 " + count + " 条告警标记为已读");
    }

    /**
     * 提交用户反馈
     *
     * @param anomalyId      告警ID
     * @param userFeedback   用户反馈（1-确认属实，2-认为误报）
     * @param feedbackRemark 反馈说明
     * @return 操作结果
     */
    @PostMapping("/feedback")
    public AjaxResult submitFeedback(@RequestParam Long anomalyId,
                                     @RequestParam Integer userFeedback,
                                     @RequestParam(required = false) String feedbackRemark) {
        if (userFeedback != 1 && userFeedback != 2) {
            return AjaxResult.error("无效的反馈类型");
        }
        return toAjax(waterAlertService.submitFeedback(anomalyId, userFeedback, feedbackRemark));
    }
}

