package com.healthtrail.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录令牌信息
 * @author valarchie
 */
@Data
@AllArgsConstructor
public class TokenDTO {

    /** 令牌字符串 */
    private String token;

    /** 当前登录用户信息 */
    private CurrentLoginUserDTO currentUser;

}
