package com.healthtrail.domain.health.report.dto;

import java.util.List;
import lombok.Data;

/**
 * 报告指标趋势对比项返回对象。
 */
@Data
public class HealthReportTrendItemDTO {

    /** 项目编码。 */
    private String itemCode;

    /** 标准项目编码。 */
    private String standardItemCode;

    /** 项目名称。 */
    private String itemName;

    /** 结果单位。 */
    private String resultUnit;

    /** currentResultValue。 */
    private String currentResultValue;

    /** previousResultValue。 */
    private String previousResultValue;

    /** changeDirection。 */
    private String changeDirection;

    /** changeSummary。 */
    private String changeSummary;

    /** 异常标记。 */
    private Integer abnormalFlag;

    /** 异常标记名称。 */
    private String abnormalFlagName;

    /** trendPoints列表。 */
    private List<HealthReportTrendPointDTO> trendPoints;
}
