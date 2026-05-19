package com.healthtrail.domain.health.insight.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 健康问题中心返回对象。
 *
 * <p>健康问题中心不重新定义“诊断”，而是把已有 `health_problem` 与
 * `health_problem_evidence` 聚合成 App 可直接消费的待关注清单。
 * 这样报告解析、慢病专项、复查任务后续都可以把线索沉淀到同一问题中心。
 */
@Data
public class HealthProblemCenterDTO {

    /** 问题总数 */
    private int totalCount;

    /** 跟进中问题数量 */
    private int followingCount;

    /** 已缓解问题数量 */
    private int relievedCount;

    /** 已关闭问题数量 */
    private int closedCount;

    /** 高风险问题数量 */
    private int highRiskCount;

    /** 问题中心摘要 */
    private String summary;

    /** 问题列表 */
    private List<ProblemItemDTO> problems = new ArrayList<>();

    @Data
    public static class ProblemItemDTO {
        /** 问题ID。 */
        private Long problemId;
        /** 成员ID。 */
        private Long memberId;
        /** 成员姓名。 */
        private String memberName;
        /** 问题名称。 */
        private String problemName;
        /** 问题类型。 */
        private String problemType;
        /** 问题状态。 */
        private Integer problemStatus;
        /** 问题状态名称 */
        private String problemStatusName;
        /** 风险等级 */
        private Integer riskLevel;
        /** 风险等级名称 */
        private String riskLevelName;
        /** 标准项目编码 */
        private String standardItemCode;
        /** 首次发现日期 */
        private Date firstFoundDate;
        /** 最近随访日期 */
        private Date lastFollowDate;
        /** 问题摘要 */
        private String summary;
        /** 证据数量 */
        private int evidenceCount;
        /**
         * 与该健康问题显式关联的慢病专项。
         *
         * <p>这里返回的是用户或业务流程确认过的关联关系，不做关键词自动推断，
         * 目的是让 App 在健康问题中心直接展示“这个问题归属哪些慢病专项”，并与慢病详情页保持同一数据口径。
         */
        private List<HealthProblemChronicProfileDTO> relatedChronicProfiles = new ArrayList<>();
        /** 近期证据列表 */
        private List<ProblemEvidenceDTO> recentEvidence = new ArrayList<>();
    }

    @Data
    public static class ProblemEvidenceDTO {
        /** 证据记录ID */
        private Long evidenceId;
        /** 证据类型 */
        private String evidenceType;
        /** 关联报告ID */
        private Long reportId;
        /** 关联指标项ID */
        private Long reportItemId;
        /** 证据标题 */
        private String evidenceTitle;
        /** 证据摘要 */
        private String evidenceSummary;
        /** 证据日期 */
        private Date evidenceDate;
        /** 置信度 */
        private Integer confidenceLevel;
        /** 确认状态 */
        private Integer confirmStatus;
    }
}
