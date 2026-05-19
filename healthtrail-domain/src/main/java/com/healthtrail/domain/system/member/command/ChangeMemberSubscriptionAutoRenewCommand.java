package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 自动续费开关命令。
 */
@Data
public class ChangeMemberSubscriptionAutoRenewCommand {

    @NotNull(message = "是否启用自动续费不能为空")
    /** enabled。 */
    private Integer enabled;

    /** 备注。 */
    private String remark;
}
