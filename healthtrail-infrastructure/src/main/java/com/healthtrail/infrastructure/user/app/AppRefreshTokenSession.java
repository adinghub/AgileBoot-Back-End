package com.healthtrail.infrastructure.user.app;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App refresh token 对应的服务端会话快照。
 *
 * <p>refresh token 本身只是一张“可校验的票据”，真正的续签控制仍然放在服务端缓存里。
 * 这样可以支持：
 * 1. refresh token 轮换；
 * 2. 主动失效当前 refresh 会话；
 * 3. refresh 时顺带删除旧 access 登录缓存，避免旧令牌长期继续可用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppRefreshTokenSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前 refresh token 对应的 App 用户ID。
     */
    private Long userId;

    /**
     * 当前 refresh token 自身的服务端会话主键。
     *
     * <p>把它一并存进会话后，refresh 成功轮换时就能直接删除旧 refresh 记录，
     * 不需要再次反向解析 JWT 或重复推断缓存 key。
     */
    private String refreshTokenKey;

    /**
     * 与本次 refresh token 配对的 access 登录缓存主键。
     *
     * <p>当 refresh 成功后，服务端会优先删除这个旧 access 缓存，
     * 然后再生成新的 access / refresh 组合，实现一次完整轮换。
     */
    private String accessCachedKey;
}
