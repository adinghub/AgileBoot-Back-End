package com.healthtrail.domain.health.report.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 体检报告模型工厂。
 */
@Component
@RequiredArgsConstructor
public class HealthReportModelFactory {

    /** 报告数据库服务。 */
    private final HealthReportService reportService;

    public HealthReportModel create() {
        return new HealthReportModel();
    }

    public HealthReportModel loadById(Long reportId) {
        HealthReportEntity entity = reportService.getById(reportId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, reportId, "体检报告");
        }
        return new HealthReportModel(entity);
    }
}
