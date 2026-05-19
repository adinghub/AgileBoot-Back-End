package com.healthtrail.domain.system.log;

import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.common.command.BulkOperationCommand;
import com.healthtrail.domain.system.log.dto.LoginLogDTO;
import com.healthtrail.domain.system.log.query.LoginLogQuery;
import com.healthtrail.domain.system.log.dto.OperationLogDTO;
import com.healthtrail.domain.system.log.query.OperationLogQuery;
import com.healthtrail.domain.system.log.db.SysLoginInfoEntity;
import com.healthtrail.domain.system.log.db.SysOperationLogEntity;
import com.healthtrail.domain.system.log.db.SysLoginInfoService;
import com.healthtrail.domain.system.log.db.SysOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 系统日志查询与导出应用服务 */
@Service
@RequiredArgsConstructor
public class LogApplicationService {

    /** 登录日志数据库服务 */
    // TODO 命名到时候统一改成叫LoginLog
    private final SysLoginInfoService loginInfoService;

    /** 操作日志数据库服务 */
    private final SysOperationLogService operationLogService;

    public PageDTO<LoginLogDTO> getLoginInfoList(LoginLogQuery query) {
        Page<SysLoginInfoEntity> page = loginInfoService.page(query.toPage(), query.toQueryWrapper());
        List<LoginLogDTO> records = page.getRecords().stream().map(LoginLogDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public void deleteLoginInfo(BulkOperationCommand<Long> deleteCommand) {
        QueryWrapper<SysLoginInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("info_id", deleteCommand.getIds());
        loginInfoService.remove(queryWrapper);
    }

    public PageDTO<OperationLogDTO> getOperationLogList(OperationLogQuery query) {
        Page<SysOperationLogEntity> page = operationLogService.page(query.toPage(), query.toQueryWrapper());
        List<OperationLogDTO> records = page.getRecords().stream().map(OperationLogDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public void deleteOperationLog(BulkOperationCommand<Long> deleteCommand) {
        operationLogService.removeBatchByIds(deleteCommand.getIds());
    }

}
