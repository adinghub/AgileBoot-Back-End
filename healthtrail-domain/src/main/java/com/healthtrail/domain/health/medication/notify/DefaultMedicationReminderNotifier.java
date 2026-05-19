package com.healthtrail.domain.health.medication.notify;

import com.healthtrail.common.utils.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认用药提醒发送器。
 *
 * <p>当前仓库还没有真正接入设备 Token、短信网关或站内消息中心，
 * 因此这里先提供一个“日志模拟发送器”作为默认实现：
 * 1. 能让提醒派发链路先完整跑通
 * 2. 不会阻塞后续真实推送能力的接入
 * 3. 后续如果接入真实发送器，可通过新增 `@Primary` 实现进行覆盖
 */
@Slf4j
@Component
public class DefaultMedicationReminderNotifier implements MedicationReminderNotifier {

    /**
     * 当前默认通过日志模拟发送成功。
     * 后续如果替换为真实推送通道，只需要提供新的 `MedicationReminderNotifier` 实现即可。
     */
    @Override
    public MedicationReminderNotifyResult notify(MedicationReminderNotice notice) {
        log.info("模拟发送用药提醒成功，提醒ID：{}，用户ID：{}，成员：{}，药品：{}，提醒时间：{}，透传载荷：{}",
            notice.getReminderId(), notice.getOwnerUserId(), notice.getMemberName(), notice.getDrugName(),
            notice.getScheduledTime(), notice.getPushPayload() == null ? "{}" : JacksonUtil.to(notice.getPushPayload()));
        return MedicationReminderNotifyResult.builder()
            .success(true)
            .channel("LOG_SIMULATION")
            .message("默认日志发送器已接收该提醒")
            .build();
    }
}
