package com.healthtrail.domain.system.member;

/**
 * 会员门禁策略类型常量。
 *
 * <p>门禁点这一层的职责是描述“某个业务动作当前怎么限制”，
 * 因此这里先把首期策略类型集中为常量，避免前后端和 SQL 再次手写字符串。
 */
public final class MemberGatePolicyTypeConstants {

    private MemberGatePolicyTypeConstants() {
    }

    /**
     * 不限制，直接放行。
     */
    public static final String ALLOW = "ALLOW";

    /**
     * 按会员规则限制。
     *
     * <p>这里的“会员规则”既可以是：
     * 1. 绑定某个 featureCode；
     * 2. 只绑定等级白名单；
     * 3. 同时绑定 featureCode 与等级白名单。
     */
    public static final String FEATURE = "FEATURE";
}
