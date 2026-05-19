package com.healthtrail.domain.system.member;

/**
 * 会员权益编码常量。
 *
 * <p>把健康系统首批内置权益编码集中收口到一个常量类里，
 * 目的是避免：
 * 1. 各个业务模块自己手写字符串导致拼写漂移；
 * 2. 后续调整权益口径时需要全局搜字符串；
 * 3. App、后台、服务端在联调时出现“同名不同码”的问题。
 */
public final class MemberFeatureCodeConstants {

    private MemberFeatureCodeConstants() {
    }

    public static final String AI_REPORT_PARSE = "AI_REPORT_PARSE";
    public static final String AI_REPORT_PARSE_QUOTA = "AI_REPORT_PARSE_QUOTA";
    public static final String AI_RESULT_INTERPRETATION = "AI_RESULT_INTERPRETATION";
    public static final String AI_RESULT_INTERPRETATION_QUOTA = "AI_RESULT_INTERPRETATION_QUOTA";
    public static final String AI_REPORT_SUMMARY = "AI_REPORT_SUMMARY";
    public static final String AI_REPORT_SUMMARY_QUOTA = "AI_REPORT_SUMMARY_QUOTA";
    public static final String MAX_FAMILY_MEMBER = "MAX_FAMILY_MEMBER";
    public static final String MAX_REPORT_UPLOAD_COUNT = "MAX_REPORT_UPLOAD_COUNT";
    public static final String MAX_ACTIVE_MEDICATION_PLAN = "MAX_ACTIVE_MEDICATION_PLAN";
}
