package com.healthtrail.domain.health.user.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * App 端登录命令对象。
 *
 * <p>当前阶段 App 登录采用“手机号 + 密码”的最小可用方案，
 * 后续如果增加验证码登录、微信登录，也可以继续在当前命令模型基础上扩展。
 */
@Data
public class AppLoginCommand {

    /**
     * 手机号作为 App 端一期的唯一登录账号。
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 原始密码，由服务端统一完成加密比对。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间")
    private String password;
}
