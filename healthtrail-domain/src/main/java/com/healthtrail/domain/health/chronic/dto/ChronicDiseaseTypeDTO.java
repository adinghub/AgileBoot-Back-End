package com.healthtrail.domain.health.chronic.dto;

import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseTypeEntity;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 慢病病种配置返回对象。
 *
 * <p>App 端新增专项时只依赖这份病种清单，
 * 因此后端返回已解析好的指标编码和关键词，方便详情页解释“为什么这个专项展示这些趋势”。
 */
@Data
@NoArgsConstructor
public class ChronicDiseaseTypeDTO {

    /** 病种配置ID */
    private Long typeId;

    /** 病种编码 */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 病种分类 */
    private String diseaseCategory;

    /** 关注指标编码列表 */
    private List<String> focusIndicatorCodes = Collections.emptyList();

    /** 关注指标关键词列表 */
    private List<String> focusIndicatorKeywords = Collections.emptyList();

    /** 默认管理目标 */
    private String targetSummary;

    /** 默认随访建议 */
    private String followUpSuggestion;

    /** 排序号 */
    private Integer sort;

    /** 状态 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    public ChronicDiseaseTypeDTO(HealthChronicDiseaseTypeEntity entity, List<String> focusIndicatorCodes,
        List<String> focusIndicatorKeywords) {
        if (entity == null) {
            return;
        }
        this.typeId = entity.getTypeId();
        this.diseaseCode = entity.getDiseaseCode();
        this.diseaseName = entity.getDiseaseName();
        this.diseaseCategory = entity.getDiseaseCategory();
        this.focusIndicatorCodes = focusIndicatorCodes == null ? Collections.emptyList() : focusIndicatorCodes;
        this.focusIndicatorKeywords = focusIndicatorKeywords == null ? Collections.emptyList() : focusIndicatorKeywords;
        this.targetSummary = entity.getTargetSummary();
        this.followUpSuggestion = entity.getFollowUpSuggestion();
        this.sort = entity.getSort();
        this.status = entity.getStatus();
        this.remark = entity.getRemark();
        this.createTime = entity.getCreateTime();
    }
}
