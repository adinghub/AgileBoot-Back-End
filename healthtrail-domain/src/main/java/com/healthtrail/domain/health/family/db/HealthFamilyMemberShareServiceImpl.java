package com.healthtrail.domain.health.family.db;

import com.healthtrail.common.enums.health.FamilyMemberShareStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 家庭成员共享关系服务实现。
 */
@Service
public class HealthFamilyMemberShareServiceImpl
    extends ServiceImpl<HealthFamilyMemberShareMapper, HealthFamilyMemberShareEntity>
    implements HealthFamilyMemberShareService {

    @Override
    public boolean existsEnabledShare(Long memberId, Long collaboratorUserId) {
        QueryWrapper<HealthFamilyMemberShareEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId)
            .eq("collaborator_user_id", collaboratorUserId)
            .eq("share_status", FamilyMemberShareStatusEnum.ENABLED.getValue());
        return this.baseMapper.exists(queryWrapper);
    }
}
