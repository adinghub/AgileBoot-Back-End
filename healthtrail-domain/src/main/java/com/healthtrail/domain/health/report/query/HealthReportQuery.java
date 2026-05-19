package com.healthtrail.domain.health.report.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 体检报告查询对象。
 *
 * <p>报告列表天然适合分页展示，因此沿用项目通用分页查询基类。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthReportQuery extends AbstractPageQuery<HealthReportEntity> {

    /**
     * 当前登录 App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID筛选。
     */
    private Long memberId;

    /**
     * 报告类型筛选。
     */
    private String reportType;

    /**
     * 解析状态筛选。
     */
    private Integer parseStatus;

    /**
     * 关键字。
     * 支持报告名称、医院名称、原始文件名模糊搜索。
     */
    private String keyword;

    @Override
    public QueryWrapper<HealthReportEntity> addQueryCondition() {
        QueryWrapper<HealthReportEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(memberId != null, "member_id", memberId)
            .eq(StrUtil.isNotBlank(reportType), "report_type", reportType)
            .eq(parseStatus != null, "parse_status", parseStatus)
            .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                .like("report_name", keyword)
                .or()
                .like("hospital_name", keyword)
                .or()
                .like("original_file_name", keyword))
            .orderByDesc("report_date")
            .orderByDesc("report_id");
        return queryWrapper;
    }
}
