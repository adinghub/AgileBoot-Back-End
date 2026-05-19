package com.healthtrail.domain.health.dashboard.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页待跟进任务操作日志表，记录用户对任务的所有操作时间线
 *
 * <p>该表专门用于记录"用户做了什么动作"，
 * 不承担当前任务状态的主存职责。
 *
 * <p>这样拆分有两个明显好处：
 * 1. follow_up_task 继续只保存当前覆盖状态，查询首页时更高效
 * 2. follow_up_task_log 可以完整保留时间线，便于前端展示和后续审计
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("follow_up_task_log")
@ApiModel(value = "HealthFollowUpTaskLogEntity对象", description = "健康系统首页待跟进任务操作日志表")
public class HealthFollowUpTaskLogEntity extends BaseEntity<HealthFollowUpTaskLogEntity> {

    private static final long serialVersionUID = 1L;

    /** 操作日志主键ID */
    @ApiModelProperty("日志ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /** 关联的任务记录ID */
    @ApiModelProperty("任务记录ID")
    @TableField("task_id")
    private Long taskId;

    /** 操作归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 任务类型快照 */
    @ApiModelProperty("任务类型")
    @TableField("task_type")
    private String taskType;

    /** 关联的原始业务数据ID */
    @ApiModelProperty("来源业务ID")
    @TableField("source_id")
    private Long sourceId;

    /** 用户执行的操作类型 */
    @ApiModelProperty("操作类型")
    @TableField("action_type")
    private String actionType;

    /** 操作前的任务状态 */
    @ApiModelProperty("操作前状态")
    @TableField("before_status")
    private Integer beforeStatus;

    /** 操作后的任务状态 */
    @ApiModelProperty("操作后状态")
    @TableField("after_status")
    private Integer afterStatus;

    /** 用户操作时的备注说明 */
    @ApiModelProperty("操作备注")
    @TableField("action_remark")
    private String actionRemark;

    /** 操作时的延后时间快照 */
    @ApiModelProperty("延后到期时间快照")
    @TableField("delayed_until_snapshot")
    private Date delayedUntilSnapshot;

    @Override
    public Serializable pkVal() {
        return this.logId;
    }
}
