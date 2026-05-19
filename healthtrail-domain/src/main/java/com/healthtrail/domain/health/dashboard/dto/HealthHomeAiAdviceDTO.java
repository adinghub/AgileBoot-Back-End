package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 首页 AI 健康建议 DTO。
 *
 * <p>这里的“AI”强调的是首页聚合后的建议输出能力，
 * 它既可以来自外部大模型，也可以来自本地规则摘要回退结果。
 */
@Data
public class HealthHomeAiAdviceDTO {

    /**
     * 建议标题。
     */
    private String title;

    /**
     * 建议摘要。
     */
    private String summary;

    /**
     * 风险等级编码。
     */
    private String riskLevel;

    /**
     * 风险等级名称。
     */
    private String riskLevelName;

    /**
     * 建议来源类型。
     * 例如：RULE_ENGINE、EXTERNAL_LLM。
     */
    private String sourceType;

    /**
     * 建议来源名称。
     */
    private String sourceName;

    /**
     * 建议项列表。
     */
    private List<String> suggestions;

    /**
     * 生成时间。
     */
    private Date generatedTime;
}
