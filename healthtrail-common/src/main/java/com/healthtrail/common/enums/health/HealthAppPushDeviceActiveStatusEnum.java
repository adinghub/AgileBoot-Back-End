package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * App Push 设备活跃状态枚举。
 *
 * <p>这里的状态是后台为了排障和运营观察派生出来的“视图状态”，
 * 并不会直接写回数据库。它的作用是把原始的：
 * 1. 设备启用状态；
 * 2. 最后活跃时间；
 *
 * <p>转换成后台更容易理解的统一标签。
 */
public enum HealthAppPushDeviceActiveStatusEnum {

    /**
     * 设备启用中，且最近 7 天内活跃。
     */
    ACTIVE("ACTIVE", "活跃"),

    /**
     * 设备启用中，但最近 7 天没有活跃，30 天内活跃过。
     */
    SILENT("SILENT", "静默"),

    /**
     * 设备启用中，但最近 30 天没有活跃。
     */
    STALE("STALE", "长期未活跃"),

    /**
     * 设备已经被后台或系统停用。
     */
    DISABLED("DISABLED", "已停用");

    private final String value;

    private final String description;

    HealthAppPushDeviceActiveStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态编码反查枚举。
     */
    public static HealthAppPushDeviceActiveStatusEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthAppPushDeviceActiveStatusEnum item : values()) {
            if (Objects.equals(item.getValue(), value)) {
                return item;
            }
        }
        return null;
    }
}
