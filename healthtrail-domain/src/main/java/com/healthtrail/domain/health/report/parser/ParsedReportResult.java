package com.healthtrail.domain.health.report.parser;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 后台解析结果。
 *
 * <p>除了结构化指标本身，这里也承载从附件里恢复出的报告级候选信息。
 * 当前首先支持：
 * 1. reportDate：解决“用户上传时没填日期，但附件正文里能提取到”的场景
 * 2. recognizedReportType：保存模型识别出的更细粒度报告类型，方便前端做展示补充
 */
@Data
public class ParsedReportResult {

    private String extractedText;

    /**
     * 从当前附件内容中恢复出的报告日期候选值。
     *
     * <p>它只是解析阶段给应用层的建议值，真正是否写回数据库，
     * 仍由应用服务按“用户手填优先、仅空值回填”的规则决定。
     */
    private Date reportDate;

    /**
     * 从当前附件内容中识别出的报告细分类型。
     *
     * <p>这个字段不会直接覆盖用户手填的 reportType，
     * 而是作为“识别建议值”单独存储下来。
     * 这样前端展示时可以做“血液检查（血常规）”这类补充提示，
     * 同时保留用户原始输入，避免把两种来源的语义混在一起。
     */
    private String recognizedReportType;

    private String message;

    private List<ParsedReportItem> items = new ArrayList<>();

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
}
