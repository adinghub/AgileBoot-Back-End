package com.healthtrail.domain.health.dashboard.command;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 首页待跟进任务已读命令。
 *
 * <p>首页任务流中的任务详情和首页预览使用的是“来源业务”维度，
 * 因此这里继续沿用 `taskType + sourceId` 的寻址方式，
 * 避免前端必须先拿到内部任务表主键才能执行已读回写。
 */
@Data
public class ReadFollowUpTaskCommand {

    /**
     * 任务类型。
     */
    @NotNull(message = "任务类型不能为空")
    @Size(max = 30, message = "任务类型长度不能超过30个字符")
    private String taskType;

    /**
     * 来源业务ID。
     */
    @NotNull(message = "来源业务ID不能为空")
    private Long sourceId;
}
