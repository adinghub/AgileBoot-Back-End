package com.healthtrail.domain.health.message.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractQuery;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台消息统计查询对象。
 *
 * <p>统计看板和列表页的诉求不同：
 * 1. 列表页强调逐条查看消息
 * 2. 统计看板强调快速了解整体运行状态
 *
 * <p>因此这里保留更偏“口径筛选”的字段，
 * 让后台可以按用户、场景、时间范围查看统计结果。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthAppMessageStatisticsQuery extends AbstractQuery<HealthAppMessageEntity> {

    /**
     * App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 业务场景编码。
     */
    private String businessScene;

    /**
     * 趋势图天数。
     *
     * <p>当前默认 7 天，最大 30 天，避免一次性返回太长趋势数据。
     */
    private Integer trendDays;

    /**
     * 失败原因排行条数。
     *
     * <p>当前默认返回前 5 条，最大 20 条，
     * 避免后台页面一次性返回过长的失败原因列表。
     */
    private Integer failureReasonTopLimit;

    @Override
    public QueryWrapper<HealthAppMessageEntity> addQueryCondition() {
        QueryWrapper<HealthAppMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(StrUtil.isNotBlank(businessScene), "business_scene", businessScene)
            .orderByDesc("message_id");
        return queryWrapper;
    }
}
