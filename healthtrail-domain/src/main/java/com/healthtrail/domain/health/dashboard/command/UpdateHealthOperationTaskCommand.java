package com.healthtrail.domain.health.dashboard.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改首页运营任务命令。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateHealthOperationTaskCommand extends AddHealthOperationTaskCommand {

    /**
     * 运营任务ID。
     */
    private Long operationTaskId;
}
