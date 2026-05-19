package com.healthtrail.domain.health.user.command;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * App 端注册命令对象。
 *
 * <p>注册阶段只保留与一期登录链路强相关的核心字段，
 * 等家庭成员、健康档案等业务展开后，再逐步补充头像、生日、性别等资料完善流程。
 */
@Data
public class AppRegisterCommand {

    /**
     * 手机号作为 App 用户唯一账号。
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 昵称允许为空，后端会在注册时自动生成默认昵称。
     */
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    /**
     * 原始密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20位之间")
    private String password;

    /**
     * 确认密码用于避免用户误输入。
     */
    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, max = 20, message = "确认密码长度必须在6到20位之间")
    private String confirmPassword;
}
