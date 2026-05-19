package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台赠送会员命令。
 */
@Data
public class AssignUserMemberCommand {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "会员等级ID不能为空")
    private Long memberLevelId;

    private Integer durationDays;

    private String remark;
}
