package com.healthtrail.domain.system.member.dto;

import com.healthtrail.domain.system.member.db.MemberGateEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员功能门禁点DTO。
 */
@Data
@NoArgsConstructor
public class MemberGateDTO {

    private Long memberGateId;
    private String gateCode;
    private String gateName;
    private String gateScope;
    private String bizModule;
    private String terminalType;
    private String defaultPolicyType;
    private Integer status;
    private Integer isBuiltin;
    private String remark;
    private Date createTime;
    private String currentPolicyType;
    private String currentFeatureCode;
    private String currentFeatureName;
    private Integer currentRuleStatus;
    private Integer currentRuleVersionNo;

    public MemberGateDTO(MemberGateEntity entity) {
        if (entity != null) {
            this.memberGateId = entity.getMemberGateId();
            this.gateCode = entity.getGateCode();
            this.gateName = entity.getGateName();
            this.gateScope = entity.getGateScope();
            this.bizModule = entity.getBizModule();
            this.terminalType = entity.getTerminalType();
            this.defaultPolicyType = entity.getDefaultPolicyType();
            this.status = entity.getStatus();
            this.isBuiltin = entity.getIsBuiltin();
            this.remark = entity.getRemark();
            this.createTime = entity.getCreateTime();
        }
    }
}
