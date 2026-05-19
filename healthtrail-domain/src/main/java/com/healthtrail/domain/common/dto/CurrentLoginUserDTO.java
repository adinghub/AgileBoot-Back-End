package com.healthtrail.domain.common.dto;

import com.healthtrail.domain.system.user.dto.UserDTO;
import java.util.Set;
import lombok.Data;

/**
 * 当前登录用户信息
 * @author valarchie
 */
@Data
public class CurrentLoginUserDTO {

    /** 用户信息 */
    private UserDTO userInfo;
    /** 角色标识 */
    private String roleKey;
    /** 权限集合 */
    private Set<String> permissions;

}
