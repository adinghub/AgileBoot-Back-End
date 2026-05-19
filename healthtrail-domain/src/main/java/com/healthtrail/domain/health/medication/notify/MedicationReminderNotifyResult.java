package com.healthtrail.domain.health.medication.notify;

import lombok.Builder;
import lombok.Data;

/**
 * 用药提醒发送结果。
 *
 * <p>应用服务只关心“本次发送是否成功、由哪个通道发送、失败原因是什么”，
 * 因此这里用一个轻量结果对象承接发送器返回值，避免发送器把内部实现细节泄露到业务层。
 */
@Data
@Builder
public class MedicationReminderNotifyResult {

    /**
     * 是否发送成功。
     */
    private boolean success;

    /**
     * 发送通道标识，例如 APP_PUSH、SMS、IN_APP、LOG_SIMULATION。
     */
    private String channel;

    /**
     * 失败原因或补充说明。
     */
    private String message;
}
