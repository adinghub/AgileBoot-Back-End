package com.healthtrail.domain.health.medication;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskTypeEnum;
import com.healthtrail.common.enums.health.MedicationReminderNotifyStatusEnum;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpRecommendedActionDTO;
import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import com.healthtrail.domain.health.drug.DrugApplicationService;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.drug.dto.DrugDTO;
import com.healthtrail.domain.health.drug.query.DrugQuery;
import com.healthtrail.domain.health.family.FamilyMemberAccessService;
import com.healthtrail.domain.health.family.dto.FamilyMemberDTO;
import com.healthtrail.domain.health.family.FamilyMemberApplicationService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.family.dto.FamilyMemberAccessContextDTO;
import com.healthtrail.domain.health.message.AppMessageApplicationService;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.healthtrail.domain.health.message.dto.HealthAppMessageCreateRequest;
import com.healthtrail.domain.health.medication.dto.MedicationAdherenceStatisticsDTO;
import com.healthtrail.domain.health.medication.dto.MedicationAdherenceTrendPointDTO;
import com.healthtrail.domain.health.medication.dto.MedicationReminderHistoryDTO;
import com.healthtrail.domain.health.medication.command.AddMedicationPlanCommand;
import com.healthtrail.domain.health.medication.command.CopyMedicationPlanCommand;
import com.healthtrail.domain.health.medication.command.SkipMedicationReminderCommand;
import com.healthtrail.domain.health.medication.command.UpdateMedicationPlanCommand;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import com.healthtrail.domain.health.medication.dto.MedicationPlanDTO;
import com.healthtrail.domain.health.medication.dto.MedicationReminderDTO;
import com.healthtrail.domain.health.medication.model.MedicationPlanModel;
import com.healthtrail.domain.health.medication.model.MedicationPlanModelFactory;
import com.healthtrail.domain.health.medication.model.MedicationDoseStageRule;
import com.healthtrail.domain.health.medication.model.MedicationReminderModel;
import com.healthtrail.domain.health.medication.model.MedicationReminderModelFactory;
import com.healthtrail.domain.health.medication.notify.MedicationReminderNotice;
import com.healthtrail.domain.health.medication.notify.MedicationReminderNotifyResult;
import com.healthtrail.domain.health.medication.notify.MedicationReminderNotifier;
import com.healthtrail.domain.health.medication.query.MedicationReminderHistoryQuery;
import com.healthtrail.domain.health.medication.query.MedicationPlanQuery;
import com.healthtrail.domain.health.medication.query.MedicationReminderQuery;
import com.healthtrail.domain.health.support.HealthBizCodeFormatter;
import com.healthtrail.domain.system.member.MemberGateApplicationService;
import com.healthtrail.domain.system.member.MemberGateCodeConstants;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 药品与用药提醒应用服务。
 *
 * <p>这个应用服务把“药品查询”“用药计划”“提醒记录”三条链路放在同一业务上下文中，
 * 是因为它们在 App 端通常会被连贯使用：
 * 用户先查药，再选家庭成员创建计划，最后查看今日提醒并反馈服药结果。
 */
@Service
@RequiredArgsConstructor
public class MedicationApplicationService {

    /** 药品主表数据库服务 */
    private final HealthDrugService drugService;

    /** 药品应用服务 */
    private final DrugApplicationService drugApplicationService;

    /** 家庭成员应用服务 */
    private final FamilyMemberApplicationService familyMemberApplicationService;

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 家庭成员访问控制服务 */
    private final FamilyMemberAccessService familyMemberAccessService;

    /** 用药计划数据库服务 */
    private final HealthMedicationPlanService medicationPlanService;

    /** 用药提醒数据库服务 */
    private final HealthMedicationReminderService medicationReminderService;

    /** 用药计划领域模型工厂 */
    private final MedicationPlanModelFactory medicationPlanModelFactory;

    /** 用药提醒领域模型工厂 */
    private final MedicationReminderModelFactory medicationReminderModelFactory;

    /** 用药提醒通知发送器 */
    private final MedicationReminderNotifier medicationReminderNotifier;

    /** App消息应用服务 */
    private final AppMessageApplicationService appMessageApplicationService;

    /** 会员权限控制应用服务 */
    private final MemberGateApplicationService memberGateApplicationService;

    /**
     * 分页查询药品列表。
     */
    public PageDTO<DrugDTO> getDrugList(DrugQuery query) {
        Page<DrugEntity> page = drugService.page(query.toPage(), query.toQueryWrapper());
        List<DrugDTO> records = page.getRecords().stream().map(DrugDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 获取药品详情。
     */
    public DrugDTO getDrugInfo(Long drugId) {
        DrugEntity drugEntity = drugService.getById(drugId);
        if (drugEntity == null) {
            throw new com.healthtrail.common.exception.ApiException(
                com.healthtrail.common.exception.error.ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, drugId, "药品");
        }
        return new DrugDTO(drugEntity);
    }

    /**
     * 查询当前用户的用药计划列表。
     */
    public List<MedicationPlanDTO> getMedicationPlanList(MedicationPlanQuery query) {
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(query.getOwnerUserId(), query.getMemberId());
        if (accessibleMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<HealthMedicationPlanEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("member_id", accessibleMemberIds)
            .eq(query.getMemberId() != null, "member_id", query.getMemberId())
            .eq(query.getStatus() != null, "status", query.getStatus())
            .orderByDesc("plan_id");
        List<HealthMedicationPlanEntity> list = medicationPlanService.list(queryWrapper);
        MedicationDisplayContext displayContext = buildMedicationDisplayContext(list, null);
        return list.stream()
            .map(entity -> buildMedicationPlanDTO(entity, displayContext))
            .collect(Collectors.toList());
    }

    /**
     * 查询单个用药计划详情。
     */
    public MedicationPlanDTO getMedicationPlanInfo(Long planId, Long ownerUserId) {
        MedicationPlanModel medicationPlanModel = medicationPlanModelFactory.loadById(planId);
        familyMemberAccessService.getRequiredAccessContext(medicationPlanModel.getMemberId(), ownerUserId);
        return buildMedicationPlanDTO(medicationPlanModel);
    }

    /**
     * 新增用药计划。
     * 创建成功后会立即生成未来提醒记录，保证 App 今日提醒页可以马上展示数据。
     */
    public void addMedicationPlan(AddMedicationPlanCommand addCommand, Long ownerUserId) {
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(addCommand.getMemberId(), ownerUserId);

        // “新增计划”虽然最终落到的是某个家庭成员名下，
        // 但会员资格判断的主体仍然应该是当前正在操作的账号本身。
        // 因此这里在成员编辑权限通过后，再统一走会员门禁中心判断：
        // 1. 页面端可以提前提示；
        // 2. 服务端这里负责最终兜底裁决，避免绕过 App 入口直接调用接口。
        memberGateApplicationService.ensureCurrentUserGateAllowed(
            ownerUserId,
            MemberGateCodeConstants.TRANSACTION_PLAN_CREATE
        );

        MedicationPlanModel medicationPlanModel = medicationPlanModelFactory.create();
        medicationPlanModel.loadAddCommand(addCommand, accessContext.getOwnerUserId());
        medicationPlanModel.checkFields(addCommand.getReminderTimes(), ownerUserId);
        medicationPlanModel.insert();
        ensurePlanCode(medicationPlanModel);
        rebuildFutureReminders(medicationPlanModel);
    }

    /**
     * 复制一条已有用药计划。
     *
     * <p>复制逻辑的目标不是“完全克隆数据库记录”，
     * 而是把现有计划当作模板，快速生成一条新的可编辑计划：
     * 1. 计划规则默认沿用原计划
     * 2. 用户可覆盖目标成员、起止日期、状态、备注
     * 3. 新计划生成后会立即重建未来提醒，保证提醒链路马上可见
     */
    public MedicationPlanDTO copyMedicationPlan(Long planId, CopyMedicationPlanCommand copyCommand, Long ownerUserId) {
        MedicationPlanModel sourcePlanModel = medicationPlanModelFactory.loadById(planId);

        // 复制动作只要求当前账号能看见原计划；
        // 真正决定能不能落库的是目标成员的编辑权限。
        familyMemberAccessService.getRequiredAccessContext(sourcePlanModel.getMemberId(), ownerUserId);
        Long targetMemberId = copyCommand != null && copyCommand.getTargetMemberId() != null
            ? copyCommand.getTargetMemberId()
            : sourcePlanModel.getMemberId();
        FamilyMemberAccessContextDTO targetAccessContext =
            familyMemberAccessService.checkCanEditMember(targetMemberId, ownerUserId);

        // “复制计划”本质上会新增一条新的用药计划记录，
        // 因此它和“手动新增计划”应共享同一条会员门禁口径，
        // 避免用户绕过新增入口，改走复制入口继续生成新计划。
        memberGateApplicationService.ensureCurrentUserGateAllowed(
            ownerUserId,
            MemberGateCodeConstants.TRANSACTION_PLAN_CREATE
        );

        AddMedicationPlanCommand addCommand =
            buildCopyAddCommand(sourcePlanModel, copyCommand, targetMemberId, targetAccessContext.getOwnerUserId());
        MedicationPlanModel copiedPlanModel = medicationPlanModelFactory.create();
        copiedPlanModel.loadAddCommand(addCommand, targetAccessContext.getOwnerUserId());
        copiedPlanModel.checkFields(addCommand.getReminderTimes(), targetAccessContext.getOwnerUserId());
        copiedPlanModel.insert();
        ensurePlanCode(copiedPlanModel);
        rebuildFutureReminders(copiedPlanModel);
        return buildMedicationPlanDTO(copiedPlanModel);
    }

    /**
     * 修改用药计划。
     * 更新完成后，会清理该计划下未来尚未执行的提醒，并根据新规则重新生成未来提醒。
     */
    public void updateMedicationPlan(UpdateMedicationPlanCommand updateCommand, Long ownerUserId) {
        MedicationPlanModel medicationPlanModel = medicationPlanModelFactory.loadById(updateCommand.getPlanId());
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(medicationPlanModel.getMemberId(), ownerUserId);
        medicationPlanModel.loadUpdateCommand(updateCommand, accessContext.getOwnerUserId());
        medicationPlanModel.checkFields(updateCommand.getReminderTimes(), ownerUserId);
        ensurePlanCode(medicationPlanModel);
        medicationPlanModel.updateById();
        rebuildFutureReminders(medicationPlanModel);
    }

    /**
     * 删除用药计划。
     * 一期采用最直接的方式：计划删除时，同时逻辑删除该计划下的所有提醒记录。
     */
    public void removeMedicationPlan(Long planId, Long ownerUserId) {
        MedicationPlanModel medicationPlanModel = medicationPlanModelFactory.loadById(planId);
        familyMemberAccessService.checkCanEditMember(medicationPlanModel.getMemberId(), ownerUserId);
        medicationPlanService.removeById(planId);

        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", planId);
        medicationReminderService.remove(queryWrapper);
    }

    /**
     * 获取今日提醒列表。
     * 在返回结果前，会先把过期未处理的提醒标记为“已过期”，保证前端看到的是最新状态。
     */
    public List<MedicationReminderDTO> getTodayReminderList(MedicationReminderQuery query) {
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(query.getOwnerUserId(), query.getMemberId());
        if (accessibleMemberIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 这里在用户态查询入口主动补一次“当天提醒窗口”，
        // 目的是兜底以下场景：
        // 1. 定时补齐任务因服务重启、部署空窗或配置问题暂时没有跑到
        // 2. 用户隔了几天重新登录/重新打开 App，主观感受会误以为“计划没了”
        // 实际上计划数据通常仍然存在，只是当天 reminder 记录尚未补齐。
        // 因此先按当前账号补齐 1 天窗口，再做后续状态刷新和列表查询，能让前端看到更稳定的今日提醒数据。
        supplementUpcomingReminders(query.getOwnerUserId(), 1);
        expireOverdueRemindersByMemberIds(accessibleMemberIds);

        Date beginOfDay = DateUtil.beginOfDay(new Date());
        Date endOfDay = DateUtil.endOfDay(new Date());

        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("member_id", accessibleMemberIds)
            .eq(query.getMemberId() != null, "member_id", query.getMemberId())
            .eq(query.getReminderStatus() != null, "reminder_status", query.getReminderStatus())
            .ge("scheduled_time", beginOfDay)
            .le("scheduled_time", endOfDay)
            .orderByAsc("scheduled_time");

        List<HealthMedicationReminderEntity> list = medicationReminderService.list(queryWrapper);
        MedicationDisplayContext displayContext = buildMedicationDisplayContext(null, list);
        return list.stream()
            .map(entity -> buildReminderDTO(entity, displayContext))
            .collect(Collectors.toList());
    }

    /**
     * 标记提醒为已服药。
     */
    @Transactional(rollbackFor = Exception.class)
    public void markReminderTaken(Long reminderId, Long ownerUserId) {
        MedicationReminderModel medicationReminderModel = medicationReminderModelFactory.loadById(reminderId);
        familyMemberAccessService.checkCanEditMember(medicationReminderModel.getMemberId(), ownerUserId);
        medicationReminderModel.markTaken();
        medicationReminderModel.updateById();
        drugApplicationService.consumeDrugStockAfterReminderTaken(ownerUserId, medicationReminderModel.getPlanId(),
            medicationReminderModel.getReminderId(), medicationReminderModel.getDoseAmount(),
            medicationReminderModel.getDoseUnit());
    }

    /**
     * 撤销提醒“已服药”反馈。
     *
     * <p>这条链路主要用于 App 端误触兜底：
     * 1. 先把提醒状态从“已服药”恢复为“待处理”
     * 2. 再把因“已服药”自动扣减的个人库存补回
     *
     * <p>这样用户撤销后，提醒反馈状态和药柜库存会一起回到一致状态。
     */
    @Transactional(rollbackFor = Exception.class)
    public void revertReminderTaken(Long reminderId, Long ownerUserId) {
        MedicationReminderModel medicationReminderModel = medicationReminderModelFactory.loadById(reminderId);
        familyMemberAccessService.checkCanEditMember(medicationReminderModel.getMemberId(), ownerUserId);
        medicationReminderModel.revertTaken();
        medicationReminderModel.updateById();
        drugApplicationService.restoreDrugStockAfterReminderTakeReverted(ownerUserId, medicationReminderModel.getPlanId(),
            medicationReminderModel.getReminderId(), medicationReminderModel.getDoseAmount(),
            medicationReminderModel.getDoseUnit());
    }

    /**
     * 自动过期所有已经过点但仍未处理的提醒。
     *
     * <p>这个方法主要给后台定时任务调用，目的是把“只有用户主动打开今日提醒页才过期”
     * 调整为“系统自己持续维护提醒状态”，这样后续无论接 App Push、站内消息还是统计报表，
     * 读取到的提醒状态都会更稳定。
     *
     * @return 本次被自动过期的提醒数量
     */
    public int expireOverdueReminders() {
        return expireOverdueReminders(null);
    }

    /**
     * 自动过期指定用户已经过点但仍未处理的提醒。
     *
     * <p>该方法主要给首页看板、提醒页等用户态查询入口复用，
     * 确保它们读取到的提醒状态都是一致且最新的。
     *
     * @param ownerUserId 指定 App 用户ID
     * @return 本次被自动过期的提醒数量
     */
    public int expireOverdueReminders(Long ownerUserId) {
        return expireOverdueRemindersInternal(ownerUserId);
    }

    /**
     * 自动补齐未来几天的提醒记录。
     *
     * <p>虽然当前新增、修改计划时已经会立即生成未来提醒，
     * 但定时补齐仍然有价值：
     * 1. 可以兜底历史脏数据或异常中断场景
     * 2. 方便后续把“创建时全量生成”平滑演进为“窗口期滚动生成”
     * 3. 为后续 Push/短信等发送通道提供更稳定的数据来源
     *
     * @param upcomingDays 需要补齐的未来天数窗口，最小按 1 天处理
     * @return 本次新增的提醒数量
     */
    public int supplementUpcomingReminders(int upcomingDays) {
        return supplementUpcomingReminders(null, upcomingDays);
    }

    /**
     * 为指定账号补齐未来几天的提醒记录。
     *
     * <p>这个重载方法主要服务于用户主动打开首页/提醒页的读场景兜底。
     * 与“全局补齐”相比，它只扫描当前账号名下的启用计划，成本更可控，
     * 适合放在高频用户请求前做一次轻量自愈，避免用户把“当天 reminder 未生成”
     * 误解成“重新登录后计划丢失”。
     *
     * @param ownerUserId 指定 App 用户ID；为空时退化为全局补齐
     * @param upcomingDays 需要补齐的未来天数窗口，最小按 1 天处理
     * @return 本次新增的提醒数量
     */
    public int supplementUpcomingReminders(Long ownerUserId, int upcomingDays) {
        int safeUpcomingDays = Math.max(upcomingDays, 1);
        Date now = new Date();
        Date windowStart = DateUtil.beginOfDay(now);
        Date windowEnd = DateUtil.endOfDay(DateUtil.offsetDay(windowStart, safeUpcomingDays - 1));

        List<HealthMedicationPlanEntity> activePlans = medicationPlanService.lambdaQuery()
            .eq(ownerUserId != null, HealthMedicationPlanEntity::getOwnerUserId, ownerUserId)
            .eq(HealthMedicationPlanEntity::getStatus, StatusEnum.ENABLE.getValue())
            .le(HealthMedicationPlanEntity::getStartDate, windowEnd)
            // 长期计划允许 end_date 为空；查询窗口内只需要满足“没有结束日期”或“结束日期晚于窗口开始”。
            .and(wrapper -> wrapper.isNull(HealthMedicationPlanEntity::getEndDate)
                .or()
                .ge(HealthMedicationPlanEntity::getEndDate, windowStart))
            .list();
        if (activePlans.isEmpty()) {
            return 0;
        }

        // 旧实现会在循环里对每条计划单独查询一次已有提醒、单独查询一次药品名称，
        // 当首页或提醒页触发“自愈补提醒”时，计划数量越多 SQL 放大越明显。
        //
        // 这里先把窗口内已有 reminder 和药品快照一次性预加载出来：
        // 1. reminder 预加载后按 planId -> scheduledTimeSet 组织
        // 2. drug 预加载后按 drugId -> drugEntity 组织
        //
        // 后续逐计划补齐时就只剩纯内存判断和最终批量写入，避免每个计划都再打两轮查库。
        Map<Long, Set<Long>> existingScheduledTimeMap = loadExistingScheduledTimeMap(activePlans, windowStart, windowEnd);
        Map<Long, DrugEntity> drugMap = loadDrugMap(activePlans.stream()
            .map(HealthMedicationPlanEntity::getDrugId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));

        int createdReminderCount = 0;
        for (HealthMedicationPlanEntity planEntity : activePlans) {
            String displayDrugName = resolveDisplayDrugName(planEntity, drugMap);
            createdReminderCount += appendMissingReminders(planEntity, windowStart, windowEnd, now,
                existingScheduledTimeMap.getOrDefault(planEntity.getPlanId(), Collections.emptySet()),
                displayDrugName);
        }
        return createdReminderCount;
    }

    /**
     * 派发已经到达提醒时间的待发送提醒。
     *
     * <p>当前实现会读取“已到提醒时间、仍待处理、尚未成功发送”的提醒记录，
     * 然后交给 `MedicationReminderNotifier` 执行真正发送。
     * 这样业务层可以稳定管理发送状态，而具体通道可以按项目阶段自由替换。
     *
     * @param batchSize 单次批处理数量上限
     * @param maxRetryCount 单条提醒最大重试次数
     * @return 本次成功发送的提醒数量
     */
    public int dispatchDueReminders(int batchSize, int maxRetryCount) {
        int safeBatchSize = Math.max(batchSize, 1);
        int safeMaxRetryCount = Math.max(maxRetryCount, 1);
        Date now = new Date();

        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reminder_status", MedicationReminderStatusEnum.PENDING.getValue())
            .le("scheduled_time", now)
            .lt("notify_retry_count", safeMaxRetryCount)
            .and(wrapper -> wrapper.eq("notify_status", MedicationReminderNotifyStatusEnum.PENDING.getValue())
                .or()
                .eq("notify_status", MedicationReminderNotifyStatusEnum.FAILED.getValue()))
            .orderByAsc("scheduled_time")
            .last("limit " + safeBatchSize);

        List<HealthMedicationReminderEntity> dueReminders = medicationReminderService.list(queryWrapper);
        int successCount = 0;
        for (HealthMedicationReminderEntity reminderEntity : dueReminders) {
            if (dispatchSingleReminder(reminderEntity, now)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 标记提醒为已跳过。
     */
    public void markReminderSkipped(Long reminderId, SkipMedicationReminderCommand skipCommand, Long ownerUserId) {
        MedicationReminderModel medicationReminderModel = medicationReminderModelFactory.loadById(reminderId);
        familyMemberAccessService.checkCanEditMember(medicationReminderModel.getMemberId(), ownerUserId);
        medicationReminderModel.markSkipped(skipCommand);
        medicationReminderModel.updateById();
    }

    /**
     * 分页查询历史服药记录。
     */
    public PageDTO<MedicationReminderHistoryDTO> getReminderHistory(MedicationReminderHistoryQuery query) {
        validateHistoryQuery(query);
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(query.getOwnerUserId(), query.getMemberId());
        if (accessibleMemberIds.isEmpty()) {
            return new PageDTO<>(Collections.emptyList(), 0L);
        }

        Page<HealthMedicationReminderEntity> page = medicationReminderService.page(query.toPage(),
            buildHistoryQueryWrapper(query, accessibleMemberIds));
        MedicationDisplayContext displayContext = buildMedicationDisplayContext(null, page.getRecords());
        List<MedicationReminderHistoryDTO> records = page.getRecords().stream()
            .map(entity -> buildHistoryDTO(entity, displayContext))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 统计依从率摘要。
     */
    public MedicationAdherenceStatisticsDTO getAdherenceStatistics(MedicationReminderHistoryQuery query) {
        validateHistoryQuery(query);
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(query.getOwnerUserId(), query.getMemberId());
        List<HealthMedicationReminderEntity> reminders = listHistoryReminders(query, accessibleMemberIds);

        MedicationAdherenceStatisticsDTO dto = new MedicationAdherenceStatisticsDTO();
        dto.setMemberId(query.getMemberId());
        dto.setMemberName(resolveMemberName(query.getMemberId()));
        dto.setStartDate(query.getStartDate());
        dto.setEndDate(query.getEndDate());
        dto.setScheduledReminderCount(reminders.size());
        dto.setTakenReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.TAKEN.getValue()));
        dto.setSkippedReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.SKIPPED.getValue()));
        dto.setExpiredReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.EXPIRED.getValue()));
        dto.setPendingReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.PENDING.getValue()));

        int denominator = dto.getTakenReminderCount() + dto.getSkippedReminderCount() + dto.getExpiredReminderCount();
        dto.setAdherenceRate(denominator <= 0 ? 0D : roundRate(dto.getTakenReminderCount() * 100D / denominator));
        return dto;
    }

    /**
     * 统计依从率趋势。
     */
    public List<MedicationAdherenceTrendPointDTO> getAdherenceTrend(MedicationReminderHistoryQuery query) {
        validateHistoryQuery(query);
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(query.getOwnerUserId(), query.getMemberId());
        List<HealthMedicationReminderEntity> reminders = listHistoryReminders(query, accessibleMemberIds);
        if (reminders.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<HealthMedicationReminderEntity>> groupedReminders = reminders.stream()
            .filter(item -> item.getReminderDate() != null)
            .collect(Collectors.groupingBy(item -> DateUtil.beginOfDay(item.getReminderDate()).getTime()));

        return groupedReminders.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> buildTrendPoint(DateUtil.date(entry.getKey()), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * 生成当前用户的成员选项快照信息。
     * 该方法主要是为了后续前端页面初始化时可以快速拿到成员列表联动数据。
     */
    public List<FamilyMemberDTO> getFamilyMemberOptions(Long ownerUserId) {
        com.healthtrail.domain.health.family.query.FamilyMemberQuery query =
            new com.healthtrail.domain.health.family.query.FamilyMemberQuery();
        query.setOwnerUserId(ownerUserId);
        query.setStatus(com.healthtrail.common.enums.common.StatusEnum.ENABLE.getValue());
        return familyMemberApplicationService.getFamilyMemberList(query);
    }

    /**
     * 构建用药计划 DTO。
     * 如果计划绑定了系统药品或用户自己录入的药品，则额外回填药品名称，方便前端直接展示。
     */
    private MedicationPlanDTO buildMedicationPlanDTO(HealthMedicationPlanEntity entity) {
        ensurePlanCode(entity);
        MedicationPlanDTO dto = new MedicationPlanDTO(entity);
        dto.setMemberCode(resolveMemberCode(entity.getMemberId()));
        if (entity.getDrugId() != null) {
            DrugEntity drugEntity = drugService.getById(entity.getDrugId());
            if (drugEntity != null) {
                dto.setDrugCode(resolveDrugCode(drugEntity));
                dto.setDrugName(drugEntity.getDrugName());
            }
        }
        if (dto.getDrugName() == null) {
            dto.setDrugName(entity.getCustomDrugName());
        }
        return dto;
    }

    /**
     * 用预加载的展示上下文构建计划 DTO。
     *
     * <p>列表页会一次性展示多条计划。
     * 如果每条计划都单独查成员表、药品表，就会形成稳定的 N+1。
     * 因此这里优先使用批量上下文中的快照；同时只做“格式化兜底”，不在查询链路里触发补码更新。
     */
    private MedicationPlanDTO buildMedicationPlanDTO(HealthMedicationPlanEntity entity, MedicationDisplayContext displayContext) {
        MedicationPlanDTO dto = new MedicationPlanDTO(entity);
        dto.setPlanCode(resolvePlanCode(displayContext.getPlanMap(), entity.getPlanId()));
        dto.setMemberCode(resolveMemberCode(displayContext.getMemberMap(), entity.getMemberId()));
        DrugEntity drugEntity = entity.getDrugId() == null ? null : displayContext.getDrugMap().get(entity.getDrugId());
        if (drugEntity != null) {
            dto.setDrugCode(resolveDrugCode(drugEntity));
            dto.setDrugName(drugEntity.getDrugName());
        }
        if (dto.getDrugName() == null) {
            dto.setDrugName(entity.getCustomDrugName());
        }
        return dto;
    }

    /**
     * 根据原计划和复制命令组装新的新增计划命令。
     *
     * <p>这里故意不直接 BeanCopy 原实体，原因是复制计划时存在一个重要权限边界：
     * 如果原计划使用的是“别的主账号私有药品”，而复制目标成员并不属于那个主账号，
     * 那么新计划就不能继续引用原 `drugId`，否则会形成越权数据引用。
     *
     * <p>因此这里会自动降级：
     * 1. 可继续引用的药品，保留 `drugId`
     * 2. 不可继续引用的药品，转成 `customDrugName`
     */
    private AddMedicationPlanCommand buildCopyAddCommand(MedicationPlanModel sourcePlanModel,
        CopyMedicationPlanCommand copyCommand, Long targetMemberId, Long targetOwnerUserId) {
        AddMedicationPlanCommand addCommand = new AddMedicationPlanCommand();
        addCommand.setMemberId(targetMemberId);
        addCommand.setStartDate(copyCommand != null && copyCommand.getStartDate() != null
            ? copyCommand.getStartDate() : sourcePlanModel.getStartDate());
        addCommand.setEndDate(copyCommand != null && copyCommand.getEndDate() != null
            ? copyCommand.getEndDate() : sourcePlanModel.getEndDate());
        addCommand.setReminderTimes(new ArrayList<>(sourcePlanModel.getReminderTimes()));
        addCommand.setMealTiming(sourcePlanModel.getMealTiming());
        addCommand.setDoseAmount(sourcePlanModel.getDoseAmount());
        addCommand.setDoseUnit(sourcePlanModel.getDoseUnit());
        addCommand.setFrequencyType(sourcePlanModel.getFrequencyType());
        addCommand.setWeeklyDays(new ArrayList<>(sourcePlanModel.getWeeklyDays()));
        addCommand.setIntervalHours(sourcePlanModel.getIntervalHours());
        addCommand.setIntervalDays(sourcePlanModel.getIntervalDays());
        addCommand.setDoseRule(sourcePlanModel.getDoseRule());
        addCommand.setRemark(copyCommand != null && StrUtil.isNotBlank(copyCommand.getRemark())
            ? copyCommand.getRemark() : sourcePlanModel.getRemark());
        addCommand.setStatus(copyCommand != null && copyCommand.getStatus() != null
            ? copyCommand.getStatus() : sourcePlanModel.getStatus());

        Long copiedDrugId = sourcePlanModel.getDrugId();
        String copiedCustomDrugName = sourcePlanModel.getCustomDrugName();
        if (copiedDrugId != null) {
        DrugEntity drugEntity = drugService.getById(copiedDrugId);
            boolean isSystemDrug = drugEntity != null && Objects.equals(drugEntity.getOwnerUserId(), DrugQuery.SYSTEM_DRUG_OWNER_ID);
            boolean belongsToTargetOwner = drugEntity != null && Objects.equals(drugEntity.getOwnerUserId(), targetOwnerUserId);
            if (!isSystemDrug && !belongsToTargetOwner) {
                copiedDrugId = null;
                copiedCustomDrugName = StrUtil.blankToDefault(sourcePlanModel.getDisplayDrugName(), sourcePlanModel.getCustomDrugName());
            }
        }

        addCommand.setDrugId(copiedDrugId);
        addCommand.setCustomDrugName(copiedCustomDrugName);
        return addCommand;
    }

    /**
     * 重建未来提醒记录。
     *
     * <p>这里有一个明确取舍：只重建“当前时刻之后”的提醒，不补历史提醒。
     * 这样可以避免用户修改计划后，把今天之前已经完成过的提醒又重新生成出来。
     */
    private void rebuildFutureReminders(MedicationPlanModel medicationPlanModel) {
        deleteFutureRemindersOfPlan(medicationPlanModel.getPlanId());

        Date now = new Date();
        Date windowStart = laterTime(DateUtil.beginOfDay(medicationPlanModel.getStartDate()), DateUtil.beginOfDay(now));
        Date windowEnd = resolveReminderWindowEnd(medicationPlanModel.getEndDate(), windowStart, 30);
        List<HealthMedicationReminderEntity> reminders = buildReminderRecords(medicationPlanModel,
            medicationPlanModel.getReminderTimes(), medicationPlanModel.getDisplayDrugName(), windowStart, windowEnd,
            now, new HashSet<>());

        if (!reminders.isEmpty()) {
            medicationReminderService.saveBatch(reminders);
        }
    }

    /**
     * 删除某个计划的未来提醒记录。
     * 这里只删除当前时刻之后的记录，保留历史记录，避免影响已经产生的历史统计。
     */
    private void deleteFutureRemindersOfPlan(Long planId) {
        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", planId)
            .ge("scheduled_time", new Date())
            .in("reminder_status", MedicationReminderStatusEnum.PENDING.getValue(),
                MedicationReminderStatusEnum.EXPIRED.getValue(), MedicationReminderStatusEnum.SKIPPED.getValue());
        medicationReminderService.remove(queryWrapper);
    }

    /**
     * 将当前用户名下已经过点但仍未处理的提醒自动标记为过期。
     * 这样前端不需要额外理解“过了时间但还是待处理”的中间状态。
     */
    private int expireOverdueRemindersInternal(Long ownerUserId) {
        Date now = new Date();
        // 这里改成委托底层服务直接执行“批量 update 并返回影响行数”，
        // 让提醒状态自愈从“两次 SQL”收敛成“一次 SQL”：
        // 1. 首页看板、今日提醒都会走这条链路；
        // 2. 它们本质上是读接口，不应该再额外付出 count + update 的双倍数据库成本；
        // 3. update 返回值已经是本次真实完成状态迁移的数量，足够上层继续使用。
        return medicationReminderService.expirePendingRemindersBefore(ownerUserId, now);
    }

    /**
     * 按成员范围批量过期提醒。
     *
     * <p>共享成员场景下，提醒的 owner_user_id 仍然是主账号，
     * 因此用户态读取时必须支持按 member_id 范围刷新状态，不能再只按 owner_user_id 刷新。
     */
    private int expireOverdueRemindersByMemberIds(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return 0;
        }

        Date now = new Date();
        // 共享成员读取场景和 owner 维度读取场景保持同一优化口径：
        // 直接返回数据库 update 影响行数，不再额外 count 一次。
        return medicationReminderService.expirePendingRemindersBeforeByMemberIds(memberIds, now);
    }

    /**
     * 构建历史记录查询条件。
     */
    private QueryWrapper<HealthMedicationReminderEntity> buildHistoryQueryWrapper(MedicationReminderHistoryQuery query,
        Set<Long> accessibleMemberIds) {
        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("member_id", accessibleMemberIds)
            .eq(query.getMemberId() != null, "member_id", query.getMemberId())
            .eq(query.getPlanId() != null, "plan_id", query.getPlanId())
            .eq(query.getReminderStatus() != null, "reminder_status", query.getReminderStatus())
            .ge(query.getStartDate() != null, "scheduled_time", DateUtil.beginOfDay(query.getStartDate()))
            .le(query.getEndDate() != null, "scheduled_time", DateUtil.endOfDay(query.getEndDate()))
            .orderByDesc("scheduled_time")
            .orderByDesc("reminder_id");
        return queryWrapper;
    }

    /**
     * 查询统计使用的提醒记录列表。
     */
    private List<HealthMedicationReminderEntity> listHistoryReminders(MedicationReminderHistoryQuery query,
        Set<Long> accessibleMemberIds) {
        if (accessibleMemberIds == null || accessibleMemberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return medicationReminderService.list(buildHistoryQueryWrapper(query, accessibleMemberIds));
    }

    /**
     * 构建历史记录 DTO。
     */
    private MedicationReminderHistoryDTO buildHistoryDTO(HealthMedicationReminderEntity entity) {
        MedicationReminderHistoryDTO dto = new MedicationReminderHistoryDTO(entity);
        dto.setMemberName(resolveMemberName(entity.getMemberId()));
        dto.setMemberCode(resolveMemberCode(entity.getMemberId()));
        dto.setPlanCode(resolvePlanCode(entity.getPlanId()));
        return dto;
    }

    /**
     * 用预加载的展示上下文构建历史提醒 DTO。
     */
    private MedicationReminderHistoryDTO buildHistoryDTO(HealthMedicationReminderEntity entity,
        MedicationDisplayContext displayContext) {
        MedicationReminderHistoryDTO dto = new MedicationReminderHistoryDTO(entity);
        dto.setMemberName(resolveMemberName(displayContext.getMemberMap(), entity.getMemberId()));
        dto.setMemberCode(resolveMemberCode(displayContext.getMemberMap(), entity.getMemberId()));
        dto.setPlanCode(resolvePlanCode(displayContext.getPlanMap(), entity.getPlanId()));
        return dto;
    }

    /**
     * 构建今日提醒 DTO。
     *
     * <p>提醒列表虽然底层来源于 reminder 表，
     * 但展示时仍然需要补齐“计划编码 / 成员编码”，
     * 这样 App 详情页就不必继续展示内部数值主键。
     */
    private MedicationReminderDTO buildReminderDTO(HealthMedicationReminderEntity entity) {
        MedicationReminderDTO dto = new MedicationReminderDTO(entity);
        dto.setMemberCode(resolveMemberCode(entity.getMemberId()));
        dto.setPlanCode(resolvePlanCode(entity.getPlanId()));
        return dto;
    }

    /**
     * 用预加载的展示上下文构建今日提醒 DTO。
     */
    private MedicationReminderDTO buildReminderDTO(HealthMedicationReminderEntity entity,
        MedicationDisplayContext displayContext) {
        MedicationReminderDTO dto = new MedicationReminderDTO(entity);
        dto.setMemberCode(resolveMemberCode(displayContext.getMemberMap(), entity.getMemberId()));
        dto.setPlanCode(resolvePlanCode(displayContext.getPlanMap(), entity.getPlanId()));
        return dto;
    }

    /**
     * 构建依从率趋势点。
     */
    private MedicationAdherenceTrendPointDTO buildTrendPoint(Date reminderDate, List<HealthMedicationReminderEntity> reminders) {
        MedicationAdherenceTrendPointDTO dto = new MedicationAdherenceTrendPointDTO();
        dto.setReminderDate(reminderDate);
        dto.setScheduledReminderCount(reminders.size());
        dto.setTakenReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.TAKEN.getValue()));
        dto.setSkippedReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.SKIPPED.getValue()));
        dto.setExpiredReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.EXPIRED.getValue()));
        dto.setPendingReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.PENDING.getValue()));

        int denominator = dto.getTakenReminderCount() + dto.getSkippedReminderCount() + dto.getExpiredReminderCount();
        dto.setAdherenceRate(denominator <= 0 ? 0D : roundRate(dto.getTakenReminderCount() * 100D / denominator));
        return dto;
    }

    /**
     * 计算某种提醒状态数量。
     */
    private int countReminderStatus(List<HealthMedicationReminderEntity> reminders, Integer reminderStatus) {
        return (int) reminders.stream()
            .filter(item -> Objects.equals(item.getReminderStatus(), reminderStatus))
            .count();
    }

    /**
     * 解析当前用户可访问的成员范围。
     */
    private Set<Long> resolveAccessibleMemberIds(Long currentUserId, Long memberId) {
        Set<Long> accessibleMemberIds = familyMemberAccessService.getAccessibleMemberIds(currentUserId);
        if (memberId == null) {
            return accessibleMemberIds;
        }
        familyMemberAccessService.getRequiredAccessContext(memberId, currentUserId);
        return Collections.singleton(memberId);
    }

    /**
     * 校验历史/统计查询参数。
     */
    private void validateHistoryQuery(MedicationReminderHistoryQuery query) {
        if (query == null) {
            return;
        }
        if (query.getStartDate() != null && query.getEndDate() != null && query.getStartDate().after(query.getEndDate())) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_HISTORY_DATE_RANGE_INVALID);
        }
    }

    /**
     * 解析成员名称。
     */
    private String resolveMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 从批量预加载的成员快照中解析名称。
     */
    private String resolveMemberName(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = memberMap == null ? null : memberMap.get(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 回填成员业务编码。
     *
     * <p>优先使用数据库已落库值；
     * 若历史数据暂未补齐，则按主键规则即时兜底。
     */
    private String resolveMemberCode(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? HealthBizCodeFormatter.formatMemberCode(memberId) : ensureMemberCode(memberEntity);
    }

    /**
     * 从批量预加载的成员快照中解析成员编码。
     *
     * <p>这里故意不触发 `updateById`：
     * 列表查询的目标是稳定读，不应该因为历史数据缺码就在读链路里顺带改库。
     * 如果数据库里已经有 memberCode 就直接复用，没有则按主键规则即时兜底格式化。
     */
    private String resolveMemberCode(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = memberMap == null ? null : memberMap.get(memberId);
        return memberEntity == null ? HealthBizCodeFormatter.formatMemberCode(memberId)
            : StrUtil.blankToDefault(memberEntity.getMemberCode(), HealthBizCodeFormatter.formatMemberCode(memberId));
    }

    /**
     * 回填药品业务编码。
     */
    private String resolveDrugCode(DrugEntity drugEntity) {
        if (drugEntity == null) {
            return null;
        }
        return ensureDrugCode(drugEntity);
    }

    /**
     * 回填计划业务编码。
     */
    private String resolvePlanCode(Long planId) {
        if (planId == null) {
            return null;
        }
        HealthMedicationPlanEntity planEntity = medicationPlanService.getById(planId);
        return planEntity == null ? HealthBizCodeFormatter.formatPlanCode(planId) : ensurePlanCode(planEntity);
    }

    /**
     * 从批量预加载的计划快照中解析计划编码。
     */
    private String resolvePlanCode(Map<Long, HealthMedicationPlanEntity> planMap, Long planId) {
        if (planId == null) {
            return null;
        }
        HealthMedicationPlanEntity planEntity = planMap == null ? null : planMap.get(planId);
        return planEntity == null ? HealthBizCodeFormatter.formatPlanCode(planId)
            : StrUtil.blankToDefault(planEntity.getPlanCode(), HealthBizCodeFormatter.formatPlanCode(planId));
    }

    /**
     * 批量构建列表展示所需的成员 / 计划 / 药品快照。
     *
     * <p>当前用药模块有三类高频列表：
     * 1. 用药计划列表
     * 2. 今日提醒列表
     * 3. 历史提醒分页
     *
     * 它们最终都要回填成员编码、计划编码、药品名称这类展示字段。
     * 这里统一抽成一个上下文，避免各列表各自重复写一套批量查库逻辑。
     */
    private MedicationDisplayContext buildMedicationDisplayContext(List<HealthMedicationPlanEntity> planEntities,
        List<HealthMedicationReminderEntity> reminderEntities) {
        Set<Long> memberIds = new HashSet<>();
        Set<Long> planIds = new HashSet<>();
        Set<Long> drugIds = new HashSet<>();
        if (planEntities != null) {
            planEntities.stream()
                .filter(Objects::nonNull)
                .forEach(planEntity -> {
                    if (planEntity.getMemberId() != null) {
                        memberIds.add(planEntity.getMemberId());
                    }
                    if (planEntity.getPlanId() != null) {
                        planIds.add(planEntity.getPlanId());
                    }
                    if (planEntity.getDrugId() != null) {
                        drugIds.add(planEntity.getDrugId());
                    }
                });
        }
        if (reminderEntities != null) {
            reminderEntities.stream()
                .filter(Objects::nonNull)
                .forEach(reminderEntity -> {
                    if (reminderEntity.getMemberId() != null) {
                        memberIds.add(reminderEntity.getMemberId());
                    }
                    if (reminderEntity.getPlanId() != null) {
                        planIds.add(reminderEntity.getPlanId());
                    }
                });
        }
        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(memberIds);
        Map<Long, HealthMedicationPlanEntity> planMap = loadPlanMap(planIds);
        if (planEntities == null) {
            planMap.values().stream()
                .map(HealthMedicationPlanEntity::getDrugId)
                .filter(Objects::nonNull)
                .forEach(drugIds::add);
        }
        return new MedicationDisplayContext(memberMap, planMap, loadDrugMap(drugIds));
    }

    /**
     * 批量加载成员快照。
     */
    private Map<Long, HealthFamilyMemberEntity> loadMemberMap(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return familyMemberService.lambdaQuery()
            .in(HealthFamilyMemberEntity::getMemberId, memberIds)
            .list()
            .stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, memberEntity -> memberEntity,
                (left, right) -> left));
    }

    /**
     * 批量加载计划快照。
     */
    private Map<Long, HealthMedicationPlanEntity> loadPlanMap(Set<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return medicationPlanService.lambdaQuery()
            .in(HealthMedicationPlanEntity::getPlanId, planIds)
            .list()
            .stream()
            .collect(Collectors.toMap(HealthMedicationPlanEntity::getPlanId, planEntity -> planEntity,
                (left, right) -> left));
    }

    /**
     * 批量加载药品快照。
     */
    private Map<Long, DrugEntity> loadDrugMap(Set<Long> drugIds) {
        if (drugIds == null || drugIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return drugService.lambdaQuery()
            .in(DrugEntity::getDrugId, drugIds)
            .list()
            .stream()
            .collect(Collectors.toMap(DrugEntity::getDrugId, drugEntity -> drugEntity,
                (left, right) -> left));
    }

    /**
     * 列表展示批量预加载上下文。
     */
    private static final class MedicationDisplayContext {

        private final Map<Long, HealthFamilyMemberEntity> memberMap;

        private final Map<Long, HealthMedicationPlanEntity> planMap;

        private final Map<Long, DrugEntity> drugMap;

        private MedicationDisplayContext(Map<Long, HealthFamilyMemberEntity> memberMap,
            Map<Long, HealthMedicationPlanEntity> planMap,
            Map<Long, DrugEntity> drugMap) {
            this.memberMap = memberMap == null ? Collections.emptyMap() : memberMap;
            this.planMap = planMap == null ? Collections.emptyMap() : planMap;
            this.drugMap = drugMap == null ? Collections.emptyMap() : drugMap;
        }

        private Map<Long, HealthFamilyMemberEntity> getMemberMap() {
            return memberMap;
        }

        private Map<Long, HealthMedicationPlanEntity> getPlanMap() {
            return planMap;
        }

        private Map<Long, DrugEntity> getDrugMap() {
            return drugMap;
        }
    }

    /**
     * 确保计划主记录已经拥有业务编码。
     */
    private String ensurePlanCode(HealthMedicationPlanEntity entity) {
        if (entity == null || entity.getPlanId() == null) {
            return null;
        }
        String targetPlanCode = HealthBizCodeFormatter.formatPlanCode(entity.getPlanId());
        if (!Objects.equals(entity.getPlanCode(), targetPlanCode)) {
            entity.setPlanCode(targetPlanCode);
            medicationPlanService.updateById(entity);
        }
        return targetPlanCode;
    }

    /**
     * 确保成员主记录已经拥有业务编码。
     */
    private String ensureMemberCode(HealthFamilyMemberEntity entity) {
        if (entity == null || entity.getMemberId() == null) {
            return null;
        }
        String targetMemberCode = HealthBizCodeFormatter.formatMemberCode(entity.getMemberId());
        if (!Objects.equals(entity.getMemberCode(), targetMemberCode)) {
            entity.setMemberCode(targetMemberCode);
            familyMemberService.updateById(entity);
        }
        return targetMemberCode;
    }

    /**
     * 确保药品主记录已经拥有业务编码。
     */
    private String ensureDrugCode(DrugEntity entity) {
        if (entity == null || entity.getDrugId() == null) {
            return null;
        }
        String targetDrugCode = HealthBizCodeFormatter.formatDrugCode(entity.getDrugId());
        if (!Objects.equals(entity.getDrugCode(), targetDrugCode)) {
            entity.setDrugCode(targetDrugCode);
            drugService.updateById(entity);
        }
        return targetDrugCode;
    }

    /**
     * 统一处理百分比保留两位小数。
     */
    private Double roundRate(double value) {
        return Math.round(value * 100D) / 100D;
    }

    /**
     * 针对单条计划补齐窗口期内缺失的提醒。
     *
     * <p>这里不会删除已有提醒，只会把窗口期内缺失的提醒补齐。
     * 这样定时任务既能兜底，也不会影响用户已经产生的提醒记录状态。
     */
    private int appendMissingReminders(HealthMedicationPlanEntity planEntity, Date windowStart, Date windowEnd, Date now,
        Set<Long> existingScheduledTimes, String displayDrugName) {
        List<String> reminderTimes = parseReminderTimes(planEntity.getReminderTimesJson());
        if (reminderTimes.isEmpty()) {
            return 0;
        }

        Date effectiveWindowStart = laterTime(DateUtil.beginOfDay(planEntity.getStartDate()), DateUtil.beginOfDay(windowStart));
        Date effectiveWindowEnd = earlierTime(resolveReminderWindowEnd(planEntity.getEndDate(), effectiveWindowStart,
            Math.max((int) DateUtil.betweenDay(effectiveWindowStart, windowEnd, true) + 1, 1)), DateUtil.endOfDay(windowEnd));
        if (effectiveWindowStart.after(effectiveWindowEnd)) {
            return 0;
        }

        if (StrUtil.isBlank(displayDrugName)) {
            return 0;
        }

        List<HealthMedicationReminderEntity> reminders = buildReminderRecords(planEntity, reminderTimes,
            displayDrugName, effectiveWindowStart, effectiveWindowEnd, now,
            existingScheduledTimes == null ? Collections.emptySet() : existingScheduledTimes);
        if (reminders.isEmpty()) {
            return 0;
        }

        medicationReminderService.saveBatch(reminders);
        return reminders.size();
    }

    /**
     * 统一构建提醒记录。
     *
     * <p>新增计划、修改计划后的全量重建，和定时任务的窗口补齐，
     * 最终都会落到这一个构建方法，确保提醒生成规则完全一致。
     */
    private List<HealthMedicationReminderEntity> buildReminderRecords(HealthMedicationPlanEntity planEntity,
        List<String> reminderTimes, String displayDrugName, Date windowStart, Date windowEnd, Date now,
        Set<Long> existingScheduledTimes) {
        if (windowStart == null || windowEnd == null || windowStart.after(windowEnd)) {
            return Collections.emptyList();
        }

        Date loopDate = DateUtil.beginOfDay(windowStart);
        Date endDate = DateUtil.endOfDay(windowEnd);
        List<HealthMedicationReminderEntity> reminders = new ArrayList<>();
        while (!loopDate.after(endDate)) {
            if (!isPlanEffectiveOnDate(planEntity, loopDate)) {
                loopDate = DateUtil.offsetDay(loopDate, 1);
                continue;
            }

            List<String> effectiveReminderTimes = buildReminderTimesForDate(planEntity, reminderTimes, loopDate);
            if (effectiveReminderTimes.isEmpty()) {
                loopDate = DateUtil.offsetDay(loopDate, 1);
                continue;
            }

            String dateText = DateUtil.formatDate(loopDate);
            for (String reminderTime : effectiveReminderTimes) {
                Date scheduledTime;
                try {
                    scheduledTime = DateUtil.parse(dateText + " " + reminderTime, "yyyy-MM-dd HH:mm");
                } catch (Exception ex) {
                    // 历史脏数据中的非法时间格式直接跳过，避免一条坏数据影响整批任务执行。
                    continue;
                }
                long scheduledTimeMillis = scheduledTime.getTime();
                if (scheduledTime.before(now) || existingScheduledTimes.contains(scheduledTimeMillis)) {
                    continue;
                }

                HealthMedicationReminderEntity reminderEntity = new HealthMedicationReminderEntity();
                reminderEntity.setOwnerUserId(planEntity.getOwnerUserId());
                reminderEntity.setPlanId(planEntity.getPlanId());
                reminderEntity.setMemberId(planEntity.getMemberId());
                reminderEntity.setReminderDate(DateUtil.beginOfDay(loopDate));
                reminderEntity.setScheduledTime(scheduledTime);
                reminderEntity.setDrugNameSnapshot(displayDrugName);
                MedicationDoseStageRule.MedicationDoseSnapshot doseSnapshot =
                    MedicationDoseStageRule.resolveDoseForDate(planEntity.getDoseRule(), planEntity.getStartDate(), loopDate);
                // 阶段剂量命中时，提醒记录保存阶段快照；未配置或未命中时继续沿用计划基础剂量。
                reminderEntity.setDoseAmount(doseSnapshot == null ? planEntity.getDoseAmount() : doseSnapshot.getDoseAmount());
                reminderEntity.setDoseUnit(doseSnapshot == null ? planEntity.getDoseUnit() : doseSnapshot.getDoseUnit());
                reminderEntity.setMealTiming(planEntity.getMealTiming());
                reminderEntity.setReminderStatus(MedicationReminderStatusEnum.PENDING.getValue());
                reminderEntity.setNotifyStatus(MedicationReminderNotifyStatusEnum.PENDING.getValue());
                reminderEntity.setNotifyRetryCount(0);
                reminders.add(reminderEntity);

                // 把本次新加入的提醒时间也记入集合，避免脏数据里的重复提醒时间被重复插入。
                existingScheduledTimes.add(scheduledTimeMillis);
            }
            loopDate = DateUtil.offsetDay(loopDate, 1);
        }
        return reminders;
    }

    /**
     * 判断计划在指定日期是否需要生效。
     *
     * <p>这里把频率判断集中到一个方法中，目的是：
     * 1. 新增计划时和定时补齐时共用同一套规则
     * 2. 后续继续增加更多频率类型时，只需要维护这一处核心判断
     */
    private boolean isPlanEffectiveOnDate(HealthMedicationPlanEntity planEntity, Date currentDate) {
        if (planEntity == null || currentDate == null) {
            return false;
        }
        Date safeCurrentDate = DateUtil.beginOfDay(currentDate);
        Date startDate = DateUtil.beginOfDay(planEntity.getStartDate());
        Date endDate = planEntity.getEndDate() == null ? null : DateUtil.endOfDay(planEntity.getEndDate());
        if (safeCurrentDate.before(startDate) || (endDate != null && safeCurrentDate.after(endDate))) {
            return false;
        }

        String frequencyType = StrUtil.blankToDefault(planEntity.getFrequencyType(), "DAILY");
        if (Objects.equals(frequencyType, "DAILY")) {
            return true;
        }
        if (Objects.equals(frequencyType, "EVERY_OTHER_DAY")) {
            long diffDays = (safeCurrentDate.getTime() - startDate.getTime()) / (24L * 60 * 60 * 1000);
            return diffDays % 2 == 0;
        }
        if (Objects.equals(frequencyType, "WEEKLY")) {
            return parseWeeklyDays(planEntity.getWeeklyDaysJson()).contains(resolveIsoDayOfWeek(safeCurrentDate));
        }
        if (Objects.equals(frequencyType, "INTERVAL_HOURS")) {
            return planEntity.getIntervalHours() != null && planEntity.getIntervalHours() > 0;
        }
        if (Objects.equals(frequencyType, "INTERVAL_DAYS")) {
            int intervalDays = planEntity.getIntervalDays() == null ? 0 : planEntity.getIntervalDays();
            long diffDays = (safeCurrentDate.getTime() - startDate.getTime()) / (24L * 60 * 60 * 1000);
            return intervalDays > 0 && diffDays % intervalDays == 0;
        }
        return true;
    }

    /**
     * 解析提醒生成窗口的结束时间。
     *
     * <p>结束日期为空代表长期计划，不能直接传给 DateUtil.endOfDay。
     * 这里按窗口天数生成一个临时结束点，既能让新增计划马上看到近期提醒，
     * 也能交给定时补齐任务继续滚动生成后续提醒。
     */
    private Date resolveReminderWindowEnd(Date planEndDate, Date windowStart, int fallbackDays) {
        if (planEndDate != null) {
            return DateUtil.endOfDay(planEndDate);
        }
        Date safeWindowStart = windowStart == null ? DateUtil.beginOfDay(new Date()) : DateUtil.beginOfDay(windowStart);
        return DateUtil.endOfDay(DateUtil.offsetDay(safeWindowStart, Math.max(fallbackDays, 1) - 1));
    }

    /**
     * 根据计划频率生成指定日期实际生效的提醒时间点列表。
     */
    private List<String> buildReminderTimesForDate(HealthMedicationPlanEntity planEntity, List<String> reminderTimes, Date currentDate) {
        if (planEntity == null) {
            return Collections.emptyList();
        }
        List<String> baseReminderTimes = reminderTimes == null ? Collections.emptyList() : reminderTimes;
        String frequencyType = StrUtil.blankToDefault(planEntity.getFrequencyType(), "DAILY");
        if (!Objects.equals(frequencyType, "INTERVAL_HOURS")) {
            return baseReminderTimes;
        }

        int intervalHours = planEntity.getIntervalHours() == null ? 0 : planEntity.getIntervalHours();
        if (intervalHours <= 0) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> generatedTimes = new LinkedHashSet<>();
        List<String> anchorTimes = baseReminderTimes.isEmpty() ? Collections.singletonList("08:00") : baseReminderTimes;
        for (String anchorTime : anchorTimes) {
            try {
                Date anchorDateTime = DateUtil.parse(DateUtil.formatDate(currentDate) + " " + anchorTime, "yyyy-MM-dd HH:mm");
                Date cursor = anchorDateTime;
                while (DateUtil.isSameDay(cursor, currentDate)) {
                    generatedTimes.add(DateUtil.format(cursor, "HH:mm"));
                    cursor = DateUtil.offsetHour(cursor, intervalHours);
                }
            } catch (Exception ex) {
                // 历史脏数据或不合规时间格式在领域层兜底跳过，避免一条非法数据阻塞整个计划生成。
            }
        }
        return generatedTimes.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 解析每周提醒的星期列表。
     */
    private List<Integer> parseWeeklyDays(String weeklyDaysJson) {
        if (StrUtil.isBlank(weeklyDaysJson)) {
            return Collections.emptyList();
        }
        List<Integer> weeklyDays = JacksonUtil.fromList(weeklyDaysJson, Integer.class);
        if (weeklyDays == null) {
            return Collections.emptyList();
        }
        return weeklyDays.stream()
            .filter(Objects::nonNull)
            .filter(day -> day >= 1 && day <= 7)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * 把 Java Calendar 的星期值转换成 ISO 星期值。
     *
     * <p>ISO 约定：周一=1，周日=7。
     * 这样可以与前后端约定的 weeklyDays 值保持一致。
     */
    private int resolveIsoDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
    }

    /**
     * 解析提醒时间JSON。
     * 这里单独做保护，是为了让定时任务在面对历史脏数据时尽量“跳过坏数据、继续服务”。
     */
    private List<String> parseReminderTimes(String reminderTimesJson) {
        return StrUtil.isBlank(reminderTimesJson)
            ? Collections.emptyList()
            : JacksonUtil.fromList(reminderTimesJson, String.class);
    }

    /**
     * 派发单条提醒，并把发送结果回写到提醒表。
     */
    private boolean dispatchSingleReminder(HealthMedicationReminderEntity reminderEntity, Date now) {
        MedicationReminderNotice notice = buildReminderNotice(reminderEntity);
        if (notice == null) {
            markReminderNotifyFailed(reminderEntity, now, "提醒缺少必要发送数据，已跳过本次发送");
            return false;
        }

        HealthAppMessageEntity messageEntity = appMessageApplicationService.createOrReuseMessage(
            buildReminderMessageCreateRequest(notice));
        notice.setMessageId(messageEntity.getMessageId());
        try {
            MedicationReminderNotifyResult notifyResult = medicationReminderNotifier.notify(notice);
            if (notifyResult != null && notifyResult.isSuccess()) {
                appMessageApplicationService.markMessageSendSuccess(messageEntity.getMessageId(),
                    notifyResult.getChannel(), notifyResult.getMessage());
                reminderEntity.setNotifyStatus(MedicationReminderNotifyStatusEnum.SUCCESS.getValue());
                reminderEntity.setNotifyTime(now);
                reminderEntity.setNotifyFailReason(null);
                medicationReminderService.updateById(reminderEntity);
                return true;
            }

            String failMessage = notifyResult == null ? "提醒发送器未返回结果" : notifyResult.getMessage();
            appMessageApplicationService.markMessageSendFailed(messageEntity.getMessageId(),
                notifyResult == null ? null : notifyResult.getChannel(), failMessage);
            markReminderNotifyFailed(reminderEntity, now, failMessage);
            return false;
        } catch (Exception ex) {
            appMessageApplicationService.markMessageSendFailed(messageEntity.getMessageId(), null, ex.getMessage());
            markReminderNotifyFailed(reminderEntity, now, ex.getMessage());
            return false;
        }
    }

    /**
     * 构建发送给通知通道的标准载荷。
     */
    private MedicationReminderNotice buildReminderNotice(HealthMedicationReminderEntity reminderEntity) {
        if (reminderEntity == null || StrUtil.isBlank(reminderEntity.getDrugNameSnapshot())
            || reminderEntity.getScheduledTime() == null) {
            return null;
        }

        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(reminderEntity.getMemberId());
        return MedicationReminderNotice.builder()
            .reminderId(reminderEntity.getReminderId())
            .ownerUserId(reminderEntity.getOwnerUserId())
            .memberId(reminderEntity.getMemberId())
            .memberName(memberEntity == null ? null : memberEntity.getMemberName())
            .drugName(reminderEntity.getDrugNameSnapshot())
            .scheduledTime(reminderEntity.getScheduledTime())
            .doseAmount(reminderEntity.getDoseAmount())
            .doseUnit(reminderEntity.getDoseUnit())
            .mealTiming(reminderEntity.getMealTiming())
            .pushPayload(buildReminderPushPayload(reminderEntity, memberEntity == null ? null : memberEntity.getMemberName()))
            .build();
    }

    /**
     * 构建消息中心落库请求。
     *
     * <p>用药提醒采用“每条提醒一条消息”的策略：
     * 1. 同一提醒重试发送时，复用同一条消息
     * 2. 不同提醒即使药名相同、成员相同，也应保留各自独立消息轨迹
     */
    private HealthAppMessageCreateRequest buildReminderMessageCreateRequest(MedicationReminderNotice notice) {
        return HealthAppMessageCreateRequest.builder()
            .ownerUserId(notice.getOwnerUserId())
            .memberId(notice.getMemberId())
            .memberName(notice.getMemberName())
            .businessScene(HealthAppMessageSceneEnum.MEDICATION_REMINDER.getValue())
            .businessId(notice.getReminderId())
            .messageTitle(notice.getPushPayload() == null ? null : notice.getPushPayload().getTitle())
            .messageContent(notice.getPushPayload() == null ? null : notice.getPushPayload().getContent())
            .payload(notice.getPushPayload())
            .dedupKey(buildReminderMessageDedupKey(notice.getReminderId()))
            .build();
    }

    /**
     * 构建用药提醒消息去重键。
     */
    private String buildReminderMessageDedupKey(Long reminderId) {
        return reminderId == null
            ? null
            : HealthAppMessageSceneEnum.MEDICATION_REMINDER.getValue() + ":" + reminderId;
    }

    /**
     * 构建提醒 Push 的标准业务透传载荷。
     *
     * <p>这里明确复用首页任务体系里的：
     * 1. `navigation`
     * 2. `recommendedAction`
     *
     * <p>让用户无论从首页任务进入，还是从系统通知进入，
     * 最终看到的业务语义、跳转目标和建议动作都保持一致。
     */
    private HealthAppPushPayloadDTO buildReminderPushPayload(HealthMedicationReminderEntity reminderEntity, String memberName) {
        HealthFollowUpNavigationDTO navigationDTO = buildReminderNavigation(reminderEntity.getReminderId());
        HealthFollowUpRecommendedActionDTO recommendedActionDTO =
            buildReminderRecommendedAction(reminderEntity, navigationDTO);

        HealthAppPushPayloadDTO payloadDTO = new HealthAppPushPayloadDTO();
        payloadDTO.setBusinessScene("MEDICATION_REMINDER");
        payloadDTO.setBusinessId(reminderEntity.getReminderId());
        payloadDTO.setTitle(StrUtil.format("{}的用药提醒待处理", StrUtil.blankToDefault(memberName, "家庭成员")));
        payloadDTO.setContent(StrUtil.format("{}{}，请及时确认是否已服药。",
            StrUtil.blankToDefault(reminderEntity.getDrugNameSnapshot(), "有一条用药提醒"),
            reminderEntity.getScheduledTime() == null
                ? ""
                : StrUtil.format("，计划时间 {}", DateUtil.format(reminderEntity.getScheduledTime(), "HH:mm"))));
        payloadDTO.setNavigation(navigationDTO);
        payloadDTO.setRecommendedAction(recommendedActionDTO);
        return payloadDTO;
    }

    /**
     * 构建提醒通知统一跳转参数。
     */
    private HealthFollowUpNavigationDTO buildReminderNavigation(Long reminderId) {
        if (reminderId == null) {
            return null;
        }
        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetPageCode(HealthFollowUpTargetPageEnum.MEDICATION_REMINDER_DETAIL.getValue());
        navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(
            HealthFollowUpTargetPageEnum.MEDICATION_REMINDER_DETAIL.getValue()));
        navigationDTO.setTargetBizId(reminderId);
        navigationDTO.setTargetBizType(HealthFollowUpTaskTypeEnum.REMINDER.getValue());
        navigationDTO.setTargetTabCode("PROCESS");
        navigationDTO.setTargetAnchorCode(HealthFollowUpTargetAnchorEnum.REMINDER_FEEDBACK.getValue());
        navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(
            HealthFollowUpTargetAnchorEnum.REMINDER_FEEDBACK.getValue()));
        return navigationDTO;
    }

    /**
     * 构建提醒通知推荐动作。
     *
     * <p>提醒通知只会在“已到提醒时间”的场景发送，
     * 因此默认就属于高优先关注动作。
     */
    private HealthFollowUpRecommendedActionDTO buildReminderRecommendedAction(HealthMedicationReminderEntity reminderEntity,
        HealthFollowUpNavigationDTO navigationDTO) {
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        actionDTO.setActionPriority(1);
        actionDTO.setRiskLevel(HealthFollowUpRiskLevelEnum.HIGH.getValue());
        actionDTO.setRiskLevelName(HealthAppI18n.followUpRiskLevelName(HealthFollowUpRiskLevelEnum.HIGH.getValue()));
        actionDTO.setRiskCssTag(HealthFollowUpRiskLevelEnum.HIGH.cssTag());
        actionDTO.setTitle("优先确认本次提醒结果");
        actionDTO.setContent(StrUtil.format("{}{}，建议尽快确认是否已服药；如本次无法执行，可进入提醒页选择跳过并记录原因。",
            StrUtil.blankToDefault(reminderEntity.getDrugNameSnapshot(), "本次用药提醒"),
            reminderEntity.getScheduledTime() == null
                ? ""
                : StrUtil.format("，计划时间为 {}", DateUtil.format(reminderEntity.getScheduledTime(), "yyyy-MM-dd HH:mm"))));
        actionDTO.setActionText("去处理提醒");
        actionDTO.setNavigation(navigationDTO);
        return actionDTO;
    }

    /**
     * 回写发送失败状态。
     * 失败后会累计重试次数，便于定时任务后续做有限次重试。
     */
    private void markReminderNotifyFailed(HealthMedicationReminderEntity reminderEntity, Date now, String failReason) {
        reminderEntity.setNotifyStatus(MedicationReminderNotifyStatusEnum.FAILED.getValue());
        reminderEntity.setNotifyTime(now);
        reminderEntity.setNotifyRetryCount(Objects.requireNonNullElse(reminderEntity.getNotifyRetryCount(), 0) + 1);
        reminderEntity.setNotifyFailReason(limitLength(StrUtil.blankToDefault(failReason, "提醒发送失败"), 255));
        medicationReminderService.updateById(reminderEntity);
    }

    /**
     * 解析计划真正用于展示和提醒快照的药品名称。
     * 如果绑定药品仍然存在，则优先用药品主数据名称；否则退回自定义药名。
     */
    private String resolveDisplayDrugName(HealthMedicationPlanEntity planEntity) {
        if (planEntity.getDrugId() != null) {
            DrugEntity drugEntity = drugService.getById(planEntity.getDrugId());
            if (drugEntity != null) {
                return drugEntity.getDrugName();
            }
        }
        return planEntity.getCustomDrugName();
    }

    /**
     * 用预加载药品快照解析计划展示药名。
     *
     * <p>该方法主要服务“同一批计划一起补提醒”的高频场景。
     * 如果药品仍存在，优先取主数据名称；否则退回计划上保留的自定义药名。
     */
    private String resolveDisplayDrugName(HealthMedicationPlanEntity planEntity, Map<Long, DrugEntity> drugMap) {
        if (planEntity == null) {
            return null;
        }
        if (planEntity.getDrugId() != null && drugMap != null) {
            DrugEntity drugEntity = drugMap.get(planEntity.getDrugId());
            if (drugEntity != null) {
                return drugEntity.getDrugName();
            }
        }
        return planEntity.getCustomDrugName();
    }

    /**
     * 一次性加载指定窗口内、指定计划集合已经存在的提醒时间点。
     *
     * <p>返回结构为：
     * planId -> scheduledTimeMillis Set
     *
     * 这样逐计划补齐时无需再分别查 reminder 表，只需要在内存里判断该时间点是否已存在即可。
     */
    private Map<Long, Set<Long>> loadExistingScheduledTimeMap(List<HealthMedicationPlanEntity> planEntities,
        Date windowStart, Date windowEnd) {
        if (planEntities == null || planEntities.isEmpty() || windowStart == null || windowEnd == null) {
            return Collections.emptyMap();
        }
        Set<Long> planIds = planEntities.stream()
            .map(HealthMedicationPlanEntity::getPlanId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (planIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Long>> existingScheduledTimeMap = new HashMap<>();
        medicationReminderService.lambdaQuery()
            .in(HealthMedicationReminderEntity::getPlanId, planIds)
            .ge(HealthMedicationReminderEntity::getScheduledTime, windowStart)
            .le(HealthMedicationReminderEntity::getScheduledTime, windowEnd)
            .list()
            .forEach(reminderEntity -> {
                if (reminderEntity.getPlanId() == null || reminderEntity.getScheduledTime() == null) {
                    return;
                }
                existingScheduledTimeMap
                    .computeIfAbsent(reminderEntity.getPlanId(), key -> new HashSet<>())
                    .add(reminderEntity.getScheduledTime().getTime());
            });
        return existingScheduledTimeMap;
    }

    /**
     * 对失败原因做长度保护，避免数据库字段溢出。
     */
    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    /**
     * 取两个时间里更晚的那个，用于确定提醒生成窗口的开始时间。
     */
    private Date laterTime(Date firstTime, Date secondTime) {
        return firstTime.after(secondTime) ? firstTime : secondTime;
    }

    /**
     * 取两个时间里更早的那个，用于确定提醒生成窗口的结束时间。
     */
    private Date earlierTime(Date firstTime, Date secondTime) {
        return firstTime.before(secondTime) ? firstTime : secondTime;
    }
}
