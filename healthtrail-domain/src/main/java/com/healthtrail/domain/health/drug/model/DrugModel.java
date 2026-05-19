package com.healthtrail.domain.health.drug.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.drug.command.AddDrugCommand;
import com.healthtrail.domain.health.drug.command.UpdateDrugCommand;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 药品领域模型，负责双层药品库的归属校验、唯一性及库存跟踪规则。
 *
 * <p>当前药品采用双层药品库：
 * 1. 系统下发药品：owner_user_id = 0
 * 2. 用户个人药品：owner_user_id = 当前App用户ID
 *
 * <p>因此这里的核心规则也围绕"归属范围"展开：
 * 1. 药品名称在同一归属范围内唯一，避免同一套药品库出现重名
 * 2. App 用户只能修改自己的个人药品
 * 3. 后台只能维护系统下发药品
 * 4. App 查询时可以同时看到"系统药品 + 自己的药品"
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DrugModel extends DrugEntity {

    /** 药品数据库服务 */
    private HealthDrugService drugService;

    public DrugModel(HealthDrugService drugService) {
        this.drugService = drugService;
    }

    public DrugModel(DrugEntity entity, HealthDrugService drugService) {
        this(drugService);
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 从新增命令装载药品字段。
     */
    public void loadAddCommand(AddDrugCommand command, Long ownerUserId) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "drugId");
            this.setOwnerUserId(ownerUserId);
        }
    }

    /**
     * 从修改命令装载药品字段。
     */
    public void loadUpdateCommand(UpdateDrugCommand command, Long ownerUserId) {
        if (command != null) {
            loadAddCommand(command, ownerUserId);
            this.setDrugId(command.getDrugId());
        }
    }

    /**
     * 校验药品名称是否重复。
     * 唯一性范围是"当前归属范围"，而不是全平台唯一。
     * 这样系统药品和用户个人药品可以分别维护各自的重名约束，不会互相误伤。
     */
    public void checkDrugNameUnique() {
        DrugEntity duplicated = drugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, getOwnerUserId())
            .eq(DrugEntity::getDrugName, getDrugName())
            .ne(getDrugId() != null, DrugEntity::getDrugId, getDrugId())
            .one();
        if (duplicated != null) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_NAME_IS_NOT_UNIQUE);
        }
    }

    /**
     * 校验当前药品是否归属于指定 App 用户。
     * 这是 App 端修改、删除个人药品时最重要的边界，避免用户误操作其他人的药品或系统药品。
     */
    public void checkOwnedByUser(Long ownerUserId) {
        if (getDrugId() == null || ownerUserId == null || !ownerUserId.equals(getOwnerUserId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getDrugId(), "药品");
        }
    }

    /**
     * 校验当前药品是否对指定 App 用户可见。
     *
     * <p>App 查询详情时允许看到两类药品：
     * 1. 当前用户自己维护的药品
     * 2. 后台下发给所有用户共享使用的系统药品
     */
    public void checkVisibleToUser(Long ownerUserId) {
        boolean systemDrug = getOwnerUserId() != null
            && getOwnerUserId().equals(com.healthtrail.domain.health.drug.query.DrugQuery.SYSTEM_DRUG_OWNER_ID);
        boolean selfDrug = getOwnerUserId() != null && getOwnerUserId().equals(ownerUserId);
        if (getDrugId() == null || ownerUserId == null || (!systemDrug && !selfDrug)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getDrugId(), "药品");
        }
    }

    /**
     * 校验当前药品是否为系统下发药品。
     * 后台入口只允许操作系统药品，避免误改用户个人药品。
     */
    public void checkSystemDrug() {
        if (getDrugId() == null || getOwnerUserId() == null
            || !getOwnerUserId().equals(com.healthtrail.domain.health.drug.query.DrugQuery.SYSTEM_DRUG_OWNER_ID)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getDrugId(), "药品");
        }
    }

    /**
     * 校验药品字段合法性。
     *
     * <p>当前除了状态值以外，还会统一约束库存字段：
     * 1. 系统药品不允许维护个人库存
     * 2. 启用库存跟踪时必须明确库存单位
     * 3. 只填写预警值时，库存默认从 0 开始
     * 4. 库存总量由批号效期明细实时汇总，这里的 stockQuantity 仅作为运行时临时值使用
     */
    public void checkFields() {
        if (getStatus() == null) {
            setStatus(StatusEnum.ENABLE.getValue());
        } else {
            BasicEnumUtil.fromValue(StatusEnum.class, getStatus());
        }

        if (isSystemDrug()) {
            clearInventoryFieldsForSystemDrug();
            return;
        }

        boolean hasStockTrackingInput = getStockQuantity() != null
            || getStockAlertThreshold() != null
            || getStockUnitId() != null
            || StrUtil.isNotBlank(getStockUnit());
        if (!hasStockTrackingInput) {
            setStockQuantity(null);
            setStockUnitId(null);
            setStockUnit(null);
            setStockAlertThreshold(null);
            setLowStockNotified(Boolean.FALSE);
            setLowStockNotifyTime(null);
            return;
        }

        if (StrUtil.isBlank(getStockUnit())) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_UNIT_REQUIRED);
        }
        if (getStockQuantity() == null) {
            setStockQuantity(BigDecimal.ZERO);
        }
        if (getLowStockNotified() == null) {
            setLowStockNotified(Boolean.FALSE);
        }
    }

    /**
     * 当前药品是否为系统药品。
     */
    public boolean isSystemDrug() {
        return getOwnerUserId() != null
            && getOwnerUserId().equals(com.healthtrail.domain.health.drug.query.DrugQuery.SYSTEM_DRUG_OWNER_ID);
    }

    /**
     * 当前药品是否为个人药品。
     */
    public boolean isPersonalDrug() {
        return getOwnerUserId() != null
            && !getOwnerUserId().equals(com.healthtrail.domain.health.drug.query.DrugQuery.SYSTEM_DRUG_OWNER_ID);
    }

    /**
     * 当前药品是否启用了库存跟踪。
     */
    public boolean isStockTrackingEnabled() {
        return isPersonalDrug() && StrUtil.isNotBlank(getStockUnit());
    }

    /**
     * 当前库存是否已经进入预警区间。
     */
    public boolean isBelowAlertThreshold() {
        return isStockTrackingEnabled()
            && getStockAlertThreshold() != null
            && getStockQuantity().compareTo(getStockAlertThreshold()) <= 0;
    }

    /**
     * 获取安全库存值。
     */
    public BigDecimal getSafeStockQuantity() {
        return getStockQuantity() == null ? BigDecimal.ZERO : getStockQuantity();
    }

    /**
     * 系统药品强制清空个人库存字段。
     *
     * <p>系统药品是所有用户共享的基础档案，不能直接保存某个用户自己的库存信息。
     */
    private void clearInventoryFieldsForSystemDrug() {
        setSourceDrugId(null);
        setStockQuantity(null);
        setStockAlertThreshold(null);
        setLowStockNotified(Boolean.FALSE);
        setLowStockNotifyTime(null);
    }
}
