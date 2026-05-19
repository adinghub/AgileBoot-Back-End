package com.healthtrail.domain.health.dashboard.dto;

import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseHomeSummaryDTO;
import java.util.List;
import lombok.Data;

/**
 * App 首页健康看板返回对象。
 *
 * <p>首页看板不是单一业务表的直出数据，而是一个聚合视图，
 * 用于把“成员、提醒、报告”三条高频链路在首页集中呈现。
 * 因此这里使用独立 DTO，避免前端需要拼多次接口才能渲染首页。
 */
@Data
public class HealthHomeDashboardDTO {

    /**
     * 顶部总览统计。
     */
    private HealthHomeOverviewDTO overview;

    /**
     * 家庭成员健康卡片。
     */
    private List<HealthHomeMemberCardDTO> memberCards;

    /**
     * 今日提醒预览列表。
     */
    private List<HealthHomeReminderDTO> todayReminderPreview;

    /**
     * 最近异常报告预览列表。
     */
    private List<HealthHomeReportDTO> recentAbnormalReports;

    /**
     * 首页慢病专项摘要。
     *
     * <p>该字段只承载慢病专项的轻量统计和推荐入口，
     * 完整指标趋势仍由慢病详情看板接口实时计算，避免首页接口过重。
     */
    private ChronicDiseaseHomeSummaryDTO chronicDiseaseSummary;
    /**
     * 首页待跟进事项。
     *
     * <p>该列表用于给首页直接渲染“现在要处理什么”，
     * 避免用户需要分别进入提醒页和报告页才能知道当前最紧急的任务。
     */
    private List<HealthHomeFollowUpItemDTO> followUpItems;

    /**
     * 首页消息预览。
     *
     * <p>该列表用于首页展示“最近消息”和“未读消息入口”，
     * 让用户不进入完整消息中心也能快速看到最新动态。
     */
    private List<HealthHomeMessageDTO> messagePreview;

    /**
     * 首页健康趋势图数据。
     *
     * <p>该趋势图不是医学诊断曲线，而是首页级的“健康关注度变化”摘要，
     * 主要用于首屏快速感知最近报告异常变化趋势。
     */
    private List<HealthHomeHealthTrendPointDTO> healthTrendPoints;

    /**
     * 首页依从率趋势图数据。
     */
    private List<HealthHomeAdherenceTrendPointDTO> adherenceTrendPoints;

    /**
     * 首页 AI 健康建议卡片。
     *
     * <p>这里统一返回已经可展示的摘要、风险等级和建议项，
     * 让 App 首页无需再次拼装多份业务数据。
     */
    private HealthHomeAiAdviceDTO aiHealthAdvice;

    /**
     * 首页任务行为分析摘要。
     *
     * <p>该字段面向首页“轻量行为分析卡片”场景，
     * 让用户在首屏就能快速知道自己最近如何处理待跟进事项。
     */
    private HealthFollowUpTaskAnalyticsDTO followUpTaskAnalytics;
}
