package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.BasicEnum;

/**
 * 系统配置
 * @author valarchie
 * 对应 sys_config表的config_key字段
 */
public enum ConfigKeyEnum implements BasicEnum<String> {

    /**
     * 系统皮肤主题配置键
     */
    SKIN_THEME("sys.index.skinName", "系统皮肤主题"),
    /**
     * 初始密码配置键
     */
    INIT_PASSWORD("sys.user.initPassword", "初始密码"),
    /**
     * 侧边栏开关配置键
     */
    SIDE_BAR_THEME("sys.index.sideTheme", "侧边栏开关"),
    /**
     * 验证码开关配置键
     */
    CAPTCHA("sys.account.captchaOnOff", "验证码开关"),
    /**
     * 注册开放功能配置键
     */
    REGISTER("sys.account.registerUser", "注册开放功能");

    private final String value;
    private final String description;

    ConfigKeyEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }


}
