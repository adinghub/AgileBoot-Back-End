package com.healthtrail.domain.health.dashboard.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 忽略首页待跟进任务命令。
 *
 * <p>忽略操作用于表达“当前先不要在首页继续提示我这个任务”，
 * 它不代表原始业务数据已经真正完成。
 */
@Data
public class IgnoreFollowUpTaskCommand {

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
