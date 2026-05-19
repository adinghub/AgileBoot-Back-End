package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.dictionary.Dictionary;
import com.healthtrail.common.enums.BasicEnum;

/**
 * 操作者类型
 * @author valarchie
 */
@Dictionary(name = "sysOperationLog.operatorType")
public enum OperatorTypeEnum implements BasicEnum<Integer> {

    /**
     * 其他
     */
    OTHER(1, "其他"),
    /**
     * Web用户
     */
    WEB(2, "Web用户"),
    /**
     * 手机端用户
     */
    MOBILE(3, "手机端用户");

    private final int value;
    private final String description;

    OperatorTypeEnum(int value, String description) {
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
