package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.system.member.MemberEntitlementApplicationService;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App端会员权益控制器，提供当前用户权益快照查询能力。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/member-entitlements")
@Tag(name = "App会员权益API", description = "App端会员权益快照接口")
public class MemberEntitlementController extends BaseController {

    private final MemberEntitlementApplicationService memberEntitlementApplicationService;

    @Operation(summary = "当前权益快照")
    @GetMapping("/current")
    public ResponseDTO<AppMemberEntitlementDTO> current() {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberEntitlementApplicationService.getCurrentEntitlements(userId));
    }
}
