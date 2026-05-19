package com.healthtrail.domain.health.problem.dto;

import com.healthtrail.domain.health.problem.db.HealthProblemEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康问题返回对象。
 */
@Data
@NoArgsConstructor
public class HealthProblemDTO {

    /** 问题ID。 */
    private Long problemId;

    /** 成员ID。 */
    private Long memberId;

    /** 问题名称。 */
    private String problemName;

    /** 问题类型。 */
    private String problemType;

    /** 问题状态。 */
    private Integer problemStatus;

    /** 风险等级。 */
    private Integer riskLevel;

    /** 标准项目编码。 */
    private String standardItemCode;

    /** 首次发现日期 */
    private Date firstFoundDate;

    /** 最近随访日期 */
    private Date lastFollowDate;

    /** 摘要。 */
    private String summary;

    public HealthProblemDTO(HealthProblemEntity entity) {
        if (entity != null) {
            this.problemId = entity.getProblemId();
            this.memberId = entity.getMemberId();
            this.problemName = entity.getProblemName();
            this.problemType = entity.getProblemType();
            this.problemStatus = entity.getProblemStatus();
            this.riskLevel = entity.getRiskLevel();
            this.standardItemCode = entity.getStandardItemCode();
            this.firstFoundDate = entity.getFirstFoundDate();
            this.lastFollowDate = entity.getLastFollowDate();
            this.summary = entity.getSummary();
        }
    }
}
