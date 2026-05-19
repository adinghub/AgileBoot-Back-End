package com.healthtrail.domain.health.family.model;

import cn.hutool.core.bean.BeanUtil;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.common.enums.common.GenderEnum;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.family.command.AddFamilyMemberCommand;
import com.healthtrail.domain.health.family.command.UpdateFamilyMemberCommand;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 家庭成员领域模型，负责成员的增删改命令装载、字段校验和归属校验。
 *
 * <p>该模型集中处理家庭成员的字段装载和业务校验，
 * 避免 Controller 或 ApplicationService 里散落大量字段判断逻辑。
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FamilyMemberModel extends HealthFamilyMemberEntity {

    /** 家庭成员数据库服务 */
    private HealthFamilyMemberService familyMemberService;

    public FamilyMemberModel(HealthFamilyMemberService familyMemberService) {
        this.familyMemberService = familyMemberService;
    }

    public FamilyMemberModel(HealthFamilyMemberEntity entity, HealthFamilyMemberService familyMemberService) {
        this(familyMemberService);
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 从新增命令装载数据。
     *
     * @param command 新增命令
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void loadAddCommand(AddFamilyMemberCommand command, Long ownerUserId) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "memberId", "ownerUserId");
            this.setOwnerUserId(ownerUserId);
        }
    }

    /**
     * 从修改命令装载数据。
     *
     * @param command 修改命令
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void loadUpdateCommand(UpdateFamilyMemberCommand command, Long ownerUserId) {
        if (command != null) {
            loadAddCommand(command, ownerUserId);
            this.setMemberId(command.getMemberId());
        }
    }

    /**
     * 校验家庭成员字段是否符合系统约定。
     * 当前主要校验性别和状态是否是系统允许的枚举值。
     */
    public void checkFields() {
        BasicEnumUtil.fromValue(GenderEnum.class, getGender());
        if (getStatus() == null) {
            setStatus(StatusEnum.ENABLE.getValue());
        } else {
            BasicEnumUtil.fromValue(StatusEnum.class, getStatus());
        }
    }

    /**
     * 校验该成员是否归属于当前登录用户。
     * 这是 App 端家庭成员接口最重要的权限边界之一，避免用户通过篡改 memberId 访问他人数据。
     *
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void checkOwnedByUser(Long ownerUserId) {
        if (getMemberId() == null || ownerUserId == null || !familyMemberService.isOwnedByUser(getMemberId(), ownerUserId)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getMemberId(), "家庭成员");
        }
    }
}
