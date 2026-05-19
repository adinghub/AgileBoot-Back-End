package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.dashboard.HealthDashboardApplicationService;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeDashboardDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 首页健康看板控制器。
 *
 * <p>首页看板的定位不是替代各业务模块详情接口，
 * 而是给 App 首页提供“一次请求拿到核心概览数据”的聚合入口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/dashboard")
@Tag(name = "首页健康看板API", description = "App端首页健康看板接口")
public class HealthDashboardController extends BaseController {

    private final HealthDashboardApplicationService healthDashboardApplicationService;

    /**
     * 获取当前登录用户的首页健康看板。
     */
    @Operation(summary = "首页健康看板")
    @GetMapping("/home")
    public ResponseDTO<HealthHomeDashboardDTO> home() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthDashboardApplicationService.getHomeDashboard(currentUserId));
    }
}
