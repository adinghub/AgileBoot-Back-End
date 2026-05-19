package com.healthtrail.infrastructure.user.app;

import com.healthtrail.infrastructure.user.base.BaseLoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 登录用户身份权限
 * @author valarchie
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AppLoginUser extends BaseLoginUser {

    private static final long serialVersionUID = 1L;

    /**
     * 预留给 App 端的会员能力开关。
     * 当前登录注册阶段暂时还没有完整的会员体系，但是先把登录态模型预留出来，
     * 后续做健康会员、报告高级分析、提醒增强能力时可以直接复用。
     */
    private boolean isVip;

    /**
     * App 端也沿用后台 token 的自动续期思路。
     * 这样在用户持续活跃访问时，不需要频繁重新登录。
     */
    private Long autoRefreshCacheTime;

    /**
     * 当前 access 登录态绑定的 refresh token 会话主键。
     *
     * <p>这样用户主动退出登录时，除了删除 access 登录缓存，
     * 还可以顺带让当前 refresh token 一并失效，避免退出后仍能静默续签。
     */
    private String refreshTokenKey;

    /**
     * 创建 App 登录态对象。
     *
     * @param userId    App 用户ID
     * @param isVip     是否会员
     * @param username  当前登录账号，这里统一存手机号，便于日志与审计复用
     * @param password  数据库存储的加密密码
     * @param cachedKey 登录缓存主键
     */
    public AppLoginUser(Long userId, Boolean isVip, String username, String password, String cachedKey) {
        this.userId = userId;
        this.isVip = isVip;
        this.username = username;
        this.password = password;
        this.cachedKey = cachedKey;
    }


}
