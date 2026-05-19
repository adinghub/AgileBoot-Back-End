package com.healthtrail.domain.health.dashboard.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 完成首页待跟进任务命令。
 *
 * <p>当前阶段“完成任务”主要面向报告建议类任务，
 * 代表用户已经完成本轮首页跟进处理，不再需要首页继续反复提示。
 */
@Data
public class CompleteFollowUpTaskCommand {

    /**
     * 任务类型。
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    /**
     * 来源业务ID。
     */
    @NotNull(message = "来源业务ID不能为空")
    private Long sourceId;

    /**
     * 操作备注。
     */
    private String remark;
}
