package com.healthtrail.domain.health.report.notify;

import lombok.Builder;
import lombok.Data;

/**
 * 报告建议通知发送结果。
 *
 * <p>应用服务只关心：
 * 1. 本次是否发送成功
 * 2. 通过哪个通道发送
 * 3. 失败时的原因是什么
 *
 * <p>因此这里保持轻量结果对象设计，避免把发送器内部实现细节暴露到业务层。
 */
@Data
@Builder
public class HealthReportAdviceNotifyResult {

    /**
     * 是否发送成功。
     */
    private boolean success;

    /**
     * 发送通道标识。
     */
    private String channel;

    /**
     * 失败原因或补充说明。
     */
    private String message;
}
