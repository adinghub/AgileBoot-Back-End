package com.healthtrail.domain.health.chronic.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 慢病专项详情看板返回对象。
 *
 * <p>该对象把“用户维护的专项档案”和“报告/问题/用药沉淀出的运行时数据”放在一起，
 * 让 App 详情页可以一次性展示专项全貌，避免页面层自行拼装不同业务数据。
 */
@Data
public class ChronicDiseaseDashboardDTO {

    /** 慢病专项档案 */
    private ChronicDiseaseProfileDTO profile;

    /** 最新报告日期 */
    private Date latestReportDate;

    /** 关注指标数量 */
    private int focusIndicatorCount;

    /**
     * 当前慢病专项下已启用的个性化指标目标数量。
     *
     * <p>目标数量单独返回，是为了让 App 能清晰告诉用户：哪些指标只是从报告中识别出来，
     * 哪些指标已经有个人目标范围可以进行比对。
     */
    private int indicatorTargetCount;

    /** 异常健康问题数量 */
    private int abnormalProblemCount;

    /** 活跃用药计划数量 */
    private int activeMedicationPlanCount;

    /** 看板摘要 */
    private String dashboardSummary;

    /**
     * 当前仍有效的复查任务。
     *
     * <p>看板页直接返回该任务，是为了让用户先看到“已经安排过复查”，
     * 再决定是否需要调整提醒时间，避免同一专项反复创建重复任务。
     */
    private ChronicDiseaseReviewTaskDTO reviewTask;

    /** 关联的健康问题列表 */
    private List<ChronicDiseaseRelatedProblemDTO> relatedProblems = new ArrayList<>();

    /** 指标趋势列表 */
    private List<ChronicDiseaseIndicatorTrendDTO> indicatorTrends = new ArrayList<>();
}
