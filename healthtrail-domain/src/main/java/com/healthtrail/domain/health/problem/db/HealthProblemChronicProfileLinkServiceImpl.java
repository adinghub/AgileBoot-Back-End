package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 健康问题与慢病档案关联数据库服务实现 */
@Service
public class HealthProblemChronicProfileLinkServiceImpl
    extends ServiceImpl<HealthProblemChronicProfileLinkMapper, HealthProblemChronicProfileLinkEntity>
    implements HealthProblemChronicProfileLinkService {
}