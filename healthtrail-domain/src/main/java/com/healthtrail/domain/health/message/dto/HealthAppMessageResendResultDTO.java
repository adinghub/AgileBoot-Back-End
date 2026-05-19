package com.healthtrail.domain.health.message.dto;

import java.util.Date;
import lombok.Data;

/**
 * 后台人工重发消息结果 DTO。
 *
 * <p>后台执行重发后，需要立即知道：
 * 1. 本次有没有发送成功
 * 2. 走了哪个通道
 * 3. 消息表当前回写成了什么状态
 *
 * <p>因此这里单独抽一个结果 DTO，避免前端还要再额外刷新一次详情页才能知道结果。
 */
@Data
public class HealthAppMessageResendResultDTO {

    /**
     * 消息ID。
     */
    private Long messageId;

    /**
     * 本次重发是否成功。
     */
    private Boolean success;

    /**
     * 本次重发通道。
     */
    private String channel;

    /**
     * 本次重发结果说明。
     */
    private String message;

    /**
     * 回写后的发送状态。
     */
    private Integer sendStatus;

    /**
     * 回写后的发送时间。
     */
    private Date sendTime;

    /**
     * 回写后的发送重试次数。
     */
    private Integer sendRetryCount;
}
