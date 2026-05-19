package com.healthtrail.infrastructure.user.base;

import lombok.Data;

/**
 * 登录信息模型，记录每次登录的IP地址、地点、浏览器、操作系统和时间。
 * @author valarchie
 */
@Data
public class LoginInfo {

    /** 登录IP地址 */
    private String ipAddress;
    /** 登录地点 */
    private String location;
    /** 浏览器类型 */
    private String browser;
    /** 操作系统 */
    private String operationSystem;
    /** 登录时间 */
    private Long loginTime;

}
