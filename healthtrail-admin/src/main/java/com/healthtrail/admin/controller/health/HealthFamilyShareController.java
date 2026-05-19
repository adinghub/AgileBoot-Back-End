package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.common.enums.health.FamilyMemberShareStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.family.FamilyMemberApplicationService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberShareService;
import com.healthtrail.domain.health.family.db.HealthFamilyShareInviteService;
import com.healthtrail.domain.health.family.dto.FamilyMemberShareDTO;
import com.healthtrail.domain.health.family.dto.FamilyShareInviteDTO;
import com.healthtrail.domain.health.family.dto.HealthFamilyMemberAdminDTO;
import com.healthtrail.domain.health.family.query.HealthFamilyMemberAdminQuery;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台家庭共享管理控制器。
 *
 * <p>后台这里不直接维护某个“共享关系主表”，而是围绕家庭成员做运维视角管理：
 * 1. 先分页定位成员
 * 2. 再查看该成员下的协同账号与邀请码
 * 3. 必要时执行移除协同 / 取消邀请
 *
 * <p>这样更贴合客服排障场景，因为实际业务沟通通常先从“哪位家庭成员的数据有问题”开始。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/family/members")
@Tag(name = "后台家庭共享API", description = "后台家庭共享与协同管理接口")
public class HealthFamilyShareController extends BaseController {

    private final HealthFamilyMemberService familyMemberService;
    private final HealthFamilyMemberShareService familyMemberShareService;
    private final HealthFamilyShareInviteService familyShareInviteService;
    private final HealthAppUserService appUserService;
    private final FamilyMemberApplicationService familyMemberApplicationService;

    /**
     * 分页查询家庭成员管理列表。
     */
    @Operation(summary = "后台家庭共享成员列表")
    @PreAuthorize("@permission.has('health:familyShare:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthFamilyMemberAdminDTO>> list(HealthFamilyMemberAdminQuery query) {
        Page<HealthFamilyMemberEntity> page = familyMemberService.page(query.toPage(), query.addQueryCondition());
        List<HealthFamilyMemberAdminDTO> records = page.getRecords().stream()
            .map(this::buildAdminMemberDTO)
            .collect(Collectors.toList());
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    /**
     * 查询某个成员当前的协同账号列表。
     *
     * <p>这里直接复用 App 端已经跑通的协同账号查询逻辑，
     * 只是在后台场景下自动代入该成员的主账号 ID，避免重复造轮子。
     */
    @Operation(summary = "后台家庭成员协同账号列表")
    @PreAuthorize("@permission.has('health:familyShare:query')")
    @GetMapping("/{memberId}/collaborators")
    public ResponseDTO<List<FamilyMemberShareDTO>> collaborators(@PathVariable Long memberId) {
        HealthFamilyMemberEntity memberEntity = getRequiredMember(memberId);
        return ResponseDTO.ok(
            familyMemberApplicationService.getCollaboratorList(memberId, memberEntity.getOwnerUserId())
        );
    }

    /**
     * 查询某个成员当前的邀请码记录。
     */
    @Operation(summary = "后台家庭成员共享邀请列表")
    @PreAuthorize("@permission.has('health:familyShare:query')")
    @GetMapping("/{memberId}/share-invites")
    public ResponseDTO<List<FamilyShareInviteDTO>> inviteList(@PathVariable Long memberId) {
        HealthFamilyMemberEntity memberEntity = getRequiredMember(memberId);
        return ResponseDTO.ok(
            familyMemberApplicationService.getShareInviteList(memberId, memberEntity.getOwnerUserId())
        );
    }

    /**
     * 后台移除协同账号。
     */
    @Operation(summary = "后台移除家庭成员协同账号")
    @PreAuthorize("@permission.has('health:familyShare:remove')")
    @AccessLog(title = "家庭共享协同", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{memberId}/collaborators/{shareId}")
    public ResponseDTO<Void> removeCollaborator(@PathVariable Long memberId, @PathVariable Long shareId) {
        HealthFamilyMemberEntity memberEntity = getRequiredMember(memberId);
        familyMemberApplicationService.removeCollaborator(memberId, shareId, memberEntity.getOwnerUserId());
        return ResponseDTO.ok();
    }

    /**
     * 后台取消共享邀请。
     */
    @Operation(summary = "后台取消家庭共享邀请")
    @PreAuthorize("@permission.has('health:familyShare:remove')")
    @AccessLog(title = "家庭共享邀请", businessType = BusinessTypeEnum.MODIFY)
    @DeleteMapping("/{memberId}/share-invites/{inviteId}")
    public ResponseDTO<Void> cancelInvite(@PathVariable Long memberId, @PathVariable Long inviteId) {
        HealthFamilyMemberEntity memberEntity = getRequiredMember(memberId);
        familyMemberApplicationService.cancelShareInvite(memberId, inviteId, memberEntity.getOwnerUserId());
        return ResponseDTO.ok();
    }

    /**
     * 组装后台成员列表 DTO。
     *
     * <p>这里会额外回填：
     * 1. 主账号昵称 / 手机号
     * 2. 当前启用中的协同人数
     * 3. 当前待接受的邀请码数量
     */
    private HealthFamilyMemberAdminDTO buildAdminMemberDTO(HealthFamilyMemberEntity entity) {
        HealthFamilyMemberAdminDTO dto = new HealthFamilyMemberAdminDTO(entity);

        HealthAppUserEntity ownerUser = entity.getOwnerUserId() == null ? null : appUserService.getById(entity.getOwnerUserId());
        if (ownerUser != null) {
            dto.setOwnerMobile(ownerUser.getMobile());
            dto.setOwnerNickname(ownerUser.getNickname());
        }

        dto.setActiveCollaboratorCount(familyMemberShareService.lambdaQuery()
            .eq(com.healthtrail.domain.health.family.db.HealthFamilyMemberShareEntity::getMemberId, entity.getMemberId())
            .eq(com.healthtrail.domain.health.family.db.HealthFamilyMemberShareEntity::getShareStatus,
                FamilyMemberShareStatusEnum.ENABLED.getValue())
            .count().intValue());
        dto.setPendingInviteCount(familyShareInviteService.lambdaQuery()
            .eq(com.healthtrail.domain.health.family.db.HealthFamilyShareInviteEntity::getMemberId, entity.getMemberId())
            .eq(com.healthtrail.domain.health.family.db.HealthFamilyShareInviteEntity::getInviteStatus, 0)
            .count().intValue());
        return dto;
    }

    private HealthFamilyMemberEntity getRequiredMember(Long memberId) {
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        if (memberEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberId, "家庭成员");
        }
        return memberEntity;
    }
}
