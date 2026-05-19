package com.healthtrail.domain.system.member;

/**
 * 会员等级编码常量。
 *
 * <p>这里把首期会员等级编码集中定义出来，
 * 目的是让 App、后端业务服务、会员准入工具共用同一套等级标识，
 * 避免后续某个功能增加会员门槛时继续散落硬编码字符串。
 */
public final class MemberLevelCodeConstants {

    private MemberLevelCodeConstants() {
    }

    /**
     * 免费版。
     *
     * <p>当用户当前没有有效会员关系时，会员准入工具会自动把账号按 FREE 口径处理，
     * 这样“免费版 < PLUS < PRO”这类等级比较就有稳定的基准点。
     */
    public static final String FREE = "FREE";

    /**
     * PLUS 会员。
     */
    public static final String PLUS = "PLUS";

    /**
     * PRO 会员。
     */
    public static final String PRO = "PRO";
}
