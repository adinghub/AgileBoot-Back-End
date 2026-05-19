package com.healthtrail.domain.health.insight.dto;

import java.util.Date;
import lombok.Data;

/**
 * 健康问题状态变更日志 DTO。
 *
 * <p>该 DTO 面向 App 展示问题处理历史，返回状态值和状态名称两套字段。
 * App 只负责展示，不需要自己推导 1/2/3 分别代表什么，避免前后端状态文案不一致。
 */
@Data
public class HealthProblemStatusLogDTO {

    /** 日志ID */
    private Long logId;

    /** 问题ID */
    private Long problemId;

    /** 家庭成员ID */
    private Long memberId;

    /** 家庭成员姓名 */
    private String memberName;

    /** 操作人用户ID */
    private Long operatorUserId;

    /**
     * 状态变更操作者昵称。
     *
     * <p>App 展示历史记录时优先显示昵称；如果用户已注销或昵称为空，前端再降级展示默认文案。
     */
    private String operatorNickname;

    /** 操作类型 */
    private String actionType;

    /** 操作类型名称 */
    private String actionTypeName;

    /** 变更前状态 */
    private Integer beforeStatus;

    /** 变更前状态名称 */
    private String beforeStatusName;

    /** 变更后状态 */
    private Integer afterStatus;

    /** 变更后状态名称 */
    private String afterStatusName;

    /** 操作备注 */
    private String actionRemark;

    /** 操作时间 */
    private Date actionTime;
}
