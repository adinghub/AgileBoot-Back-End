package com.healthtrail.domain.health.dashboard.dto;

import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import java.util.Date;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页运营任务 DTO。
 */
@Data
@NoArgsConstructor
public class HealthOperationTaskDTO {

    /** 运营任务ID */
    private Long operationTaskId;

    /** 所属用户ID */
    private Long ownerUserId;

    /** 家庭成员ID */
    private Long memberId;

    /** 任务标题 */
    private String taskTitle;

    /** 任务内容 */
    private String taskContent;

    /** 操作按钮文案 */
    private String actionText;

    /** 风险等级 */
    private String riskLevel;

    /** 风险等级名称 */
    private String riskLevelName;

    /** 排序权重 */
    private Integer priorityWeight;

    /** 目标页面编码 */
    private String targetPageCode;

    /** 目标页面名称 */
    private String targetPageName;

    /** 目标业务主键ID */
    private Long targetBizId;

    /** 目标业务类型 */
    private String targetBizType;

    /** 目标标签页编码 */
    private String targetTabCode;

    /** 目标锚点编码 */
    private String targetAnchorCode;

    /** 目标锚点名称 */
    private String targetAnchorName;

    /** 生效开始时间 */
    private Date startTime;

    /** 生效结束时间 */
    private Date endTime;

    /** 状态 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    public HealthOperationTaskDTO(HealthOperationTaskEntity entity) {
        if (entity == null) {
            return;
        }
        this.operationTaskId = entity.getOperationTaskId();
        this.ownerUserId = entity.getOwnerUserId();
        this.memberId = entity.getMemberId();
        this.taskTitle = entity.getTaskTitle();
        this.taskContent = entity.getTaskContent();
        this.actionText = entity.getActionText();
        this.riskLevel = entity.getRiskLevel();
        this.riskLevelName = resolveRiskLevelName(entity.getRiskLevel());
        this.priorityWeight = entity.getPriorityWeight();
        this.targetPageCode = entity.getTargetPageCode();
        this.targetPageName = HealthAppI18n.targetPageName(entity.getTargetPageCode());
        this.targetBizId = entity.getTargetBizId();
        this.targetBizType = entity.getTargetBizType();
        this.targetTabCode = entity.getTargetTabCode();
        this.targetAnchorCode = entity.getTargetAnchorCode();
        this.targetAnchorName = HealthAppI18n.targetAnchorName(entity.getTargetAnchorCode());
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
        this.status = entity.getStatus();
        this.remark = entity.getRemark();
        this.createTime = entity.getCreateTime();
    }

    private String resolveRiskLevelName(String riskLevel) {
        HealthFollowUpRiskLevelEnum riskLevelEnum = HealthFollowUpRiskLevelEnum.fromValue(riskLevel);
        return Objects.isNull(riskLevelEnum) ? null : HealthAppI18n.followUpRiskLevelName(riskLevelEnum.getValue());
    }
}
