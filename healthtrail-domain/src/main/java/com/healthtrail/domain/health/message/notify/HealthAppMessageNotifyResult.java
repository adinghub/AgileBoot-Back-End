package com.healthtrail.domain.health.message.notify;

import lombok.Builder;
import lombok.Data;

/**
 * App 消息发送结果。
 *
 * <p>该结果对象用于描述“本次人工重发到底有没有发出去”，
 * 并统一返回给应用服务做状态回写与后台接口响应。
 */
@Data
@Builder
public class HealthAppMessageNotifyResult {

    /**
     * 是否成功。
     */
    private boolean success;

    /**
     * 发送通道标识，例如 APP_PUSH。
     */
    private String channel;

    /**
     * 发送结果说明。
     */
    private String message;
}
