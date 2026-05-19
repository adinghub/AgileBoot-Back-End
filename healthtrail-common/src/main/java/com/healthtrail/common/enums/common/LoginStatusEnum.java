package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;
import com.healthtrail.common.enums.DictionaryEnum;

/**
 * 用户状态
 * @author valarchie
 */
// TODO 表记得改成LoginLog
@Dictionary(name = "sysLoginLog.status")
public enum LoginStatusEnum implements DictionaryEnum<Integer> {
    /**
     * 登录成功
     */
    LOGIN_SUCCESS(1, "登录成功", CssTag.SUCCESS),
    /**
     * 退出成功
     */
    LOGOUT(2, "退出成功", CssTag.INFO),
    /**
     * 注册
     */
    REGISTER(3, "注册", CssTag.PRIMARY),
    /**
     * 登录失败
     */
    LOGIN_FAIL(0, "登录失败", CssTag.DANGER);

    private final int value;
    private final String msg;
    private final String cssTag;

    LoginStatusEnum(int status, String msg, String cssTag) {
        this.value = status;
        this.msg = msg;
        this.cssTag = cssTag;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return msg;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }
}
