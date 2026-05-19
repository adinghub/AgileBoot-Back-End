package com.healthtrail.api.controller.common;

import com.healthtrail.api.customize.service.login.AppLoginService;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.user.command.AppLoginCommand;
import com.healthtrail.domain.health.user.command.AppRefreshTokenCommand;
import com.healthtrail.domain.health.user.command.AppRegisterCommand;
import com.healthtrail.domain.health.user.dto.AppLoginDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端登录注册控制器。
 *
 * <p>该控制器放在 common 路径下，原因是登录注册天然属于匿名可访问接口。
 * 当前安全配置已经放行 `/common/**`，这样可以避免单独再维护一套匿名白名单。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/common/app/auth")
@Tag(name = "App认证API", description = "App用户登录注册接口")
public class LoginController extends BaseController {

    private final AppLoginService appLoginService;

    /**
     * App 用户注册。
     *
     * @param registerCommand 注册命令
     * @return 注册成功后的登录态信息
     */
    @Operation(summary = "App用户注册")
    @PostMapping("/register")
    public ResponseDTO<AppLoginDTO> register(@Valid @RequestBody AppRegisterCommand registerCommand) {
        return ResponseDTO.ok(appLoginService.register(registerCommand));
    }

    /**
     * App 用户登录。
     *
     * @param loginCommand 登录命令
     * @return 登录成功后的 token 和用户信息
     */
    @Operation(summary = "App用户登录")
    @PostMapping("/login")
    public ResponseDTO<AppLoginDTO> login(@Valid @RequestBody AppLoginCommand loginCommand) {
        return ResponseDTO.ok(appLoginService.login(loginCommand));
    }

    /**
     * App refresh token 刷新登录态。
     *
     * <p>这个接口允许客户端在 access token 过期后，用仍然有效的 refresh token
     * 静默换取一套新的 access / refresh 令牌，避免用户频繁被打回登录页。
     */
    @Operation(summary = "App刷新登录态")
    @PostMapping("/refresh")
    public ResponseDTO<AppLoginDTO> refresh(@Valid @RequestBody AppRefreshTokenCommand refreshTokenCommand) {
        return ResponseDTO.ok(appLoginService.refresh(refreshTokenCommand));
    }
}
