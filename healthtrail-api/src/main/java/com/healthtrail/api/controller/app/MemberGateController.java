package com.healthtrail.api.controller.app;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.system.member.MemberGateApplicationService;
import com.healthtrail.domain.system.member.dto.AppMemberGateAccessDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App端会员门禁控制器，提供当前用户功能门禁检查结果。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/member-gates")
@Tag(name = "App会员门禁API", description = "App端当前用户门禁结果接口")
public class MemberGateController extends BaseController {

    private final MemberGateApplicationService memberGateApplicationService;

    @Operation(summary = "当前用户门禁快照")
    @GetMapping("/current")
    public ResponseDTO<List<AppMemberGateAccessDTO>> current(
        @RequestParam(required = false) String gateCodes) {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        List<String> requestedGateCodes = parseGateCodes(gateCodes);
        return ResponseDTO.ok(memberGateApplicationService.checkCurrentUserGates(userId, requestedGateCodes));
    }

    private List<String> parseGateCodes(String gateCodes) {
        if (StrUtil.isBlank(gateCodes)) {
            return Collections.emptyList();
        }
        return Arrays.stream(gateCodes.split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toList());
    }
}
