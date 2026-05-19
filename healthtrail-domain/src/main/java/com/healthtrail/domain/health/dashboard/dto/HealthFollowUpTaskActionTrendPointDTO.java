package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页任务行为趋势点。
 *
 * <p>趋势点只承载轻量统计值，方便 App 直接做折线图或简表展示。
 */
@Data
public class HealthFollowUpTaskActionTrendPointDTO {

    /**
     * 统计日期，格式为 yyyy-MM-dd。
     */
    private String statDate;

    /** 已读行为次数 */
    private Integer readActionCount;

    /** 延后行为次数 */
    private Integer delayActionCount;

    /** 完成行为次数 */
    private Integer completeActionCount;

    /** 忽略行为次数 */
    private Integer ignoreActionCount;

    /** 恢复行为次数 */
    private Integer restoreActionCount;

    /** 行为总次数 */
    private Integer totalActionCount;
}
