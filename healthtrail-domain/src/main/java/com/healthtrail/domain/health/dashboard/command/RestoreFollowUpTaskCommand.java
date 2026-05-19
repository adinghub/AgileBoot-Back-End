package com.healthtrail.domain.health.dashboard.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 恢复首页待跟进任务命令。
 *
 * <p>恢复操作用于把已忽略、已完成或已延后的任务重新恢复为“待跟进”状态，
 * 让它重新进入首页任务流。
 */
@Data
public class RestoreFollowUpTaskCommand {

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
