package com.healthtrail.domain.system.member.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 用户会员订单数据库服务实现 */
@Service
public class UserMemberOrderServiceImpl extends ServiceImpl<UserMemberOrderMapper, UserMemberOrderEntity>
    implements UserMemberOrderService {
}
