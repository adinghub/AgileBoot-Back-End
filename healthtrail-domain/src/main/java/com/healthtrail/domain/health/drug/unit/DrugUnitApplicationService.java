package com.healthtrail.domain.health.drug.unit;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.domain.health.attachment.AttachmentTypeConstants;
import com.healthtrail.domain.health.attachment.db.AttachmentEntity;
import com.healthtrail.domain.health.attachment.db.AttachmentService;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.drug.unit.command.AddDrugUnitCommand;
import com.healthtrail.domain.health.drug.unit.command.UpdateDrugUnitCommand;
import com.healthtrail.domain.health.drug.unit.db.DrugUnitEntity;
import com.healthtrail.domain.health.drug.unit.db.DrugUnitService;
import com.healthtrail.domain.health.drug.unit.dto.DrugUnitDTO;
import com.healthtrail.domain.health.drug.unit.query.DrugUnitQuery;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 药品单位应用服务。
 *
 * <p>该服务负责把“后台维护单位主数据”和“前端搜索选择单位”两条链路收口到同一套口径：
 * 1. 单位名称和编码唯一
 * 2. 停用单位不再下发给 App 端选择器
 * 3. 单位一旦被药品引用，删除前先阻断，避免形成悬挂引用
 */
@Service
@RequiredArgsConstructor
public class DrugUnitApplicationService {

    /** 药品单位数据库服务 */
    private final DrugUnitService drugUnitService;

    /** 附件数据库服务 */
    private final AttachmentService attachmentService;

    /** 药品主表数据库服务 */
    private final HealthDrugService drugService;

    /**
     * 分页查询药品单位。
     */
    public PageDTO<DrugUnitDTO> getUnitPage(DrugUnitQuery query) {
        Page<DrugUnitEntity> page = drugUnitService.page(query.toPage(), query.toQueryWrapper());
        List<DrugUnitDTO> records = page.getRecords().stream().map(DrugUnitDTO::new).collect(Collectors.toList());
        fillIconUrl(records);
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 获取药品单位详情。
     */
    public DrugUnitDTO getUnitInfo(Long unitId) {
        DrugUnitDTO dto = new DrugUnitDTO(loadUnit(unitId));
        fillIconUrl(Collections.singletonList(dto));
        return dto;
    }

    /**
     * 获取 App 端可选药品单位列表。
     *
     * <p>当前单位量不会特别大，
     * 因此这里直接返回最多 100 条启用数据，足够支持常规搜索下拉场景。
     */
    public List<DrugUnitDTO> listEnabledUnits(String keyword) {
        List<DrugUnitEntity> unitEntities = drugUnitService.lambdaQuery()
            .eq(DrugUnitEntity::getStatus, StatusEnum.ENABLE.getValue())
            .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                .like(DrugUnitEntity::getUnitName, StrUtil.trim(keyword))
                .or()
                .like(DrugUnitEntity::getUnitCode, StrUtil.trim(keyword))
                .or()
                .like(DrugUnitEntity::getUnitAlias, StrUtil.trim(keyword)))
            .orderByAsc(DrugUnitEntity::getSort)
            .orderByAsc(DrugUnitEntity::getUnitId)
            .page(new Page<>(1, 100))
            .getRecords();
        List<DrugUnitDTO> unitDTOList = unitEntities.stream().map(DrugUnitDTO::new).collect(Collectors.toList());
        fillIconUrl(unitDTOList);
        return unitDTOList;
    }

    /**
     * 新增药品单位。
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUnit(AddDrugUnitCommand command) {
        DrugUnitEntity entity = new DrugUnitEntity();
        BeanUtil.copyProperties(command, entity, "unitId");
        fillDefaultFields(entity);
        validateFields(entity);
        checkUnique(entity);
        validateIconAttachment(entity.getIconAttachmentId());
        entity.setDeleted(0);
        drugUnitService.save(entity);
    }

    /**
     * 修改药品单位。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUnit(UpdateDrugUnitCommand command) {
        DrugUnitEntity entity = loadUnit(command.getUnitId());
        BeanUtil.copyProperties(command, entity, "unitId");
        fillDefaultFields(entity);
        validateFields(entity);
        checkUnique(entity);
        validateIconAttachment(entity.getIconAttachmentId());
        drugUnitService.updateById(entity);
    }

    /**
     * 删除药品单位。
     *
     * <p>这里不允许删除仍被药品引用的单位，
     * 避免药品详情里出现“只有单位ID、没有单位主数据”的悬挂数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUnit(Long unitId) {
        loadUnit(unitId);
        long usedCount = drugService.lambdaQuery()
            .eq(DrugEntity::getStockUnitId, unitId)
            .count();
        if (usedCount > 0) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_UNIT_ALREADY_USED);
        }
        drugUnitService.removeById(unitId);
    }

    /**
     * 加载一个启用中的药品单位。
     *
     * <p>药品保存或补库存时会调用这个方法，
     * 用来把标准单位ID解析为最终写入药品表的单位名称快照。
     */
    public DrugUnitEntity loadEnabledUnit(Long unitId) {
        DrugUnitEntity entity = loadUnit(unitId);
        if (!Objects.equals(entity.getStatus(), StatusEnum.ENABLE.getValue())) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_UNIT_DISABLED);
        }
        return entity;
    }

    /**
     * 加载单位实体。
     */
    private DrugUnitEntity loadUnit(Long unitId) {
        DrugUnitEntity entity = drugUnitService.getById(unitId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, unitId, "药品单位");
        }
        return entity;
    }

    /**
     * 补齐默认字段。
     */
    private void fillDefaultFields(DrugUnitEntity entity) {
        entity.setUnitName(normalizeText(entity.getUnitName()));
        entity.setUnitCode(StrUtil.blankToDefault(normalizeText(entity.getUnitCode()), entity.getUnitName()));
        entity.setUnitAlias(normalizeText(entity.getUnitAlias()));
        entity.setPrecisionScale(entity.getPrecisionScale() == null ? 0 : entity.getPrecisionScale());
        entity.setSort(entity.getSort() == null ? 0 : entity.getSort());
        entity.setStatus(entity.getStatus() == null ? StatusEnum.ENABLE.getValue() : entity.getStatus());
        entity.setRemark(normalizeText(entity.getRemark()));
    }

    /**
     * 单位字段校验。
     */
    private void validateFields(DrugUnitEntity entity) {
        if (StrUtil.isBlank(entity.getUnitName())) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_UNIT_NAME_REQUIRED);
        }
        if (entity.getPrecisionScale() != null && (entity.getPrecisionScale() < 0 || entity.getPrecisionScale() > 4)) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION);
        }
    }

    /**
     * 校验单位编码与单位名称唯一性。
     */
    private void checkUnique(DrugUnitEntity entity) {
        DrugUnitEntity duplicatedCode = drugUnitService.lambdaQuery()
            .eq(DrugUnitEntity::getUnitCode, entity.getUnitCode())
            .ne(entity.getUnitId() != null, DrugUnitEntity::getUnitId, entity.getUnitId())
            .one();
        if (duplicatedCode != null) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_UNIT_CODE_IS_NOT_UNIQUE);
        }

        DrugUnitEntity duplicatedName = drugUnitService.lambdaQuery()
            .eq(DrugUnitEntity::getUnitName, entity.getUnitName())
            .ne(entity.getUnitId() != null, DrugUnitEntity::getUnitId, entity.getUnitId())
            .one();
        if (duplicatedName != null) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_UNIT_NAME_IS_NOT_UNIQUE);
        }
    }

    /**
     * 校验图标附件是否存在。
     */
    private void validateIconAttachment(Long iconAttachmentId) {
        if (iconAttachmentId == null) {
            return;
        }
        AttachmentEntity attachmentEntity = attachmentService.getById(iconAttachmentId);
        if (attachmentEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, iconAttachmentId, "附件");
        }
        if (!Objects.equals(attachmentEntity.getAttachmentType(), AttachmentTypeConstants.DRUG_UNIT_ICON)) {
            throw new ApiException(ErrorCode.Business.HEALTH_ATTACHMENT_TYPE_INVALID);
        }
    }

    /**
     * 回填单位图标地址。
     */
    private void fillIconUrl(List<DrugUnitDTO> unitDTOList) {
        if (unitDTOList == null || unitDTOList.isEmpty()) {
            return;
        }

        Map<Long, AttachmentEntity> attachmentMap = listAttachmentMap(unitDTOList.stream()
            .map(DrugUnitDTO::getIconAttachmentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));

        for (DrugUnitDTO unitDTO : unitDTOList) {
            if (unitDTO == null || unitDTO.getIconAttachmentId() == null) {
                continue;
            }
            AttachmentEntity attachmentEntity = attachmentMap.get(unitDTO.getIconAttachmentId());
            unitDTO.setIconUrl(attachmentEntity == null ? null : FileUploadUtils.getAccessUrl(attachmentEntity.getFileUrl()));
        }
    }

    /**
     * 批量查询附件并转成 Map，减少列表场景下的重复查库。
     */
    private Map<Long, AttachmentEntity> listAttachmentMap(Collection<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return attachmentService.lambdaQuery()
            .in(AttachmentEntity::getAttachmentId, attachmentIds)
            .list()
            .stream()
            .collect(Collectors.toMap(AttachmentEntity::getAttachmentId, entity -> entity));
    }

    /**
     * 统一做字符串裁剪与空串转 null。
     */
    private String normalizeText(String text) {
        String trimmedText = StrUtil.trim(text);
        return StrUtil.isBlank(trimmedText) ? null : trimmedText;
    }
}
