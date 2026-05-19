package com.healthtrail.domain.health.report.parser;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.dto.HealthReportAnalysisDTO;
import com.healthtrail.domain.health.report.llm.HealthReportExternalLlmClient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 报告级结果解读生成器。
 *
 * <p>它解决的是“整份报告怎么看”的问题，而不是“单个指标是什么”的问题。
 * 生成结果应尽量回答三类用户问题：
 * 1. 这次结果整体更偏正常还是异常；
 * 2. 重点指标这次提示什么；
 * 3. 如果存在阳性对照 / 阴性对照 / 质控项，本次实验是否具备基本有效性说明。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportResultInterpretationGenerator {

    private static final Pattern FIRST_NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(\\.\\d+)?");
    /**
     * “高于参考范围”类表述关键词。
     *
     * <p>外部大模型偶尔会把正常值误写成“高于范围”，
     * 这里集中维护一组高方向关键词，供返回结果做一致性校验时复用。
     */
    private static final String[] HIGH_DIRECTION_KEYWORDS = {"高于参考范围", "超出参考范围", "偏高", "升高"};
    /**
     * “低于参考范围”类表述关键词。
     *
     * <p>和偏高校验对称，避免模型把低方向也说反。
     */
    private static final String[] LOW_DIRECTION_KEYWORDS = {"低于参考范围", "低于正常参考范围", "偏低", "降低"};

    /** 外部大模型客户端 */
    private final HealthReportExternalLlmClient externalLlmClient;

    /**
     * 生成报告级结果解读。
     *
     * <p>优先使用外部大模型产出更自然、接近医学报告说明口吻的文本；
     * 若模型不可用，则退回到本地规则化文案，确保 App 始终有内容可展示。
     */
    public String generate(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems,
        HealthReportAnalysisDTO analysisDTO) {
        String llmInterpretation = buildExternalInterpretation(reportEntity, reportItems, analysisDTO);
        if (StrUtil.isNotBlank(llmInterpretation)) {
            return limitLength(llmInterpretation, 2000);
        }
        return limitLength(buildFallbackInterpretation(reportEntity, reportItems, analysisDTO), 2000);
    }

    /**
     * 只使用本地规则生成报告级结果解读。
     *
     * <p>这个入口专门服务“已有成功解析结果、再次解析时跳过大模型”的场景：
     * 1. 业务上仍然需要把主表状态从“处理中”切回“已解析”；
     * 2. 但为了节省费用，不能再打外部模型；
     * 3. 同时又不能把 `resultInterpretation` 留成排队占位文案。
     *
     * <p>因此这里显式暴露一个“仅本地兜底文案”入口，供应用服务在复用旧结果时自愈。
     */
    public String generateFallbackOnly(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems,
        HealthReportAnalysisDTO analysisDTO) {
        return limitLength(buildFallbackInterpretation(reportEntity, reportItems, analysisDTO), 2000);
    }

    private String buildExternalInterpretation(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems,
        HealthReportAnalysisDTO analysisDTO) {
        if (!externalLlmClient.isAvailable() || reportItems == null || reportItems.isEmpty()) {
            return null;
        }
        JSONArray itemArray = new JSONArray();
        List<HealthReportItemEntity> focusItems = collectExternalInterpretationItems(reportItems);
        focusItems.stream()
            .forEach(item -> itemArray.add(buildInterpretationItemJson(item)));

        String systemPrompt = "你是一名医学检验报告结果解读助手。"
            + "请基于结构化结果，给普通用户输出“结果解读”。"
            + "当前输入可能来自同一检查项目的多个附件、多个页面或补充截图，你必须把它们视为同一次检查的不同部分综合理解，不要误判成多次独立检查。"
            + "要求："
            + "1. 重点解释本次结果代表什么，不解释系统实现；"
            + "2. 你必须严格服从结构化数据里的 computedStatus / abnormalFlagName，不得自行改判；"
            + "3. 对所有带数字的结果，尤其是带小数的结果，例如 0.92、0.08、13.7，必须按数学大小比较参考范围，不能凭直觉、不能按位数、不能因为有小数就视为异常；"
            + "4. 如果 computedStatus 是 NORMAL，绝不能写成高于参考范围、低于参考范围、超出范围；"
            + "5. 如果 computedStatus 是 HIGH，只能描述为偏高或高于范围；如果是 LOW，只能描述为偏低或低于范围；"
            + "6. 例如结果值 0.92、参考范围 0-14，数学上属于参考范围内，必须按正常表述，绝不能写成偏高；"
            + "7. 当 hasReferenceRange=false 时，你必须先识别该指标属于哪一类：算法依赖型、固定阈值型、动态变化型、描述型；"
            + "8. 算法依赖型指标是指必须结合多个指标、对照项、公式或额外临床参数才能判断正常/异常，例如 IGRA、OGTT、eGFR、血气分析氧合相关指标；"
            + "9. 对算法依赖型指标，优先检查当前这份报告的其他结构化指标里是否已经提供了完成判读所需的配套数值、对照项或组合条件；如果同页信息足够，你应基于这些同报告指标做联合判定；"
            + "10. 例如 IGRA 类项目，若当前报告同时给出了 T、N、P 或阳性/阴性对照等配套结果，你可以依据这些同页指标之间的关系尝试判断；"
            + "11. 只有当当前报告内缺少完成算法所需的关键参数时，才禁止直接下正常/异常结论，并明确提示该指标不能仅凭单一数值解读，需要医生结合临床判断；"
            + "12. 对固定阈值型指标，只有在你能从当前输入中明确得到稳定阈值依据时，才可做正常/异常表述；如果当前输入没有给出阈值，也不能编造阈值；"
            + "13. 对动态变化型指标，如果缺少历史结果和时间间隔，不要下趋势性结论；"
            + "14. 对描述型指标，不要把单一数值硬解释成确诊结论，应强调需要结合临床背景；"
            + "15. 如果 computedStatus 是 UNKNOWN，先尝试利用当前报告内的同组指标、对照项、配套指标做联合理解；若仍无法可靠判断，再表述为需结合原始报告和临床判断；"
            + "16. 当前系统没有外部医学知识库可供你补全未提供的参考范围，因此凡是输入未给出的范围、阈值、公式、算法条件，都不能自行补写成确定事实；但你可以使用当前报告中已经给出的其他指标做保守联动推理；"
            + "17. 如果出现阳性对照、阴性对照、质控项、P/N对照，请补充实验有效性说明；"
            + "18. 不要下确诊结论，不要替代医生诊断；"
            + "19. 语气准确、权威、克制、简洁，不写空泛套话；"
            + "20. 只对需要重点关注的指标展开说明；正常指标不需要逐项罗列，也不要为了凑条数把正常项一个个写出来；"
            + "21. 可以提示结合医生诊断或按需复查，但不能给出治疗方案；"
            + "22. 如果当前报告整体未见明确异常，可以只做 1 到 2 条整体性概述，不要单独展开某个正常指标；"
            + "23. 优先解释异常、偏离范围、阳性结果，以及阳性对照/阴性对照/质控项这类会影响实验有效性的内容；"
            + "24. 如果输入里的重点指标列表为空，表示当前没有需要逐条解读的异常项，你应输出简短总体结论，而不是自行补写新的异常点；"
            + "25. 优先按“1. ... 2. ...”编号输出 1 到 4 条。";

        int totalItemCount = analysisDTO == null || analysisDTO.getTotalItemCount() == null
            ? reportItems.size()
            : analysisDTO.getTotalItemCount();
        int abnormalItemCount = analysisDTO == null || analysisDTO.getAbnormalItemCount() == null
            ? (int) reportItems.stream().filter(item -> item != null && isAbnormal(item.getAbnormalFlag())).count()
            : analysisDTO.getAbnormalItemCount();

        String userPrompt = "请为下面这份检查报告生成结果解读。\n"
            + "报告名称：" + StrUtil.blankToDefault(reportEntity.getReportName(), "体检报告") + "\n"
            + "报告类型：" + StrUtil.blankToDefault(reportEntity.getReportType(), "未填写") + "\n"
            + "报告日期：" + (reportEntity.getReportDate() == null ? "未填写" : DateUtil.formatDate(reportEntity.getReportDate())) + "\n"
            + "结构化指标总数：" + totalItemCount + "\n"
            + "异常/重点关注指标数：" + abnormalItemCount + "\n"
            + "解析摘要：" + (analysisDTO == null ? "" : StrUtil.blankToDefault(analysisDTO.getSummary(), "")) + "\n"
            + "重点指标（仅包含需要优先解读的异常项或质控项，未列出的正常项无需逐条展开）：\n"
            + JSONUtil.toJsonPrettyStr(itemArray);

        String interpretation = externalLlmClient.generateText(systemPrompt, userPrompt);
        if (StrUtil.isBlank(interpretation)) {
            return interpretation;
        }
        if (!isInterpretationConsistentWithItems(interpretation, reportItems)) {
            log.warn("外部结果解读与结构化判定不一致，回退到本地规则文案。body={}", limitLength(interpretation, 1000));
            return null;
        }
        return interpretation;
    }

    /**
     * 本地兜底文案。
     *
     * <p>这部分不追求像大模型那样自然，但至少保证：
     * 1. 结果方向可读；
     * 2. 重点异常能看懂；
     * 3. 常见阳性对照 / 质控项能有基础说明。
     */
    private String buildFallbackInterpretation(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems,
        HealthReportAnalysisDTO analysisDTO) {
        if (reportItems == null || reportItems.isEmpty()) {
            return "1. 当前报告还没有结构化指标结果，暂时无法生成结果解读。";
        }

        List<String> paragraphs = new ArrayList<>();
        paragraphs.add(buildOverallResultParagraph(reportItems, analysisDTO));

        List<HealthReportItemEntity> focusItems = reportItems.stream()
            .filter(item -> item != null && isAbnormal(item.getAbnormalFlag()))
            .limit(2)
            .collect(Collectors.toList());
        if (!focusItems.isEmpty()) {
            for (int index = 0; index < focusItems.size(); index++) {
                paragraphs.add((paragraphs.size() + 1) + ". " + buildFocusItemParagraph(focusItems.get(index)));
            }
        }

        String validityParagraph = buildValidityParagraph(reportItems);
        if (StrUtil.isNotBlank(validityParagraph)) {
            paragraphs.add((paragraphs.size() + 1) + ". " + validityParagraph);
        }
        return StrUtil.join("\n", paragraphs);
    }

    private String buildOverallResultParagraph(List<HealthReportItemEntity> reportItems, HealthReportAnalysisDTO analysisDTO) {
        int totalCount = analysisDTO == null || analysisDTO.getTotalItemCount() == null
            ? reportItems.size()
            : analysisDTO.getTotalItemCount();
        int abnormalCount = analysisDTO == null || analysisDTO.getAbnormalItemCount() == null
            ? (int) reportItems.stream().filter(item -> item != null && isAbnormal(item.getAbnormalFlag())).count()
            : analysisDTO.getAbnormalItemCount();
        if (abnormalCount <= 0) {
            return "1. 本次结构化结果共识别 " + totalCount
                + " 项指标，目前未见明确异常项，整体结果更倾向于处于参考范围内，建议继续结合原始报告和医生意见判断。";
        }
        return "1. 本次结构化结果共识别 " + totalCount + " 项指标，其中 " + abnormalCount
            + " 项存在重点关注结果，提示本次报告中有需要优先查看的异常或偏离参考范围项目。";
    }

    private String buildFocusItemParagraph(HealthReportItemEntity item) {
        String itemName = StrUtil.blankToDefault(item.getItemName(), "该指标");
        String resultText = buildDisplayResult(item);
        String referenceText = buildReferenceText(item);
        Integer abnormalFlag = item.getAbnormalFlag();
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())) {
            return itemName + "本次结果为 " + resultText + "，高于参考范围" + referenceText + "，通常提示该项检测值偏高，建议结合原始报告和线下意见进一步判断。";
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())) {
            return itemName + "本次结果为 " + resultText + "，低于参考范围" + referenceText + "，通常提示该项检测值偏低，建议结合原始报告和线下意见进一步判断。";
        }
        if (containsAny(item.getResultValue(), "阴性", "未见异常", "正常")) {
            return itemName + "本次结果为 " + resultText + "，结合参考信息" + referenceText + "，通常提示当前未见该项对应的异常结果提示。";
        }
        if (containsAny(item.getResultValue(), "阳性")) {
            return itemName + "本次结果为 " + resultText + "，通常提示该项检测出现阳性结果，建议结合完整报告说明和线下意见进一步确认。";
        }
        return itemName + "本次结果为 " + resultText + "，参考信息为 " + referenceText + "，建议结合原始报告和医生意见综合判断。";
    }

    /**
     * 为外部大模型构造单项指标上下文。
     *
     * <p>这里额外补充 computedStatus / abnormalFlagName，目的是把后端已经算好的方向信息
     * 直接显式传给模型，而不是让模型再根据数值和参考范围自行猜测。
     * 这样既减少“0.92 被说成高于 0-14”这类常识性错误，也方便后续线上排查输入快照。
     */
    private JSONObject buildInterpretationItemJson(HealthReportItemEntity item) {
        return JSONUtil.createObj()
            .set("itemName", item.getItemName())
            .set("resultValue", item.getResultValue())
            .set("resultUnit", item.getResultUnit())
            .set("referenceText", item.getReferenceText())
            .set("referenceMin", item.getReferenceMin())
            .set("referenceMax", item.getReferenceMax())
            // 显式告诉模型当前条目是否给出了参考范围，避免模型在“无范围”场景下擅自脑补。
            .set("hasReferenceRange", hasReferenceRange(item))
            .set("abnormalFlag", item.getAbnormalFlag())
            .set("abnormalFlagName", resolveAbnormalFlagName(item.getAbnormalFlag()))
            .set("computedStatus", resolveComputedStatus(item.getAbnormalFlag()));
    }

    /**
     * 收敛传给外部大模型的“结果解读输入指标”。
     *
     * <p>这里刻意不再把整份报告所有指标都原样传给模型，原因有两点：
     * 1. 用户真正关心的是“哪些异常项需要看”，正常项逐条展开既占 token，也会让结果显得啰嗦；
     * 2. 只保留异常项、阳性项和质控/对照项后，模型更容易把篇幅集中在真正需要解释的内容上。
     *
     * <p>如果本次报告没有异常项，就允许重点列表为空，由提示词引导模型输出整体性结论，
     * 而不是退化成把正常指标重新罗列一遍。
     */
    private List<HealthReportItemEntity> collectExternalInterpretationItems(List<HealthReportItemEntity> reportItems) {
        if (reportItems == null || reportItems.isEmpty()) {
            return new ArrayList<>();
        }
        List<HealthReportItemEntity> focusItems = new ArrayList<>();
        for (HealthReportItemEntity item : reportItems) {
            if (item == null) {
                continue;
            }
            // 先收集异常/阳性项，让模型优先解释真正需要关注的结果。
            if (shouldExposeToExternalInterpretation(item)) {
                focusItems.add(item);
                continue;
            }
            // 再补充质控/对照项，确保像 IGRA、病原学检测这类报告还能说明实验是否有效。
            if (isValidityRelatedItem(item)) {
                focusItems.add(item);
            }
        }
        return focusItems.stream().limit(8).collect(Collectors.toList());
    }

    /**
     * 判断某个指标是否应该出现在外部大模型的重点解读列表中。
     *
     * <p>这里除了显式异常标记，也把常见“阳性”结果纳入重点项，
     * 因为很多病原学/免疫学报告未必总能稳定给出 abnormalFlag，但“阳性”本身通常就值得解释。
     */
    private boolean shouldExposeToExternalInterpretation(HealthReportItemEntity item) {
        if (item == null) {
            return false;
        }
        if (isAbnormal(item.getAbnormalFlag())) {
            return true;
        }
        return isPositiveStyleResult(item.getResultValue());
    }

    /**
     * 判断当前指标是否属于质控/对照类项目。
     *
     * <p>这类指标即便本身不是异常项，也可能直接影响整份报告的实验有效性判断，
     * 因此需要允许它们进入重点上下文，供模型补充“本次检测是否具备基本可信度”的说明。
     */
    private boolean isValidityRelatedItem(HealthReportItemEntity item) {
        if (item == null) {
            return false;
        }
        return containsAny(item.getItemName(),
            "阳性对照", "阴性对照", "质控", "P对照", "N对照", "positive control", "negative control");
    }

    /**
     * 判断结果值是否属于常见“阳性表达”。
     *
     * <p>这里不用单独匹配任意 “+” 号，是为了避免把带符号的普通文本、血型或其他非阳性语义误当成重点异常。
     * 只保留检验报告里更常见、语义更稳定的阳性写法，减少误判。
     */
    private boolean isPositiveStyleResult(String resultValue) {
        return containsAny(resultValue, "阳性", "(+)", "（+）", "++", "+++", "++++", "1+", "2+", "3+", "4+");
    }

    /**
     * 校验外部模型输出是否和结构化判定方向一致。
     *
     * <p>当前并不尝试做复杂自然语言理解，只拦截最危险、最伤用户信任的矛盾：
     * 1. 正常项被说成“高于/低于参考范围”
     * 2. 偏高项被说成“偏低”
     * 3. 偏低项被说成“偏高”
     *
     * <p>一旦发现矛盾，主流程会直接回退到本地规则文案，
     * 保证最终落库给 App 展示的结果至少方向正确。
     */
    private boolean isInterpretationConsistentWithItems(String interpretation, List<HealthReportItemEntity> reportItems) {
        if (StrUtil.isBlank(interpretation) || reportItems == null || reportItems.isEmpty()) {
            return true;
        }
        for (HealthReportItemEntity item : reportItems) {
            if (item == null || StrUtil.isBlank(item.getItemName())) {
                continue;
            }
            String itemName = item.getItemName().trim();
            int itemNameIndex = interpretation.indexOf(itemName);
            if (itemNameIndex < 0) {
                continue;
            }
            String itemContext = extractItemContext(interpretation, itemNameIndex, itemName.length());
            Integer abnormalFlag = item.getAbnormalFlag();
            if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.NORMAL.getValue())) {
                if (containsAny(itemContext, HIGH_DIRECTION_KEYWORDS) || containsAny(itemContext, LOW_DIRECTION_KEYWORDS)) {
                    return false;
                }
                continue;
            }
            if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())
                && containsAny(itemContext, LOW_DIRECTION_KEYWORDS)) {
                return false;
            }
            if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())
                && containsAny(itemContext, HIGH_DIRECTION_KEYWORDS)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从整段解读中截取和当前指标最接近的一小段上下文。
     *
     * <p>一致性校验只需要关注“提到该指标附近有没有说反方向”，
     * 没必要扫描整篇全文，否则别的异常项出现“偏高/偏低”时会误伤当前正常项。
     */
    private String extractItemContext(String interpretation, int itemNameIndex, int itemNameLength) {
        int start = Math.max(0, itemNameIndex - 12);
        int end = Math.min(interpretation.length(), itemNameIndex + itemNameLength + 48);
        return interpretation.substring(start, end);
    }

    /**
     * 识别报告中常见的质控/对照项，给出“实验有效性”解释。
     */
    private String buildValidityParagraph(List<HealthReportItemEntity> reportItems) {
        if (reportItems == null || reportItems.isEmpty()) {
            return null;
        }
        HealthReportItemEntity positiveControlItem = reportItems.stream()
            .filter(Objects::nonNull)
            .filter(item -> containsAny(item.getItemName(), "阳性对照", "结核杆菌γ(P)", "γ(P)", "positive control", "P对照"))
            .findFirst()
            .orElse(null);
        if (positiveControlItem == null) {
            return null;
        }
        String resultText = buildDisplayResult(positiveControlItem);
        BigDecimal numericValue = extractFirstNumber(positiveControlItem.getResultValue());
        if (numericValue != null && numericValue.compareTo(BigDecimal.ZERO) > 0) {
            return "实验有效性方面，报告中的质控/阳性对照项“"
                + StrUtil.blankToDefault(positiveControlItem.getItemName(), "阳性对照")
                + "”结果为 " + resultText + "，提示本次检测已记录到有效的对照反应，可作为结果可信度参考。";
        }
        return "实验有效性方面，报告中已包含质控/阳性对照项“"
            + StrUtil.blankToDefault(positiveControlItem.getItemName(), "阳性对照")
            + "”，建议结合原始报告中的质控说明进一步确认本次实验有效性。";
    }

    private boolean isAbnormal(Integer abnormalFlag) {
        return Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())
            || Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())
            || Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue());
    }

    /**
     * 把异常标记值转成更适合提示词消费的稳定状态码。
     *
     * <p>相比直接让模型理解数字 0/1/2/3/4，字符串状态更直观，也更不容易误读。
     */
    private String resolveComputedStatus(Integer abnormalFlag) {
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.NORMAL.getValue())) {
            return "NORMAL";
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())) {
            return "HIGH";
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())) {
            return "LOW";
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue())) {
            return "ABNORMAL";
        }
        return "UNKNOWN";
    }

    /**
     * 解析异常标记名称，便于直接传给外部模型做上下文说明。
     */
    private String resolveAbnormalFlagName(Integer abnormalFlag) {
        if (abnormalFlag == null) {
            return null;
        }
        for (HealthReportItemAbnormalFlagEnum abnormalFlagEnum : HealthReportItemAbnormalFlagEnum.values()) {
            if (abnormalFlagEnum.getValue().equals(abnormalFlag)) {
                return abnormalFlagEnum.description();
            }
        }
        return null;
    }

    private String buildDisplayResult(HealthReportItemEntity item) {
        String value = StrUtil.blankToDefault(item.getResultValue(), "").trim();
        String unit = StrUtil.blankToDefault(item.getResultUnit(), "").trim();
        if (value.isEmpty() && unit.isEmpty()) {
            return "-";
        }
        return (value + " " + unit).trim();
    }

    private String buildReferenceText(HealthReportItemEntity item) {
        if (StrUtil.isNotBlank(item.getReferenceText())) {
            return "（" + item.getReferenceText().trim() + "）";
        }
        if (item.getReferenceMin() != null && item.getReferenceMax() != null) {
            return "（" + item.getReferenceMin() + "-" + item.getReferenceMax() + "）";
        }
        if (item.getReferenceMin() != null) {
            return "（≥" + item.getReferenceMin() + "）";
        }
        if (item.getReferenceMax() != null) {
            return "（≤" + item.getReferenceMax() + "）";
        }
        return "（未提供参考范围）";
    }

    /**
     * 判断结构化结果里是否存在可直接用于比较的参考范围信息。
     *
     * <p>这里单独抽成方法而不是在提示词构造处写一长串判空，目的是让后续维护者更容易看懂：
     * 当前条目只要满足“有参考原文”或“有上下限任一值”，就认为模型拥有可直接引用的范围依据。
     * 如果三者都没有，就应进入“无参考范围指标”的保守解读模式。
     */
    private boolean hasReferenceRange(HealthReportItemEntity item) {
        if (item == null) {
            return false;
        }
        return StrUtil.isNotBlank(item.getReferenceText())
            || item.getReferenceMin() != null
            || item.getReferenceMax() != null;
    }

    private BigDecimal extractFirstNumber(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        Matcher matcher = FIRST_NUMBER_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        try {
            return new BigDecimal(matcher.group());
        } catch (Exception ignore) {
            return null;
        }
    }

    private boolean containsAny(String text, String... keywords) {
        if (StrUtil.isBlank(text) || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (StrUtil.containsIgnoreCase(text, keyword)) {
                return true;
            }
        }
        return false;
    }

    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
