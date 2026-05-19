package com.healthtrail.domain.health.report.dto;

import java.util.List;
import lombok.Data;

/**
 * 体检报告分析摘要返回对象。
 *
 * <p>当前阶段这是一个“规则化摘要”，不宣称医学诊断结论，
 * 目的是给 App 前端快速展示报告概览与异常项提示。
 */
@Data
public class HealthReportAnalysisDTO {

    /** 报告ID。 */
    private Long reportId;

    /** parseStatus。 */
    private Integer parseStatus;

    /** totalItemCount。 */
    private Integer totalItemCount;

    /** normalItemCount。 */
    private Integer normalItemCount;

    /** lowItemCount。 */
    private Integer lowItemCount;

    /** highItemCount。 */
    private Integer highItemCount;

    /** abnormalItemCount。 */
    private Integer abnormalItemCount;

    /** unknownItemCount。 */
    private Integer unknownItemCount;

    /** 摘要。 */
    private String summary;

    /**
     * 异常项明细。
     * 这里仅返回异常子集，方便前端在摘要区域快速渲染重点内容。
     */
    private List<HealthReportItemDTO> abnormalItems;
}
