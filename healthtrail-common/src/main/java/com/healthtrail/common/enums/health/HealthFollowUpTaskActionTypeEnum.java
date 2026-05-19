package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;
import java.util.Objects;

/**
 * 首页待跟进任务操作类型枚举。
 *
 * <p>首页任务的“状态”描述的是当前结果，
 * 而“操作类型”描述的是用户本次到底做了什么动作。
 *
 * <p>例如：
 * 1. 状态可能都变成“待跟进”
 * 2. 但一次是首次创建，一次是恢复显示
 *
 * <p>因此任务时间线不能只看最终状态，还需要单独记录动作类型。
 */
@Dictionary(name = "health.followUpTaskActionType")
public enum HealthFollowUpTaskActionTypeEnum implements DictionaryEnum<String> {

    /**
     * 已读。
     *
     * <p>首页任务已读不会改变任务状态，
     * 但它是一个真实发生过的用户行为，因此仍需进入任务日志。
     */
    READ("READ", "标记已读", CssTag.INFO),

    /**
     * 延后。
     */
    DELAY("DELAY", "延后任务", CssTag.WARNING),

    /**
     * 完成。
     */
    COMPLETE("COMPLETE", "完成任务", CssTag.SUCCESS),

    /**
     * 忽略。
     */
    IGNORE("IGNORE", "忽略任务", CssTag.INFO),

    /**
     * 恢复。
     *
     * <p>恢复的语义是把原先被隐藏、完成或延后的首页任务重新放回待跟进队列。
     */
    RESTORE("RESTORE", "恢复任务", CssTag.PRIMARY);

    private final String value;

    private final String description;

    private final String cssTag;

    HealthFollowUpTaskActionTypeEnum(String value, String description, String cssTag) {
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

    /**
     * 按操作值解析枚举。
     */
    public static HealthFollowUpTaskActionTypeEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthFollowUpTaskActionTypeEnum actionTypeEnum : values()) {
            if (Objects.equals(actionTypeEnum.getValue(), value)) {
                return actionTypeEnum;
            }
        }
        return null;
    }
}
