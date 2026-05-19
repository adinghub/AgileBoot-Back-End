package com.healthtrail.domain.system.member;

/**
 * 会员准入拒绝原因枚举。
 *
 * <p>准入工具不只需要告诉业务“能不能用”，
 * 还需要明确说明“为什么不能用”，这样页面可以直接拿原因弹提示，
 * 后端业务也能区分“配置错误”与“用户会员条件不满足”。
 */
public enum MemberAccessDenyReasonEnum {

    /**
     * 调用方没有提供任何可判断的门槛条件。
     */
    INVALID_REQUIREMENT,

    /**
     * 要求的会员等级编码不存在或未启用，属于开发配置问题。
     */
    INVALID_REQUIRED_LEVEL,

    /**
     * 当前账号等级低于功能要求的最低等级。
     */
    REQUIRED_LEVEL_NOT_MET,

    /**
     * 指定的权益编码没有配置到权益快照里，属于会员配置缺失。
     */
    FEATURE_NOT_FOUND,

    /**
     * 权益存在，但当前等级并未开放。
     */
    FEATURE_NOT_ENABLED,

    /**
     * 次数型权益剩余额度不足。
     */
    QUOTA_NOT_ENOUGH,

    /**
     * 限额型权益的当前额度低于功能要求。
     */
    LIMIT_NOT_ENOUGH
}
