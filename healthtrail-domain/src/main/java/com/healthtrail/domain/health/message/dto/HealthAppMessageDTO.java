package com.healthtrail.domain.health.message.dto;

import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpRecommendedActionDTO;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App 消息中心列表 DTO。
 *
 * <p>列表页除了基础消息信息外，
 * 还直接带出跳转和推荐动作，避免 App 再自行解析 JSON。
 */
@Data
@NoArgsConstructor
public class HealthAppMessageDTO {

    /** 消息ID。 */
    private Long messageId;

    /** 成员ID。 */
    private Long memberId;

    /** 成员姓名。 */
    private String memberName;

    /** 业务场景。 */
    private String businessScene;

    /** 业务场景名称。 */
    private String businessSceneName;

    /** 业务ID。 */
    private Long businessId;

    /** 消息标题。 */
    private String messageTitle;

    /** 消息内容。 */
    private String messageContent;

    /** 已读状态。 */
    private Integer readStatus;

    /** 已读时间。 */
    private Date readTime;

    /** 发送状态。 */
    private Integer sendStatus;

    /** 发送时间。 */
    private Date sendTime;

    /** 发送重试次数。 */
    private Integer sendRetryCount;

    /** 发送渠道。 */
    private String sendChannel;

    /** 发送结果。 */
    private String sendResultMessage;

    /** 创建时间。 */
    private Date createTime;

    /** navigation。 */
    private HealthFollowUpNavigationDTO navigation;

    /** recommendedAction。 */
    private HealthFollowUpRecommendedActionDTO recommendedAction;
}
