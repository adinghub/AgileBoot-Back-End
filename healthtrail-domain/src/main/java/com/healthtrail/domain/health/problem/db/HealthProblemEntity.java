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
 * 健康问题表，记录报告解析出的异常线索及需持续关注的管理主题
 *
 * <p>健康问题用于承接检查报告解析出的异常线索，
 * 它不是疾病诊断，而是"需要持续关注或随访的管理主题"。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_problem")
@ApiModel(value = "HealthProblemEntity对象", description = "健康系统健康问题表")
public class HealthProblemEntity extends BaseEntity<HealthProblemEntity> {

    private static final long serialVersionUID = 1L;

    /** 健康问题主键ID */
    @ApiModelProperty("健康问题ID")
    @TableId(value = "problem_id", type = IdType.AUTO)
    private Long problemId;

    /** 问题归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 健康问题的名称描述 */
    @ApiModelProperty("问题名称")
    @TableField("problem_name")
    private String problemName;

    /** 问题类型分类 */
    @ApiModelProperty("问题类型")
    @TableField("problem_type")
    private String problemType;

    /** 问题当前处理状态 */
    @ApiModelProperty("问题状态")
    @TableField("problem_status")
    private Integer problemStatus;

    /** 风险等级 */
    @ApiModelProperty("风险等级")
    @TableField("risk_level")
    private Integer riskLevel;

    /** 关联的标准指标编码 */
    @ApiModelProperty("标准指标编码")
    @TableField("standard_item_code")
    private String standardItemCode;

    /** 问题首次被发现的日期 */
    @ApiModelProperty("首次发现日期")
    @TableField("first_found_date")
    private Date firstFoundDate;

    /** 最近一次跟进的日期 */
    @ApiModelProperty("最近跟进日期")
    @TableField("last_follow_date")
    private Date lastFollowDate;

    /** 问题摘要描述 */
    @ApiModelProperty("摘要")
    @TableField("summary")
    private String summary;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.problemId;
    }
}
