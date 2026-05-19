package com.healthtrail.domain.health.report.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 体检报告数据库服务实现。
 */
@Service
public class HealthReportServiceImpl extends ServiceImpl<HealthReportMapper, HealthReportEntity>
    implements HealthReportService {

    @Override
    public boolean isOwnedByUser(Long reportId, Long ownerUserId) {
        QueryWrapper<HealthReportEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_id", reportId)
            .eq("owner_user_id", ownerUserId);
        return this.baseMapper.exists(queryWrapper);
    }
}
