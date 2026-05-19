package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 兑换会员兑换码命令。
 */
@Data
public class RedeemMemberCodeCommand {

    @NotBlank(message = "兑换码不能为空")
    /** redeemCode。 */
    private String redeemCode;
}
