package com.healthtrail.domain.system.member.dto;

import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.system.member.db.MemberGateRuleEntity;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员功能门禁规则DTO。
 */
@Data
@NoArgsConstructor
public class MemberGateRuleDTO {

    private Long memberGateRuleId;
    private Long memberGateId;
    private String gateCode;
    private String gateName;
    /**
     * 门禁点作用域。
     *
     * <p>规则管理页除了看规则本身，还需要知道当前点位属于页面入口、按钮动作还是接口入口，
     * 这样运营或产品在配置时才能快速判断影响范围。
     */
    private String gateScope;
    /**
     * 所属业务模块。
     *
     * <p>和 gateCode 相比，bizModule 更适合列表分组和人工检索。
     */
    private String bizModule;
    /**
     * 生效终端类型。
     */
    private String terminalType;
    /**
     * 门禁点自身状态。
     *
     * <p>这里和规则状态是两个概念：
     * 1. gateStatus 表示“这个点位是否仍然启用”；
     * 2. status 表示“当前规则是否启用”。
     */
    private Integer gateStatus;
    /**
     * 是否内置门禁点。
     */
    private Integer isBuiltin;
    private String policyType;
    private String featureCode;
    private String featureName;
    private List<String> allowedLevelCodes;
    private String denyClientMode;
    private String denyTitle;
    private String denyMessage;
    private Integer guideMemberPage;
    private Integer priority;
    private Integer status;
    private Integer versionNo;
    private String remark;
    private Date updateTime;

    public MemberGateRuleDTO(MemberGateRuleEntity entity) {
        if (entity != null) {
            this.memberGateRuleId = entity.getMemberGateRuleId();
            this.memberGateId = entity.getMemberGateId();
            this.policyType = entity.getPolicyType();
            this.featureCode = entity.getFeatureCode();
            this.allowedLevelCodes = parseAllowedLevelCodes(entity.getAllowedLevelCodesJson());
            this.denyClientMode = entity.getDenyClientMode();
            this.denyTitle = entity.getDenyTitle();
            this.denyMessage = entity.getDenyMessage();
            this.guideMemberPage = entity.getGuideMemberPage();
            this.priority = entity.getPriority();
            this.status = entity.getStatus();
            this.versionNo = entity.getVersionNo();
            this.remark = entity.getRemark();
            this.updateTime = entity.getUpdateTime();
        }
    }

    private static List<String> parseAllowedLevelCodes(String allowedLevelCodesJson) {
        if (allowedLevelCodesJson == null || allowedLevelCodesJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return JacksonUtil.fromList(allowedLevelCodesJson, String.class);
    }
}
