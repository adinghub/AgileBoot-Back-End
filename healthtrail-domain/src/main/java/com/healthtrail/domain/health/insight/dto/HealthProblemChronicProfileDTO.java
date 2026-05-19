package com.healthtrail.domain.health.insight.dto;

import java.util.Date;
import lombok.Data;

/**
 * 健康问题关联的慢病专项摘要。
 *
 * <p>该 DTO 同时服务“问题中心已关联专项展示”和“关联候选列表”。
 * `linked` 表示当前问题是否已经关联该专项，App 可以用同一份结构展示绑定和解绑动作。
 */
@Data
public class HealthProblemChronicProfileDTO {

    /** 关联记录ID */
    private Long linkId;

    /** 慢病专项档案ID */
    private Long profileId;

    /** 家庭成员ID */
    private Long memberId;

    /** 家庭成员姓名 */
    private String memberName;

    /** 病种编码 */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 专项档案状态 */
    private Integer profileStatus;

    /** 专项档案状态名称 */
    private String profileStatusName;

    /** 风险等级 */
    private String riskLevel;

    /** 风险等级名称 */
    private String riskLevelName;

    /** 是否已关联 */
    private boolean linked;

    /** 关联备注 */
    private String linkRemark;

    /** 关联时间 */
    private Date linkTime;
}
