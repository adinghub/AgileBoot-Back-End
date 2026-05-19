package com.healthtrail.domain.health.device.dto;

import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpRecommendedActionDTO;
import lombok.Data;

/**
 * App Push 业务透传载荷 DTO。
 *
 * <p>该对象用于承接“真正发给 App 客户端的业务层数据”，
 * 它和具体厂商 Push SDK 无关，只描述客户端收到通知后真正关心的业务信息：
 * 1. 这是一条什么类型的业务通知
 * 2. 这条通知对应哪个业务主键
 * 3. 点击通知后应该跳到哪里
 * 4. 当前最建议用户先做什么
 *
 * <p>这样后续无论接入：
 * 1. 设备 Push 透传字段
 * 2. 站内信消息体
 * 3. WebSocket 实时通知
 *
 * <p>都可以直接复用同一份业务结构。
 */
@Data
public class HealthAppPushPayloadDTO {

    /**
     * 业务场景编码。
     *
     * <p>例如：
     * 1. `MEDICATION_REMINDER`
     * 2. `REPORT_FOLLOW_UP`
     */
    private String businessScene;

    /**
     * 业务主键ID。
     */
    private Long businessId;

    /**
     * 通知标题。
     *
     * <p>这是推荐给客户端做通知栏标题的业务标题，
     * 不强制等价于厂商 Push 的最终展示标题。
     */
    private String title;

    /**
     * 通知正文。
     */
    private String content;

    /**
     * 点击通知后的统一跳转参数。
     */
    private HealthFollowUpNavigationDTO navigation;

    /**
     * 当前最推荐的下一步动作。
     *
     * <p>客户端如果希望在通知详情页、落地页或通知展开卡片中展示更强引导，
     * 可直接复用这部分数据。
     */
    private HealthFollowUpRecommendedActionDTO recommendedAction;
}
