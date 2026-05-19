package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页健康趋势点 DTO。
 *
 * <p>首页趋势图不追求医学报告详情页那样的细粒度指标对比，
 * 而是更强调“最近这段时间整体健康关注度是在上升还是下降”。
 */
@Data
public class HealthHomeHealthTrendPointDTO {

    /**
     * 统计日期文本，格式建议为 yyyy-MM-dd。
     */
    private String statDate;

    /**
     * 首页健康评分。
     */
    private Double healthScore;

    /**
     * 当天或当次报告对应的异常指标数量。
     */
    private Integer abnormalItemCount;

    /**
     * 当天涉及的异常报告数量。
     */
    private Integer abnormalReportCount;
}
