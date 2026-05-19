package com.healthtrail.domain.health.report.llm;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.healthtrail.domain.health.report.config.HealthReportExternalLlmProperties;
import com.healthtrail.domain.health.report.parser.HealthReportRetryableParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 体检报告外部大模型客户端。
 *
 * <p>当前只承担一件事情：把“系统提示词 + 用户提示词”发送到兼容的 Chat Completions 接口，
 * 并提取首个可用文本回复。
 * 之所以单独抽成组件，而不是把 HTTP 请求直接写进应用服务，是为了：
 * 1. 让业务服务专注在“构建总结上下文”
 * 2. 让外部模型通信细节集中管理
 * 3. 以后如需改成流式、切换供应商或增加鉴权方式时，改动范围更可控
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportExternalLlmClient {

    /** 外部大模型配置属性 */
    private final HealthReportExternalLlmProperties properties;

    /**
     * 调用外部大模型生成总结文本。
     *
     * @return 成功时返回模型文本；未启用、未配置或调用失败时返回 {@code null}
     */
    public String generateSummary(String systemPrompt, String userPrompt) {
        return executeGenerateText("AI总结-文本", systemPrompt, buildPlainTextUserContent(userPrompt));
    }

    /**
     * 调用外部大模型生成通用文本结果。
     *
     * <p>保留这个通用入口后，报告模块可以同时复用它做两件事：
     * 1. 生成自然语言总结；
     * 2. 生成结构化 JSON 解析结果。
     *
     * <p>这样能避免“总结一套客户端、结构化解析又一套客户端”的重复维护。
     */
    public String generateText(String systemPrompt, String userPrompt) {
        return executeGenerateText("通用文本调用", systemPrompt, buildPlainTextUserContent(userPrompt));
    }

    /**
     * 调用外部大模型生成“纯 JSON 文本”结果。
     *
     * <p>该入口主要给结构化提取类场景使用。相比只在提示词里要求“请返回 JSON”，
     * 这里会进一步在请求体里显式声明 `response_format=json_object`，尽量减少：
     * 1. 模型返回思考过程；
     * 2. 模型返回额外解释文字；
     * 3. 模型把 JSON 包在自然语言说明里。
     *
     * <p>不同 OpenAI 兼容网关对这个字段的支持程度可能不完全一致，
     * 因此调用方仍然需要保留兜底的 JSON 提取/清洗逻辑。
     */
    public String generateJsonText(String systemPrompt, String userPrompt) {
        return executeGenerateText("结构化JSON-文本", systemPrompt, buildPlainTextUserContent(userPrompt), true);
    }

    /**
     * 调用外部大模型生成“可重试”的结构化 JSON 文本结果。
     *
     * <p>这个入口和普通 `generateJsonText(...)` 的唯一区别在于：
     * 当本次失败明显属于“第三方暂时不可用”时，它不会简单返回 `null`，
     * 而是抛出 `HealthReportRetryableParseException`，交给上层后台解析队列决定是否重试。
     *
     * <p>这样可以避免把“临时超时”误判成“真的识别不到任何指标”。
     */
    public String generateRetryableJsonText(String systemPrompt, String userPrompt) {
        return executeGenerateText("结构化JSON-文本", systemPrompt, buildPlainTextUserContent(userPrompt), true, true);
    }

    /**
     * 调用外部大模型生成“图片 + 文本”联合输入的结果。
     *
     * <p>这里主要给体检报告结构化解析使用：
     * 1. 图片负责提供表格版式、列对齐和视觉上下文；
     * 2. 用户提示词可携带从 PDF 或文本文件中直接抽出的辅助文本；
     * 3. 服务端不会为了生成辅助文本调用 OCR，图片内容主要由视觉模型直接读取。
     *
     * <p>注意这里传入的 imageDataUrl 需要是标准 data URL，例如：
     * `data:image/jpeg;base64,xxxx`
     * 这样就不依赖对象存储外链是否可被第三方模型网关直接访问。
     */
    public String generateTextWithImage(String systemPrompt, String userPrompt, String imageDataUrl) {
        JSONArray userContent = new JSONArray();
        if (StrUtil.isNotBlank(userPrompt)) {
            userContent.add(JSONUtil.createObj()
                .set("type", "text")
                .set("text", userPrompt));
        }
        if (StrUtil.isNotBlank(imageDataUrl)) {
            // 多模态输入优先走“图片原始内容直传”，避免依赖 MinIO / OSS 外链可达性。
            //
            // 这里额外做一层供应商兼容，是因为智谱官方 GLM-4.6V-Flash 文档示例里，
            // `image_url.url` 传的是“纯 Base64 字符串”，而不是 `data:image/...;base64,...`。
            // 如果把 data URL 原样发给 open.bigmodel.cn，网关有概率直接按非法图片字段拒绝请求。
            userContent.add(JSONUtil.createObj()
                .set("type", "image_url")
                .set("image_url", JSONUtil.createObj().set("url", normalizeImagePayloadForProvider(imageDataUrl))));
        }
        return executeGenerateText("通用多模态调用", systemPrompt, userContent);
    }

    /**
     * 调用外部大模型生成“图片 + 文本”的纯 JSON 结果。
     *
     * <p>这是视觉结构化解析的首选入口：
     * 1. 图片负责版面理解；
     * 2. 文本负责补充辅助上下文；
     * 3. `response_format=json_object` 负责尽量约束输出为 JSON。
     */
    public String generateJsonTextWithImage(String systemPrompt, String userPrompt, String imageDataUrl) {
        JSONArray userContent = new JSONArray();
        if (StrUtil.isNotBlank(userPrompt)) {
            userContent.add(JSONUtil.createObj()
                .set("type", "text")
                .set("text", userPrompt));
        }
        if (StrUtil.isNotBlank(imageDataUrl)) {
            userContent.add(JSONUtil.createObj()
                .set("type", "image_url")
                .set("image_url", JSONUtil.createObj().set("url", normalizeImagePayloadForProvider(imageDataUrl))));
        }
        return executeGenerateText("结构化JSON-多模态", systemPrompt, userContent, true);
    }

    /**
     * 调用外部大模型生成“可重试”的图片 + 文本结构化 JSON 结果。
     *
     * <p>图片视觉解析比纯文本结构化更容易受到：
     * 1. 单次处理时长；
     * 2. 网关繁忙程度；
     * 3. 多模态链路稳定性；
     * 影响。
     *
     * <p>因此这里提供可重试版本，让上层在超时、429、5xx 等临时故障下可以自动回队列。
     */
    public String generateRetryableJsonTextWithImage(String systemPrompt, String userPrompt, String imageDataUrl) {
        JSONArray userContent = new JSONArray();
        if (StrUtil.isNotBlank(userPrompt)) {
            userContent.add(JSONUtil.createObj()
                .set("type", "text")
                .set("text", userPrompt));
        }
        if (StrUtil.isNotBlank(imageDataUrl)) {
            userContent.add(JSONUtil.createObj()
                .set("type", "image_url")
                .set("image_url", JSONUtil.createObj().set("url", normalizeImagePayloadForProvider(imageDataUrl))));
        }
        return executeGenerateText("结构化JSON-多模态", systemPrompt, userContent, true, true);
    }

    /**
     * 判断当前外部大模型配置是否可用于文本或多模态生成。
     *
     * <p>业务层用这个入口，而不是自己重复判断配置项，是为了把“配置可用性口径”
     * 收敛到同一处，避免不同调用方出现不一致行为。
     */
    public boolean isAvailable() {
        if (!properties.isEnabled()) {
            return false;
        }
        if (StrUtil.hasBlank(properties.getApiUrl(), properties.getApiKey(), properties.getModel())) {
            log.warn("外部大模型已启用，但配置不完整：apiUrl/model/apiKey 至少有一个为空，将回退到规则总结");
            return false;
        }
        return true;
    }

    /**
     * 真正执行外部模型请求的内部入口。
     *
     * <p>这里刻意不用 `generateText` 继续重载，原因是 Java 在 `String` 与 `Object`
     * 重载并存时，`generateText(String, String)` 内部再次传入字符串会优先命中自身，
     * 很容易形成无限递归。改成独立方法名后，文本链路和多模态链路都会显式汇聚到这一处。
     */
    private String executeGenerateText(String sceneName, String systemPrompt, Object userContent) {
        return executeGenerateText(sceneName, systemPrompt, userContent, false);
    }

    /**
     * 带输出格式约束的统一调用入口。
     *
     * <p>`jsonMode=true` 时会尽量要求兼容网关按 JSON Object 形式返回。
     * 这里故意不把它做成全局默认值，因为报告总结、结果解读等自然语言场景仍然需要自由文本输出。
     */
    private String executeGenerateText(String sceneName, String systemPrompt, Object userContent, boolean jsonMode) {
        return executeGenerateText(sceneName, systemPrompt, userContent, jsonMode, false);
    }

    /**
     * 带“可重试异常透传”能力的统一调用入口。
     *
     * <p>这里额外引入 `propagateRetryableFailure` 的原因是：
     * 1. 报告结构化主链路希望把临时故障交给后台队列重试；
     * 2. 但逐项解释、总结文案这类增强能力，不应该因为一次超时就打断整个主流程。
     *
     * <p>因此是否把失败上抛为“可重试异常”，由调用方按业务重要性明确选择。
     */
    private String executeGenerateText(String sceneName, String systemPrompt, Object userContent, boolean jsonMode,
        boolean propagateRetryableFailure) {
        if (!isAvailable()) {
            log.info("外部大模型调用跳过，scene={}, reason=配置不可用", sceneName);
            return null;
        }
        String providerName = StrUtil.blankToDefault(getProviderName(), "OpenAI-Compatible");
        String modelName = StrUtil.blankToDefault(properties.getModel(), "UNKNOWN");
        String userContentPreview = buildUserContentPreview(userContent);
        long startTimeMillis = System.currentTimeMillis();
        log.info("外部大模型开始调用，scene={}, provider={}, model={}, jsonMode={}, systemPromptLength={}, userContentPreview={}",
            sceneName, providerName, modelName,
            jsonMode,
            systemPrompt == null ? 0 : systemPrompt.length(),
            limitLength(userContentPreview, 600));
        try (HttpResponse response = buildRequest(systemPrompt, userContent, jsonMode).execute()) {
            if (response == null) {
                log.warn("外部大模型调用未获得响应，scene={}, provider={}, model={}", sceneName, providerName, modelName);
                throwRetryableIfNecessary(propagateRetryableFailure,
                    StrUtil.format("外部大模型未获得响应，scene={}, provider={}, model={}", sceneName, providerName, modelName), null);
                log.info("外部大模型调用结束，scene={}, provider={}, model={}, success=false, durationMs={}",
                    sceneName, providerName, modelName, System.currentTimeMillis() - startTimeMillis);
                return null;
            }
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                log.warn("外部大模型调用失败，scene={}, provider={}, model={}, status={}, body={}",
                    sceneName, providerName, modelName, response.getStatus(), limitLength(response.body(), 1000));
                if (propagateRetryableFailure && isRetryableHttpStatus(response.getStatus())) {
                    throw new HealthReportRetryableParseException(StrUtil.format(
                        "外部大模型临时失败，scene={}, provider={}, model={}, status={}",
                        sceneName, providerName, modelName, response.getStatus()));
                }
                log.info("外部大模型调用结束，scene={}, provider={}, model={}, success=false, durationMs={}",
                    sceneName, providerName, modelName, System.currentTimeMillis() - startTimeMillis);
                return null;
            }
            String responseText = extractContent(response.body());
            // 某些“带思考能力”的兼容模型即使在 JSON 模式下，也会把 `<think>...</think>`
            // 混到最终 content 里。
            //
            // 这里统一在客户端出口做一次收口，目的有两层：
            // 1. 日志里不再直接暴露思考过程，避免排查时被长段 reasoning 淹没；
            // 2. 业务层默认拿到“可消费的最终答案”，减少每个调用方重复做 `<think>` 清洗。
            //
            // 注意：我们只清理“显式包在 think 标签中的内容”，不会擅自改写模型正文。
            String sanitizedResponseText = sanitizeVisibleResponseContent(responseText);
            log.info("外部大模型调用结果，scene={}, provider={}, model={}, status={}, responsePreview={}",
                sceneName, providerName, modelName, response.getStatus(), limitLength(sanitizedResponseText, 600));
            log.info("外部大模型调用结束，scene={}, provider={}, model={}, success={}, durationMs={}",
                sceneName, providerName, modelName, StrUtil.isNotBlank(sanitizedResponseText),
                System.currentTimeMillis() - startTimeMillis);
            return sanitizedResponseText;
        } catch (Exception ex) {
            if (ex instanceof HealthReportRetryableParseException retryableParseException) {
                log.info("外部大模型调用结束，scene={}, provider={}, model={}, success=false, durationMs={}",
                    sceneName, providerName, modelName, System.currentTimeMillis() - startTimeMillis);
                throw retryableParseException;
            }
            log.error("外部大模型调用异常，scene={}, provider={}, model={}", sceneName, providerName, modelName, ex);
            if (propagateRetryableFailure && isRetryableException(ex)) {
                throw new HealthReportRetryableParseException(StrUtil.format(
                    "外部大模型调用超时或网络异常，scene={}, provider={}, model={}",
                    sceneName, providerName, modelName), ex);
            }
            log.info("外部大模型调用结束，scene={}, provider={}, model={}, success=false, durationMs={}",
                sceneName, providerName, modelName, System.currentTimeMillis() - startTimeMillis);
            return null;
        }
    }

    /**
     * 对外暴露当前供应商名称，方便业务层回填提示文案。
     */
    public String getProviderName() {
        return properties.getProviderName();
    }

    private HttpRequest buildRequest(String systemPrompt, Object userContent, boolean jsonMode) {
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", properties.getModel());
        requestBody.set("temperature", properties.getTemperature());
        if (properties.getMaxTokens() != null) {
            requestBody.set("max_tokens", properties.getMaxTokens());
        }
        if (properties.isDisableThinkingOutput()) {
            // 这里通过兼容接口的 `thinking.type=disabled` 主动向上游表达：
            // “不要把思考过程输出到可见内容中”。
            //
            // 这样做的核心收益不是日志更干净，而是节省输出 token，
            // 尤其是在结构化 JSON 场景里，可以降低“thinking 占掉了配额，真正 JSON 被截断”的风险。
            //
            // 由于不同网关对这个字段的支持程度可能不同，所以真正是否开启由配置控制，
            // 代码层只负责在开启后稳定透传，不强制所有环境默认带上。
            requestBody.set("thinking", JSONUtil.createObj().set("type", "disabled"));
        }
        if (jsonMode) {
            // 结构化抽取场景显式要求返回 JSON Object。
            // 即便上游网关不完全遵守，这个信号也通常能显著降低“思考文本 + JSON 混排”的概率。
            requestBody.set("response_format", JSONUtil.createObj().set("type", "json_object"));
        }
        requestBody.set("messages", buildMessages(systemPrompt, userContent));

        HttpRequest request = HttpRequest.post(properties.getApiUrl())
            .timeout(properties.getTimeoutMs() == null ? 15000 : properties.getTimeoutMs())
            .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
            .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
            .body(requestBody.toString());
        if (properties.getHeaders() != null && !properties.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : properties.getHeaders().entrySet()) {
                if (StrUtil.isNotBlank(entry.getKey()) && StrUtil.isNotBlank(entry.getValue())) {
                    request.header(entry.getKey(), entry.getValue());
                }
            }
        }
        return request;
    }

    private JSONArray buildMessages(String systemPrompt, Object userContent) {
        JSONArray messages = new JSONArray();
        messages.add(JSONUtil.createObj()
            .set("role", "system")
            .set("content", StrUtil.blankToDefault(systemPrompt, "")));
        messages.add(JSONUtil.createObj()
            .set("role", "user")
            .set("content", userContent == null ? "" : userContent));
        return messages;
    }

    /**
     * 统一构造纯文本用户消息内容。
     *
     * <p>单独收在这里，是为了让纯文本、多模态两条链路最终都走到相同的
     * `executeGenerateText(systemPrompt, userContent)` 主流程里，减少分叉逻辑。
     */
    private String buildPlainTextUserContent(String userPrompt) {
        return StrUtil.blankToDefault(userPrompt, "");
    }

    /**
     * 把用户输入内容压缩成适合日志查看的摘要。
     *
     * <p>日志的目标是帮助排查“有没有发出去、发了什么类型的数据”，
     * 而不是把完整 prompt 或整段 Base64 图片重新打到日志里。
     * 因此这里做两件事：
     * 1. 文本输入只保留截断预览；
     * 2. 多模态输入只描述类型、文本长度和图片条数，不输出完整 data URL。
     */
    private String buildUserContentPreview(Object userContent) {
        if (userContent == null) {
            return "";
        }
        if (userContent instanceof CharSequence) {
            return userContent.toString();
        }
        if (userContent instanceof JSONArray jsonArray) {
            JSONArray previewArray = new JSONArray();
            for (int index = 0; index < jsonArray.size(); index++) {
                Object item = jsonArray.get(index);
                if (!(item instanceof JSONObject jsonObject)) {
                    previewArray.add(StrUtil.blankToDefault(Objects.toString(item, null), ""));
                    continue;
                }
                String type = jsonObject.getStr("type");
                if ("text".equals(type)) {
                    previewArray.add(JSONUtil.createObj()
                        .set("type", "text")
                        .set("textLength", StrUtil.length(jsonObject.getStr("text")))
                        .set("textPreview", limitLength(jsonObject.getStr("text"), 200)));
                    continue;
                }
                if ("image_url".equals(type)) {
                    String imageUrl = jsonObject.getByPath("image_url.url", String.class);
                    previewArray.add(JSONUtil.createObj()
                        .set("type", "image_url")
                        .set("isDataUrl", StrUtil.startWithIgnoreCase(StrUtil.blankToDefault(imageUrl, ""), "data:"))
                        .set("imageUrlLength", StrUtil.length(imageUrl)));
                    continue;
                }
                previewArray.add(JSONUtil.createObj()
                    .set("type", type)
                    .set("rawPreview", limitLength(jsonObject.toString(), 200)));
            }
            return previewArray.toString();
        }
        return userContent.toString();
    }

    /**
     * 兼容不同供应商对 content 字段的返回形式：
     * 1. 标准字符串
     * 2. 数组分片（例如 content 数组）
     * 3. 旧式 text 字段
     */
    private String extractContent(String responseBody) {
        if (StrUtil.isBlank(responseBody)) {
            return null;
        }
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        JSONObject firstChoice = choices.getJSONObject(0);
        Object messageContent = firstChoice.getByPath("message.content");
        String normalizedContent = normalizeContent(messageContent);
        if (StrUtil.isNotBlank(normalizedContent)) {
            return normalizedContent;
        }
        return StrUtil.blankToDefault(firstChoice.getStr("text"), null);
    }

    private String normalizeContent(Object rawValue) {
        if (rawValue == null || rawValue instanceof JSONNull) {
            return null;
        }
        if (rawValue instanceof CharSequence) {
            return rawValue.toString().trim();
        }
        if (rawValue instanceof JSONArray) {
            List<String> textParts = ((JSONArray) rawValue).stream()
                .map(this::readArrayContentPart)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
            return textParts.isEmpty() ? null : StrUtil.join("", textParts).trim();
        }
        return rawValue.toString().trim();
    }

    private String readArrayContentPart(Object item) {
        if (item == null || item instanceof JSONNull) {
            return null;
        }
        if (item instanceof CharSequence) {
            return item.toString();
        }
        if (item instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) item;
            return StrUtil.blankToDefault(jsonObject.getStr("text"), jsonObject.getStr("content"));
        }
        return item.toString();
    }

    /**
     * 按供应商文档把图片字段收敛成对方更容易接受的格式。
     *
     * <p>当前重点兼容智谱 `open.bigmodel.cn` / `Zhipu-GLM` 多模态接口：
     * 官方 GLM-4.6V-Flash Java 示例中的 `image_url.url` 传的是纯 Base64 字符串，
     * 不是标准 data URL。
     *
     * <p>因此当我们识别到当前走的是智谱链路，且入参是
     * `data:image/...;base64,xxxx` 时，会自动剥掉前缀，只保留 `xxxx`。
     *
     * <p>这样可以同时兼顾两类场景：
     * 1. 对智谱网关：尽量贴近官方示例格式；
     * 2. 对其它 OpenAI 兼容供应商：仍可继续使用原始 data URL。
     */
    private String normalizeImagePayloadForProvider(String imagePayload) {
        String normalizedPayload = StrUtil.blankToDefault(imagePayload, "").trim();
        if (StrUtil.isBlank(normalizedPayload)) {
            return normalizedPayload;
        }
        if (!shouldStripDataUrlPrefixForImage()) {
            return normalizedPayload;
        }
        if (!StrUtil.startWithIgnoreCase(normalizedPayload, "data:")) {
            return normalizedPayload;
        }
        String base64Payload = StrUtil.subAfter(normalizedPayload, ",", false);
        return StrUtil.isBlank(base64Payload) ? normalizedPayload : base64Payload.trim();
    }

    /**
     * 判断当前供应商是否更适合接收“纯 Base64”图片字段。
     *
     * <p>这里同时参考：
     * 1. 配置里的 providerName；
     * 2. 实际请求地址。
     *
     * <p>做双判断的原因是线上环境不一定总会严格维护 providerName，
     * 但 `open.bigmodel.cn` 这样的网关域名通常更稳定。
     */
    private boolean shouldStripDataUrlPrefixForImage() {
        String providerName = StrUtil.blankToDefault(properties.getProviderName(), "");
        String apiUrl = StrUtil.blankToDefault(properties.getApiUrl(), "");
        return StrUtil.containsIgnoreCase(providerName, "zhipu")
            || StrUtil.containsIgnoreCase(providerName, "bigmodel")
            || StrUtil.containsIgnoreCase(apiUrl, "open.bigmodel.cn");
    }

    /**
     * 判断 HTTP 状态码是否更适合交给后台队列自动重试。
     *
     * <p>这里刻意只放“高概率是临时性”的状态：
     * 1. 408 请求超时；
     * 2. 429 限流；
     * 3. 5xx 服务端异常。
     *
     * <p>像 400 / 401 / 403 / 404 这类更像配置错误或请求格式错误的状态，不自动重试，
     * 避免把确定性错误塞进死循环。
     */
    private boolean isRetryableHttpStatus(int status) {
        return status == 408 || status == 429 || status >= 500;
    }

    /**
     * 判断异常是否更像临时性的网络 / 超时问题。
     *
     * <p>这里不强依赖具体异常类名，是因为不同 HTTP 客户端、不同 JDK 版本、不同网关中间层
     * 抛出来的异常类型可能不完全一致。我们优先用“异常链 + 关键字”双重兜底，提高兼容性。
     */
    private boolean isRetryableException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String exceptionName = current.getClass().getSimpleName();
            String message = StrUtil.blankToDefault(current.getMessage(), "");
            if (StrUtil.containsAnyIgnoreCase(exceptionName, "Timeout", "ConnectException", "SocketException")
                || StrUtil.containsAnyIgnoreCase(message, "timed out", "timeout", "Connection reset",
                "Connection refused", "Read timed out")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void throwRetryableIfNecessary(boolean propagateRetryableFailure, String message, Throwable cause) {
        if (!propagateRetryableFailure) {
            return;
        }
        throw new HealthReportRetryableParseException(message, cause);
    }

    /**
     * 清理模型返回中“用户不应该看到、日志里也不需要展开”的思考过程。
     *
     * <p>当前先只处理最常见、也最容易稳定识别的 `<think>...</think>` 包裹形式。
     * 这么做是一个刻意保守的选择：
     * 1. 能稳定覆盖本次联调里已经出现的问题格式；
     * 2. 不会误伤正常正文或 JSON 主体；
     * 3. 后续如果接入的网关还有别的 reasoning 包装格式，可以继续在这里集中扩展。
     *
     * <p>如果整段返回除了 think 以外已经没有任何可见正文，则保留原值，
     * 让上层仍然能感知“模型这次没有给出真正答案”，而不是静默吞掉所有内容。
     */
    private String sanitizeVisibleResponseContent(String rawContent) {
        if (StrUtil.isBlank(rawContent)) {
            return rawContent;
        }
        String textWithoutThink = rawContent.replaceAll("(?is)<think>.*?</think>", "").trim();
        if (StrUtil.isNotBlank(textWithoutThink)) {
            return textWithoutThink;
        }
        if (StrUtil.containsIgnoreCase(rawContent, "</think>")) {
            String textAfterThink = StrUtil.trim(StrUtil.subAfter(rawContent, "</think>", true));
            if (StrUtil.isNotBlank(textAfterThink)) {
                return textAfterThink;
            }
        }
        return rawContent.trim();
    }

    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
