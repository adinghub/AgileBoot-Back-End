package com.healthtrail.domain.health.family.db;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 家庭共享邀请服务实现。
 */
@Service
public class HealthFamilyShareInviteServiceImpl
    extends ServiceImpl<HealthFamilyShareInviteMapper, HealthFamilyShareInviteEntity>
    implements HealthFamilyShareInviteService {

    @Override
    public HealthFamilyShareInviteEntity getByInviteCode(String inviteCode) {
        if (StrUtil.isBlank(inviteCode)) {
            return null;
        }
        QueryWrapper<HealthFamilyShareInviteEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("invite_code", inviteCode.trim().toUpperCase());
        return this.page(new Page<>(1, 1), queryWrapper).getRecords().stream().findFirst().orElse(null);
    }
}
