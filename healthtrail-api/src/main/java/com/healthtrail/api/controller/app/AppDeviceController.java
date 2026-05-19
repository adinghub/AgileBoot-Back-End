package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.device.AppDeviceApplicationService;
import com.healthtrail.domain.health.device.command.RegisterAppDeviceCommand;
import com.healthtrail.domain.health.device.dto.AppDeviceDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端设备控制器。
 *
 * <p>该控制器用于维护提醒 Push 所依赖的设备绑定关系：
 * 1. 设备首次拿到 Token 后注册
 * 2. Token 刷新后重复上报
 * 3. 用户主动退出或禁用通知时停用设备
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/push/devices")
@Tag(name = "App设备API", description = "App端设备与推送注册接口")
public class AppDeviceController extends BaseController {

    private final AppDeviceApplicationService appDeviceApplicationService;

    /**
     * 注册或刷新当前设备。
     */
    @Operation(summary = "注册当前设备")
    @PostMapping
    public ResponseDTO<Void> register(@Valid @RequestBody RegisterAppDeviceCommand registerCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        appDeviceApplicationService.registerDevice(registerCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 获取当前用户启用中的设备列表。
     */
    @Operation(summary = "当前用户设备列表")
    @GetMapping
    public ResponseDTO<List<AppDeviceDTO>> list() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(appDeviceApplicationService.getCurrentUserDevices(currentUserId));
    }

    /**
     * 停用指定设备。
     */
    @Operation(summary = "停用当前设备")
    @DeleteMapping("/{deviceCode}")
    public ResponseDTO<Void> unregister(@PathVariable String deviceCode) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        appDeviceApplicationService.unregisterDevice(deviceCode, currentUserId);
        return ResponseDTO.ok();
    }
}
