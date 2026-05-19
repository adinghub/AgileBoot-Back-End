package com.healthtrail.domain.health.drug.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改药品命令对象。
 *
 * <p>修改时仍沿用新增命令的大部分字段定义，只额外增加药品ID用于定位目标数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateDrugCommand extends AddDrugCommand {

    /**
     * 药品ID。
     */
    private Long drugId;
}
