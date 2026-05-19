package com.healthtrail.infrastructure.cache.redis;

import java.util.concurrent.TimeUnit;

/**
 * @author valarchie
 */
public enum CacheKeyEnum {

    /**
     * Redis各类缓存集合
     */
    CAPTCHAT("captcha_codes:", 2, TimeUnit.MINUTES),
    LOGIN_USER_KEY("login_tokens:", 30, TimeUnit.MINUTES),
    /**
     * App 端登录态缓存。
     *
     * <p>移动端用户通常预期“登录一次后较长时间内都不需要重复登录”，
     * 因此这里和后台管理端分开配置，单独放宽为 30 天。
     *
     * <p>注意：这并不代表完全放弃失效能力。
     * 只要用户主动退出、服务端主动删除缓存、Redis 重启丢失缓存，
     * 当前 token 仍然会失效；这里只是把“纯空闲超时”的窗口拉长，
     * 让 App 登录体验更接近日常消费型应用。
     */
    APP_LOGIN_USER_KEY("app_login_tokens:", 30, TimeUnit.DAYS),
    /**
     * App refresh token 服务端会话缓存。
     *
     * <p>refresh token 自身会写入 JWT 过期时间，但服务端仍保留一份可主动删除的会话记录，
     * 这样可以支持登录轮换、主动退出、服务端踢出等能力。
     */
    APP_REFRESH_TOKEN_KEY("app_refresh_tokens:", 30, TimeUnit.DAYS),
    RATE_LIMIT_KEY("rate_limit:", 60, TimeUnit.SECONDS),
    USER_ENTITY_KEY("user_entity:", 60, TimeUnit.MINUTES),
    ROLE_ENTITY_KEY("role_entity:", 60, TimeUnit.MINUTES),
    POST_ENTITY_KEY("post_entity:", 60, TimeUnit.MINUTES),
    ROLE_MODEL_INFO_KEY("role_model_info:", 60, TimeUnit.MINUTES),

    ;


    CacheKeyEnum(String key, int expiration, TimeUnit timeUnit) {
        this.key = key;
        this.expiration = expiration;
        this.timeUnit = timeUnit;
    }

    private final String key;
    private final int expiration;
    private final TimeUnit timeUnit;

    public String key() {
        return key;
    }

    public int expiration() {
        return expiration;
    }

    public TimeUnit timeUnit() {
        return timeUnit;
    }

}
