package com.healthtrail.domain.health.insight;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import com.healthtrail.domain.health.chronic.ChronicDiseaseApplicationService;
import com.healthtrail.domain.health.chronic.command.SaveChronicDiseaseProfileCommand;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseProfileDTO;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileService;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseTypeEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseTypeService;
import com.healthtrail.domain.health.chronic.db.HealthChronicIndicatorTargetEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicIndicatorTargetService;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskService;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchService;
import com.healthtrail.domain.health.family.FamilyMemberAccessService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.family.dto.FamilyMemberAccessContextDTO;
import com.healthtrail.domain.health.insight.command.CreateVoiceDiaryEntryCommand;
import com.healthtrail.domain.health.insight.command.CreateCareActionTaskCommand;
import com.healthtrail.domain.health.insight.command.CareActionTaskActionCommand;
import com.healthtrail.domain.health.insight.command.ConfirmReviewSuggestionCommand;
import com.healthtrail.domain.health.insight.command.SaveChronicDiaryEntryCommand;
import com.healthtrail.domain.health.insight.command.SaveDailyIndicatorRecordCommand;
import com.healthtrail.domain.health.insight.command.UpdateHealthProblemStatusCommand;
import com.healthtrail.domain.health.insight.command.LinkHealthProblemChronicProfileCommand;
import com.healthtrail.domain.health.insight.db.HealthChronicDiaryEntryEntity;
import com.healthtrail.domain.health.insight.db.HealthChronicDiaryEntryService;
import com.healthtrail.domain.health.insight.dto.ChronicDiaryEntryDTO;
import com.healthtrail.domain.health.insight.dto.ChronicDiseaseTemplateDTO;
import com.healthtrail.domain.health.insight.dto.HealthProblemCenterDTO;
import com.healthtrail.domain.health.insight.dto.HealthProblemChronicProfileDTO;
import com.healthtrail.domain.health.insight.dto.HealthProblemStatusLogDTO;
import com.healthtrail.domain.health.insight.dto.MedicalVisitPackageExportDTO;
import com.healthtrail.domain.health.insight.dto.HealthInsightWorkbenchDTO;
import com.healthtrail.domain.health.insight.dto.HealthLongTermManagementDTO;
import com.healthtrail.domain.health.insight.dto.HealthPrivacyDataExportDTO;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemChronicProfileLinkEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemChronicProfileLinkService;
import com.healthtrail.domain.health.problem.db.HealthProblemEvidenceEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemEvidenceService;
import com.healthtrail.domain.health.problem.db.HealthProblemService;
import com.healthtrail.domain.health.problem.db.HealthProblemStatusLogEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemStatusLogService;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemService;
import com.healthtrail.domain.health.report.db.HealthReportService;
import com.healthtrail.domain.system.member.MemberGateApplicationService;
import com.healthtrail.domain.system.member.MemberGateCodeConstants;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 健康洞察应用服务。
 *
 * <p>该服务承接健康洞察与长期管理能力的 1.0 聚合层，但不会重写已有业务模块：
 * 1. 慢病复查继续复用 `operation_task`
 * 2. 指标预警继续复用 `report/report_item`
 * 3. 就医资料包复用成员、报告、用药、慢病档案
 * 4. 慢病日记只新增一张通用记录表
 * 5. 照护看板复用成员、提醒、报告、慢病风险
 * 6. 用药安全复用用药计划和药品库存
 * 7. 周报/月报先做规则化摘要，后续可升级为深度解读
 * 8. 语音录入先承接 ASR 文本，后续可扩展音频处理链路
 * 9. 时间线复用各模块最近事件
 * 10. 家庭长期管理把计划、指标、随访、协作和隐私摘要放到同一个视图
 */
@Service
@RequiredArgsConstructor
public class HealthInsightApplicationService {

    private static final int RECENT_REPORT_LIMIT = 20;
    private static final int TIMELINE_LIMIT = 30;
    private static final int NEAR_EXPIRE_DAYS = 30;
    private static final String SOURCE_TYPE_MANUAL = "MANUAL";
    private static final String SOURCE_TYPE_VOICE = "VOICE";
    private static final String SOURCE_TYPE_INDICATOR = "INDICATOR";
    private static final String TARGET_BIZ_TYPE_CARE_ACTION = "CARE_ACTION";
    private static final String TARGET_BIZ_TYPE_LONG_TERM_REVIEW = "LONG_TERM_REVIEW";
    private static final String TARGET_BIZ_TYPE_INDICATOR_REVIEW = "INDICATOR_REVIEW";
    private static final String PROBLEM_STATUS_ACTION_CHANGE = "STATUS_CHANGE";
    private static final String PROBLEM_STATUS_ACTION_REVIEW_TASK_COMPLETE = "REVIEW_TASK_COMPLETE";
    private static final int PROBLEM_STATUS_LOG_LIMIT = 50;
    private static final String HEALTH_PROBLEM_REVIEW_TASK_TYPE = "HEALTH_PROBLEM_REVIEW";
    /**
     * 指标波动预警阈值。
     *
     * <p>这里先使用 20% 作为轻量规则阈值，只用于健康洞察列表排序和提示，
     * 不作为医学判断结论。后续如果做病种级规则配置，可以把该值迁移到配置表。
     */
    private static final BigDecimal INDICATOR_VOLATILITY_PERCENT_THRESHOLD = new BigDecimal("20");

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");


    /** 家庭成员访问控制服务 */
    private final FamilyMemberAccessService familyMemberAccessService;
    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;
    /** 慢病管理应用服务 */
    private final ChronicDiseaseApplicationService chronicDiseaseApplicationService;
    /** 慢病档案数据库服务 */
    private final HealthChronicDiseaseProfileService chronicDiseaseProfileService;
    /** 慢病类型数据库服务 */
    private final HealthChronicDiseaseTypeService chronicDiseaseTypeService;
    /** 慢病指标目标数据库服务 */
    private final HealthChronicIndicatorTargetService chronicIndicatorTargetService;
    /** 运营任务数据库服务 */
    private final HealthOperationTaskService operationTaskService;
    /** 报告主表数据库服务 */
    private final HealthReportService reportService;
    /** 报告指标项数据库服务 */
    private final HealthReportItemService reportItemService;
    /** 用药计划数据库服务 */
    private final HealthMedicationPlanService medicationPlanService;
    /** 用药提醒数据库服务 */
    private final HealthMedicationReminderService medicationReminderService;
    /** 药品主表数据库服务 */
    private final HealthDrugService drugService;
    /** 药品批号效期库存数据库服务 */
    private final HealthDrugStockBatchService drugStockBatchService;
    /** 慢病日记数据库服务 */
    private final HealthChronicDiaryEntryService chronicDiaryEntryService;
    /** 健康问题数据库服务 */
    private final HealthProblemService healthProblemService;
    /** 健康问题证据数据库服务 */
    private final HealthProblemEvidenceService healthProblemEvidenceService;
    /** 健康问题慢病档案关联数据库服务 */
    private final HealthProblemChronicProfileLinkService healthProblemChronicProfileLinkService;
    /** 健康问题状态日志数据库服务 */
    private final HealthProblemStatusLogService healthProblemStatusLogService;
    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;
    /** 会员门禁应用服务 */
    private final MemberGateApplicationService memberGateApplicationService;

    /**
     * 查询健康洞察工作台。
     *
     * <p>该方法只做当前账号可访问成员的数据聚合，不扩大权限边界。
     * 所有成员维度的数据都先经过 `FamilyMemberAccessService` 解析，
     * 这样共享成员、只读成员和自有成员可以在同一工作台中统一展示。
     */
    public HealthInsightWorkbenchDTO getWorkbench(Long currentUserId) {
        List<HealthFamilyMemberEntity> members = listAccessibleMembers(currentUserId);
        if (members.isEmpty()) {
            return buildEmptyWorkbench();
        }

        Set<Long> memberIds = members.stream()
            .map(HealthFamilyMemberEntity::getMemberId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HealthFamilyMemberEntity> memberMap = members.stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, Function.identity(), (left, right) -> left,
                LinkedHashMap::new));
        List<HealthChronicDiseaseProfileEntity> profiles = listActiveProfiles(memberIds);
        List<HealthOperationTaskEntity> reviewTasks = listActiveReviewTasks(memberIds);
        List<HealthReportEntity> reports = listRecentReports(memberIds);
        Map<Long, HealthReportEntity> reportMap = reports.stream()
            .collect(Collectors.toMap(HealthReportEntity::getReportId, Function.identity(), (left, right) -> left));
        List<HealthReportItemEntity> reportItems = listReportItems(reportMap.keySet());
        List<HealthMedicationPlanEntity> activePlans = listActiveMedicationPlans(memberIds);
        List<HealthMedicationReminderEntity> pendingReminders = listPendingReminders(memberIds);
        List<HealthChronicDiaryEntryEntity> latestDiaryEntries = listLatestDiaryEntries(memberIds, 5);
        List<HealthChronicDiaryEntryEntity> dailyIndicatorEntries = listRecentDailyIndicatorEntries(memberIds, 24);
        List<HealthProblemStatusLogEntity> problemStatusLogs = listRecentProblemStatusLogs(memberIds, 8);
        List<HealthProblemEntity> healthProblems = listHealthProblems(memberIds);
        List<HealthOperationTaskEntity> careActionTasks = listActiveCareActionTasks(memberIds);
        List<HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO> indicatorAlerts =
            buildIndicatorAlerts(reportItems, reportMap, memberMap);

        HealthInsightWorkbenchDTO dto = new HealthInsightWorkbenchDTO();
        dto.setReviewLoop(buildReviewLoop(profiles, reviewTasks));
        dto.setIndicatorAlerts(indicatorAlerts);
        dto.setMedicalVisitPackage(buildMedicalVisitPackage(members, profiles, reports, activePlans, reportItems));
        dto.setChronicDiary(buildChronicDiarySummary(memberIds, latestDiaryEntries, memberMap));
        dto.setCareDashboard(buildCareDashboard(members, profiles, reports, reportItems, pendingReminders, memberMap));
        dto.setMedicationSafety(buildMedicationSafety(currentUserId, activePlans));
        dto.setPeriodicReports(buildPeriodicReports(dto));
        dto.setAiPeriodicReport(dto.getPeriodicReports().isEmpty()
            ? buildPeriodicReport(dto, "WEEKLY")
            : dto.getPeriodicReports().get(0));
        dto.setVoiceEntry(buildVoiceEntryGuide());
        dto.setTimelineItems(buildTimeline(memberMap, reports, pendingReminders, reviewTasks, latestDiaryEntries, problemStatusLogs));
        dto.setLongTermManagement(buildLongTermManagement(members, profiles, reviewTasks, reports, reportItems,
            activePlans, pendingReminders, dailyIndicatorEntries, healthProblems, careActionTasks, indicatorAlerts,
            dto, currentUserId));
        return dto;
    }
    /** 保存手动慢病日记。 */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiaryEntryDTO createDiaryEntry(SaveChronicDiaryEntryCommand command, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        HealthChronicDiaryEntryEntity entity = new HealthChronicDiaryEntryEntity();
        fillDiaryEntry(entity, command.getMemberId(), accessContext.getOwnerUserId(), command.getProfileId(),
            command.getDiseaseCode(), command.getEntryType(), command.getRecordTime(), command.getEntryTitle(),
            command.getEntryContent(), command.getMetricPayloadJson(), SOURCE_TYPE_MANUAL, command.getRemark());
        chronicDiaryEntryService.save(entity);
        return new ChronicDiaryEntryDTO(entity, resolveMemberName(command.getMemberId()));
    }

    /**
     * 保存语音慢病日记。
     *
     * <p>当前输入是 ASR 后的文本。App 可以先把本地或云端识别结果交给这条业务链路，
     * 服务端负责把结果纳入统一慢病日记和健康时间线。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiaryEntryDTO createVoiceDiaryEntry(CreateVoiceDiaryEntryCommand command, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        HealthChronicDiaryEntryEntity entity = new HealthChronicDiaryEntryEntity();
        fillDiaryEntry(entity, command.getMemberId(), accessContext.getOwnerUserId(), command.getProfileId(),
            command.getDiseaseCode(), "GENERAL", command.getRecordTime(), "语音健康记录", command.getRecognizedText(),
            null, SOURCE_TYPE_VOICE, null);
        chronicDiaryEntryService.save(entity);
        return new ChronicDiaryEntryDTO(entity, resolveMemberName(command.getMemberId()));
    }

    /**
     * 查询家庭健康长期管理聚合数据。
     *
     * <p>该方法复用健康洞察工作台已经完成的批量聚合，避免长期管理页再次逐项查询同一批成员、
     * 报告、用药、问题和日记数据。独立路径只用于后续页面单独刷新长期管理区域。
     */
    public HealthLongTermManagementDTO getLongTermManagement(Long currentUserId) {
        return getWorkbench(currentUserId).getLongTermManagement();
    }

    /**
     * 保存日常指标记录。
     *
     * <p>App 可以在记录指标时选择一个慢病专项。后端不能直接信任传入的 `profileId`，
     * 必须校验该专项属于本次记录的家庭成员；校验通过后，病种编码统一使用专项快照值，
     * 避免出现“记录挂在 A 档案，病种编码却写成 B 病种”的脏数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiaryEntryDTO createDailyIndicatorRecord(SaveDailyIndicatorRecordCommand command,
        Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        HealthChronicDiseaseProfileEntity profile = loadDailyIndicatorProfile(command.getMemberId(),
            command.getProfileId());
        String entryType = normalizeDailyIndicatorType(command.getIndicatorType());
        HealthChronicDiaryEntryEntity entity = new HealthChronicDiaryEntryEntity();
        fillDiaryEntry(entity, command.getMemberId(), accessContext.getOwnerUserId(),
            profile == null ? null : profile.getProfileId(),
            resolveDailyIndicatorDiseaseCode(command.getDiseaseCode(), profile), entryType, command.getRecordTime(),
            resolveDailyIndicatorTitle(command, entryType),
            buildDailyIndicatorContent(command, entryType), buildDailyIndicatorMetricPayload(command, entryType),
            SOURCE_TYPE_INDICATOR, command.getNote());
        chronicDiaryEntryService.save(entity);
        return new ChronicDiaryEntryDTO(entity, resolveMemberName(command.getMemberId()));
    }

    /**
     * 创建家庭照护任务。
     *
     * <p>照护任务进入首页运营任务流，后续完成、忽略、延期等状态仍走原有任务机制，
     * 避免家庭协作能力再生成一套独立待办表。
     */
    @Transactional(rollbackFor = Exception.class)
    public void createCareActionTask(CreateCareActionTaskCommand command, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        Date now = new Date();
        HealthOperationTaskEntity task = new HealthOperationTaskEntity();
        task.setOwnerUserId(accessContext.getOwnerUserId());
        task.setMemberId(command.getMemberId());
        task.setTaskTitle(limitLength(StrUtil.trim(command.getTaskTitle()), 100));
        task.setTaskContent(limitLength(StrUtil.blankToDefault(StrUtil.trim(command.getTaskContent()), "请按家庭照护计划处理。"),
            500));
        task.setActionText(limitLength(StrUtil.blankToDefault(StrUtil.trim(command.getActionText()), "去处理"), 30));
        task.setRiskLevel(StrUtil.blankToDefault(StrUtil.trim(command.getRiskLevel()), "MEDIUM"));
        task.setPriorityWeight(resolveCareTaskPriority(task.getRiskLevel()));
        task.setTargetPageCode(HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getValue());
        task.setTargetPageName(HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getDescription());
        task.setTargetBizType(TARGET_BIZ_TYPE_CARE_ACTION);
        task.setStartTime(command.getStartTime() == null ? now : command.getStartTime());
        task.setEndTime(command.getEndTime());
        task.setStatus(StatusEnum.ENABLE.getValue());
        task.setRemark(StrUtil.trim(command.getRemark()));
        task.setCreatorId(currentUserId);
        task.setCreateTime(now);
        task.setUpdaterId(currentUserId);
        task.setUpdateTime(now);
        task.setDeleted(0);
        operationTaskService.save(task);
    }
    /**
     * 确认复查建议并生成首页任务。
     *
     * <p>建议本身来自运行时推导，不作为持久化实体保存；用户确认后才落到 `operation_task`。
     * 同一目标重复确认时刷新已有任务，避免首页出现多张相同复查卡片。
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReviewSuggestion(ConfirmReviewSuggestionCommand command, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        Date now = new Date();
        String targetBizType = resolveReviewSuggestionTaskType(command.getTargetType());
        HealthOperationTaskEntity task = loadExistingSuggestionTask(accessContext.getOwnerUserId(), command.getMemberId(),
            targetBizType, command.getTargetBizId());
        if (task == null) {
            task = new HealthOperationTaskEntity();
            task.setOwnerUserId(accessContext.getOwnerUserId());
            task.setMemberId(command.getMemberId());
            task.setTargetBizType(targetBizType);
            task.setTargetBizId(command.getTargetBizId());
            task.setCreatorId(currentUserId);
            task.setCreateTime(now);
            task.setDeleted(0);
        }
        task.setTaskTitle(limitLength(StrUtil.trim(command.getTitle()), 100));
        task.setTaskContent(limitLength(StrUtil.blankToDefault(StrUtil.trim(command.getReason()), "建议按长期管理计划安排复查。"),
            500));
        task.setActionText("去查看");
        task.setRiskLevel(StrUtil.blankToDefault(StrUtil.trim(command.getPriorityLevel()), "MEDIUM"));
        task.setPriorityWeight(resolveCareTaskPriority(task.getRiskLevel()));
        task.setTargetPageCode(HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getValue());
        task.setTargetPageName(HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getDescription());
        task.setStartTime(command.getSuggestReviewTime() == null ? now : command.getSuggestReviewTime());
        task.setEndTime(DateUtil.offsetDay(task.getStartTime(), 14));
        task.setStatus(StatusEnum.ENABLE.getValue());
        task.setRemark(limitLength("由长期管理复查建议确认生成：" + StrUtil.blankToDefault(command.getSuggestionCode(), "-"), 500));
        task.setUpdaterId(currentUserId);
        task.setUpdateTime(now);
        if (task.getOperationTaskId() == null) {
            operationTaskService.save(task);
        } else {
            operationTaskService.updateById(task);
        }
    }

    /** 完成家庭照护任务。 */
    @Transactional(rollbackFor = Exception.class)
    public void completeCareActionTask(Long operationTaskId, CareActionTaskActionCommand command, Long currentUserId) {
        closeCareActionTask(operationTaskId, command, currentUserId, "已完成");
    }

    /** 取消家庭照护任务。 */
    @Transactional(rollbackFor = Exception.class)
    public void cancelCareActionTask(Long operationTaskId, CareActionTaskActionCommand command, Long currentUserId) {
        closeCareActionTask(operationTaskId, command, currentUserId, "已取消");
    }

    /**
     * 构建长期管理隐私数据包。
     *
     * <p>该数据包按类别组织当前账号可访问的数据，适合 App 转为复制文本或本地文件。
     * 这里仍然走家庭成员访问范围，避免端上自行拼装时把无权成员的数据混入导出内容。
     */
    public HealthPrivacyDataExportDTO buildPrivacyDataExport(Long currentUserId) {
        List<HealthFamilyMemberEntity> members = listAccessibleMembers(currentUserId);
        Set<Long> memberIds = members.stream()
            .map(HealthFamilyMemberEntity::getMemberId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<HealthChronicDiseaseProfileEntity> profiles = listActiveProfiles(memberIds);
        List<HealthReportEntity> reports = listRecentReports(memberIds);
        Map<Long, HealthReportEntity> reportMap = reports.stream()
            .collect(Collectors.toMap(HealthReportEntity::getReportId, Function.identity(), (left, right) -> left));
        List<HealthReportItemEntity> reportItems = listReportItems(reportMap.keySet());
        List<HealthMedicationPlanEntity> activePlans = listActiveMedicationPlans(memberIds);
        List<HealthChronicDiaryEntryEntity> diaryEntries = listLatestDiaryEntries(memberIds, 50);
        List<HealthProblemEntity> healthProblems = listHealthProblems(memberIds);
        List<HealthOperationTaskEntity> careActionTasks = listActiveCareActionTasks(memberIds);

        HealthPrivacyDataExportDTO dto = new HealthPrivacyDataExportDTO();
        dto.setGeneratedTime(new Date());
        dto.setTitle("健康长期管理数据包");
        dto.getSections().add(HealthPrivacyDataExportDTO.section("FAMILY_MEMBER", "家庭成员", buildMemberExportItems(members)));
        dto.getSections().add(HealthPrivacyDataExportDTO.section("CHRONIC_PROFILE", "慢病专项", buildProfileExportItems(profiles)));
        dto.getSections().add(HealthPrivacyDataExportDTO.section("HEALTH_REPORT", "体检报告", buildReportExportItems(reports, reportItems)));
        dto.getSections().add(HealthPrivacyDataExportDTO.section("MEDICATION_PLAN", "用药计划", buildMedicationPlanExportItems(activePlans)));
        dto.getSections().add(HealthPrivacyDataExportDTO.section("DIARY_ENTRY", "健康记录", buildDiaryExportItems(diaryEntries)));
        dto.getSections().add(HealthPrivacyDataExportDTO.section("HEALTH_PROBLEM", "健康问题", buildProblemExportItems(healthProblems)));
        dto.getSections().add(HealthPrivacyDataExportDTO.section("CARE_TASK", "照护任务", buildCareTaskExportItems(careActionTasks)));
        dto.getTips().add("导出内容可能包含家庭成员健康资料，请只保存到可信设备。 ");
        dto.getTips().add("分享给医生或家属前，建议先检查是否包含不需要展示的成员。 ");
        dto.setSummary(StrUtil.format("本次整理{}类数据，共{}条记录。", dto.getSections().size(),
            dto.getSections().stream().mapToInt(HealthPrivacyDataExportDTO.DataSectionDTO::getItemCount).sum()));
        return dto;
    }
    /** 独立查询健康时间线，供 App 后续做完整时间线页面。 */
    public List<HealthInsightWorkbenchDTO.HealthTimelineItemDTO> listTimeline(Long memberId, Integer limit,
        Long currentUserId) {
        Set<Long> memberIds;
        if (memberId == null) {
            memberIds = familyMemberAccessService.getAccessibleMemberIds(currentUserId);
        } else {
            familyMemberAccessService.getRequiredAccessContext(memberId, currentUserId);
            memberIds = Collections.singleton(memberId);
        }
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, HealthFamilyMemberEntity> memberMap = familyMemberService.listByIds(memberIds).stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, Function.identity(), (left, right) -> left));
        List<HealthReportEntity> reports = listRecentReports(memberIds);
        List<HealthMedicationReminderEntity> reminders = listPendingReminders(memberIds);
        List<HealthOperationTaskEntity> reviewTasks = listActiveReviewTasks(memberIds);
        int safeLimit = limit == null || limit <= 0 ? TIMELINE_LIMIT : limit;
        List<HealthChronicDiaryEntryEntity> diaries = listLatestDiaryEntries(memberIds, safeLimit);
        List<HealthProblemStatusLogEntity> problemStatusLogs = listRecentProblemStatusLogs(memberIds, safeLimit);
        return buildTimeline(memberMap, reports, reminders, reviewTasks, diaries, problemStatusLogs).stream()
            .limit(safeLimit)
            .collect(Collectors.toList());
    }

    /**
     * 查询健康问题中心。
     *
     * <p>问题中心直接复用报告解析沉淀下来的 `health_problem`，并批量加载最近证据。
     * 这样 App 能按“待关注的问题”工作，而不是每次都回到某一份报告里重新找异常项。
     */
    public HealthProblemCenterDTO getHealthProblemCenter(Long memberId, Long currentUserId) {
        Set<Long> memberIds = resolveAccessibleMemberIds(memberId, currentUserId);
        if (memberIds.isEmpty()) {
            return new HealthProblemCenterDTO();
        }

        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(memberIds);
        List<HealthProblemEntity> problems = healthProblemService.lambdaQuery()
            .in(HealthProblemEntity::getMemberId, memberIds)
            .orderByDesc(HealthProblemEntity::getRiskLevel)
            .orderByAsc(HealthProblemEntity::getProblemStatus)
            .orderByDesc(HealthProblemEntity::getLastFollowDate)
            .orderByDesc(HealthProblemEntity::getProblemId)
            .list();
        Map<Long, List<HealthProblemEvidenceEntity>> evidenceMap = loadProblemEvidenceMap(problems);
        Map<Long, List<HealthProblemChronicProfileLinkEntity>> chronicProfileLinkMap = loadProblemChronicProfileLinkMap(problems);
        Map<Long, HealthChronicDiseaseProfileEntity> chronicProfileMap = loadLinkedChronicProfileMap(chronicProfileLinkMap);

        HealthProblemCenterDTO dto = new HealthProblemCenterDTO();
        dto.setTotalCount(problems.size());
        dto.setFollowingCount((int) problems.stream().filter(item -> Objects.equals(item.getProblemStatus(), 1)).count());
        dto.setRelievedCount((int) problems.stream().filter(item -> Objects.equals(item.getProblemStatus(), 2)).count());
        dto.setClosedCount((int) problems.stream().filter(item -> Objects.equals(item.getProblemStatus(), 3)).count());
        dto.setHighRiskCount((int) problems.stream().filter(item -> Objects.equals(item.getRiskLevel(), 3)).count());
        dto.setSummary(buildProblemCenterSummary(dto));
        dto.setProblems(problems.stream()
            .map(problem -> buildProblemItem(problem, memberMap, evidenceMap.get(problem.getProblemId()),
                chronicProfileLinkMap.get(problem.getProblemId()), chronicProfileMap))
            .collect(Collectors.toList()));
        return dto;
    }

    /**
     * 生成就医资料包导出数据。
     *
     * <p>这里返回结构化数据而不是文件流，原因是 App 可以复用同一份数据做预览、复制、图片/PDF 渲染和系统分享。
     * 后端负责保证资料包只包含当前账号有权限访问的成员数据。
     */
    public MedicalVisitPackageExportDTO buildMedicalVisitPackageExport(Long memberId, Long currentUserId) {
        memberGateApplicationService.ensureCurrentUserGateAllowed(currentUserId,
            MemberGateCodeConstants.HEALTH_VISIT_PACKAGE_EXPORT);
        Set<Long> memberIds = resolveAccessibleMemberIds(memberId, currentUserId);
        if (memberIds.isEmpty()) {
            MedicalVisitPackageExportDTO empty = new MedicalVisitPackageExportDTO();
            empty.setGeneratedTime(new Date());
            empty.setPackageTitle("就医资料包");
            empty.setSummary("暂无可访问成员，暂不能生成就医资料包。");
            return empty;
        }

        Long selectedMemberId = memberId == null ? memberIds.iterator().next() : memberId;
        HealthFamilyMemberEntity member = familyMemberService.getById(selectedMemberId);
        Set<Long> singletonMemberIds = Collections.singleton(selectedMemberId);
        List<HealthChronicDiseaseProfileEntity> profiles = listActiveProfiles(singletonMemberIds);
        List<HealthReportEntity> reports = listRecentReports(singletonMemberIds).stream().limit(5).collect(Collectors.toList());
        Map<Long, HealthReportEntity> reportMap = reports.stream()
            .collect(Collectors.toMap(HealthReportEntity::getReportId, Function.identity(), (left, right) -> left));
        List<HealthReportItemEntity> reportItems = listReportItems(reportMap.keySet());
        List<HealthMedicationPlanEntity> plans = listActiveMedicationPlans(singletonMemberIds);
        List<HealthProblemEntity> problems = healthProblemService.lambdaQuery()
            .eq(HealthProblemEntity::getMemberId, selectedMemberId)
            .ne(HealthProblemEntity::getProblemStatus, 3)
            .orderByDesc(HealthProblemEntity::getRiskLevel)
            .orderByDesc(HealthProblemEntity::getLastFollowDate)
            .list();
        return buildVisitPackageDTO(member, profiles, reports, reportItems, plans, problems);
    }

    /**
     * 更新健康问题状态。
     *
     * <p>只有对问题所属成员具备编辑权限的账号才能更新状态，避免只读协同账号误关闭问题。
     */
    @Transactional(rollbackFor = Exception.class)
    public HealthProblemCenterDTO.ProblemItemDTO updateHealthProblemStatus(Long problemId,
        UpdateHealthProblemStatusCommand command, Long currentUserId) {
        HealthProblemEntity problem = healthProblemService.getById(problemId);
        if (problem == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, problemId, "健康问题");
        }
        familyMemberAccessService.checkCanEditMember(problem.getMemberId(), currentUserId);

        Integer beforeStatus = problem.getProblemStatus();
        Date actionTime = new Date();
        problem.setProblemStatus(command.getProblemStatus());
        problem.setLastFollowDate(actionTime);
        if (StrUtil.isNotBlank(command.getRemark())) {
            problem.setRemark(StrUtil.trim(command.getRemark()));
        }
        healthProblemService.updateById(problem);

        // 主表只保留当前状态；每次 App 主动提交状态动作都写入审计表，方便问题中心展示处理历史。
        saveProblemStatusLog(problem, beforeStatus, command.getProblemStatus(), command.getRemark(), currentUserId,
            actionTime);

        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(Collections.singleton(problem.getMemberId()));
        Map<Long, List<HealthProblemEvidenceEntity>> evidenceMap = loadProblemEvidenceMap(Collections.singletonList(problem));
        Map<Long, List<HealthProblemChronicProfileLinkEntity>> chronicProfileLinkMap =
            loadProblemChronicProfileLinkMap(Collections.singletonList(problem));
        Map<Long, HealthChronicDiseaseProfileEntity> chronicProfileMap = loadLinkedChronicProfileMap(chronicProfileLinkMap);
        return buildProblemItem(problem, memberMap, evidenceMap.get(problem.getProblemId()),
            chronicProfileLinkMap.get(problem.getProblemId()), chronicProfileMap);
    }

    /**
     * 将健康问题设置为后续复查提醒。
     *
     * <p>这里复用首页待跟进任务流，而不是新增一套提醒表，避免健康问题、首页任务和后续处理入口出现三套状态。
     * 同一个健康问题重复设置时只刷新提醒时间和内容，避免首页出现重复卡片。
     */
    @Transactional(rollbackFor = Exception.class)
    public void createHealthProblemReviewTask(Long problemId, Long currentUserId) {
        HealthProblemEntity problem = loadRequiredProblemWithAccess(problemId, currentUserId, true);
        memberGateApplicationService.ensureCurrentUserGateAllowed(currentUserId,
            MemberGateCodeConstants.HEALTH_PROBLEM_REVIEW_TASK);
        Date now = new Date();
        Date reviewStartTime = DateUtil.offsetDay(now, 30);
        Date reviewEndTime = DateUtil.offsetDay(reviewStartTime, 14);
        HealthOperationTaskEntity task = operationTaskService.lambdaQuery()
            .eq(HealthOperationTaskEntity::getOwnerUserId, problem.getOwnerUserId())
            .eq(HealthOperationTaskEntity::getTargetBizType, HEALTH_PROBLEM_REVIEW_TASK_TYPE)
            .eq(HealthOperationTaskEntity::getTargetBizId, problemId)
            .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue())
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
        if (task == null) {
            task = new HealthOperationTaskEntity();
            task.setOwnerUserId(problem.getOwnerUserId());
            task.setMemberId(problem.getMemberId());
            task.setTargetBizType(HEALTH_PROBLEM_REVIEW_TASK_TYPE);
            task.setTargetBizId(problemId);
            task.setCreatorId(currentUserId);
            task.setCreateTime(now);
            task.setDeleted(0);
        }
        task.setTaskTitle(limitLength("复查提醒：" + StrUtil.blankToDefault(problem.getProblemName(), "健康问题"), 100));
        task.setTaskContent(limitLength(buildHealthProblemReviewTaskContent(problem), 500));
        task.setActionText("去查看");
        task.setRiskLevel(resolveOperationTaskRiskLevel(problem.getRiskLevel()));
        task.setPriorityWeight(Objects.equals(problem.getRiskLevel(), 3) ? 90 : 60);
        task.setTargetPageCode(HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getValue());
        task.setTargetPageName(HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getDescription());
        task.setStartTime(reviewStartTime);
        task.setEndTime(reviewEndTime);
        task.setStatus(StatusEnum.ENABLE.getValue());
        task.setRemark("由健康问题详情设置");
        task.setUpdaterId(currentUserId);
        task.setUpdateTime(now);
        if (task.getOperationTaskId() == null) {
            operationTaskService.save(task);
        } else {
            operationTaskService.updateById(task);
        }
    }

    private String buildHealthProblemReviewTaskContent(HealthProblemEntity problem) {
        String summary = StrUtil.blankToDefault(problem.getSummary(), "建议结合近期报告和身体情况复查确认。");
        return StrUtil.format("{}：{}", StrUtil.blankToDefault(problem.getProblemName(), "健康问题"), summary);
    }

    private String resolveOperationTaskRiskLevel(Integer riskLevel) {
        if (Objects.equals(riskLevel, 3)) {
            return "HIGH";
        }
        if (Objects.equals(riskLevel, 2)) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
    /**
     * 查询单个健康问题的状态变更历史。
     *
     * <p>读历史只要求当前账号能访问该成员；写状态仍然在 updateHealthProblemStatus 中要求编辑权限。
     */
    public List<HealthProblemStatusLogDTO> listHealthProblemStatusLogs(Long problemId, Long currentUserId) {
        HealthProblemEntity problem = healthProblemService.getById(problemId);
        if (problem == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, problemId, "健康问题");
        }
        familyMemberAccessService.getRequiredAccessContext(problem.getMemberId(), currentUserId);
        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(Collections.singleton(problem.getMemberId()));
        List<HealthProblemStatusLogEntity> logs = healthProblemStatusLogService.lambdaQuery()
            .eq(HealthProblemStatusLogEntity::getProblemId, problemId)
            .orderByDesc(HealthProblemStatusLogEntity::getActionTime)
            .orderByDesc(HealthProblemStatusLogEntity::getLogId)
            .page(new Page<>(1, PROBLEM_STATUS_LOG_LIMIT))
            .getRecords();
        Map<Long, HealthAppUserEntity> operatorMap = loadAppUserMap(logs.stream()
            .map(HealthProblemStatusLogEntity::getOperatorUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
        return logs.stream()
            .map(log -> buildProblemStatusLogDTO(log, memberMap, operatorMap))
            .collect(Collectors.toList());
    }

    private Map<Long, HealthAppUserEntity> loadAppUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 状态历史列表最多读取最近 50 条，操作者昵称一次性批量加载，避免每条日志单独查 app_user。
        return healthAppUserService.listByIds(userIds).stream()
            .collect(Collectors.toMap(HealthAppUserEntity::getUserId, Function.identity(), (left, right) -> left,
                LinkedHashMap::new));
    }
    private void saveProblemStatusLog(HealthProblemEntity problem, Integer beforeStatus, Integer afterStatus,
        String remark, Long currentUserId, Date actionTime) {
        HealthProblemStatusLogEntity log = new HealthProblemStatusLogEntity();
        log.setProblemId(problem.getProblemId());
        log.setOwnerUserId(problem.getOwnerUserId());
        log.setMemberId(problem.getMemberId());
        log.setOperatorUserId(currentUserId);
        log.setActionType(PROBLEM_STATUS_ACTION_CHANGE);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setActionRemark(StrUtil.trim(remark));
        log.setActionTime(actionTime);
        log.setDeleted(0);
        healthProblemStatusLogService.save(log);
    }

    private HealthProblemStatusLogDTO buildProblemStatusLogDTO(HealthProblemStatusLogEntity log,
        Map<Long, HealthFamilyMemberEntity> memberMap, Map<Long, HealthAppUserEntity> operatorMap) {
        HealthProblemStatusLogDTO dto = new HealthProblemStatusLogDTO();
        dto.setLogId(log.getLogId());
        dto.setProblemId(log.getProblemId());
        dto.setMemberId(log.getMemberId());
        dto.setMemberName(memberMap.get(log.getMemberId()) == null ? null : memberMap.get(log.getMemberId()).getMemberName());
        dto.setOperatorUserId(log.getOperatorUserId());
        HealthAppUserEntity operator = operatorMap.get(log.getOperatorUserId());
        dto.setOperatorNickname(operator == null ? null : operator.getNickname());
        dto.setActionType(log.getActionType());
        dto.setActionTypeName(resolveProblemStatusActionName(log.getActionType()));
        dto.setBeforeStatus(log.getBeforeStatus());
        dto.setBeforeStatusName(resolveProblemStatusName(log.getBeforeStatus()));
        dto.setAfterStatus(log.getAfterStatus());
        dto.setAfterStatusName(resolveProblemStatusName(log.getAfterStatus()));
        dto.setActionRemark(log.getActionRemark());
        dto.setActionTime(log.getActionTime());
        return dto;
    }
    /**
     * 查询某个健康问题可关联的慢病专项候选列表。
     *
     * <p>候选范围限定在问题所属家庭成员下，避免把 A 成员的问题误关联到 B 成员的慢病专项。
     */
    public List<HealthProblemChronicProfileDTO> listHealthProblemChronicProfileCandidates(Long problemId,
        Long currentUserId) {
        HealthProblemEntity problem = loadRequiredProblemWithAccess(problemId, currentUserId, false);
        List<HealthChronicDiseaseProfileEntity> profiles = chronicDiseaseProfileService.lambdaQuery()
            .eq(HealthChronicDiseaseProfileEntity::getMemberId, problem.getMemberId())
            .orderByAsc(HealthChronicDiseaseProfileEntity::getProfileStatus)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getLastReviewDate)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getProfileId)
            .list();
        if (profiles.isEmpty()) {
            return Collections.emptyList();
        }
        List<HealthProblemChronicProfileLinkEntity> links = healthProblemChronicProfileLinkService.lambdaQuery()
            .eq(HealthProblemChronicProfileLinkEntity::getProblemId, problemId)
            .list();
        Map<Long, HealthProblemChronicProfileLinkEntity> linkMap = links.stream()
            .collect(Collectors.toMap(HealthProblemChronicProfileLinkEntity::getProfileId, Function.identity(),
                (left, right) -> left, LinkedHashMap::new));
        Set<Long> linkedProfileIds = linkMap.keySet();
        Map<Long, HealthChronicDiseaseProfileEntity> profileMap = profiles.stream()
            .collect(Collectors.toMap(HealthChronicDiseaseProfileEntity::getProfileId, Function.identity(),
                (left, right) -> left, LinkedHashMap::new));
        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(Collections.singleton(problem.getMemberId()));
        List<HealthProblemChronicProfileDTO> result = buildRelatedChronicProfileDTOs(links, profileMap,
            linkedProfileIds, memberMap);
        profiles.stream()
            .filter(profile -> !linkedProfileIds.contains(profile.getProfileId()))
            .map(profile -> buildChronicProfileDTO(null, profile, false, memberMap))
            .forEach(result::add);
        return result;
    }

    /**
     * 将健康问题关联到慢病专项。
     *
     * <p>关联属于业务确认动作，必须要求成员编辑权限；重复关联时只更新备注和时间，保证用户关系不会被重复创建。
     */
    @Transactional(rollbackFor = Exception.class)
    public HealthProblemChronicProfileDTO linkHealthProblemToChronicProfile(Long problemId,
        LinkHealthProblemChronicProfileCommand command, Long currentUserId) {
        HealthProblemEntity problem = loadRequiredProblemWithAccess(problemId, currentUserId, true);
        HealthChronicDiseaseProfileEntity profile = loadRequiredChronicProfileForProblem(problem, command.getProfileId(),
            currentUserId);
        Date now = new Date();
        HealthProblemChronicProfileLinkEntity link = healthProblemChronicProfileLinkService.lambdaQuery()
            .eq(HealthProblemChronicProfileLinkEntity::getProblemId, problemId)
            .eq(HealthProblemChronicProfileLinkEntity::getProfileId, profile.getProfileId())
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
        if (link == null) {
            link = new HealthProblemChronicProfileLinkEntity();
            link.setOwnerUserId(problem.getOwnerUserId());
            link.setMemberId(problem.getMemberId());
            link.setProblemId(problemId);
            link.setProfileId(profile.getProfileId());
            link.setLinkSource("MANUAL");
            link.setCreatorId(currentUserId);
            link.setCreateTime(now);
            link.setDeleted(0);
        }
        link.setLinkRemark(StrUtil.trim(command.getLinkRemark()));
        link.setLinkTime(now);
        link.setUpdaterId(currentUserId);
        link.setUpdateTime(now);
        if (link.getLinkId() == null) {
            healthProblemChronicProfileLinkService.save(link);
        } else {
            healthProblemChronicProfileLinkService.updateById(link);
        }
        return buildChronicProfileDTO(link, profile, true, loadMemberMap(Collections.singleton(profile.getMemberId())));
    }

    /**
     * 解除健康问题和慢病专项的关联。
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlinkHealthProblemFromChronicProfile(Long problemId, Long profileId, Long currentUserId) {
        HealthProblemEntity problem = loadRequiredProblemWithAccess(problemId, currentUserId, true);
        loadRequiredChronicProfileForProblem(problem, profileId, currentUserId);
        HealthProblemChronicProfileLinkEntity link = healthProblemChronicProfileLinkService.lambdaQuery()
            .eq(HealthProblemChronicProfileLinkEntity::getProblemId, problemId)
            .eq(HealthProblemChronicProfileLinkEntity::getProfileId, profileId)
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
        if (link != null) {
            healthProblemChronicProfileLinkService.removeById(link.getLinkId());
        }
    }
    /**
     * 从慢病模板创建专项档案。
     *
     * <p>实际建档仍然复用慢病专项应用服务，健康洞察层只提供“从模板入口创建”的轻包装，
     * 确保重复校验、病种配置校验和默认目标填充仍然只有一套实现。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiseaseProfileDTO createProfileFromTemplate(SaveChronicDiseaseProfileCommand command,
        Long currentUserId) {
        return chronicDiseaseApplicationService.createProfile(command, currentUserId);
    }
    /**
     * 查询慢病专项模板。
     *
     * <p>模板从慢病病种配置表生成，新增病种只需要插入配置，不需要 App 或后端新增分支。
     */
    public List<ChronicDiseaseTemplateDTO> listChronicDiseaseTemplates() {
        return chronicDiseaseTypeService.lambdaQuery()
            .eq(HealthChronicDiseaseTypeEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByAsc(HealthChronicDiseaseTypeEntity::getSort)
            .orderByAsc(HealthChronicDiseaseTypeEntity::getTypeId)
            .list()
            .stream()
            .map(this::buildChronicDiseaseTemplate)
            .collect(Collectors.toList());
    }
    private Set<Long> resolveAccessibleMemberIds(Long memberId, Long currentUserId) {
        if (memberId == null) {
            return familyMemberAccessService.getAccessibleMemberIds(currentUserId);
        }
        familyMemberAccessService.getRequiredAccessContext(memberId, currentUserId);
        return Collections.singleton(memberId);
    }

    private Map<Long, HealthFamilyMemberEntity> loadMemberMap(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return familyMemberService.listByIds(memberIds).stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, Function.identity(), (left, right) -> left,
                LinkedHashMap::new));
    }

    private Map<Long, List<HealthProblemEvidenceEntity>> loadProblemEvidenceMap(List<HealthProblemEntity> problems) {
        Set<Long> problemIds = problems == null ? Collections.emptySet() : problems.stream()
            .map(HealthProblemEntity::getProblemId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (problemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return healthProblemEvidenceService.lambdaQuery()
            .in(HealthProblemEvidenceEntity::getProblemId, problemIds)
            .orderByDesc(HealthProblemEvidenceEntity::getEvidenceDate)
            .orderByDesc(HealthProblemEvidenceEntity::getEvidenceId)
            .list()
            .stream()
            .collect(Collectors.groupingBy(HealthProblemEvidenceEntity::getProblemId, LinkedHashMap::new,
                Collectors.toList()));
    }

    private Map<Long, List<HealthProblemChronicProfileLinkEntity>> loadProblemChronicProfileLinkMap(
        List<HealthProblemEntity> problems) {
        Set<Long> problemIds = problems == null ? Collections.emptySet() : problems.stream()
            .map(HealthProblemEntity::getProblemId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (problemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return healthProblemChronicProfileLinkService.lambdaQuery()
            .in(HealthProblemChronicProfileLinkEntity::getProblemId, problemIds)
            .orderByDesc(HealthProblemChronicProfileLinkEntity::getLinkTime)
            .orderByDesc(HealthProblemChronicProfileLinkEntity::getLinkId)
            .list()
            .stream()
            .collect(Collectors.groupingBy(HealthProblemChronicProfileLinkEntity::getProblemId, LinkedHashMap::new,
                Collectors.toList()));
    }

    private Map<Long, HealthChronicDiseaseProfileEntity> loadLinkedChronicProfileMap(
        Map<Long, List<HealthProblemChronicProfileLinkEntity>> linkMap) {
        Set<Long> profileIds = linkMap == null ? Collections.emptySet() : linkMap.values().stream()
            .flatMap(List::stream)
            .map(HealthProblemChronicProfileLinkEntity::getProfileId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (profileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return chronicDiseaseProfileService.listByIds(profileIds).stream()
            .collect(Collectors.toMap(HealthChronicDiseaseProfileEntity::getProfileId, Function.identity(),
                (left, right) -> left, LinkedHashMap::new));
    }

    private List<HealthProblemChronicProfileDTO> buildRelatedChronicProfileDTOs(
        List<HealthProblemChronicProfileLinkEntity> links, Map<Long, HealthChronicDiseaseProfileEntity> profileMap,
        Set<Long> linkedProfileIds, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (links == null || links.isEmpty()) {
            return new ArrayList<>();
        }
        return links.stream()
            .map(link -> buildChronicProfileDTO(link, profileMap.get(link.getProfileId()),
                linkedProfileIds == null || linkedProfileIds.isEmpty() || linkedProfileIds.contains(link.getProfileId()),
                memberMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private HealthProblemChronicProfileDTO buildChronicProfileDTO(HealthProblemChronicProfileLinkEntity link,
        HealthChronicDiseaseProfileEntity profile, boolean linked, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (profile == null) {
            return null;
        }
        HealthProblemChronicProfileDTO dto = new HealthProblemChronicProfileDTO();
        dto.setLinkId(link == null ? null : link.getLinkId());
        dto.setProfileId(profile.getProfileId());
        dto.setMemberId(profile.getMemberId());
        HealthFamilyMemberEntity member = memberMap == null ? null : memberMap.get(profile.getMemberId());
        dto.setMemberName(member == null ? null : member.getMemberName());
        dto.setDiseaseCode(profile.getDiseaseCode());
        dto.setDiseaseName(profile.getDiseaseNameSnapshot());
        dto.setProfileStatus(profile.getProfileStatus());
        dto.setProfileStatusName(resolveChronicProfileStatusName(profile.getProfileStatus()));
        dto.setRiskLevel(profile.getRiskLevel());
        dto.setRiskLevelName(resolveChronicRiskLevelName(profile.getRiskLevel()));
        dto.setLinked(linked);
        dto.setLinkRemark(link == null ? null : link.getLinkRemark());
        dto.setLinkTime(link == null ? null : link.getLinkTime());
        return dto;
    }
    private HealthProblemCenterDTO.ProblemItemDTO buildProblemItem(HealthProblemEntity problem,
        Map<Long, HealthFamilyMemberEntity> memberMap, List<HealthProblemEvidenceEntity> evidenceList,
        List<HealthProblemChronicProfileLinkEntity> chronicProfileLinks,
        Map<Long, HealthChronicDiseaseProfileEntity> chronicProfileMap) {
        HealthProblemCenterDTO.ProblemItemDTO dto = new HealthProblemCenterDTO.ProblemItemDTO();
        dto.setProblemId(problem.getProblemId());
        dto.setMemberId(problem.getMemberId());
        HealthFamilyMemberEntity member = memberMap.get(problem.getMemberId());
        dto.setMemberName(member == null ? null : member.getMemberName());
        dto.setProblemName(problem.getProblemName());
        dto.setProblemType(problem.getProblemType());
        dto.setProblemStatus(problem.getProblemStatus());
        dto.setProblemStatusName(resolveProblemStatusName(problem.getProblemStatus()));
        dto.setRiskLevel(problem.getRiskLevel());
        dto.setRiskLevelName(resolveProblemRiskLevelName(problem.getRiskLevel()));
        dto.setStandardItemCode(problem.getStandardItemCode());
        dto.setFirstFoundDate(problem.getFirstFoundDate());
        dto.setLastFollowDate(problem.getLastFollowDate());
        dto.setSummary(problem.getSummary());
        List<HealthProblemEvidenceEntity> safeEvidence = evidenceList == null ? Collections.emptyList() : evidenceList;
        dto.setEvidenceCount(safeEvidence.size());
        dto.setRelatedChronicProfiles(buildRelatedChronicProfileDTOs(chronicProfileLinks, chronicProfileMap,
            Collections.emptySet(), memberMap));
        dto.setRecentEvidence(safeEvidence.stream()
            .limit(3)
            .map(this::buildProblemEvidenceDTO)
            .collect(Collectors.toList()));
        return dto;
    }

    private HealthProblemCenterDTO.ProblemEvidenceDTO buildProblemEvidenceDTO(HealthProblemEvidenceEntity entity) {
        HealthProblemCenterDTO.ProblemEvidenceDTO dto = new HealthProblemCenterDTO.ProblemEvidenceDTO();
        dto.setEvidenceId(entity.getEvidenceId());
        dto.setEvidenceType(entity.getEvidenceType());
        dto.setReportId(entity.getReportId());
        dto.setReportItemId(entity.getReportItemId());
        dto.setEvidenceTitle(entity.getEvidenceTitle());
        dto.setEvidenceSummary(entity.getEvidenceSummary());
        dto.setEvidenceDate(entity.getEvidenceDate());
        dto.setConfidenceLevel(entity.getConfidenceLevel());
        dto.setConfirmStatus(entity.getConfirmStatus());
        return dto;
    }

    private HealthProblemEntity loadRequiredProblemWithAccess(Long problemId, Long currentUserId, boolean editRequired) {
        HealthProblemEntity problem = healthProblemService.getById(problemId);
        if (problem == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, problemId, "健康问题");
        }
        if (editRequired) {
            familyMemberAccessService.checkCanEditMember(problem.getMemberId(), currentUserId);
        } else {
            familyMemberAccessService.getRequiredAccessContext(problem.getMemberId(), currentUserId);
        }
        return problem;
    }

    private HealthChronicDiseaseProfileEntity loadRequiredChronicProfileForProblem(HealthProblemEntity problem,
        Long profileId, Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = chronicDiseaseProfileService.getById(profileId);
        if (profile == null || !Objects.equals(profile.getMemberId(), problem.getMemberId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, profileId, "慢病专项");
        }
        familyMemberAccessService.checkCanEditMember(profile.getMemberId(), currentUserId);
        return profile;
    }
    private String buildProblemCenterSummary(HealthProblemCenterDTO dto) {
        if (dto.getTotalCount() <= 0) {
            return "当前暂无健康问题，后续报告解析出的异常会自动沉淀到这里。";
        }
        return String.format(Locale.ROOT, "当前共有%d个健康问题，其中%d个跟进中、%d个高风险。",
            dto.getTotalCount(), dto.getFollowingCount(), dto.getHighRiskCount());
    }

    private String resolveChronicProfileStatusName(Integer status) {
        if (status == null || Objects.equals(status, 1)) {
            return "跟进中";
        }
        if (Objects.equals(status, 2)) {
            return "已稳定";
        }
        if (Objects.equals(status, 3)) {
            return "已关闭";
        }
        return "未知状态";
    }

    private String resolveChronicRiskLevelName(String riskLevel) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return "高优先关注";
        }
        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return "中优先关注";
        }
        return "低优先关注";
    }
    private String resolveProblemStatusName(Integer status) {
        if (Objects.equals(status, 1)) {
            return "跟进中";
        }
        if (Objects.equals(status, 2)) {
            return "已缓解";
        }
        if (Objects.equals(status, 3)) {
            return "已关闭";
        }
        return "未知";
    }

    private String resolveProblemRiskLevelName(Integer riskLevel) {
        if (Objects.equals(riskLevel, 3)) {
            return "高风险";
        }
        if (Objects.equals(riskLevel, 2)) {
            return "中风险";
        }
        if (Objects.equals(riskLevel, 1)) {
            return "低风险";
        }
        return "未分级";
    }
    private MedicalVisitPackageExportDTO buildVisitPackageDTO(HealthFamilyMemberEntity member,
        List<HealthChronicDiseaseProfileEntity> profiles, List<HealthReportEntity> reports,
        List<HealthReportItemEntity> reportItems, List<HealthMedicationPlanEntity> plans,
        List<HealthProblemEntity> problems) {
        MedicalVisitPackageExportDTO dto = new MedicalVisitPackageExportDTO();
        dto.setGeneratedTime(new Date());
        dto.setPackageTitle(member == null ? "就医资料包" : member.getMemberName() + "的就医资料包");
        dto.setSummary(buildVisitPackageSummary(member, profiles, reports, plans, problems));
        dto.setMember(buildVisitPackageMember(member));
        dto.setChronicProfiles(profiles.stream().map(this::buildVisitPackageProfile).collect(Collectors.toList()));
        dto.setRecentReports(buildVisitPackageReports(reports, reportItems));
        dto.setMedicationPlans(buildVisitPackageMedicationPlans(plans));
        dto.setHealthProblems(problems.stream().map(this::buildVisitPackageProblem).collect(Collectors.toList()));
        dto.setChecklist(buildVisitPackageChecklist(dto));
        return dto;
    }

    private String buildVisitPackageSummary(HealthFamilyMemberEntity member, List<HealthChronicDiseaseProfileEntity> profiles,
        List<HealthReportEntity> reports, List<HealthMedicationPlanEntity> plans, List<HealthProblemEntity> problems) {
        if (member == null) {
            return "未找到成员资料，暂不能生成完整资料包。";
        }
        return String.format(Locale.ROOT, "%s的资料包已整理：慢病专项%d项、近期报告%d份、当前用药%d项、健康问题%d个。",
            member.getMemberName(), profiles.size(), reports.size(), plans.size(), problems.size());
    }

    private MedicalVisitPackageExportDTO.MemberSectionDTO buildVisitPackageMember(HealthFamilyMemberEntity member) {
        MedicalVisitPackageExportDTO.MemberSectionDTO dto = new MedicalVisitPackageExportDTO.MemberSectionDTO();
        if (member == null) {
            return dto;
        }
        dto.setMemberId(member.getMemberId());
        dto.setMemberCode(member.getMemberCode());
        dto.setMemberName(member.getMemberName());
        dto.setGender(member.getGender());
        dto.setBirthday(member.getBirthday());
        dto.setRelationType(member.getRelationType());
        dto.setHeight(member.getHeight());
        dto.setWeight(member.getWeight());
        dto.setBloodType(member.getBloodType());
        dto.setAllergyHistory(member.getAllergyHistory());
        dto.setChronicHistory(member.getChronicHistory());
        dto.setRemark(member.getRemark());
        return dto;
    }

    private MedicalVisitPackageExportDTO.ChronicProfileSectionDTO buildVisitPackageProfile(
        HealthChronicDiseaseProfileEntity profile) {
        MedicalVisitPackageExportDTO.ChronicProfileSectionDTO dto = new MedicalVisitPackageExportDTO.ChronicProfileSectionDTO();
        dto.setProfileId(profile.getProfileId());
        dto.setDiseaseCode(profile.getDiseaseCode());
        dto.setDiseaseName(profile.getDiseaseNameSnapshot());
        dto.setProfileStatus(profile.getProfileStatus());
        dto.setRiskLevel(profile.getRiskLevel());
        dto.setDiagnosedDate(profile.getDiagnosedDate());
        dto.setTargetSummary(profile.getTargetSummary());
        dto.setCurrentSummary(profile.getCurrentSummary());
        dto.setLastReviewDate(profile.getLastReviewDate());
        return dto;
    }

    private List<MedicalVisitPackageExportDTO.ReportSectionDTO> buildVisitPackageReports(List<HealthReportEntity> reports,
        List<HealthReportItemEntity> reportItems) {
        Map<Long, List<HealthReportItemEntity>> itemMap = reportItems.stream()
            .filter(item -> item.getAbnormalFlag() != null && item.getAbnormalFlag() != 0)
            .collect(Collectors.groupingBy(HealthReportItemEntity::getReportId, LinkedHashMap::new, Collectors.toList()));
        return reports.stream().map(report -> {
            MedicalVisitPackageExportDTO.ReportSectionDTO dto = new MedicalVisitPackageExportDTO.ReportSectionDTO();
            dto.setReportId(report.getReportId());
            dto.setReportName(report.getReportName());
            dto.setReportType(report.getReportType());
            dto.setHospitalName(report.getHospitalName());
            dto.setReportDate(report.getReportDate());
            dto.setAnalysisSummary(report.getAnalysisSummary());
            dto.setAbnormalItems(itemMap.getOrDefault(report.getReportId(), Collections.emptyList()).stream()
                .limit(8)
                .map(this::buildVisitPackageReportItem)
                .collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    private MedicalVisitPackageExportDTO.ReportItemSectionDTO buildVisitPackageReportItem(HealthReportItemEntity item) {
        MedicalVisitPackageExportDTO.ReportItemSectionDTO dto = new MedicalVisitPackageExportDTO.ReportItemSectionDTO();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setResultValue(item.getResultValue());
        dto.setResultUnit(item.getResultUnit());
        dto.setReferenceText(item.getReferenceText());
        dto.setAbnormalFlag(item.getAbnormalFlag());
        dto.setItemInterpretation(item.getItemInterpretation());
        return dto;
    }

    private List<MedicalVisitPackageExportDTO.MedicationPlanSectionDTO> buildVisitPackageMedicationPlans(
        List<HealthMedicationPlanEntity> plans) {
        Set<Long> drugIds = plans.stream()
            .map(HealthMedicationPlanEntity::getDrugId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, DrugEntity> drugMap = drugIds.isEmpty()
            ? Collections.emptyMap()
            : drugService.listByIds(drugIds).stream()
                .collect(Collectors.toMap(DrugEntity::getDrugId, Function.identity(), (left, right) -> left));
        return plans.stream().map(plan -> {
            MedicalVisitPackageExportDTO.MedicationPlanSectionDTO dto = new MedicalVisitPackageExportDTO.MedicationPlanSectionDTO();
            DrugEntity drug = drugMap.get(plan.getDrugId());
            dto.setPlanId(plan.getPlanId());
            dto.setPlanCode(plan.getPlanCode());
            dto.setDrugId(plan.getDrugId());
            dto.setDrugName(drug == null ? plan.getCustomDrugName() : drug.getDrugName());
            dto.setStartDate(plan.getStartDate());
            dto.setEndDate(plan.getEndDate());
            dto.setDoseAmount(plan.getDoseAmount());
            dto.setDoseUnit(plan.getDoseUnit());
            dto.setFrequencyType(plan.getFrequencyType());
            dto.setMealTiming(plan.getMealTiming());
            dto.setRemark(plan.getRemark());
            return dto;
        }).collect(Collectors.toList());
    }

    private MedicalVisitPackageExportDTO.ProblemSectionDTO buildVisitPackageProblem(HealthProblemEntity problem) {
        MedicalVisitPackageExportDTO.ProblemSectionDTO dto = new MedicalVisitPackageExportDTO.ProblemSectionDTO();
        dto.setProblemId(problem.getProblemId());
        dto.setProblemName(problem.getProblemName());
        dto.setProblemType(problem.getProblemType());
        dto.setProblemStatus(problem.getProblemStatus());
        dto.setProblemStatusName(resolveProblemStatusName(problem.getProblemStatus()));
        dto.setRiskLevel(problem.getRiskLevel());
        dto.setRiskLevelName(resolveProblemRiskLevelName(problem.getRiskLevel()));
        dto.setFirstFoundDate(problem.getFirstFoundDate());
        dto.setLastFollowDate(problem.getLastFollowDate());
        dto.setSummary(problem.getSummary());
        return dto;
    }

    private List<String> buildVisitPackageChecklist(MedicalVisitPackageExportDTO dto) {
        List<String> checklist = new ArrayList<>();
        checklist.add("携带身份证、医保卡和既往病历资料。 ");
        checklist.add("向医生说明当前正在服用的药品、剂量和服药频率。 ");
        if (!dto.getHealthProblems().isEmpty()) {
            checklist.add("优先咨询资料包中的高风险健康问题和近期异常指标。 ");
        }
        if (!dto.getChronicProfiles().isEmpty()) {
            checklist.add("复诊时确认慢病管理目标、复查周期和是否需要调整用药。 ");
        }
        return checklist;
    }

    private ChronicDiseaseTemplateDTO buildChronicDiseaseTemplate(HealthChronicDiseaseTypeEntity entity) {
        ChronicDiseaseTemplateDTO dto = new ChronicDiseaseTemplateDTO();
        dto.setTemplateCode("CHRONIC_TEMPLATE_" + entity.getDiseaseCode());
        dto.setTemplateName(entity.getDiseaseName() + "管理模板");
        dto.setTemplateCategory(entity.getDiseaseCategory());
        dto.setDiseaseCode(entity.getDiseaseCode());
        dto.setDiseaseName(entity.getDiseaseName());
        dto.setTargetSummary(entity.getTargetSummary());
        dto.setFollowUpSuggestion(entity.getFollowUpSuggestion());
        dto.setFocusIndicatorCodes(parseStringArray(entity.getFocusIndicatorCodesJson()));
        dto.setFocusIndicatorKeywords(parseStringArray(entity.getFocusIndicatorKeywordsJson()));
        dto.setDefaultDiaryTypes(resolveDefaultDiaryTypes(entity));
        dto.setCreateProfileTips(buildTemplateTips(entity, dto.getDefaultDiaryTypes()));
        return dto;
    }

    private List<String> parseStringArray(String jsonText) {
        if (StrUtil.isBlank(jsonText)) {
            return Collections.emptyList();
        }
        try {
            JSONArray jsonArray = JSONUtil.parseArray(jsonText);
            List<String> values = new ArrayList<>();
            for (Object value : jsonArray) {
                String text = value == null ? null : value.toString().trim();
                if (StrUtil.isNotBlank(text)) {
                    values.add(text);
                }
            }
            return values;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private List<String> resolveDefaultDiaryTypes(HealthChronicDiseaseTypeEntity entity) {
        String sourceText = (StrUtil.nullToEmpty(entity.getDiseaseName()) + " " + StrUtil.nullToEmpty(entity.getDiseaseCode())
            + " " + StrUtil.nullToEmpty(entity.getFocusIndicatorKeywordsJson())).toLowerCase(Locale.ROOT);
        LinkedHashSet<String> types = new LinkedHashSet<>();
        types.add("SYMPTOM");
        types.add("MEDICATION");
        if (sourceText.contains("血压") || sourceText.contains("hypertension")) {
            types.add("BLOOD_PRESSURE");
        }
        if (sourceText.contains("糖") || sourceText.contains("glucose") || sourceText.contains("diabetes")) {
            types.add("BLOOD_GLUCOSE");
            types.add("DIET");
        }
        if (sourceText.contains("尿酸") || sourceText.contains("痛风") || sourceText.contains("gout")) {
            types.add("DIET");
        }
        types.add("EXERCISE");
        types.add("REVIEW_NOTE");
        return new ArrayList<>(types);
    }

    private List<String> buildTemplateTips(HealthChronicDiseaseTypeEntity entity, List<String> diaryTypes) {
        List<String> tips = new ArrayList<>();
        tips.add("为家庭成员建立" + entity.getDiseaseName() + "专项后，可持续关联报告趋势、复查任务和慢病日记。");
        if (StrUtil.isNotBlank(entity.getFollowUpSuggestion())) {
            tips.add(entity.getFollowUpSuggestion());
        }
        tips.add("建议日记类型：" + String.join("、", diaryTypes));
        return tips;
    }
    private HealthOperationTaskEntity loadExistingSuggestionTask(Long ownerUserId, Long memberId, String targetBizType,
        Long targetBizId) {
        com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<HealthOperationTaskEntity> query =
            operationTaskService.lambdaQuery()
                .eq(HealthOperationTaskEntity::getOwnerUserId, ownerUserId)
                .eq(HealthOperationTaskEntity::getMemberId, memberId)
                .eq(HealthOperationTaskEntity::getTargetBizType, targetBizType)
                .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue());
        if (targetBizId == null) {
            query.isNull(HealthOperationTaskEntity::getTargetBizId);
        } else {
            query.eq(HealthOperationTaskEntity::getTargetBizId, targetBizId);
        }
        return query.page(new Page<>(1, 1)).getRecords().stream().findFirst().orElse(null);
    }

    private String resolveReviewSuggestionTaskType(String targetType) {
        if ("HEALTH_PROBLEM".equalsIgnoreCase(targetType)) {
            return HEALTH_PROBLEM_REVIEW_TASK_TYPE;
        }
        if ("CHRONIC_REVIEW".equalsIgnoreCase(targetType)) {
            return ChronicDiseaseApplicationService.TARGET_BIZ_TYPE_CHRONIC_REVIEW;
        }
        if ("INDICATOR_ALERT".equalsIgnoreCase(targetType)) {
            return TARGET_BIZ_TYPE_INDICATOR_REVIEW;
        }
        return TARGET_BIZ_TYPE_LONG_TERM_REVIEW;
    }

    private void closeCareActionTask(Long operationTaskId, CareActionTaskActionCommand command, Long currentUserId,
        String actionName) {
        HealthOperationTaskEntity task = operationTaskService.getById(operationTaskId);
        if (task == null || !Objects.equals(task.getTargetBizType(), TARGET_BIZ_TYPE_CARE_ACTION)) {
            throw new ApiException(ErrorCode.Business.HEALTH_OPERATION_TASK_NOT_FOUND);
        }
        familyMemberAccessService.checkCanEditMember(task.getMemberId(), currentUserId);
        Date now = new Date();
        task.setStatus(StatusEnum.DISABLE.getValue());
        task.setEndTime(now);
        task.setRemark(limitLength(buildCareTaskCloseRemark(task.getRemark(), command, actionName), 500));
        task.setUpdaterId(currentUserId);
        task.setUpdateTime(now);
        operationTaskService.updateById(task);
    }

    private String buildCareTaskCloseRemark(String oldRemark, CareActionTaskActionCommand command, String actionName) {
        List<String> parts = new ArrayList<>();
        if (StrUtil.isNotBlank(oldRemark)) {
            parts.add(StrUtil.trim(oldRemark));
        }
        String remark = command == null ? null : StrUtil.trim(command.getRemark());
        parts.add(StrUtil.isBlank(remark) ? actionName : actionName + "：" + remark);
        return String.join("；", parts);
    }

    private List<Map<String, Object>> buildMemberExportItems(List<HealthFamilyMemberEntity> members) {
        return members.stream().map(member -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("memberId", member.getMemberId());
            item.put("memberName", member.getMemberName());
            item.put("relationType", member.getRelationType());
            item.put("gender", member.getGender());
            item.put("birthday", member.getBirthday());
            item.put("height", member.getHeight());
            item.put("weight", member.getWeight());
            item.put("bloodType", member.getBloodType());
            item.put("allergyHistory", member.getAllergyHistory());
            item.put("chronicHistory", member.getChronicHistory());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildProfileExportItems(List<HealthChronicDiseaseProfileEntity> profiles) {
        return profiles.stream().map(profile -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("profileId", profile.getProfileId());
            item.put("memberId", profile.getMemberId());
            item.put("diseaseCode", profile.getDiseaseCode());
            item.put("diseaseName", profile.getDiseaseNameSnapshot());
            item.put("riskLevel", profile.getRiskLevel());
            item.put("targetSummary", profile.getTargetSummary());
            item.put("currentSummary", profile.getCurrentSummary());
            item.put("lastReviewDate", profile.getLastReviewDate());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildReportExportItems(List<HealthReportEntity> reports,
        List<HealthReportItemEntity> reportItems) {
        Map<Long, List<HealthReportItemEntity>> itemMap = reportItems.stream()
            .collect(Collectors.groupingBy(HealthReportItemEntity::getReportId, LinkedHashMap::new, Collectors.toList()));
        return reports.stream().map(report -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("reportId", report.getReportId());
            item.put("memberId", report.getMemberId());
            item.put("reportName", report.getReportName());
            item.put("hospitalName", report.getHospitalName());
            item.put("reportDate", report.getReportDate());
            item.put("analysisSummary", report.getAnalysisSummary());
            item.put("abnormalItemCount", itemMap.getOrDefault(report.getReportId(), Collections.emptyList()).stream()
                .filter(this::isAbnormalItem).count());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMedicationPlanExportItems(List<HealthMedicationPlanEntity> activePlans) {
        return activePlans.stream().map(plan -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("planId", plan.getPlanId());
            item.put("memberId", plan.getMemberId());
            item.put("drugId", plan.getDrugId());
            item.put("drugName", plan.getCustomDrugName());
            item.put("doseAmount", plan.getDoseAmount());
            item.put("doseUnit", plan.getDoseUnit());
            item.put("frequencyType", plan.getFrequencyType());
            item.put("mealTiming", plan.getMealTiming());
            item.put("startDate", plan.getStartDate());
            item.put("endDate", plan.getEndDate());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildDiaryExportItems(List<HealthChronicDiaryEntryEntity> diaryEntries) {
        return diaryEntries.stream().map(entry -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("diaryEntryId", entry.getDiaryEntryId());
            item.put("memberId", entry.getMemberId());
            item.put("entryType", entry.getEntryType());
            item.put("recordTime", entry.getRecordTime());
            item.put("entryTitle", entry.getEntryTitle());
            item.put("entryContent", entry.getEntryContent());
            item.put("sourceType", entry.getSourceType());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildProblemExportItems(List<HealthProblemEntity> healthProblems) {
        return healthProblems.stream().map(problem -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("problemId", problem.getProblemId());
            item.put("memberId", problem.getMemberId());
            item.put("problemName", problem.getProblemName());
            item.put("problemStatusName", resolveProblemStatusName(problem.getProblemStatus()));
            item.put("riskLevelName", resolveProblemRiskLevelName(problem.getRiskLevel()));
            item.put("firstFoundDate", problem.getFirstFoundDate());
            item.put("lastFollowDate", problem.getLastFollowDate());
            item.put("summary", problem.getSummary());
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildCareTaskExportItems(List<HealthOperationTaskEntity> careActionTasks) {
        return careActionTasks.stream().map(task -> {
            Map<String, Object> item = HealthPrivacyDataExportDTO.item();
            item.put("operationTaskId", task.getOperationTaskId());
            item.put("memberId", task.getMemberId());
            item.put("taskTitle", task.getTaskTitle());
            item.put("taskContent", task.getTaskContent());
            item.put("riskLevel", task.getRiskLevel());
            item.put("startTime", task.getStartTime());
            item.put("endTime", task.getEndTime());
            return item;
        }).collect(Collectors.toList());
    }
    private HealthLongTermManagementDTO buildLongTermManagement(List<HealthFamilyMemberEntity> members,
        List<HealthChronicDiseaseProfileEntity> profiles, List<HealthOperationTaskEntity> reviewTasks,
        List<HealthReportEntity> reports, List<HealthReportItemEntity> reportItems,
        List<HealthMedicationPlanEntity> activePlans, List<HealthMedicationReminderEntity> pendingReminders,
        List<HealthChronicDiaryEntryEntity> dailyIndicatorEntries, List<HealthProblemEntity> healthProblems,
        List<HealthOperationTaskEntity> careActionTasks,
        List<HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO> indicatorAlerts, HealthInsightWorkbenchDTO workbench,
        Long currentUserId) {
        Set<Long> memberIds = members.stream()
            .map(HealthFamilyMemberEntity::getMemberId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HealthFamilyMemberEntity> memberMap = members.stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, Function.identity(), (left, right) -> left,
                LinkedHashMap::new));

        HealthLongTermManagementDTO dto = new HealthLongTermManagementDTO();
        dto.setDailyIndicatorBoard(buildDailyIndicatorBoard(memberIds, dailyIndicatorEntries, memberMap));
        dto.setIndicatorTargetTrends(buildIndicatorTargetTrends(profiles, reports, reportItems, memberMap));
        dto.setReviewSuggestions(buildReviewSuggestions(reviewTasks, healthProblems, indicatorAlerts, memberMap));
        dto.setVisitPackageShare(buildVisitPackageShare(workbench.getMedicalVisitPackage(), members, profiles, reports,
            activePlans, healthProblems));
        dto.setCareCollaboration(buildCareCollaboration(members, pendingReminders, careActionTasks, memberMap,
            workbench.getCareDashboard()));
        dto.setCareAssignmentSummary(buildCareAssignmentSummary(careActionTasks, memberMap));
        dto.setMedicationSafetyRules(buildMedicationSafetyRules(activePlans, memberMap));
        dto.setNextActionHints(buildNextActionHints(dto));
        dto.setMemberFocusSummaries(buildMemberFocusSummaries(members, dto));
        dto.setPrivacyExport(buildPrivacyExportSummary(memberIds, members, profiles, reports, reportItems, activePlans,
            healthProblems, careActionTasks));
        dto.setCapabilities(buildLongTermCapabilityCards(dto, profiles, reviewTasks, reports, activePlans,
            healthProblems, indicatorAlerts, workbench));
        dto.setSummary(StrUtil.format("已汇总{}位家庭成员、{}个慢病专项、{}份近期报告、{}个健康问题，长期管理能力可逐项使用。",
            members.size(), profiles.size(), reports.size(), healthProblems.size()));
        return dto;
    }

    private List<HealthLongTermManagementDTO.CapabilityCardDTO> buildLongTermCapabilityCards(
        HealthLongTermManagementDTO longTermManagement, List<HealthChronicDiseaseProfileEntity> profiles,
        List<HealthOperationTaskEntity> reviewTasks, List<HealthReportEntity> reports,
        List<HealthMedicationPlanEntity> activePlans, List<HealthProblemEntity> healthProblems,
        List<HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO> indicatorAlerts, HealthInsightWorkbenchDTO workbench) {
        List<HealthLongTermManagementDTO.CapabilityCardDTO> cards = new ArrayList<>();
        cards.add(buildCapabilityCard("FAMILY_PLAN_ENGINE", "家庭健康计划", workbench.getReviewLoop().getSummary(),
            "查看计划", profiles.size() + reviewTasks.size(), "MEDIUM", true));
        cards.add(buildCapabilityCard("DAILY_INDICATOR_RECORD", "日常指标记录",
            longTermManagement.getDailyIndicatorBoard().getSummary(), "查看趋势",
            longTermManagement.getDailyIndicatorBoard().getRecentRecordCount()
                + longTermManagement.getIndicatorTargetTrends().size(), "LOW", true));
        cards.add(buildCapabilityCard("CHRONIC_FOLLOW_UP_TEMPLATE", "慢病随访模板",
            "慢病模板来自病种配置，可覆盖所有已维护慢病。", "选择模板", countEnabledChronicDiseaseTypes(), "LOW", true));
        cards.add(buildCapabilityCard("REVIEW_TASK_RULE", "复查任务规则",
            "根据慢病复查、健康问题和异常指标生成复查建议。", "查看建议",
            longTermManagement.getReviewSuggestions().size(), "MEDIUM", true));
        cards.add(buildCapabilityCard("PROBLEM_LIFECYCLE", "健康问题生命周期", buildProblemLifecycleSummary(healthProblems),
            "查看问题", healthProblems.size(), healthProblems.stream().anyMatch(item -> Objects.equals(item.getRiskLevel(), 3))
                ? "HIGH" : "MEDIUM", true));
        cards.add(buildCapabilityCard("VISIT_PACKAGE_SHARE", "就医资料包分享",
            longTermManagement.getVisitPackageShare().getSummary(), "分享资料", reports.size() + activePlans.size(), "LOW",
            true));
        cards.add(buildCapabilityCard("MEDICATION_SAFETY", "用药安全增强", workbench.getMedicationSafety().getSummary(),
            "查看规则", workbench.getMedicationSafety().getLowStockDrugCount()
                + workbench.getMedicationSafety().getDuplicateMedicationCount()
                + longTermManagement.getMedicationSafetyRules().size(), "HIGH", true));
        cards.add(buildCapabilityCard("FAMILY_CARE_COLLABORATION", "家庭照护协作",
            longTermManagement.getCareCollaboration().getSummary(), "查看分派",
            longTermManagement.getCareCollaboration().getPendingCareTaskCount(), "MEDIUM", true));
        cards.add(buildCapabilityCard("PERIODIC_REPORT", "健康周报月报", workbench.getAiPeriodicReport().getSummary(),
            "查看报告", workbench.getPeriodicReports().size(), "LOW", true));
        cards.add(buildCapabilityCard("PRIVACY_EXPORT", "数据导出与隐私",
            longTermManagement.getPrivacyExport().getSummary(), "查看摘要",
            longTermManagement.getPrivacyExport().getCategories().size(), "LOW", true));
        return cards;
    }

    private HealthLongTermManagementDTO.CapabilityCardDTO buildCapabilityCard(String code, String title, String summary,
        String actionText, int dataCount, String riskLevel, boolean actionable) {
        HealthLongTermManagementDTO.CapabilityCardDTO dto = new HealthLongTermManagementDTO.CapabilityCardDTO();
        dto.setCapabilityCode(code);
        dto.setTitle(title);
        dto.setSummary(summary);
        dto.setActionText(actionText);
        dto.setDataCount(dataCount);
        dto.setRiskLevel(riskLevel);
        dto.setActionable(actionable);
        return dto;
    }

    private HealthLongTermManagementDTO.DailyIndicatorBoardDTO buildDailyIndicatorBoard(Set<Long> memberIds,
        List<HealthChronicDiaryEntryEntity> dailyIndicatorEntries, Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthLongTermManagementDTO.DailyIndicatorBoardDTO dto = new HealthLongTermManagementDTO.DailyIndicatorBoardDTO();
        dto.setSupportedTypes(buildSupportedDailyIndicatorTypes());
        dto.setTodayRecordCount(countTodayDailyIndicatorEntries(memberIds));
        dto.setRecentRecordCount(dailyIndicatorEntries.size());
        dailyIndicatorEntries.stream()
            .map(HealthChronicDiaryEntryEntity::getRecordTime)
            .filter(Objects::nonNull)
            .max(Date::compareTo)
            .ifPresent(dto::setLatestRecordTime);
        dto.setRecentRecords(dailyIndicatorEntries.stream()
            .map(entry -> new ChronicDiaryEntryDTO(entry, resolveMemberNameFromMap(memberMap, entry.getMemberId())))
            .collect(Collectors.toList()));
        dto.setTrendSummaries(buildDailyIndicatorTrendSummaries(dailyIndicatorEntries, memberMap));
        dto.setSummary(StrUtil.format("今天已记录{}条日常指标，最近保留{}条血压、血糖、体重等记录。",
            dto.getTodayRecordCount(), dto.getRecentRecordCount()));
        return dto;
    }

    /**
     * 基于最近的日常指标记录生成轻量趋势。
     *
     * <p>这里不重新查询全量历史，而是复用工作台已经加载的近期记录，按成员和指标类型分组，
     * 只比较最近两次可量化值。这样能在首屏提供趋势感知，同时避免长期历史记录导致查询和计算放大。
     */
    private List<HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO> buildDailyIndicatorTrendSummaries(
        List<HealthChronicDiaryEntryEntity> dailyIndicatorEntries, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (dailyIndicatorEntries == null || dailyIndicatorEntries.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<HealthChronicDiaryEntryEntity>> groupedEntries = dailyIndicatorEntries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getMemberId() + "::" + entry.getEntryType(),
                LinkedHashMap::new, Collectors.toList()));
        return groupedEntries.values().stream()
            .map(entries -> buildDailyIndicatorTrendSummary(entries, memberMap))
            .filter(Objects::nonNull)
            .limit(8)
            .collect(Collectors.toList());
    }

    private HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO buildDailyIndicatorTrendSummary(
        List<HealthChronicDiaryEntryEntity> entries, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        entries.sort(Comparator
            .comparing(HealthChronicDiaryEntryEntity::getRecordTime, Comparator.nullsLast(Date::compareTo))
            .thenComparing(HealthChronicDiaryEntryEntity::getDiaryEntryId, Comparator.nullsLast(Long::compareTo))
            .reversed());
        HealthChronicDiaryEntryEntity latest = entries.get(0);
        HealthChronicDiaryEntryEntity previous = entries.size() > 1 ? entries.get(1) : null;
        BigDecimal latestValue = resolveDailyIndicatorPrimaryNumber(latest);
        BigDecimal previousValue = resolveDailyIndicatorPrimaryNumber(previous);
        String changeDirection = resolveDailyIndicatorChangeDirection(latestValue, previousValue);

        HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO dto =
            new HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO();
        dto.setMemberId(latest.getMemberId());
        dto.setMemberName(resolveMemberNameFromMap(memberMap, latest.getMemberId()));
        dto.setEntryType(latest.getEntryType());
        dto.setTypeName(resolveDailyIndicatorName(latest.getEntryType()));
        dto.setRecordCount(entries.size());
        dto.setLatestValueText(resolveDailyIndicatorValueText(latest));
        dto.setLatestRecordTime(latest.getRecordTime());
        dto.setPreviousValueText(resolveDailyIndicatorValueText(previous));
        dto.setChangeDirection(changeDirection);
        dto.setChangeDirectionName(resolveDailyIndicatorChangeDirectionName(changeDirection));
        dto.setSummary(buildDailyIndicatorTrendSummaryText(dto));
        return dto;
    }

    /**
     * 安全解析日常指标扩展 JSON。
     *
     * <p>历史日记可能没有结构化 payload，也可能来自手工录入文本；解析失败时返回空对象，
     * 上层会回退到 entryContent 展示，避免单条脏数据影响整个长期管理卡片。
     */
    private JSONObject parseDailyIndicatorMetricPayload(HealthChronicDiaryEntryEntity entry) {
        if (entry == null || StrUtil.isBlank(entry.getMetricPayloadJson())) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(entry.getMetricPayloadJson());
        } catch (RuntimeException ignored) {
            return new JSONObject();
        }
    }

    private BigDecimal resolveDailyIndicatorPrimaryNumber(HealthChronicDiaryEntryEntity entry) {
        JSONObject payload = parseDailyIndicatorMetricPayload(entry);
        Object primaryValue = payload.get("primaryValue");
        return primaryValue == null ? null : parseReportItemNumber(String.valueOf(primaryValue));
    }

    private String resolveDailyIndicatorValueText(HealthChronicDiaryEntryEntity entry) {
        if (entry == null) {
            return null;
        }
        JSONObject payload = parseDailyIndicatorMetricPayload(entry);
        Object primaryValue = payload.get("primaryValue");
        Object secondaryValue = payload.get("secondaryValue");
        String unit = StrUtil.blankToDefault(payload.getStr("metricUnit"), "");
        if (primaryValue == null && secondaryValue == null) {
            return StrUtil.trim(entry.getEntryContent());
        }
        if (secondaryValue != null) {
            return StrUtil.format("{}/{}{}", primaryValue == null ? "-" : primaryValue, secondaryValue, unit);
        }
        return StrUtil.format("{}{}", primaryValue, unit);
    }

    private String resolveDailyIndicatorChangeDirection(BigDecimal latestValue, BigDecimal previousValue) {
        if (latestValue == null || previousValue == null) {
            return previousValue == null ? "NO_BASELINE" : "UNMEASURABLE";
        }
        int compare = latestValue.compareTo(previousValue);
        if (compare == 0) {
            return "UNCHANGED";
        }
        return compare > 0 ? "UP" : "DOWN";
    }

    private String resolveDailyIndicatorChangeDirectionName(String changeDirection) {
        if (Objects.equals(changeDirection, "UP")) {
            return "较上次升高";
        }
        if (Objects.equals(changeDirection, "DOWN")) {
            return "较上次降低";
        }
        if (Objects.equals(changeDirection, "UNCHANGED")) {
            return "较上次持平";
        }
        if (Objects.equals(changeDirection, "UNMEASURABLE")) {
            return "暂无法量化";
        }
        return "等待更多记录";
    }

    private String buildDailyIndicatorTrendSummaryText(
        HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO dto) {
        if (Objects.equals(dto.getChangeDirection(), "NO_BASELINE")) {
            return StrUtil.format("{}最近记录为{}，继续记录后可形成趋势。", dto.getTypeName(),
                StrUtil.blankToDefault(dto.getLatestValueText(), "-"));
        }
        if (Objects.equals(dto.getChangeDirection(), "UNMEASURABLE")) {
            return StrUtil.format("{}最近记录为{}，上次记录为{}，暂无法量化对比。", dto.getTypeName(),
                StrUtil.blankToDefault(dto.getLatestValueText(), "-"),
                StrUtil.blankToDefault(dto.getPreviousValueText(), "-"));
        }
        return StrUtil.format("{}最近记录为{}，上次为{}，{}。", dto.getTypeName(),
            StrUtil.blankToDefault(dto.getLatestValueText(), "-"),
            StrUtil.blankToDefault(dto.getPreviousValueText(), "-"), dto.getChangeDirectionName());
    }

    private List<HealthLongTermManagementDTO.DailyIndicatorTypeDTO> buildSupportedDailyIndicatorTypes() {
        List<HealthLongTermManagementDTO.DailyIndicatorTypeDTO> types = new ArrayList<>();
        types.add(buildDailyIndicatorType("BLOOD_PRESSURE", "血压", "mmHg", "如 128 / 82", "适合高血压、心血管风险等长期观察。"));
        types.add(buildDailyIndicatorType("BLOOD_GLUCOSE", "血糖", "mmol/L", "如 6.1", "适合糖尿病、糖耐量异常等长期观察。"));
        types.add(buildDailyIndicatorType("WEIGHT", "体重", "kg", "如 68.5", "适合体重管理、代谢类慢病和康复阶段跟踪。"));
        types.add(buildDailyIndicatorType("HEART_RATE", "心率", "次/分", "如 76", "适合心血管、运动恢复和不适症状记录。"));
        types.add(buildDailyIndicatorType("SPO2", "血氧", "%", "如 98", "适合呼吸系统、术后恢复和家庭照护场景。"));
        types.add(buildDailyIndicatorType("TEMPERATURE", "体温", "℃", "如 36.7", "适合感染、发热和术后恢复观察。"));
        types.add(buildDailyIndicatorType("SYMPTOM", "症状", "", "如 头晕、咳嗽、乏力", "适合任何慢病或临时不适记录。"));
        types.add(buildDailyIndicatorType("SLEEP", "睡眠", "小时", "如 7.5", "适合睡眠、精神状态和慢病恢复观察。"));
        types.add(buildDailyIndicatorType("EXERCISE", "运动", "分钟", "如 30", "适合康复运动、控糖和体重管理记录。"));
        types.add(buildDailyIndicatorType("GENERAL", "通用记录", "", "补充任意健康事件", "用于暂未结构化的健康观察。"));
        return types;
    }

    private HealthLongTermManagementDTO.DailyIndicatorTypeDTO buildDailyIndicatorType(String entryType, String typeName,
        String unitHint, String placeholder, String description) {
        HealthLongTermManagementDTO.DailyIndicatorTypeDTO dto = new HealthLongTermManagementDTO.DailyIndicatorTypeDTO();
        dto.setEntryType(entryType);
        dto.setTypeName(typeName);
        dto.setUnitHint(unitHint);
        dto.setPlaceholder(placeholder);
        dto.setDescription(description);
        return dto;
    }

    private List<HealthLongTermManagementDTO.IndicatorTargetTrendDTO> buildIndicatorTargetTrends(
        List<HealthChronicDiseaseProfileEntity> profiles, List<HealthReportEntity> reports,
        List<HealthReportItemEntity> reportItems, Map<Long, HealthFamilyMemberEntity> memberMap) {
        Set<Long> profileIds = profiles.stream()
            .map(HealthChronicDiseaseProfileEntity::getProfileId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (profileIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, HealthChronicDiseaseProfileEntity> profileMap = profiles.stream()
            .collect(Collectors.toMap(HealthChronicDiseaseProfileEntity::getProfileId, Function.identity(),
                (left, right) -> left, LinkedHashMap::new));
        Map<Long, HealthReportEntity> reportMap = reports.stream()
            .collect(Collectors.toMap(HealthReportEntity::getReportId, Function.identity(), (left, right) -> left,
                LinkedHashMap::new));
        return listActiveIndicatorTargets(profileIds).stream()
            .map(target -> buildIndicatorTargetTrend(target, profileMap, reportItems, reportMap, memberMap))
            .limit(12)
            .collect(Collectors.toList());
    }

    private List<HealthChronicIndicatorTargetEntity> listActiveIndicatorTargets(Set<Long> profileIds) {
        if (profileIds == null || profileIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chronicIndicatorTargetService.lambdaQuery()
            .in(HealthChronicIndicatorTargetEntity::getProfileId, profileIds)
            .eq(HealthChronicIndicatorTargetEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByAsc(HealthChronicIndicatorTargetEntity::getProfileId)
            .orderByAsc(HealthChronicIndicatorTargetEntity::getIndicatorName)
            .list();
    }

    private HealthLongTermManagementDTO.IndicatorTargetTrendDTO buildIndicatorTargetTrend(
        HealthChronicIndicatorTargetEntity target, Map<Long, HealthChronicDiseaseProfileEntity> profileMap,
        List<HealthReportItemEntity> reportItems, Map<Long, HealthReportEntity> reportMap,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthChronicDiseaseProfileEntity profile = profileMap.get(target.getProfileId());
        Long memberId = target.getMemberId() == null && profile != null ? profile.getMemberId() : target.getMemberId();
        HealthReportItemEntity latestItem = findLatestTargetReportItem(target, memberId, reportItems, reportMap);
        HealthReportEntity latestReport = latestItem == null ? null : reportMap.get(latestItem.getReportId());
        String status = resolveLongTermTargetHitStatus(latestItem, target);

        HealthLongTermManagementDTO.IndicatorTargetTrendDTO dto = new HealthLongTermManagementDTO.IndicatorTargetTrendDTO();
        dto.setMemberId(memberId);
        dto.setMemberName(resolveMemberNameFromMap(memberMap, memberId));
        dto.setProfileId(target.getProfileId());
        dto.setDiseaseName(profile == null ? null : profile.getDiseaseNameSnapshot());
        dto.setIndicatorCode(target.getIndicatorCode());
        dto.setIndicatorName(StrUtil.blankToDefault(target.getIndicatorName(), latestItem == null ? "指标" : latestItem.getItemName()));
        dto.setLatestValue(latestItem == null ? null : latestItem.getResultValue());
        dto.setLatestUnit(latestItem == null ? null : latestItem.getResultUnit());
        dto.setLatestTime(latestReport == null ? null : latestReport.getReportDate());
        dto.setTargetMin(target.getTargetMin());
        dto.setTargetMax(target.getTargetMax());
        dto.setTargetUnit(target.getResultUnit());
        dto.setTargetText(target.getTargetText());
        dto.setTargetHitStatus(status);
        dto.setTargetHitStatusName(resolveLongTermTargetHitStatusName(status));
        dto.setSummary(buildLongTermTargetSummary(latestItem, target, status));
        return dto;
    }

    private HealthReportItemEntity findLatestTargetReportItem(HealthChronicIndicatorTargetEntity target, Long memberId,
        List<HealthReportItemEntity> reportItems, Map<Long, HealthReportEntity> reportMap) {
        if (memberId == null || reportItems == null || reportItems.isEmpty()) {
            return null;
        }
        return reportItems.stream()
            .filter(item -> matchesLongTermTargetIndicator(item, target))
            .filter(item -> {
                HealthReportEntity report = reportMap.get(item.getReportId());
                return report != null && Objects.equals(report.getMemberId(), memberId);
            })
            .max(Comparator
                .comparing((HealthReportItemEntity item) -> safeLongTermReportDate(reportMap.get(item.getReportId())))
                .thenComparing(HealthReportItemEntity::getReportId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(HealthReportItemEntity::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(HealthReportItemEntity::getItemId, Comparator.nullsLast(Long::compareTo)))
            .orElse(null);
    }

    private boolean matchesLongTermTargetIndicator(HealthReportItemEntity item, HealthChronicIndicatorTargetEntity target) {
        String targetCode = normalizeLongTermIndicatorCode(target.getIndicatorCode());
        if (StrUtil.isBlank(targetCode)) {
            return false;
        }
        return Objects.equals(targetCode, normalizeLongTermIndicatorCode(item.getStandardItemCode()))
            || Objects.equals(targetCode, normalizeLongTermIndicatorCode(item.getItemCode()))
            || Objects.equals(targetCode, normalizeLongTermIndicatorCode(item.getItemName()));
    }

    private String resolveLongTermTargetHitStatus(HealthReportItemEntity latestItem,
        HealthChronicIndicatorTargetEntity target) {
        if (latestItem == null) {
            return "NO_RECENT_VALUE";
        }
        if (target.getTargetMin() == null && target.getTargetMax() == null) {
            return "TEXT_ONLY";
        }
        BigDecimal currentValue = parseReportItemNumber(latestItem.getResultValue());
        if (currentValue == null) {
            return "UNMEASURABLE";
        }
        if (target.getTargetMin() != null && currentValue.compareTo(target.getTargetMin()) < 0) {
            return "BELOW";
        }
        if (target.getTargetMax() != null && currentValue.compareTo(target.getTargetMax()) > 0) {
            return "ABOVE";
        }
        return "IN_RANGE";
    }

    private String resolveLongTermTargetHitStatusName(String status) {
        if (Objects.equals(status, "IN_RANGE")) {
            return "目标内";
        }
        if (Objects.equals(status, "BELOW")) {
            return "低于目标";
        }
        if (Objects.equals(status, "ABOVE")) {
            return "高于目标";
        }
        if (Objects.equals(status, "TEXT_ONLY")) {
            return "已维护说明";
        }
        if (Objects.equals(status, "NO_RECENT_VALUE")) {
            return "无近期值";
        }
        return "暂无法判断";
    }

    private String buildLongTermTargetSummary(HealthReportItemEntity latestItem, HealthChronicIndicatorTargetEntity target,
        String status) {
        String targetText = buildLongTermTargetRangeText(target);
        String indicatorName = StrUtil.blankToDefault(target.getIndicatorName(), "该指标");
        if (latestItem == null) {
            return StrUtil.format("{}已维护个人目标：{}，近期报告暂无可匹配结果。", indicatorName, targetText);
        }
        String currentText = StrUtil.format("{}{}", StrUtil.blankToDefault(latestItem.getResultValue(), "-"),
            StrUtil.blankToDefault(latestItem.getResultUnit(), ""));
        if (Objects.equals(status, "IN_RANGE")) {
            return StrUtil.format("最新值 {} 已进入个人目标范围：{}。", currentText, targetText);
        }
        if (Objects.equals(status, "BELOW")) {
            return StrUtil.format("最新值 {} 低于个人目标范围：{}，建议结合复查计划继续观察。", currentText, targetText);
        }
        if (Objects.equals(status, "ABOVE")) {
            return StrUtil.format("最新值 {} 高于个人目标范围：{}，建议优先关注复查和干预执行。", currentText, targetText);
        }
        if (Objects.equals(status, "TEXT_ONLY")) {
            return StrUtil.format("该指标已维护目标说明：{}。", targetText);
        }
        return StrUtil.format("该指标已维护个人目标：{}，但最新值 {} 暂无法量化比对。", targetText, currentText);
    }

    private String buildLongTermTargetRangeText(HealthChronicIndicatorTargetEntity target) {
        if (StrUtil.isNotBlank(target.getTargetText())) {
            return target.getTargetText();
        }
        String unit = StrUtil.blankToDefault(target.getResultUnit(), "");
        if (target.getTargetMin() != null && target.getTargetMax() != null) {
            return StrUtil.format("{}-{}{}", target.getTargetMin().stripTrailingZeros().toPlainString(),
                target.getTargetMax().stripTrailingZeros().toPlainString(), unit);
        }
        if (target.getTargetMin() != null) {
            return StrUtil.format("不低于{}{}", target.getTargetMin().stripTrailingZeros().toPlainString(), unit);
        }
        if (target.getTargetMax() != null) {
            return StrUtil.format("不高于{}{}", target.getTargetMax().stripTrailingZeros().toPlainString(), unit);
        }
        return "未填写目标范围";
    }

    private String normalizeLongTermIndicatorCode(String value) {
        return StrUtil.blankToDefault(value, "").trim().toUpperCase(Locale.ROOT);
    }

    private Date safeLongTermReportDate(HealthReportEntity report) {
        return report == null || report.getReportDate() == null ? new Date(0L) : report.getReportDate();
    }
    private List<HealthLongTermManagementDTO.ReviewSuggestionDTO> buildReviewSuggestions(
        List<HealthOperationTaskEntity> reviewTasks, List<HealthProblemEntity> healthProblems,
        List<HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO> indicatorAlerts,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        List<HealthLongTermManagementDTO.ReviewSuggestionDTO> suggestions = new ArrayList<>();
        for (HealthOperationTaskEntity task : reviewTasks) {
            if (suggestions.size() >= 6) {
                return suggestions;
            }
            addReviewSuggestion(suggestions, "REVIEW_TASK_" + task.getOperationTaskId(),
                StrUtil.blankToDefault(task.getTaskTitle(), "慢病复查提醒"),
                StrUtil.blankToDefault(task.getTaskContent(), "慢病专项已存在复查任务。"), "CHRONIC_REVIEW",
                task.getMemberId(), resolveMemberNameFromMap(memberMap, task.getMemberId()), task.getTargetBizId(),
                task.getStartTime(), StrUtil.blankToDefault(task.getRiskLevel(), "MEDIUM"));
        }
        for (HealthProblemEntity problem : healthProblems) {
            if (suggestions.size() >= 6) {
                return suggestions;
            }
            if (!Objects.equals(problem.getProblemStatus(), 1)) {
                continue;
            }
            addReviewSuggestion(suggestions, "HEALTH_PROBLEM_" + problem.getProblemId(),
                StrUtil.blankToDefault(problem.getProblemName(), "健康问题") + "建议跟进",
                StrUtil.blankToDefault(problem.getSummary(), "该问题仍在跟进中，建议结合近期报告安排复查。"),
                "HEALTH_PROBLEM", problem.getMemberId(), resolveMemberNameFromMap(memberMap, problem.getMemberId()),
                problem.getProblemId(), DateUtil.offsetDay(new Date(), Objects.equals(problem.getRiskLevel(), 3) ? 7 : 30),
                resolveOperationTaskRiskLevel(problem.getRiskLevel()));
        }
        for (HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO alert : indicatorAlerts) {
            if (suggestions.size() >= 6) {
                return suggestions;
            }
            addReviewSuggestion(suggestions, "INDICATOR_ALERT_" + alert.getReportId() + "_" + alert.getIndicatorCode(),
                StrUtil.blankToDefault(alert.getAlertTitle(), "指标趋势建议复查"),
                StrUtil.blankToDefault(alert.getAlertContent(), "近期指标存在异常或明显波动，建议持续记录并按需复查。"),
                "INDICATOR_ALERT", alert.getMemberId(), alert.getMemberName(), alert.getReportId(),
                DateUtil.offsetDay(new Date(), HealthFollowUpRiskLevelEnum.HIGH.getValue().equals(alert.getRiskLevel()) ? 7 : 30),
                alert.getRiskLevel());
        }
        return suggestions;
    }

    private void addReviewSuggestion(List<HealthLongTermManagementDTO.ReviewSuggestionDTO> suggestions,
        String suggestionCode, String title, String reason, String targetType, Long memberId, String memberName,
        Long targetBizId, Date suggestReviewTime, String priorityLevel) {
        HealthLongTermManagementDTO.ReviewSuggestionDTO dto = new HealthLongTermManagementDTO.ReviewSuggestionDTO();
        dto.setSuggestionCode(suggestionCode);
        dto.setTitle(title);
        dto.setReason(reason);
        dto.setTargetType(targetType);
        dto.setMemberId(memberId);
        dto.setMemberName(memberName);
        dto.setTargetBizId(targetBizId);
        dto.setSuggestReviewTime(suggestReviewTime);
        dto.setPriorityLevel(priorityLevel);
        suggestions.add(dto);
    }

    private HealthLongTermManagementDTO.VisitPackageShareDTO buildVisitPackageShare(
        HealthInsightWorkbenchDTO.MedicalVisitPackageDTO packageSummary, List<HealthFamilyMemberEntity> members,
        List<HealthChronicDiseaseProfileEntity> profiles, List<HealthReportEntity> reports,
        List<HealthMedicationPlanEntity> activePlans, List<HealthProblemEntity> healthProblems) {
        HealthLongTermManagementDTO.VisitPackageShareDTO dto = new HealthLongTermManagementDTO.VisitPackageShareDTO();
        dto.setTitle(StrUtil.blankToDefault(packageSummary.getPackageTitle(), "就医资料包"));
        dto.setSummary(StrUtil.blankToDefault(packageSummary.getSummary(), "已整理成员、慢病、报告和用药摘要。"));
        dto.setSections(packageSummary.getSections());
        dto.setPrivacySafeShareText(buildPrivacySafeVisitPackageText(dto, members, profiles, reports, activePlans,
            healthProblems));
        List<String> lines = new ArrayList<>();
        lines.add(dto.getTitle());
        lines.add(dto.getSummary());
        lines.add(StrUtil.format("成员：{}位；慢病专项：{}项；近期报告：{}份；当前用药：{}项；健康问题：{}个。",
            members.size(), profiles.size(), reports.size(), activePlans.size(), healthProblems.size()));
        if (!packageSummary.getSections().isEmpty()) {
            lines.add("资料范围：" + String.join("、", packageSummary.getSections()));
        }
        dto.getShareCardSections().add(StrUtil.format("家庭成员 {} 位", members.size()));
        dto.getShareCardSections().add(StrUtil.format("慢病专项 {} 项", profiles.size()));
        dto.getShareCardSections().add(StrUtil.format("近期报告 {} 份", reports.size()));
        dto.getShareCardSections().add(StrUtil.format("当前用药 {} 项", activePlans.size()));
        dto.getShareCardSections().add(StrUtil.format("健康问题 {} 个", healthProblems.size()));
        dto.setShareText(String.join("\n", lines));
        dto.setLongImageText(String.join("\n\n", lines));
        return dto;
    }

    /**
     * 生成资料包脱敏分享文本。
     *
     * <p>脱敏版只保留数量和资料范围，不包含姓名、医院、药名和具体指标值，
     * 适合用户在不确定分享对象可信度时先发出概览，再决定是否发送完整资料包。
     */
    private String buildPrivacySafeVisitPackageText(HealthLongTermManagementDTO.VisitPackageShareDTO dto,
        List<HealthFamilyMemberEntity> members, List<HealthChronicDiseaseProfileEntity> profiles,
        List<HealthReportEntity> reports, List<HealthMedicationPlanEntity> activePlans,
        List<HealthProblemEntity> healthProblems) {
        List<String> lines = new ArrayList<>();
        lines.add(StrUtil.blankToDefault(dto.getTitle(), "就医资料包") + "（脱敏版）");
        lines.add("该摘要只包含数量和资料范围，不包含姓名、医院、药名和具体指标结果。");
        lines.add(StrUtil.format("成员{}位，慢病专项{}项，近期报告{}份，当前用药{}项，健康问题{}个。",
            members.size(), profiles.size(), reports.size(), activePlans.size(), healthProblems.size()));
        if (!dto.getSections().isEmpty()) {
            lines.add("资料范围：" + String.join("、", dto.getSections()));
        }
        lines.add("如需给医生或家属查看明细，请使用完整资料包并确认分享范围。");
        return String.join(System.lineSeparator(), lines);
    }

    private HealthLongTermManagementDTO.CareCollaborationDTO buildCareCollaboration(List<HealthFamilyMemberEntity> members,
        List<HealthMedicationReminderEntity> pendingReminders, List<HealthOperationTaskEntity> careActionTasks,
        Map<Long, HealthFamilyMemberEntity> memberMap, HealthInsightWorkbenchDTO.CareDashboardDTO careDashboard) {
        HealthLongTermManagementDTO.CareCollaborationDTO dto = new HealthLongTermManagementDTO.CareCollaborationDTO();
        dto.setMemberCount(members.size());
        dto.setPendingCareTaskCount(careActionTasks.size());
        dto.setFocusMemberName(careDashboard.getFocusMemberName());
        dto.setPendingTasks(careActionTasks.stream().limit(6).map(task -> buildCareTask(task, memberMap))
            .collect(Collectors.toList()));
        dto.setSummary(StrUtil.format("{}位成员可协同管理，当前有{}条照护任务和{}条待处理用药提醒。",
            dto.getMemberCount(), dto.getPendingCareTaskCount(), pendingReminders.size()));
        return dto;
    }

    private HealthLongTermManagementDTO.CareTaskDTO buildCareTask(HealthOperationTaskEntity task,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthLongTermManagementDTO.CareTaskDTO dto = new HealthLongTermManagementDTO.CareTaskDTO();
        dto.setOperationTaskId(task.getOperationTaskId());
        dto.setMemberId(task.getMemberId());
        dto.setMemberName(resolveMemberNameFromMap(memberMap, task.getMemberId()));
        dto.setTaskTitle(task.getTaskTitle());
        dto.setTaskContent(task.getTaskContent());
        dto.setRiskLevel(task.getRiskLevel());
        dto.setStartTime(task.getStartTime());
        return dto;
    }

    private HealthLongTermManagementDTO.CareAssignmentSummaryDTO buildCareAssignmentSummary(
        List<HealthOperationTaskEntity> careActionTasks, Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthLongTermManagementDTO.CareAssignmentSummaryDTO dto = new HealthLongTermManagementDTO.CareAssignmentSummaryDTO();
        dto.setTaskCount(careActionTasks.size());
        dto.setOwnerTaskCount(careActionTasks.size());
        dto.setMemberTaskCount((int) careActionTasks.stream()
            .filter(task -> task.getMemberId() != null)
            .count());
        dto.setAssignments(careActionTasks.stream()
            .limit(6)
            .map(task -> buildCareAssignmentItem(task, memberMap))
            .collect(Collectors.toList()));
        dto.setSummary(careActionTasks.isEmpty()
            ? "当前暂无需要分派的照护任务。"
            : StrUtil.format("当前{}条照护任务由主账号承接，其中{}条已绑定到家庭成员。", dto.getTaskCount(),
                dto.getMemberTaskCount()));
        return dto;
    }

    private HealthLongTermManagementDTO.CareAssignmentItemDTO buildCareAssignmentItem(HealthOperationTaskEntity task,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthLongTermManagementDTO.CareAssignmentItemDTO dto = new HealthLongTermManagementDTO.CareAssignmentItemDTO();
        dto.setOperationTaskId(task.getOperationTaskId());
        dto.setOwnerUserId(task.getOwnerUserId());
        dto.setMemberId(task.getMemberId());
        dto.setMemberName(resolveMemberNameFromMap(memberMap, task.getMemberId()));
        dto.setTaskTitle(task.getTaskTitle());
        dto.setRiskLevel(task.getRiskLevel());
        dto.setStartTime(task.getStartTime());
        dto.setAssigneeRole("OWNER_ACCOUNT");
        dto.setAssignmentSource(StrUtil.isBlank(task.getTargetBizType()) ? "LONG_TERM_MANUAL" : task.getTargetBizType());
        dto.setSummary(StrUtil.format("{} · {} · {}", StrUtil.blankToDefault(dto.getMemberName(), "家庭成员"),
            StrUtil.blankToDefault(task.getRiskLevel(), "MEDIUM"),
            StrUtil.blankToDefault(task.getTaskContent(), "请按家庭照护计划处理。")));
        return dto;
    }

    private List<HealthLongTermManagementDTO.MedicationSafetyRuleDTO> buildMedicationSafetyRules(
        List<HealthMedicationPlanEntity> activePlans, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (activePlans.isEmpty()) {
            return Collections.emptyList();
        }
        List<HealthLongTermManagementDTO.MedicationSafetyRuleDTO> rules = new ArrayList<>();
        Map<String, List<HealthMedicationPlanEntity>> duplicatePlanMap = activePlans.stream()
            .collect(Collectors.groupingBy(plan -> plan.getMemberId() + "::" + resolvePlanDrugKey(plan),
                LinkedHashMap::new, Collectors.toList()));
        duplicatePlanMap.values().stream()
            .filter(plans -> plans.size() > 1)
            .forEach(plans -> rules.add(buildMedicationSafetyRule("DUPLICATE_MEDICATION", "重复用药计划",
                HealthFollowUpRiskLevelEnum.HIGH.getValue(),
                StrUtil.format("{}存在{}条{}相关计划，请核对是否重复录入。",
                    resolveMemberNameFromMap(memberMap, plans.get(0).getMemberId()), plans.size(),
                    resolvePlanDrugName(plans.get(0))), plans)));

        List<HealthMedicationPlanEntity> missingDosePlans = activePlans.stream()
            .filter(this::isMissingDosePlan)
            .collect(Collectors.toList());
        if (!missingDosePlans.isEmpty()) {
            rules.add(buildMedicationSafetyRule("MISSING_DOSE", "剂量信息缺失",
                HealthFollowUpRiskLevelEnum.MEDIUM.getValue(),
                StrUtil.format("{}条用药计划缺少每次剂量或剂量单位，建议补全后再用于提醒。", missingDosePlans.size()),
                missingDosePlans));
        }

        List<HealthMedicationPlanEntity> missingFrequencyPlans = activePlans.stream()
            .filter(this::isMissingFrequencyPlan)
            .collect(Collectors.toList());
        if (!missingFrequencyPlans.isEmpty()) {
            rules.add(buildMedicationSafetyRule("MISSING_FREQUENCY", "频率信息缺失",
                HealthFollowUpRiskLevelEnum.MEDIUM.getValue(),
                StrUtil.format("{}条用药计划缺少频率或提醒时间，可能影响后续服药提醒。", missingFrequencyPlans.size()),
                missingFrequencyPlans));
        }

        List<HealthMedicationPlanEntity> expiredEnabledPlans = activePlans.stream()
            .filter(this::isExpiredEnabledPlan)
            .collect(Collectors.toList());
        if (!expiredEnabledPlans.isEmpty()) {
            rules.add(buildMedicationSafetyRule("EXPIRED_ENABLED_PLAN", "已过结束日期仍启用",
                HealthFollowUpRiskLevelEnum.HIGH.getValue(),
                StrUtil.format("{}条用药计划已过结束日期但仍处于启用状态，请确认是否继续服用。", expiredEnabledPlans.size()),
                expiredEnabledPlans));
        }

        Map<String, List<HealthMedicationPlanEntity>> mealTimingMap = activePlans.stream()
            .collect(Collectors.groupingBy(plan -> plan.getMemberId() + "::"
                + StrUtil.blankToDefault(plan.getMealTiming(), "未设置时机"), LinkedHashMap::new, Collectors.toList()));
        mealTimingMap.values().stream()
            .filter(plans -> plans.size() >= 3)
            .forEach(plans -> rules.add(buildMedicationSafetyRule("DENSE_MEAL_TIMING", "同一时机计划较多",
                HealthFollowUpRiskLevelEnum.LOW.getValue(),
                StrUtil.format("{}在{}有{}条用药计划，建议检查提醒是否过于集中。",
                    resolveMemberNameFromMap(memberMap, plans.get(0).getMemberId()),
                    StrUtil.blankToDefault(plans.get(0).getMealTiming(), "未设置时机"), plans.size()), plans)));
        return rules.stream().limit(8).collect(Collectors.toList());
    }

    private HealthLongTermManagementDTO.MedicationSafetyRuleDTO buildMedicationSafetyRule(String code, String name,
        String riskLevel, String summary, List<HealthMedicationPlanEntity> relatedPlans) {
        HealthLongTermManagementDTO.MedicationSafetyRuleDTO dto = new HealthLongTermManagementDTO.MedicationSafetyRuleDTO();
        dto.setRuleCode(code);
        dto.setRuleName(name);
        dto.setRiskLevel(riskLevel);
        dto.setSummary(summary);
        dto.setRelatedPlanIds(relatedPlans.stream()
            .map(HealthMedicationPlanEntity::getPlanId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
        return dto;
    }

    private boolean isMissingDosePlan(HealthMedicationPlanEntity plan) {
        if (StrUtil.isNotBlank(plan.getDoseRule())) {
            return false;
        }
        return plan.getDoseAmount() == null || plan.getDoseAmount().compareTo(BigDecimal.ZERO) <= 0
            || StrUtil.isBlank(plan.getDoseUnit());
    }

    private boolean isMissingFrequencyPlan(HealthMedicationPlanEntity plan) {
        return StrUtil.isBlank(plan.getFrequencyType()) && StrUtil.isBlank(plan.getReminderTimesJson())
            && StrUtil.isBlank(plan.getDoseRule());
    }

    private boolean isExpiredEnabledPlan(HealthMedicationPlanEntity plan) {
        return plan.getEndDate() != null && plan.getEndDate().before(DateUtil.beginOfDay(new Date()));
    }

    private String resolvePlanDrugName(HealthMedicationPlanEntity plan) {
        if (StrUtil.isNotBlank(plan.getCustomDrugName())) {
            return plan.getCustomDrugName().trim();
        }
        if (plan.getDrugId() != null) {
            return "药品#" + plan.getDrugId();
        }
        return "未命名药品";
    }
    /**
     * 汇总长期管理下一步建议。
     *
     * <p>五期把已经存在的复查建议、个人目标趋势、用药规则和照护任务收口成一个有优先级的行动列表。
     * 这里故意只做本次工作台内存数据排序，不新增任务表，避免把“建议”误写成真实待办。
     */
    private List<HealthLongTermManagementDTO.NextActionHintDTO> buildNextActionHints(
        HealthLongTermManagementDTO data) {
        List<HealthLongTermManagementDTO.NextActionHintDTO> hints = new ArrayList<>();
        for (HealthLongTermManagementDTO.MedicationSafetyRuleDTO rule : data.getMedicationSafetyRules()) {
            hints.add(buildNextActionHint("MEDICATION_" + rule.getRuleCode(), "MEDICATION_SAFETY",
                rule.getRuleName(), rule.getSummary(), "核对用药", rule.getRiskLevel(), null, null, null,
                rule.getRuleCode()));
        }
        for (HealthLongTermManagementDTO.IndicatorTargetTrendDTO trend : data.getIndicatorTargetTrends()) {
            if (!isActionableTargetTrend(trend.getTargetHitStatus())) {
                continue;
            }
            hints.add(buildNextActionHint("TARGET_" + trend.getProfileId() + "_" + trend.getIndicatorCode(),
                "TARGET_TREND", StrUtil.blankToDefault(trend.getIndicatorName(), "指标目标"), trend.getSummary(),
                "查看目标", resolveTargetTrendRiskLevel(trend.getTargetHitStatus()), trend.getMemberId(),
                trend.getMemberName(), trend.getProfileId(), trend.getTargetHitStatus()));
        }
        for (HealthLongTermManagementDTO.ReviewSuggestionDTO suggestion : data.getReviewSuggestions()) {
            hints.add(buildNextActionHint("REVIEW_" + suggestion.getSuggestionCode(), "REVIEW_SUGGESTION",
                suggestion.getTitle(), suggestion.getReason(), "生成任务", suggestion.getPriorityLevel(),
                suggestion.getMemberId(), suggestion.getMemberName(), suggestion.getTargetBizId(),
                suggestion.getTargetType()));
        }
        for (HealthLongTermManagementDTO.CareTaskDTO task : data.getCareCollaboration().getPendingTasks()) {
            hints.add(buildNextActionHint("CARE_" + task.getOperationTaskId(), "CARE_TASK", task.getTaskTitle(),
                task.getTaskContent(), "处理任务", task.getRiskLevel(), task.getMemberId(), task.getMemberName(),
                task.getOperationTaskId(), "CARE_ACTION"));
        }
        for (HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO trend
            : data.getDailyIndicatorBoard().getTrendSummaries()) {
            if (!Objects.equals(trend.getChangeDirection(), "UP") && !Objects.equals(trend.getChangeDirection(), "DOWN")) {
                continue;
            }
            hints.add(buildNextActionHint("DAILY_TREND_" + trend.getMemberId() + "_" + trend.getEntryType(),
                "DAILY_INDICATOR_TREND", trend.getTypeName() + "趋势", trend.getSummary(), "继续记录",
                HealthFollowUpRiskLevelEnum.LOW.getValue(), trend.getMemberId(), trend.getMemberName(), null,
                trend.getEntryType()));
        }
        if (hints.isEmpty()) {
            hints.add(buildNextActionHint("KEEP_RECORDING", "LONG_TERM_BASELINE", "继续积累健康记录",
                "当前暂无高优先级行动，建议继续记录日常指标并定期维护资料包。", "记录指标",
                HealthFollowUpRiskLevelEnum.LOW.getValue(), null, null, null, "DAILY_INDICATOR"));
        }
        return hints.stream()
            .sorted(Comparator.comparing(HealthLongTermManagementDTO.NextActionHintDTO::getPriorityWeight).reversed())
            .limit(6)
            .collect(Collectors.toList());
    }

    private HealthLongTermManagementDTO.NextActionHintDTO buildNextActionHint(String code, String type, String title,
        String summary, String actionText, String riskLevel, Long memberId, String memberName, Long targetBizId,
        String targetBizType) {
        HealthLongTermManagementDTO.NextActionHintDTO dto = new HealthLongTermManagementDTO.NextActionHintDTO();
        dto.setHintCode(code);
        dto.setHintType(type);
        dto.setTitle(StrUtil.blankToDefault(title, "长期管理建议"));
        dto.setSummary(StrUtil.blankToDefault(summary, "建议按长期管理计划持续跟进。"));
        dto.setActionText(StrUtil.blankToDefault(actionText, "查看"));
        dto.setRiskLevel(StrUtil.blankToDefault(riskLevel, HealthFollowUpRiskLevelEnum.LOW.getValue()));
        dto.setPriorityWeight(resolveNextActionPriority(dto.getRiskLevel(), type));
        dto.setMemberId(memberId);
        dto.setMemberName(memberName);
        dto.setTargetBizId(targetBizId);
        dto.setTargetBizType(targetBizType);
        return dto;
    }

    private boolean isActionableTargetTrend(String targetHitStatus) {
        return Objects.equals(targetHitStatus, "ABOVE") || Objects.equals(targetHitStatus, "BELOW")
            || Objects.equals(targetHitStatus, "UNMEASURABLE");
    }

    private String resolveTargetTrendRiskLevel(String targetHitStatus) {
        if (Objects.equals(targetHitStatus, "ABOVE") || Objects.equals(targetHitStatus, "BELOW")) {
            return HealthFollowUpRiskLevelEnum.MEDIUM.getValue();
        }
        return HealthFollowUpRiskLevelEnum.LOW.getValue();
    }

    private int resolveNextActionPriority(String riskLevel, String hintType) {
        int base = resolveCareTaskPriority(riskLevel);
        if (Objects.equals(hintType, "MEDICATION_SAFETY")) {
            return base + 8;
        }
        if (Objects.equals(hintType, "TARGET_TREND")) {
            return base + 6;
        }
        if (Objects.equals(hintType, "REVIEW_SUGGESTION")) {
            return base + 4;
        }
        if (Objects.equals(hintType, "CARE_TASK")) {
            return base + 2;
        }
        return base;
    }

    /**
     * 按家庭成员汇总长期管理关注点。
     *
     * <p>六期只使用本次长期管理对象中已经算好的建议、趋势和任务数据，
     * 不再回表查询，避免成员越多时再次放大工作台首屏查询成本。
     */
    private List<HealthLongTermManagementDTO.MemberFocusSummaryDTO> buildMemberFocusSummaries(
        List<HealthFamilyMemberEntity> members, HealthLongTermManagementDTO data) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, HealthLongTermManagementDTO.MemberFocusSummaryDTO> summaryMap = new LinkedHashMap<>();
        for (HealthFamilyMemberEntity member : members) {
            HealthLongTermManagementDTO.MemberFocusSummaryDTO item = new HealthLongTermManagementDTO.MemberFocusSummaryDTO();
            item.setMemberId(member.getMemberId());
            item.setMemberName(member.getMemberName());
            item.setRiskLevel(HealthFollowUpRiskLevelEnum.LOW.getValue());
            summaryMap.put(member.getMemberId(), item);
        }
        fillMemberFocusFromNextActions(summaryMap, data);
        fillMemberFocusFromSourceData(summaryMap, data);
        summaryMap.values().forEach(this::fillMemberFocusSummaryText);
        return summaryMap.values().stream()
            .sorted(Comparator.comparing(HealthLongTermManagementDTO.MemberFocusSummaryDTO::getPriorityWeight).reversed()
                .thenComparing(Comparator.comparing(
                    HealthLongTermManagementDTO.MemberFocusSummaryDTO::getNextActionCount).reversed()))
            .limit(8)
            .collect(Collectors.toList());
    }

    private void fillMemberFocusFromNextActions(
        Map<Long, HealthLongTermManagementDTO.MemberFocusSummaryDTO> summaryMap,
        HealthLongTermManagementDTO data) {
        for (HealthLongTermManagementDTO.NextActionHintDTO hint : data.getNextActionHints()) {
            HealthLongTermManagementDTO.MemberFocusSummaryDTO item = summaryMap.get(hint.getMemberId());
            if (item == null) {
                continue;
            }
            item.setNextActionCount(item.getNextActionCount() + 1);
            item.setPriorityWeight(Math.max(item.getPriorityWeight(), hint.getPriorityWeight()));
            item.setRiskLevel(resolveHigherRiskLevel(item.getRiskLevel(), hint.getRiskLevel()));
        }
    }

    private void fillMemberFocusFromSourceData(
        Map<Long, HealthLongTermManagementDTO.MemberFocusSummaryDTO> summaryMap,
        HealthLongTermManagementDTO data) {
        for (HealthLongTermManagementDTO.ReviewSuggestionDTO suggestion : data.getReviewSuggestions()) {
            HealthLongTermManagementDTO.MemberFocusSummaryDTO item = summaryMap.get(suggestion.getMemberId());
            if (item != null) {
                item.setReviewSuggestionCount(item.getReviewSuggestionCount() + 1);
                item.setRiskLevel(resolveHigherRiskLevel(item.getRiskLevel(), suggestion.getPriorityLevel()));
                raiseMemberFocusPriority(item, suggestion.getPriorityLevel(), "REVIEW_SUGGESTION");
            }
        }
        for (HealthLongTermManagementDTO.IndicatorTargetTrendDTO trend : data.getIndicatorTargetTrends()) {
            if (!isActionableTargetTrend(trend.getTargetHitStatus())) {
                continue;
            }
            HealthLongTermManagementDTO.MemberFocusSummaryDTO item = summaryMap.get(trend.getMemberId());
            if (item != null) {
                item.setTargetWarningCount(item.getTargetWarningCount() + 1);
                String riskLevel = resolveTargetTrendRiskLevel(trend.getTargetHitStatus());
                item.setRiskLevel(resolveHigherRiskLevel(item.getRiskLevel(), riskLevel));
                raiseMemberFocusPriority(item, riskLevel, "TARGET_TREND");
            }
        }
        for (HealthLongTermManagementDTO.CareTaskDTO task : data.getCareCollaboration().getPendingTasks()) {
            HealthLongTermManagementDTO.MemberFocusSummaryDTO item = summaryMap.get(task.getMemberId());
            if (item != null) {
                item.setCareTaskCount(item.getCareTaskCount() + 1);
                item.setRiskLevel(resolveHigherRiskLevel(item.getRiskLevel(), task.getRiskLevel()));
                raiseMemberFocusPriority(item, task.getRiskLevel(), "CARE_TASK");
            }
        }
        for (HealthLongTermManagementDTO.DailyIndicatorTrendSummaryDTO trend
            : data.getDailyIndicatorBoard().getTrendSummaries()) {
            if (!Objects.equals(trend.getChangeDirection(), "UP") && !Objects.equals(trend.getChangeDirection(), "DOWN")) {
                continue;
            }
            HealthLongTermManagementDTO.MemberFocusSummaryDTO item = summaryMap.get(trend.getMemberId());
            if (item != null) {
                item.setDailyTrendCount(item.getDailyTrendCount() + 1);
                raiseMemberFocusPriority(item, HealthFollowUpRiskLevelEnum.LOW.getValue(), "DAILY_INDICATOR_TREND");
            }
        }
    }

    /**
     * 从源数据补权重，避免成员没有进入“下一步建议”前 6 条时被排序低估。
     *
     * <p>权重仍复用下一步建议的规则，不在成员关注摘要里额外定义新的优先级体系。
     */
    private void raiseMemberFocusPriority(HealthLongTermManagementDTO.MemberFocusSummaryDTO item, String riskLevel,
        String sourceType) {
        item.setPriorityWeight(Math.max(item.getPriorityWeight(), resolveNextActionPriority(riskLevel, sourceType)));
    }

    /**
     * 补齐成员关注摘要展示文案和标签。
     *
     * <p>这里不再反查任何业务表，只把本次已经统计出的计数转成 App 可直接展示的短标签。
     * 这样成员关注区既能复用长期管理聚合结果，也不会因为标签展示引入新的查询成本。
     */
    private void fillMemberFocusSummaryText(HealthLongTermManagementDTO.MemberFocusSummaryDTO item) {
        if (item.getReviewSuggestionCount() > 0) {
            item.getTags().add("复查 " + item.getReviewSuggestionCount());
        }
        if (item.getTargetWarningCount() > 0) {
            item.getTags().add("目标 " + item.getTargetWarningCount());
        }
        if (item.getCareTaskCount() > 0) {
            item.getTags().add("照护 " + item.getCareTaskCount());
        }
        if (item.getDailyTrendCount() > 0) {
            item.getTags().add("趋势 " + item.getDailyTrendCount());
        }
        if (item.getTags().isEmpty()) {
            item.getTags().add("平稳观察");
        }
        item.setSummary(StrUtil.format("{}当前有{}条下一步建议、{}条复查建议、{}个目标趋势关注点、{}条照护任务。",
            StrUtil.blankToDefault(item.getMemberName(), "家庭成员"), item.getNextActionCount(),
            item.getReviewSuggestionCount(), item.getTargetWarningCount(), item.getCareTaskCount()));
    }

    /**
     * 在多个来源给出风险等级时取更高风险。
     *
     * <p>复用照护任务已有的等级权重方法，避免在成员关注摘要里再维护一套 HIGH/MEDIUM/LOW 顺序。
     */
    private String resolveHigherRiskLevel(String left, String right) {
        return resolveCareTaskPriority(right) > resolveCareTaskPriority(left) ? right : left;
    }

    private HealthLongTermManagementDTO.PrivacyExportSummaryDTO buildPrivacyExportSummary(Set<Long> memberIds,
        List<HealthFamilyMemberEntity> members, List<HealthChronicDiseaseProfileEntity> profiles,
        List<HealthReportEntity> reports, List<HealthReportItemEntity> reportItems,
        List<HealthMedicationPlanEntity> activePlans, List<HealthProblemEntity> healthProblems,
        List<HealthOperationTaskEntity> careActionTasks) {
        HealthLongTermManagementDTO.PrivacyExportSummaryDTO dto = new HealthLongTermManagementDTO.PrivacyExportSummaryDTO();
        int diaryCount = countDiaryEntries(memberIds);
        dto.getCategories().add(buildPrivacyCategory("FAMILY_MEMBER", "家庭成员", "成员基础资料、关系和健康基础信息。",
            members.size()));
        dto.getCategories().add(buildPrivacyCategory("CHRONIC_PROFILE", "慢病专项", "慢病档案、目标摘要和复查状态。",
            profiles.size()));
        dto.getCategories().add(buildPrivacyCategory("HEALTH_REPORT", "体检报告", "报告主表和已解析指标。",
            reports.size() + reportItems.size()));
        dto.getCategories().add(buildPrivacyCategory("MEDICATION_PLAN", "用药计划", "当前启用的用药计划和提醒来源。",
            activePlans.size()));
        dto.getCategories().add(buildPrivacyCategory("DIARY_ENTRY", "健康记录", "慢病日记、日常指标和语音文本记录。",
            diaryCount));
        dto.getCategories().add(buildPrivacyCategory("HEALTH_PROBLEM", "健康问题", "报告解析沉淀出的待关注问题和处理状态。",
            healthProblems.size()));
        dto.getCategories().add(buildPrivacyCategory("CARE_TASK", "照护任务", "家庭照护协作任务和长期管理事项。",
            careActionTasks.size()));
        dto.getTips().add("导出前建议先确认当前设备安全，避免资料被其他应用读取。 ");
        dto.getTips().add("分享给医生或家属前，可先检查是否包含不需要展示的成员资料。 ");
        dto.getTips().add("删除成员、报告或日记前，请先确认后续就医是否仍需要这些记录。 ");
        dto.setSummary(StrUtil.format("当前长期管理摘要覆盖{}类资料，共{}条可归档数据。", dto.getCategories().size(),
            dto.getCategories().stream().mapToInt(HealthLongTermManagementDTO.PrivacyCategoryDTO::getItemCount).sum()));
        return dto;
    }

    private HealthLongTermManagementDTO.PrivacyCategoryDTO buildPrivacyCategory(String code, String name,
        String description, int count) {
        HealthLongTermManagementDTO.PrivacyCategoryDTO dto = new HealthLongTermManagementDTO.PrivacyCategoryDTO();
        dto.setCategoryCode(code);
        dto.setCategoryName(name);
        dto.setDescription(description);
        dto.setItemCount(count);
        return dto;
    }

    private String buildProblemLifecycleSummary(List<HealthProblemEntity> healthProblems) {
        long followingCount = healthProblems.stream().filter(item -> Objects.equals(item.getProblemStatus(), 1)).count();
        long highRiskCount = healthProblems.stream().filter(item -> Objects.equals(item.getRiskLevel(), 3)).count();
        return StrUtil.format("当前{}个健康问题处于跟进中，其中{}个为高风险。", followingCount, highRiskCount);
    }

    /**
     * 读取日常指标关联的慢病专项。
     *
     * <p>这里不只按 profileId 读取，还要求专项属于本次记录的 memberId，并排除已关闭档案。
     * 这样 App 端切换成员后即使出现旧档案 ID，也不会把指标写到其他成员或已关闭档案下。
     */
    private HealthChronicDiseaseProfileEntity loadDailyIndicatorProfile(Long memberId, Long profileId) {
        if (profileId == null) {
            return null;
        }
        HealthChronicDiseaseProfileEntity profile = chronicDiseaseProfileService.getById(profileId);
        if (profile == null || !Objects.equals(profile.getMemberId(), memberId)
            || Objects.equals(profile.getProfileStatus(), 3)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, profileId, "慢病专项");
        }
        return profile;
    }

    private String resolveDailyIndicatorDiseaseCode(String requestDiseaseCode,
        HealthChronicDiseaseProfileEntity profile) {
        if (profile != null) {
            return profile.getDiseaseCode();
        }
        return requestDiseaseCode;
    }

    private String normalizeDailyIndicatorType(String indicatorType) {
        String value = StrUtil.blankToDefault(indicatorType, "GENERAL").trim().toUpperCase(Locale.ROOT);
        if ("BP".equals(value) || "BLOODPRESSURE".equals(value)) {
            return "BLOOD_PRESSURE";
        }
        if ("GLUCOSE".equals(value) || "BLOODSUGAR".equals(value) || "BLOOD_SUGAR".equals(value)) {
            return "BLOOD_GLUCOSE";
        }
        if (isSupportedDailyIndicatorType(value)) {
            return value;
        }
        return "GENERAL";
    }

    private boolean isSupportedDailyIndicatorType(String entryType) {
        return Objects.equals(entryType, "BLOOD_PRESSURE") || Objects.equals(entryType, "BLOOD_GLUCOSE")
            || Objects.equals(entryType, "WEIGHT") || Objects.equals(entryType, "HEART_RATE")
            || Objects.equals(entryType, "SPO2") || Objects.equals(entryType, "TEMPERATURE")
            || Objects.equals(entryType, "SYMPTOM") || Objects.equals(entryType, "SLEEP")
            || Objects.equals(entryType, "EXERCISE") || Objects.equals(entryType, "GENERAL");
    }

    private String resolveDailyIndicatorName(String entryType) {
        if (Objects.equals(entryType, "BLOOD_PRESSURE")) {
            return "血压";
        }
        if (Objects.equals(entryType, "BLOOD_GLUCOSE")) {
            return "血糖";
        }
        if (Objects.equals(entryType, "WEIGHT")) {
            return "体重";
        }
        if (Objects.equals(entryType, "HEART_RATE")) {
            return "心率";
        }
        if (Objects.equals(entryType, "SPO2")) {
            return "血氧";
        }
        if (Objects.equals(entryType, "TEMPERATURE")) {
            return "体温";
        }
        if (Objects.equals(entryType, "SYMPTOM")) {
            return "症状";
        }
        if (Objects.equals(entryType, "SLEEP")) {
            return "睡眠";
        }
        if (Objects.equals(entryType, "EXERCISE")) {
            return "运动";
        }
        return "健康";
    }

    private String resolveDailyIndicatorTitle(SaveDailyIndicatorRecordCommand command, String entryType) {
        return StrUtil.blankToDefault(StrUtil.trim(command.getEntryTitle()), resolveDailyIndicatorName(entryType) + "记录");
    }

    private String buildDailyIndicatorContent(SaveDailyIndicatorRecordCommand command, String entryType) {
        List<String> parts = new ArrayList<>();
        String valueText = buildDailyIndicatorValueText(command);
        if (StrUtil.isNotBlank(valueText)) {
            parts.add(resolveDailyIndicatorName(entryType) + "：" + valueText);
        }
        if (StrUtil.isNotBlank(command.getMeasureScene())) {
            parts.add("场景：" + StrUtil.trim(command.getMeasureScene()));
        }
        if (StrUtil.isNotBlank(command.getNote())) {
            parts.add(StrUtil.trim(command.getNote()));
        }
        if (parts.isEmpty()) {
            parts.add("记录了一条" + resolveDailyIndicatorName(entryType) + "数据。");
        }
        return String.join("；", parts);
    }

    private String buildDailyIndicatorValueText(SaveDailyIndicatorRecordCommand command) {
        String unit = StrUtil.nullToEmpty(command.getMetricUnit()).trim();
        if (command.getPrimaryValue() == null && command.getSecondaryValue() == null) {
            return null;
        }
        if (command.getPrimaryValue() != null && command.getSecondaryValue() != null) {
            return command.getPrimaryValue().stripTrailingZeros().toPlainString() + "/"
                + command.getSecondaryValue().stripTrailingZeros().toPlainString() + unit;
        }
        BigDecimal value = command.getPrimaryValue() == null ? command.getSecondaryValue() : command.getPrimaryValue();
        return value.stripTrailingZeros().toPlainString() + unit;
    }

    private String buildDailyIndicatorMetricPayload(SaveDailyIndicatorRecordCommand command, String entryType) {
        if (StrUtil.isNotBlank(command.getMetricPayloadJson())) {
            return StrUtil.trim(command.getMetricPayloadJson());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("indicatorType", entryType);
        if (command.getPrimaryValue() != null) {
            payload.put("primaryValue", command.getPrimaryValue());
        }
        if (command.getSecondaryValue() != null) {
            payload.put("secondaryValue", command.getSecondaryValue());
        }
        if (StrUtil.isNotBlank(command.getMetricUnit())) {
            payload.put("metricUnit", StrUtil.trim(command.getMetricUnit()));
        }
        if (StrUtil.isNotBlank(command.getMeasureScene())) {
            payload.put("measureScene", StrUtil.trim(command.getMeasureScene()));
        }
        return JSONUtil.toJsonStr(payload);
    }

    private int resolveCareTaskPriority(String riskLevel) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return 90;
        }
        if ("LOW".equalsIgnoreCase(riskLevel)) {
            return 30;
        }
        return 60;
    }

    private int countEnabledChronicDiseaseTypes() {
        return safeIntCount(chronicDiseaseTypeService.lambdaQuery()
            .eq(HealthChronicDiseaseTypeEntity::getStatus, StatusEnum.ENABLE.getValue())
            .count());
    }

    private int countTodayDailyIndicatorEntries(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return 0;
        }
        Date begin = DateUtil.beginOfDay(new Date());
        Date end = DateUtil.offsetDay(begin, 1);
        return safeIntCount(chronicDiaryEntryService.lambdaQuery()
            .in(HealthChronicDiaryEntryEntity::getMemberId, memberIds)
            .eq(HealthChronicDiaryEntryEntity::getStatus, StatusEnum.ENABLE.getValue())
            .in(HealthChronicDiaryEntryEntity::getEntryType, buildDailyIndicatorEntryTypes())
            .ge(HealthChronicDiaryEntryEntity::getRecordTime, begin)
            .lt(HealthChronicDiaryEntryEntity::getRecordTime, end)
            .count());
    }

    private int countDiaryEntries(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return 0;
        }
        return safeIntCount(chronicDiaryEntryService.lambdaQuery()
            .in(HealthChronicDiaryEntryEntity::getMemberId, memberIds)
            .eq(HealthChronicDiaryEntryEntity::getStatus, StatusEnum.ENABLE.getValue())
            .count());
    }

    private int safeIntCount(long count) {
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }

    private String resolveMemberNameFromMap(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        HealthFamilyMemberEntity member = memberId == null ? null : memberMap.get(memberId);
        return member == null ? "家庭成员" : member.getMemberName();
    }
    private HealthInsightWorkbenchDTO buildEmptyWorkbench() {
        HealthInsightWorkbenchDTO dto = new HealthInsightWorkbenchDTO();
        dto.getCareDashboard().setSummary("当前暂无可访问的家庭成员，请先添加成员或接受家庭共享邀请。");
        dto.getMedicalVisitPackage().setSummary("暂无成员数据，暂不能生成就医资料包。");
        dto.getVoiceEntry().setSupported(true);
        dto.getVoiceEntry().setFlow("录音 -> ASR -> 慢病日记 -> 健康时间线");
        dto.getVoiceEntry().setSummary("可先添加家庭成员，再使用语音记录健康事件。");
        dto.getLongTermManagement().setSummary("暂无可访问成员，长期管理能力会在添加成员后展示。");
        dto.getLongTermManagement().getDailyIndicatorBoard().setSupportedTypes(buildSupportedDailyIndicatorTypes());
        dto.getLongTermManagement().getDailyIndicatorBoard().setSummary("可先添加家庭成员，再记录血压、血糖、体重等日常指标。");
        dto.getLongTermManagement().getPrivacyExport().setSummary("暂无成员数据，暂不需要导出或清理。");
        return dto;
    }

    private List<HealthFamilyMemberEntity> listAccessibleMembers(Long currentUserId) {
        Set<Long> memberIds = familyMemberAccessService.getAccessibleMemberIds(currentUserId);
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return familyMemberService.listByIds(memberIds).stream()
            .filter(member -> Objects.equals(member.getStatus(), StatusEnum.ENABLE.getValue()))
            .sorted(Comparator.comparing(HealthFamilyMemberEntity::getMemberId, Comparator.nullsLast(Long::compareTo)))
            .collect(Collectors.toList());
    }

    private List<HealthChronicDiseaseProfileEntity> listActiveProfiles(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chronicDiseaseProfileService.lambdaQuery()
            .in(HealthChronicDiseaseProfileEntity::getMemberId, memberIds)
            .ne(HealthChronicDiseaseProfileEntity::getProfileStatus, 3)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getRiskLevel)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getProfileId)
            .list();
    }

    private List<HealthOperationTaskEntity> listActiveReviewTasks(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        Date now = new Date();
        return operationTaskService.lambdaQuery()
            .in(HealthOperationTaskEntity::getMemberId, memberIds)
            .eq(HealthOperationTaskEntity::getTargetBizType,
                ChronicDiseaseApplicationService.TARGET_BIZ_TYPE_CHRONIC_REVIEW)
            .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue())
            .and(wrapper -> wrapper.isNull(HealthOperationTaskEntity::getEndTime)
                .or()
                .ge(HealthOperationTaskEntity::getEndTime, now))
            .orderByAsc(HealthOperationTaskEntity::getStartTime)
            .list();
    }
    private List<HealthReportEntity> listRecentReports(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reportService.lambdaQuery()
            .in(HealthReportEntity::getMemberId, memberIds)
            .orderByDesc(HealthReportEntity::getReportDate)
            .orderByDesc(HealthReportEntity::getReportId)
            .page(new Page<>(1, RECENT_REPORT_LIMIT))
            .getRecords();
    }

    private List<HealthReportItemEntity> listReportItems(Set<Long> reportIds) {
        if (reportIds == null || reportIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reportItemService.lambdaQuery()
            .in(HealthReportItemEntity::getReportId, reportIds)
            .orderByAsc(HealthReportItemEntity::getReportId)
            .orderByAsc(HealthReportItemEntity::getSort)
            .list();
    }

    private List<HealthMedicationPlanEntity> listActiveMedicationPlans(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return medicationPlanService.lambdaQuery()
            .in(HealthMedicationPlanEntity::getMemberId, memberIds)
            .eq(HealthMedicationPlanEntity::getStatus, StatusEnum.ENABLE.getValue())
            .list();
    }

    private List<HealthMedicationReminderEntity> listPendingReminders(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return medicationReminderService.lambdaQuery()
            .in(HealthMedicationReminderEntity::getMemberId, memberIds)
            .eq(HealthMedicationReminderEntity::getReminderStatus, MedicationReminderStatusEnum.PENDING.getValue())
            .orderByAsc(HealthMedicationReminderEntity::getScheduledTime)
            .page(new Page<>(1, 50))
            .getRecords();
    }

    private List<HealthChronicDiaryEntryEntity> listLatestDiaryEntries(Set<Long> memberIds, int limit) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chronicDiaryEntryService.lambdaQuery()
            .in(HealthChronicDiaryEntryEntity::getMemberId, memberIds)
            .eq(HealthChronicDiaryEntryEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByDesc(HealthChronicDiaryEntryEntity::getRecordTime)
            .orderByDesc(HealthChronicDiaryEntryEntity::getDiaryEntryId)
            .page(new Page<>(1, Math.max(1, limit)))
            .getRecords();
    }

    private List<HealthChronicDiaryEntryEntity> listRecentDailyIndicatorEntries(Set<Long> memberIds, int limit) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chronicDiaryEntryService.lambdaQuery()
            .in(HealthChronicDiaryEntryEntity::getMemberId, memberIds)
            .eq(HealthChronicDiaryEntryEntity::getStatus, StatusEnum.ENABLE.getValue())
            .in(HealthChronicDiaryEntryEntity::getEntryType, buildDailyIndicatorEntryTypes())
            .orderByDesc(HealthChronicDiaryEntryEntity::getRecordTime)
            .orderByDesc(HealthChronicDiaryEntryEntity::getDiaryEntryId)
            .page(new Page<>(1, Math.max(1, limit)))
            .getRecords();
    }

    private List<String> buildDailyIndicatorEntryTypes() {
        List<String> values = new ArrayList<>();
        values.add("BLOOD_PRESSURE");
        values.add("BLOOD_GLUCOSE");
        values.add("WEIGHT");
        values.add("HEART_RATE");
        values.add("SPO2");
        values.add("TEMPERATURE");
        values.add("SYMPTOM");
        values.add("SLEEP");
        values.add("EXERCISE");
        values.add("GENERAL");
        return values;
    }

    private List<HealthProblemEntity> listHealthProblems(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return healthProblemService.lambdaQuery()
            .in(HealthProblemEntity::getMemberId, memberIds)
            .orderByDesc(HealthProblemEntity::getRiskLevel)
            .orderByAsc(HealthProblemEntity::getProblemStatus)
            .orderByDesc(HealthProblemEntity::getLastFollowDate)
            .orderByDesc(HealthProblemEntity::getProblemId)
            .list();
    }

    private List<HealthOperationTaskEntity> listActiveCareActionTasks(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        Date now = new Date();
        return operationTaskService.lambdaQuery()
            .in(HealthOperationTaskEntity::getMemberId, memberIds)
            .eq(HealthOperationTaskEntity::getTargetBizType, TARGET_BIZ_TYPE_CARE_ACTION)
            .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue())
            .and(wrapper -> wrapper.isNull(HealthOperationTaskEntity::getEndTime)
                .or()
                .ge(HealthOperationTaskEntity::getEndTime, now))
            .orderByAsc(HealthOperationTaskEntity::getStartTime)
            .orderByDesc(HealthOperationTaskEntity::getPriorityWeight)
            .page(new Page<>(1, 20))
            .getRecords();
    }
    private List<HealthProblemStatusLogEntity> listRecentProblemStatusLogs(Set<Long> memberIds, int limit) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return healthProblemStatusLogService.lambdaQuery()
            .in(HealthProblemStatusLogEntity::getMemberId, memberIds)
            .orderByDesc(HealthProblemStatusLogEntity::getActionTime)
            .orderByDesc(HealthProblemStatusLogEntity::getLogId)
            .page(new Page<>(1, Math.max(1, limit)))
            .getRecords();
    }
    private HealthInsightWorkbenchDTO.ReviewLoopSummaryDTO buildReviewLoop(
        List<HealthChronicDiseaseProfileEntity> profiles, List<HealthOperationTaskEntity> reviewTasks) {
        HealthInsightWorkbenchDTO.ReviewLoopSummaryDTO dto = new HealthInsightWorkbenchDTO.ReviewLoopSummaryDTO();
        Date now = new Date();
        dto.setActiveProfileCount(profiles.size());
        dto.setPendingReviewTaskCount(reviewTasks.size());
        dto.setDueReviewTaskCount((int) reviewTasks.stream()
            .filter(task -> task.getStartTime() != null && !task.getStartTime().after(now))
            .count());
        reviewTasks.stream()
            .min(Comparator.comparing(HealthOperationTaskEntity::getStartTime, Comparator.nullsLast(Date::compareTo)))
            .ifPresent(task -> {
                dto.setNextReviewTaskId(task.getOperationTaskId());
                dto.setNextReviewProfileId(task.getTargetBizId());
                dto.setNextReviewTime(task.getStartTime());
            });
        dto.setSummary(StrUtil.format("当前有{}个慢病专项，{}条复查任务，其中{}条已经到期需要处理。",
            dto.getActiveProfileCount(), dto.getPendingReviewTaskCount(), dto.getDueReviewTaskCount()));
        return dto;
    }

    private List<HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO> buildIndicatorAlerts(
        List<HealthReportItemEntity> reportItems, Map<Long, HealthReportEntity> reportMap,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        Map<String, List<HealthReportItemEntity>> grouped = new LinkedHashMap<>();
        for (HealthReportItemEntity item : reportItems) {
            HealthReportEntity report = reportMap.get(item.getReportId());
            Long memberId = report == null ? null : report.getMemberId();
            String indicatorKey = resolveIndicatorGroupKey(item);
            if (memberId == null || StrUtil.isBlank(indicatorKey)) {
                continue;
            }
            grouped.computeIfAbsent(memberId + "::" + indicatorKey, key -> new ArrayList<>()).add(item);
        }

        return grouped.values().stream()
            .map(items -> buildIndicatorAlert(items, reportMap, memberMap))
            .filter(Objects::nonNull)
            .sorted(this::compareIndicatorAlert)
            .limit(8)
            .collect(Collectors.toList());
    }

    private HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO buildIndicatorAlert(List<HealthReportItemEntity> items,
        Map<Long, HealthReportEntity> reportMap, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        items.sort((left, right) -> compareReportItemByReportTimeDesc(left, right, reportMap));
        HealthReportItemEntity latestItem = items.get(0);
        HealthReportItemEntity previousItem = items.size() > 1 ? items.get(1) : null;
        int abnormalCount = (int) items.stream().filter(this::isAbnormalItem).count();
        int consecutiveAbnormalCount = countConsecutiveAbnormalItems(items);
        BigDecimal latestNumber = parseReportItemNumber(latestItem.getResultValue());
        BigDecimal previousNumber = previousItem == null ? null : parseReportItemNumber(previousItem.getResultValue());
        String changeDirection = resolveIndicatorChangeDirection(latestNumber, previousNumber);
        BigDecimal changePercent = resolveIndicatorChangePercent(latestNumber, previousNumber);
        boolean volatilityAlert = changePercent != null
            && changePercent.compareTo(INDICATOR_VOLATILITY_PERCENT_THRESHOLD) >= 0;

        String ruleType = resolveIndicatorAlertRuleType(latestItem, consecutiveAbnormalCount, volatilityAlert);
        if (StrUtil.isBlank(ruleType)) {
            return null;
        }

        HealthReportEntity latestReport = reportMap.get(latestItem.getReportId());
        HealthReportEntity previousReport = previousItem == null ? null : reportMap.get(previousItem.getReportId());
        Long memberId = latestReport == null ? null : latestReport.getMemberId();
        String memberName = memberId == null || memberMap.get(memberId) == null
            ? "家庭成员"
            : memberMap.get(memberId).getMemberName();

        HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO dto =
            new HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO();
        dto.setMemberId(memberId);
        dto.setMemberName(memberName);
        dto.setReportId(latestItem.getReportId());
        dto.setLatestReportDate(latestReport == null ? null : latestReport.getReportDate());
        dto.setPreviousReportId(previousItem == null ? null : previousItem.getReportId());
        dto.setPreviousReportDate(previousReport == null ? null : previousReport.getReportDate());
        dto.setIndicatorCode(resolveIndicatorDisplayCode(latestItem));
        dto.setIndicatorName(latestItem.getItemName());
        dto.setAbnormalFlagName(resolveAbnormalFlagName(latestItem.getAbnormalFlag()));
        dto.setAbnormalCount(abnormalCount);
        dto.setConsecutiveAbnormalCount(consecutiveAbnormalCount);
        dto.setLatestResultValue(latestItem.getResultValue());
        dto.setLatestResultUnit(latestItem.getResultUnit());
        dto.setPreviousResultValue(previousItem == null ? null : previousItem.getResultValue());
        dto.setPreviousResultUnit(previousItem == null ? null : previousItem.getResultUnit());
        dto.setChangeDirection(changeDirection);
        dto.setChangeDirectionName(resolveIndicatorChangeDirectionName(changeDirection));
        dto.setChangePercent(changePercent);
        dto.setChangeSummary(buildIndicatorChangeSummary(latestItem, previousItem, changeDirection, changePercent));
        dto.setRuleType(ruleType);
        dto.setRuleTypeName(resolveIndicatorRuleTypeName(ruleType));
        dto.setRiskLevel(resolveIndicatorAlertRiskLevel(ruleType, abnormalCount, consecutiveAbnormalCount));
        dto.setAlertTitle(StrUtil.format("{}的{}需要关注", memberName,
            StrUtil.blankToDefault(latestItem.getItemName(), "体检指标")));
        dto.setAlertReasons(buildIndicatorAlertReasons(dto));
        dto.setAlertContent(buildIndicatorAlertContent(dto));
        return dto;
    }

    private String resolveIndicatorGroupKey(HealthReportItemEntity item) {
        return StrUtil.blankToDefault(resolveIndicatorDisplayCode(item), item == null ? null : item.getItemName())
            .trim()
            .toUpperCase(Locale.ROOT);
    }

    private String resolveIndicatorDisplayCode(HealthReportItemEntity item) {
        if (item == null) {
            return null;
        }
        return StrUtil.blankToDefault(item.getStandardItemCode(),
            StrUtil.blankToDefault(item.getItemCode(), item.getItemName()));
    }

    private int compareReportItemByReportTimeDesc(HealthReportItemEntity left, HealthReportItemEntity right,
        Map<Long, HealthReportEntity> reportMap) {
        Date leftDate = resolveReportDate(left, reportMap);
        Date rightDate = resolveReportDate(right, reportMap);
        int dateCompare = Comparator.nullsLast(Date::compareTo).compare(rightDate, leftDate);
        if (dateCompare != 0) {
            return dateCompare;
        }
        return Long.compare(nullToZero(right == null ? null : right.getReportId()),
            nullToZero(left == null ? null : left.getReportId()));
    }

    private Date resolveReportDate(HealthReportItemEntity item, Map<Long, HealthReportEntity> reportMap) {
        if (item == null || reportMap == null) {
            return null;
        }
        HealthReportEntity report = reportMap.get(item.getReportId());
        return report == null ? null : report.getReportDate();
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private int countConsecutiveAbnormalItems(List<HealthReportItemEntity> sortedItems) {
        int count = 0;
        for (HealthReportItemEntity item : sortedItems) {
            if (!isAbnormalItem(item)) {
                break;
            }
            count++;
        }
        return count;
    }

    private String resolveIndicatorAlertRuleType(HealthReportItemEntity latestItem, int consecutiveAbnormalCount,
        boolean volatilityAlert) {
        if (consecutiveAbnormalCount >= 2) {
            return "CONTINUOUS_ABNORMAL";
        }
        if (isAbnormalItem(latestItem)) {
            return "LATEST_ABNORMAL";
        }
        return volatilityAlert ? "VOLATILITY" : null;
    }

    private String resolveIndicatorRuleTypeName(String ruleType) {
        if (Objects.equals(ruleType, "CONTINUOUS_ABNORMAL")) {
            return "连续异常";
        }
        if (Objects.equals(ruleType, "LATEST_ABNORMAL")) {
            return "最新异常";
        }
        if (Objects.equals(ruleType, "VOLATILITY")) {
            return "明显波动";
        }
        return "趋势提醒";
    }

    private String resolveIndicatorAlertRiskLevel(String ruleType, int abnormalCount, int consecutiveAbnormalCount) {
        if (Objects.equals(ruleType, "CONTINUOUS_ABNORMAL") || consecutiveAbnormalCount >= 2 || abnormalCount >= 3) {
            return HealthFollowUpRiskLevelEnum.HIGH.getValue();
        }
        return HealthFollowUpRiskLevelEnum.MEDIUM.getValue();
    }

    private BigDecimal parseReportItemNumber(String resultValue) {
        if (StrUtil.isBlank(resultValue)) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(resultValue);
        if (!matcher.find()) {
            return null;
        }
        try {
            return new BigDecimal(matcher.group());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String resolveIndicatorChangeDirection(BigDecimal latestNumber, BigDecimal previousNumber) {
        if (latestNumber == null || previousNumber == null) {
            return previousNumber == null ? "NO_BASELINE" : "UNMEASURABLE";
        }
        int compare = latestNumber.compareTo(previousNumber);
        if (compare == 0) {
            return "UNCHANGED";
        }
        return compare > 0 ? "UP" : "DOWN";
    }

    private String resolveIndicatorChangeDirectionName(String changeDirection) {
        if (Objects.equals(changeDirection, "UP")) {
            return "上升";
        }
        if (Objects.equals(changeDirection, "DOWN")) {
            return "下降";
        }
        if (Objects.equals(changeDirection, "UNCHANGED")) {
            return "基本持平";
        }
        if (Objects.equals(changeDirection, "UNMEASURABLE")) {
            return "暂无法量化";
        }
        return "暂无基线";
    }

    private BigDecimal resolveIndicatorChangePercent(BigDecimal latestNumber, BigDecimal previousNumber) {
        if (latestNumber == null || previousNumber == null || BigDecimal.ZERO.compareTo(previousNumber) == 0) {
            return null;
        }
        return latestNumber.subtract(previousNumber)
            .abs()
            .multiply(BigDecimal.valueOf(100))
            .divide(previousNumber.abs(), 1, RoundingMode.HALF_UP);
    }

    private String buildIndicatorChangeSummary(HealthReportItemEntity latestItem, HealthReportItemEntity previousItem,
        String changeDirection, BigDecimal changePercent) {
        if (previousItem == null) {
            return "当前仅有一条可比报告数据，建议继续补充后续检查形成趋势。";
        }
        if (Objects.equals(changeDirection, "UNMEASURABLE") || Objects.equals(changeDirection, "NO_BASELINE")) {
            return "最近两次结果暂无法量化比较，建议查看原始报告和参考范围。";
        }
        String percentText = changePercent == null ? "" : StrUtil.format("，波动约{}%",
            changePercent.stripTrailingZeros().toPlainString());
        return StrUtil.format("相较上次 {}{}，本次 {}{}{}，趋势为{}。",
            StrUtil.blankToDefault(previousItem.getResultValue(), "-"),
            StrUtil.blankToDefault(previousItem.getResultUnit(), ""),
            StrUtil.blankToDefault(latestItem.getResultValue(), "-"),
            StrUtil.blankToDefault(latestItem.getResultUnit(), ""),
            percentText, resolveIndicatorChangeDirectionName(changeDirection));
    }

    private List<String> buildIndicatorAlertReasons(HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO dto) {
        List<String> reasons = new ArrayList<>();
        reasons.add(dto.getRuleTypeName());
        if (dto.getConsecutiveAbnormalCount() >= 2) {
            reasons.add(StrUtil.format("连续{}次异常", dto.getConsecutiveAbnormalCount()));
        } else if (dto.getAbnormalCount() > 0) {
            reasons.add(StrUtil.format("近期{}次异常", dto.getAbnormalCount()));
        }
        if (dto.getChangePercent() != null
            && dto.getChangePercent().compareTo(INDICATOR_VOLATILITY_PERCENT_THRESHOLD) >= 0) {
            reasons.add(StrUtil.format("波动{}%", dto.getChangePercent().stripTrailingZeros().toPlainString()));
        }
        return reasons;
    }

    private String buildIndicatorAlertContent(HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO dto) {
        String latestValue = StrUtil.format("{}{}", StrUtil.blankToDefault(dto.getLatestResultValue(), "-"),
            StrUtil.blankToDefault(dto.getLatestResultUnit(), ""));
        if (Objects.equals(dto.getRuleType(), "CONTINUOUS_ABNORMAL")) {
            return StrUtil.format("最近该指标已连续{}次异常，最新结果为{}。{}",
                dto.getConsecutiveAbnormalCount(), latestValue, dto.getChangeSummary());
        }
        if (Objects.equals(dto.getRuleType(), "LATEST_ABNORMAL")) {
            return StrUtil.format("最新报告中该指标为{}，结果为{}。{}",
                StrUtil.blankToDefault(dto.getAbnormalFlagName(), "异常"), latestValue, dto.getChangeSummary());
        }
        return StrUtil.format("最近两次该指标出现明显波动，最新结果为{}。{}", latestValue, dto.getChangeSummary());
    }

    private int compareIndicatorAlert(HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO left,
        HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO right) {
        int priorityCompare = Integer.compare(indicatorAlertPriority(right), indicatorAlertPriority(left));
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        int abnormalCompare = Integer.compare(right.getAbnormalCount(), left.getAbnormalCount());
        if (abnormalCompare != 0) {
            return abnormalCompare;
        }
        return Comparator.nullsLast(Date::compareTo).compare(right.getLatestReportDate(), left.getLatestReportDate());
    }

    private int indicatorAlertPriority(HealthInsightWorkbenchDTO.IndicatorTrendAlertDTO dto) {
        if (Objects.equals(dto.getRuleType(), "CONTINUOUS_ABNORMAL")) {
            return 30;
        }
        if (Objects.equals(dto.getRuleType(), "LATEST_ABNORMAL")) {
            return 20;
        }
        return 10;
    }

    private HealthInsightWorkbenchDTO.MedicalVisitPackageDTO buildMedicalVisitPackage(
        List<HealthFamilyMemberEntity> members, List<HealthChronicDiseaseProfileEntity> profiles,
        List<HealthReportEntity> reports, List<HealthMedicationPlanEntity> activePlans,
        List<HealthReportItemEntity> reportItems) {
        HealthInsightWorkbenchDTO.MedicalVisitPackageDTO dto =
            new HealthInsightWorkbenchDTO.MedicalVisitPackageDTO();
        dto.setPackageTitle("就医资料包");
        dto.setMemberCount(members.size());
        dto.setChronicProfileCount(profiles.size());
        dto.setRecentReportCount(reports.size());
        dto.setActiveMedicationPlanCount(activePlans.size());
        dto.setAbnormalIndicatorCount((int) reportItems.stream().filter(this::isAbnormalItem).count());
        dto.getSections().add("家庭成员基础信息");
        dto.getSections().add("慢病专项档案");
        dto.getSections().add("最近体检报告与异常指标");
        dto.getSections().add("当前启用中的用药计划");
        dto.setSummary(StrUtil.format("可整理{}位成员、{}个慢病专项、{}份近期报告和{}条用药计划，适合复诊前快速核对。",
            dto.getMemberCount(), dto.getChronicProfileCount(), dto.getRecentReportCount(),
            dto.getActiveMedicationPlanCount()));
        return dto;
    }

    private HealthInsightWorkbenchDTO.ChronicDiarySummaryDTO buildChronicDiarySummary(Set<Long> memberIds,
        List<HealthChronicDiaryEntryEntity> latestDiaryEntries, Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthInsightWorkbenchDTO.ChronicDiarySummaryDTO dto = new HealthInsightWorkbenchDTO.ChronicDiarySummaryDTO();
        Long count = chronicDiaryEntryService.lambdaQuery()
            .in(HealthChronicDiaryEntryEntity::getMemberId, memberIds)
            .eq(HealthChronicDiaryEntryEntity::getStatus, StatusEnum.ENABLE.getValue())
            .count();
        dto.setDiaryCount(count == null ? 0 : count.intValue());
        if (!latestDiaryEntries.isEmpty()) {
            HealthChronicDiaryEntryEntity latest = latestDiaryEntries.get(0);
            dto.setLatestRecordTime(latest.getRecordTime());
            dto.setLatestTitle(latest.getEntryTitle());
        }
        dto.setLatestEntries(latestDiaryEntries.stream()
            .map(entry -> new ChronicDiaryEntryDTO(entry, memberMap.get(entry.getMemberId()) == null
                ? null
                : memberMap.get(entry.getMemberId()).getMemberName()))
            .collect(Collectors.toList()));
        dto.setSummary(dto.getDiaryCount() == 0
            ? "暂未记录慢病日记，可先记录症状、血压、血糖、饮食、运动或睡眠。"
            : StrUtil.format("已累计记录{}条慢病日记，最近一次记录可进入健康时间线查看。", dto.getDiaryCount()));
        return dto;
    }
    private HealthInsightWorkbenchDTO.CareDashboardDTO buildCareDashboard(List<HealthFamilyMemberEntity> members,
        List<HealthChronicDiseaseProfileEntity> profiles, List<HealthReportEntity> reports,
        List<HealthReportItemEntity> reportItems, List<HealthMedicationReminderEntity> pendingReminders,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthInsightWorkbenchDTO.CareDashboardDTO dto = new HealthInsightWorkbenchDTO.CareDashboardDTO();
        dto.setMemberCount(members.size());
        dto.setPendingReminderCount(pendingReminders.size());
        Set<Long> abnormalReportIds = reportItems.stream()
            .filter(this::isAbnormalItem)
            .map(HealthReportItemEntity::getReportId)
            .collect(Collectors.toSet());
        dto.setAbnormalReportCount((int) reports.stream()
            .filter(report -> abnormalReportIds.contains(report.getReportId()))
            .count());
        dto.setHighRiskChronicProfileCount((int) profiles.stream()
            .filter(profile -> Objects.equals(profile.getRiskLevel(), HealthFollowUpRiskLevelEnum.HIGH.getValue()))
            .count());
        resolveFocusMember(profiles, pendingReminders, members).ifPresent(memberId -> {
            dto.setFocusMemberId(memberId);
            dto.setFocusMemberName(memberMap.get(memberId) == null ? null : memberMap.get(memberId).getMemberName());
        });
        dto.setSummary(StrUtil.format("当前照护{}位成员，{}条提醒待处理，{}份近期报告存在异常，{}个慢病专项高优先关注。",
            dto.getMemberCount(), dto.getPendingReminderCount(), dto.getAbnormalReportCount(),
            dto.getHighRiskChronicProfileCount()));
        return dto;
    }

    private HealthInsightWorkbenchDTO.MedicationSafetyDTO buildMedicationSafety(Long currentUserId,
        List<HealthMedicationPlanEntity> activePlans) {
        HealthInsightWorkbenchDTO.MedicationSafetyDTO dto = new HealthInsightWorkbenchDTO.MedicationSafetyDTO();
        dto.setActiveMedicationPlanCount(activePlans.size());
        dto.setDuplicateMedicationCount(countDuplicateMedications(activePlans));
        dto.setLowStockDrugCount(countLowStockDrugs(currentUserId));
        dto.setNearExpireBatchCount(countNearExpireBatches(currentUserId));
        if (dto.getDuplicateMedicationCount() > 0) {
            dto.getWarnings().add(StrUtil.format("存在{}组成员内重复用药计划，请核对是否重复录入。",
                dto.getDuplicateMedicationCount()));
        }
        if (dto.getLowStockDrugCount() > 0) {
            dto.getWarnings().add(StrUtil.format("有{}种药品库存低于预警值。", dto.getLowStockDrugCount()));
        }
        if (dto.getNearExpireBatchCount() > 0) {
            dto.getWarnings().add(StrUtil.format("有{}个库存批次将在{}天内到期。", dto.getNearExpireBatchCount(),
                NEAR_EXPIRE_DAYS));
        }
        dto.setSummary(dto.getWarnings().isEmpty()
            ? "当前未发现明显重复用药、低库存或近效期库存风险。"
            : String.join("；", dto.getWarnings()));
        return dto;
    }

    private List<HealthInsightWorkbenchDTO.AiPeriodicReportDTO> buildPeriodicReports(
        HealthInsightWorkbenchDTO source) {
        List<HealthInsightWorkbenchDTO.AiPeriodicReportDTO> reports = new ArrayList<>();
        reports.add(buildPeriodicReport(source, "WEEKLY"));
        reports.add(buildPeriodicReport(source, "MONTHLY"));
        return reports;
    }

    private HealthInsightWorkbenchDTO.AiPeriodicReportDTO buildPeriodicReport(HealthInsightWorkbenchDTO source,
        String reportType) {
        boolean monthly = Objects.equals("MONTHLY", reportType);
        HealthInsightWorkbenchDTO.AiPeriodicReportDTO dto = new HealthInsightWorkbenchDTO.AiPeriodicReportDTO();
        dto.setReportType(monthly ? "MONTHLY" : "WEEKLY");
        dto.setTitle(monthly ? "健康月报" : "健康周报");
        dto.setGeneratedTime(new Date());
        dto.setSummary(StrUtil.format("{}重点：{}；{}；{}。", monthly ? "本月" : "本周",
            source.getReviewLoop().getSummary(), source.getCareDashboard().getSummary(),
            source.getMedicationSafety().getSummary()));
        if (source.getIndicatorAlerts().isEmpty()) {
            dto.getSuggestions().add(monthly
                ? "本月建议继续补充体检报告和慢病日记，形成更连续的趋势依据。"
                : "本周建议继续补充体检报告和慢病日记，形成更连续的趋势依据。");
        } else {
            dto.getSuggestions().add("优先查看连续异常或高优先级指标，必要时安排复查。");
        }
        if (source.getReviewLoop().getDueReviewTaskCount() > 0) {
            dto.getSuggestions().add("已有到期复查任务，建议尽快进入慢病专项处理。");
        }
        if (source.getMedicationSafety().getWarnings().isEmpty()) {
            dto.getSuggestions().add("当前用药安全摘要未发现明显风险，建议继续保持提醒反馈。");
        } else {
            dto.getSuggestions().add("请先核对用药安全提示中的重复用药、库存和效期问题。");
        }
        if (monthly) {
            dto.getSuggestions().add("月报更适合做家庭健康复盘，可结合近一个月的报告、日记和用药执行情况观察变化。");
        }
        return dto;
    }

    private HealthInsightWorkbenchDTO.VoiceEntryGuideDTO buildVoiceEntryGuide() {
        HealthInsightWorkbenchDTO.VoiceEntryGuideDTO dto = new HealthInsightWorkbenchDTO.VoiceEntryGuideDTO();
        dto.setSupported(true);
        dto.setFlow("录音 -> ASR -> LLM/规则结构化 -> 慢病日记 -> 健康时间线");
        dto.setExampleText("例如：今天早上血压 135/85，头有点晕，晚上已经按时吃药。");
        dto.setSummary("当前版本先支持提交语音识别后的文本并保存为慢病日记，后续可继续扩展音频上传和结构化解析。");
        return dto;
    }
    private String buildProblemStatusLogContent(HealthProblemStatusLogEntity log) {
        if (Objects.equals(PROBLEM_STATUS_ACTION_REVIEW_TASK_COMPLETE, log.getActionType())) {
            String remark = StrUtil.trim(log.getActionRemark());
            return StrUtil.isBlank(remark) ? "完成了一次复查跟进。" : "完成了一次复查跟进。备注：" + remark;
        }
        String statusChange = StrUtil.format("从{}调整为{}。",
            resolveProblemStatusName(log.getBeforeStatus()), resolveProblemStatusName(log.getAfterStatus()));
        if (StrUtil.isBlank(log.getActionRemark())) {
            return statusChange;
        }
        return statusChange + "备注：" + StrUtil.trim(log.getActionRemark());
    }

    private String resolveProblemStatusActionName(String actionType) {
        if (Objects.equals(PROBLEM_STATUS_ACTION_CHANGE, actionType)) {
            return "状态变更";
        }
        if (Objects.equals(PROBLEM_STATUS_ACTION_REVIEW_TASK_COMPLETE, actionType)) {
            return "复查完成";
        }
        return "问题处理";
    }
    private List<HealthInsightWorkbenchDTO.HealthTimelineItemDTO> buildTimeline(
        Map<Long, HealthFamilyMemberEntity> memberMap, List<HealthReportEntity> reports,
        List<HealthMedicationReminderEntity> reminders, List<HealthOperationTaskEntity> reviewTasks,
        List<HealthChronicDiaryEntryEntity> diaries, List<HealthProblemStatusLogEntity> problemStatusLogs) {
        List<HealthInsightWorkbenchDTO.HealthTimelineItemDTO> items = new ArrayList<>();
        reports.forEach(report -> items.add(buildTimelineItem("REPORT", report.getReportDate(), report.getMemberId(),
            memberMap, report.getReportId(), StrUtil.blankToDefault(report.getReportName(), "体检报告"),
            StrUtil.blankToDefault(report.getAnalysisSummary(), "上传或更新了一份体检报告。"),
            HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue(), "REPORT_ADVICE")));
        reminders.forEach(reminder -> items.add(buildTimelineItem("REMINDER", reminder.getScheduledTime(),
            reminder.getMemberId(), memberMap, reminder.getReminderId(), "待处理用药提醒",
            StrUtil.format("{} {}{}", StrUtil.blankToDefault(reminder.getDrugNameSnapshot(), "用药"),
                reminder.getDoseAmount() == null ? "" : reminder.getDoseAmount().stripTrailingZeros().toPlainString(),
                StrUtil.blankToDefault(reminder.getDoseUnit(), "")),
            HealthFollowUpTargetPageEnum.MEDICATION_REMINDER_DETAIL.getValue(), "REMINDER")));
        reviewTasks.forEach(task -> items.add(buildTimelineItem("CHRONIC_REVIEW", task.getStartTime(),
            task.getMemberId(), memberMap, task.getTargetBizId(), StrUtil.blankToDefault(task.getTaskTitle(), "慢病复查任务"),
            StrUtil.blankToDefault(task.getTaskContent(), "需要回到慢病专项完成复查。"),
            HealthFollowUpTargetPageEnum.WORKSPACE_CHRONIC_DISEASE.getValue(),
            ChronicDiseaseApplicationService.TARGET_BIZ_TYPE_CHRONIC_REVIEW)));
        diaries.forEach(diary -> items.add(buildTimelineItem("CHRONIC_DIARY", diary.getRecordTime(), diary.getMemberId(),
            memberMap, diary.getDiaryEntryId(), StrUtil.blankToDefault(diary.getEntryTitle(), "慢病日记"),
            StrUtil.blankToDefault(diary.getEntryContent(), "记录了一条慢病日记。"),
            HealthFollowUpTargetPageEnum.WORKSPACE_CHRONIC_DISEASE.getValue(), "CHRONIC_DIARY")));
        problemStatusLogs.forEach(log -> items.add(buildTimelineItem("HEALTH_PROBLEM_STATUS", log.getActionTime(),
            log.getMemberId(), memberMap, log.getProblemId(), resolveProblemStatusActionName(log.getActionType()), buildProblemStatusLogContent(log),
            HealthFollowUpTargetPageEnum.WORKSPACE_HEALTH_INSIGHT.getValue(), log.getActionType())));
        return items.stream()
            .sorted(Comparator.comparing(HealthInsightWorkbenchDTO.HealthTimelineItemDTO::getEventTime,
                Comparator.nullsLast(Date::compareTo)).reversed())
            .limit(TIMELINE_LIMIT)
            .collect(Collectors.toList());
    }

    private HealthInsightWorkbenchDTO.HealthTimelineItemDTO buildTimelineItem(String itemType, Date eventTime,
        Long memberId, Map<Long, HealthFamilyMemberEntity> memberMap, Long businessId, String title, String content,
        String targetPageCode, String targetBizType) {
        HealthInsightWorkbenchDTO.HealthTimelineItemDTO dto = new HealthInsightWorkbenchDTO.HealthTimelineItemDTO();
        dto.setItemType(itemType);
        dto.setEventTime(eventTime);
        dto.setMemberId(memberId);
        dto.setMemberName(memberMap.get(memberId) == null ? null : memberMap.get(memberId).getMemberName());
        dto.setBusinessId(businessId);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setTargetPageCode(targetPageCode);
        dto.setTargetBizType(targetBizType);
        return dto;
    }

    private void fillDiaryEntry(HealthChronicDiaryEntryEntity entity, Long memberId, Long ownerUserId, Long profileId,
        String diseaseCode, String entryType, Date recordTime, String entryTitle, String entryContent,
        String metricPayloadJson, String sourceType, String remark) {
        entity.setOwnerUserId(ownerUserId);
        entity.setMemberId(memberId);
        entity.setProfileId(profileId);
        entity.setDiseaseCode(StrUtil.trim(diseaseCode));
        entity.setEntryType(StrUtil.blankToDefault(StrUtil.trim(entryType), "GENERAL").toUpperCase(Locale.ROOT));
        entity.setRecordTime(recordTime == null ? new Date() : recordTime);
        entity.setEntryTitle(StrUtil.blankToDefault(StrUtil.trim(entryTitle), "慢病日记"));
        entity.setEntryContent(StrUtil.trim(entryContent));
        entity.setMetricPayloadJson(StrUtil.trim(metricPayloadJson));
        entity.setSourceType(sourceType);
        entity.setStatus(StatusEnum.ENABLE.getValue());
        entity.setRemark(StrUtil.trim(remark));
    }

    private boolean isAbnormalItem(HealthReportItemEntity item) {
        if (item == null || item.getAbnormalFlag() == null) {
            return false;
        }
        return Objects.equals(item.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.LOW.getValue())
            || Objects.equals(item.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.HIGH.getValue())
            || Objects.equals(item.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue());
    }

    private String resolveAbnormalFlagName(Integer abnormalFlag) {
        if (abnormalFlag == null) {
            return "待判断";
        }
        for (HealthReportItemAbnormalFlagEnum item : HealthReportItemAbnormalFlagEnum.values()) {
            if (Objects.equals(item.getValue(), abnormalFlag)) {
                return item.description();
            }
        }
        return "待判断";
    }
    private java.util.Optional<Long> resolveFocusMember(List<HealthChronicDiseaseProfileEntity> profiles,
        List<HealthMedicationReminderEntity> pendingReminders, List<HealthFamilyMemberEntity> members) {
        return profiles.stream()
            .filter(profile -> Objects.equals(profile.getRiskLevel(), HealthFollowUpRiskLevelEnum.HIGH.getValue()))
            .map(HealthChronicDiseaseProfileEntity::getMemberId)
            .findFirst()
            .or(() -> pendingReminders.stream().map(HealthMedicationReminderEntity::getMemberId).findFirst())
            .or(() -> members.stream().map(HealthFamilyMemberEntity::getMemberId).findFirst());
    }

    private int countDuplicateMedications(List<HealthMedicationPlanEntity> activePlans) {
        Map<String, Long> countMap = activePlans.stream()
            .collect(Collectors.groupingBy(plan -> plan.getMemberId() + "::" + resolvePlanDrugKey(plan),
                Collectors.counting()));
        return (int) countMap.values().stream().filter(count -> count > 1).count();
    }

    private String resolvePlanDrugKey(HealthMedicationPlanEntity plan) {
        if (plan.getDrugId() != null) {
            return "DRUG_" + plan.getDrugId();
        }
        return "CUSTOM_" + StrUtil.blankToDefault(plan.getCustomDrugName(), "UNKNOWN").trim();
    }

    private int countLowStockDrugs(Long currentUserId) {
        List<DrugEntity> drugs = drugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, currentUserId)
            .eq(DrugEntity::getStatus, StatusEnum.ENABLE.getValue())
            .isNotNull(DrugEntity::getStockAlertThreshold)
            .list();
        if (drugs.isEmpty()) {
            return 0;
        }
        Set<Long> drugIds = drugs.stream().map(DrugEntity::getDrugId).collect(Collectors.toSet());
        Map<Long, BigDecimal> stockMap = drugStockBatchService.lambdaQuery()
            .in(HealthDrugStockBatchEntity::getDrugId, drugIds)
            .list()
            .stream()
            .collect(Collectors.groupingBy(HealthDrugStockBatchEntity::getDrugId,
                Collectors.reducing(BigDecimal.ZERO,
                    batch -> batch.getStockQuantity() == null ? BigDecimal.ZERO : batch.getStockQuantity(),
                    BigDecimal::add)));
        return (int) drugs.stream()
            .filter(drug -> stockMap.getOrDefault(drug.getDrugId(), BigDecimal.ZERO)
                .compareTo(drug.getStockAlertThreshold()) < 0)
            .count();
    }

    private int countNearExpireBatches(Long currentUserId) {
        Date now = DateUtil.beginOfDay(new Date());
        Date end = DateUtil.endOfDay(DateUtil.offsetDay(now, NEAR_EXPIRE_DAYS));
        Long count = drugStockBatchService.lambdaQuery()
            .eq(HealthDrugStockBatchEntity::getOwnerUserId, currentUserId)
            .isNotNull(HealthDrugStockBatchEntity::getExpireDate)
            .gt(HealthDrugStockBatchEntity::getStockQuantity, BigDecimal.ZERO)
            .ge(HealthDrugStockBatchEntity::getExpireDate, now)
            .le(HealthDrugStockBatchEntity::getExpireDate, end)
            .count();
        return count == null ? 0 : count.intValue();
    }

    private String resolveMemberName(Long memberId) {
        HealthFamilyMemberEntity member = memberId == null ? null : familyMemberService.getById(memberId);
        return member == null ? null : member.getMemberName();
    }
}
