package com.healthtrail.domain.health.medication.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 阶段剂量配置。
 *
 * <p>该对象会被序列化保存在 medication_plan.dose_rule 字段中，例如：
 * [{"durationDays":4,"doseAmount":1,"doseUnit":"片"},{"durationDays":null,"doseAmount":2,"doseUnit":"片"}]。
 * durationDays 为空表示从当前阶段开始长期使用该剂量。
 */
@Data
public class MedicationDoseStageDTO {

    /**
     * 当前阶段持续天数。
     */
    private Integer durationDays;

    /**
     * 当前阶段每次服用剂量。
     */
    private BigDecimal doseAmount;

    /**
     * 当前阶段剂量单位。
     */
    private String doseUnit;
}
