package com.healthtrail.domain.health.user.dto;

import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App 用户信息返回对象。
 *
 * <p>该 DTO 用于返回给 App 前端展示，不暴露密码等敏感字段。
 */
@Data
@NoArgsConstructor
public class AppUserDTO {

    /** 用户ID。 */
    private Long userId;

    /** 登录手机号。 */
    private String mobile;

    /** 用户昵称。 */
    private String nickname;

    /** 头像地址。 */
    private String avatar;

    /** 账号状态。 */
    private Integer status;

    /** 最近登录时间。 */
    private Date lastLoginTime;

    /** 注册来源。 */
    private String registerSource;

    public AppUserDTO(HealthAppUserEntity entity) {
        if (entity != null) {
            this.userId = entity.getUserId();
            this.mobile = entity.getMobile();
            this.nickname = entity.getNickname();
            this.avatar = entity.getAvatar();
            this.status = entity.getStatus();
            this.lastLoginTime = entity.getLastLoginTime();
            this.registerSource = entity.getRegisterSource();
        }
    }
}
