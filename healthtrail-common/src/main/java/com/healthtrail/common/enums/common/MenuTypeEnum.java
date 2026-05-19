package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.BasicEnum;

/**
 * @author valarchie
 * 对应 sys_menu表的menu_type字段
 */
public enum MenuTypeEnum implements BasicEnum<Integer> {

    /**
     * 页面菜单
     */
    MENU(1, "页面"),
    /**
     * 目录菜单
     */
    CATALOG(2, "目录"),
    /**
     * 内嵌Iframe菜单
     */
    IFRAME(3, "内嵌Iframe"),
    /**
     * 外链跳转菜单
     */
    OUTSIDE_LINK_REDIRECT(4, "外链跳转");

    private final int value;
    private final String description;

    MenuTypeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }


}
