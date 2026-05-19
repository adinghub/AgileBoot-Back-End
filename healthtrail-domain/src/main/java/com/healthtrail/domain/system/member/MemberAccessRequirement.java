package com.healthtrail.domain.system.member;

import lombok.Data;

/**
 * 会员准入要求。
 *
 * <p>后续业务要接会员门槛时，统一构造这一对象即可，
 * 不需要在业务代码里自己拼“等级比较 + 权益开关 + 次数判断”。
 *
 * <p>常见使用方式：
 * 1. 只限制等级：只传 {@code requiredLevelCode}
 * 2. 只限制权益开关：只传 {@code featureCode}
 * 3. 同时限制等级与权益：两个字段都传
 * 4. 次数型权益要求至少剩余 N 次：再传 {@code requiredRemainingQuota}
 * 5. 限额型权益要求额度至少为 N：再传 {@code requiredLimitValue}
 */
@Data
public class MemberAccessRequirement {

    /**
     * 会员权益编码，例如 {@code AI_REPORT_SUMMARY}。
     */
    private String featureCode;

    /**
     * 最低会员等级编码，例如 {@code PLUS}。
     */
    private String requiredLevelCode;

    /**
     * 次数型权益至少还需要剩余多少次。
     *
     * <p>例如某个动作一次要消耗 1 次额度，这里通常就传 1。
     */
    private Integer requiredRemainingQuota;

    /**
     * 限额型权益至少要达到的额度。
     *
     * <p>例如某功能要求当前会员支持“至少 20 个家庭成员上限”，
     * 就可以把该门槛填到这里。
     */
    private Integer requiredLimitValue;
}
