package com.healthtrail.domain.system.member;

/**
 * 会员门禁点编码常量。
 *
 * <p>这里集中维护的是“业务动作门禁码”，而不是会员权益编码：
 * 1. 业务服务层只表达“当前在拦哪个动作”；
 * 2. 具体动作最终绑定到哪个 featureCode，由后台门禁规则配置决定；
 * 3. 后续如果某个动作要从免费改成会员限制，或从一种权益切到另一种权益，
 *    业务代码不需要再到处改字符串。
 *
 * <p>当前这批编码和 App 端常量保持一致，方便前后端围绕同一组 gateCode 对齐。
 */
public final class MemberGateCodeConstants {

    private MemberGateCodeConstants() {
    }

    /**
     * 报告对外分享 / 导出入口。
     */
    public static final String REPORT_EXPORT = "REPORT.EXPORT";

    /**
     * AI 助手 / AI 总结入口。
     */
    public static final String AI_ASSISTANT_OPEN = "AI.ASSISTANT.OPEN";

    /**
     * 包含 AI 总结内容的报告导出入口。
     */
    public static final String AI_REPORT_EXPORT = "AI.REPORT.EXPORT";

    /**
     * 新增计划入口。
     *
     * <p>当前健康项目先复用这枚初始化门禁码来承载“新增用药计划”动作，
     * 这样能够直接复用已落地的会员门禁配置能力。
     * 后续如果产品希望把健康计划和资产计划拆成不同门禁点，
     * 再新增专门的 gateCode 并调整配置即可。
     */
    public static final String TRANSACTION_PLAN_CREATE = "TRANSACTION_PLAN.CREATE";

    /**
     * 健康洞察就医资料包导出入口。
     */
    public static final String HEALTH_VISIT_PACKAGE_EXPORT = "HEALTH.VISIT_PACKAGE.EXPORT";

    /**
     * 健康问题一键复查提醒入口。
     */
    public static final String HEALTH_PROBLEM_REVIEW_TASK = "HEALTH.PROBLEM.REVIEW_TASK";

    /**
     * 健康周报/月报查看入口。
     */
    public static final String HEALTH_PERIODIC_REPORT_VIEW = "HEALTH.PERIODIC_REPORT.VIEW";
}
