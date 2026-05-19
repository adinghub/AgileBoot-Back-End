package com.healthtrail.domain.health.medication.notify;

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
 * 基于 App 设备 Token 的用药提醒发送器。
 *
 * <p>这是当前阶段更接近真实业务的 Push 发送器：
 * 1. 先从设备表读取当前用户的活跃设备
 * 2. 再按设备 Token 做逐条发送
 * 3. 当前先用日志模拟“发往具体设备”，为后续接厂商 SDK 预留位置
 *
 * <p>之所以标记为 `@Primary`，是为了在存在默认日志发送器时优先走设备 Token 这条链路。
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class AppPushMedicationReminderNotifier implements MedicationReminderNotifier {

    /**
     * 统一维护“没有可用设备 Token”的失败原因文案。
     *
     * <p>这个字符串不仅会写回提醒表 / 消息中心，也会被后台统计看板按失败原因聚合，
     * 因此这里集中定义，避免不同调用路径出现“同义不同文案”，导致监控口径被打散。
     */
    private final HealthAppDeviceService appDeviceService;

    /** 外部Push网关客户端 */
    private final HealthExternalPushGatewayClient externalPushGatewayClient;

    /** Push派发审计应用服务 */
    private final AppPushDeliveryApplicationService appPushDeliveryApplicationService;

    /**
     * 根据当前用户启用中的设备列表派发提醒。
     * 如果用户还没有注册设备，则返回失败，方便后续任务重试或人工排查。
     */
    @Override
    public MedicationReminderNotifyResult notify(MedicationReminderNotice notice) {
        List<HealthAppDeviceEntity> activeDevices = appDeviceService.listActiveDevices(notice.getOwnerUserId())
            .stream()
            .filter(device -> device.getStatus() != null && device.getStatus().equals(StatusEnum.ENABLE.getValue()))
            .filter(device -> StrUtil.isNotBlank(device.getDeviceToken()))
            .collect(Collectors.toList());

        if (activeDevices.isEmpty()) {
            // 这里显式打 warn，而不是仅仅把失败原因塞回返回对象，
            // 是因为“没有活跃设备 Token”通常意味着：
            // 1. App 登录后没有重新上报设备；
            // 2. 用户手动停用了设备但没有重新注册；
            // 3. 推送联调阶段 App 端尚未完成真实 Token 接入。
            //
            // 这类问题从用户视角会直接表现成“提醒没有通知”，
            // 因此需要在后端日志里留下足够清晰的排障线索。
            log.warn("用药提醒Push发送失败，原因=无活跃设备Token，提醒ID：{}，用户ID：{}，成员ID：{}，药品：{}",
                notice.getReminderId(), notice.getOwnerUserId(), notice.getMemberId(), notice.getDrugName());
            return MedicationReminderNotifyResult.builder()
                .success(false)
                .channel("APP_PUSH")
                .message(AppPushAuditSupport.NO_ACTIVE_DEVICE_TOKEN_MESSAGE)
                .build();
        }

        String pushContent = buildPushContent(notice);
        if (externalPushGatewayClient.isReady()) {
            HealthExternalPushGatewayResult gatewayResult = externalPushGatewayClient.pushBatch(activeDevices,
                "按时服药提醒", pushContent, notice.getPushPayload());
            appPushDeliveryApplicationService.recordBatchDelivery(
                notice.getMessageId(),
                notice.getOwnerUserId(),
                notice.getMemberId(),
                "MEDICATION_REMINDER",
                notice.getReminderId(),
                activeDevices,
                gatewayResult.getChannel(),
                gatewayResult.isSuccess(),
                gatewayResult.getMessage(),
                gatewayResult.getVendorCode(),
                gatewayResult.getVendorMessage());
            return MedicationReminderNotifyResult.builder()
                .success(gatewayResult.isSuccess())
                .channel(gatewayResult.getChannel())
                .message(gatewayResult.getMessage())
                .build();
        }

        String pushPayload = notice.getPushPayload() == null ? "{}" : JacksonUtil.to(notice.getPushPayload());
        activeDevices.forEach(device -> log.info(
            "模拟发送设备Push成功，提醒ID：{}，用户ID：{}，设备编码：{}，平台：{}，Token：{}，内容：{}，透传载荷：{}",
            notice.getReminderId(), notice.getOwnerUserId(), device.getDeviceCode(), device.getPushPlatform(),
            device.getDeviceToken(), pushContent, pushPayload));
        appPushDeliveryApplicationService.recordBatchDelivery(
            notice.getMessageId(),
            notice.getOwnerUserId(),
            notice.getMemberId(),
            "MEDICATION_REMINDER",
            notice.getReminderId(),
            activeDevices,
            "APP_PUSH",
            true,
            "已派发到" + activeDevices.size() + "个设备",
            null,
            "模拟发送");

        return MedicationReminderNotifyResult.builder()
            .success(true)
            .channel("APP_PUSH")
            .message("已派发到" + activeDevices.size() + "个设备")
            .build();
    }

    /**
     * 组装 Push 文案。
     * 当前先采用统一模板，后续真正对接厂商时可扩展为标题、正文、透传参数等结构。
     */
    private String buildPushContent(MedicationReminderNotice notice) {
        if (notice.getPushPayload() != null && StrUtil.isNotBlank(notice.getPushPayload().getContent())) {
            return notice.getPushPayload().getContent();
        }
        String memberName = StrUtil.blankToDefault(notice.getMemberName(), "家庭成员");
        return memberName + " 需要按时服用 " + notice.getDrugName();
    }
}
