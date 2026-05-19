package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * 首页待跟进任务跳转页面枚举。
 *
 * <p>这里不直接把 App 端真实路由路径写死到后端，
 * 而是输出稳定的页面编码，让不同客户端各自映射到本端页面。
 *
 * <p>这样做的好处是：
 * 1. 后端能表达“应该跳哪类业务页”
 * 2. 前端仍保留自身路由组织自由度
 * 3. 后续如果页面路径调整，后端协议无需跟着频繁改动
 */
public enum HealthFollowUpTargetPageEnum {

    /**
     * 用药提醒处理页。
     */
    MEDICATION_REMINDER_DETAIL("MEDICATION_REMINDER_DETAIL", "用药提醒处理页"),

    /**
     * 体检报告详情页。
     *
     * <p>报告建议类任务仍然归属于报告详情上下文，
     * 只是在打开后前端可以优先定位到建议/分析区域。
     */
    HEALTH_REPORT_DETAIL("HEALTH_REPORT_DETAIL", "体检报告详情页"),

    /**
     * 消息中心页。
     *
     * <p>运营任务和系统引导消息有时只需要把用户带到统一消息入口，
     * 因此这里补充一个通用消息中心页面编码，供 App 做底部导航跳转。
     */
    MESSAGE_CENTER("MESSAGE_CENTER", "消息中心页"),

    /**
     * 用药计划列表页。
     *
     * <p>当前 App 工作台里“用药计划”以标签页形式存在，
     * 后端只输出稳定页面编码，由客户端自行映射到本端路由或 tab。
     */
    WORKSPACE_PLAN_LIST("WORKSPACE_PLAN_LIST", "用药计划页"),

    /**
     * 今日提醒列表页。
     */
    WORKSPACE_REMINDER_LIST("WORKSPACE_REMINDER_LIST", "今日提醒页"),

    /**
     * 个人药柜页。
     *
     * <p>低库存提醒消息会引导用户回到这里处理补库存动作，
     * 前端可将该页面编码映射到“我的药柜列表”或“指定药品详情页”。
     */
    WORKSPACE_DRUG_CABINET("WORKSPACE_DRUG_CABINET", "个人药柜页"),

    /**
     * 慢病专项页。
     *
     * <p>慢病专项是按 diseaseCode 配置驱动的通用页面，
     * 后端只输出这个稳定编码，App 再决定打开工作台慢病标签或指定档案详情。
     */
    WORKSPACE_CHRONIC_DISEASE("WORKSPACE_CHRONIC_DISEASE", "慢病专项页"),

    /**
     * 健康洞察工作台。
     *
     * <p>健康问题状态变更、健康时间线等聚合型事件不天然属于某一份报告或某一个慢病专项，
     * 因此提供一个稳定的健康洞察页编码，App 可以据此回到健康洞察工作台再定位到问题中心。
     */
    WORKSPACE_HEALTH_INSIGHT("WORKSPACE_HEALTH_INSIGHT", "健康洞察工作台");

    private final String value;

    private final String description;

    HealthFollowUpTargetPageEnum(String value, String description) {
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
     * 按页面编码解析枚举。
     */
    public static HealthFollowUpTargetPageEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthFollowUpTargetPageEnum pageEnum : values()) {
            if (Objects.equals(pageEnum.getValue(), value)) {
                return pageEnum;
            }
        }
        return null;
    }
}
