package com.healthtrail.domain.health.dashboard.command;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 首页任务批量操作命令。
 *
 * <p>任务中心批量操作以 `taskId` 集合为输入，
 * 因为任务中心列表天然已经持有内部任务主键，
 * 这样前端不需要自己再拆分 `taskType + sourceId`。
 */
@Data
public class BatchFollowUpTaskCommand {

    /**
     * 待处理任务ID集合。
     */
    @NotEmpty(message = "任务ID列表不能为空")
    private List<Long> taskIds;

    /**
     * 批量操作备注。
     *
     * <p>不同动作对备注是否真正落库由具体服务逻辑决定，
     * 这里先统一预留，避免后续再拆多套命令对象。
     */
    @Size(max = 255, message = "操作备注长度不能超过255个字符")
    private String remark;
}
