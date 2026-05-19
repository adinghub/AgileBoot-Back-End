package com.healthtrail.domain.health.report.dto;

import java.util.Date;
import lombok.Data;

/**
 * AI 智能总结结果返回对象。
 *
 * <p>当前返回结构刻意保持轻量，方便 App 在详情弹窗或摘要卡片中直接展示：
 * 1. 总结正文
 * 2. 风险声明
 * 3. 处理时间
 */
@Data
public class HealthReportAiSummaryDTO {

    /** 报告ID。 */
    private Long reportId;

    /** AI摘要状态。 */
    private Integer aiSummaryStatus;

    /** 摘要。 */
    private String summary;

    /** disclAImer。 */
    private String disclaimer;

    /** processedTime。 */
    private Date processedTime;
}
