package com.healthtrail.domain.health.chronic.dto;

import java.util.Date;
import lombok.Data;

/**
 * 慢病专项指标趋势点。
 *
 * <p>趋势点始终来自体检报告原始指标，
 * App 只负责展示，不在本地重新计算异常或变化方向。
 */
@Data
public class ChronicDiseaseIndicatorPointDTO {

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
