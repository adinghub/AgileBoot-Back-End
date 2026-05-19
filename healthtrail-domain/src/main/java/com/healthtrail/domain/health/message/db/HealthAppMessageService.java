package com.healthtrail.domain.health.message.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * App 消息中心 Service。
 */
public interface HealthAppMessageService extends IService<HealthAppMessageEntity> {

    /**
     * 按归属用户和去重键判断消息是否已存在。
     */
    boolean existsByOwnerUserIdAndDedupKey(Long ownerUserId, String dedupKey);
}
