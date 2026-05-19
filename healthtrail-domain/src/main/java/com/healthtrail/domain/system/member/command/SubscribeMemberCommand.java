package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开通会员命令。
 */
@Data
public class SubscribeMemberCommand {

    @NotNull(message = "会员等级ID不能为空")
    /** memberLevelId。 */
    private Long memberLevelId;

    /** 备注。 */
    private String remark;
}
