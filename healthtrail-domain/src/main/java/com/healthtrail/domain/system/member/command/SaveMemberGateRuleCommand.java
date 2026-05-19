package com.healthtrail.domain.system.member.command;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存会员功能门禁规则命令。
 */
@Data
public class SaveMemberGateRuleCommand {

    @NotNull(message = "门禁点ID不能为空")
    private Long memberGateId;

    @NotBlank(message = "策略类型不能为空")
    private String policyType;

    private String featureCode;

    /**
     * 等级白名单。
     *
     * <p>允许为空。为空时表示不额外做等级白名单限制，
     * 只按 featureCode 对应的权益结果判断。
     */
    private List<String> allowedLevelCodes;

    private String denyClientMode;
    private String denyTitle;
    private String denyMessage;
    private Integer guideMemberPage;
    private Integer priority;
    private Integer status;
    private String remark;
}
