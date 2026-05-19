package com.healthtrail.domain.health.dashboard.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 首页运营任务分页查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthOperationTaskQuery extends AbstractPageQuery<HealthOperationTaskEntity> {

    /**
     * 目标 App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 风险等级。
     */
    private String riskLevel;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 关键字，匹配标题和内容。
     */
    private String keyword;

    @Override
    public QueryWrapper<HealthOperationTaskEntity> addQueryCondition() {
        QueryWrapper<HealthOperationTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(memberId != null, "member_id", memberId)
            .eq(status != null, "status", status)
            .eq(StrUtil.isNotBlank(riskLevel), "risk_level", riskLevel)
            .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                .like("task_title", keyword)
                .or()
                .like("task_content", keyword))
            .orderByDesc("status")
            .orderByDesc("priority_weight")
            .orderByDesc("start_time")
            .orderByDesc("operation_task_id");
        return queryWrapper;
    }
}
