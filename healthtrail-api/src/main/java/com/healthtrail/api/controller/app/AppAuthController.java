package com.healthtrail.api.controller.app;

import com.healthtrail.api.customize.service.login.AppLoginService;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端已登录认证控制器。
 *
 * <p>登录和注册接口放在匿名可访问的 `/common` 路径下，
 * 而退出登录属于登录后接口，因此单独放在 `/app/auth` 路径下。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/auth")
@Tag(name = "App认证扩展API", description = "App用户已登录后的认证接口")
public class AppAuthController extends BaseController {

    private final AppLoginService appLoginService;

    /**
     * App 用户退出登录。
     *
     * @return 退出结果
     */
    @Operation(summary = "App用户退出登录")
    @PostMapping("/logout")
    public ResponseDTO<Void> logout() {
        appLoginService.logout();
        return ResponseDTO.ok();
    }
}
