package com.healthtrail.domain.health.message.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台 App 消息审计查询对象。
 *
 * <p>后台和 App 端的查询诉求不同：
 * 1. App 端更关注“我自己的消息”
 * 2. 后台更关注“某条消息为什么发了/没发、发给了谁、现在状态如何”
 *
 * <p>因此后台查询额外补充了用户、业务主键、发送状态、关键字等筛选项，
 * 方便运营、客服和开发排查消息链路问题。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthAppMessageAdminQuery extends AbstractPageQuery<HealthAppMessageEntity> {

    /**
     * App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 业务场景编码。
     */
    private String businessScene;

    /**
     * 业务主键ID。
     */
    private Long businessId;

    /**
     * 已读状态。
     */
    private Integer readStatus;

    /**
     * 最近一次发送状态。
     */
    private Integer sendStatus;

    /**
     * 去重键。
     */
    private String dedupKey;

    /**
     * 关键字。
     *
     * <p>当前支持消息标题、消息正文、成员名称快照模糊搜索。
     */
    private String keyword;

    @Override
    public QueryWrapper<HealthAppMessageEntity> addQueryCondition() {
        QueryWrapper<HealthAppMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(memberId != null, "member_id", memberId)
            .eq(StrUtil.isNotBlank(businessScene), "business_scene", businessScene)
            .eq(businessId != null, "business_id", businessId)
            .eq(readStatus != null, "read_status", readStatus)
            .eq(sendStatus != null, "send_status", sendStatus)
            .eq(StrUtil.isNotBlank(dedupKey), "dedup_key", dedupKey)
            .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper.like("message_title", keyword)
                .or()
                .like("message_content", keyword)
                .or()
                .like("member_name_snapshot", keyword))
            .orderByDesc("message_id");
        return queryWrapper;
    }
}
