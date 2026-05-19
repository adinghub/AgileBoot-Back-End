package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberApplicationService;
import com.healthtrail.domain.system.member.command.AssignUserMemberCommand;
import com.healthtrail.domain.system.member.command.ClearUserMemberCommand;
import com.healthtrail.domain.system.member.dto.UserMemberDTO;
import com.healthtrail.domain.system.member.query.UserMemberQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户会员管理接口，提供用户会员关系查询、赠送和清空能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/user-members")
@Tag(name = "后台用户会员API", description = "后台用户会员管理接口")
public class SysUserMemberController extends BaseController {

    private final MemberApplicationService memberApplicationService;

    @Operation(summary = "用户会员列表")
    @PreAuthorize("@permission.has('system:userMember:list')")
    @GetMapping
    public ResponseDTO<PageDTO<UserMemberDTO>> list(UserMemberQuery query) {
        return ResponseDTO.ok(memberApplicationService.getUserMemberPage(query));
    }

    @Operation(summary = "用户会员详情")
    @PreAuthorize("@permission.has('system:userMember:query')")
    @GetMapping("/{userId}")
    public ResponseDTO<UserMemberDTO> getInfo(@PathVariable Long userId) {
        return ResponseDTO.ok(memberApplicationService.getUserMemberInfo(userId));
    }

    @Operation(summary = "后台赠送会员")
    @PreAuthorize("@permission.has('system:userMember:grant')")
    @AccessLog(title = "用户会员", businessType = BusinessTypeEnum.ADD)
    @PostMapping("/grant")
    public ResponseDTO<Void> grant(@Valid @RequestBody AssignUserMemberCommand command) {
        memberApplicationService.assignUserMember(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "清空用户会员")
    @PreAuthorize("@permission.has('system:userMember:clear')")
    @AccessLog(title = "用户会员", businessType = BusinessTypeEnum.DELETE)
    @PostMapping("/clear")
    public ResponseDTO<Void> clear(@Valid @RequestBody ClearUserMemberCommand command) {
        memberApplicationService.clearUserMember(command);
        return ResponseDTO.ok();
    }
}
