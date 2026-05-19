package com.healthtrail.domain.system.member;

/**
 * App 门禁结果状态常量。
 *
 * <p>和直接抛异常不同，App 查询门禁快照时需要知道“为什么不能用”，
 * 这样页面才能决定置灰、隐藏、弹窗或者引导去会员页。
 */
public final class MemberGateAccessStatusConstants {

    private MemberGateAccessStatusConstants() {
    }

    public static final String ALLOW = "ALLOW";
    public static final String ALLOW_BY_DEFAULT = "ALLOW_BY_DEFAULT";
    public static final String GATE_DISABLED_ALLOW = "GATE_DISABLED_ALLOW";
    public static final String RULE_DISABLED_ALLOW = "RULE_DISABLED_ALLOW";
    public static final String LEVEL_REQUIRED = "LEVEL_REQUIRED";
    public static final String FEATURE_DISABLED = "FEATURE_DISABLED";
    public static final String LIMIT_REACHED = "LIMIT_REACHED";
    public static final String QUOTA_REACHED = "QUOTA_REACHED";
    public static final String MEMBER_DENIED = "MEMBER_DENIED";
}
