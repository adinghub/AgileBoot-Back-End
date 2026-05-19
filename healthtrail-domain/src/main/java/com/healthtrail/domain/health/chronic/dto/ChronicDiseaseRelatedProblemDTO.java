package com.healthtrail.domain.health.chronic.dto;

import java.util.Date;
import lombok.Data;

/**
 * 慢病专项关联的健康问题摘要。
 *
 * <p>慢病详情页只需要知道关联问题的标题、状态、风险和摘要，
 * 不返回完整证据列表，避免慢病看板因为问题证据过多而变重。
 */
@Data
public class ChronicDiseaseRelatedProblemDTO {

    /** 关联记录ID */
    private Long linkId;

    /** 问题ID。 */
    private Long problemId;

    /** 问题名称。 */
    private String problemName;

    /** 问题类型。 */
    private String problemType;

    /** 问题状态。 */
    private Integer problemStatus;

    /** 问题状态名称 */
    private String problemStatusName;

    /** 风险等级。 */
    private Integer riskLevel;

    /** 风险等级名称 */
    private String riskLevelName;

    /** 问题摘要 */
    private String summary;

    /** 首次发现日期 */
    private Date firstFoundDate;

    /** 最近随访日期 */
    private Date lastFollowDate;

    /** 关联备注 */
    private String linkRemark;

    /** 关联时间 */
    private Date linkTime;
}
