package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改会员功能门禁点命令。
 */
@Data
public class UpdateMemberGateCommand {

    @NotNull(message = "门禁点ID不能为空")
    private Long memberGateId;

    @NotBlank(message = "门禁点编码不能为空")
    private String gateCode;

    @NotBlank(message = "门禁点名称不能为空")
    private String gateName;

    @NotBlank(message = "门禁点作用域不能为空")
    private String gateScope;

    @NotBlank(message = "所属业务模块不能为空")
    private String bizModule;

    @NotBlank(message = "终端类型不能为空")
    private String terminalType;

    @NotBlank(message = "默认策略类型不能为空")
    private String defaultPolicyType;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "是否内置不能为空")
    private Integer isBuiltin;

    private String remark;
}
