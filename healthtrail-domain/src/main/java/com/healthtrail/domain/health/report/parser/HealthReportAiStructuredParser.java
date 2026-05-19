package com.healthtrail.domain.health.report.parser;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.healthtrail.domain.health.report.config.HealthReportAiStructuredParseProperties;
import com.healthtrail.domain.health.report.llm.HealthReportExternalLlmClient;
import java.math.BigDecimal;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 报告 AI 结构化解析器。
 *
 * <p>它不替代现有规则解析，而是作为“增强层”存在：
 * 1. PDF 或文本文件版式比较规整时，规则解析更快、更稳定；
 * 2. 医院报告排版复杂、字段换行、表格错位时，AI 往往更容易恢复语义；
 * 3. 因此当前策略是：优先跑规则，再在规则结果过少时尝试 AI 补强。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportAiStructuredParser {

    /** 外部大模型客户端 */
    private final HealthReportExternalLlmClient externalLlmClient;

    /** AI结构化解析配置属性 */
    private final HealthReportAiStructuredParseProperties parseProperties;

    /** 标准指标解析器 */
    private final HealthReportStandardIndicatorResolver standardIndicatorResolver;

    /**
     * 根据已抽取文本抽取结构化指标。
     *
     * @return 成功时返回带 items 的结果；如果未启用、调用失败或结果不合法，则返回空结果
     */
    public ParsedReportResult parse(String extractedText) {
        ParsedReportResult result = new ParsedReportResult();
        result.setExtractedText(extractedText);
        if (!isAvailable(extractedText)) {
            result.setMessage("AI 结构化解析未启用或输入文本为空。");
            return result;
        }

        String jsonText = externalLlmClient.generateRetryableJsonText(buildSystemPrompt(), buildUserPrompt(extractedText));
        return parseJsonResult(extractedText, jsonText, "AI 结构化解析");
    }

    /**
     * 根据“图片 + 可选辅助文本”做联合结构化解析。
     *
     * <p>这条链路是为了提升拍照上传体检单的解析质量：
     * 1. 图片负责提供版面、表格边界、列关系；
     * 2. 已抽取文本只作为补充材料，不会触发任何 OCR 调用；
     * 3. 最终仍强制模型只返回 JSON，避免把说明文字混进结构化结果里。
     *
     * <p>如果图片不可用、配置不完整或模型返回格式不对，调用方仍然可以继续走
     * 规则解析 / 文本解析，不会因为这里失败导致整条链路中断。
     */
    public ParsedReportResult parseWithVision(String extractedText, String imageDataUrl) {
        ParsedReportResult result = new ParsedReportResult();
        result.setExtractedText(extractedText);
        if (!isVisionAvailable(extractedText, imageDataUrl)) {
            result.setMessage("AI 视觉结构化解析未启用或图片内容不可用。");
            return result;
        }

        String userPrompt = buildVisionUserPrompt(extractedText);
        String jsonText = externalLlmClient.generateRetryableJsonTextWithImage(buildVisionSystemPrompt(), userPrompt, imageDataUrl);
        return parseJsonResult(extractedText, jsonText, "AI 视觉结构化解析");
    }

    /**
     * 判断是否具备调用条件。
     */
    public boolean isAvailable(String extractedText) {
        return parseProperties.isEnabled()
            && externalLlmClient.isAvailable()
            && StrUtil.isNotBlank(extractedText);
    }

    /**
     * 判断是否具备调用视觉解析的条件。
     *
     * <p>这里刻意沿用和文本结构化解析一致的可用性判断，只额外加上：
     * 1. 图片开关是否开启；
     * 2. 图片 data URL 是否存在。
     */
    public boolean isVisionAvailable(String extractedText, String imageDataUrl) {
        return parseProperties.isEnabled()
            && parseProperties.isPreferVisionOnImage()
            && externalLlmClient.isAvailable()
            && (StrUtil.isNotBlank(extractedText) || StrUtil.isNotBlank(imageDataUrl))
            && StrUtil.isNotBlank(imageDataUrl);
    }

    private ParsedReportItem toParsedItem(JSONObject itemObject, int defaultSort) {
        if (itemObject == null) {
            return null;
        }
        // 线上联调里已经观察到：即使我们提示词写死了 schema，
        // 不同模型/网关仍可能返回轻微变体字段名，例如：
        // 1. `name` / `item` / `indicatorName` 代替 `itemName`
        // 2. `result` / `value` 代替 `resultValue`
        // 3. `unit` / `referenceRange` / `interpretation` 等常见缩写写法
        //
        // 如果这里只接受“完全命中字面字段名”的理想情况，
        // 模型其实已经识别出指标，也会在最后一步被全部丢弃，表现成“AI 没识别到任何项目”。
        // 因此这里统一做一层字段别名兼容，把后端的消费口径收敛到同一处。
        String itemName = firstNonBlank(itemObject,
            "itemName", "name", "item", "indicatorName", "indicator", "projectName", "testName");
        String resultValue = firstNonBlank(itemObject,
            "resultValue", "result", "value", "testResult", "itemValue");
        if (StrUtil.isBlank(itemName) || StrUtil.isBlank(resultValue)) {
            return null;
        }

        ParsedReportItem item = new ParsedReportItem();
        item.setItemCode(firstNonBlank(itemObject, "itemCode", "code", "indicatorCode", "projectCode", "testCode"));
        item.setItemName(itemName);
        item.setResultValue(resultValue);
        item.setResultUnit(firstNonBlank(itemObject, "resultUnit", "unit", "valueUnit", "itemUnit"));
        item.setReferenceText(firstNonBlank(itemObject,
            "referenceText", "referenceRange", "reference", "referenceValue", "range", "normalRange"));
        item.setReferenceMin(parseDecimal(firstNonBlank(itemObject, "referenceMin", "min", "lowerLimit", "rangeMin")));
        item.setReferenceMax(parseDecimal(firstNonBlank(itemObject, "referenceMax", "max", "upperLimit", "rangeMax")));
        item.setItemInterpretation(normalizeInterpretation(firstNonBlank(itemObject,
            "itemInterpretation", "interpretation", "comment", "advice", "summary")));
        item.setSort(resolveSort(itemObject, defaultSort));
        item.setRemark("AI 结构化解析生成，建议用户确认。");
        item.setStandardItemCode(standardIndicatorResolver.resolve(item.getItemCode(), itemName));
        return item;
    }

    /**
     * 统一解析模型返回的 JSON 结构。
     *
     * <p>文本结构化解析和视觉结构化解析最终都要求同一种 JSON 结果，
     * 因此把这段收敛到一个方法里，避免两处 JSON 校验逻辑越改越飘。
     */
    private ParsedReportResult parseJsonResult(String extractedText, String jsonText, String sceneName) {
        ParsedReportResult result = new ParsedReportResult();
        result.setExtractedText(extractedText);
        if (StrUtil.isBlank(jsonText)) {
            result.setMessage(sceneName + "未返回有效内容。");
            return result;
        }

        try {
            String normalizedJsonText = extractStructuredJsonText(jsonText);
            if (StrUtil.isBlank(normalizedJsonText)) {
                log.warn("{}返回了内容，但未能从中提取出有效 JSON。body={}", sceneName, limitLength(jsonText, 1200));
                result.setMessage(sceneName + "结果格式不正确。");
                return result;
            }
            if (!StrUtil.equals(normalizedJsonText, StrUtil.trim(jsonText))) {
                log.info("{}返回内容中检测到思考文本或额外说明，已自动提取 JSON 主体。", sceneName);
            }
            JSONObject jsonObject = JSONUtil.parseObj(normalizedJsonText);
            // 顶层 reportDate 代表模型综合多附件、多页之后识别出的整份报告日期候选值。
            // 这里只做解析，不直接决定是否覆盖主表，最终仍由应用服务做空值保护。
            result.setReportDate(parseReportDate(jsonObject.getStr("reportDate")));
            // 顶层 recognizedReportType 代表模型识别出的更细粒度报告类型，
            // 例如“血液检查”进一步细化为“血常规”。
            //
            // 这里同样只负责把识别结果带回应用层，
            // 是否写回数据库、如何和用户手填类型并存，由应用服务统一决策。
            result.setRecognizedReportType(normalizeText(jsonObject.getStr("recognizedReportType")));
            // 顶层 items 仍然是首选标准字段。
            // 但为了兼容不同兼容网关/模型偶发输出的轻微包装差异，
            // 这里允许从若干常见别名路径中兜底取数组，而不是“一旦不叫 items 就整份丢掉”。
            JSONArray items = extractItemsArray(jsonObject);
            if (items == null || items.isEmpty()) {
                log.warn("{}返回 JSON 解析成功，但 items 为空。json={}", sceneName, limitLength(normalizedJsonText, 1200));
                result.setMessage(sceneName + "未识别出可用指标。");
                return result;
            }

            for (int index = 0; index < items.size(); index++) {
                JSONObject itemObject = items.getJSONObject(index);
                ParsedReportItem item = toParsedItem(itemObject, index + 1);
                if (item == null) {
                    continue;
                }
                result.getItems().add(item);
            }
            if (!result.hasItems()) {
                log.warn("{}返回了 {} 个原始项目，但字段未命中可消费 schema。firstItem={}",
                    sceneName, items.size(), limitLength(items.getJSONObject(0).toString(), 400));
            }
            result.setMessage(result.hasItems()
                ? StrUtil.format("{}识别出 {} 项指标。", sceneName, result.getItems().size())
                : sceneName + "结果为空。");
            return result;
        } catch (Exception ex) {
            log.warn("{}结果无法转成 JSON，将忽略本次结果。body={}", sceneName, limitLength(jsonText, 1000), ex);
            result.setMessage(sceneName + "结果格式不正确。");
            return result;
        }
    }

    /**
     * 结构化解析提示词更强调“字段准确落槽”，而不是医学解释。
     */
    private String buildSystemPrompt() {
        return "你是一名医疗检查报告结构化抽取助手。"
            + "请只根据用户提供的报告文本提取检验项目，不要做医学判断，不要补造不存在的数据。"
            + "用户提供的内容可能来自同一检查项目的多个附件、多页截图或多张报告图片，你必须先把它们当成同一次检查的不同页面综合理解，再输出统一结果。"
            + "不要因为同一项目在不同附件重复出现，就拆成多份独立报告；遇到重复项目时应优先合并、去重，并尽量保留信息更完整的一条。"
            + "输出必须是 JSON 对象，顶层字段固定为 reportDate、recognizedReportType 和 items。"
            + "reportDate 表示当前整份报告最可信的报告日期，格式必须为 yyyy-MM-dd；如果无法确认，请返回空字符串或 null。"
            + "recognizedReportType 表示当前整份报告更准确的细分类型，例如血常规、肝功能、尿常规；如果无法确认，请返回空字符串或 null。"
            + "items 是数组，每个元素包含：itemCode,itemName,resultValue,resultUnit,referenceText,referenceMin,referenceMax,itemInterpretation,sort。"
            + "itemInterpretation 需要用一到两句中文完成两件事：先简要说明该指标主要反映什么，再结合本次结果和参考范围给出克制的健康提示。"
            + "允许提示结合医生诊断或按需复查，但不能下诊断结论、不能写治疗方案，尽量控制在80个汉字内。"
            + "如果某个字段不存在，请返回空字符串或 null。"
            + "不要输出 markdown，不要输出解释文字。";
    }

    /**
     * 视觉解析的系统提示词比文本版更强调“读图抽字段”，
     * 但输出约束仍然保持一致，确保调用方无需区分返回结构。
     */
    private String buildVisionSystemPrompt() {
        return "你是一名医疗检查报告视觉结构化抽取助手。"
            + "请根据用户提供的检查单图片以及补充文本提取检验项目。"
            + "不要做医学判断，不要补造不存在的数据。"
            + "用户提供的图片可能是同一检查项目的多个附件、不同页、不同截图或补充页，你必须先合并理解这些页面之间的关系，再输出统一的结构化结果。"
            + "如果某个项目在不同附件重复出现，请合并去重，优先保留结果、单位、参考范围、对照项更完整的一条。"
            + "输出必须是 JSON 对象，顶层字段固定为 reportDate、recognizedReportType 和 items。"
            + "reportDate 表示当前整份报告最可信的报告日期，格式必须为 yyyy-MM-dd；如果无法确认，请返回空字符串或 null。"
            + "recognizedReportType 表示当前整份报告更准确的细分类型，例如血常规、肝功能、尿常规；如果无法确认，请返回空字符串或 null。"
            + "items 是数组，每个元素包含：itemCode,itemName,resultValue,resultUnit,referenceText,referenceMin,referenceMax,itemInterpretation,sort。"
            + "itemInterpretation 需要用一到两句中文完成两件事：先简要说明该指标主要反映什么，再结合本次结果和参考范围给出克制的健康提示。"
            + "允许提示结合医生诊断或按需复查，但不能下诊断结论、不能写治疗方案，尽量控制在80个汉字内。"
            + "如果某个字段不存在，请返回空字符串或 null。"
            + "不要输出 markdown，不要输出解释文字。";
    }

    private String buildUserPrompt(String extractedText) {
        String normalizedText = extractedText;
        if (normalizedText.length() > parseProperties.getMaxPromptTextLength()) {
            normalizedText = normalizedText.substring(0, parseProperties.getMaxPromptTextLength());
        }
        return "请从下面这段报告文本中提取检验报告的结构化指标，严格返回 JSON。\n"
            + "要求：\n"
            + "1. 用户这次上传的文本可能来自同一检查项目的多个附件或多页内容，请按同一次检查合并理解，不要拆成多份报告；\n"
            + "2. 同时尽量识别整份报告的报告日期/检查日期/检验日期，并填入顶层 reportDate；若无法确认，请返回空字符串或 null；\n"
            + "3. 同时尽量判断这份报告更准确的细分类型，并填入顶层 recognizedReportType，例如血常规、肝功能、尿常规；若无法确认，请返回空字符串或 null；\n"
            + "4. 只提取真正的检验项目，不要把就诊人、科室、时间等头部信息当作项目；\n"
            + "5. 允许识别希腊字母 gamma/γ，也允许原始文本把 γ 记录成 y；\n"
            + "6. 参考范围如果单独显示在下一行，也尽量挂回对应项目；\n"
            + "7. 同一项目在不同附件重复出现时，请合并去重，优先保留信息更完整的一条；\n"
            + "8. itemInterpretation 需要同时说明“指标主要反映什么”和“本次结果值得关注什么”，可提示结合医生诊断，但不要写诊断结论或治疗方案；\n"
            + "9. sort 从 1 开始按页面顺序递增。\n\n"
            + "报告文本如下：\n"
            + normalizedText;
    }

    /**
     * 视觉解析以图片为主，已抽取文本只作为辅助材料。
     */
    private String buildVisionUserPrompt(String extractedText) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请先阅读图片中的体检/检验报告，再结合补充文本做结构化抽取，严格返回 JSON。\n")
            .append("要求：\n")
            .append("1. 用户本次上传的图片可能是同一检查项目的多个附件、不同页、补充页或对照页，请先按同一次检查合并理解，不要把它们拆成多份独立报告；\n")
            .append("2. 同时尽量识别整份报告的报告日期/检查日期/检验日期，并填入顶层 reportDate；若无法确认，请返回空字符串或 null；\n")
            .append("3. 同时尽量判断这份报告更准确的细分类型，并填入顶层 recognizedReportType，例如血常规、肝功能、尿常规；若无法确认，请返回空字符串或 null；\n")
            .append("4. 只提取真正的检验项目，不要把姓名、年龄、科室、条码号、日期等头部信息当作项目；\n")
            .append("5. 优先保证 itemName、resultValue、resultUnit、referenceText 的落槽准确；\n")
            .append("6. 如果参考范围和项目分列显示，请尽量匹配回对应项目；\n")
            .append("7. 如果某个项目在不同附件重复出现，请合并去重，优先保留结果、单位、参考范围、对照项更完整的一条；\n")
            .append("8. itemInterpretation 需要同时说明“指标主要反映什么”和“本次结果值得关注什么”，可提示结合医生诊断，但不要写诊断结论或治疗方案，尽量控制在80字内；\n")
            .append("9. 对同一项目不要重复输出；\n")
            .append("10. sort 从 1 开始按页面从上到下顺序递增。\n");
        if (parseProperties.isIncludeExtractedTextWhenVisionEnabled() && StrUtil.isNotBlank(extractedText)) {
            String normalizedText = extractedText;
            if (normalizedText.length() > parseProperties.getMaxPromptTextLength()) {
                normalizedText = normalizedText.substring(0, parseProperties.getMaxPromptTextLength());
            }
            promptBuilder.append("\n补充报告文本如下（仅作辅助校对）：\n")
                .append(normalizedText);
        }
        return promptBuilder.toString();
    }

    private String stripMarkdownFence(String text) {
        String normalizedText = StrUtil.blankToDefault(text, "").trim();
        if (!normalizedText.startsWith("```")) {
            return normalizedText;
        }
        normalizedText = normalizedText.replaceFirst("^```[a-zA-Z0-9_-]*\\s*", "");
        normalizedText = normalizedText.replaceFirst("\\s*```$", "");
        return normalizedText.trim();
    }

    /**
     * 从模型返回内容中提取真正的 JSON 主体。
     *
     * <p>线上联调发现，某些支持思维链/思考输出的视觉模型，即使提示词要求“只返回 JSON”，
     * 仍可能返回以下混合格式：
     * 1. `<think>...</think>{...json...}`
     * 2. `思考文本 + Markdown 代码块 JSON`
     * 3. `说明文字 + JSON + 结束语`
     *
     * <p>当前结构化解析只关心最终 JSON，因此这里统一做三步收口：
     * 1. 去掉 markdown fence；
     * 2. 去掉常见的 `<think>...</think>` 思考块；
     * 3. 从剩余文本中提取第一段结构完整的 JSON 对象。
     *
     * <p>这样可以避免“模型明明回了结果，但因为前面多了一段思考文字，最终被误判成无指标”。
     */
    private String extractStructuredJsonText(String rawText) {
        String normalizedText = stripMarkdownFence(rawText);
        if (StrUtil.isBlank(normalizedText)) {
            return normalizedText;
        }
        String textWithoutThink = normalizedText.replaceAll("(?is)<think>.*?</think>", "").trim();
        if (StrUtil.isBlank(textWithoutThink) && StrUtil.containsIgnoreCase(normalizedText, "</think>")) {
            textWithoutThink = StrUtil.subAfter(normalizedText, "</think>", true);
        }
        String candidateText = StrUtil.blankToDefault(textWithoutThink, normalizedText).trim();
        if (candidateText.startsWith("{") && candidateText.endsWith("}")) {
            return candidateText;
        }
        return extractFirstJsonObject(candidateText);
    }

    /**
     * 从混合文本里截取第一段结构完整的 JSON 对象。
     *
     * <p>这里使用轻量级的大括号平衡扫描，而不是正则，原因是：
     * 1. JSON 可能包含嵌套对象；
     * 2. 指标解释文本里可能出现花括号样式字符；
     * 3. 正则对这类嵌套结构不稳定，可维护性也更差。
     */
    private String extractFirstJsonObject(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        int startIndex = text.indexOf('{');
        if (startIndex < 0) {
            return null;
        }
        boolean inString = false;
        boolean escaping = false;
        int braceDepth = 0;
        for (int index = startIndex; index < text.length(); index++) {
            char currentChar = text.charAt(index);
            if (escaping) {
                escaping = false;
                continue;
            }
            if (currentChar == '\\') {
                escaping = true;
                continue;
            }
            if (currentChar == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (currentChar == '{') {
                braceDepth++;
                continue;
            }
            if (currentChar == '}') {
                braceDepth--;
                if (braceDepth == 0) {
                    return text.substring(startIndex, index + 1).trim();
                }
            }
        }
        return null;
    }

    private String normalizeText(String text) {
        String normalizedText = StrUtil.trim(text);
        return StrUtil.isBlank(normalizedText) ? null : normalizedText;
    }

    /**
     * 读取模型返回中的首个非空字符串字段。
     *
     * <p>这里统一承担“字段别名兼容”职责，避免调用点散落一堆
     * `blankToDefault(a, blankToDefault(b, c))` 这种难维护的链式逻辑。
     */
    private String firstNonBlank(JSONObject jsonObject, String... fieldNames) {
        if (jsonObject == null || fieldNames == null) {
            return null;
        }
        for (String fieldName : fieldNames) {
            String value = normalizeText(jsonObject.getStr(fieldName));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 兼容模型偶发把 items 数组包进不同层级的返回格式。
     *
     * <p>当前优先支持我们已经在线上和兼容网关里最容易遇到的几种形态：
     * 1. 顶层 `items`
     * 2. 顶层 `reportItems` / `results` / `indicators`
     * 3. `data.items` / `data.reportItems` / `data.results`
     */
    private JSONArray extractItemsArray(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        JSONArray directItems = firstNonEmptyArray(jsonObject,
            "items", "reportItems", "results", "indicators", "list");
        if (directItems != null) {
            return directItems;
        }
        JSONObject dataObject = jsonObject.getJSONObject("data");
        return firstNonEmptyArray(dataObject, "items", "reportItems", "results", "indicators", "list");
    }

    private JSONArray firstNonEmptyArray(JSONObject jsonObject, String... fieldNames) {
        if (jsonObject == null || fieldNames == null) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JSONArray jsonArray = jsonObject.getJSONArray(fieldName);
            if (jsonArray != null && !jsonArray.isEmpty()) {
                return jsonArray;
            }
        }
        return null;
    }

    /**
     * 对排序字段做兼容读取。
     *
     * <p>多数模型会按要求返回 `sort`，但也有概率输出 `order` / `index` 之类字段。
     * 排序只影响展示顺序，不应该因为字段名轻微漂移就整项丢弃，所以这里做安全兜底。
     */
    private int resolveSort(JSONObject itemObject, int defaultSort) {
        if (itemObject == null) {
            return defaultSort;
        }
        Integer sortValue = itemObject.getInt("sort");
        if (sortValue != null) {
            return sortValue;
        }
        sortValue = itemObject.getInt("order");
        if (sortValue != null) {
            return sortValue;
        }
        sortValue = itemObject.getInt("index");
        return sortValue == null ? defaultSort : sortValue;
    }

    /**
     * 对模型生成的指标解读做轻量收口。
     *
     * <p>这里不做复杂 NLP 清洗，只做三件事：
     * 1. 去空白；
     * 2. 截断超长内容，避免模型偶发输出一大段说明；
     * 3. 空字符串统一转 null。
     */
    private String normalizeInterpretation(String text) {
        String normalizedText = normalizeText(text);
        if (normalizedText == null) {
            return null;
        }
        return normalizedText.length() <= 300 ? normalizedText : normalizedText.substring(0, 300);
    }

    private BigDecimal parseDecimal(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 解析模型返回的报告日期。
     *
     * <p>这里只接受稳定的绝对日期格式，避免模型在没有把握时返回相对时间或自然语言描述，
     * 却被系统误当成真实报告日期。
     */
    private Date parseReportDate(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return DateUtil.parseDate(value.trim());
        } catch (Exception ex) {
            log.debug("AI 结构化解析返回的 reportDate 无法识别，value={}", value);
            return null;
        }
    }

    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
