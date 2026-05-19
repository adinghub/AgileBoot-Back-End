package com.healthtrail.domain.system.member;

import com.healthtrail.domain.system.member.db.UserMemberEntity;
import com.healthtrail.domain.system.member.enums.UserMemberStatusEnum;
import java.util.Date;
import java.util.Objects;

/**
 * 会员有效状态判断工具。
 *
 * <p>现有会员模块里既有“状态字段是否为 ACTIVE”的判断，也有“结束时间是否已过期”的判断，
 * 如果每个服务都自己组合条件，后续很容易出现：
 * 1. 某些地方把长期会员（结束时间为空）误判成无效；
 * 2. 某些地方把已过期但状态还没被异步任务回收的记录误判成有效；
 * 3. App 展示、会员准入、开通续费冲突判断口径不一致。
 *
 * <p>因此这里把“当前时刻是否仍然是有效会员”收口成唯一入口，后续所有会员门槛判断统一复用。
 */
public final class MemberActiveStatusSupport {

    private MemberActiveStatusSupport() {
    }

    /**
     * 判断给定会员关系在“当前时刻”是否有效。
     */
    public static boolean isMemberCurrentlyActive(UserMemberEntity userMemberEntity) {
        return isMemberCurrentlyActive(userMemberEntity, new Date());
    }

    /**
     * 判断给定会员关系在指定时刻是否有效。
     *
     * <p>有效定义如下：
     * 1. 记录存在；
     * 2. 状态为 ACTIVE；
     * 3. 结束时间为空时视为长期有效；
     * 4. 结束时间不为空时必须晚于当前判断时刻。
     */
    public static boolean isMemberCurrentlyActive(UserMemberEntity userMemberEntity, Date now) {
        if (userMemberEntity == null) {
            return false;
        }
        if (!Objects.equals(UserMemberStatusEnum.ACTIVE.name(), userMemberEntity.getStatus())) {
            return false;
        }
        Date evaluationTime = now == null ? new Date() : now;
        return userMemberEntity.getEffectiveEndTime() == null
            || userMemberEntity.getEffectiveEndTime().after(evaluationTime);
    }
}
