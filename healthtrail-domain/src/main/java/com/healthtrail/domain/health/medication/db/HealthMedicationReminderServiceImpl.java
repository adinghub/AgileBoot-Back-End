package com.healthtrail.domain.health.medication.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import java.util.Date;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 用药提醒记录数据库服务实现。
 */
@Service
public class HealthMedicationReminderServiceImpl
    extends ServiceImpl<HealthMedicationReminderMapper, HealthMedicationReminderEntity>
    implements HealthMedicationReminderService {

    @Override
    public boolean isOwnedByUser(Long reminderId, Long ownerUserId) {
        // 提醒反馈必须限定在当前账号名下。
        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reminder_id", reminderId)
            .eq("owner_user_id", ownerUserId);
        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public int expirePendingRemindersBefore(Long ownerUserId, Date now) {
        if (now == null) {
            return 0;
        }

        // 这里刻意让数据库直接返回 update 行数，而不是先 count 再 update：
        // 1. 首页、提醒页每次读取都可能触发状态自愈，如果仍然先 count，会把高频读入口放大成两次 SQL；
        // 2. update 返回值就是本次真实发生状态迁移的数量，已经足够支撑上层统计与日志；
        // 3. 这也能避免 count 和 update 之间存在并发窗口，导致返回数量与最终实际更新数不一致。
        UpdateWrapper<HealthMedicationReminderEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq("reminder_status", MedicationReminderStatusEnum.PENDING.getValue())
            .lt("scheduled_time", now)
            .set("reminder_status", MedicationReminderStatusEnum.EXPIRED.getValue());
        return this.baseMapper.update(null, updateWrapper);
    }

    @Override
    public int expirePendingRemindersBeforeByMemberIds(Set<Long> memberIds, Date now) {
        if (memberIds == null || memberIds.isEmpty() || now == null) {
            return 0;
        }

        // 成员维度刷新是共享家庭成员场景的关键兜底。
        // 这里同样直接执行批量 update，让“今日提醒”读取链路不再额外产生一次 count SQL。
        UpdateWrapper<HealthMedicationReminderEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("member_id", memberIds)
            .eq("reminder_status", MedicationReminderStatusEnum.PENDING.getValue())
            .lt("scheduled_time", now)
            .set("reminder_status", MedicationReminderStatusEnum.EXPIRED.getValue());
        return this.baseMapper.update(null, updateWrapper);
    }
}
