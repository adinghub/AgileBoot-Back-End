package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.push.AppPushDeviceAdminApplicationService;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeviceAdminDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeviceAdminDetailDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeviceStatisticsDTO;
import com.healthtrail.domain.health.push.query.HealthAppPushDeviceAdminQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 App 设备管理控制器。
 *
 * <p>该控制器回答的是“当前账号有哪些设备、这些设备最近是否活跃、最近推送表现如何”。
 * 当用户反馈“为什么没收到提醒”时，后台通常先从这里确认：
 * 1. 有没有设备；
 * 2. 设备是不是停用了；
 * 3. 设备最近有没有活跃；
 * 4. 最近 Push 到这台设备有没有成功。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/push/devices")
@Tag(name = "后台App设备管理API", description = "后台 App 设备活跃管理与设备状态排障接口")
public class HealthAppPushDeviceController extends BaseController {

    private final AppPushDeviceAdminApplicationService appPushDeviceAdminApplicationService;

    /**
     * 分页查询设备列表。
     */
    @Operation(summary = "设备管理列表")
    @PreAuthorize("@permission.has('health:push:device:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthAppPushDeviceAdminDTO>> list(HealthAppPushDeviceAdminQuery query) {
        if (query.getTimeRangeColumn() == null) {
            query.setTimeRangeColumn("lastActiveTime");
        }
        return ResponseDTO.ok(appPushDeviceAdminApplicationService.getDeviceList(query));
    }

    /**
     * 查询设备统计概览。
     */
    @Operation(summary = "设备管理统计")
    @PreAuthorize("@permission.has('health:push:device:statistics')")
    @GetMapping("/statistics")
    public ResponseDTO<HealthAppPushDeviceStatisticsDTO> statistics(HealthAppPushDeviceAdminQuery query) {
        if (query.getTimeRangeColumn() == null) {
            query.setTimeRangeColumn("lastActiveTime");
        }
        return ResponseDTO.ok(appPushDeviceAdminApplicationService.getDeviceStatistics(query));
    }

    /**
     * 查询设备详情。
     */
    @Operation(summary = "设备管理详情")
    @PreAuthorize("@permission.has('health:push:device:detail')")
    @GetMapping("/{deviceId}")
    public ResponseDTO<HealthAppPushDeviceAdminDetailDTO> getInfo(
        @PathVariable @NotNull @Positive Long deviceId) {
        return ResponseDTO.ok(appPushDeviceAdminApplicationService.getDeviceDetail(deviceId));
    }

    /**
     * 停用设备。
     *
     * <p>该操作主要用于后台明确确认某台设备已经无效，
     * 希望后续不再继续参与 Push 派发时使用。
     */
    @Operation(summary = "停用设备")
    @PreAuthorize("@permission.has('health:push:device:disable')")
    @AccessLog(title = "停用App设备", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{deviceId}/disable")
    public ResponseDTO<String> disable(@PathVariable @NotNull @Positive Long deviceId) {
        appPushDeviceAdminApplicationService.disableDevice(deviceId);
        return ResponseDTO.ok("设备已停用");
    }
}
