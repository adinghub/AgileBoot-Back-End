package com.healthtrail.domain.system.member;

import lombok.Data;

/**
 * 会员准入结果。
 *
 * <p>这里返回的不只是一个布尔值，
 * 还会带上当前等级、目标等级、权益快照、拒绝原因等上下文，
 * 方便调用方直接展示提示或写审计日志。
 */
@Data
public class MemberAccessResult {

    /**
     * 是否通过准入校验。
     */
    private boolean allowed;

    /**
     * 拒绝原因编码；通过时为空。
     */
    private String denyReasonCode;

    /**
     * 给调用方直接展示或记录的解释文案。
     */
    private String message;

    /**
     * 当前用户 ID。
     */
    private Long userId;

    /**
     * 当前是否存在有效会员。
     */
    private Boolean hasActiveMember;

    /**
     * 当前有效会员等级 ID；无有效会员时为空。
     */
    private Long currentMemberLevelId;

    /**
     * 当前参与比较的等级编码。
     *
     * <p>无有效会员时会按 FREE 口径返回。
     */
    private String currentLevelCode;

    /**
     * 当前参与比较的等级名称。
     */
    private String currentLevelName;

    /**
     * 当前等级排序值。
     */
    private Integer currentLevelSort;

    /**
     * 要求的等级编码。
     */
    private String requiredLevelCode;

    /**
     * 要求的等级名称。
     */
    private String requiredLevelName;

    /**
     * 要求的等级排序值。
     */
    private Integer requiredLevelSort;

    /**
     * 参与判断的权益编码。
     */
    private String featureCode;

    /**
     * 参与判断的权益名称。
     */
    private String featureName;

    /**
     * 参与判断的权益类型。
     */
    private String featureType;

    /**
     * 权益是否启用，1 启用 0 未启用。
     */
    private Integer featureEnabled;

    /**
     * 当前权益额度。
     */
    private Integer limitValue;

    /**
     * 当前周期已用次数。
     */
    private Integer usedCount;

    /**
     * 当前周期剩余次数。
     */
    private Integer remainingCount;
}
