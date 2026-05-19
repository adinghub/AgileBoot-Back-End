package com.healthtrail.domain.system.member.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 会员门槛规则数据库服务实现 */
@Service
public class MemberGateRuleServiceImpl extends ServiceImpl<MemberGateRuleMapper, MemberGateRuleEntity>
    implements MemberGateRuleService {
}
