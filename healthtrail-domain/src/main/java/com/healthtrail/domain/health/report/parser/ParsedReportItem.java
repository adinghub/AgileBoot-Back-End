package com.healthtrail.domain.health.report.parser;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 后台解析得到的单条检查指标。
 */
@Data
public class ParsedReportItem {

    private String itemCode;

    private String standardItemCode;

    private String itemName;

    private String resultValue;

    private String resultUnit;

    private BigDecimal referenceMin;

    private BigDecimal referenceMax;

    private String referenceText;

    /**
     * 指标解读。
     *
     * <p>这里要求是“解释指标本身代表什么”的简短说明，
     * 不是针对本次结果做诊断，也不替代医生结论。
     */
    private String itemInterpretation;

    private Integer sort;

    private String remark;
}
