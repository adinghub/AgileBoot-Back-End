package com.healthtrail.domain.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.healthtrail.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import com.healthtrail.infrastructure.cache.redis.RedisCacheTemplate;
import com.healthtrail.infrastructure.user.web.SystemLoginUser;
import com.healthtrail.domain.system.dept.db.SysDeptEntity;
import com.healthtrail.domain.system.post.db.SysPostEntity;
import com.healthtrail.domain.system.role.db.SysRoleEntity;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * 缓存中心  提供全局访问点
 * 如果是领域类的缓存  可以自己新建一个直接放在CacheCenter   不用放在infrastructure包里的GuavaCacheService
 * 或者RedisCacheService
 * @author valarchie
 */
@Component
public class CacheCenter {

    /** 系统参数配置本地缓存 */
    public static AbstractGuavaCacheTemplate<String> configCache;

    /** 部门数据本地缓存 */
    public static AbstractGuavaCacheTemplate<SysDeptEntity> deptCache;

    /** 验证码 Redis 缓存 */
    public static RedisCacheTemplate<String> captchaCache;

    /** 后台登录用户 Redis 缓存 */
    public static RedisCacheTemplate<SystemLoginUser> loginUserCache;

    /** 系统用户实体 Redis 缓存 */
    public static RedisCacheTemplate<SysUserEntity> userCache;

    /** 角色实体 Redis 缓存 */
    public static RedisCacheTemplate<SysRoleEntity> roleCache;

    /** 岗位实体 Redis 缓存 */
    public static RedisCacheTemplate<SysPostEntity> postCache;

    @PostConstruct
    public void init() {
        GuavaCacheService guavaCache = SpringUtil.getBean(GuavaCacheService.class);
        RedisCacheService redisCache = SpringUtil.getBean(RedisCacheService.class);

        configCache = guavaCache.configCache;
        deptCache = guavaCache.deptCache;

        captchaCache = redisCache.captchaCache;
        loginUserCache = redisCache.loginUserCache;
        userCache = redisCache.userCache;
        roleCache = redisCache.roleCache;
        postCache = redisCache.postCache;
    }

}
