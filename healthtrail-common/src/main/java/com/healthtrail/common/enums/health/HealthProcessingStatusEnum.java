package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 健康扩展能力处理状态枚举。
 *
 * <p>该枚举给 OCR 占位解析、AI 总结占位能力复用，
 * 避免在报告模块里再额外发明多套含义相同的状态值。
 */
@Dictionary(name = "health.processingStatus")
public enum HealthProcessingStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 待处理。
     */
    PENDING(0, "待处理", CssTag.WARNING),

    /**
     * 处理中。
     */
    PROCESSING(1, "处理中", CssTag.INFO),

    /**
     * 已完成。
     */
    COMPLETED(2, "已完成", CssTag.SUCCESS),

    /**
     * 处理失败。
     */
    FAILED(3, "处理失败", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    HealthProcessingStatusEnum(int value, String description, String cssTag) {
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
