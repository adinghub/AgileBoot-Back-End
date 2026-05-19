package com.healthtrail.domain.system.member.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 会员等级功能关联数据库服务实现 */
@Service
public class MemberLevelFeatureServiceImpl extends ServiceImpl<MemberLevelFeatureMapper, MemberLevelFeatureEntity>
    implements MemberLevelFeatureService {
}
