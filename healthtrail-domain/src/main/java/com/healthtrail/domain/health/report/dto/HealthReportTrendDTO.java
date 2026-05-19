package com.healthtrail.domain.health.report.dto;

import java.util.List;
import lombok.Data;

/**
 * 报告趋势对比总览返回对象。
 */
@Data
public class HealthReportTrendDTO {

    /** 报告ID。 */
    private Long reportId;

    /** 成员ID。 */
    private Long memberId;

    /** 成员姓名。 */
    private String memberName;

    /** 报告类型。 */
    private String reportType;

    /** comparedReportCount。 */
    private Integer comparedReportCount;

    /** 明细列表。 */
    private List<HealthReportTrendItemDTO> items;
}
