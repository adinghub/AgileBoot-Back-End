package com.healthtrail.domain.health.family;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.health.FamilyMemberAccessRoleEnum;
import com.healthtrail.common.enums.health.FamilyMemberAccessSourceEnum;
import com.healthtrail.common.enums.health.FamilyMemberShareStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberShareEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberShareService;
import com.healthtrail.domain.health.family.dto.FamilyMemberAccessContextDTO;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 家庭成员访问控制服务。
 *
 * <p>随着“家庭共享/邀请”落地，原先简单的“owner_user_id == 当前用户”判断已经不够用了。
 * 这个服务把访问控制统一收口，供家庭成员、用药、报告等多个业务模块复用。
 */
@Service
@RequiredArgsConstructor
public class FamilyMemberAccessService {

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 家庭成员共享关系数据库服务 */
    private final HealthFamilyMemberShareService familyMemberShareService;

    /**
     * 获取当前账号可访问的全部成员ID。
     *
     * @param currentUserId 当前账号ID
     * @return 成员ID集合
     */
    public Set<Long> getAccessibleMemberIds(Long currentUserId) {
        if (currentUserId == null) {
            return Collections.emptySet();
        }

        Set<Long> memberIds = new LinkedHashSet<>();
        memberIds.addAll(familyMemberService.lambdaQuery()
            .eq(HealthFamilyMemberEntity::getOwnerUserId, currentUserId)
            .list()
            .stream()
            .map(HealthFamilyMemberEntity::getMemberId)
            .collect(Collectors.toList()));

        memberIds.addAll(familyMemberShareService.lambdaQuery()
            .eq(HealthFamilyMemberShareEntity::getCollaboratorUserId, currentUserId)
            .eq(HealthFamilyMemberShareEntity::getShareStatus, FamilyMemberShareStatusEnum.ENABLED.getValue())
            .list()
            .stream()
            .map(HealthFamilyMemberShareEntity::getMemberId)
            .collect(Collectors.toList()));
        return memberIds;
    }

    /**
     * 批量获取当前账号对一组成员的访问上下文。
     *
     * @param memberIds 成员ID集合
     * @param currentUserId 当前账号ID
     * @return 成员ID到访问上下文的映射
     */
    public Map<Long, FamilyMemberAccessContextDTO> getAccessContextMap(Set<Long> memberIds, Long currentUserId) {
        if (memberIds == null || memberIds.isEmpty() || currentUserId == null) {
            return Collections.emptyMap();
        }

        Map<Long, FamilyMemberAccessContextDTO> accessContextMap = new LinkedHashMap<>();
        List<HealthFamilyMemberEntity> ownedMembers = familyMemberService.lambdaQuery()
            .eq(HealthFamilyMemberEntity::getOwnerUserId, currentUserId)
            .in(HealthFamilyMemberEntity::getMemberId, memberIds)
            .list();
        for (HealthFamilyMemberEntity ownedMember : ownedMembers) {
            accessContextMap.put(ownedMember.getMemberId(), FamilyMemberAccessContextDTO.builder()
                .memberId(ownedMember.getMemberId())
                .ownerUserId(ownedMember.getOwnerUserId())
                .currentUserId(currentUserId)
                .accessSource(FamilyMemberAccessSourceEnum.OWNER.getValue())
                .accessSourceName(HealthAppI18n.familyAccessSourceName(FamilyMemberAccessSourceEnum.OWNER.getValue()))
                .accessRole(FamilyMemberAccessRoleEnum.OWNER.getValue())
                .accessRoleName(HealthAppI18n.familyAccessRoleName(FamilyMemberAccessRoleEnum.OWNER.getValue()))
                .owner(true)
                .canEdit(true)
                .build());
        }

        List<HealthFamilyMemberShareEntity> shareEntities = familyMemberShareService.lambdaQuery()
            .eq(HealthFamilyMemberShareEntity::getCollaboratorUserId, currentUserId)
            .eq(HealthFamilyMemberShareEntity::getShareStatus, FamilyMemberShareStatusEnum.ENABLED.getValue())
            .in(HealthFamilyMemberShareEntity::getMemberId, memberIds)
            .list();
        for (HealthFamilyMemberShareEntity shareEntity : shareEntities) {
            FamilyMemberAccessRoleEnum roleEnum = resolveRole(shareEntity.getShareRole());
            if (roleEnum == null) {
                continue;
            }
            accessContextMap.putIfAbsent(shareEntity.getMemberId(), FamilyMemberAccessContextDTO.builder()
                .memberId(shareEntity.getMemberId())
                .ownerUserId(shareEntity.getOwnerUserId())
                .currentUserId(currentUserId)
                .accessSource(FamilyMemberAccessSourceEnum.SHARED.getValue())
                .accessSourceName(HealthAppI18n.familyAccessSourceName(FamilyMemberAccessSourceEnum.SHARED.getValue()))
                .accessRole(roleEnum.getValue())
                .accessRoleName(HealthAppI18n.familyAccessRoleName(roleEnum.getValue()))
                .owner(false)
                .canEdit(FamilyMemberAccessRoleEnum.EDITOR.getValue().equals(roleEnum.getValue()))
                .shareId(shareEntity.getShareId())
                .build());
        }
        return accessContextMap;
    }

    /**
     * 获取单个成员的访问上下文。
     *
     * @param memberId 成员ID
     * @param currentUserId 当前账号ID
     * @return 访问上下文
     */
    public FamilyMemberAccessContextDTO getRequiredAccessContext(Long memberId, Long currentUserId) {
        if (memberId == null || currentUserId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberId, "家庭成员");
        }
        Map<Long, FamilyMemberAccessContextDTO> accessContextMap =
            getAccessContextMap(Collections.singleton(memberId), currentUserId);
        FamilyMemberAccessContextDTO accessContext = accessContextMap.get(memberId);
        if (accessContext == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberId, "家庭成员");
        }
        return accessContext;
    }

    /**
     * 校验当前账号是否可编辑指定成员。
     *
     * @param memberId 成员ID
     * @param currentUserId 当前账号ID
     * @return 访问上下文，便于调用方继续复用
     */
    public FamilyMemberAccessContextDTO checkCanEditMember(Long memberId, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext = getRequiredAccessContext(memberId, currentUserId);
        if (!accessContext.isCanEdit()) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_MEMBER_NO_EDIT_PERMISSION);
        }
        return accessContext;
    }

    /**
     * 校验当前账号是否是成员主账号。
     * 共享邀请、移除协同账号等高风险动作只允许主账号执行。
     *
     * @param memberId 成员ID
     * @param currentUserId 当前账号ID
     * @return 访问上下文
     */
    public FamilyMemberAccessContextDTO checkOwner(Long memberId, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext = getRequiredAccessContext(memberId, currentUserId);
        if (!accessContext.isOwner()) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_MEMBER_NO_EDIT_PERMISSION);
        }
        return accessContext;
    }

    /**
     * 判断当前账号是否已经是某个成员的主账号。
     *
     * @param memberId 成员ID
     * @param currentUserId 当前账号ID
     * @return true 表示当前账号就是主账号
     */
    public boolean isOwner(Long memberId, Long currentUserId) {
        return familyMemberService.isOwnedByUser(memberId, currentUserId);
    }

    /**
     * 判断共享角色是否具备编辑能力。
     *
     * @param shareRole 共享角色
     * @return true 表示可编辑
     */
    public boolean canEdit(String shareRole) {
        return Objects.equals(FamilyMemberAccessRoleEnum.OWNER.getValue(), shareRole)
            || Objects.equals(FamilyMemberAccessRoleEnum.EDITOR.getValue(), shareRole);
    }

    /**
     * 解析共享角色枚举。
     */
    public FamilyMemberAccessRoleEnum resolveRole(String shareRole) {
        if (StrUtil.isBlank(shareRole)) {
            return null;
        }
        for (FamilyMemberAccessRoleEnum item : FamilyMemberAccessRoleEnum.values()) {
            if (item.getValue().equalsIgnoreCase(shareRole.trim())) {
                return item;
            }
        }
        return null;
    }
}
