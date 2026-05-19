package com.healthtrail.domain.health.medication.query;

import lombok.Data;

/**
 * 用药提醒查询对象。
 *
 * <p>今日提醒是 App 首页和提醒页的高频场景，因此这里单独定义查询对象，
 * 方便后续继续扩展按状态、按成员筛选等能力。
 */
@Data
public class MedicationReminderQuery {

    /**
     * 当前登录用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID，可为空。
     */
    private Long memberId;

    /**
     * 提醒状态，可为空。
     */
    private Integer reminderStatus;
}
