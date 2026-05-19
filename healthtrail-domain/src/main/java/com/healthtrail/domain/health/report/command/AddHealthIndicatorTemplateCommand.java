package com.healthtrail.domain.health.report.command;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增指标模板命令。
 */
@Data
public class AddHealthIndicatorTemplateCommand {

    /** 报告类型。 */
    private String reportType;

    /** 项目编码。 */
    private String itemCode;

    @NotBlank(message = "指标名称不能为空")
    /** 项目名称。 */
    private String itemName;

    /** 结果单位。 */
    private String resultUnit;

    /** 参考下限。 */
    private BigDecimal referenceMin;

    /** 参考上限。 */
    private BigDecimal referenceMax;

    /** 参考范围。 */
    private String referenceText;

    /** suggestionTemplate。 */
    private String suggestionTemplate;

    /** 排序。 */
    private Integer sort;

    /** 状态。 */
    private Integer status;
}
