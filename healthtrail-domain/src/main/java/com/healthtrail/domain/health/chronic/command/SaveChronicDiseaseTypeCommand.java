package com.healthtrail.domain.health.chronic.command;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 保存慢病病种配置命令。
 *
 * <p>病种配置是“所有慢病都能进入专项”的核心配置位：
 * 1. diseaseCode 表达稳定病种身份；
 * 2. 指标编码用于标准化报告项的精准匹配；
 * 3. 指标关键词用于历史报告、手工录入或识别结果尚未标准化时的名称兜底。
 */
@Data
public class SaveChronicDiseaseTypeCommand {

    /** 病种编码，例如 HYPERTENSION / DIABETES / COPD。 */
    @NotBlank(message = "病种编码不能为空")
    @Size(max = 64, message = "病种编码长度不能超过64个字符")
    private String diseaseCode;

    /** 病种名称，用于 App 展示和后台检索。 */
    @NotBlank(message = "病种名称不能为空")
    @Size(max = 100, message = "病种名称长度不能超过100个字符")
    private String diseaseName;

    /** 病种分类，例如 代谢类 / 心血管类 / 呼吸类。 */
    @Size(max = 100, message = "病种分类长度不能超过100个字符")
    private String diseaseCategory;

    /** 标准指标编码清单，优先用于精准匹配报告指标。 */
    private List<String> focusIndicatorCodes = Collections.emptyList();

    /** 指标关键词清单，用于非标准化指标名称的兜底匹配。 */
    private List<String> focusIndicatorKeywords = Collections.emptyList();

    /** 默认管理目标，会作为新建专项的目标参考。 */
    @Size(max = 500, message = "默认目标摘要长度不能超过500个字符")
    private String targetSummary;

    /** 默认随访建议，用于模板提示和建档说明。 */
    @Size(max = 500, message = "默认随访建议长度不能超过500个字符")
    private String followUpSuggestion;

    /** 排序号，数值越小越靠前。 */
    private Integer sort;

    /** 状态：1 启用，0 停用。 */
    private Integer status;

    /** 备注，仅用于后台维护说明。 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}