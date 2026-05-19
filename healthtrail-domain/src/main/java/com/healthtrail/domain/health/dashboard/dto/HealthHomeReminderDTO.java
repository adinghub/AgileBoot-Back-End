package com.healthtrail.domain.health.dashboard.dto;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 首页今日提醒预览 DTO。
 *
 * <p>首页只展示轻量预览字段，真正的完整提醒列表仍然由提醒模块接口负责。
 */
@Data
public class HealthHomeReminderDTO {

    /** 提醒ID。 */
    private Long reminderId;

    /** 成员ID。 */
    private Long memberId;

    /**
     * 家庭成员业务编码。
     */
    private String memberCode;

    /** 成员姓名。 */
    private String memberName;

    /** 计划提醒时间。 */
    private Date scheduledTime;

    /** 药品名称。 */
    private String drugName;

    /** 剂量。 */
    private BigDecimal doseAmount;

    /** 剂量单位。 */
    private String doseUnit;

    /** 就餐时间类型 */
    private String mealTiming;

    /** 提醒状态 */
    private Integer reminderStatus;
}
