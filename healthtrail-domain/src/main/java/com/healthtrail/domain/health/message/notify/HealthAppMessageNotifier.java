package com.healthtrail.domain.health.message.notify;

/**
 * App 消息发送器。
 *
 * <p>该接口专门服务于消息中心维度的发送动作，
 * 与用药提醒、报告建议各自的发送器解耦。
 * 这样后台人工重发时，不需要重新还原原始业务对象，也能直接发送。
 */
public interface HealthAppMessageNotifier {

    /**
     * 发送一条消息。
     *
     * @param notice 消息发送载荷
     * @return 发送结果
     */
    HealthAppMessageNotifyResult notify(HealthAppMessageNotice notice);
}
