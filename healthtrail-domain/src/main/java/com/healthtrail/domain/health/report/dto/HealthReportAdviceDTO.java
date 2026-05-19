package com.healthtrail.domain.health.report.dto;

import java.util.List;
import lombok.Data;

/**
 * 体检报告异常建议返回对象。
 *
 * <p>该对象用于承接“报告异常分析之后的业务动作建议”，
 * 它不替代医学诊断，也不输出治疗方案，
 * 而是把当前系统能给 App 端落地的建议统一组织起来：
 * 1. 哪些异常项值得优先关注
 * 2. 当前建议用户做什么
 * 3. 是否可以联动现有用药计划继续跟踪
 */
@Data
public class HealthReportAdviceDTO {

    /** 报告ID。 */
    private Long reportId;

    /** 成员ID。 */
    private Long memberId;

    /** 成员姓名。 */
    private String memberName;

    /**
     * 异常项数量。
     * 这里的口径与报告分析摘要保持一致，只统计偏低、偏高、异常三类。
     */
    private Integer abnormalItemCount;

    /**
     * 当前成员是否存在启用中的用药计划。
     */
    private Boolean hasActiveMedicationPlan;

    /**
     * 当前成员启用中的用药计划数量。
     */
    private Integer activeMedicationPlanCount;

    /**
     * 建议摘要。
     * 用于前端在详情页顶部快速展示本次建议的总述。
     */
    private String summary;

    /**
     * 具体建议列表。
     */
    private List<HealthReportAdviceItemDTO> adviceItems;
}
