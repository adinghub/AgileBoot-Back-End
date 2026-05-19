package com.healthtrail.domain.health.message.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * App 消息中心 Service 实现。
 */
@Service
public class HealthAppMessageServiceImpl extends ServiceImpl<HealthAppMessageMapper, HealthAppMessageEntity>
    implements HealthAppMessageService {

    @Override
    public boolean existsByOwnerUserIdAndDedupKey(Long ownerUserId, String dedupKey) {
        QueryWrapper<HealthAppMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_user_id", ownerUserId)
            .eq("dedup_key", dedupKey);
        return this.baseMapper.exists(queryWrapper);
    }
}
