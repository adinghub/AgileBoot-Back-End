package com.healthtrail.domain.system.member.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 会员功能配额使用记录数据库服务实现 */
@Service
public class MemberFeatureQuotaUsageServiceImpl
    extends ServiceImpl<MemberFeatureQuotaUsageMapper, MemberFeatureQuotaUsageEntity>
    implements MemberFeatureQuotaUsageService {
}
