package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 健康问题状态变更日志数据库服务实现 */
@Service
public class HealthProblemStatusLogServiceImpl
    extends ServiceImpl<HealthProblemStatusLogMapper, HealthProblemStatusLogEntity>
    implements HealthProblemStatusLogService {
}