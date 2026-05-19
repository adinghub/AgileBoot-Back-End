package com.healthtrail.domain.system.log.dto;

import com.healthtrail.common.annotation.ExcelColumn;
import com.healthtrail.common.annotation.ExcelSheet;
import com.healthtrail.common.enums.common.LoginStatusEnum;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.domain.system.log.db.SysLoginInfoEntity;
import java.util.Date;
import lombok.Data;

/**
 * 登录日志返回对象
 * @author valarchie
 */
@Data
@ExcelSheet(name = "登录日志")
public class LoginLogDTO {

    public LoginLogDTO(SysLoginInfoEntity entity) {
        if (entity != null) {
            logId = entity.getInfoId() + "";
            username = entity.getUsername();
            ipAddress = entity.getIpAddress();
            loginLocation = entity.getLoginLocation();
            operationSystem = entity.getOperationSystem();
            browser = entity.getBrowser();
            status = entity.getStatus();
            statusStr = BasicEnumUtil.getDescriptionByValue(LoginStatusEnum.class, entity.getStatus());
            msg = entity.getMsg();
            loginTime = entity.getLoginTime();
        }
    }


    @ExcelColumn(name = "ID")
    private String logId;

    @ExcelColumn(name = "用户名")
    private String username;

    @ExcelColumn(name = "ip地址")
    private String ipAddress;

    @ExcelColumn(name = "登录地点")
    private String loginLocation;

    @ExcelColumn(name = "操作系统")
    private String operationSystem;

    @ExcelColumn(name = "浏览器")
    private String browser;

    private Integer status;

    @ExcelColumn(name = "状态")
    private String statusStr;

    @ExcelColumn(name = "描述")
    private String msg;

    @ExcelColumn(name = "登录时间")
    private Date loginTime;

}
