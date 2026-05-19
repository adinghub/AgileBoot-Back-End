package com.healthtrail.domain.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.healthtrail.infrastructure.cache.RedisUtil;
import com.healthtrail.infrastructure.cache.redis.CacheKeyEnum;
import com.healthtrail.infrastructure.cache.redis.RedisCacheTemplate;
import com.healthtrail.infrastructure.user.app.AppLoginUser;
import com.healthtrail.infrastructure.user.app.AppRefreshTokenSession;
import com.healthtrail.infrastructure.user.web.SystemLoginUser;
import com.healthtrail.domain.system.post.db.SysPostEntity;
import com.healthtrail.domain.system.role.db.SysRoleEntity;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.post.db.SysPostService;
import com.healthtrail.domain.system.role.db.SysRoleService;
import com.healthtrail.domain.system.user.db.SysUserService;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Redis 缓存服务，管理各类基于 Redis 的业务缓存实例（验证码、登录态、实体等）
 *
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class RedisCacheService {

    /** Redis 操作工具 */
    private final RedisUtil redisUtil;

    /** 验证码 Redis 缓存 */
    public RedisCacheTemplate<String> captchaCache;
    /** 后台管理系统登录用户 Redis 缓存 */
    public RedisCacheTemplate<SystemLoginUser> loginUserCache;
    /** App 端登录用户 Redis 缓存 */
    public RedisCacheTemplate<AppLoginUser> appLoginUserCache;
    /** App 端 refresh token 会话 Redis 缓存 */
    public RedisCacheTemplate<AppRefreshTokenSession> appRefreshTokenCache;
    /** 系统用户实体 Redis 缓存 */
    public RedisCacheTemplate<SysUserEntity> userCache;
    /** 角色实体 Redis 缓存 */
    public RedisCacheTemplate<SysRoleEntity> roleCache;

    /** 岗位实体 Redis 缓存 */
    public RedisCacheTemplate<SysPostEntity> postCache;

//    public RedisCacheTemplate<RoleInfo> roleModelInfoCache;

    @PostConstruct
    public void init() {

        captchaCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.CAPTCHAT);

        loginUserCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.LOGIN_USER_KEY);

        /**
         * App 端登录态和后台登录态必须隔离缓存前缀。
         * 否则相同的 cachedKey 在不同用户模型之间会互相污染，最终导致反序列化或权限上下文错乱。
         */
        appLoginUserCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.APP_LOGIN_USER_KEY);

        /**
         * App refresh token 会话单独分前缀，避免和 access 登录缓存混用。
         *
         * <p>两者虽然都服务于 App 登录，但生命周期和删除时机并不相同：
         * - access 缓存：接口访问直接鉴权使用
         * - refresh 会话：access 失效后的静默续签使用
         */
        appRefreshTokenCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.APP_REFRESH_TOKEN_KEY);

        userCache = new RedisCacheTemplate<SysUserEntity>(redisUtil, CacheKeyEnum.USER_ENTITY_KEY) {
            @Override
            public SysUserEntity getObjectFromDb(Object id) {
                SysUserService userService = SpringUtil.getBean(SysUserService.class);
                return userService.getById((Serializable) id);
            }
        };

        roleCache = new RedisCacheTemplate<SysRoleEntity>(redisUtil, CacheKeyEnum.ROLE_ENTITY_KEY) {
            @Override
            public SysRoleEntity getObjectFromDb(Object id) {
                SysRoleService roleService = SpringUtil.getBean(SysRoleService.class);
                return roleService.getById((Serializable) id);
            }
        };

//        roleModelInfoCache = new RedisCacheTemplate<RoleInfo>(redisUtil, CacheKeyEnum.ROLE_MODEL_INFO_KEY) {
//            @Override
//            public RoleInfo getObjectFromDb(Object id) {
//                UserDetailsService userDetailsService = SpringUtil.getBean(UserDetailsService.class);
//                return userDetailsService.getRoleInfo((Long) id);
//            }
//
//        };

        postCache = new RedisCacheTemplate<SysPostEntity>(redisUtil, CacheKeyEnum.POST_ENTITY_KEY) {
            @Override
            public SysPostEntity getObjectFromDb(Object id) {
                SysPostService postService = SpringUtil.getBean(SysPostService.class);
                return postService.getById((Serializable) id);
            }

        };


    }


}
