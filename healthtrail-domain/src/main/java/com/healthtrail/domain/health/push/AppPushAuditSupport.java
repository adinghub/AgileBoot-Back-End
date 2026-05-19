package com.healthtrail.domain.health.push;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppPushDeliveryFailureReasonCategoryEnum;
import com.healthtrail.common.enums.health.HealthAppPushDeviceActiveStatusEnum;
import java.util.Date;

/**
 * App Push 审计公共辅助类。
 *
 * <p>推送链路、后台设备管理页、推送审计页都需要共享同一套：
 * 1. 活跃状态口径；
 * 2. 失败分类口径；
 * 3. Token 脱敏规则；
 *
 * <p>因此把这些“口径级逻辑”集中放在一个帮助类里，
 * 避免不同模块各自实现后出现统计和展示不一致。
 */
public final class AppPushAuditSupport {

    /**
     * 当前系统对“没有任何可推设备”统一使用的失败提示文案。
     *
     * <p>消息中心、提醒表、后台统计都依赖这个口径，
     * 因此集中定义，避免相同问题出现多个近义文案。
     */
    public static final String NO_ACTIVE_DEVICE_TOKEN_MESSAGE = "当前用户没有可用的活跃设备Token";

    private static final int ACTIVE_DAYS = 7;

    private static final int SILENT_DAYS = 30;

    private AppPushAuditSupport() {
    }

    /**
     * 将原始设备状态与最后活跃时间转换为后台统一活跃标签。
     */
    public static String resolveDeviceActiveStatus(Integer deviceStatus, Date lastActiveTime) {
        if (deviceStatus != null && deviceStatus.equals(StatusEnum.DISABLE.getValue())) {
            return HealthAppPushDeviceActiveStatusEnum.DISABLED.getValue();
        }
        if (lastActiveTime != null && lastActiveTime.after(DateUtil.offsetDay(new Date(), -ACTIVE_DAYS))) {
            return HealthAppPushDeviceActiveStatusEnum.ACTIVE.getValue();
        }
        if (lastActiveTime != null && lastActiveTime.after(DateUtil.offsetDay(new Date(), -SILENT_DAYS))) {
            return HealthAppPushDeviceActiveStatusEnum.SILENT.getValue();
        }
        return HealthAppPushDeviceActiveStatusEnum.STALE.getValue();
    }

    /**
     * 把设备活跃状态编码转换成中文说明。
     */
    public static String resolveDeviceActiveStatusName(String activeStatus) {
        HealthAppPushDeviceActiveStatusEnum statusEnum = HealthAppPushDeviceActiveStatusEnum.fromValue(activeStatus);
        return statusEnum == null ? activeStatus : statusEnum.getDescription();
    }

    /**
     * 对设备 Token 做轻量脱敏，后台只保留最小必要的排障信息。
     */
    public static String maskDeviceToken(String deviceToken) {
        if (StrUtil.isBlank(deviceToken)) {
            return null;
        }
        if (deviceToken.length() <= 7) {
            return deviceToken.charAt(0) + "***";
        }
        return deviceToken.substring(0, 3) + "****" + deviceToken.substring(deviceToken.length() - 4);
    }

    /**
     * 根据发送结果文案归一化失败分类。
     *
     * <p>当前外部网关还没有完全稳定的结构化错误码返回，
     * 因此一期先采用“通道文案 + 已知固定口径”进行归类。
     */
    public static String resolveFailureReasonCategory(boolean success, String resultMessage) {
        if (success) {
            return null;
        }
        if (StrUtil.isBlank(resultMessage)) {
            return HealthAppPushDeliveryFailureReasonCategoryEnum.UNKNOWN.getValue();
        }
        if (StrUtil.equals(resultMessage, NO_ACTIVE_DEVICE_TOKEN_MESSAGE)) {
            return HealthAppPushDeliveryFailureReasonCategoryEnum.NO_ACTIVE_DEVICE.getValue();
        }
        if (StrUtil.containsAnyIgnoreCase(resultMessage, "停用")) {
            return HealthAppPushDeliveryFailureReasonCategoryEnum.DEVICE_DISABLED.getValue();
        }
        if (StrUtil.containsAnyIgnoreCase(resultMessage, "token", "Token")
            && StrUtil.containsAny(resultMessage, "空", "缺失", "没有")) {
            return HealthAppPushDeliveryFailureReasonCategoryEnum.DEVICE_TOKEN_EMPTY.getValue();
        }
        if (StrUtil.containsAnyIgnoreCase(resultMessage, "未启用", "配置不完整", "未返回响应", "异常", "超时", "timeout")) {
            return HealthAppPushDeliveryFailureReasonCategoryEnum.CHANNEL_EXCEPTION.getValue();
        }
        return HealthAppPushDeliveryFailureReasonCategoryEnum.VENDOR_REJECTED.getValue();
    }

    /**
     * 把失败分类编码转换成中文说明。
     */
    public static String resolveFailureReasonCategoryName(String category) {
        HealthAppPushDeliveryFailureReasonCategoryEnum categoryEnum =
            HealthAppPushDeliveryFailureReasonCategoryEnum.fromValue(category);
        return categoryEnum == null ? category : categoryEnum.getDescription();
    }

    /**
     * 把设备状态值转换成中文说明。
     */
    public static String resolveDeviceStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return status.equals(StatusEnum.ENABLE.getValue()) ? StatusEnum.ENABLE.description() : StatusEnum.DISABLE.description();
    }

    /**
     * 统一截断文本，避免单条厂商错误响应过长把后台审计字段撑爆。
     */
    public static String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
