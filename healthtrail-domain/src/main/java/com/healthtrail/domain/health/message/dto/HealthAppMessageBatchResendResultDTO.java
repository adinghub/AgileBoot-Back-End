package com.healthtrail.domain.health.message.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台批量重发消息结果 DTO。
 *
 * <p>该对象用于同时承接：
 * 1. 整批执行的总体统计
 * 2. 每条消息的明细结果
 *
 * <p>这样后台既可以先展示顶部统计提示，
 * 也可以在表格中回显每条消息的执行状态。
 */
@Data
public class HealthAppMessageBatchResendResultDTO {

    /**
     * 本次请求传入的消息数量。
     */
    private Integer requestCount;

    /**
     * 去重后的实际执行数量。
     */
    private Integer actualCount;

    /**
     * 成功数量。
     */
    private Integer successCount;

    /**
     * 失败数量。
     */
    private Integer failedCount;

    /**
     * 每条消息的执行结果明细。
     */
    private List<HealthAppMessageBatchResendItemDTO> results;
}
