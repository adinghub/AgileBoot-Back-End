package com.healthtrail.domain.health.report.dto;

import com.healthtrail.domain.health.report.db.HealthIndicatorTemplateEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指标模板返回对象。
 */
@Data
@NoArgsConstructor
public class HealthIndicatorTemplateDTO {

    /** templateId。 */
    private Long templateId;

    /** 报告类型。 */
    private String reportType;

    /** 项目编码。 */
    private String itemCode;

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

    /** 创建时间。 */
    private Date createTime;

    public HealthIndicatorTemplateDTO(HealthIndicatorTemplateEntity entity) {
        if (entity != null) {
            this.templateId = entity.getTemplateId();
            this.reportType = entity.getReportType();
            this.itemCode = entity.getItemCode();
            this.itemName = entity.getItemName();
            this.resultUnit = entity.getResultUnit();
            this.referenceMin = entity.getReferenceMin();
            this.referenceMax = entity.getReferenceMax();
            this.referenceText = entity.getReferenceText();
            this.suggestionTemplate = entity.getSuggestionTemplate();
            this.sort = entity.getSort();
            this.status = entity.getStatus();
            this.createTime = entity.getCreateTime();
        }
    }
}
