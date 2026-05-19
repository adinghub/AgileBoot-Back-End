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
 * 首页待跟进任务表，记录用户对原始业务任务的延后、已读、完成等操作覆盖
 *
 * <p>该表并不是首页任务本身的"来源表"，
 * 而是首页任务流对原始业务对象的一层操作覆盖记录。
 * 例如：
 * 1. 某条提醒被用户延后到下午再看
 * 2. 某份报告建议被用户标记为本轮已处理
 *
 * <p>通过这种方式可以做到：
 * 1. 不侵入原始业务表
 * 2. 首页任务流又能具备可操作状态
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("follow_up_task")
@ApiModel(value = "HealthFollowUpTaskEntity对象", description = "健康系统首页待跟进任务表")
public class HealthFollowUpTaskEntity extends BaseEntity<HealthFollowUpTaskEntity> {

    private static final long serialVersionUID = 1L;

    /** 任务记录主键ID */
    @ApiModelProperty("任务记录ID")
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    /** 任务归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 任务类型，如提醒、报告建议等 */
    @ApiModelProperty("任务类型")
    @TableField("task_type")
    private String taskType;

    /** 关联的原始业务数据ID */
    @ApiModelProperty("来源业务ID")
    @TableField("source_id")
    private Long sourceId;

    /** 任务当前操作状态 */
    @ApiModelProperty("任务状态")
    @TableField("task_status")
    private Integer taskStatus;

    /** 用户是否已读 */
    @ApiModelProperty("已读状态")
    @TableField("read_status")
    private Integer readStatus;

    /** 用户标记已读的时间 */
    @ApiModelProperty("已读时间")
    @TableField("read_time")
    private Date readTime;

    /** 用户延后到该时间再提醒 */
    @ApiModelProperty("延后到期时间")
    @TableField("delayed_until")
    private Date delayedUntil;

    /** 用户标记完成的时间 */
    @ApiModelProperty("完成时间")
    @TableField("complete_time")
    private Date completeTime;

    /** 用户操作时的备注说明 */
    @ApiModelProperty("操作备注")
    @TableField("action_remark")
    private String actionRemark;

    @Override
    public Serializable pkVal() {
        return this.taskId;
    }
}
