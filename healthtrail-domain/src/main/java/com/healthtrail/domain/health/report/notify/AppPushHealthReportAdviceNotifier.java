package com.healthtrail.domain.health.report.notify;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import com.healthtrail.domain.health.device.db.HealthAppDeviceService;
import com.healthtrail.domain.health.device.push.HealthExternalPushGatewayClient;
import com.healthtrail.domain.health.device.push.HealthExternalPushGatewayResult;
import com.healthtrail.domain.health.push.AppPushAuditSupport;
import com.healthtrail.domain.health.push.AppPushDeliveryApplicationService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 基于 App 设备 Token 的报告建议通知发送器。
 *
 * <p>当前阶段与提醒 Push 的处理方式保持一致：
 * 1. 先读取当前用户活跃设备
 * 2. 再按设备 Token 做逐条发送
 * 3. 当前仍用日志模拟“发往具体设备”，为后续接厂商 SDK 预留位置
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class AppPushHealthReportAdviceNotifier implements HealthReportAdviceNotifier {

    /** App设备数据库服务 */
    private final HealthAppDeviceService appDeviceService;

    /** 外部Push网关客户端 */
    private final HealthExternalPushGatewayClient externalPushGatewayClient;

    /** Push派发审计应用服务 */
    private final AppPushDeliveryApplicationService appPushDeliveryApplicationService;

    /**
     * 根据当前用户启用中的设备列表派发报告建议通知。
     */
    @Override
    public HealthReportAdviceNotifyResult notify(HealthReportAdviceNotice notice) {
        List<HealthAppDeviceEntity> activeDevices = appDeviceService.listActiveDevices(notice.getOwnerUserId())
            .stream()
            .filter(device -> device.getStatus() != null && device.getStatus().equals(StatusEnum.ENABLE.getValue()))
            .filter(device -> StrUtil.isNotBlank(device.getDeviceToken()))
            .collect(Collectors.toList());

        if (activeDevices.isEmpty()) {
            return HealthReportAdviceNotifyResult.builder()
                .success(false)
                .channel("APP_PUSH")
                .message(AppPushAuditSupport.NO_ACTIVE_DEVICE_TOKEN_MESSAGE)
                .build();
        }

        String pushContent = buildPushContent(notice);
        if (externalPushGatewayClient.isReady()) {
            HealthExternalPushGatewayResult gatewayResult = externalPushGatewayClient.pushBatch(activeDevices,
                notice.getPushPayload() == null ? null : notice.getPushPayload().getTitle(),
                pushContent, notice.getPushPayload());
            appPushDeliveryApplicationService.recordBatchDelivery(
                notice.getMessageId(),
                notice.getOwnerUserId(),
                notice.getMemberId(),
                "REPORT_FOLLOW_UP",
                notice.getReportId(),
                activeDevices,
                gatewayResult.getChannel(),
                gatewayResult.isSuccess(),
                gatewayResult.getMessage(),
                gatewayResult.getVendorCode(),
                gatewayResult.getVendorMessage());
            return HealthReportAdviceNotifyResult.builder()
                .success(gatewayResult.isSuccess())
                .channel(gatewayResult.getChannel())
                .message(gatewayResult.getMessage())
                .build();
        }

        String pushPayload = notice.getPushPayload() == null ? "{}" : JacksonUtil.to(notice.getPushPayload());
        activeDevices.forEach(device -> log.info(
            "模拟发送报告建议设备Push成功，报告ID：{}，用户ID：{}，设备编码：{}，平台：{}，Token：{}，内容：{}，透传载荷：{}",
            notice.getReportId(), notice.getOwnerUserId(), device.getDeviceCode(), device.getPushPlatform(),
            device.getDeviceToken(), pushContent, pushPayload));
        appPushDeliveryApplicationService.recordBatchDelivery(
            notice.getMessageId(),
            notice.getOwnerUserId(),
            notice.getMemberId(),
            "REPORT_FOLLOW_UP",
            notice.getReportId(),
            activeDevices,
            "APP_PUSH",
            true,
            "已派发到" + activeDevices.size() + "个设备",
            null,
            "模拟发送");

        return HealthReportAdviceNotifyResult.builder()
            .success(true)
            .channel("APP_PUSH")
            .message("已派发到" + activeDevices.size() + "个设备")
            .build();
    }

    /**
     * 组装 Push 文案。
     */
    private String buildPushContent(HealthReportAdviceNotice notice) {
        if (notice.getPushPayload() != null && StrUtil.isNotBlank(notice.getPushPayload().getContent())) {
            return notice.getPushPayload().getContent();
        }
        String memberName = StrUtil.blankToDefault(notice.getMemberName(), "家庭成员");
        return memberName + " 有新的报告建议待跟进";
    }
}
