package com.healthtrail.domain.health.drug.unit.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改药品单位命令。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateDrugUnitCommand extends AddDrugUnitCommand {

    /**
     * 单位ID。
     */
    private Long unitId;
}
