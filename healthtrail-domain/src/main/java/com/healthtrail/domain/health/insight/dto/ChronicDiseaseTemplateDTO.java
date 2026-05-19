package com.healthtrail.domain.health.insight.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 慢病专项模板返回对象。
 *
 * <p>模板基于 `health_chronic_disease_type` 生成，不把高血压、糖尿病等病种写死到 App。
 * 新增慢病只需要维护病种配置，即可自动出现在模板列表中。
 */
@Data
public class ChronicDiseaseTemplateDTO {

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 模板分类 */
    private String templateCategory;

    /** 病种编码 */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 默认管理目标 */
    private String targetSummary;

    /** 默认随访建议 */
    private String followUpSuggestion;

    /** 关注指标编码列表 */
    private List<String> focusIndicatorCodes = new ArrayList<>();

    /** 关注指标关键词列表 */
    private List<String> focusIndicatorKeywords = new ArrayList<>();

    /** 默认日记类型列表 */
    private List<String> defaultDiaryTypes = new ArrayList<>();

    /** 建档提示列表 */
    private List<String> createProfileTips = new ArrayList<>();
}
