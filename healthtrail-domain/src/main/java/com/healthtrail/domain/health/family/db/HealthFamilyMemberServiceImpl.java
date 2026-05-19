package com.healthtrail.domain.health.family.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 家庭成员数据库服务实现。
 */
@Service
public class HealthFamilyMemberServiceImpl extends ServiceImpl<HealthFamilyMemberMapper, HealthFamilyMemberEntity>
    implements HealthFamilyMemberService {

    @Override
    public boolean isOwnedByUser(Long memberId, Long ownerUserId) {
        QueryWrapper<HealthFamilyMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId)
            .eq("owner_user_id", ownerUserId);
        return this.baseMapper.exists(queryWrapper);
    }
}
