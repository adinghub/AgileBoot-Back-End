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
 * 健康问题状态变更审计表，记录问题状态的每次流转过程
 *
 * <p>健康问题主表只保存当前状态，方便问题中心快速查询；状态从"跟进中"变成"已缓解"或"已关闭"的过程
 * 需要单独沉淀到审计表，便于 App 展示问题处理时间线，也方便后续排查"是谁在什么时候关闭了问题"。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_problem_status_log")
@ApiModel(value = "HealthProblemStatusLogEntity对象", description = "健康系统健康问题状态变更审计表")
public class HealthProblemStatusLogEntity extends BaseEntity<HealthProblemStatusLogEntity> {

    private static final long serialVersionUID = 1L;

    /** 状态变更日志主键ID */
    @ApiModelProperty("日志ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /** 关联的健康问题ID */
    @ApiModelProperty("健康问题ID")
    @TableField("problem_id")
    private Long problemId;

    /** 日志归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 执行操作的用户ID */
    @ApiModelProperty("操作人用户ID")
    @TableField("operator_user_id")
    private Long operatorUserId;

    /** 操作类型，如创建、状态变更、关闭 */
    @ApiModelProperty("操作类型")
    @TableField("action_type")
    private String actionType;

    /** 操作前的问题状态 */
    @ApiModelProperty("变更前状态")
    @TableField("before_status")
    private Integer beforeStatus;

    /** 操作后的问题状态 */
    @ApiModelProperty("变更后状态")
    @TableField("after_status")
    private Integer afterStatus;

    /** 操作时的备注说明 */
    @ApiModelProperty("操作备注")
    @TableField("action_remark")
    private String actionRemark;

    /** 操作发生的时间 */
    @ApiModelProperty("操作发生时间")
    @TableField("action_time")
    private Date actionTime;

    @Override
    public Serializable pkVal() {
        return this.logId;
    }
}
