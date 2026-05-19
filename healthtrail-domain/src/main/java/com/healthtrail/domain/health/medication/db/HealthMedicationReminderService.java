package com.healthtrail.domain.health.medication.db;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Date;
import java.util.Set;

/**
 * 用药提醒记录数据库服务接口。
 */
public interface HealthMedicationReminderService extends IService<HealthMedicationReminderEntity> {

    /**
     * 校验提醒记录是否归属于指定 App 用户。
     *
     * @param reminderId 提醒记录ID
     * @param ownerUserId App 用户ID
     * @return true 表示归属关系成立
     */
    boolean isOwnedByUser(Long reminderId, Long ownerUserId);

    /**
     * 把指定账号名下已经过点但仍为待处理的提醒一次性更新成“已过期”。
     *
     * <p>这里直接返回数据库真实更新行数，而不是先 `count()` 再 `update()`：
     * 1. 可以把“查询数量 + 执行更新”两次 SQL 收敛成一次 SQL；
     * 2. 首页、今日提醒这类高频读入口在做状态自愈时，额外成本更可控；
     * 3. 调用方仍然能拿到本次真正变更了多少条记录。
     *
     * @param ownerUserId 指定 App 用户ID；为空时表示全局范围
     * @param now 过期判定时间点
     * @return 本次实际更新为“已过期”的提醒数量
     */
    int expirePendingRemindersBefore(Long ownerUserId, Date now);

    /**
     * 按成员范围把已经过点但仍为待处理的提醒一次性更新成“已过期”。
     *
     * <p>共享成员场景下，提醒记录的 owner_user_id 仍可能属于主账号，
     * 因此用户态入口需要支持按成员范围刷新，而不能只按 owner_user_id。
     *
     * @param memberIds 需要刷新的成员ID集合
     * @param now 过期判定时间点
     * @return 本次实际更新为“已过期”的提醒数量
     */
    int expirePendingRemindersBeforeByMemberIds(Set<Long> memberIds, Date now);
}
