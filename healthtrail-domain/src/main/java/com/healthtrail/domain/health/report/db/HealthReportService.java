package com.healthtrail.domain.health.report.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 体检报告数据库服务接口。
 */
public interface HealthReportService extends IService<HealthReportEntity> {

    /**
     * 检查报告是否归属于指定 App 用户。
     *
     * @param reportId 报告ID
     * @param ownerUserId App 用户ID
     * @return true 表示归属关系成立
     */
    boolean isOwnedByUser(Long reportId, Long ownerUserId);
}
