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
 * 首页运营任务表，用于后台主动向用户投放引导任务
 *
 * <p>这张表用于承接"后台主动投放到首页任务流"的任务数据，
 * 与提醒、报告等原始业务数据解耦。
 * 这样产品或运营人员就能针对指定用户、指定成员补充主动引导任务，
 * 同时仍然复用首页任务流已有的已读、延后、完成、忽略机制。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("operation_task")
@ApiModel(value = "HealthOperationTaskEntity对象", description = "健康系统首页运营任务表")
public class HealthOperationTaskEntity extends BaseEntity<HealthOperationTaskEntity> {

    private static final long serialVersionUID = 1L;

    /** 运营任务主键ID */
    @ApiModelProperty("运营任务ID")
    @TableId(value = "operation_task_id", type = IdType.AUTO)
    private Long operationTaskId;

    /** 目标用户的App用户ID */
    @ApiModelProperty("目标App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 可选的目标家庭成员ID */
    @ApiModelProperty("可选的目标家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 任务标题 */
    @ApiModelProperty("任务标题")
    @TableField("task_title")
    private String taskTitle;

    /** 任务详细内容 */
    @ApiModelProperty("任务内容")
    @TableField("task_content")
    private String taskContent;

    /** 操作按钮的显示文案 */
    @ApiModelProperty("动作按钮文案")
    @TableField("action_text")
    private String actionText;

    /** 风险等级标识 */
    @ApiModelProperty("风险等级")
    @TableField("risk_level")
    private String riskLevel;

    /** 排序权重，数值越大越靠前 */
    @ApiModelProperty("排序权重，越大越靠前")
    @TableField("priority_weight")
    private Integer priorityWeight;

    /** 点击后跳转的目标页面编码 */
    @ApiModelProperty("目标页面编码")
    @TableField("target_page_code")
    private String targetPageCode;

    /** 目标页面显示名称 */
    @ApiModelProperty("目标页面名称")
    @TableField("target_page_name")
    private String targetPageName;

    /** 目标业务数据的主键ID */
    @ApiModelProperty("目标业务主键ID")
    @TableField("target_biz_id")
    private Long targetBizId;

    /** 目标业务数据类型 */
    @ApiModelProperty("目标业务类型")
    @TableField("target_biz_type")
    private String targetBizType;

    /** 目标页面内的标签页编码 */
    @ApiModelProperty("目标标签编码")
    @TableField("target_tab_code")
    private String targetTabCode;

    /** 目标页面内的锚点位置编码 */
    @ApiModelProperty("目标锚点编码")
    @TableField("target_anchor_code")
    private String targetAnchorCode;

    /** 目标锚点的显示名称 */
    @ApiModelProperty("目标锚点名称")
    @TableField("target_anchor_name")
    private String targetAnchorName;

    /** 任务生效开始时间 */
    @ApiModelProperty("生效开始时间")
    @TableField("start_time")
    private Date startTime;

    /** 任务生效结束时间 */
    @ApiModelProperty("生效结束时间")
    @TableField("end_time")
    private Date endTime;

    /** 任务状态，1启用 0停用 */
    @ApiModelProperty("状态（1启用 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.operationTaskId;
    }
}
