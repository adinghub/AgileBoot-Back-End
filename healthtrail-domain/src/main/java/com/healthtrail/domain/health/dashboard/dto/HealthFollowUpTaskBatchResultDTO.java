package com.healthtrail.domain.health.dashboard.dto;

import java.util.List;
import lombok.Data;

/**
 * 首页任务批量操作结果。
 *
 * <p>该对象用于把一次批量操作的整体结果回传给前端，
 * 便于页面给出“成功 N 条、跳过 M 条”的明确反馈。
 */
@Data
public class HealthFollowUpTaskBatchResultDTO {

    /** 请求处理数量 */
    private Integer requestedCount;

    /** 成功处理数量 */
    private Integer successCount;

    /** 跳过数量 */
    private Integer skippedCount;

    /** 成功处理的任务ID列表 */
    private List<Long> successTaskIds;

    /** 跳过项明细列表 */
    private List<HealthFollowUpTaskBatchSkippedItemDTO> skippedItems;
}
