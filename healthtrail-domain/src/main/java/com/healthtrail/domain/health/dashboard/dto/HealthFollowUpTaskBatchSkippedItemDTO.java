package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页任务批量操作跳过项。
 *
 * <p>批量操作采用“尽力而为、部分成功”策略时，
 * 前端需要知道哪些任务被跳过，以及为什么被跳过。
 */
@Data
public class HealthFollowUpTaskBatchSkippedItemDTO {

    /** 任务ID */
    private Long taskId;

    /** 跳过原因 */
    private String reason;
}
