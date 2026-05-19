package com.healthtrail.domain.health.message.dto;

import java.util.Date;
import lombok.Data;

/**
 * 批量重发单条结果 DTO。
 *
 * <p>批量重发很容易出现“部分成功、部分失败”的情况，
 * 因此需要把每条消息的执行结果单独返回给后台，
 * 方便页面逐行展示处理结果，而不是只给一个总成功数。
 */
@Data
public class HealthAppMessageBatchResendItemDTO {

    /**
     * 消息ID。
     */
    private Long messageId;

    /**
     * 本条是否重发成功。
     */
    private Boolean success;

    /**
     * 本条消息本次走的通道。
     */
    private String channel;

    /**
     * 本条消息本次执行结果说明。
     */
    private String message;

    /**
     * 当前消息表里的发送状态。
     */
    private Integer sendStatus;

    /**
     * 当前消息表里的发送时间。
     */
    private Date sendTime;

    /**
     * 当前消息表里的发送重试次数。
     */
    private Integer sendRetryCount;
}
