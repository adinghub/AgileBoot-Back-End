package com.healthtrail.domain.health.report.dto;

import java.util.Date;
import lombok.Data;

/**
 * 报告指标趋势点返回对象。
 */
@Data
public class HealthReportTrendPointDTO {

    /** 报告ID。 */
    private Long reportId;

    /** 报告日期。 */
    private Date reportDate;

    /** 结果值。 */
    private String resultValue;

    /** 结果单位。 */
    private String resultUnit;

    /** 异常标记。 */
    private Integer abnormalFlag;

    /** 异常标记名称。 */
    private String abnormalFlagName;
}
