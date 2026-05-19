package com.healthtrail.domain.health.message.dto;

import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import java.util.Date;
import lombok.Data;

/**
 * 后台 App 消息审计列表 DTO。
 *
 * <p>后台列表的目标不是直接给终端用户展示，
 * 而是帮助后台人员快速回答几个问题：
 * 1. 这条消息发给了哪个 App 用户
 * 2. 来自哪个业务场景
 * 3. 发送有没有成功
 * 4. 用户读了没有
 */
@Data
public class HealthAppMessageAdminDTO {

    /**
     * 消息ID。
     */
    private Long messageId;

    /**
     * 归属 App 用户ID。
     */
    private Long ownerUserId;

    /**
     * App 用户手机号。
     */
    private String ownerUserMobile;

    /**
     * App 用户昵称。
     */
    private String ownerUserNickname;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 家庭成员名称快照。
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
     * 业务主键ID。
     */
    private Long businessId;

    /**
     * 消息标题。
     */
    private String messageTitle;

    /**
     * 消息正文。
     */
    private String messageContent;

    /**
     * 已读状态。
     */
    private Integer readStatus;

    /**
     * 已读时间。
     */
    private Date readTime;

    /**
     * 最近一次发送状态。
     */
    private Integer sendStatus;

    /**
     * 最近一次发送时间。
     */
    private Date sendTime;

    /**
     * 最近一次发送通道。
     */
    private String sendChannel;

    /**
     * 发送重试次数。
     */
    private Integer sendRetryCount;

    /**
     * 最近一次发送结果说明。
     */
    private String sendResultMessage;

    /**
     * 消息去重键。
     */
    private String dedupKey;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 统一跳转参数。
     *
     * <p>后台虽然一般不会真正执行跳转，
     * 但保留这部分字段有助于排查“客户端收到后应该去哪里”。
     */
    private HealthFollowUpNavigationDTO navigation;
}
