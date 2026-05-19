package com.healthtrail.domain.health.report.command;

import lombok.Data;

/**
 * 报告导出数据请求命令。
 *
 * <p>这个命令不直接表示“下载文件”，而是表示“本次导出想带哪些内容”：
 * 1. App 端分享面板里的勾选项最终都会收敛到这里；
 * 2. 后端根据这份命令统一生成导出文本、文件名和用于图片渲染的数据块；
 * 3. 这样导出内容规则可以逐步从 App 本地拼装收回到服务端统一维护。
 */
@Data
public class HealthReportExportDataCommand {

    /**
     * 导出形态编码。
     *
     * <p>当前与 App 端保持一致：
     * 1. summaryCard
     * 2. detailImage
     * 3. textSummary
     */
    private String format;

    /**
     * 是否隐藏个人信息。
     */
    private Boolean hidePersonalInfo;

    /**
     * 是否包含分析摘要。
     */
    private Boolean includeAnalysisSummary;

    /**
     * 是否包含结果解读。
     */
    private Boolean includeResultInterpretation;

    /**
     * 是否包含 AI 总结。
     */
    private Boolean includeAiSummary;

    /**
     * 是否包含异常指标。
     */
    private Boolean includeAbnormalItems;

    /**
     * 是否包含建议内容。
     */
    private Boolean includeAdvice;

    /**
     * 是否包含完整指标列表。
     */
    private Boolean includeIndicatorItems;

    /**
     * 是否包含补充信息，例如医院和健康问题。
     */
    private Boolean includeSupplementalInfo;
}
