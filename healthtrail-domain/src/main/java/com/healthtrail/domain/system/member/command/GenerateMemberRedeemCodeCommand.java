package com.healthtrail.domain.system.member.command;

import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量生成会员兑换码命令。
 */
@Data
public class GenerateMemberRedeemCodeCommand {

    @NotNull(message = "会员等级ID不能为空")
    private Long memberLevelId;

    @NotNull(message = "生成数量不能为空")
    private Integer generateCount;

    private Date expireTime;

    private String remark;
}
