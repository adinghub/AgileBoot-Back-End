package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页待跟进任务推荐下一步动作 DTO。
 *
 * <p>该对象用于承接“任务详情页最应该先展示给用户的下一步动作建议”，
 * 让前端可以把它渲染成一张独立引导卡片。
 *
 * <p>与任务日志不同，它不强调历史；
 * 与任务基础信息不同，它不强调事实；
 * 它强调的是“现在最建议先做什么”。
 */
@Data
public class HealthFollowUpRecommendedActionDTO {

    /**
     * 推荐动作优先级，数值越小越优先。
     */
    private Integer actionPriority;

    /**
     * 风险等级编码。
     */
    private String riskLevel;

    /**
     * 风险等级名称。
     */
    private String riskLevelName;

    /**
     * 风险等级样式标记。
     *
     * <p>前端可直接据此控制标签颜色，
     * 减少再次维护映射关系。
     */
    private String riskCssTag;

    /**
     * 建议标题。
     */
    private String title;

    /**
     * 建议内容。
     */
    private String content;

    /**
     * 建议按钮文案。
     */
    private String actionText;

    /**
     * 推荐动作对应的跳转参数。
     *
     * <p>通常会复用当前任务本身的 navigation，
     * 但如果未来需要跳到不同页面，也保留了扩展空间。
     */
    private HealthFollowUpNavigationDTO navigation;
}
