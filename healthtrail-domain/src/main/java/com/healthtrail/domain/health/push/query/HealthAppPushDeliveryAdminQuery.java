package com.healthtrail.domain.health.push.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.push.db.HealthAppPushDeliveryLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台 App Push 设备级派发审计查询对象。
 *
 * <p>与设备管理查询一样，凡是可以直接落在 `app_push_delivery_log` 表上的条件，
 * 统一在这里生成；需要跨消息表、用户表补充的条件，则交给应用服务处理。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HealthAppPushDeliveryAdminQuery extends AbstractPageQuery<HealthAppPushDeliveryLogEntity> {

    private Long messageId;

    private Long ownerUserId;

    private String mobile;

    private String businessScene;

    private Long businessId;

    private Long deviceId;

    private String deviceCode;

    private String pushPlatform;

    private Integer messageSendStatus;

    private Integer deviceSendStatus;

    private String failureReasonCategory;

    @Override
    public QueryWrapper<HealthAppPushDeliveryLogEntity> addQueryCondition() {
        QueryWrapper<HealthAppPushDeliveryLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(messageId != null, "message_id", messageId)
            .eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(StrUtil.isNotBlank(businessScene), "business_scene", businessScene)
            .eq(businessId != null, "business_id", businessId)
            .eq(deviceId != null, "device_id", deviceId)
            .eq(StrUtil.isNotBlank(deviceCode), "device_code", deviceCode)
            .eq(StrUtil.isNotBlank(pushPlatform), "push_platform", pushPlatform)
            .eq(deviceSendStatus != null, "send_status", deviceSendStatus)
            .eq(StrUtil.isNotBlank(failureReasonCategory), "failure_reason_category", failureReasonCategory)
            .orderByDesc("delivery_id");
        return queryWrapper;
    }
}
