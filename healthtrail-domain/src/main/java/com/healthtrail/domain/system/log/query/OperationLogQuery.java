package com.healthtrail.domain.system.log.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.log.db.SysOperationLogEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询对象
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogQuery extends AbstractPageQuery<SysOperationLogEntity> {

    /** 业务类型 */
    private String businessType;
    /** 操作状态 */
    private String status;
    /** 用户名 */
    private String username;
    /** 请求模块 */
    private String requestModule;

    @Override
    public QueryWrapper<SysOperationLogEntity> addQueryCondition() {
        QueryWrapper<SysOperationLogEntity> queryWrapper = new QueryWrapper<SysOperationLogEntity>()
            .like(businessType!=null, "business_type", businessType)
            .eq(status != null, "status", status)
            .like(StrUtil.isNotEmpty(username), "username", username)
            .like(StrUtil.isNotEmpty(requestModule), "request_module", requestModule);

        this.timeRangeColumn = "operation_time";

        return queryWrapper;
    }
}
