package com.healthtrail.domain.health.medication.notify;

/**
 * 用药提醒发送器。
 *
 * <p>这是健康提醒模块预留的通知通道扩展点：
 * 1. 当前可以先用日志模拟发送，保证链路跑通
 * 2. 后续可以平滑替换为 App Push、短信、公众号模板消息、站内信等真实通道
 */
public interface MedicationReminderNotifier {

    /**
     * 发送单条用药提醒。
     *
     * @param notice 标准化提醒载荷
     * @return 发送结果
     */
    MedicationReminderNotifyResult notify(MedicationReminderNotice notice);
}
