package com.healthtrail.domain.health.medication.dto;

import java.util.Date;
import lombok.Data;

/**
 * 用药依从率统计返回对象。
 */
@Data
public class MedicationAdherenceStatisticsDTO {

    /** 成员ID。 */
    private Long memberId;

    /** 成员姓名。 */
    private String memberName;

    /** 开始日期。 */
    private Date startDate;

    /** 结束日期。 */
    private Date endDate;

    /** 计划提醒总数 */
    private Integer scheduledReminderCount;

    /** 已服药提醒数量 */
    private Integer takenReminderCount;

    /** 已跳过提醒数量 */
    private Integer skippedReminderCount;

    /** 已过期提醒数量 */
    private Integer expiredReminderCount;

    /** 待处理提醒数量 */
    private Integer pendingReminderCount;

    /**
     * 依从率。
     *
     * <p>当前口径采用：
     * 已服药 / （已服药 + 已跳过 + 已过期）
     * 这样可以排除未来待执行提醒对统计的干扰。
     */
    private Double adherenceRate;
}
