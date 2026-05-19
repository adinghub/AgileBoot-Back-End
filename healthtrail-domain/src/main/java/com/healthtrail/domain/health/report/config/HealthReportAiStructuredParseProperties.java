package com.healthtrail.domain.health.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 报告 AI 结构化解析配置。
 *
 * <p>这套配置和“AI 总结”拆开管理，原因是两者虽然都依赖外部大模型，
 * 但业务目标完全不同：
 * 1. 结构化解析关注“把文件文本或图片内容转成标准字段”；
 * 2. AI 总结关注“把结构化结果解释成人能读懂的话”。
 *
 * <p>拆开后可以做到：
 * 1. 只开结构化解析，不开总结；
 * 2. 单独关闭结构化解析排查问题，而不影响已有总结能力；
 * 3. 分别调整输入截断长度和触发门槛。
 */
@Component
@ConfigurationProperties(prefix = "health.report.ai.structured-parse")
@Data
public class HealthReportAiStructuredParseProperties {

    /**
     * 是否启用 AI 结构化解析增强。
     *
     * <p>只有这里开启，且外部大模型本身也配置可用时，才会真正尝试调用。
     */
    private boolean enabled = true;

    /**
     * 触发 AI 增强的最小规则解析条数。
     *
     * <p>例如设置为 2 时：
     * 1. 如果正则一条都没识别出来，触发 AI；
     * 2. 如果只识别出 1 条，也触发 AI；
     * 3. 如果已经稳定识别出多条，就优先相信本地规则，减少额外调用。
     */
    private int minRuleItemCount = 2;

    /**
     * 送给大模型的辅助文本最大长度。
     *
     * <p>限制长度是为了：
     * 1. 控制请求成本；
     * 2. 避免超长整页报告把关键信息淹没；
     * 3. 在大模型网关对输入长度有限制时，保持行为稳定。
     */
    private int maxPromptTextLength = 12000;

    /**
     * 图片类报告是否优先尝试 AI 视觉结构化解析。
     *
     * <p>开启后，JPG / PNG 这类拍照或截图报告会优先把图片本身交给大模型识别，
     * 由模型直接利用版面、表格列关系和上下文抽取结构化字段。
     *
     * <p>即使开启了这里，只要外部模型不可用、图片过大、或模型返回无效 JSON，
     * 系统仍会自动回退到当前的文本规则解析流程，不会影响基础可用性。
     */
    private boolean preferVisionOnImage = true;

    /**
     * 允许送给大模型做视觉解析的图片最大字节数。
     *
     * <p>这里限制的是“原始文件字节数”，不是 Base64 后的长度。
     * 目的是避免：
     * 1. 用户上传超大原图时请求体暴涨；
     * 2. 兼容网关对请求大小有限制时直接报错；
     * 3. 一张图拖慢整个上传后的后台解析任务。
     */
    private int maxImageBytes = 2 * 1024 * 1024;

    /**
     * 在启用视觉解析时，是否把文件中已能直接抽取到的文本一并作为补充上下文发给模型。
     *
     * <p>默认开启是为了兼容少量“图片 + 可抽取文本”并存的报告格式。
     * 该字段不代表 OCR 能力，服务端不会再为了生成这段文本调用云 OCR 或本地 OCR 命令。
     */
    private boolean includeExtractedTextWhenVisionEnabled = true;
}
