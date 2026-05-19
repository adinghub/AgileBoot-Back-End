package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;
import com.healthtrail.common.enums.DictionaryEnum;

/**
 * 对应sys_operation_log的business_type
 *
 * @author valarchie
 */
@Dictionary(name = "sysOperationLog.businessType")
public enum BusinessTypeEnum implements DictionaryEnum<Integer> {

    /**
     * 其他操作
     */
    OTHER(0, "其他操作", CssTag.INFO),
    /**
     * 添加操作
     */
    ADD(1, "添加", CssTag.PRIMARY),
    /**
     * 修改操作
     */
    MODIFY(2, "修改", CssTag.PRIMARY),
    /**
     * 删除操作
     */
    DELETE(3, "删除", CssTag.DANGER),
    /**
     * 授权操作
     */
    GRANT(4, "授权", CssTag.PRIMARY),
    /**
     * 导出操作
     */
    EXPORT(5, "导出", CssTag.WARNING),
    /**
     * 导入操作
     */
    IMPORT(6, "导入", CssTag.WARNING),
    /**
     * 强退操作
     */
    FORCE_LOGOUT(7, "强退", CssTag.DANGER),
    /**
     * 清空操作
     */
    CLEAN(8, "清空", CssTag.DANGER),
    ;

    private final int value;
    private final String description;
    private final String cssTag;

    BusinessTypeEnum(int value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }

}
