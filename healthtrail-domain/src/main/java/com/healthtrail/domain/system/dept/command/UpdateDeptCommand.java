package com.healthtrail.domain.system.dept.command;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改部门命令
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateDeptCommand extends AddDeptCommand {

    /** 部门ID */
    @NotNull
    @PositiveOrZero
    private Long deptId;

}
