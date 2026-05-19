package com.healthtrail.domain.health.dashboard.query;

import lombok.Data;

/**
 * 首页任务中心查询对象。
 *
 * <p>当前任务中心先保留最常用的筛选项：
 * 1. 按任务类型筛选
 * 2. 按任务状态筛选
 */
@Data
public class HealthFollowUpTaskQuery {

    /**
     * 任务类型。
     */
    private String taskType;

    /**
     * 任务状态。
     */
    private Integer taskStatus;
}
