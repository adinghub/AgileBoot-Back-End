package com.healthtrail.domain.system.member.dto;

import java.util.List;
import lombok.Data;

/**
 * App 当前用户门禁结果DTO。
 *
 * <p>这个对象直接面向前端页面消费，
 * 页面不需要知道 gateCode 背后绑定了什么 featureCode，只需要拿最终裁决结果即可。
 */
@Data
public class AppMemberGateAccessDTO {

    /** 门禁编码。 */
    private String gateCode;
    /** 门禁名称。 */
    private String gateName;
    /** allowed。 */
    private Boolean allowed;
    /** accessStatus。 */
    private String accessStatus;
    /** 策略类型。 */
    private String policyType;
    /** 权益编码。 */
    private String featureCode;
    /** 权益名称。 */
    private String featureName;
    /** 拒绝展示方式。 */
    private String denyClientMode;
    /** 拒绝标题。 */
    private String denyTitle;
    /** 拒绝文案。 */
    private String denyMessage;
    /** 是否引导会员页。 */
    private Boolean guideMemberPage;
    /** 权益上限。 */
    private Integer limitValue;
    /** 已用额度。 */
    private Integer usedValue;
    /** 剩余额度。 */
    private Integer remainingValue;
    /** 等级编码。 */
    private String levelCode;
    /** 等级名称。 */
    private String levelName;
    /**
     * 规则附加的等级白名单。
     *
     * <p>App 拿到这一组编码后，可以在无法访问时更准确地展示“哪些等级可用”，
     * 而不需要自己反推 gateCode 当前到底绑定了什么门禁规则。
     */
    private List<String> allowedLevelCodes;
}
