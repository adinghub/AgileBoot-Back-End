package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import lombok.Data;

/**
 * 首页待跟进任务操作日志 DTO。
 *
 * <p>该对象面向 App 端任务详情时间线展示，
 * 所以重点返回“动作 + 状态变化 + 操作时间”。
 */
@Data
public class HealthFollowUpTaskLogDTO {

    /**
     * 日志ID。
     */
    private Long logId;

    /**
     * 所属任务记录ID。
     */
    private Long taskId;

    /**
     * 操作类型。
     */
    private String actionType;

    /**
     * 操作类型名称。
     */
    private String actionTypeName;

    /**
     * 操作前状态。
     */
    private Integer beforeStatus;

    /**
     * 操作前状态名称。
     */
    private String beforeStatusName;

    /**
     * 操作后状态。
     */
    private Integer afterStatus;

    /**
     * 操作后状态名称。
     */
    private String afterStatusName;

    /**
     * 操作备注。
     */
    private String actionRemark;

    /**
     * 延后时间快照。
     *
     * <p>只有延后类操作通常会有值，
     * 其他动作返回 null 即可。
     */
    private Date delayedUntilSnapshot;

    /**
     * 操作发生时间。
     */
    private Date actionTime;
}
