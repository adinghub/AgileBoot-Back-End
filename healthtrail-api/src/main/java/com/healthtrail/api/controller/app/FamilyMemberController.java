package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.family.FamilyMemberApplicationService;
import com.healthtrail.domain.health.family.command.AddFamilyMemberCommand;
import com.healthtrail.domain.health.family.command.CreateFamilyShareInviteCommand;
import com.healthtrail.domain.health.family.command.UpdateFamilyMemberCommand;
import com.healthtrail.domain.health.family.dto.FamilyMemberDTO;
import com.healthtrail.domain.health.family.dto.FamilyMemberShareDTO;
import com.healthtrail.domain.health.family.dto.FamilyShareInviteDTO;
import com.healthtrail.domain.health.family.query.FamilyMemberQuery;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端家庭成员控制器。
 *
 * <p>这里的所有接口都默认操作“当前登录用户自己的家庭成员”，
 * 因此每次请求都会从认证上下文中读取 App 用户ID，并注入到查询或命令流程中。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/family/members")
@Tag(name = "家庭成员API", description = "App端家庭成员管理接口")
public class FamilyMemberController extends BaseController {

    private final FamilyMemberApplicationService familyMemberApplicationService;

    /**
     * 获取当前登录用户的家庭成员列表。
     *
     * @param query 查询参数
     * @return 家庭成员列表
     */
    @Operation(summary = "家庭成员列表")
    @GetMapping
    public ResponseDTO<List<FamilyMemberDTO>> list(FamilyMemberQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(familyMemberApplicationService.getFamilyMemberList(query));
    }

    /**
     * 获取家庭成员详情。
     *
     * @param memberId 家庭成员ID
     * @return 家庭成员详情
     */
    @Operation(summary = "家庭成员详情")
    @GetMapping("/{memberId}")
    public ResponseDTO<FamilyMemberDTO> getInfo(@PathVariable Long memberId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(familyMemberApplicationService.getFamilyMemberInfo(memberId, currentUserId));
    }

    /**
     * 新增家庭成员。
     *
     * @param addCommand 新增命令
     * @return 新增结果
     */
    @Operation(summary = "新增家庭成员")
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddFamilyMemberCommand addCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        familyMemberApplicationService.addFamilyMember(addCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 修改家庭成员。
     *
     * @param memberId 家庭成员ID
     * @param updateCommand 修改命令
     * @return 修改结果
     */
    @Operation(summary = "修改家庭成员")
    @PutMapping("/{memberId}")
    public ResponseDTO<Void> edit(@PathVariable Long memberId, @Valid @RequestBody UpdateFamilyMemberCommand updateCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        updateCommand.setMemberId(memberId);
        familyMemberApplicationService.updateFamilyMember(updateCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 删除家庭成员。
     *
     * @param memberId 家庭成员ID
     * @return 删除结果
     */
    @Operation(summary = "删除家庭成员")
    @DeleteMapping("/{memberId}")
    public ResponseDTO<Void> remove(@PathVariable Long memberId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        familyMemberApplicationService.removeFamilyMember(memberId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 查询指定成员的协同账号列表。
     *
     * @param memberId 家庭成员ID
     * @return 协同账号列表
     */
    @Operation(summary = "家庭成员协同账号列表")
    @GetMapping("/{memberId}/collaborators")
    public ResponseDTO<List<FamilyMemberShareDTO>> collaborators(@PathVariable Long memberId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(familyMemberApplicationService.getCollaboratorList(memberId, currentUserId));
    }

    /**
     * 查询指定成员的共享邀请列表。
     *
     * @param memberId 家庭成员ID
     * @return 邀请列表
     */
    @Operation(summary = "家庭成员共享邀请列表")
    @GetMapping("/{memberId}/share-invites")
    public ResponseDTO<List<FamilyShareInviteDTO>> inviteList(@PathVariable Long memberId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(familyMemberApplicationService.getShareInviteList(memberId, currentUserId));
    }

    /**
     * 为指定成员创建共享邀请。
     *
     * @param memberId 家庭成员ID
     * @param command 创建邀请命令
     * @return 邀请详情
     */
    @Operation(summary = "创建家庭成员共享邀请")
    @PostMapping("/{memberId}/share-invites")
    public ResponseDTO<FamilyShareInviteDTO> createInvite(@PathVariable Long memberId,
        @Valid @RequestBody CreateFamilyShareInviteCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(familyMemberApplicationService.createShareInvite(memberId, command, currentUserId));
    }

    /**
     * 接受家庭共享邀请。
     *
     * @param inviteCode 邀请码
     * @return 接受结果
     */
    @Operation(summary = "接受家庭共享邀请")
    @PostMapping("/share-invites/{inviteCode}/accept")
    public ResponseDTO<FamilyShareInviteDTO> acceptInvite(@PathVariable String inviteCode) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(familyMemberApplicationService.acceptShareInvite(inviteCode, currentUserId));
    }

    /**
     * 取消尚未接受的共享邀请。
     *
     * @param memberId 家庭成员ID
     * @param inviteId 邀请ID
     * @return 取消结果
     */
    @Operation(summary = "取消家庭共享邀请")
    @DeleteMapping("/{memberId}/share-invites/{inviteId}")
    public ResponseDTO<Void> cancelInvite(@PathVariable Long memberId, @PathVariable Long inviteId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        familyMemberApplicationService.cancelShareInvite(memberId, inviteId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 移除协同账号。
     *
     * @param memberId 家庭成员ID
     * @param shareId 共享关系ID
     * @return 移除结果
     */
    @Operation(summary = "移除家庭成员协同账号")
    @DeleteMapping("/{memberId}/collaborators/{shareId}")
    public ResponseDTO<Void> removeCollaborator(@PathVariable Long memberId, @PathVariable Long shareId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        familyMemberApplicationService.removeCollaborator(memberId, shareId, currentUserId);
        return ResponseDTO.ok();
    }
}
