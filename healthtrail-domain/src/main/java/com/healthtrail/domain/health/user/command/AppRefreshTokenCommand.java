package com.healthtrail.domain.health.user.command;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * App refresh token 刷新命令。
 *
 * <p>由于 refresh token 可能在 access token 已失效时单独使用，
 * 因此这里不复用 Authorization 请求头，而是明确从请求体中接收当前 refresh token。
 */
@Data
public class AppRefreshTokenCommand {

    /**
     * 当前客户端持有的 refresh token。
     */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
