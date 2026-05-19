package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 首页家庭成员健康卡片 DTO。
 *
 * <p>成员卡片是首页最贴近“健康主体”的展示单元，
 * 目的是让 App 用户一眼看到每个家庭成员今天需要关注什么。
 */
@Data
public class HealthHomeMemberCardDTO {

    /** 成员ID。 */
    private Long memberId;

    /**
     * 家庭成员业务编码。
     */
    private String memberCode;

    /** 成员姓名。 */
    private String memberName;

    /** 与用户的关系类型 */
    private String relationType;

    /**
     * 今日提醒总数。
     */
    private Integer todayReminderCount;

    /**
     * 今日待处理提醒数。
     */
    private Integer todayPendingReminderCount;

    /**
     * 最近一份报告ID。
     */
    private Long latestReportId;

    /**
     * 最近一份报告名称。
     */
    private String latestReportName;

    /**
     * 最近一份报告日期。
     */
    private Date latestReportDate;

    /**
     * 最近一份报告分析摘要。
     */
    private String latestReportSummary;

    /**
     * 首页成员健康评分。
     *
     * <p>该评分只作为首页排序和概览提示使用，
     * 不代表医学评估分值。
     */
    private Integer healthScore;

    /**
     * 该成员当前命中的慢病/慢病风险标签。
     */
    private List<String> chronicTags;

    /**
     * 首页个性化排序说明。
     */
    private String sortReason;

    /**
     * 个性化排序权重。
     */
    private Integer sortWeight;
}
