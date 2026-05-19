package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.BasicEnum;

/**
 *
 * @author valarchie
 */
@Deprecated
public enum MenuComponentEnum implements BasicEnum<Integer> {

    /**
     * 布局组件
     */
    LAYOUT(1,"Layout"),
    /**
     * 父视图组件
     */
    PARENT_VIEW(2,"ParentView"),
    /**
     * 内链组件
     */
    INNER_LINK(3,"InnerLink");

    private final int value;
    private final String description;

    MenuComponentEnum(int value, String description) {
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
