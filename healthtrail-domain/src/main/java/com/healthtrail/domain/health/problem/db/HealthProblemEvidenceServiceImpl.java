package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 健康问题证据数据库服务实现 */
@Service
public class HealthProblemEvidenceServiceImpl extends ServiceImpl<HealthProblemEvidenceMapper, HealthProblemEvidenceEntity>
    implements HealthProblemEvidenceService {
}
