package com.healthtrail.domain.health.report.notify;

/**
 * 报告建议通知发送器。
 *
 * <p>这是报告建议模块预留的通知通道扩展点：
 * 1. 当前可以先用日志模拟发送，保证链路跑通
 * 2. 后续可以平滑替换为 App Push、短信、站内信等真实通道
 */
public interface HealthReportAdviceNotifier {

    /**
     * 发送单条报告建议通知。
     *
     * @param notice 标准化报告建议通知载荷
     * @return 发送结果
     */
    HealthReportAdviceNotifyResult notify(HealthReportAdviceNotice notice);
}
