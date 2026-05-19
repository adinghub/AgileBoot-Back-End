package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * App 推送平台枚举。
 *
 * <p>设备注册后，后续真正接入 Push 厂商 SDK 时通常都需要知道设备属于哪个平台。
 * 因此这里先统一收敛常见平台枚举，避免后续设备表直接落字符串散值。
 */
@Dictionary(name = "health.appPushPlatform")
public enum AppPushPlatformEnum implements DictionaryEnum<String> {

    /**
     * 安卓通用推送通道。
     */
    ANDROID("ANDROID", "Android", CssTag.PRIMARY),

    /**
     * iOS APNs 通道。
     */
    IOS("IOS", "iOS", CssTag.SUCCESS),

    /**
     * 鸿蒙设备。
     */
    HARMONY("HARMONY", "HarmonyOS", CssTag.WARNING);

    private final String value;
    private final String description;
    private final String cssTag;

    AppPushPlatformEnum(String value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public String getValue() {
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
