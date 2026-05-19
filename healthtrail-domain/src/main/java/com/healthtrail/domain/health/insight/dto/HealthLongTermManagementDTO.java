package com.healthtrail.domain.health.insight.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 家庭健康长期管理十项能力聚合对象。
 *
 * <p>这个对象不是新增十套独立业务，而是把已有家庭成员、慢病专项、报告指标、
 * 健康问题、用药计划、运营任务和慢病日记重新组织成一个长期管理视图。
 * 这样 App 可以先拥有统一入口，后续再逐项深化计划、指标、随访、协作和隐私能力。
 */
@Data
public class HealthLongTermManagementDTO {

    /** 摘要。 */
    private String summary;

    /** capabilities列表。 */
    private List<CapabilityCardDTO> capabilities = new ArrayList<>();

    /** dAIlyIndicatorBoard。 */
    private DailyIndicatorBoardDTO dailyIndicatorBoard = new DailyIndicatorBoardDTO();

    /** reviewSuggestions列表。 */
    private List<ReviewSuggestionDTO> reviewSuggestions = new ArrayList<>();

    /** visitPackageShare。 */
    private VisitPackageShareDTO visitPackageShare = new VisitPackageShareDTO();

    /** careCollaboration。 */
    private CareCollaborationDTO careCollaboration = new CareCollaborationDTO();

    /** privacyExport。 */
    private PrivacyExportSummaryDTO privacyExport = new PrivacyExportSummaryDTO();

    /** indicatorTargetTrends列表。 */
    private List<IndicatorTargetTrendDTO> indicatorTargetTrends = new ArrayList<>();

    /** medicationSafetyRules列表。 */
    private List<MedicationSafetyRuleDTO> medicationSafetyRules = new ArrayList<>();

    /** careAssignmentSummary。 */
    private CareAssignmentSummaryDTO careAssignmentSummary = new CareAssignmentSummaryDTO();

    /** nextActionHints列表。 */
    private List<NextActionHintDTO> nextActionHints = new ArrayList<>();

    /** memberFocusSummaries列表。 */
    private List<MemberFocusSummaryDTO> memberFocusSummaries = new ArrayList<>();

    /** 十项能力卡片，用于 App 按统一样式展示能力入口和当前状态。 */
    @Data
    public static class CapabilityCardDTO {
        /** 能力编码 */
        private String capabilityCode;
        /** 卡片标题 */
        private String title;
        /** 卡片摘要 */
        private String summary;
        /** 操作按钮文案 */
        private String actionText;
        /** 数据统计数量 */
        private int dataCount;
        /** 风险等级 */
        private String riskLevel;
        /** 是否可执行操作 */
        private boolean actionable;
    }

    /** 日常指标记录看板，血压、血糖、体重等都复用同一套慢病日记存储。 */
    @Data
    public static class DailyIndicatorBoardDTO {
        /** 今日记录数量 */
        private int todayRecordCount;
        /** 近期记录数量 */
        private int recentRecordCount;
        /** 最新记录时间 */
        private Date latestRecordTime;
        /** 指标看板摘要 */
        private String summary;
        /** 支持的指标类型列表 */
        private List<DailyIndicatorTypeDTO> supportedTypes = new ArrayList<>();
        /** 近期记录列表 */
        private List<ChronicDiaryEntryDTO> recentRecords = new ArrayList<>();
        /** 趋势摘要列表 */
        private List<DailyIndicatorTrendSummaryDTO> trendSummaries = new ArrayList<>();
    }

    /** App 快捷记录所需的指标类型说明，不把慢病范围固定在某一个病种。 */
    @Data
    public static class DailyIndicatorTypeDTO {
        /** 记录类型编码 */
        private String entryType;
        /** 类型名称 */
        private String typeName;
        /** 单位提示 */
        private String unitHint;
        /** 输入占位符 */
        private String placeholder;
        /** 描述说明 */
        private String description;
    }

    /**
     * 日常手动指标的轻量趋势摘要。
     *
     * <p>该对象只基于用户已经记录的慢病日记指标生成，不额外保存趋势快照。
     * 这样既能让 App 在长期管理区展示“最近一次较上次是升、降还是持平”，
     * 又不会引入新的统计表和一致性维护成本。
     */
    @Data
    public static class DailyIndicatorTrendSummaryDTO {
        /** 成员ID。 */
        private Long memberId;
        /** 成员姓名。 */
        private String memberName;
        /** 记录类型编码 */
        private String entryType;
        /** 类型名称 */
        private String typeName;
        /** 记录数量 */
        private int recordCount;
        /** 最新值文本 */
        private String latestValueText;
        /** 最新记录时间 */
        private Date latestRecordTime;
        /** 上一次值文本 */
        private String previousValueText;
        /** 变化方向 */
        private String changeDirection;
        /** 变化方向名称 */
        private String changeDirectionName;
        /** 趋势摘要 */
        private String summary;
    }

    /**
     * 慢病指标目标与近期结果的运行时比对结果。
     *
     * <p>三期不额外新建趋势表，而是复用慢病专项里已维护的个人目标范围，
     * 再和近期报告指标做轻量匹配。这样 App 能展示“当前值是否进入目标”，
     * 同时不会把目标范围写回报告原始结果。
     */
    @Data
    public static class IndicatorTargetTrendDTO {
        /** 成员ID。 */
        private Long memberId;
        /** 成员姓名。 */
        private String memberName;
        /** 专项档案ID */
        private Long profileId;
        /** 病种名称 */
        private String diseaseName;
        /** 指标编码 */
        private String indicatorCode;
        /** 指标名称 */
        private String indicatorName;
        /** 最新结果值 */
        private String latestValue;
        /** 最新结果单位 */
        private String latestUnit;
        /** 最新记录时间 */
        private Date latestTime;
        /** 目标下限 */
        private BigDecimal targetMin;
        /** 目标上限 */
        private BigDecimal targetMax;
        /** 目标范围单位 */
        private String targetUnit;
        /** 目标文本说明 */
        private String targetText;
        /** 目标命中状态 */
        private String targetHitStatus;
        /** 目标命中状态名称 */
        private String targetHitStatusName;
        /** 目标趋势摘要 */
        private String summary;
    }

    /** 从慢病专项、健康问题和异常指标推导出的复查建议。 */
    @Data
    public static class ReviewSuggestionDTO {
        /** 建议编码 */
        private String suggestionCode;
        /** 建议标题 */
        private String title;
        /** 建议原因 */
        private String reason;
        /** 目标类型 */
        private String targetType;
        /** 家庭成员ID */
        private Long memberId;
        /** 家庭成员姓名 */
        private String memberName;
        /** 目标业务主键ID */
        private Long targetBizId;
        /** 建议复查时间 */
        private Date suggestReviewTime;
        /** 优先级 */
        private String priorityLevel;
    }

    /** 就医资料包分享摘要，App 可用它做复制、系统分享或本地渲染。 */
    @Data
    public static class VisitPackageShareDTO {
        /** 分享标题 */
        private String title;
        /** 分享摘要 */
        private String summary;
        /** 分享文案 */
        private String shareText;
        /** 长图分享文案 */
        private String longImageText;
        /** 隐私安全分享文案 */
        private String privacySafeShareText;
        /** 资料包分区列表 */
        private List<String> sections = new ArrayList<>();
        /** 分享卡片分区列表 */
        private List<String> shareCardSections = new ArrayList<>();
    }

    /** 家庭照护协作摘要，当前先复用首页运营任务承接手动照护事项。 */
    @Data
    public static class CareCollaborationDTO {
        /** 家庭成员数量 */
        private int memberCount;
        /** 待处理照护任务数量 */
        private int pendingCareTaskCount;
        /** 重点关注成员姓名 */
        private String focusMemberName;
        /** 照护协作摘要 */
        private String summary;
        /** 待处理照护任务列表 */
        private List<CareTaskDTO> pendingTasks = new ArrayList<>();
    }

    @Data
    public static class CareTaskDTO {
        /** 运营任务ID */
        private Long operationTaskId;
        /** 家庭成员ID */
        private Long memberId;
        /** 家庭成员姓名 */
        private String memberName;
        /** 任务标题 */
        private String taskTitle;
        /** 任务内容 */
        private String taskContent;
        /** 风险等级 */
        private String riskLevel;
        /** 生效开始时间 */
        private Date startTime;
    }

    /**
     * 照护任务分派摘要。
     *
     * <p>当前任务表还没有独立协作者字段，所以这里如实展示“负责人账号 + 成员维度”的分派口径。
     * 后续如果补充协作者表，可以在保持该对象结构的基础上把 assigneeRole 扩展为真实家属角色。
     */
    @Data
    public static class CareAssignmentSummaryDTO {
        /** 照护任务总数 */
        private int taskCount;
        /** 负责人任务数量 */
        private int ownerTaskCount;
        /** 成员维度任务数量 */
        private int memberTaskCount;
        /** 分派摘要 */
        private String summary;
        /** 分派明细列表 */
        private List<CareAssignmentItemDTO> assignments = new ArrayList<>();
    }

    @Data
    public static class CareAssignmentItemDTO {
        /** 运营任务ID */
        private Long operationTaskId;
        /** 负责人用户ID */
        private Long ownerUserId;
        /** 家庭成员ID */
        private Long memberId;
        /** 家庭成员姓名 */
        private String memberName;
        /** 任务标题 */
        private String taskTitle;
        /** 风险等级 */
        private String riskLevel;
        /** 生效开始时间 */
        private Date startTime;
        /** 分派角色 */
        private String assigneeRole;
        /** 分派来源 */
        private String assignmentSource;
        /** 分派摘要 */
        private String summary;
    }

    /**
     * 用药安全规则摘要。
     *
     * <p>该对象只承接可从当前用药计划直接推导出的风险规则，
     * 例如重复计划、剂量缺失、频率缺失和已过结束日期仍启用。
     * 它用于提醒用户核对，不替代医生或药师的专业判断。
     */
    @Data
    public static class MedicationSafetyRuleDTO {
        /** 规则编码 */
        private String ruleCode;
        /** 规则名称 */
        private String ruleName;
        /** 风险等级 */
        private String riskLevel;
        /** 规则摘要 */
        private String summary;
        /** 关联用药计划ID列表 */
        private List<Long> relatedPlanIds = new ArrayList<>();
    }

    /**
     * 长期管理下一步建议。
     *
     * <p>该对象把复查建议、目标趋势、用药规则和照护任务统一转成可排序的行动提示。
     * App 只展示后端给出的优先级和动作文案，避免页面自己重新理解十项能力之间的优先关系。
     */
    @Data
    public static class NextActionHintDTO {
        /** 建议编码 */
        private String hintCode;
        /** 建议类型 */
        private String hintType;
        /** 建议标题 */
        private String title;
        /** 建议摘要 */
        private String summary;
        /** 操作按钮文案 */
        private String actionText;
        /** 风险等级 */
        private String riskLevel;
        /** 排序优先级权重 */
        private int priorityWeight;
        /** 家庭成员ID */
        private Long memberId;
        /** 家庭成员姓名 */
        private String memberName;
        /** 目标业务主键ID */
        private Long targetBizId;
        /** 目标业务类型 */
        private String targetBizType;
    }

    /**
     * 家庭成员关注摘要。
     *
     * <p>该对象把长期管理里的复查建议、目标趋势、照护任务和日常趋势按成员收口，
     * 用于回答“当前哪个成员更需要优先看一眼”。它不改变成员、慢病或任务原始数据。
     */
    @Data
    public static class MemberFocusSummaryDTO {
        /** 家庭成员ID */
        private Long memberId;
        /** 家庭成员姓名 */
        private String memberName;
        /** 风险等级 */
        private String riskLevel;
        /** 排序优先级权重 */
        private int priorityWeight;
        /** 下一步建议数量 */
        private int nextActionCount;
        /** 复查建议数量 */
        private int reviewSuggestionCount;
        /** 目标预警数量 */
        private int targetWarningCount;
        /** 照护任务数量 */
        private int careTaskCount;
        /** 用药规则命中数量 */
        private int medicationRuleCount;
        /** 日常趋势记录数量 */
        private int dailyTrendCount;
        /** 关注摘要 */
        private String summary;
        /** 标签列表 */
        private List<String> tags = new ArrayList<>();
    }

    /** 数据导出与隐私工具摘要，只返回类别和数量，不在该对象里携带敏感明细。 */
    @Data
    public static class PrivacyExportSummaryDTO {
        /** 隐私导出摘要 */
        private String summary;
        /** 隐私数据类别列表 */
        private List<PrivacyCategoryDTO> categories = new ArrayList<>();
        /** 隐私提示列表 */
        private List<String> tips = new ArrayList<>();
    }

    @Data
    public static class PrivacyCategoryDTO {
        /** 类别编码 */
        private String categoryCode;
        /** 类别名称 */
        private String categoryName;
        /** 类别描述 */
        private String description;
        /** 数据项数量 */
        private int itemCount;
    }
}
