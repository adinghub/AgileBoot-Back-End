package com.healthtrail.domain.health.medication.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改用药计划命令对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateMedicationPlanCommand extends AddMedicationPlanCommand {

    /**
     * 用药计划ID。
     */
    private Long planId;
}
