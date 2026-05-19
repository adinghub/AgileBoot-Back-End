package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;
import java.util.Objects;

/**
 * 首页待跟进任务推荐动作风险等级枚举。
 *
 * <p>该等级不是医学诊断风险等级，
 * 只用于表达“当前这条任务在产品交互上建议多高优先关注”。
 *
 * <p>这样前端可以直接根据后端返回结果做颜色和强调样式，
 * 而不用自己再拆解任务类型、时间、异常标记重新判断。
 */
@Dictionary(name = "health.followUpRiskLevel")
public enum HealthFollowUpRiskLevelEnum implements DictionaryEnum<String> {

    /**
     * 高风险关注。
     */
    HIGH("HIGH", "高优先关注", CssTag.DANGER),

    /**
     * 中风险关注。
     */
    MEDIUM("MEDIUM", "中优先关注", CssTag.WARNING),

    /**
     * 低风险关注。
     */
    LOW("LOW", "低优先关注", CssTag.INFO);

    private final String value;

    private final String description;

    private final String cssTag;

    HealthFollowUpRiskLevelEnum(String value, String description, String cssTag) {
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
     * 按风险等级值解析枚举。
     */
    public static HealthFollowUpRiskLevelEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthFollowUpRiskLevelEnum riskLevelEnum : values()) {
            if (Objects.equals(riskLevelEnum.getValue(), value)) {
                return riskLevelEnum;
            }
        }
        return null;
    }
}
