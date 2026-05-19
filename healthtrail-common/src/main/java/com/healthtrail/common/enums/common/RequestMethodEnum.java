package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.BasicEnum;

/**
 * Http Method
 * @author valarchie
 */
public enum RequestMethodEnum implements BasicEnum<Integer> {

    /**
     * GET请求
     */
    GET(1, "GET"),
    /**
     * POST请求
     */
    POST(2, "POST"),
    /**
     * PUT请求
     */
    PUT(3, "PUT"),
    /**
     * DELETE请求
     */
    DELETE(4, "DELETE"),
    /**
     * 未知请求方式
     */
    UNKNOWN(-1, "UNKNOWN");

    private final int value;
    private final String description;

    RequestMethodEnum(int value, String description) {
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
