package com.healthtrail.domain.health.chronic.dto;

import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户慢病专项档案返回对象。
 */
@Data
@NoArgsConstructor
public class ChronicDiseaseProfileDTO {

    /** 专项档案ID */
    private Long profileId;

    /** 所属用户ID */
    private Long ownerUserId;

    /** 成员ID。 */
    private Long memberId;

    /** 成员姓名。 */
    private String memberName;

    /** 病种编码 */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 病种分类 */
    private String diseaseCategory;

    /** 专项档案状态 */
    private Integer profileStatus;

    /** 专项档案状态名称 */
    private String profileStatusName;

    /** 风险等级。 */
    private String riskLevel;

    /** 风险等级名称 */
    private String riskLevelName;

    /** 确诊日期 */
    private Date diagnosedDate;

    /** 个性化管理目标 */
    private String targetSummary;

    /** 当前情况摘要 */
    private String currentSummary;

    /** 最近复盘日期 */
    private Date lastReviewDate;

    /** 随访建议 */
    private String followUpSuggestion;

    /** 备注。 */
    private String remark;

    public ChronicDiseaseProfileDTO(HealthChronicDiseaseProfileEntity entity, ChronicDiseaseTypeDTO typeDTO,
        String memberName) {
        if (entity == null) {
            return;
        }
        this.profileId = entity.getProfileId();
        this.ownerUserId = entity.getOwnerUserId();
        this.memberId = entity.getMemberId();
        this.memberName = memberName;
        this.diseaseCode = entity.getDiseaseCode();
        this.diseaseName = typeDTO == null ? entity.getDiseaseNameSnapshot() : typeDTO.getDiseaseName();
        this.diseaseCategory = typeDTO == null ? null : typeDTO.getDiseaseCategory();
        this.profileStatus = entity.getProfileStatus();
        this.profileStatusName = resolveProfileStatusName(entity.getProfileStatus());
        this.riskLevel = entity.getRiskLevel();
        this.riskLevelName = resolveRiskLevelName(entity.getRiskLevel());
        this.diagnosedDate = entity.getDiagnosedDate();
        this.targetSummary = entity.getTargetSummary();
        this.currentSummary = entity.getCurrentSummary();
        this.lastReviewDate = entity.getLastReviewDate();
        this.followUpSuggestion = typeDTO == null ? null : typeDTO.getFollowUpSuggestion();
        this.remark = entity.getRemark();
    }

    private static String resolveProfileStatusName(Integer profileStatus) {
        if (profileStatus == null || profileStatus == 1) {
            return "跟进中";
        }
        if (profileStatus == 2) {
            return "已稳定";
        }
        if (profileStatus == 3) {
            return "已关闭";
        }
        return "未知状态";
    }

    private static String resolveRiskLevelName(String riskLevel) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return "高优先关注";
        }
        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return "中优先关注";
        }
        return "低优先关注";
    }
}
