package com.healthtrail.domain.health.message.notify;

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
 * 基于 App 设备 Token 的消息中心发送器。
 *
 * <p>该发送器专门服务于“消息中心维度”的人工重发，
 * 发送内容直接来自消息表里的标题、正文和业务透传载荷，
 * 不再依赖原始提醒或报告对象重新组装。
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class AppPushHealthAppMessageNotifier implements HealthAppMessageNotifier {

    /** App设备数据库服务 */
    private final HealthAppDeviceService appDeviceService;

    /** 外部Push网关客户端 */
    private final HealthExternalPushGatewayClient externalPushGatewayClient;

    /** Push派发审计应用服务 */
    private final AppPushDeliveryApplicationService appPushDeliveryApplicationService;

    /**
     * 根据当前用户启用中的设备列表派发消息。
     */
    @Override
    public HealthAppMessageNotifyResult notify(HealthAppMessageNotice notice) {
        List<HealthAppDeviceEntity> activeDevices = appDeviceService.listActiveDevices(notice.getOwnerUserId())
            .stream()
            .filter(device -> device.getStatus() != null && device.getStatus().equals(StatusEnum.ENABLE.getValue()))
            .filter(device -> StrUtil.isNotBlank(device.getDeviceToken()))
            .collect(Collectors.toList());

        if (activeDevices.isEmpty()) {
            return HealthAppMessageNotifyResult.builder()
                .success(false)
                .channel("APP_PUSH")
                .message(AppPushAuditSupport.NO_ACTIVE_DEVICE_TOKEN_MESSAGE)
                .build();
        }

        String pushContent = buildPushContent(notice);
        if (externalPushGatewayClient.isReady()) {
            HealthExternalPushGatewayResult gatewayResult = externalPushGatewayClient.pushBatch(activeDevices,
                notice.getMessageTitle(), pushContent, notice.getPayload());
            appPushDeliveryApplicationService.recordBatchDelivery(
                notice.getMessageId(),
                notice.getOwnerUserId(),
                notice.getMemberId(),
                notice.getBusinessScene(),
                notice.getBusinessId(),
                activeDevices,
                gatewayResult.getChannel(),
                gatewayResult.isSuccess(),
                gatewayResult.getMessage(),
                gatewayResult.getVendorCode(),
                gatewayResult.getVendorMessage());
            return HealthAppMessageNotifyResult.builder()
                .success(gatewayResult.isSuccess())
                .channel(gatewayResult.getChannel())
                .message(gatewayResult.getMessage())
                .build();
        }

        String pushPayload = notice.getPayload() == null ? "{}" : JacksonUtil.to(notice.getPayload());
        activeDevices.forEach(device -> log.info(
            "模拟后台人工重发消息Push成功，消息ID：{}，用户ID：{}，设备编码：{}，平台：{}，Token：{}，标题：{}，内容：{}，透传载荷：{}",
            notice.getMessageId(), notice.getOwnerUserId(), device.getDeviceCode(), device.getPushPlatform(),
            device.getDeviceToken(), notice.getMessageTitle(), pushContent, pushPayload));
        appPushDeliveryApplicationService.recordBatchDelivery(
            notice.getMessageId(),
            notice.getOwnerUserId(),
            notice.getMemberId(),
            notice.getBusinessScene(),
            notice.getBusinessId(),
            activeDevices,
            "APP_PUSH",
            true,
            "已派发到" + activeDevices.size() + "个设备",
            null,
            "模拟发送");

        return HealthAppMessageNotifyResult.builder()
            .success(true)
            .channel("APP_PUSH")
            .message("已派发到" + activeDevices.size() + "个设备")
            .build();
    }

    /**
     * 组装 Push 文案。
     */
    private String buildPushContent(HealthAppMessageNotice notice) {
        if (StrUtil.isNotBlank(notice.getMessageContent())) {
            return notice.getMessageContent();
        }
        return StrUtil.blankToDefault(notice.getMessageTitle(), "您有一条新的健康消息");
    }
}
