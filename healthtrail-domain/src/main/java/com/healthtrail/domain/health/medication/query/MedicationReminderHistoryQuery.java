package com.healthtrail.domain.health.medication.query;

import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 历史服药记录查询对象。
 *
 * <p>该查询对象同时服务：
 * 1. 历史服药记录页
 * 2. 依从率统计
 * 3. 趋势分析
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MedicationReminderHistoryQuery extends AbstractPageQuery<HealthMedicationReminderEntity> {

    /**
     * 当前登录账号ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 用药计划ID。
     */
    private Long planId;

    /**
     * 提醒状态。
     */
    private Integer reminderStatus;

    /**
     * 开始日期。
     */
    private Date startDate;

    /**
     * 结束日期。
     */
    private Date endDate;

    @Override
    public QueryWrapper<HealthMedicationReminderEntity> addQueryCondition() {
        // 历史记录的实际查询条件由应用服务统一组装，这里返回空条件即可满足分页基类约束。
        return new QueryWrapper<>();
    }
}
