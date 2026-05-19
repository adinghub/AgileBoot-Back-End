package com.healthtrail.domain.health.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * App 登录注册统一返回对象。
 *
 * <p>注册成功后会直接返回登录态，因此注册与登录都复用同一个返回模型，
 * 这样前端接入时只需要维护一套成功处理逻辑。
 */
@Data
@AllArgsConstructor
public class AppLoginDTO {

    /**
     * JWT token 内容。
     */
    private String token;

    /**
     * token 类型，前端可以直接拼接成 Authorization 请求头。
     */
    private String tokenType;

    /**
     * refresh token 内容。
     *
     * <p>当前 App 端采用 access token + refresh token 双令牌模型：
     * - access token 用于日常接口访问
     * - refresh token 用于 access token 失效后的静默续签
     */
    private String refreshToken;

    /**
     * 当前登录成功的 App 用户信息。
     */
    private AppUserDTO userInfo;
}
