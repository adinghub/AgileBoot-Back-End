package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 体检报告指标异常标记枚举。
 *
 * <p>该枚举用于表达单条指标的判定结果，
 * 它是体检报告“结构化结果”和“异常分析摘要”之间最关键的桥梁。
 */
@Dictionary(name = "health.reportItemAbnormalFlag")
public enum HealthReportItemAbnormalFlagEnum implements DictionaryEnum<Integer> {

    /**
     * 当前无法自动判断。
     * 常见于定性结果复杂、缺少参考范围或结果格式过于特殊的场景。
     */
    UNKNOWN(0, "待判断", CssTag.INFO),

    /**
     * 正常。
     */
    NORMAL(1, "正常", CssTag.SUCCESS),

    /**
     * 偏低。
     */
    LOW(2, "偏低", CssTag.WARNING),

    /**
     * 偏高。
     */
    HIGH(3, "偏高", CssTag.DANGER),

    /**
     * 异常。
     * 主要用于定性结果无法明确区分高低，但可以确认“不正常”的情况。
     */
    ABNORMAL(4, "异常", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    HealthReportItemAbnormalFlagEnum(int value, String description, String cssTag) {
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
