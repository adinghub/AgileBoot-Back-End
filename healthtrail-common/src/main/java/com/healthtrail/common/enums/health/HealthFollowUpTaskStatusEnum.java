package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 首页待跟进任务状态枚举。
 *
 * <p>该状态只描述“首页任务流中的任务处理状态”，
 * 不直接等价于原始业务数据状态。
 * 例如：
 * 1. 用药提醒本身仍可能是待处理
 * 2. 但首页任务可以先被延后展示
 *
 * <p>因此这里单独定义任务状态，避免和提醒状态、报告状态混用。
 */
@Dictionary(name = "health.followUpTaskStatus")
public enum HealthFollowUpTaskStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 待跟进。
     */
    PENDING(0, "待跟进", CssTag.WARNING),

    /**
     * 已延后。
     */
    DELAYED(1, "已延后", CssTag.INFO),

    /**
     * 已完成。
     */
    COMPLETED(2, "已完成", CssTag.SUCCESS),

    /**
     * 已忽略。
     *
     * <p>忽略表示用户暂时不希望首页继续展示该任务，
     * 它和完成不同：完成更强调“本轮已经处理过”，
     * 忽略更强调“当前先不要打扰我”。
     */
    IGNORED(3, "已忽略", CssTag.INFO);

    private final int value;
    private final String description;
    private final String cssTag;

    HealthFollowUpTaskStatusEnum(int value, String description, String cssTag) {
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
