package com.healthtrail.domain.health.message.query;

import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * App 消息中心查询对象。
 *
 * <p>当前先保留最常用的查询条件：
 * 1. 按场景筛选
 * 2. 按已读状态筛选
 * 3. 按分页滚动读取
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthAppMessageQuery extends AbstractPageQuery<HealthAppMessageEntity> {

    /**
     * 当前登录用户ID。
     */
    private Long ownerUserId;

    /**
     * 业务场景编码。
     */
    private String businessScene;

    /**
     * 已读状态。
     */
    private Integer readStatus;

    @Override
    public QueryWrapper<HealthAppMessageEntity> addQueryCondition() {
        QueryWrapper<HealthAppMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(businessScene != null && !businessScene.isEmpty(), "business_scene", businessScene)
            .eq(readStatus != null, "read_status", readStatus)
            .orderByAsc("read_status")
            .orderByDesc("message_id");
        return queryWrapper;
    }
}
