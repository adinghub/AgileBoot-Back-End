package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 清空用户会员命令。
 */
@Data
public class ClearUserMemberCommand {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String remark;
}
