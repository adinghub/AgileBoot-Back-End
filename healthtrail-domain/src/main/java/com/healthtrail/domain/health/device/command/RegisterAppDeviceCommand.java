package com.healthtrail.domain.health.device.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * App 设备注册命令对象。
 *
 * <p>设备注册是提醒 Push 能力的基础动作。
 * App 每次拿到新的设备 Token 后，都应该通过这个命令把“当前设备 + 当前账号”的绑定关系同步到后端。
 */
@Data
public class RegisterAppDeviceCommand {

    /**
     * 设备唯一编码。
     * 建议由 App 侧为一次安装实例生成稳定值，例如 UUID。
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 64, message = "设备编码长度不能超过64个字符")
    private String deviceCode;

    /**
     * Push 平台，例如 ANDROID / IOS / HARMONY。
     */
    @NotBlank(message = "推送平台不能为空")
    @Size(max = 20, message = "推送平台长度不能超过20个字符")
    private String pushPlatform;

    /**
     * 设备 Token。
     * 真正接入厂商 Push 时，这个值会用于精确定位设备。
     */
    @NotBlank(message = "设备Token不能为空")
    @Size(max = 255, message = "设备Token长度不能超过255个字符")
    private String deviceToken;

    /**
     * 设备型号。
     */
    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    private String deviceModel;

    /**
     * 手机厂商。
     */
    @Size(max = 100, message = "设备厂商长度不能超过100个字符")
    private String manufacturer;

    /**
     * 操作系统版本。
     */
    @Size(max = 50, message = "系统版本长度不能超过50个字符")
    private String osVersion;

    /**
     * App 版本号。
     */
    @Size(max = 50, message = "App版本长度不能超过50个字符")
    private String appVersion;
}
