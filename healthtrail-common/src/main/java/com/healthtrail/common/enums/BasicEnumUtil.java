package com.healthtrail.common.enums;

import cn.hutool.core.convert.Convert;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.enums.BasicEnum;

import java.util.Objects;

/**
 * 基础枚举工具类，提供根据枚举值安全查找枚举实例及获取描述等通用方法
 *
 * @author valarchie
 */
public class BasicEnumUtil {

    private BasicEnumUtil() {
    }

    public static final String UNKNOWN = "未知";

    /**
     * 安全地根据枚举值查找枚举实例。
     *
     * <p>这里把泛型边界明确收紧成“既是 Enum，又实现 BasicEnum”，
     * 这样遍历时就不需要再做原始类型强转，能够直接消除编译器的 unchecked warning。
     */
    public static <V, E extends Enum<E> & BasicEnum<V>> E fromValueSafely(Class<E> enumClass, Object value) {
        E target = null;

        for (E enumConstant : enumClass.getEnumConstants()) {
            if (Objects.equals(enumConstant.getValue(), value)) {
                target = enumConstant;
            }
        }

        return target;
    }

    public static <V, E extends Enum<E> & BasicEnum<V>> E fromValue(Class<E> enumClass, Object value) {
        E target = null;

        for (E enumConstant : enumClass.getEnumConstants()) {
            if (Objects.equals(enumConstant.getValue(), value)) {
                target = enumConstant;
            }
        }

        if (target == null) {
            throw new ApiException(ErrorCode.Internal.GET_ENUM_FAILED, enumClass.getSimpleName());
        }

        return target;
    }

    public static <V, E extends Enum<E> & BasicEnum<V>> String getDescriptionByBool(Class<E> enumClass, Boolean bool) {
        Integer value = Convert.toInt(bool, 0);
        return getDescriptionByValue(enumClass, value);
    }

    public static <V, E extends Enum<E> & BasicEnum<V>> String getDescriptionByValue(Class<E> enumClass, Object value) {
        E basicEnum = fromValueSafely(enumClass, value);
        if (basicEnum != null) {
            return basicEnum.description();
        }
        return UNKNOWN;
    }

}
