package com.healthtrail.admin.controller.health;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.health.push.AppPushDeliveryApplicationService;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryAdminDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryAdminDetailDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryStatisticsDTO;
import com.healthtrail.domain.health.push.query.HealthAppPushDeliveryAdminQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 App Push 设备级派发审计控制器。
 *
 * <p>消息审计页更适合看“整条消息最近一次整体结果”，
 * 而这里更适合看“到底命中了哪些设备、每台设备结果怎样、失败原因是什么”。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/push/deliveries")
@Tag(name = "后台App Push派发审计API", description = "后台 App Push 设备级派发审计与统计接口")
public class HealthAppPushDeliveryController extends BaseController {

    private final AppPushDeliveryApplicationService appPushDeliveryApplicationService;

    /**
     * 分页查询设备级派发审计列表。
     */
    @Operation(summary = "Push派发审计列表")
    @PreAuthorize("@permission.has('health:push:delivery:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthAppPushDeliveryAdminDTO>> list(HealthAppPushDeliveryAdminQuery query) {
        if (query.getTimeRangeColumn() == null) {
            query.setTimeRangeColumn("sendTime");
        }
        return ResponseDTO.ok(appPushDeliveryApplicationService.getDeliveryList(query));
    }

    /**
     * 查询设备级派发审计统计。
     */
    @Operation(summary = "Push派发审计统计")
    @PreAuthorize("@permission.has('health:push:delivery:statistics')")
    @GetMapping("/statistics")
    public ResponseDTO<HealthAppPushDeliveryStatisticsDTO> statistics(HealthAppPushDeliveryAdminQuery query) {
        if (query.getTimeRangeColumn() == null) {
            query.setTimeRangeColumn("sendTime");
        }
        return ResponseDTO.ok(appPushDeliveryApplicationService.getDeliveryStatistics(query));
    }

    /**
     * 查询设备级派发审计详情。
     */
    @Operation(summary = "Push派发审计详情")
    @PreAuthorize("@permission.has('health:push:delivery:detail')")
    @GetMapping("/{deliveryId}")
    public ResponseDTO<HealthAppPushDeliveryAdminDetailDTO> getInfo(
        @PathVariable @NotNull @Positive Long deliveryId) {
        return ResponseDTO.ok(appPushDeliveryApplicationService.getDeliveryDetail(deliveryId));
    }
}
