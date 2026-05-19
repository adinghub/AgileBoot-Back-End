package com.healthtrail.domain.health.insight.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 健康洞察工作台聚合返回对象。
 *
 * <p>该 DTO 对应健康洞察与长期管理能力的统一 App 入口：
 * 1. 复查闭环
 * 2. 指标异常趋势预警
 * 3. 就医资料包
 * 4. 慢病日记
 * 5. 家属照护看板
 * 6. 用药安全
 * 7. 健康周报/月报
 * 8. 语音录入
 * 9. 健康时间线
 * 10. 家庭健康长期管理十项能力
 *
 * <p>它不是替代原有业务模块，而是把已有慢病、报告、用药、家庭成员、消息和任务数据，
 * 按“健康运营工作台”的方式重新组织，给 App 提供一个可持续扩展的统一入口。
 */
@Data
public class HealthInsightWorkbenchDTO {

    /** 复查闭环摘要。 */
    private ReviewLoopSummaryDTO reviewLoop = new ReviewLoopSummaryDTO();

    /** 指标异常趋势预警列表。 */
    private List<IndicatorTrendAlertDTO> indicatorAlerts = new ArrayList<>();

    /** 就医资料包摘要。 */
    private MedicalVisitPackageDTO medicalVisitPackage = new MedicalVisitPackageDTO();

    /** 慢病日记摘要。 */
    private ChronicDiarySummaryDTO chronicDiary = new ChronicDiarySummaryDTO();

    /** 家属照护看板摘要。 */
    private CareDashboardDTO careDashboard = new CareDashboardDTO();

    /** 用药安全摘要。 */
    private MedicationSafetyDTO medicationSafety = new MedicationSafetyDTO();

    /** 健康周报/月报摘要（兼容旧版App单击）。 */
    private AiPeriodicReportDTO aiPeriodicReport = new AiPeriodicReportDTO();

    /**
     * 可切换的健康周报/月报摘要。
     *
     * <p>保留 aiPeriodicReport 是为了兼容旧 App；新页面优先使用该列表展示周报和月报。
     */
    private List<AiPeriodicReportDTO> periodicReports = new ArrayList<>();

    /** 语音录入引导。 */
    private VoiceEntryGuideDTO voiceEntry = new VoiceEntryGuideDTO();

    /** 健康时间线条目列表。 */
    private List<HealthTimelineItemDTO> timelineItems = new ArrayList<>();

    /**
     * 家庭健康长期管理聚合数据。
     *
     * <p>该字段把计划、日常指标、随访模板、复查建议、问题生命周期、资料包、用药安全、
     * 家庭协作、周月报和隐私摘要收口到同一个对象，避免 App 为十个能力分别拉取和拼装数据。
     */
    private HealthLongTermManagementDTO longTermManagement = new HealthLongTermManagementDTO();

    /** 复查/复诊闭环摘要。 */
    @Data
    public static class ReviewLoopSummaryDTO {
        /** 活跃慢病专项数量 */
        private int activeProfileCount;
        /** 待处理复查任务数量 */
        private int pendingReviewTaskCount;
        /** 已到期复查任务数量 */
        private int dueReviewTaskCount;
        /** 下一个复查任务ID */
        private Long nextReviewTaskId;
        /** 下一个复查专项档案ID */
        private Long nextReviewProfileId;
        /** 下一个复查时间 */
        private Date nextReviewTime;
        /** 复查摘要 */
        private String summary;
    }

    /** 指标异常趋势预警项。 */
    @Data
    public static class IndicatorTrendAlertDTO {
        /** 成员ID。 */
        private Long memberId;
        /** 成员姓名。 */
        private String memberName;
        /** 报告ID。 */
        private Long reportId;
        /** 最新报告日期 */
        private Date latestReportDate;
        /** 上一次报告ID */
        private Long previousReportId;
        /** 上一次报告日期 */
        private Date previousReportDate;
        /** 指标编码 */
        private String indicatorCode;
        /** 指标名称 */
        private String indicatorName;
        /** 异常标记名称。 */
        private String abnormalFlagName;
        /** 异常次数 */
        private int abnormalCount;
        /** 连续异常次数 */
        private int consecutiveAbnormalCount;
        /** 最新结果值 */
        private String latestResultValue;
        /** 最新结果单位 */
        private String latestResultUnit;
        /** 上一次结果值 */
        private String previousResultValue;
        /** 上一次结果单位 */
        private String previousResultUnit;
        /** 变化方向 */
        private String changeDirection;
        /** 变化方向名称 */
        private String changeDirectionName;
        /** 变化百分比 */
        private java.math.BigDecimal changePercent;
        /** 变化摘要 */
        private String changeSummary;
        /** 预警规则类型 */
        private String ruleType;
        /** 预警规则类型名称 */
        private String ruleTypeName;
        /** 风险等级 */
        private String riskLevel;
        /** 预警标题 */
        private String alertTitle;
        /** 预警内容 */
        private String alertContent;
        /** 预警原因列表 */
        private List<String> alertReasons = new ArrayList<>();
    }

    /** 就医资料包摘要。 */
    @Data
    public static class MedicalVisitPackageDTO {
        /** 资料包标题 */
        private String packageTitle;
        /** 家庭成员数量 */
        private int memberCount;
        /** 慢病专项数量 */
        private int chronicProfileCount;
        /** 近期报告数量 */
        private int recentReportCount;
        /** 活跃用药计划数量 */
        private int activeMedicationPlanCount;
        /** 异常指标数量 */
        private int abnormalIndicatorCount;
        /** 资料包摘要 */
        private String summary;
        /** 资料包分区列表 */
        private List<String> sections = new ArrayList<>();
    }

    /** 慢病日记摘要。 */
    @Data
    public static class ChronicDiarySummaryDTO {
        /** 日记记录数量 */
        private int diaryCount;
        /** 最新记录时间 */
        private Date latestRecordTime;
        /** 最新标题 */
        private String latestTitle;
        /** 日记摘要 */
        private String summary;
        /** 最新日记列表 */
        private List<ChronicDiaryEntryDTO> latestEntries = new ArrayList<>();
    }

    /** 家属照护看板摘要。 */
    @Data
    public static class CareDashboardDTO {
        /** 家庭成员数量 */
        private int memberCount;
        /** 待处理提醒数量 */
        private int pendingReminderCount;
        /** 异常报告数量 */
        private int abnormalReportCount;
        /** 高风险慢病专项数量 */
        private int highRiskChronicProfileCount;
        /** 重点关注成员ID */
        private Long focusMemberId;
        /** 重点关注成员姓名 */
        private String focusMemberName;
        /** 照护摘要 */
        private String summary;
    }

    /** 用药安全摘要。 */
    @Data
    public static class MedicationSafetyDTO {
        /** 活跃用药计划数量 */
        private int activeMedicationPlanCount;
        /** 重复用药数量 */
        private int duplicateMedicationCount;
        /** 低库存药品数量 */
        private int lowStockDrugCount;
        /** 近效期批次数量 */
        private int nearExpireBatchCount;
        /** 用药安全摘要 */
        private String summary;
        /** 警告列表 */
        private List<String> warnings = new ArrayList<>();
    }

    /** 健康周报/月报摘要。 */
    @Data
    public static class AiPeriodicReportDTO {
        /** 报告类型 */
        private String reportType;
        /** 报告标题 */
        private String title;
        /** 报告摘要 */
        private String summary;
        /** 生成时间 */
        private Date generatedTime;
        /** 建议列表 */
        private List<String> suggestions = new ArrayList<>();
    }

    /** 语音录入引导。 */
    @Data
    public static class VoiceEntryGuideDTO {
        /** 是否支持语音录入 */
        private boolean supported;
        /** 语音录入流程说明 */
        private String flow;
        /** 示例文本 */
        private String exampleText;
        /** 语音录入摘要 */
        private String summary;
    }

    /** 健康时间线条目。 */
    @Data
    public static class HealthTimelineItemDTO {
        /** 时间线条目类型 */
        private String itemType;
        /** 事件时间 */
        private Date eventTime;
        /** 家庭成员ID */
        private Long memberId;
        /** 家庭成员姓名 */
        private String memberName;
        /** 业务主键ID */
        private Long businessId;
        /** 条目标题 */
        private String title;
        /** 条目内容 */
        private String content;
        /** 目标页面编码 */
        private String targetPageCode;
        /** 目标业务类型 */
        private String targetBizType;
    }
}
