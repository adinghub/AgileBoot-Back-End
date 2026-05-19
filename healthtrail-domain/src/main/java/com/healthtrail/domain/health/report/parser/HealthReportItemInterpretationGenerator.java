package com.healthtrail.domain.health.report.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.llm.HealthReportExternalLlmClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 报告指标解读生成器。
 *
 * <p>这层能力专门解决一个体验问题：
 * 1. 结构化解析能拿到“指标名 / 结果 / 参考范围”，但用户未必知道“这个指标本身表示什么、这次结果值得关注什么”；
 * 2. 如果把解释写进总摘要，用户在逐项查看时又不方便复用；
 * 3. 因此这里把“指标解读”单独沉淀到 report_item 行级字段，方便前端逐条展示。
 *
 * <p>注意这里生成的是“指标结果说明”，不是诊断意见：
 * 1. 先解释指标主要反映的生理/检验意义；
 * 2. 再结合当前结果给出一句克制的风险提示或复查提醒；
 * 3. 不直接判断疾病，不替代医生结论和个体化建议。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportItemInterpretationGenerator {

    /** 外部大模型客户端 */
    private final HealthReportExternalLlmClient externalLlmClient;

    /**
     * 为缺少指标解读的结构化结果批量回填解释。
     *
     * <p>这里选择“批量一次性请求”而不是逐条请求，原因有三点：
     * 1. 同一份报告里的指标往往几十条，逐条请求成本高；
     * 2. 批量上下文更稳定，模型更容易保持风格一致；
     * 3. 后端链路更简单，失败时也更容易整体回退。
     */
    public void fillInterpretationsIfNecessary(List<HealthReportItemEntity> reportItems) {
        if (reportItems == null || reportItems.isEmpty()) {
            return;
        }
        List<HealthReportItemEntity> missingInterpretationItems = reportItems.stream()
            .filter(Objects::nonNull)
            .filter(item -> StrUtil.isBlank(item.getItemInterpretation()))
            .toList();
        if (missingInterpretationItems.isEmpty()) {
            return;
        }
        if (!externalLlmClient.isAvailable()) {
            log.info("外部大模型不可用，跳过 report_item 指标解读回填，本次待回填条数：{}", missingInterpretationItems.size());
            return;
        }

        String responseText = externalLlmClient.generateJsonText(buildSystemPrompt(),
            buildUserPrompt(missingInterpretationItems));
        if (StrUtil.isBlank(responseText)) {
            log.info("指标解读生成未返回有效内容，本次待回填条数：{}", missingInterpretationItems.size());
            return;
        }

        try {
            JSONObject jsonObject = JSONUtil.parseObj(stripMarkdownFence(responseText));
            JSONArray items = jsonObject.getJSONArray("items");
            if (items == null || items.isEmpty()) {
                log.info("指标解读生成结果为空数组，本次待回填条数：{}", missingInterpretationItems.size());
                return;
            }
            int applyCount = Math.min(items.size(), missingInterpretationItems.size());
            for (int index = 0; index < applyCount; index++) {
                String interpretation = normalizeInterpretation(items.getJSONObject(index).getStr("itemInterpretation"));
                if (StrUtil.isBlank(interpretation)) {
                    continue;
                }
                missingInterpretationItems.get(index).setItemInterpretation(interpretation);
            }
            log.info("已为报告指标批量回填解读，目标条数：{}，实际命中条数：{}", missingInterpretationItems.size(), applyCount);
        } catch (Exception ex) {
            log.warn("指标解读生成结果无法转成 JSON，将忽略本次回填。body={}", limitLength(responseText, 1000), ex);
        }
    }

    private String buildSystemPrompt() {
        return "你是一名医学检验指标解释助手。"
            + "你的任务不是诊断疾病，而是用权威、准确、简洁的中文生成“指标结果说明”。"
            + "输出必须是 JSON 对象，顶层字段固定为 items。"
            + "items 是数组，每个元素只包含：itemInterpretation。"
            + "每条 itemInterpretation 都必须："
            + "1. 先简要说明指标主要反映什么；"
            + "2. 再结合本次结果、参考范围或异常标记，给出一句克制的健康提示；"
            + "3. 可以提示结合医生诊断、按需复查，但不能下诊断结论，不能给药物或治疗方案；"
            + "4. 尽量控制在60到100个汉字；"
            + "5. 用语克制、专业、准确，便于普通用户理解。"
            + "不要输出 markdown，不要输出额外说明文字。";
    }

    /**
     * 把当前待解释指标序列化成稳定数组，要求模型严格按顺序返回。
     *
     * <p>这里故意不让模型自己拼 key 回传，而是直接依赖数组顺序：
     * 1. 输入和输出都是同序数组，实现最简单；
     * 2. 减少 itemName 重名或 OCR 别名导致的回填匹配歧义；
     * 3. 即使标准指标编码为空，仍然能安全回填。
     */
    private String buildUserPrompt(List<HealthReportItemEntity> reportItems) {
        List<JSONObject> payloadItems = new ArrayList<>();
        for (HealthReportItemEntity reportItem : reportItems) {
            payloadItems.add(JSONUtil.createObj()
                .set("itemName", reportItem.getItemName())
                .set("standardItemCode", reportItem.getStandardItemCode())
                .set("resultValue", reportItem.getResultValue())
                .set("resultUnit", reportItem.getResultUnit())
                .set("referenceText", reportItem.getReferenceText())
                // 这里把后端已经判好的异常方向一起传给模型，
                // 目的是减少“结果明明正常，却被写成偏高/偏低”的空泛或误导性文案。
                .set("abnormalFlagName", resolveAbnormalFlagName(reportItem.getAbnormalFlag())));
        }
        return "请按输入数组顺序，为每个指标生成一条“指标结果说明”。\n"
            + "再次强调：先解释指标主要反映什么，再结合本次结果给出克制、专业、简短的健康提示。"
            + "不要下诊断结论，不要给治疗方案，可提示结合医生诊断或按需复查。\n"
            + "输入指标如下：\n"
            + JSONUtil.toJsonStr(JSONUtil.createObj().set("items", payloadItems));
    }

    /**
     * 把异常标记数值转成人能看懂的文字，直接喂给模型做稳定约束。
     */
    private String resolveAbnormalFlagName(Integer abnormalFlag) {
        if (abnormalFlag == null) {
            return null;
        }
        return switch (abnormalFlag) {
            case 1 -> "正常";
            case 2 -> "偏低";
            case 3 -> "偏高";
            case 4 -> "异常";
            default -> "待判断";
        };
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

    private String normalizeInterpretation(String text) {
        String normalizedText = StrUtil.trim(text);
        if (StrUtil.isBlank(normalizedText)) {
            return null;
        }
        return normalizedText.length() <= 300 ? normalizedText : normalizedText.substring(0, 300);
    }

    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
