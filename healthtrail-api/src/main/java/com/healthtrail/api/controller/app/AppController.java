package com.healthtrail.api.controller.app;

import com.healthtrail.api.customize.service.login.AppLoginService;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.user.dto.AppUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 用户基础控制器。
 *
 * <p>这里先提供最基础的“获取当前登录用户”接口，
 * 方便 App 在登录成功后进入首页时立即拉取当前用户资料。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/user")
@Tag(name = "App用户API", description = "App用户基础信息接口")
public class AppController extends BaseController {

    private final AppLoginService appLoginService;

    /**
     * 获取当前登录的 App 用户信息。
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前登录用户")
    @GetMapping("/current")
    public ResponseDTO<AppUserDTO> currentUser() {
        return ResponseDTO.ok(appLoginService.getCurrentUser());
    }
}
