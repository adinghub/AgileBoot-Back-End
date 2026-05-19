package com.healthtrail.domain.health.dashboard.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 首页运营任务数据库服务实现。
 */
@Service
public class HealthOperationTaskServiceImpl
    extends ServiceImpl<HealthOperationTaskMapper, HealthOperationTaskEntity>
    implements HealthOperationTaskService {
}
