package com.healthtrail.domain.health.family;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.health.FamilyMemberAccessRoleEnum;
import com.healthtrail.common.enums.health.FamilyMemberShareStatusEnum;
import com.healthtrail.common.enums.health.FamilyShareInviteStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.family.command.AddFamilyMemberCommand;
import com.healthtrail.domain.health.family.command.CreateFamilyShareInviteCommand;
import com.healthtrail.domain.health.family.command.UpdateFamilyMemberCommand;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberShareEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberShareService;
import com.healthtrail.domain.health.family.db.HealthFamilyShareInviteEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyShareInviteService;
import com.healthtrail.domain.health.family.dto.FamilyMemberAccessContextDTO;
import com.healthtrail.domain.health.family.dto.FamilyMemberDTO;
import com.healthtrail.domain.health.family.dto.FamilyMemberShareDTO;
import com.healthtrail.domain.health.family.dto.FamilyShareInviteDTO;
import com.healthtrail.domain.health.family.model.FamilyMemberModel;
import com.healthtrail.domain.health.family.model.FamilyMemberModelFactory;
import com.healthtrail.domain.health.family.query.FamilyMemberQuery;
import com.healthtrail.domain.health.support.HealthBizCodeFormatter;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 家庭成员应用服务。
 *
 * <p>该服务负责组织家庭成员相关的核心业务流程，
 * 包括列表查询、详情查询以及增删改操作。
 */
@Service
@RequiredArgsConstructor
public class FamilyMemberApplicationService {

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 家庭成员领域模型工厂 */
    private final FamilyMemberModelFactory familyMemberModelFactory;

    /** 家庭成员共享关系数据库服务 */
    private final HealthFamilyMemberShareService familyMemberShareService;

    /** 家庭共享邀请数据库服务 */
    private final HealthFamilyShareInviteService familyShareInviteService;

    /** App用户数据库服务 */
    private final HealthAppUserService appUserService;

    /** 家庭成员访问控制服务 */
    private final FamilyMemberAccessService familyMemberAccessService;

    /**
     * 获取当前用户的家庭成员列表。
     *
     * @param query 家庭成员查询条件
     * @return 家庭成员列表
     */
    public List<FamilyMemberDTO> getFamilyMemberList(FamilyMemberQuery query) {
        Set<Long> accessibleMemberIds = new LinkedHashSet<>(
            familyMemberAccessService.getAccessibleMemberIds(query.getOwnerUserId()));
        if (accessibleMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<HealthFamilyMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("member_id", accessibleMemberIds)
            .like(StrUtil.isNotBlank(query.getMemberName()), "member_name", query.getMemberName())
            .eq(query.getStatus() != null, "status", query.getStatus())
            .orderByAsc("owner_user_id")
            .orderByAsc("member_name")
            .orderByAsc("member_id");

        List<HealthFamilyMemberEntity> list = familyMemberService.list(queryWrapper);
        Map<Long, FamilyMemberAccessContextDTO> accessContextMap =
            familyMemberAccessService.getAccessContextMap(accessibleMemberIds, query.getOwnerUserId());
        return list.stream()
            .map(entity -> buildFamilyMemberDTO(entity, accessContextMap.get(entity.getMemberId())))
            .collect(Collectors.toList());
    }

    /**
     * 获取家庭成员详情。
     *
     * @param memberId 家庭成员ID
     * @param ownerUserId 当前登录 App 用户ID
     * @return 家庭成员详情
     */
    public FamilyMemberDTO getFamilyMemberInfo(Long memberId, Long ownerUserId) {
        FamilyMemberModel familyMemberModel = familyMemberModelFactory.loadById(memberId);
        FamilyMemberAccessContextDTO accessContext = familyMemberAccessService.getRequiredAccessContext(memberId, ownerUserId);
        return buildFamilyMemberDTO(familyMemberModel, accessContext);
    }

    /**
     * 新增家庭成员。
     *
     * @param addCommand 新增命令
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void addFamilyMember(AddFamilyMemberCommand addCommand, Long ownerUserId) {
        FamilyMemberModel familyMemberModel = familyMemberModelFactory.create();
        familyMemberModel.loadAddCommand(addCommand, ownerUserId);
        familyMemberModel.checkFields();
        familyMemberModel.insert();
        ensureMemberCode(familyMemberModel);
    }

    /**
     * 修改家庭成员。
     *
     * @param updateCommand 修改命令
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void updateFamilyMember(UpdateFamilyMemberCommand updateCommand, Long ownerUserId) {
        FamilyMemberModel familyMemberModel = familyMemberModelFactory.loadById(updateCommand.getMemberId());
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(updateCommand.getMemberId(), ownerUserId);
        familyMemberModel.loadUpdateCommand(updateCommand, accessContext.getOwnerUserId());
        familyMemberModel.checkFields();
        ensureMemberCode(familyMemberModel);
        familyMemberModel.updateById();
    }

    /**
     * 删除家庭成员。
     *
     * @param memberId 家庭成员ID
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void removeFamilyMember(Long memberId, Long ownerUserId) {
        familyMemberAccessService.checkOwner(memberId, ownerUserId);
        FamilyMemberModel familyMemberModel = familyMemberModelFactory.loadById(memberId);
        familyMemberModel.deleteById();
    }

    /**
     * 获取成员协同账号列表。
     *
     * @param memberId 家庭成员ID
     * @param currentUserId 当前账号ID
     * @return 协同账号列表
     */
    public List<FamilyMemberShareDTO> getCollaboratorList(Long memberId, Long currentUserId) {
        familyMemberAccessService.checkOwner(memberId, currentUserId);

        List<HealthFamilyMemberShareEntity> shareEntities = familyMemberShareService.lambdaQuery()
            .eq(HealthFamilyMemberShareEntity::getMemberId, memberId)
            .orderByDesc(HealthFamilyMemberShareEntity::getAcceptedTime)
            .orderByDesc(HealthFamilyMemberShareEntity::getShareId)
            .list();
        // 协同账号列表会一次性展示多个协作者。
        // 如果每条分享记录都单独 `getById(collaboratorUserId)`，协作者越多 SQL 次数越多。
        // 这里先批量加载本页涉及的 App 用户，再按 userId 回填展示字段，避免列表内 N+1 查询。
        Map<Long, HealthAppUserEntity> appUserMap = loadAppUserMap(shareEntities.stream()
            .map(HealthFamilyMemberShareEntity::getCollaboratorUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));

        return shareEntities.stream().map(entity -> {
            FamilyMemberShareDTO dto = new FamilyMemberShareDTO(entity);
            HealthAppUserEntity appUserEntity = appUserMap.get(entity.getCollaboratorUserId());
            if (appUserEntity != null) {
                dto.setCollaboratorMobile(appUserEntity.getMobile());
                dto.setCollaboratorNickname(appUserEntity.getNickname());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取成员共享邀请列表。
     *
     * @param memberId 家庭成员ID
     * @param currentUserId 当前账号ID
     * @return 邀请列表
     */
    public List<FamilyShareInviteDTO> getShareInviteList(Long memberId, Long currentUserId) {
        familyMemberAccessService.checkOwner(memberId, currentUserId);

        List<HealthFamilyShareInviteEntity> inviteEntities = familyShareInviteService.lambdaQuery()
            .eq(HealthFamilyShareInviteEntity::getMemberId, memberId)
            .orderByDesc(HealthFamilyShareInviteEntity::getInviteId)
            .list();
        // 邀请列表同样可能包含多个已绑定账号的 invitee。
        // 批量查用户后再组装 DTO，可以避免每条邀请记录都触发一次 App 用户表查询。
        Map<Long, HealthAppUserEntity> appUserMap = loadAppUserMap(inviteEntities.stream()
            .map(HealthFamilyShareInviteEntity::getInviteeUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));

        return inviteEntities.stream().map(entity -> {
            FamilyShareInviteDTO dto = new FamilyShareInviteDTO(entity);
            HealthAppUserEntity appUserEntity = entity.getInviteeUserId() == null
                ? null
                : appUserMap.get(entity.getInviteeUserId());
            if (appUserEntity != null) {
                dto.setInviteeMobile(appUserEntity.getMobile());
                dto.setInviteeNickname(appUserEntity.getNickname());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 创建共享邀请。
     *
     * @param memberId 家庭成员ID
     * @param command 创建邀请命令
     * @param currentUserId 当前账号ID
     * @return 新创建的邀请码信息
     */
    public FamilyShareInviteDTO createShareInvite(Long memberId, CreateFamilyShareInviteCommand command, Long currentUserId) {
        FamilyMemberAccessRoleEnum roleEnum = familyMemberAccessService.resolveRole(command.getShareRole());
        if (roleEnum == null || FamilyMemberAccessRoleEnum.OWNER == roleEnum) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_SHARE_ROLE_INVALID);
        }

        FamilyMemberAccessContextDTO accessContext = familyMemberAccessService.checkOwner(memberId, currentUserId);
        HealthFamilyShareInviteEntity inviteEntity = new HealthFamilyShareInviteEntity();
        inviteEntity.setMemberId(memberId);
        inviteEntity.setOwnerUserId(accessContext.getOwnerUserId());
        inviteEntity.setInviteCode(generateInviteCode());
        inviteEntity.setShareRole(roleEnum.getValue());
        inviteEntity.setInviteStatus(FamilyShareInviteStatusEnum.PENDING.getValue());
        inviteEntity.setExpireTime(DateUtil.offsetHour(new Date(), command.getExpireHours() == null ? 72 : command.getExpireHours()));
        inviteEntity.setRemark(command.getRemark());
        familyShareInviteService.save(inviteEntity);
        return new FamilyShareInviteDTO(inviteEntity);
    }

    /**
     * 接受共享邀请。
     *
     * @param inviteCode 邀请码
     * @param currentUserId 当前账号ID
     * @return 接受后的邀请信息
     */
    public FamilyShareInviteDTO acceptShareInvite(String inviteCode, Long currentUserId) {
        HealthFamilyShareInviteEntity inviteEntity = familyShareInviteService.getByInviteCode(inviteCode);
        if (inviteEntity == null) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_SHARE_INVITE_NOT_FOUND);
        }
        if (!FamilyShareInviteStatusEnum.PENDING.getValue().equals(inviteEntity.getInviteStatus())) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_SHARE_INVITE_STATUS_INVALID);
        }
        if (inviteEntity.getExpireTime() != null && inviteEntity.getExpireTime().before(new Date())) {
            inviteEntity.setInviteStatus(FamilyShareInviteStatusEnum.EXPIRED.getValue());
            familyShareInviteService.updateById(inviteEntity);
            throw new ApiException(ErrorCode.Business.APP_FAMILY_SHARE_INVITE_EXPIRED);
        }
        if (Objects.equals(inviteEntity.getOwnerUserId(), currentUserId)) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_CAN_NOT_SHARE_TO_SELF);
        }
        if (familyMemberShareService.existsEnabledShare(inviteEntity.getMemberId(), currentUserId)) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_MEMBER_ALREADY_SHARED);
        }

        HealthFamilyMemberShareEntity shareEntity = familyMemberShareService.lambdaQuery()
            .eq(HealthFamilyMemberShareEntity::getMemberId, inviteEntity.getMemberId())
            .eq(HealthFamilyMemberShareEntity::getCollaboratorUserId, currentUserId)
            .last("limit 1")
            .one();
        if (shareEntity == null) {
            shareEntity = new HealthFamilyMemberShareEntity();
            shareEntity.setMemberId(inviteEntity.getMemberId());
            shareEntity.setOwnerUserId(inviteEntity.getOwnerUserId());
            shareEntity.setCollaboratorUserId(currentUserId);
        }
        shareEntity.setInviteId(inviteEntity.getInviteId());
        shareEntity.setShareRole(inviteEntity.getShareRole());
        shareEntity.setShareStatus(FamilyMemberShareStatusEnum.ENABLED.getValue());
        shareEntity.setAcceptedTime(new Date());
        shareEntity.setRemark(inviteEntity.getRemark());
        if (shareEntity.getShareId() == null) {
            familyMemberShareService.save(shareEntity);
        } else {
            familyMemberShareService.updateById(shareEntity);
        }

        inviteEntity.setInviteStatus(FamilyShareInviteStatusEnum.ACCEPTED.getValue());
        inviteEntity.setInviteeUserId(currentUserId);
        inviteEntity.setAcceptedTime(new Date());
        familyShareInviteService.updateById(inviteEntity);
        return new FamilyShareInviteDTO(inviteEntity);
    }

    /**
     * 移除某个协同账号。
     * 当前实现采用“停用共享关系”的方式，避免直接硬删除丢失审计线索。
     *
     * @param memberId 家庭成员ID
     * @param shareId 共享关系ID
     * @param currentUserId 当前账号ID
     */
    public void removeCollaborator(Long memberId, Long shareId, Long currentUserId) {
        familyMemberAccessService.checkOwner(memberId, currentUserId);
        HealthFamilyMemberShareEntity shareEntity = familyMemberShareService.getById(shareId);
        if (shareEntity == null || !Objects.equals(shareEntity.getMemberId(), memberId)) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_MEMBER_SHARE_NOT_FOUND);
        }
        shareEntity.setShareStatus(FamilyMemberShareStatusEnum.DISABLED.getValue());
        familyMemberShareService.updateById(shareEntity);
    }

    /**
     * 取消尚未接受的邀请。
     *
     * @param memberId 家庭成员ID
     * @param inviteId 邀请ID
     * @param currentUserId 当前账号ID
     */
    public void cancelShareInvite(Long memberId, Long inviteId, Long currentUserId) {
        familyMemberAccessService.checkOwner(memberId, currentUserId);
        HealthFamilyShareInviteEntity inviteEntity = familyShareInviteService.getById(inviteId);
        if (inviteEntity == null || !Objects.equals(inviteEntity.getMemberId(), memberId)) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_SHARE_INVITE_NOT_FOUND);
        }
        if (!FamilyShareInviteStatusEnum.PENDING.getValue().equals(inviteEntity.getInviteStatus())) {
            throw new ApiException(ErrorCode.Business.APP_FAMILY_SHARE_INVITE_STATUS_INVALID);
        }
        inviteEntity.setInviteStatus(FamilyShareInviteStatusEnum.CANCELED.getValue());
        familyShareInviteService.updateById(inviteEntity);
    }

    /**
     * 构建带访问权限信息的家庭成员 DTO。
     */
    private FamilyMemberDTO buildFamilyMemberDTO(HealthFamilyMemberEntity entity, FamilyMemberAccessContextDTO accessContext) {
        FamilyMemberDTO dto = new FamilyMemberDTO(entity);
        if (accessContext != null) {
            dto.setAccessSource(accessContext.getAccessSource());
            dto.setAccessSourceName(accessContext.getAccessSourceName());
            dto.setAccessRole(accessContext.getAccessRole());
            dto.setAccessRoleName(accessContext.getAccessRoleName());
            dto.setIsOwner(accessContext.isOwner());
            dto.setCanEdit(accessContext.isCanEdit());
        }
        return dto;
    }

    /**
     * 确保家庭成员已经拥有业务编码。
     *
     * <p>当前编码规则直接由主键推导，因此：
     * 1. 新增后可立即补写
     * 2. 历史数据即使未回填，也能在服务层兜底修正
     */
    private void ensureMemberCode(HealthFamilyMemberEntity entity) {
        if (entity == null || entity.getMemberId() == null) {
            return;
        }
        String targetMemberCode = HealthBizCodeFormatter.formatMemberCode(entity.getMemberId());
        if (Objects.equals(entity.getMemberCode(), targetMemberCode)) {
            return;
        }
        entity.setMemberCode(targetMemberCode);
        familyMemberService.updateById(entity);
    }

    /**
     * 批量加载 App 用户快照。
     *
     * <p>家庭协同列表和邀请列表都只需要展示手机号、昵称等轻量字段，
     * 不需要为了每条记录重新查询一次用户表。
     * 统一收口到这个方法后，后续新增类似列表也可以复用同一套批量加载口径。
     */
    private Map<Long, HealthAppUserEntity> loadAppUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appUserService.listByIds(userIds).stream()
            .filter(userEntity -> userEntity.getUserId() != null)
            .collect(Collectors.toMap(HealthAppUserEntity::getUserId, userEntity -> userEntity,
                (left, right) -> left));
    }

    /**
     * 生成家庭共享邀请码。
     * 这里采用 8 位大写短码，方便移动端口头传达或手输录入。
     */
    private String generateInviteCode() {
        return IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
    }
}
