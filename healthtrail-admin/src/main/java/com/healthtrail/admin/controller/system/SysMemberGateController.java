package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberGateApplicationService;
import com.healthtrail.domain.system.member.command.AddMemberGateCommand;
import com.healthtrail.domain.system.member.command.UpdateMemberGateCommand;
import com.healthtrail.domain.system.member.dto.MemberGateDTO;
import com.healthtrail.domain.system.member.query.MemberGateQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台会员门禁点管理接口，提供功能门禁点的增删改查能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/member-gates")
@Tag(name = "后台会员门禁点API", description = "后台会员功能门禁点管理接口")
public class SysMemberGateController extends BaseController {

    private final MemberGateApplicationService memberGateApplicationService;

    @Operation(summary = "会员门禁点列表")
    @PreAuthorize("@permission.has('system:memberGate:list')")
    @GetMapping
    public ResponseDTO<PageDTO<MemberGateDTO>> list(MemberGateQuery query) {
        return ResponseDTO.ok(memberGateApplicationService.getMemberGatePage(query));
    }

    @Operation(summary = "会员门禁点详情")
    @PreAuthorize("@permission.has('system:memberGate:query')")
    @GetMapping("/{memberGateId}")
    public ResponseDTO<MemberGateDTO> getInfo(@PathVariable Long memberGateId) {
        return ResponseDTO.ok(memberGateApplicationService.getMemberGateInfo(memberGateId));
    }

    @Operation(summary = "新增会员门禁点")
    @PreAuthorize("@permission.has('system:memberGate:add')")
    @AccessLog(title = "会员门禁点", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddMemberGateCommand command) {
        memberGateApplicationService.addMemberGate(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改会员门禁点")
    @PreAuthorize("@permission.has('system:memberGate:edit')")
    @AccessLog(title = "会员门禁点", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{memberGateId}")
    public ResponseDTO<Void> edit(@PathVariable Long memberGateId, @Valid @RequestBody UpdateMemberGateCommand command) {
        command.setMemberGateId(memberGateId);
        memberGateApplicationService.updateMemberGate(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除会员门禁点")
    @PreAuthorize("@permission.has('system:memberGate:remove')")
    @AccessLog(title = "会员门禁点", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{memberGateIds}")
    public ResponseDTO<Void> remove(@PathVariable String memberGateIds) {
        List<Long> ids = Arrays.stream(memberGateIds.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(Long::valueOf)
            .collect(Collectors.toList());
        memberGateApplicationService.removeMemberGates(ids);
        return ResponseDTO.ok();
    }
}
