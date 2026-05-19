package com.healthtrail.domain.health.report.dto;

import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 体检报告指标结果返回对象。
 */
@Data
@NoArgsConstructor
public class HealthReportItemDTO {

    /** 项目ID。 */
    private Long itemId;

    /** 报告ID。 */
    private Long reportId;

    /** 项目编码。 */
    private String itemCode;

    /** 标准项目编码。 */
    private String standardItemCode;

    /** 项目名称。 */
    private String itemName;

    /** 结果值。 */
    private String resultValue;

    /** 结果单位。 */
    private String resultUnit;

    /** 参考下限。 */
    private BigDecimal referenceMin;

    /** 参考上限。 */
    private BigDecimal referenceMax;

    /** 参考范围。 */
    private String referenceText;

    /** 项目解读。 */
    private String itemInterpretation;

    /** 异常标记。 */
    private Integer abnormalFlag;

    /** 异常标记名称。 */
    private String abnormalFlagName;

    /** 排序。 */
    private Integer sort;

    /** 备注。 */
    private String remark;

    public HealthReportItemDTO(HealthReportItemEntity entity) {
        if (entity != null) {
            this.itemId = entity.getItemId();
            this.reportId = entity.getReportId();
            this.itemCode = entity.getItemCode();
            this.standardItemCode = entity.getStandardItemCode();
            this.itemName = entity.getItemName();
            this.resultValue = entity.getResultValue();
            this.resultUnit = entity.getResultUnit();
            this.referenceMin = entity.getReferenceMin();
            this.referenceMax = entity.getReferenceMax();
            this.referenceText = entity.getReferenceText();
            this.itemInterpretation = entity.getItemInterpretation();
            this.abnormalFlag = entity.getAbnormalFlag();
            this.abnormalFlagName = resolveAbnormalFlagName(entity.getAbnormalFlag());
            this.sort = entity.getSort();
            this.remark = entity.getRemark();
        }
    }

    /**
     * 回填异常标记名称，方便前端直接展示标签文案。
     */
    private String resolveAbnormalFlagName(Integer abnormalFlagValue) {
        if (abnormalFlagValue == null) {
            return null;
        }
        for (HealthReportItemAbnormalFlagEnum abnormalFlagEnum : HealthReportItemAbnormalFlagEnum.values()) {
            if (abnormalFlagEnum.getValue().equals(abnormalFlagValue)) {
                return HealthAppI18n.reportAbnormalFlagName(abnormalFlagEnum.getValue());
            }
        }
        return null;
    }
}
