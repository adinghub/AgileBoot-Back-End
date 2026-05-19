package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import lombok.Data;

/**
 * 首页消息预览 DTO。
 *
 * <p>首页消息预览的定位不是替代完整消息中心列表，
 * 而是给首页提供一个“最近有什么系统消息值得看”的轻量入口。
 *
 * <p>因此这里刻意只保留首页真正关心的字段：
 * 1. 用于展示的标题正文
 * 2. 用于角标和样式判断的已读状态
 * 3. 用于点击跳转的统一导航参数
 */
@Data
public class HealthHomeMessageDTO {

    /**
     * 消息ID。
     */
    private Long messageId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 家庭成员名称。
     */
    private String memberName;

    /**
     * 业务场景编码。
     */
    private String businessScene;

    /**
     * 业务场景名称。
     */
    private String businessSceneName;

    /**
     * 消息标题。
     */
    private String messageTitle;

    /**
     * 业务主键ID。
     */
    private Long businessId;

    /**
     * 消息正文。
     */
    private String messageContent;

    /**
     * 已读状态。
     */
    private Integer readStatus;

    /**
     * 消息发送时间。
     *
     * <p>首页消息预览更关注“最近一次真正送达用户的时间”，
     * 因此优先对齐消息中心列表中的 sendTime 语义。
     */
    private Date sendTime;

    /**
     * 点击首页消息卡片时的统一跳转参数。
     */
    private HealthFollowUpNavigationDTO navigation;

    /**
     * 推荐下一步动作。
     */
    private HealthFollowUpRecommendedActionDTO recommendedAction;
}
