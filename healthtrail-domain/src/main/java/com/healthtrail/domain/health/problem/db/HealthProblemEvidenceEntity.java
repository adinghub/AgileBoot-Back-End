package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 健康问题证据表，将报告指标等证据挂到健康问题下，支持反向追溯
 *
 * <p>该表把报告、指标、医生诊断等证据挂到同一个健康问题下，
 * 让前端可以从"问题"反查支撑它的检查报告和异常项。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_problem_evidence")
@ApiModel(value = "HealthProblemEvidenceEntity对象", description = "健康系统健康问题证据表")
public class HealthProblemEvidenceEntity extends BaseEntity<HealthProblemEvidenceEntity> {

    private static final long serialVersionUID = 1L;

    /** 证据记录主键ID */
    @ApiModelProperty("证据ID")
    @TableId(value = "evidence_id", type = IdType.AUTO)
    private Long evidenceId;

    /** 关联的健康问题ID */
    @ApiModelProperty("健康问题ID")
    @TableField("problem_id")
    private Long problemId;

    /** 证据归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 证据类型，如报告指标、医生诊断等 */
    @ApiModelProperty("证据类型")
    @TableField("evidence_type")
    private String evidenceType;

    /** 关联的体检报告ID */
    @ApiModelProperty("报告ID")
    @TableField("report_id")
    private Long reportId;

    /** 关联的报告指标结果ID */
    @ApiModelProperty("报告指标ID")
    @TableField("report_item_id")
    private Long reportItemId;

    /** 证据的标题描述 */
    @ApiModelProperty("证据标题")
    @TableField("evidence_title")
    private String evidenceTitle;

    /** 证据的摘要描述 */
    @ApiModelProperty("证据摘要")
    @TableField("evidence_summary")
    private String evidenceSummary;

    /** 证据相关的日期 */
    @ApiModelProperty("证据日期")
    @TableField("evidence_date")
    private Date evidenceDate;

    /** 证据的可信度等级 */
    @ApiModelProperty("置信等级")
    @TableField("confidence_level")
    private Integer confidenceLevel;

    /** 证据的人工确认状态 */
    @ApiModelProperty("确认状态")
    @TableField("confirm_status")
    private Integer confirmStatus;

    @Override
    public Serializable pkVal() {
        return this.evidenceId;
    }
}
