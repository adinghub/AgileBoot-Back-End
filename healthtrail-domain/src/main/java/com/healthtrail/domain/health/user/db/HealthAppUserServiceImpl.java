package com.healthtrail.domain.health.user.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * App 用户服务实现类。
 */
@Service
public class HealthAppUserServiceImpl extends ServiceImpl<HealthAppUserMapper, HealthAppUserEntity>
    implements HealthAppUserService {

    @Override
    public boolean isMobileDuplicated(String mobile, Long userId) {
        QueryWrapper<HealthAppUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile)
            .ne(userId != null, "user_id", userId);
        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public HealthAppUserEntity getByMobile(String mobile) {
        QueryWrapper<HealthAppUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        return this.getOne(queryWrapper);
    }
}
