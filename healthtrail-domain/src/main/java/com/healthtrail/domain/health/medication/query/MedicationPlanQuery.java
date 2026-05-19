package com.healthtrail.domain.health.medication.query;

import com.healthtrail.common.core.page.AbstractQuery;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用药计划查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MedicationPlanQuery extends AbstractQuery<HealthMedicationPlanEntity> {

    /**
     * 当前登录用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 状态。
     */
    private Integer status;

    @Override
    public QueryWrapper<HealthMedicationPlanEntity> addQueryCondition() {
        return new QueryWrapper<HealthMedicationPlanEntity>()
            .eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(memberId != null, "member_id", memberId)
            .eq(status != null, "status", status);
    }
}
