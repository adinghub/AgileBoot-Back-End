package com.healthtrail.domain.health.report.dto;

import com.healthtrail.domain.health.problem.dto.HealthProblemDTO;
import java.util.List;
import lombok.Data;

/**
 * 报告导出数据返回对象。
 *
 * <p>这份 DTO 服务于 App 端“分享/保存/复制”导出链路，目标是把最终导出内容统一收口到服务端：
 * 1. 返回最终分享文本；
 * 2. 返回最终文件名；
 * 3. 返回图片导出仍需要的结构化区块数据；
 * 4. 让 App 只负责展示与系统分享，不再负责决定导出内容口径。
 */
@Data
public class HealthReportExportDataDTO {

    /**
     * 导出形态编码。
     */
    private String format;

    /**
     * 是否隐藏了个人信息。
     */
    private Boolean hidePersonalInfo;

    /**
     * 报告标题。
     */
    private String reportTitle;

    /**
     * 成员展示名称。
     * 已隐藏时返回 null。
     */
    private String memberLabel;

    /**
     * 报告日期文本。
     */
    private String reportDateText;

    /**
     * 报告类型文本。
     */
    private String reportTypeText;

    /**
     * 医院名称。
     */
    private String hospitalName;

    /**
     * 总指标数量。
     */
    private Integer totalIndicatorCount;

    /**
     * 异常指标数量。
     */
    private Integer abnormalCount;

    /**
     * 待判断指标数量。
     */
    private Integer pendingIndicatorCount;

    /**
     * 分析摘要。
     */
    private String analysisSummary;

    /**
     * 结果解读。
     */
    private String resultInterpretation;

    /**
     * AI 总结。
     */
    private String aiSummary;

    /**
     * 建议摘要。
     */
    private String adviceSummary;

    /**
     * 导出时需要的完整指标列表。
     */
    private List<HealthReportItemDTO> indicatorItems;

    /**
     * 导出时需要的异常指标列表。
     */
    private List<HealthReportItemDTO> abnormalItems;

    /**
     * 导出时需要的建议列表。
     */
    private List<HealthReportAdviceItemDTO> adviceItems;

    /**
     * 导出时需要的重点健康问题列表。
     */
    private List<HealthProblemDTO> healthProblems;

    /**
     * 最终分享文本。
     */
    private String shareText;

    /**
     * 最终导出文件名。
     */
    private String shareFileName;
}
