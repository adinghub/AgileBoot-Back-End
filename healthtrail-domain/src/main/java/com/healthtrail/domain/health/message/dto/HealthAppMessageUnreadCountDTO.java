package com.healthtrail.domain.health.message.dto;

import lombok.Data;

/**
 * App 消息未读数量 DTO。
 */
@Data
public class HealthAppMessageUnreadCountDTO {

    /**
     * 未读数量。
     */
    private Long unreadCount;
}
