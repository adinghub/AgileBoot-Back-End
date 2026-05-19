package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberApplicationService;
import com.healthtrail.domain.system.member.command.AddMemberLevelCommand;
import com.healthtrail.domain.system.member.command.UpdateMemberLevelCommand;
import com.healthtrail.domain.system.member.dto.MemberLevelDTO;
import com.healthtrail.domain.system.member.query.MemberLevelQuery;
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
 * 后台会员等级管理接口，提供会员等级的增删改查能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/member-levels")
@Tag(name = "后台会员等级API", description = "后台会员等级管理接口")
public class SysMemberLevelController extends BaseController {

    private final MemberApplicationService memberApplicationService;

    @Operation(summary = "会员等级列表")
    @PreAuthorize("@permission.has('system:memberLevel:list')")
    @GetMapping
    public ResponseDTO<PageDTO<MemberLevelDTO>> list(MemberLevelQuery query) {
        return ResponseDTO.ok(memberApplicationService.getMemberLevelPage(query));
    }

    @Operation(summary = "启用中的会员等级列表")
    @PreAuthorize("@permission.has('system:memberLevel:list')")
    @GetMapping("/enabled")
    public ResponseDTO<List<MemberLevelDTO>> enabledList() {
        return ResponseDTO.ok(memberApplicationService.getEnabledMemberLevels());
    }

    @Operation(summary = "会员等级详情")
    @PreAuthorize("@permission.has('system:memberLevel:query')")
    @GetMapping("/{memberLevelId}")
    public ResponseDTO<MemberLevelDTO> getInfo(@PathVariable Long memberLevelId) {
        return ResponseDTO.ok(memberApplicationService.getMemberLevelInfo(memberLevelId));
    }

    @Operation(summary = "新增会员等级")
    @PreAuthorize("@permission.has('system:memberLevel:add')")
    @AccessLog(title = "会员等级", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddMemberLevelCommand command) {
        memberApplicationService.addMemberLevel(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改会员等级")
    @PreAuthorize("@permission.has('system:memberLevel:edit')")
    @AccessLog(title = "会员等级", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{memberLevelId}")
    public ResponseDTO<Void> edit(@PathVariable Long memberLevelId,
        @Valid @RequestBody UpdateMemberLevelCommand command) {
        command.setMemberLevelId(memberLevelId);
        memberApplicationService.updateMemberLevel(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除会员等级")
    @PreAuthorize("@permission.has('system:memberLevel:remove')")
    @AccessLog(title = "会员等级", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{memberLevelIds}")
    public ResponseDTO<Void> remove(@PathVariable String memberLevelIds) {
        List<Long> ids = Arrays.stream(memberLevelIds.split(","))
            .map(String::trim).filter(value -> !value.isEmpty()).map(Long::valueOf).collect(Collectors.toList());
        memberApplicationService.removeMemberLevels(ids);
        return ResponseDTO.ok();
    }
}
