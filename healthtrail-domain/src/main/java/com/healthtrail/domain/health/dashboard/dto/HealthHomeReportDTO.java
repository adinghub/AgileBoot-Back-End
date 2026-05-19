package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import lombok.Data;

/**
 * 首页异常报告预览 DTO。
 *
 * <p>首页只输出最近值得关注的异常报告摘要，
 * 方便前端做“点击进入报告详情”的快捷入口。
 */
@Data
public class HealthHomeReportDTO {

    /** 报告ID。 */
    private Long reportId;

    /** 成员ID。 */
    private Long memberId;

    /** 成员姓名。 */
    private String memberName;

    /** 报告名称。 */
    private String reportName;

    /** 报告日期。 */
    private Date reportDate;

    /**
     * 异常指标数量。
     */
    private Integer abnormalItemCount;

    /**
     * 报告摘要。
     */
    private String analysisSummary;
}
