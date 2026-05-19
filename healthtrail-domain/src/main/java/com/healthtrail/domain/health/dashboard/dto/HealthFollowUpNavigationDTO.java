package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页待跟进任务导航信息 DTO。
 *
 * <p>该对象专门给 App 前端做页面跳转使用，
 * 让首页、任务中心、任务详情页都能拿到统一的跳转参数。
 */
@Data
public class HealthFollowUpNavigationDTO {

    /**
     * 目标页面编码。
     *
     * <p>例如：
     * 1. `MEDICATION_REMINDER_DETAIL`
     * 2. `HEALTH_REPORT_DETAIL`
     */
    private String targetPageCode;

    /**
     * 目标页面名称。
     */
    private String targetPageName;

    /**
     * 目标业务主键ID。
     *
     * <p>提醒任务对应 reminderId，报告任务对应 reportId。
     */
    private Long targetBizId;

    /**
     * 目标业务类型。
     *
     * <p>用于帮助前端快速识别当前跳转参数到底属于哪一类业务对象。
     */
    private String targetBizType;

    /**
     * 推荐打开的默认标签页编码。
     *
     * <p>例如报告建议任务可直接给出 `ADVICE`，
     * 提示前端默认落到报告建议区域。
     */
    private String targetTabCode;

    /**
     * 推荐打开的默认锚点编码。
     *
     * <p>用于进一步表达页面内应该优先定位到哪一块区域，
     * 例如异常指标区、报告建议区、原始报告区。
     */
    private String targetAnchorCode;

    /**
     * 推荐打开的默认锚点名称。
     */
    private String targetAnchorName;
}
