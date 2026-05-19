package com.healthtrail.domain.health.report.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 体检报告指标结果数据库服务实现。
 */
@Service
public class HealthReportItemServiceImpl extends ServiceImpl<HealthReportItemMapper, HealthReportItemEntity>
    implements HealthReportItemService {
}
