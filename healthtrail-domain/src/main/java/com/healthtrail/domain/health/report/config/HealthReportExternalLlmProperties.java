package com.healthtrail.domain.health.report.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 体检报告外部大模型接入配置。
 *
 * <p>这里采用“OpenAI 兼容协议”作为默认接入规范，原因是：
 * 1. OpenAI、DeepSeek 以及大量中转网关都支持类似的 Chat Completions 协议
 * 2. 后端只要维护一套请求/响应结构，就能降低后续切换模型供应商的成本
 * 3. 当前业务只需要文本总结，不涉及工具调用或多模态，因此兼容协议已经足够
 */
@Component
@ConfigurationProperties(prefix = "health.report.ai.external-llm")
@Data
public class HealthReportExternalLlmProperties {

    /**
     * 是否启用外部大模型能力。
     *
     * <p>关闭时系统自动回退到本地规则总结，不会影响现有功能链路。
     */
    private boolean enabled = false;

    /**
     * 供应商名称，仅用于日志和提示文案展示。
     */
    private String providerName = "OpenAI-Compatible";

    /**
     * Chat Completions 接口地址。
     * 例如：
     * https://api.openai.com/v1/chat/completions
     */
    private String apiUrl;

    /**
     * 接口密钥。
     */
    private String apiKey;

    /**
     * 模型名称。
     */
    private String model;

    /**
     * 当前配置的外部模型是否支持图片输入。
     *
     * <p>这里单独做成显式配置，而不是靠模型名猜测，原因是同一个“供应商名”下面，
     * 既可能挂纯文本模型，也可能挂视觉模型，甚至还可能接第三方 OpenAI 兼容网关。
     * 如果只靠 provider/model 名称做硬编码判断，后续切换网关时很容易再次踩坑。
     *
     * <p>因此这里要求环境配置明确声明：
     * 1. `true` 表示后端可以向该接口发送 `image_url` 多模态消息；
     * 2. `false` 表示该接口只接受文本消息，图片上传场景需要人工补录或等待后续视觉能力开启。
     */
    private boolean supportsImageInput = false;

    /**
     * 温度参数。
     * 当前默认值偏低，优先保证总结稳定可控。
     */
    private Double temperature = 0.2D;

    /**
     * 最大输出 token 数。
     */
    private Integer maxTokens = 700;

    /**
     * 是否在请求体中显式要求关闭模型思考输出。
     *
     * <p>这个开关的目标非常明确：当接入的兼容网关支持 `thinking.type=disabled`
     * 这类协议时，后端可以主动要求模型不要把思考过程输出到可见内容里，
     * 从而减少：
     * 1. 思考文本占用输出 token；
     * 2. 结构化 JSON 被长段 reasoning 挤压甚至截断；
     * 3. 业务层额外清理思考内容的负担。
     *
     * <p>这里默认保持 `false`，原因是不同供应商 / 不同模型对该参数的支持程度并不完全一致。
     * 只有在确认当前网关支持时，才建议在环境配置里显式打开。
     */
    private boolean disableThinkingOutput = false;

    /**
     * HTTP 超时时间，单位毫秒。
     */
    private Integer timeoutMs = 15000;

    /**
     * 额外透传请求头。
     * 某些兼容网关会要求额外的组织ID、应用ID等信息，这里预留扩展位。
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 自定义系统提示词。
     * 如果未配置，则使用代码内置的健康总结提示词模板。
     */
    private String systemPrompt;
}
