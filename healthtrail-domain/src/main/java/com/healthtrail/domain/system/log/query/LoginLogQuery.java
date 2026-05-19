package com.healthtrail.domain.system.log.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.log.db.SysLoginInfoEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志查询对象
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginLogQuery extends AbstractPageQuery<SysLoginInfoEntity> {

    /** IP地址 */
    private String ipAddress;
    /** 登录状态 */
    private String status;
    /** 用户名 */
    private String username;


    @Override
    public QueryWrapper<SysLoginInfoEntity> addQueryCondition() {
        QueryWrapper<SysLoginInfoEntity> queryWrapper = new QueryWrapper<SysLoginInfoEntity>()
            .like(StrUtil.isNotEmpty(ipAddress), "ip_address", ipAddress)
            .eq(StrUtil.isNotEmpty(status), "status", status)
            .like(StrUtil.isNotEmpty(username), "username", username);

        addSortCondition(queryWrapper);

        // 可以手动设置  也可以由前端传回
//        this.timeRangeColumn = "login_time";
//        addTimeCondition(queryWrapper, "login_time");

        return queryWrapper;
    }
}
