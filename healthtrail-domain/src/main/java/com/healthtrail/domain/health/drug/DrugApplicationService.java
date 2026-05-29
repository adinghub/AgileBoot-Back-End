package com.healthtrail.domain.health.drug;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.attachment.AttachmentTypeConstants;
import com.healthtrail.domain.health.attachment.db.AttachmentEntity;
import com.healthtrail.domain.health.attachment.db.AttachmentService;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpRecommendedActionDTO;
import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import com.healthtrail.domain.health.drug.command.AddDrugCommand;
import com.healthtrail.domain.health.drug.command.ImportDrugCommand;
import com.healthtrail.domain.health.drug.command.ImportSystemDrugCommand;
import com.healthtrail.domain.health.drug.command.IncreaseDrugStockCommand;
import com.healthtrail.domain.health.drug.command.TemporaryUseDrugStockCommand;
import com.healthtrail.domain.health.drug.command.UpdateDrugCommand;
import com.healthtrail.domain.health.drug.command.UpdateDrugStockConfigCommand;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchService;
import com.healthtrail.domain.health.drug.db.HealthDrugStockLogEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugStockLogService;
import com.healthtrail.domain.health.drug.db.HealthDrugTemporaryMedicationRecordEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugTemporaryMedicationRecordService;
import com.healthtrail.domain.health.drug.dto.DrugDTO;
import com.healthtrail.domain.health.drug.dto.DrugImportResultDTO;
import com.healthtrail.domain.health.drug.dto.DrugStockBatchDTO;
import com.healthtrail.domain.health.drug.dto.DrugStockLogDTO;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.drug.model.DrugModel;
import com.healthtrail.domain.health.drug.model.DrugModelFactory;
import com.healthtrail.domain.health.drug.query.DrugQuery;
import com.healthtrail.domain.health.drug.unit.DrugUnitApplicationService;
import com.healthtrail.domain.health.drug.unit.db.DrugUnitEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.message.AppMessageApplicationService;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.healthtrail.domain.health.message.dto.HealthAppMessageCreateRequest;
import com.healthtrail.domain.health.message.notify.HealthAppMessageNotice;
import com.healthtrail.domain.health.message.notify.HealthAppMessageNotifyResult;
import com.healthtrail.domain.health.message.notify.HealthAppMessageNotifier;
import com.healthtrail.domain.health.support.HealthBizCodeFormatter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 药品应用服务。
 *
 * <p>该服务同时承接以下几类能力：
 * 1. 双层药品库查询与维护
 * 2. 系统药品引入到个人药柜
 * 3. 个人药柜批号效期库存维护
 * 4. 低库存消息提醒
 *
 * <p>本次库存模型升级后，药品主表不再冗余保存汇总库存，
 * 所有库存数量都从批号效期明细表实时汇总，确保展示、提醒和扣减口径完全一致。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DrugApplicationService {

    /**
     * 默认药品图片地址。
     *
     * <p>当用户或后台没有上传药品图片时，
     * 前端可以直接使用该地址渲染默认图，不需要自己再做空值判断兜底。
     */
    private static final String DEFAULT_DRUG_IMAGE_URL = "/images/drug-default.svg";

    /**
     * 批次库存展示与扣减的统一排序规则。
     *
     * <p>排序口径固定为：
     * 1. 效期越早越靠前
     * 2. 批号为空的放前面，非空批号按字典序排列
     * 3. 最后再用批次ID兜底，保证排序稳定
     *
     * <p>这样前端展示顺序和后端自动扣减顺序保持一致，避免“看见的是一套顺序，扣减的是另一套顺序”。
     */
    private static final Comparator<HealthDrugStockBatchEntity> STOCK_BATCH_SORT_COMPARATOR = Comparator
        .comparing(HealthDrugStockBatchEntity::getExpireDate, Comparator.nullsLast(Date::compareTo))
        .thenComparing(HealthDrugStockBatchEntity::getBatchNo, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
        .thenComparing(HealthDrugStockBatchEntity::getBatchId, Comparator.nullsLast(Long::compareTo));

    /**
     * 新增个人药品时的库存初始化流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_CREATE_INIT = "CREATE_INIT";

    /**
     * 引入系统药品时的库存初始化流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_IMPORT_INIT = "IMPORT_INIT";

    /**
     * 手工补库存流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_MANUAL_INCREASE = "MANUAL_INCREASE";

    /**
     * 修改库存配置流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_ALERT_THRESHOLD_UPDATE = "ALERT_THRESHOLD_UPDATE";

    /**
     * 标记已服药后自动扣减库存流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_REMINDER_TAKE_DEDUCT = "REMINDER_TAKE_DEDUCT";

    /**
     * 撤销已服药后回补库存流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_REMINDER_TAKE_RESTORE = "REMINDER_TAKE_RESTORE";

    /**
     * 临时用药扣减库存流水类型。
     */
    private static final String STOCK_CHANGE_TYPE_TEMPORARY_USE_DEDUCT = "TEMPORARY_USE_DEDUCT";

    /**
     * 站内消息渠道名。
     *
     * <p>近效期提醒本次明确要求“不响铃、不做设备通知”，
     * 因此消息创建成功后直接标记为站内消息送达即可。
     */
    private static final String MESSAGE_CHANNEL_IN_APP = "IN_APP";

    /** 药品主表数据库服务 */
    private final HealthDrugService drugService;

    /** 药品批号效期库存数据库服务 */
    private final HealthDrugStockBatchService drugStockBatchService;

    /** 药品库存流水数据库服务 */
    private final HealthDrugStockLogService drugStockLogService;

    /** 临时用药记录数据库服务 */
    private final HealthDrugTemporaryMedicationRecordService temporaryMedicationRecordService;

    /** 用药计划数据库服务 */
    private final HealthMedicationPlanService medicationPlanService;

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 药品领域模型工厂 */
    private final DrugModelFactory drugModelFactory;

    /** 药品单位应用服务 */
    private final DrugUnitApplicationService drugUnitApplicationService;

    /** 附件数据库服务 */
    private final AttachmentService attachmentService;

    /** App消息应用服务 */
    private final AppMessageApplicationService appMessageApplicationService;

    /** App消息通知发送器 */
    private final HealthAppMessageNotifier healthAppMessageNotifier;

    /**
     * 分页查询药品列表。
     *
     * <p>列表返回时会把每条药品的当前库存实时汇总出来，
     * 并且按同一套规则回填批号效期库存明细，方便前端直接展示药柜库存结构。
     *
     * @param query 查询条件
     * @return 分页药品列表
     */
    public PageDTO<DrugDTO> getDrugList(DrugQuery query) {
        Page<DrugEntity> page = drugService.page(query.toPage(), query.toQueryWrapper());
        List<DrugDTO> records = page.getRecords().stream().map(DrugDTO::new).collect(Collectors.toList());
        fillRealtimeStockData(records, true);
        fillDrugImageUrl(records);
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 获取药品详情。
     *
     * @param drugId 药品ID
     * @param currentUserId 当前登录 App 用户ID
     * @return 药品详情
     */
    public DrugDTO getDrugInfo(Long drugId, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkVisibleToUser(currentUserId);
        return buildDrugDTO(drugModel);
    }

    /**
     * 查询指定药品的库存流水。
     *
     * <p>这里严格限定为“当前用户自己拥有的个人药品”，原因是：
     * 1. 库存流水本质上是个人药柜数据，不属于系统药品公共数据
     * 2. 只有先校验归属，前端展示的流水才不会串到其他人的药柜
     * 3. App 详情页需要直接按时间倒序展示最近变更记录
     *
     * @param drugId 药品ID
     * @param currentUserId 当前登录 App 用户ID
     * @return 库存流水列表，按创建时间倒序返回
     */
    public List<DrugStockLogDTO> getDrugStockLogs(Long drugId, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkOwnedByUser(currentUserId);

        return drugStockLogService.lambdaQuery()
            .eq(HealthDrugStockLogEntity::getOwnerUserId, currentUserId)
            .eq(HealthDrugStockLogEntity::getDrugId, drugId)
            .orderByDesc(HealthDrugStockLogEntity::getCreateTime)
            .orderByDesc(HealthDrugStockLogEntity::getLogId)
            .list()
            .stream()
            .map(DrugStockLogDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * 巡检并派发药品近效期提醒。
     *
     * <p>这里把巡检逻辑放在应用服务，而不是直接塞进调度器里，原因是：
     * 1. 后续如果要支持人工触发、后台补扫或集成测试，可以直接复用这一入口
     * 2. 近效期提醒依赖药品、批次、消息三类领域对象，放在应用服务更容易保持收口
     *
     * @param alertDays 近效期提醒窗口天数
     * @return 本次新创建的提醒数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int inspectAndDispatchNearExpiryAlerts(int alertDays) {
        int safeAlertDays = alertDays <= 0 ? 7 : alertDays;
        Date today = DateUtil.beginOfDay(new Date());
        Date alertDeadline = DateUtil.endOfDay(DateUtil.offsetDay(today, safeAlertDays));

        List<HealthDrugStockBatchEntity> positiveBatches = drugStockBatchService.lambdaQuery()
            .gt(HealthDrugStockBatchEntity::getStockQuantity, BigDecimal.ZERO)
            .isNotNull(HealthDrugStockBatchEntity::getExpireDate)
            // `is_default_batch` 在 PostgreSQL 中落的是 SMALLINT(0/1)，
            // 这里刻意不用 `Boolean.FALSE` 直接做 wrapper 条件，
            // 因为 MyBatis-Plus 在构造查询参数时不会自动复用实体字段上的 typeHandler，
            // 最终会把布尔值直接绑定成 `boolean`，触发 PG 的
            // “operator does not exist: smallint = boolean”。
            //
            // 因此查询侧统一显式传 0/1，和当前表结构保持一致。
            .eq(HealthDrugStockBatchEntity::getDefaultBatch, toSmallintFlag(false))
            .list();
        if (positiveBatches.isEmpty()) {
            return 0;
        }

        Map<Long, DrugEntity> drugEntityMap = drugService.listByIds(positiveBatches.stream()
                .map(HealthDrugStockBatchEntity::getDrugId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()))
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(DrugEntity::getDrugId, entity -> entity));

        int createdAlertCount = 0;
        for (HealthDrugStockBatchEntity batchEntity : positiveBatches) {
            if (batchEntity == null || batchEntity.getBatchId() == null) {
                continue;
            }

            Date expireDate = batchEntity.getExpireDate();
            if (expireDate == null) {
                resetNearExpiryAlertStateIfNecessary(batchEntity);
                continue;
            }

            Date normalizedExpireDate = DateUtil.beginOfDay(expireDate);
            boolean inAlertWindow = !normalizedExpireDate.before(today) && !normalizedExpireDate.after(alertDeadline);
            if (!inAlertWindow) {
                resetNearExpiryAlertStateIfNecessary(batchEntity);
                continue;
            }
            if (Boolean.TRUE.equals(batchEntity.getNearExpireNotified())) {
                continue;
            }

            DrugEntity drugEntity = drugEntityMap.get(batchEntity.getDrugId());
            if (!isEligiblePersonalDrugForNearExpiryAlert(drugEntity, batchEntity)) {
                resetNearExpiryAlertStateIfNecessary(batchEntity);
                continue;
            }

            if (dispatchNearExpiryAlertQuietly(drugEntity, batchEntity, today)) {
                createdAlertCount++;
            }
        }
        return createdAlertCount;
    }

    /**
     * 新增药品。
     *
     * <p>当前仅允许 App 侧新增个人药品。
     * 如果新增时已经给了库存初始值，也会同步写入首批库存明细和初始化库存流水。
     *
     * @param currentUserId 当前登录 App 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDrug(AddDrugCommand addCommand, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.create();
        drugModel.loadAddCommand(addCommand, currentUserId);
        resolveDrugReferenceFields(drugModel);
        drugModel.checkDrugNameUnique();
        drugModel.checkFields();
        validateInitialStockBatchInput(drugModel.getSafeStockQuantity(),
            addCommand == null ? null : addCommand.getBatchNo(),
            addCommand == null ? null : addCommand.getExpireDate());
        drugModel.insert();
        ensureDrugCode(drugModel);

        if (drugModel.isStockTrackingEnabled()) {
            createInitialStockBatchIfNecessary(drugModel,
                addCommand == null ? null : addCommand.getBatchNo(),
                addCommand == null ? null : addCommand.getExpireDate());

            BigDecimal initialStock = fillRealtimeStockQuantity(drugModel);
            if (initialStock.compareTo(BigDecimal.ZERO) > 0) {
                saveStockLog(drugModel, STOCK_CHANGE_TYPE_CREATE_INIT, BigDecimal.ZERO, initialStock,
                    initialStock, null, null, "新增个人药品时初始化首批库存");
            }
            handleLowStockAlertAfterStockChange(drugModel, null, null);
        }
    }

    /**
     * 把系统药品引入到当前用户的个人药柜。
     *
     * <p>引入后的数据不再是系统共享药品，而是一条真正归属于当前用户的个人药品记录。
     * 这样后续库存、备注、预警值都可以独立维护，不会影响其他用户。
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugDTO importSystemDrug(Long sourceDrugId, ImportSystemDrugCommand importCommand, Long currentUserId) {
        DrugModel systemDrugModel = drugModelFactory.loadById(sourceDrugId);
        if (!systemDrugModel.isSystemDrug()) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_IMPORT_ONLY_ALLOW_SYSTEM_DRUG);
        }

        DrugModel importedDrugModel = findImportedSystemDrug(currentUserId, sourceDrugId);
        if (importedDrugModel != null) {
            return buildDrugDTO(importedDrugModel);
        }

        AddDrugCommand addCommand = buildImportSystemAddCommand(systemDrugModel, importCommand);
        DrugModel personalDrugModel = drugModelFactory.create();
        personalDrugModel.loadAddCommand(addCommand, currentUserId);
        resolveDrugReferenceFields(personalDrugModel);
        personalDrugModel.setSourceDrugId(sourceDrugId);
        personalDrugModel.setLowStockNotified(Boolean.FALSE);
        personalDrugModel.setLowStockNotifyTime(null);
        personalDrugModel.checkDrugNameUnique();
        personalDrugModel.checkFields();
        validateInitialStockBatchInput(personalDrugModel.getSafeStockQuantity(),
            addCommand.getBatchNo(), addCommand.getExpireDate());
        personalDrugModel.insert();
        ensureDrugCode(personalDrugModel);

        if (personalDrugModel.isStockTrackingEnabled()) {
            createInitialStockBatchIfNecessary(personalDrugModel, addCommand.getBatchNo(), addCommand.getExpireDate());

            BigDecimal initialStock = fillRealtimeStockQuantity(personalDrugModel);
            if (initialStock.compareTo(BigDecimal.ZERO) > 0) {
                saveStockLog(personalDrugModel, STOCK_CHANGE_TYPE_IMPORT_INIT, BigDecimal.ZERO, initialStock,
                    initialStock, null, null, "从系统药品引入到个人药柜时初始化首批库存");
            }
            handleLowStockAlertAfterStockChange(personalDrugModel, null, null);
        }
        return buildDrugDTO(personalDrugModel);
    }

    /**
     * 批量导入个人药品。
     *
     * <p>导入接口采用“尽量多成功、重复则跳过”的策略，原因是：
     * 1. 用户常常是从备忘录、聊天记录、医院单据里一次性粘贴多行文本
     * 2. 如果因为某一行重复就整批失败，体验会非常差
     * 3. 对个人药品库来说，重复药名跳过比报错更符合实际使用场景
     *
     * <p>当前支持的导入格式为：
     * `药品名称|通用名|规格|剂型|药品类型|用法说明|备注`
     * 其中只有第一列药品名称必填。
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugImportResultDTO importDrugs(ImportDrugCommand importCommand, Long currentUserId) {
        List<String> importLines = parseImportLines(importCommand);
        if (importLines.isEmpty()) {
            DrugImportResultDTO emptyResult = new DrugImportResultDTO();
            emptyResult.setRequestedCount(0);
            emptyResult.setImportedCount(0);
            emptyResult.setSkippedCount(0);
            emptyResult.setDuplicateCount(0);
            emptyResult.setImportedDrugs(Collections.emptyList());
            emptyResult.setSkippedDrugNames(Collections.emptyList());
            return emptyResult;
        }

        Integer defaultStatus = Objects.requireNonNullElse(importCommand.getDefaultStatus(), StatusEnum.ENABLE.getValue());
        String defaultDrugType = normalizeText(importCommand.getDefaultDrugType());
        List<DrugDTO> importedDrugs = new ArrayList<>();
        List<String> skippedDrugNames = new ArrayList<>();
        int duplicateCount = 0;

        for (String importLine : importLines) {
            String[] columns = importLine.split("\\|", -1);
            String drugName = readImportColumn(columns, 0);
            if (StrUtil.isBlank(drugName)) {
                skippedDrugNames.add("未填写药名");
                continue;
            }

            if (existsDrugName(currentUserId, drugName)) {
                duplicateCount++;
                skippedDrugNames.add(drugName);
                continue;
            }

            AddDrugCommand addCommand = buildImportAddCommand(columns, defaultDrugType, defaultStatus);
            DrugModel drugModel = drugModelFactory.create();
            drugModel.loadAddCommand(addCommand, currentUserId);
            drugModel.checkFields();
            drugModel.insert();
            ensureDrugCode(drugModel);
            importedDrugs.add(buildDrugDTO(drugModel));
        }

        DrugImportResultDTO resultDTO = new DrugImportResultDTO();
        resultDTO.setRequestedCount(importLines.size());
        resultDTO.setImportedCount(importedDrugs.size());
        resultDTO.setSkippedCount(importLines.size() - importedDrugs.size());
        resultDTO.setDuplicateCount(duplicateCount);
        resultDTO.setImportedDrugs(importedDrugs);
        resultDTO.setSkippedDrugNames(skippedDrugNames);
        return resultDTO;
    }

    /**
     * 给个人药品增加库存。
     *
     * <p>这里采用增量累加，而不是覆盖绝对值。
     * 每次补库存都落到某个“批号 + 效期”库存桶里，再实时汇总成药品当前总库存。
     * 同时当库存恢复到安全区后，会自动重置“已提醒”状态，允许下一轮真正再次触发提醒。
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugDTO increaseDrugStock(Long drugId, IncreaseDrugStockCommand increaseCommand, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkOwnedByUser(currentUserId);

        ResolvedStockUnit resolvedCommandStockUnit = resolveStockUnitSnapshot(
            increaseCommand == null ? null : increaseCommand.getStockUnitId(),
            increaseCommand == null ? null : increaseCommand.getStockUnit());
        String commandStockUnit = resolvedCommandStockUnit == null ? null : resolvedCommandStockUnit.getUnitName();
        if (StrUtil.isNotBlank(drugModel.getStockUnit())
            && StrUtil.isNotBlank(commandStockUnit)
            && !Objects.equals(StrUtil.trim(drugModel.getStockUnit()), StrUtil.trim(commandStockUnit))) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_UNIT_NOT_MATCH);
        }
        if (StrUtil.isBlank(drugModel.getStockUnit()) && StrUtil.isBlank(commandStockUnit)) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_UNIT_REQUIRED);
        }
        validateIncreaseStockBatchInput(increaseCommand);

        BigDecimal beforeQuantity = fillRealtimeStockQuantity(drugModel);
        BigDecimal increaseQuantity = increaseCommand.getIncreaseQuantity();
        if (StrUtil.isBlank(drugModel.getStockUnit()) && resolvedCommandStockUnit != null) {
            drugModel.setStockUnitId(resolvedCommandStockUnit.getUnitId());
            drugModel.setStockUnit(resolvedCommandStockUnit.getUnitName());
        } else if (resolvedCommandStockUnit != null
            && StrUtil.isNotBlank(drugModel.getStockUnit())
            && Objects.equals(StrUtil.trim(drugModel.getStockUnit()), StrUtil.trim(resolvedCommandStockUnit.getUnitName()))
            && drugModel.getStockUnitId() == null) {
            drugModel.setStockUnitId(resolvedCommandStockUnit.getUnitId());
        }
        if (drugModel.getLowStockNotified() == null) {
            drugModel.setLowStockNotified(Boolean.FALSE);
        }
        drugService.updateById(drugModel);

        createOrIncreaseStockBatch(drugModel, increaseCommand.getBatchNo(), increaseCommand.getExpireDate(), increaseQuantity);
        BigDecimal afterQuantity = fillRealtimeStockQuantity(drugModel);

        saveStockLog(drugModel, STOCK_CHANGE_TYPE_MANUAL_INCREASE, beforeQuantity, increaseQuantity,
            afterQuantity, null, null,
            limitLength(StrUtil.blankToDefault(increaseCommand.getOperationRemark(), "手工补库存"), 255));
        handleLowStockAlertAfterStockChange(drugModel, null, null);
        return buildDrugDTO(drugModel);
    }

    /**
     * 登记一次临时用药并同步扣减库存。
     *
     * <p>这条链路专门服务“没有计划、只是偶尔吃一次药”的场景，因此它和提醒反馈自动扣减不同：
     * 1. 由用户主动发起，库存不足时直接失败
     * 2. 不创建提醒，只沉淀临时用药记录和库存流水
     * 3. 扣减顺序优先消费默认批次，再按普通批次 FEFO 扣减
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugDTO temporaryUseDrugStock(Long drugId, TemporaryUseDrugStockCommand temporaryUseCommand, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkOwnedByUser(currentUserId);
        validateTemporaryUseCommand(temporaryUseCommand);
        checkTemporaryUseStockTracking(drugModel);
        validateTemporaryUseMemberOwnership(currentUserId, temporaryUseCommand == null ? null : temporaryUseCommand.getMemberId());

        BigDecimal beforeQuantity = fillRealtimeStockQuantity(drugModel);
        BigDecimal requiredQuantity = temporaryUseCommand.getUsedQuantity();
        if (beforeQuantity.compareTo(requiredQuantity) < 0) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_NOT_ENOUGH);
        }

        BigDecimal actualDecreaseQuantity = deductRealtimeStockWithDefaultBatchFirst(drugModel, requiredQuantity);
        if (actualDecreaseQuantity.compareTo(requiredQuantity) < 0) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_NOT_ENOUGH);
        }

        BigDecimal afterQuantity = fillRealtimeStockQuantity(drugModel);
        HealthDrugTemporaryMedicationRecordEntity recordEntity = buildTemporaryMedicationRecord(drugModel, temporaryUseCommand,
            actualDecreaseQuantity);
        temporaryMedicationRecordService.save(recordEntity);

        String operationRemark = buildTemporaryUseRemark(temporaryUseCommand, actualDecreaseQuantity, drugModel.getStockUnit());
        saveStockLog(drugModel, STOCK_CHANGE_TYPE_TEMPORARY_USE_DEDUCT, beforeQuantity, actualDecreaseQuantity.negate(),
            afterQuantity, null, null, recordEntity.getRecordId(), limitLength(operationRemark, 255));
        handleLowStockAlertAfterStockChange(drugModel, null, null);
        return buildDrugDTO(drugModel);
    }

    /**
     * 修改个人药品库存配置。
     *
     * <p>该接口主要维护库存单位与低库存预警值：
     * 1. 不直接覆盖库存绝对值
     * 2. 当前库存通过批次实时汇总
     * 3. 如果阈值改动后当前库存已落入预警区间，会立即触发提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugDTO updateDrugStockConfig(Long drugId, UpdateDrugStockConfigCommand updateCommand, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkOwnedByUser(currentUserId);

        BigDecimal beforeQuantity = fillRealtimeStockQuantity(drugModel);
        ResolvedStockUnit resolvedTargetStockUnit = resolveStockUnitSnapshot(
            updateCommand == null ? null : updateCommand.getStockUnitId(),
            updateCommand == null ? null : updateCommand.getStockUnit());
        String targetStockUnit = resolvedTargetStockUnit == null
            ? drugModel.getStockUnit()
            : resolvedTargetStockUnit.getUnitName();
        Long targetStockUnitId = resolvedTargetStockUnit == null
            ? drugModel.getStockUnitId()
            : resolvedTargetStockUnit.getUnitId();
        if (resolvedTargetStockUnit == null && StrUtil.isBlank(targetStockUnit)) {
            targetStockUnitId = null;
        }
        if (StrUtil.isBlank(targetStockUnit)
            && (updateCommand == null
            || updateCommand.getStockAlertThreshold() != null
            || beforeQuantity.compareTo(BigDecimal.ZERO) > 0)) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_UNIT_REQUIRED);
        }

        BigDecimal beforeThreshold = drugModel.getStockAlertThreshold();
        String beforeStockUnit = drugModel.getStockUnit();
        drugModel.setStockUnitId(targetStockUnitId);
        drugModel.setStockUnit(targetStockUnit);
        drugModel.setStockAlertThreshold(updateCommand == null ? drugModel.getStockAlertThreshold()
            : updateCommand.getStockAlertThreshold());
        if (drugModel.getLowStockNotified() == null) {
            drugModel.setLowStockNotified(Boolean.FALSE);
        }
        drugService.updateById(drugModel);
        drugModel.setStockQuantity(beforeQuantity);

        String remark = StrUtil.format("修改库存配置：预警值 {} -> {}，库存单位 {} -> {}",
            Objects.toString(beforeThreshold, "null"),
            Objects.toString(drugModel.getStockAlertThreshold(), "null"),
            StrUtil.blankToDefault(beforeStockUnit, "null"),
            StrUtil.blankToDefault(targetStockUnit, "null"));
        saveStockLog(drugModel, STOCK_CHANGE_TYPE_ALERT_THRESHOLD_UPDATE, beforeQuantity, BigDecimal.ZERO,
            beforeQuantity, null, null, limitLength(remark, 255));
        handleLowStockAlertAfterStockChange(drugModel, null, null);
        return buildDrugDTO(drugModel);
    }

    /**
     * 修改药品。
     *
     * <p>这里仍然允许修改药品档案字段，但库存变更链路统一收敛到专用库存接口，
     * 以免前端一次“编辑药品基本信息”顺手把库存绝对值直接覆盖掉。
     *
     * @param currentUserId 当前登录 App 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDrug(UpdateDrugCommand updateCommand, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(updateCommand.getDrugId());
        drugModel.checkOwnedByUser(currentUserId);

        fillRealtimeStockQuantity(drugModel);
        Long sourceDrugId = drugModel.getSourceDrugId();
        BigDecimal stockQuantity = drugModel.getStockQuantity();
        Long stockUnitId = drugModel.getStockUnitId();
        String stockUnit = drugModel.getStockUnit();
        BigDecimal stockAlertThreshold = drugModel.getStockAlertThreshold();
        Boolean lowStockNotified = drugModel.getLowStockNotified();
        Date lowStockNotifyTime = drugModel.getLowStockNotifyTime();

        drugModel.loadUpdateCommand(updateCommand, currentUserId);
        drugModel.setSourceDrugId(sourceDrugId);
        drugModel.setStockQuantity(stockQuantity);
        drugModel.setStockUnitId(stockUnitId);
        drugModel.setStockUnit(stockUnit);
        drugModel.setStockAlertThreshold(stockAlertThreshold);
        drugModel.setLowStockNotified(lowStockNotified);
        drugModel.setLowStockNotifyTime(lowStockNotifyTime);
        resolveDrugReferenceFields(drugModel);
        drugModel.checkDrugNameUnique();
        drugModel.checkFields();
        drugModel.updateById();
    }

    /**
     * 删除药品。
     * 当前使用逻辑删除，避免直接丢失历史计划引用过的药品主数据。
     *
     * @param currentUserId 当前登录 App 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeDrug(Long drugId, Long currentUserId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkOwnedByUser(currentUserId);
        drugService.removeById(drugId);
    }

    /**
     * 根据提醒反馈结果自动扣减库存。
     *
     * <p>当前只对“当前用户自己的个人药品 + 已启用库存跟踪”的场景生效：
     * 1. 系统药品不会直接维护个人库存
     * 2. 自定义药名没有对应库存主档
     * 3. 没有开启库存跟踪的个人药品也不会扣减
     *
     * <p>库存扣减按最早效期优先消费，确保库存展示顺序和实际消费顺序一致。
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeDrugStockAfterReminderTaken(Long ownerUserId, Long planId, Long reminderId,
        BigDecimal doseAmount, String doseUnit) {
        if (ownerUserId == null || planId == null || doseAmount == null || doseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        HealthMedicationPlanEntity planEntity = medicationPlanService.getById(planId);
        if (planEntity == null || planEntity.getDrugId() == null) {
            return;
        }

        DrugModel drugModel = drugModelFactory.loadById(planEntity.getDrugId());
        if (!drugModel.isPersonalDrug() || !Objects.equals(drugModel.getOwnerUserId(), ownerUserId)
            || !drugModel.isStockTrackingEnabled()) {
            return;
        }

        // 理论上计划创建/修改阶段已经保证单位一致，这里仍做一次兜底保护，
        // 避免历史脏数据导致扣减逻辑抛错并影响“已服药”反馈主流程。
        if (StrUtil.isBlank(doseUnit) || !Objects.equals(StrUtil.trim(doseUnit), StrUtil.trim(drugModel.getStockUnit()))) {
            return;
        }

        BigDecimal beforeQuantity = fillRealtimeStockQuantity(drugModel);
        BigDecimal actualDecreaseQuantity = deductRealtimeStockByExpireOrder(drugModel, doseAmount);
        BigDecimal afterQuantity = fillRealtimeStockQuantity(drugModel);
        String operationRemark = actualDecreaseQuantity.compareTo(doseAmount) < 0
            ? StrUtil.format("提醒反馈自动扣减库存时库存不足，计划剂量 {}{}，当前库存已按 0 归零",
                stripTrailingZero(doseAmount), doseUnit)
            : StrUtil.format("标记已服药后按效期顺序自动扣减库存 {}{}", stripTrailingZero(doseAmount), doseUnit);

        saveStockLog(drugModel, STOCK_CHANGE_TYPE_REMINDER_TAKE_DEDUCT, beforeQuantity, actualDecreaseQuantity.negate(),
            afterQuantity, planEntity.getPlanId(), reminderId, limitLength(operationRemark, 255));
        handleLowStockAlertAfterStockChange(drugModel, planEntity.getPlanId(), reminderId);
    }

    /**
     * 撤销“已服药”反馈后，把本次自动扣减的库存补回药柜。
     *
     * <p>这里沿用和扣减时相同的前置约束：
     * 1. 只处理个人药品
     * 2. 只处理启用了库存跟踪的药品
     * 3. 只处理计划剂量单位与库存单位一致的场景
     *
     * <p>由于当前提醒反馈不会记录“具体是从哪个批次扣掉的”，
     * 所以撤销时无法 100% 还原到原先被扣减的那个批次。
     *
     * <p>这里采用一个更稳妥的回补策略：
     * 1. 优先回补到当前药品历史上最早的库存桶，尽量保持 FEFO 顺序稳定
     * 2. 如果历史批次都不存在，再创建一个“系统补回桶”兜底
     *
     * <p>这样既能兼容当前 PostgreSQL 下 `expire_date` 必填的约束，
     * 也能保证撤销“已服药”后，药柜总库存和提醒反馈重新保持一致。
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreDrugStockAfterReminderTakeReverted(Long ownerUserId, Long planId, Long reminderId,
        BigDecimal doseAmount, String doseUnit) {
        if (ownerUserId == null || planId == null || doseAmount == null || doseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        HealthMedicationPlanEntity planEntity = medicationPlanService.getById(planId);
        if (planEntity == null || planEntity.getDrugId() == null) {
            return;
        }

        DrugModel drugModel = drugModelFactory.loadById(planEntity.getDrugId());
        if (!drugModel.isPersonalDrug() || !Objects.equals(drugModel.getOwnerUserId(), ownerUserId)
            || !drugModel.isStockTrackingEnabled()) {
            return;
        }

        if (StrUtil.isBlank(doseUnit) || !Objects.equals(StrUtil.trim(doseUnit), StrUtil.trim(drugModel.getStockUnit()))) {
            return;
        }

        BigDecimal beforeQuantity = fillRealtimeStockQuantity(drugModel);
        RestoreStockBatchBucket restoreBucket = resolveRestoreStockBatchBucket(drugModel);
        createOrIncreaseStockBatch(drugModel, restoreBucket.getBatchNo(), restoreBucket.getExpireDate(), doseAmount);
        BigDecimal afterQuantity = fillRealtimeStockQuantity(drugModel);
        String operationRemark = StrUtil.format("撤销已服药后回补库存 {}{}", stripTrailingZero(doseAmount), doseUnit);
        saveStockLog(drugModel, STOCK_CHANGE_TYPE_REMINDER_TAKE_RESTORE, beforeQuantity, doseAmount, afterQuantity,
            planEntity.getPlanId(), reminderId, limitLength(operationRemark, 255));
        handleLowStockAlertAfterStockChange(drugModel, planEntity.getPlanId(), reminderId);
    }

    /**
     * 获取系统下发药品详情。
     * 后台详情页只允许查询系统药品，避免管理员通过这个入口访问用户个人药品数据。
     */
    public DrugDTO getSystemDrugInfo(Long drugId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkSystemDrug();
        return buildDrugDTO(drugModel);
    }

    /**
     * 新增系统下发药品。
     * 归属固定为 0，表示该药品可被所有 App 用户在创建用药计划时直接使用。
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSystemDrug(AddDrugCommand addCommand) {
        DrugModel drugModel = drugModelFactory.create();
        drugModel.loadAddCommand(addCommand, DrugQuery.SYSTEM_DRUG_OWNER_ID);
        resolveDrugReferenceFields(drugModel);
        drugModel.checkDrugNameUnique();
        drugModel.checkFields();
        drugModel.insert();
    }

    /**
     * 修改系统下发药品。
     * 后台只允许修改系统药品自身，不允许借此改写任何用户私人药品。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSystemDrug(UpdateDrugCommand updateCommand) {
        DrugModel drugModel = drugModelFactory.loadById(updateCommand.getDrugId());
        drugModel.checkSystemDrug();
        drugModel.loadUpdateCommand(updateCommand, DrugQuery.SYSTEM_DRUG_OWNER_ID);
        resolveDrugReferenceFields(drugModel);
        drugModel.checkDrugNameUnique();
        drugModel.checkFields();
        drugModel.updateById();
    }

    /**
     * 删除系统下发药品。
     * 当前仍使用逻辑删除，避免历史提醒、历史计划中的药品快照失去追溯来源。
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeSystemDrug(Long drugId) {
        DrugModel drugModel = drugModelFactory.loadById(drugId);
        drugModel.checkSystemDrug();
        drugService.removeById(drugId);
    }

    /**
     * 当库存发生变化后，统一刷新低库存提醒状态。
     *
     * <p>这里专门做成“有状态提醒”，而不是每次库存变动都重复发消息：
     * 1. 没有配置预警值则不提醒
     * 2. 库存恢复到安全区后，重置已提醒标记
     * 3. 只有从安全区进入预警区间且当前周期尚未提醒过时，才真正派发消息
     */
    private void handleLowStockAlertAfterStockChange(DrugModel drugModel, Long relatedPlanId, Long relatedReminderId) {
        fillRealtimeStockQuantity(drugModel);
        if (drugModel == null || !drugModel.isStockTrackingEnabled() || drugModel.getStockAlertThreshold() == null) {
            if (drugModel != null && (Boolean.TRUE.equals(drugModel.getLowStockNotified())
                || drugModel.getLowStockNotifyTime() != null)) {
                drugModel.setLowStockNotified(Boolean.FALSE);
                drugModel.setLowStockNotifyTime(null);
                drugService.updateById(drugModel);
            }
            return;
        }

        if (!drugModel.isBelowAlertThreshold()) {
            if (Boolean.TRUE.equals(drugModel.getLowStockNotified()) || drugModel.getLowStockNotifyTime() != null) {
                drugModel.setLowStockNotified(Boolean.FALSE);
                drugModel.setLowStockNotifyTime(null);
                drugService.updateById(drugModel);
            }
            return;
        }

        if (Boolean.TRUE.equals(drugModel.getLowStockNotified())) {
            if (hasSuccessfulLowStockAlert(drugModel)) {
                return;
            }
            // 历史实现中，只要消息记录创建成功就会把低库存周期标记为“已提醒”，
            // 即使设备 Push 实际失败也会被提前封口。这里在发现最近一次低库存消息并未成功送达时，
            // 主动把状态回滚，让库存变动或设备重新注册都还能继续补发。
            drugModel.setLowStockNotified(Boolean.FALSE);
            drugModel.setLowStockNotifyTime(null);
            drugService.updateById(drugModel);
        }
        dispatchLowStockAlertQuietly(drugModel, relatedPlanId, relatedReminderId);
    }

    /**
     * 低库存时创建消息中心记录并尝试走 App Push。
     *
     * <p>这里即使设备推送失败，也保留站内消息，让用户至少还能在消息中心看到提醒。
     */
    private void dispatchLowStockAlertQuietly(DrugModel drugModel, Long relatedPlanId, Long relatedReminderId) {
        Date notifyTime = new Date();
        boolean pushSuccess = false;
        HealthAppMessageEntity messageEntity = null;
        try {
            HealthAppPushPayloadDTO payloadDTO = buildLowStockPushPayload(drugModel);
            messageEntity = prepareLowStockMessageForDispatch(drugModel, payloadDTO);
            if (messageEntity == null || messageEntity.getMessageId() == null) {
                return;
            }

            HealthAppMessageNotice notice = buildLowStockNotice(drugModel, messageEntity, payloadDTO);
            HealthAppMessageNotifyResult notifyResult = healthAppMessageNotifier.notify(notice);
            if (notifyResult != null && notifyResult.isSuccess()) {
                appMessageApplicationService.markMessageSendSuccess(messageEntity.getMessageId(),
                    notifyResult.getChannel(), notifyResult.getMessage());
                pushSuccess = true;
            } else if (messageEntity != null) {
                appMessageApplicationService.markMessageSendFailed(messageEntity.getMessageId(),
                    notifyResult == null ? null : notifyResult.getChannel(),
                    notifyResult == null ? "低库存提醒发送器未返回结果" : notifyResult.getMessage());
            }
        } catch (Exception ex) {
            if (messageEntity != null && messageEntity.getMessageId() != null) {
                appMessageApplicationService.markMessageSendFailed(messageEntity.getMessageId(), null, ex.getMessage());
            }
        }

        if (pushSuccess) {
            drugModel.setLowStockNotified(Boolean.TRUE);
            drugModel.setLowStockNotifyTime(notifyTime);
            drugService.updateById(drugModel);
        }
    }

    /**
     * 在设备重新登录或刷新 Token 后，补跑当前用户仍未完成的低库存提醒。
     *
     * <p>低库存提醒的主触发点仍然是库存变化，但真实业务里经常会出现下面这类时序：
     * 1. 药品先进入低库存
     * 2. 当时没有可用设备 Token，Push 失败
     * 3. 用户稍后重新登录，App 再次上报设备 Token
     *
     * <p>如果此时不主动补跑，当前这轮低库存周期就会永远停留在“消息存在但用户没收到 Push”的状态。
     * 因此这里在设备注册成功后，再扫描一次当前用户仍处于低库存的药品，把还没真正送达成功的提醒补发出去。
     *
     * <p>注意这里故意按药品逐条兜底捕获异常：
     * 某一条药品补发失败不能反过来影响设备注册主流程，更不能阻断同一用户其他药品的补发。
     */
    public void redrivePendingLowStockAlertsAfterDeviceRegistration(Long ownerUserId) {
        if (ownerUserId == null) {
            return;
        }

        List<DrugEntity> candidateDrugs = drugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, ownerUserId)
            .eq(DrugEntity::getStatus, StatusEnum.ENABLE.getValue())
            .isNotNull(DrugEntity::getStockUnit)
            .isNotNull(DrugEntity::getStockAlertThreshold)
            .list();
        if (candidateDrugs == null || candidateDrugs.isEmpty()) {
            return;
        }

        for (DrugEntity item : candidateDrugs) {
            DrugModel drugModel = new DrugModel(item, drugService);
            try {
                handleLowStockAlertAfterStockChange(drugModel, null, null);
            } catch (Exception ex) {
                log.warn("低库存提醒补发失败，ownerUserId={}, drugId={}, reason={}",
                    ownerUserId, item.getDrugId(), ex.getMessage());
            }
        }
    }

    /**
     * 仅通过站内消息派发近效期提醒。
     *
     * <p>和低库存提醒不同，这里明确不调用设备 Push 发送器：
     * 1. 用户要求“只提醒，不响铃通知”
     * 2. 消息中心和首页消息预览已经足够承载这类温和提醒
     * 3. 直接回写发送成功状态，可以让后台审计与前端展示保持一致
     */
    private boolean dispatchNearExpiryAlertQuietly(DrugEntity drugEntity, HealthDrugStockBatchEntity batchEntity, Date today) {
        HealthAppPushPayloadDTO payloadDTO = buildNearExpiryPayload(drugEntity, batchEntity, today);
        HealthAppMessageEntity messageEntity = appMessageApplicationService.createOrReuseMessage(
            buildNearExpiryMessageCreateRequest(drugEntity, batchEntity, payloadDTO));
        if (messageEntity == null || messageEntity.getMessageId() == null) {
            return false;
        }

        appMessageApplicationService.markMessageSendSuccess(
            messageEntity.getMessageId(),
            MESSAGE_CHANNEL_IN_APP,
            "近效期提醒仅写入站内消息，不派发设备通知");

        batchEntity.setNearExpireNotified(Boolean.TRUE);
        batchEntity.setNearExpireNotifyTime(new Date());
        drugStockBatchService.updateById(batchEntity);
        return true;
    }

    /**
     * 组装低库存消息创建请求。
     */
    private HealthAppMessageCreateRequest buildLowStockMessageCreateRequest(DrugModel drugModel,
        HealthAppPushPayloadDTO payloadDTO) {
        return HealthAppMessageCreateRequest.builder()
            .ownerUserId(drugModel.getOwnerUserId())
            .memberId(null)
            .memberName(null)
            .businessScene(HealthAppMessageSceneEnum.LOW_STOCK_ALERT.getValue())
            .businessId(drugModel.getDrugId())
            .messageTitle(payloadDTO == null ? null : payloadDTO.getTitle())
            .messageContent(payloadDTO == null ? null : payloadDTO.getContent())
            .payload(payloadDTO)
            // 低库存提醒允许“恢复安全后再次进入预警区”时重新创建一条新消息，
            // 因此不能直接用固定 dedupKey 全局去重。当前采用“按业务对象查询最近一条消息，
            // 如果上次发送未成功则复用，否则为下一轮预警创建新消息”的策略。
            .dedupKey(null)
            .build();
    }

    /**
     * 组装低库存发送通知。
     */
    private HealthAppMessageNotice buildLowStockNotice(DrugModel drugModel, HealthAppMessageEntity messageEntity,
        HealthAppPushPayloadDTO payloadDTO) {
        return HealthAppMessageNotice.builder()
            .messageId(messageEntity == null ? null : messageEntity.getMessageId())
            .ownerUserId(drugModel.getOwnerUserId())
            .businessScene(HealthAppMessageSceneEnum.LOW_STOCK_ALERT.getValue())
            .businessId(drugModel.getDrugId())
            .messageTitle(payloadDTO == null ? null : payloadDTO.getTitle())
            .messageContent(payloadDTO == null ? null : payloadDTO.getContent())
            .payload(payloadDTO)
            .build();
    }

    /**
     * 构建低库存消息的统一业务透传载荷。
     */
    private HealthAppPushPayloadDTO buildLowStockPushPayload(DrugModel drugModel) {
        HealthFollowUpNavigationDTO navigationDTO = buildLowStockNavigation(drugModel.getDrugId());
        HealthFollowUpRecommendedActionDTO actionDTO = buildLowStockRecommendedAction(drugModel, navigationDTO);

        HealthAppPushPayloadDTO payloadDTO = new HealthAppPushPayloadDTO();
        payloadDTO.setBusinessScene(HealthAppMessageSceneEnum.LOW_STOCK_ALERT.getValue());
        payloadDTO.setBusinessId(drugModel.getDrugId());
        payloadDTO.setTitle(StrUtil.format("{} 库存不足", StrUtil.blankToDefault(drugModel.getDrugName(), "药品")));
        payloadDTO.setContent(StrUtil.format("当前库存 {}{}，已达到预警值 {}{}，建议尽快补充。",
            stripTrailingZero(drugModel.getSafeStockQuantity()),
            StrUtil.blankToDefault(drugModel.getStockUnit(), ""),
            stripTrailingZero(drugModel.getStockAlertThreshold()),
            StrUtil.blankToDefault(drugModel.getStockUnit(), "")));
        payloadDTO.setNavigation(navigationDTO);
        payloadDTO.setRecommendedAction(actionDTO);
        return payloadDTO;
    }

    /**
     * 为本次低库存发送准备消息记录。
     *
     * <p>这里要同时满足两类诉求：
     * 1. 上一次低库存 Push 失败时，不要不断新增重复消息，而是复用原消息并累计重试次数
     * 2. 当上一轮低库存已经成功送达，且药品后续恢复安全再重新进入低库存时，允许重新生成一条新消息
     */
    private HealthAppMessageEntity prepareLowStockMessageForDispatch(DrugModel drugModel,
        HealthAppPushPayloadDTO payloadDTO) {
        HealthAppMessageCreateRequest createRequest = buildLowStockMessageCreateRequest(drugModel, payloadDTO);
        HealthAppMessageEntity latestMessage = getLatestLowStockMessage(drugModel);
        if (latestMessage == null
            || Objects.equals(latestMessage.getSendStatus(), HealthAppMessageSendStatusEnum.SUCCESS.getValue())) {
            return appMessageApplicationService.createOrReuseMessage(createRequest);
        }

        HealthAppMessageEntity refreshedMessage = appMessageApplicationService.refreshMessageSnapshot(
            latestMessage.getMessageId(), createRequest);
        return refreshedMessage == null ? appMessageApplicationService.createOrReuseMessage(createRequest)
            : refreshedMessage;
    }

    /**
     * 判断当前低库存周期是否已经完成成功送达。
     *
     * <p>这里显式把“模型状态字段”和“消息发送结果”两层一起判断：
     * 1. `lowStockNotified = true` 代表业务认为当前周期已经封口
     * 2. 最新一条低库存消息必须确实是发送成功，才允许维持这个封口状态
     *
     * <p>这样可以兼容修复历史脏数据：
     * 如果老逻辑曾在发送失败时错误地把 `lowStockNotified` 写成 `true`，
     * 新逻辑会识别出“状态字段已封口，但消息并未成功”，然后自动回滚并继续补发。
     */
    private boolean hasSuccessfulLowStockAlert(DrugModel drugModel) {
        if (drugModel == null || !Boolean.TRUE.equals(drugModel.getLowStockNotified())) {
            return false;
        }
        HealthAppMessageEntity latestMessage = getLatestLowStockMessage(drugModel);
        return latestMessage != null
            && Objects.equals(latestMessage.getSendStatus(), HealthAppMessageSendStatusEnum.SUCCESS.getValue());
    }

    /**
     * 查询当前药品最近一条低库存消息。
     */
    private HealthAppMessageEntity getLatestLowStockMessage(DrugModel drugModel) {
        if (drugModel == null || drugModel.getOwnerUserId() == null || drugModel.getDrugId() == null) {
            return null;
        }
        return appMessageApplicationService.getLatestMessageByBusiness(
            drugModel.getOwnerUserId(),
            HealthAppMessageSceneEnum.LOW_STOCK_ALERT.getValue(),
            drugModel.getDrugId());
    }

    /**
     * 组装近效期提醒消息创建请求。
     */
    private HealthAppMessageCreateRequest buildNearExpiryMessageCreateRequest(DrugEntity drugEntity,
        HealthDrugStockBatchEntity batchEntity, HealthAppPushPayloadDTO payloadDTO) {
        return HealthAppMessageCreateRequest.builder()
            .ownerUserId(batchEntity.getOwnerUserId())
            .memberId(null)
            .memberName(null)
            .businessScene(HealthAppMessageSceneEnum.NEAR_EXPIRY_ALERT.getValue())
            .businessId(drugEntity.getDrugId())
            .messageTitle(payloadDTO == null ? null : payloadDTO.getTitle())
            .messageContent(payloadDTO == null ? null : payloadDTO.getContent())
            .payload(payloadDTO)
            // 近效期提醒去重完全依赖批次表上的提醒状态字段控制，
            // 这样批次离开窗口后又重新进入时，仍然可以重新创建一条新消息。
            .dedupKey(null)
            .build();
    }

    /**
     * 构建近效期提醒业务载荷。
     */
    private HealthAppPushPayloadDTO buildNearExpiryPayload(DrugEntity drugEntity, HealthDrugStockBatchEntity batchEntity,
        Date today) {
        HealthFollowUpNavigationDTO navigationDTO = buildLowStockNavigation(drugEntity.getDrugId());
        HealthFollowUpRecommendedActionDTO actionDTO = buildNearExpiryRecommendedAction(drugEntity, batchEntity, today,
            navigationDTO);
        long remainingDays = DateUtil.betweenDay(today, DateUtil.beginOfDay(batchEntity.getExpireDate()), true);

        HealthAppPushPayloadDTO payloadDTO = new HealthAppPushPayloadDTO();
        payloadDTO.setBusinessScene(HealthAppMessageSceneEnum.NEAR_EXPIRY_ALERT.getValue());
        payloadDTO.setBusinessId(drugEntity.getDrugId());
        payloadDTO.setTitle(StrUtil.format("{} 即将到期", StrUtil.blankToDefault(drugEntity.getDrugName(), "药品")));
        payloadDTO.setContent(StrUtil.format("{}批次 {} 将于 {} 到期，当前剩余 {}{}，距到期还有 {} 天，请及时处理。",
            StrUtil.blankToDefault(drugEntity.getDrugName(), "该药品"),
            resolveBatchDisplayName(batchEntity),
            DateUtil.formatDate(batchEntity.getExpireDate()),
            stripTrailingZero(safeQuantity(batchEntity.getStockQuantity())),
            StrUtil.blankToDefault(drugEntity.getStockUnit(), ""),
            remainingDays));
        payloadDTO.setNavigation(navigationDTO);
        payloadDTO.setRecommendedAction(actionDTO);
        return payloadDTO;
    }

    /**
     * 构建低库存提醒的跳转参数。
     */
    private HealthFollowUpNavigationDTO buildLowStockNavigation(Long drugId) {
        if (drugId == null) {
            return null;
        }
        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetPageCode(HealthFollowUpTargetPageEnum.WORKSPACE_DRUG_CABINET.getValue());
        navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(
            HealthFollowUpTargetPageEnum.WORKSPACE_DRUG_CABINET.getValue()));
        navigationDTO.setTargetBizId(drugId);
        navigationDTO.setTargetBizType("DRUG");
        navigationDTO.setTargetTabCode("CABINET");
        navigationDTO.setTargetAnchorCode("STOCK");
        navigationDTO.setTargetAnchorName("库存信息");
        return navigationDTO;
    }

    /**
     * 构建近效期推荐动作。
     */
    private HealthFollowUpRecommendedActionDTO buildNearExpiryRecommendedAction(DrugEntity drugEntity,
        HealthDrugStockBatchEntity batchEntity, Date today, HealthFollowUpNavigationDTO navigationDTO) {
        long remainingDays = Math.max(0, DateUtil.betweenDay(today, DateUtil.beginOfDay(batchEntity.getExpireDate()), true));
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        actionDTO.setActionPriority(remainingDays <= 3 ? 1 : 2);
        fillRiskLevel(actionDTO, remainingDays <= 3 ? HealthFollowUpRiskLevelEnum.HIGH : HealthFollowUpRiskLevelEnum.MEDIUM);
        actionDTO.setTitle(remainingDays <= 3 ? "药品即将到期，请尽快处理" : "药品已进入近效期窗口");
        actionDTO.setContent(StrUtil.format("{}批次 {} 将于 {} 到期，当前库存 {}{}，建议尽快查看并决定是否优先使用或处理。",
            StrUtil.blankToDefault(drugEntity.getDrugName(), "该药品"),
            resolveBatchDisplayName(batchEntity),
            DateUtil.formatDate(batchEntity.getExpireDate()),
            stripTrailingZero(safeQuantity(batchEntity.getStockQuantity())),
            StrUtil.blankToDefault(drugEntity.getStockUnit(), "")));
        actionDTO.setActionText("查看药品");
        actionDTO.setNavigation(navigationDTO);
        return actionDTO;
    }

    /**
     * 判断该药品是否适合生成近效期提醒。
     *
     * <p>本次仅覆盖个人药柜真实可管理的药品：
     * 1. 药品主档必须存在
     * 2. 必须属于当前批次归属用户
     * 3. 系统药品公共主档不参与
     */
    private boolean isEligiblePersonalDrugForNearExpiryAlert(DrugEntity drugEntity, HealthDrugStockBatchEntity batchEntity) {
        return drugEntity != null
            && drugEntity.getDrugId() != null
            && batchEntity != null
            && Objects.equals(drugEntity.getDrugId(), batchEntity.getDrugId())
            && !Objects.equals(drugEntity.getOwnerUserId(), 0L)
            && Objects.equals(drugEntity.getOwnerUserId(), batchEntity.getOwnerUserId());
    }

    /**
     * 当批次离开近效期窗口后，重置当前提醒周期状态。
     */
    private void resetNearExpiryAlertStateIfNecessary(HealthDrugStockBatchEntity batchEntity) {
        if (batchEntity == null) {
            return;
        }
        if (!Boolean.TRUE.equals(batchEntity.getNearExpireNotified()) && batchEntity.getNearExpireNotifyTime() == null) {
            return;
        }
        batchEntity.setNearExpireNotified(Boolean.FALSE);
        batchEntity.setNearExpireNotifyTime(null);
        drugStockBatchService.updateById(batchEntity);
    }

    /**
     * 把批号为空的情况转换成稳定可读的批次展示名。
     */
    private String resolveBatchDisplayName(HealthDrugStockBatchEntity batchEntity) {
        if (batchEntity == null) {
            return "未命名批次";
        }
        if (Boolean.TRUE.equals(batchEntity.getDefaultBatch())) {
            return "默认批次";
        }
        return StrUtil.blankToDefault(StrUtil.trim(batchEntity.getBatchNo()), "未填写批号");
    }

    /**
     * 构建低库存推荐动作。
     */
    private HealthFollowUpRecommendedActionDTO buildLowStockRecommendedAction(DrugModel drugModel,
        HealthFollowUpNavigationDTO navigationDTO) {
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        actionDTO.setActionPriority(BigDecimal.ZERO.compareTo(drugModel.getSafeStockQuantity()) == 0 ? 1 : 2);
        fillRiskLevel(actionDTO, BigDecimal.ZERO.compareTo(drugModel.getSafeStockQuantity()) == 0
            ? HealthFollowUpRiskLevelEnum.HIGH
            : HealthFollowUpRiskLevelEnum.MEDIUM);
        actionDTO.setTitle(BigDecimal.ZERO.compareTo(drugModel.getSafeStockQuantity()) == 0 ? "当前已缺货，请尽快补库存" : "库存已经进入预警区间");
        actionDTO.setContent(StrUtil.format("{}当前库存为 {}{}，建议尽快补货，避免后续提醒执行时出现缺药。",
            StrUtil.blankToDefault(drugModel.getDrugName(), "该药品"),
            stripTrailingZero(drugModel.getSafeStockQuantity()),
            StrUtil.blankToDefault(drugModel.getStockUnit(), "")));
        actionDTO.setActionText("去补库存");
        actionDTO.setNavigation(navigationDTO);
        return actionDTO;
    }

    /**
     * 回填推荐动作风险等级字段。
     */
    private void fillRiskLevel(HealthFollowUpRecommendedActionDTO actionDTO, HealthFollowUpRiskLevelEnum riskLevelEnum) {
        if (actionDTO == null || riskLevelEnum == null) {
            return;
        }
        actionDTO.setRiskLevel(riskLevelEnum.getValue());
        actionDTO.setRiskLevelName(HealthAppI18n.followUpRiskLevelName(riskLevelEnum.getValue()));
        actionDTO.setRiskCssTag(riskLevelEnum.cssTag());
    }

    /**
     * 解析导入文本中的有效行。
     *
     * <p>这里会主动忽略：
     * 1. 空行
     * 2. 仅包含空白字符的行
     * 3. 以 `#` 开头的注释行
     *
     * <p>这样前端可以在导入弹窗里给出示例说明文字，
     * 用户也能在粘贴文本时保留少量注释，不影响真实导入。
     */
    private List<String> parseImportLines(ImportDrugCommand importCommand) {
        if (importCommand == null || StrUtil.isBlank(importCommand.getRawText())) {
            return Collections.emptyList();
        }

        return StrUtil.split(importCommand.getRawText(), '\n').stream()
            .map(line -> StrUtil.trim(line == null ? null : line.replace("\r", "")))
            .filter(StrUtil::isNotBlank)
            .filter(line -> !line.startsWith("#"))
            .collect(Collectors.toList());
    }

    /**
     * 判断当前用户的个人药品库中是否已经存在同名药品。
     */
    private boolean existsDrugName(Long currentUserId, String drugName) {
        return drugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, currentUserId)
            .eq(DrugEntity::getDrugName, drugName)
            .count() > 0;
    }

    /**
     * 查询当前用户是否已经引入过某条系统药品。
     */
    private DrugModel findImportedSystemDrug(Long currentUserId, Long sourceDrugId) {
        DrugEntity importedEntity = drugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, currentUserId)
            .eq(DrugEntity::getSourceDrugId, sourceDrugId)
            .one();
        return importedEntity == null ? null : new DrugModel(importedEntity, drugService);
    }

    /**
     * 根据系统药品构建个人药柜新增命令。
     *
     * <p>这里故意仍然走新增药品命令对象，
     * 这样可以统一复用药品名称、库存字段的同一套校验逻辑。
     */
    private AddDrugCommand buildImportSystemAddCommand(DrugModel sourceDrugModel, ImportSystemDrugCommand importCommand) {
        AddDrugCommand addCommand = new AddDrugCommand();
        addCommand.setDrugName(StrUtil.blankToDefault(
            normalizeText(importCommand == null ? null : importCommand.getTargetDrugName()),
            sourceDrugModel.getDrugName()));
        addCommand.setGenericName(sourceDrugModel.getGenericName());
        addCommand.setBrandName(sourceDrugModel.getBrandName());
        addCommand.setDosageForm(sourceDrugModel.getDosageForm());
        addCommand.setSpecification(sourceDrugModel.getSpecification());
        addCommand.setIndication(sourceDrugModel.getIndication());
        addCommand.setUsageInstruction(sourceDrugModel.getUsageInstruction());
        addCommand.setAdverseReaction(sourceDrugModel.getAdverseReaction());
        addCommand.setContraindication(sourceDrugModel.getContraindication());
        addCommand.setManufacturer(sourceDrugModel.getManufacturer());
        addCommand.setDrugType(sourceDrugModel.getDrugType());
        addCommand.setStatus(StatusEnum.ENABLE.getValue());
        addCommand.setStockQuantity(importCommand == null ? null : importCommand.getStockQuantity());
        addCommand.setBatchNo(normalizeText(importCommand == null ? null : importCommand.getBatchNo()));
        addCommand.setExpireDate(importCommand == null ? null : normalizeDate(importCommand.getExpireDate()));
        boolean hasImportUnitInput = importCommand != null
            && (importCommand.getStockUnitId() != null || StrUtil.isNotBlank(importCommand.getStockUnit()));
        addCommand.setStockUnitId(hasImportUnitInput
            ? importCommand.getStockUnitId()
            : sourceDrugModel.getStockUnitId());
        addCommand.setStockUnit(hasImportUnitInput
            ? normalizeText(importCommand.getStockUnit())
            : sourceDrugModel.getStockUnit());
        addCommand.setStockAlertThreshold(importCommand == null ? null : importCommand.getStockAlertThreshold());
        addCommand.setRemark(normalizeText(importCommand == null ? null : importCommand.getRemark()));
        // 药品图片是非强制字段，因此这里不能使用 `Objects.requireNonNullElse(...)`。
        // 原因是当“导入命令没传图片”且“系统药品本身也没有图片”时，两边都会是 null，
        // `requireNonNullElse` 会直接抛出 NPE，导致本应允许成功的“无图引入”场景失败。
        // 这里改成显式三元兜底：优先使用本次导入传入的图片，没有则沿用系统药品图片，
        // 如果系统药品也没有图片，就继续保持 null，后续在 DTO 回填阶段统一给默认图片地址。
        addCommand.setImageAttachmentId(importCommand != null && importCommand.getImageAttachmentId() != null
            ? importCommand.getImageAttachmentId()
            : sourceDrugModel.getImageAttachmentId());
        return addCommand;
    }

    /**
     * 根据导入列构建新增药品命令。
     *
     * <p>虽然当前是批量导入场景，但最终仍然统一落到新增药品命令对象，
     * 这样可以最大限度复用既有校验逻辑，避免批量导入和单条新增出现字段口径不一致。
     */
    private AddDrugCommand buildImportAddCommand(String[] columns, String defaultDrugType, Integer defaultStatus) {
        AddDrugCommand addCommand = new AddDrugCommand();
        addCommand.setDrugName(readImportColumn(columns, 0));
        addCommand.setGenericName(readImportColumn(columns, 1));
        addCommand.setSpecification(readImportColumn(columns, 2));
        addCommand.setDosageForm(readImportColumn(columns, 3));
        addCommand.setDrugType(StrUtil.blankToDefault(readImportColumn(columns, 4), defaultDrugType));
        addCommand.setUsageInstruction(readImportColumn(columns, 5));
        addCommand.setRemark(readImportColumn(columns, 6));
        addCommand.setStatus(defaultStatus);
        return addCommand;
    }

    /**
     * 为药品返回对象回填实时库存与批次明细。
     *
     * <p>这里统一从批次表读取库存，再反算 DTO 上的：
     * 1. `stockQuantity`
     * 2. `stockTrackingEnabled`
     * 3. `stockBelowAlert`
     * 4. `stockBatches`
     *
     * <p>这样所有查询接口都能使用同一套库存口径。
     */
    private void fillRealtimeStockData(List<DrugDTO> drugDTOList, boolean includeBatchDetails) {
        if (drugDTOList == null || drugDTOList.isEmpty()) {
            return;
        }

        List<Long> drugIds = drugDTOList.stream()
            .filter(Objects::nonNull)
            .filter(drugDTO -> !Boolean.TRUE.equals(drugDTO.getSystemDrug()))
            .map(DrugDTO::getDrugId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Map<Long, List<HealthDrugStockBatchEntity>> stockBatchMap = drugIds.isEmpty()
            ? Collections.emptyMap()
            : listPositiveStockBatchesByDrugIds(drugIds).stream()
                .collect(Collectors.groupingBy(HealthDrugStockBatchEntity::getDrugId));

        for (DrugDTO drugDTO : drugDTOList) {
            if (drugDTO == null) {
                continue;
            }
            if (Boolean.TRUE.equals(drugDTO.getSystemDrug())) {
                drugDTO.setStockQuantity(null);
                drugDTO.setStockTrackingEnabled(Boolean.FALSE);
                drugDTO.setStockBelowAlert(Boolean.FALSE);
                drugDTO.setStockBatches(Collections.emptyList());
                continue;
            }

            boolean stockTrackingEnabled = StrUtil.isNotBlank(drugDTO.getStockUnit());
            List<HealthDrugStockBatchEntity> sortedBatchEntities =
                sortStockBatches(stockBatchMap.getOrDefault(drugDTO.getDrugId(), Collections.emptyList()));
            BigDecimal totalStockQuantity = sumStockQuantity(sortedBatchEntities);

            drugDTO.setStockTrackingEnabled(stockTrackingEnabled);
            drugDTO.setStockQuantity(stockTrackingEnabled ? totalStockQuantity : null);
            drugDTO.setStockBelowAlert(stockTrackingEnabled
                && drugDTO.getStockAlertThreshold() != null
                && totalStockQuantity.compareTo(drugDTO.getStockAlertThreshold()) <= 0);
            drugDTO.setStockBatches(includeBatchDetails && stockTrackingEnabled
                ? sortedBatchEntities.stream().map(DrugStockBatchDTO::new).collect(Collectors.toList())
                : Collections.emptyList());
        }
    }

    /**
     * 回填药品图片地址。
     *
     * <p>这里统一由后端返回最终可展示图片地址：
     * 1. 上传过图片则返回附件表中的文件地址
     * 2. 未上传图片或附件已失效时统一回退默认药品图
     *
     * <p>这样前端列表、详情、卡片组件都可以直接使用同一个字段渲染。
     */
    private void fillDrugImageUrl(List<DrugDTO> drugDTOList) {
        if (drugDTOList == null || drugDTOList.isEmpty()) {
            return;
        }

        java.util.Set<Long> attachmentIds = drugDTOList.stream()
            .map(DrugDTO::getImageAttachmentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, AttachmentEntity> attachmentMap = attachmentIds.isEmpty()
            ? Collections.emptyMap()
            : attachmentService.lambdaQuery()
                .in(AttachmentEntity::getAttachmentId, attachmentIds)
                .list()
                .stream()
                .collect(Collectors.toMap(AttachmentEntity::getAttachmentId, entity -> entity));

        for (DrugDTO drugDTO : drugDTOList) {
            if (drugDTO == null) {
                continue;
            }
            AttachmentEntity attachmentEntity = drugDTO.getImageAttachmentId() == null
                ? null
                : attachmentMap.get(drugDTO.getImageAttachmentId());
            drugDTO.setImageUrl(attachmentEntity == null
                ? DEFAULT_DRUG_IMAGE_URL
                : StrUtil.blankToDefault(FileUploadUtils.getAccessUrl(attachmentEntity.getFileUrl()),
                    DEFAULT_DRUG_IMAGE_URL));
        }
    }

    /**
     * 构建带实时库存信息的药品 DTO。
     */
    private DrugDTO buildDrugDTO(DrugEntity entity) {
        ensureDrugCode(entity);
        DrugDTO drugDTO = new DrugDTO(entity);
        fillRealtimeStockData(Collections.singletonList(drugDTO), true);
        fillDrugImageUrl(Collections.singletonList(drugDTO));
        return drugDTO;
    }

    /**
     * 确保药品已经拥有可展示的业务编码。
     *
     * <p>因为系统药品、个人药品、系统引入药品共用同一张主表，
     * 所以统一使用同一套 `DRG` 前缀即可满足展示和排障诉求。
     */
    private void ensureDrugCode(DrugEntity entity) {
        if (entity == null || entity.getDrugId() == null) {
            return;
        }
        String targetDrugCode = HealthBizCodeFormatter.formatDrugCode(entity.getDrugId());
        if (Objects.equals(entity.getDrugCode(), targetDrugCode)) {
            return;
        }
        entity.setDrugCode(targetDrugCode);
        drugService.updateById(entity);
    }

    /**
     * 回填药品当前实时库存总量。
     *
     * <p>因为药品主表不保存汇总库存，所以每次进行提醒判断、流水记录或接口返回前，
     * 都要先把批次库存汇总为运行时库存值。
     */
    private BigDecimal fillRealtimeStockQuantity(DrugModel drugModel) {
        if (drugModel == null) {
            return BigDecimal.ZERO;
        }
        if (!drugModel.isPersonalDrug() || drugModel.getDrugId() == null) {
            drugModel.setStockQuantity(null);
            return BigDecimal.ZERO;
        }

        BigDecimal totalStockQuantity = sumStockQuantity(
            listPositiveStockBatches(drugModel.getOwnerUserId(), drugModel.getDrugId()));
        drugModel.setStockQuantity(totalStockQuantity);
        return totalStockQuantity;
    }

    /**
     * 统一解析药品引用型字段。
     *
     * <p>当前主要包括两类：
     * 1. 标准单位ID -> 单位名称快照
     * 2. 图片附件ID -> 附件存在性校验
     *
     * <p>把这一步放在应用服务里做的原因是：
     * 1. Controller 只负责接参，不承载跨表解析逻辑
     * 2. Model 只负责领域规则，不直接依赖单位表、附件表
     */
    private void resolveDrugReferenceFields(DrugModel drugModel) {
        if (drugModel == null) {
            return;
        }

        ResolvedStockUnit resolvedStockUnit = resolveStockUnitSnapshot(
            drugModel.getStockUnitId(), drugModel.getStockUnit());
        drugModel.setStockUnitId(resolvedStockUnit == null ? null : resolvedStockUnit.getUnitId());
        drugModel.setStockUnit(resolvedStockUnit == null ? null : resolvedStockUnit.getUnitName());

        validateAttachmentExists(drugModel.getImageAttachmentId());
    }

    /**
     * 创建新增/引入时的首批库存明细。
     */
    private void createInitialStockBatchIfNecessary(DrugModel drugModel, String batchNo, Date expireDate) {
        if (drugModel == null || !drugModel.isStockTrackingEnabled()) {
            return;
        }
        BigDecimal initialStockQuantity = drugModel.getSafeStockQuantity();
        if (initialStockQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        createOrIncreaseStockBatch(drugModel, batchNo, expireDate, initialStockQuantity);
    }

    /**
     * 创建或合并某个“批号 + 效期”的库存明细。
     *
     * <p>如果用户后续补库存时录入了相同的批号和效期，则直接累加到原有批次；
     * 如果没有找到相同的库存桶，则新增一条批次明细。
     */
    private void createOrIncreaseStockBatch(DrugModel drugModel, String batchNo, Date expireDate,
        BigDecimal increaseQuantity) {
        if (drugModel == null || drugModel.getDrugId() == null || increaseQuantity == null
            || increaseQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String normalizedBatchNo = normalizeText(batchNo);
        Date normalizedExpireDate = normalizeDate(expireDate);
        boolean defaultBatch = isDefaultBatch(normalizedBatchNo, normalizedExpireDate);
        HealthDrugStockBatchEntity existingBatchEntity = findStockBatch(
            drugModel.getOwnerUserId(), drugModel.getDrugId(), normalizedBatchNo, normalizedExpireDate, defaultBatch);
        if (existingBatchEntity != null) {
            existingBatchEntity.setStockQuantity(safeQuantity(existingBatchEntity.getStockQuantity()).add(increaseQuantity));
            drugStockBatchService.updateById(existingBatchEntity);
            return;
        }

        HealthDrugStockBatchEntity batchEntity = new HealthDrugStockBatchEntity();
        batchEntity.setOwnerUserId(drugModel.getOwnerUserId());
        batchEntity.setDrugId(drugModel.getDrugId());
        batchEntity.setBatchNo(normalizedBatchNo);
        batchEntity.setExpireDate(normalizedExpireDate);
        batchEntity.setDefaultBatch(defaultBatch);
        batchEntity.setStockQuantity(increaseQuantity);
        drugStockBatchService.save(batchEntity);
    }

    /**
     * 按效期顺序实时扣减库存。
     *
     * <p>扣减规则固定为 FEFO（先到期先出）：
     * 1. 先扣最早到期的批次
     * 2. 相同效期下再按批号和批次ID稳定排序
     * 3. 如果总库存不足，不阻断主流程，只扣到 0 为止
     *
     * @return 实际成功扣减的库存数量
     */
    private BigDecimal deductRealtimeStockByExpireOrder(DrugModel drugModel, BigDecimal requiredQuantity) {
        if (drugModel == null || drugModel.getDrugId() == null || requiredQuantity == null
            || requiredQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal remainingQuantity = requiredQuantity;
        List<HealthDrugStockBatchEntity> availableBatchEntities =
            listPositiveStockBatches(drugModel.getOwnerUserId(), drugModel.getDrugId());
        for (HealthDrugStockBatchEntity batchEntity : availableBatchEntities) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal currentBatchQuantity = safeQuantity(batchEntity.getStockQuantity());
            if (currentBatchQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal decreaseQuantity = currentBatchQuantity.min(remainingQuantity);
            batchEntity.setStockQuantity(currentBatchQuantity.subtract(decreaseQuantity));
            drugStockBatchService.updateById(batchEntity);
            remainingQuantity = remainingQuantity.subtract(decreaseQuantity);
        }
        return requiredQuantity.subtract(remainingQuantity);
    }

    /**
     * 按“默认批次优先，其余批次 FEFO”实时扣减库存。
     *
     * <p>这套规则专门给临时用药使用，原因是：
     * 1. 家庭常备药最常见的是没有批号和效期的默认批次
     * 2. 用户如果明确录入了批号和效期，通常更希望这些精细批次留给计划用药或精确管理
     * 3. 先消费默认批次，能更符合“先把粗粒度库存桶用掉”的直觉
     */
    private BigDecimal deductRealtimeStockWithDefaultBatchFirst(DrugModel drugModel, BigDecimal requiredQuantity) {
        if (drugModel == null || drugModel.getDrugId() == null || requiredQuantity == null
            || requiredQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        List<HealthDrugStockBatchEntity> availableBatchEntities =
            listPositiveStockBatches(drugModel.getOwnerUserId(), drugModel.getDrugId());
        if (availableBatchEntities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<HealthDrugStockBatchEntity> deductionOrder = new ArrayList<>(availableBatchEntities.size());
        deductionOrder.addAll(availableBatchEntities.stream()
            .filter(batchEntity -> Boolean.TRUE.equals(batchEntity.getDefaultBatch()))
            .collect(Collectors.toList()));
        deductionOrder.addAll(availableBatchEntities.stream()
            .filter(batchEntity -> !Boolean.TRUE.equals(batchEntity.getDefaultBatch()))
            .collect(Collectors.toList()));
        return deductFromBatchSequence(deductionOrder, requiredQuantity);
    }

    /**
     * 按给定批次顺序逐个扣减库存。
     *
     * <p>把真正的逐桶扣减逻辑抽出来后：
     * 1. FEFO 扣减
     * 2. 默认批次优先扣减
     * 都能复用同一套“更新库存并回写批次”的实现，避免两套代码再出现细节漂移。
     */
    private BigDecimal deductFromBatchSequence(List<HealthDrugStockBatchEntity> batchEntities, BigDecimal requiredQuantity) {
        if (batchEntities == null || batchEntities.isEmpty() || requiredQuantity == null
            || requiredQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal remainingQuantity = requiredQuantity;
        for (HealthDrugStockBatchEntity batchEntity : batchEntities) {
            if (batchEntity == null || remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal currentBatchQuantity = safeQuantity(batchEntity.getStockQuantity());
            if (currentBatchQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal decreaseQuantity = currentBatchQuantity.min(remainingQuantity);
            batchEntity.setStockQuantity(currentBatchQuantity.subtract(decreaseQuantity));
            drugStockBatchService.updateById(batchEntity);
            remainingQuantity = remainingQuantity.subtract(decreaseQuantity);
        }
        return requiredQuantity.subtract(remainingQuantity);
    }

    /**
     * 查询某个药品当前仍有库存的批次列表。
     *
     * <p>这里会过滤掉库存已经扣到 0 的批次，只保留真正还可展示、还可参与后续扣减的库存明细。
     */
    private List<HealthDrugStockBatchEntity> listPositiveStockBatches(Long ownerUserId, Long drugId) {
        if (ownerUserId == null || drugId == null) {
            return Collections.emptyList();
        }
        return sortStockBatches(drugStockBatchService.lambdaQuery()
            .eq(HealthDrugStockBatchEntity::getOwnerUserId, ownerUserId)
            .eq(HealthDrugStockBatchEntity::getDrugId, drugId)
            .gt(HealthDrugStockBatchEntity::getStockQuantity, BigDecimal.ZERO)
            .list());
    }

    /**
     * 批量查询多条药品当前仍有库存的批次列表。
     */
    private List<HealthDrugStockBatchEntity> listPositiveStockBatchesByDrugIds(List<Long> drugIds) {
        if (drugIds == null || drugIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sortStockBatches(drugStockBatchService.lambdaQuery()
            .in(HealthDrugStockBatchEntity::getDrugId, drugIds)
            .gt(HealthDrugStockBatchEntity::getStockQuantity, BigDecimal.ZERO)
            .list());
    }

    /**
     * 为“撤销已服药”解析库存回补目标桶。
     *
     * <p>优先级如下：
     * 1. 优先复用该药品历史上的默认批次
     * 2. 如果没有默认批次，再复用历史上最早的普通库存桶
     * 3. 如果历史批次已经完全没有记录，则退回到“默认批次兜底桶”
     *
     * <p>第二种情况理论上只会出现在历史脏数据或人工清理批次后，
     * 因此这里直接回补到默认批次，先保证撤销链路可用且不再强制伪造效期。
     */
    private RestoreStockBatchBucket resolveRestoreStockBatchBucket(DrugModel drugModel) {
        if (drugModel == null || drugModel.getOwnerUserId() == null || drugModel.getDrugId() == null) {
            return new RestoreStockBatchBucket(null, null, true);
        }

        HealthDrugStockBatchEntity defaultBatchEntity = drugStockBatchService.lambdaQuery()
            .eq(HealthDrugStockBatchEntity::getOwnerUserId, drugModel.getOwnerUserId())
            .eq(HealthDrugStockBatchEntity::getDrugId, drugModel.getDrugId())
            .eq(HealthDrugStockBatchEntity::getDefaultBatch, toSmallintFlag(true))
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
        if (defaultBatchEntity != null) {
            return new RestoreStockBatchBucket(defaultBatchEntity.getBatchNo(), defaultBatchEntity.getExpireDate(), true);
        }

        HealthDrugStockBatchEntity earliestBatchEntity = drugStockBatchService.lambdaQuery()
            .eq(HealthDrugStockBatchEntity::getOwnerUserId, drugModel.getOwnerUserId())
            .eq(HealthDrugStockBatchEntity::getDrugId, drugModel.getDrugId())
            .eq(HealthDrugStockBatchEntity::getDefaultBatch, toSmallintFlag(false))
            .orderByAsc(HealthDrugStockBatchEntity::getExpireDate)
            .orderByAsc(HealthDrugStockBatchEntity::getBatchNo)
            .orderByAsc(HealthDrugStockBatchEntity::getBatchId)
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
        if (earliestBatchEntity != null) {
            return new RestoreStockBatchBucket(earliestBatchEntity.getBatchNo(), earliestBatchEntity.getExpireDate(),
                Boolean.TRUE.equals(earliestBatchEntity.getDefaultBatch()));
        }

        return new RestoreStockBatchBucket(null, null, true);
    }

    /**
     * 查询某个“药品 + 批号 + 效期”对应的库存桶。
     *
     * <p>批号允许为空，因此查询时需要同时兼容：
     * 1. 非空批号直接等值匹配
     * 2. 空批号走 `IS NULL`
     */
    private HealthDrugStockBatchEntity findStockBatch(Long ownerUserId, Long drugId, String batchNo, Date expireDate,
        boolean defaultBatch) {
        if (ownerUserId == null || drugId == null) {
            return null;
        }
        if (expireDate != null) {
            return drugStockBatchService.lambdaQuery()
                .eq(HealthDrugStockBatchEntity::getOwnerUserId, ownerUserId)
                .eq(HealthDrugStockBatchEntity::getDrugId, drugId)
                .eq(HealthDrugStockBatchEntity::getDefaultBatch, toSmallintFlag(defaultBatch))
                .eq(HealthDrugStockBatchEntity::getExpireDate, expireDate)
                .eq(StrUtil.isNotBlank(batchNo), HealthDrugStockBatchEntity::getBatchNo, batchNo)
                .isNull(StrUtil.isBlank(batchNo), HealthDrugStockBatchEntity::getBatchNo)
                .page(new Page<>(1, 1))
                .getRecords()
                .stream()
                .findFirst()
                .orElse(null);
        } else {
            return drugStockBatchService.lambdaQuery()
                .eq(HealthDrugStockBatchEntity::getOwnerUserId, ownerUserId)
                .eq(HealthDrugStockBatchEntity::getDrugId, drugId)
                .eq(HealthDrugStockBatchEntity::getDefaultBatch, toSmallintFlag(defaultBatch))
                .isNull(HealthDrugStockBatchEntity::getExpireDate)
                .eq(StrUtil.isNotBlank(batchNo), HealthDrugStockBatchEntity::getBatchNo, batchNo)
                .isNull(StrUtil.isBlank(batchNo), HealthDrugStockBatchEntity::getBatchNo)
                .page(new Page<>(1, 1))
                .getRecords()
                .stream()
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * 把 Java 布尔语义转换成当前数据库里约定的 SMALLINT 标记值。
     *
     * <p>当前药品批次表仍使用 `SMALLINT` 保存布尔型状态：
     * 1. `1` 表示 true
     * 2. `0` 表示 false
     *
     * <p>单独抽一个方法的原因是：
     * 1. 避免业务代码到处散落魔法值 `0/1`
     * 2. 后续如果表结构统一升级成 PostgreSQL 原生 boolean，这里会成为最明确的收口点
     * 3. 让开发人员看到查询条件时能马上理解“为什么这里不用 Boolean.TRUE/FALSE”
     */
    private short toSmallintFlag(boolean value) {
        return (short) (value ? 1 : 0);
    }

    /**
     * 校验新增/引入时的首批库存输入。
     *
     * <p>规则如下：
     * 1. 只有真正录入了库存数量时，才允许带批号/效期
     * 2. 默认批次允许批号和效期都为空
     * 3. 批号和效期也允许只填其中一个，用于兼容用户只掌握部分批次信息的场景
     */
    private void validateInitialStockBatchInput(BigDecimal stockQuantity, String batchNo, Date expireDate) {
        boolean hasPositiveStockQuantity = stockQuantity != null && stockQuantity.compareTo(BigDecimal.ZERO) > 0;
        boolean hasBatchDetail = StrUtil.isNotBlank(batchNo) || expireDate != null;
        if (!hasPositiveStockQuantity && hasBatchDetail) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_BATCH_DETAIL_REQUIRE_QUANTITY);
        }
    }

    /**
     * 校验补库存时的批次输入。
     *
     * <p>补库存既可以录入普通批次，也可以直接录入默认批次，
     * 因此这里不再强制要求效期必填，只校验数量本身。
     */
    private void validateIncreaseStockBatchInput(IncreaseDrugStockCommand increaseCommand) {
        if (increaseCommand == null || increaseCommand.getIncreaseQuantity() == null
            || increaseCommand.getIncreaseQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_BATCH_DETAIL_REQUIRE_QUANTITY);
        }
    }

    /**
     * 校验临时用药请求体。
     */
    private void validateTemporaryUseCommand(TemporaryUseDrugStockCommand temporaryUseCommand) {
        if (temporaryUseCommand == null || temporaryUseCommand.getUsedQuantity() == null
            || temporaryUseCommand.getUsedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_STOCK_BATCH_DETAIL_REQUIRE_QUANTITY);
        }
    }

    /**
     * 校验当前药品是否允许登记临时用药。
     *
     * <p>临时用药的前提是“当前药品本身就处于库存跟踪模式”，
     * 否则用户登记了一次临时用药，系统却没有库存单位和库存基准可扣，会让结果语义不完整。
     */
    private void checkTemporaryUseStockTracking(DrugModel drugModel) {
        if (drugModel == null || !drugModel.isStockTrackingEnabled()) {
            throw new ApiException(ErrorCode.Business.HEALTH_DRUG_TEMPORARY_USE_REQUIRE_STOCK_TRACKING);
        }
    }

    /**
     * 校验临时用药选择的家庭成员是否属于当前用户。
     */
    private void validateTemporaryUseMemberOwnership(Long ownerUserId, Long memberId) {
        if (ownerUserId == null || memberId == null) {
            return;
        }
        long matchedCount = familyMemberService.lambdaQuery()
            .eq(com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity::getOwnerUserId, ownerUserId)
            .eq(com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity::getMemberId, memberId)
            .count();
        if (matchedCount <= 0) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberId, "家庭成员");
        }
    }

    /**
     * 根据请求构建临时用药记录实体。
     *
     * <p>这里把“成员、时间、症状、备注”和“实际扣减数量”一起固化下来，
     * 方便后续做临时用药历史展示时不必再回放库存流水推断。
     */
    private HealthDrugTemporaryMedicationRecordEntity buildTemporaryMedicationRecord(DrugModel drugModel,
        TemporaryUseDrugStockCommand temporaryUseCommand, BigDecimal deductedQuantity) {
        HealthDrugTemporaryMedicationRecordEntity recordEntity = new HealthDrugTemporaryMedicationRecordEntity();
        recordEntity.setOwnerUserId(drugModel.getOwnerUserId());
        recordEntity.setMemberId(temporaryUseCommand == null ? null : temporaryUseCommand.getMemberId());
        recordEntity.setDrugId(drugModel.getDrugId());
        recordEntity.setUsedQuantity(temporaryUseCommand == null ? null : temporaryUseCommand.getUsedQuantity());
        recordEntity.setStockUnitSnapshot(drugModel.getStockUnit());
        recordEntity.setUseTime(normalizeDateTime(temporaryUseCommand == null ? null : temporaryUseCommand.getUseTime()));
        recordEntity.setSymptom(limitLength(normalizeText(temporaryUseCommand == null ? null : temporaryUseCommand.getSymptom()),
            100));
        recordEntity.setRemark(limitLength(normalizeText(temporaryUseCommand == null ? null : temporaryUseCommand.getRemark()),
            255));
        recordEntity.setDeductedQuantity(deductedQuantity);
        return recordEntity;
    }

    /**
     * 生成临时用药库存流水备注。
     *
     * <p>库存流水仍然要保持人能直接读懂，
     * 因此这里把“扣了多少 + 因为什么临时使用”拼成一条稳定文案，便于后台排障。
     */
    private String buildTemporaryUseRemark(TemporaryUseDrugStockCommand temporaryUseCommand, BigDecimal deductedQuantity,
        String stockUnit) {
        String symptom = normalizeText(temporaryUseCommand == null ? null : temporaryUseCommand.getSymptom());
        String baseText = StrUtil.format("登记临时用药并扣减库存 {}{}", stripTrailingZero(deductedQuantity), stockUnit);
        if (StrUtil.isBlank(symptom)) {
            return baseText;
        }
        return StrUtil.format("{}，用途/症状：{}", baseText, symptom);
    }

    /**
     * 判断当前批次参数是否代表默认批次。
     */
    private boolean isDefaultBatch(String batchNo, Date expireDate) {
        return StrUtil.isBlank(batchNo) && expireDate == null;
    }

    /**
     * 对批次列表做统一排序，保证展示和扣减使用同一顺序。
     */
    private List<HealthDrugStockBatchEntity> sortStockBatches(List<HealthDrugStockBatchEntity> batchEntities) {
        if (batchEntities == null || batchEntities.isEmpty()) {
            return Collections.emptyList();
        }
        return batchEntities.stream()
            .filter(Objects::nonNull)
            .sorted(STOCK_BATCH_SORT_COMPARATOR)
            .collect(Collectors.toList());
    }

    /**
     * 汇总批次库存总量。
     */
    private BigDecimal sumStockQuantity(List<HealthDrugStockBatchEntity> batchEntities) {
        if (batchEntities == null || batchEntities.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return batchEntities.stream()
            .map(HealthDrugStockBatchEntity::getStockQuantity)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 保存库存流水。
     */
    private void saveStockLog(DrugModel drugModel, String changeType, BigDecimal beforeQuantity, BigDecimal changeQuantity,
        BigDecimal afterQuantity, Long relatedPlanId, Long relatedReminderId, String operationRemark) {
        saveStockLog(drugModel, changeType, beforeQuantity, changeQuantity, afterQuantity, relatedPlanId, relatedReminderId,
            null, operationRemark);
    }

    /**
     * 保存库存流水。
     *
     * <p>这里额外补上“关联临时用药记录 ID”，
     * 让库存流水和业务记录之间可以直接做双向追溯，而不需要再从备注文本里猜。
     */
    private void saveStockLog(DrugModel drugModel, String changeType, BigDecimal beforeQuantity, BigDecimal changeQuantity,
        BigDecimal afterQuantity, Long relatedPlanId, Long relatedReminderId, Long relatedTemporaryMedicationId,
        String operationRemark) {
        if (drugModel == null || drugModel.getDrugId() == null) {
            return;
        }

        HealthDrugStockLogEntity logEntity = new HealthDrugStockLogEntity();
        logEntity.setOwnerUserId(drugModel.getOwnerUserId());
        logEntity.setDrugId(drugModel.getDrugId());
        logEntity.setChangeType(changeType);
        logEntity.setBeforeQuantity(beforeQuantity);
        logEntity.setChangeQuantity(changeQuantity);
        logEntity.setAfterQuantity(afterQuantity);
        logEntity.setStockUnitSnapshot(drugModel.getStockUnit());
        logEntity.setAlertThresholdSnapshot(drugModel.getStockAlertThreshold());
        logEntity.setRelatedPlanId(relatedPlanId);
        logEntity.setRelatedReminderId(relatedReminderId);
        logEntity.setRelatedTempMedicationId(relatedTemporaryMedicationId);
        logEntity.setOperationRemark(operationRemark);
        drugStockLogService.save(logEntity);
    }

    /**
     * 解析库存单位。
     *
     * <p>当前优先级固定为：
     * 1. 如果传了标准单位ID，则以后端主数据名称为准
     * 2. 如果没传单位ID，则兼容保留原有纯文本单位能力
     */
    private ResolvedStockUnit resolveStockUnitSnapshot(Long stockUnitId, String stockUnit) {
        if (stockUnitId != null) {
            DrugUnitEntity unitEntity = drugUnitApplicationService.loadEnabledUnit(stockUnitId);
            return new ResolvedStockUnit(unitEntity.getUnitId(), unitEntity.getUnitName());
        }

        String normalizedStockUnit = normalizeText(stockUnit);
        return normalizedStockUnit == null ? null : new ResolvedStockUnit(null, normalizedStockUnit);
    }

    /**
     * 校验附件是否存在。
     */
    private void validateAttachmentExists(Long attachmentId) {
        if (attachmentId == null) {
            return;
        }
        AttachmentEntity attachmentEntity = attachmentService.getById(attachmentId);
        if (attachmentEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, attachmentId, "附件");
        }
        if (!Objects.equals(attachmentEntity.getAttachmentType(), AttachmentTypeConstants.DRUG_IMAGE)) {
            throw new ApiException(ErrorCode.Business.HEALTH_ATTACHMENT_TYPE_INVALID);
        }
    }

    /**
     * 安全读取导入列。
     *
     * <p>导入列数量允许不足，缺失列按 null 处理，
     * 这样用户只写药名时也能正常完成导入。
     */
    private String readImportColumn(String[] columns, int index) {
        if (columns == null || index < 0 || index >= columns.length) {
            return null;
        }
        return normalizeText(columns[index]);
    }

    /**
     * 统一做字符串去空白与空串转 null。
     */
    private String normalizeText(String rawValue) {
        String trimmedText = StrUtil.trim(rawValue);
        return StrUtil.isBlank(trimmedText) ? null : trimmedText;
    }

    /**
     * 把效期统一归一到日期粒度。
     *
     * <p>库存批次只关心“到哪一天过期”，不关心具体时分秒，
     * 因此这里统一取自然日开始时间，避免同一天因为时间精度不同而被错误拆成两个批次。
     */
    private Date normalizeDate(Date rawDate) {
        return rawDate == null ? null : DateUtil.beginOfDay(rawDate);
    }

    /**
     * 把时间字段统一规整到秒级。
     *
     * <p>临时用药记录需要保留“哪一刻用了药”，
     * 但不需要保留数据库驱动或前端传参带来的毫秒噪音，因此这里统一裁到秒级。
     */
    private Date normalizeDateTime(Date rawDate) {
        if (rawDate == null) {
            return new Date();
        }
        return DateUtil.date(rawDate).setField(cn.hutool.core.date.DateField.MILLISECOND, 0);
    }

    /**
     * 对库存数字做更友好的字符串格式化。
     */
    private String stripTrailingZero(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * 把空库存安全转成 0，避免做加减时出现空指针。
     */
    private BigDecimal safeQuantity(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    /**
     * 对字符串做统一长度保护，避免库存流水备注、消息正文超出数据库字段长度。
     */
    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    /**
     * 解析后的库存单位快照。
     *
     * <p>之所以单独抽成小对象，是为了让“单位ID + 单位名称”在应用服务内部始终成对流转，
     * 避免后续某一步只更新了名称、忘记更新ID。
     */
    @lombok.Value
    private static class ResolvedStockUnit {
        Long unitId;
        String unitName;
    }

    /**
     * 撤销服药时用于定位回补库存桶的轻量值对象。
     *
     * <p>现在除了批号和效期，还要显式携带“是不是默认批次”：
     * 1. 默认批次允许批号和效期都为空
     * 2. 只靠空值已经不足以完整表达目标库存桶语义
     */
    @lombok.Value
    private static class RestoreStockBatchBucket {
        String batchNo;
        Date expireDate;
        boolean defaultBatch;
    }
}
