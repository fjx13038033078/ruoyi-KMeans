package com.ruoyi.web.controller.water;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.water.service.WaterStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 首页统计 Controller
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
@RestController
@RequestMapping("/water/statistics")
@RequiredArgsConstructor
public class WaterStatisticsController extends BaseController {

    private final WaterStatisticsService waterStatisticsService;

    /**
     * 获取首页统计数据
     * 根据用户角色返回不同的统计数据：
     * - 管理员(admin)/业务操作员(operator)：全局统计
     * - 普通用户(common)：个人统计
     *
     * @return 统计数据
     */
    @GetMapping("/home")
    public AjaxResult getHomeStatistics() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        List<SysRole> roles = user.getRoles();

        // 判断用户角色：admin、operator 为管理角色，common 为普通用户
        boolean isNormalUser = true;
        if (roles != null && !roles.isEmpty()) {
            for (SysRole role : roles) {
                String roleKey = role.getRoleKey();
                // admin 或 operator 角色为管理角色
                if ("admin".equals(roleKey) || "operator".equals(roleKey)) {
                    isNormalUser = false;
                    break;
                }
            }
        }

        // 超级管理员（userId=1）也不是普通用户
        if (user.isAdmin()) {
            isNormalUser = false;
        }

        Map<String, Object> statistics;
        if (isNormalUser) {
            // 普通用户：返回个人统计
            statistics = waterStatisticsService.getUserStatistics(user.getUserId());
            statistics.put("userType", "user");
        } else {
            // 管理员/业务操作员：返回全局统计
            statistics = waterStatisticsService.getGlobalStatistics();
            statistics.put("userType", "admin");
        }

        return AjaxResult.success(statistics);
    }

    /**
     * 获取全局统计数据（管理员/业务操作员专用）
     *
     * @return 全局统计数据
     */
    @GetMapping("/global")
    public AjaxResult getGlobalStatistics() {
        Map<String, Object> statistics = waterStatisticsService.getGlobalStatistics();
        return AjaxResult.success(statistics);
    }

    /**
     * 获取个人统计数据（普通用户专用）
     *
     * @return 个人统计数据
     */
    @GetMapping("/my")
    public AjaxResult getMyStatistics() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> statistics = waterStatisticsService.getUserStatistics(userId);
        return AjaxResult.success(statistics);
    }
}

