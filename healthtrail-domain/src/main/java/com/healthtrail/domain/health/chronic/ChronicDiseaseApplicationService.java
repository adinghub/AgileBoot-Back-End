package com.healthtrail.domain.health.chronic;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.chronic.command.CancelChronicDiseaseReviewTaskCommand;
import com.healthtrail.domain.health.chronic.command.CreateChronicDiseaseReviewTaskCommand;
import com.healthtrail.domain.health.chronic.command.SaveChronicIndicatorTargetCommand;
import com.healthtrail.domain.health.chronic.command.SaveChronicDiseaseProfileCommand;
import com.healthtrail.domain.health.chronic.command.SaveChronicDiseaseTypeCommand;
import com.healthtrail.domain.health.chronic.command.UpdateChronicDiseaseTypeCommand;
import com.healthtrail.domain.health.chronic.db.HealthChronicIndicatorTargetEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicIndicatorTargetService;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileService;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseTypeEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseTypeService;
import com.healthtrail.domain.health.chronic.dto.ChronicIndicatorTargetDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseDashboardDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseIndicatorPointDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseHomeSummaryDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseIndicatorTrendDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseProfileDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseRelatedProblemDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseReviewTaskDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseTypeDTO;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskService;
import com.healthtrail.domain.health.chronic.query.ChronicDiseaseTypeQuery;
import com.healthtrail.domain.health.family.FamilyMemberAccessService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.family.dto.FamilyMemberAccessContextDTO;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.problem.db.HealthProblemEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemChronicProfileLinkEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemChronicProfileLinkService;
import com.healthtrail.domain.health.problem.db.HealthProblemService;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemService;
import com.healthtrail.domain.health.report.db.HealthReportService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * App 慢病专项应用服务。
 *
 * <p>慢病专项的核心边界是“通用专项容器”，不是某个单病种功能。
 * 本服务只识别 `diseaseCode` 和病种配置里的指标映射，
 * 不在业务逻辑里写“高血压要查哪些字段、糖尿病要查哪些字段”的 if/else。
 * 这样新增慢病时只要维护 `health_chronic_disease_type`，
 * App 新增专项和详情看板就能按同一条链路工作。
 */
@Service
@RequiredArgsConstructor
public class ChronicDiseaseApplicationService {

    /**
     * 趋势页最多回看最近 12 份报告。
     *
     * <p>这个限制是性能边界，不是业务规则。慢病专项用于移动端看板，
     * 需要优先保证详情看板的响应速度，长期深度分析后续可以单独做分页或会员报告。
     */
    private static final int DASHBOARD_REPORT_LIMIT = 12;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

    /**
     * 慢病复查任务目标业务类型。
     *
     * <p>复查任务复用首页 `operation_task`，因此需要一个稳定的 targetBizType，
     * 让首页、任务中心和 App 跳转层都能识别“这是一条慢病专项复查任务”。
     */
    public static final String TARGET_BIZ_TYPE_CHRONIC_REVIEW = "CHRONIC_REVIEW";

    /**
     * 一键创建复查任务时的默认提醒间隔。
     *
     * <p>这是产品交互默认值，不是医学随访周期。不同慢病的真实复查建议仍应在病种配置
     * 或医生建议里表达，快捷按钮只负责给用户一个可立即使用的提醒落点。
     */
    private static final int DEFAULT_REVIEW_AFTER_DAYS = 30;

    /**
     * 复查任务到期后继续在首页任务流展示的天数。
     *
     * <p>保留 7 天窗口可以覆盖用户短期忘记处理的情况，
     * 同时避免很久以前的复查任务一直占用首页首屏。
     */
    private static final int REVIEW_TASK_VISIBLE_DAYS = 7;
    /** 慢病病种配置数据库服务 */
    private final HealthChronicDiseaseTypeService chronicDiseaseTypeService;

    /** 慢病专项档案数据库服务 */
    private final HealthChronicDiseaseProfileService chronicDiseaseProfileService;

    /** 慢病指标目标数据库服务 */
    private final HealthChronicIndicatorTargetService indicatorTargetService;

    /** 家庭成员访问控制服务 */
    private final FamilyMemberAccessService familyMemberAccessService;

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 体检报告数据库服务 */
    private final HealthReportService reportService;

    /** 体检报告指标项数据库服务 */
    private final HealthReportItemService reportItemService;

    /** 健康问题数据库服务 */
    private final HealthProblemService problemService;

    /** 健康问题与慢病档案关联数据库服务 */
    private final HealthProblemChronicProfileLinkService problemChronicProfileLinkService;

    /** 用药计划数据库服务 */
    private final HealthMedicationPlanService medicationPlanService;

    /** 首页运营任务数据库服务 */
    private final HealthOperationTaskService operationTaskService;

    /**
     * 后台分页查询慢病病种配置。
     *
     * <p>该能力让运营可以直接维护“哪些慢病可建专项、重点看哪些指标”，
     * App 侧仍然只消费启用后的病种清单，不需要为单个病种新增页面分支。
     */
    public PageDTO<ChronicDiseaseTypeDTO> getDiseaseTypePage(ChronicDiseaseTypeQuery query) {
        Page<HealthChronicDiseaseTypeEntity> page = chronicDiseaseTypeService.page(query.toPage(),
            query.toQueryWrapper());
        List<ChronicDiseaseTypeDTO> records = page.getRecords().stream()
            .map(this::buildDiseaseTypeDTO)
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 后台查询慢病病种详情。
     */
    public ChronicDiseaseTypeDTO getDiseaseTypeInfo(Long typeId) {
        return buildDiseaseTypeDTO(loadRequiredDiseaseTypeEntity(typeId));
    }

    /**
     * 后台新增慢病病种配置。
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDiseaseType(SaveChronicDiseaseTypeCommand command) {
        checkDiseaseCodeDuplicated(null, command.getDiseaseCode());
        HealthChronicDiseaseTypeEntity entity = new HealthChronicDiseaseTypeEntity();
        fillDiseaseTypeEntity(entity, command);
        chronicDiseaseTypeService.save(entity);
    }

    /**
     * 后台修改慢病病种配置。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDiseaseType(UpdateChronicDiseaseTypeCommand command) {
        HealthChronicDiseaseTypeEntity entity = loadRequiredDiseaseTypeEntity(command.getTypeId());
        checkDiseaseCodeDuplicated(command.getTypeId(), command.getDiseaseCode());
        fillDiseaseTypeEntity(entity, command);
        chronicDiseaseTypeService.updateById(entity);
    }

    /**
     * 后台删除慢病病种配置。
     *
     * <p>已创建专项会保存病种快照名称，但删除病种后不能再新建该病种专项；
     * 因此删除动作只适合误配置场景，常规下线优先把状态改为停用。
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeDiseaseType(Long typeId) {
        loadRequiredDiseaseTypeEntity(typeId);
        chronicDiseaseTypeService.removeById(typeId);
    }

    /**
     * 查询启用中的慢病病种配置。
     *
     * <p>App 新增慢病专项时必须从这份清单选择病种，
     * 这样后端可以持续通过配置支持所有慢病，而不是由前端自己维护一份病种枚举。
     */
    public List<ChronicDiseaseTypeDTO> listEnabledDiseaseTypes() {
        return chronicDiseaseTypeService.lambdaQuery()
            .eq(HealthChronicDiseaseTypeEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByAsc(HealthChronicDiseaseTypeEntity::getSort)
            .orderByAsc(HealthChronicDiseaseTypeEntity::getTypeId)
            .list()
            .stream()
            .map(this::buildDiseaseTypeDTO)
            .collect(Collectors.toList());
    }

    /**
     * 查询当前账号可访问的慢病专项档案。
     */
    public List<ChronicDiseaseProfileDTO> listProfiles(Long memberId, Integer profileStatus, String keyword, Long currentUserId) {
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(memberId, currentUserId);
        if (accessibleMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<HealthChronicDiseaseProfileEntity> profiles = chronicDiseaseProfileService.lambdaQuery()
            .in(HealthChronicDiseaseProfileEntity::getMemberId, accessibleMemberIds)
            .eq(profileStatus != null, HealthChronicDiseaseProfileEntity::getProfileStatus, profileStatus)
            .like(StrUtil.isNotBlank(keyword), HealthChronicDiseaseProfileEntity::getDiseaseNameSnapshot, keyword)
            .orderByAsc(HealthChronicDiseaseProfileEntity::getProfileStatus)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getLastReviewDate)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getProfileId)
            .list();
        if (profiles.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ChronicDiseaseTypeDTO> diseaseTypeMap = loadDiseaseTypeDTOMap();
        Map<Long, String> memberNameMap = loadMemberNameMap(profiles.stream()
            .map(HealthChronicDiseaseProfileEntity::getMemberId)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
        return profiles.stream()
            .map(profile -> buildProfileDTO(profile, diseaseTypeMap, memberNameMap))
            .collect(Collectors.toList());
    }

    /**
     * 新增慢病专项档案。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiseaseProfileDTO createProfile(SaveChronicDiseaseProfileCommand command, Long currentUserId) {
        FamilyMemberAccessContextDTO accessContext = familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        ChronicDiseaseTypeDTO diseaseTypeDTO = loadRequiredDiseaseType(command.getDiseaseCode());
        checkProfileDuplicated(null, command.getMemberId(), diseaseTypeDTO.getDiseaseCode());

        HealthChronicDiseaseProfileEntity entity = new HealthChronicDiseaseProfileEntity();
        fillProfileEntity(entity, command, diseaseTypeDTO, accessContext.getOwnerUserId());
        chronicDiseaseProfileService.save(entity);
        return buildProfileDTO(entity, Collections.singletonMap(diseaseTypeDTO.getDiseaseCode(), diseaseTypeDTO),
            Collections.singletonMap(entity.getMemberId(), resolveMemberName(entity.getMemberId())));
    }

    /**
     * 修改慢病专项档案。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiseaseProfileDTO updateProfile(Long profileId, SaveChronicDiseaseProfileCommand command, Long currentUserId) {
        HealthChronicDiseaseProfileEntity entity = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(entity.getMemberId(), currentUserId);
        FamilyMemberAccessContextDTO newAccessContext = familyMemberAccessService.checkCanEditMember(command.getMemberId(), currentUserId);
        ChronicDiseaseTypeDTO diseaseTypeDTO = loadRequiredDiseaseType(command.getDiseaseCode());
        checkProfileDuplicated(profileId, command.getMemberId(), diseaseTypeDTO.getDiseaseCode());

        fillProfileEntity(entity, command, diseaseTypeDTO, newAccessContext.getOwnerUserId());
        chronicDiseaseProfileService.updateById(entity);
        return buildProfileDTO(entity, Collections.singletonMap(diseaseTypeDTO.getDiseaseCode(), diseaseTypeDTO),
            Collections.singletonMap(entity.getMemberId(), resolveMemberName(entity.getMemberId())));
    }

    /**
     * 删除慢病专项档案。
     *
     * <p>这里使用 MyBatis-Plus 逻辑删除能力，
     * 保留历史数据可追溯性，避免后续做慢病专项审计或恢复时完全丢失原始配置。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProfile(Long profileId, Long currentUserId) {
        HealthChronicDiseaseProfileEntity entity = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(entity.getMemberId(), currentUserId);
        chronicDiseaseProfileService.removeById(profileId);
    }

    /**
     * 查询慢病专项详情看板。
     */
    public ChronicDiseaseDashboardDTO getProfileDashboard(Long profileId, Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.getRequiredAccessContext(profile.getMemberId(), currentUserId);

        ChronicDiseaseTypeDTO diseaseTypeDTO = loadRequiredDiseaseType(profile.getDiseaseCode());
        ChronicDiseaseProfileDTO profileDTO = buildProfileDTO(profile,
            Collections.singletonMap(diseaseTypeDTO.getDiseaseCode(), diseaseTypeDTO),
            Collections.singletonMap(profile.getMemberId(), resolveMemberName(profile.getMemberId())));

        List<HealthReportEntity> reports = listRecentReports(profile.getMemberId());
        List<HealthChronicIndicatorTargetEntity> indicatorTargets = listActiveIndicatorTargets(profile.getProfileId());
        Map<String, HealthChronicIndicatorTargetEntity> indicatorTargetMap = buildIndicatorTargetMap(indicatorTargets);
        List<ChronicDiseaseIndicatorTrendDTO> trendItems = buildIndicatorTrends(reports, diseaseTypeDTO, indicatorTargetMap);

        ChronicDiseaseDashboardDTO dashboardDTO = new ChronicDiseaseDashboardDTO();
        dashboardDTO.setProfile(profileDTO);
        dashboardDTO.setLatestReportDate(resolveLatestReportDate(reports));
        List<ChronicDiseaseRelatedProblemDTO> relatedProblems = listLinkedProblems(profile);
        dashboardDTO.setIndicatorTrends(trendItems);
        dashboardDTO.setRelatedProblems(relatedProblems);
        dashboardDTO.setFocusIndicatorCount(trendItems.size());
        dashboardDTO.setIndicatorTargetCount(indicatorTargets.size());
        dashboardDTO.setAbnormalProblemCount((int) relatedProblems.stream()
            .filter(problem -> !Objects.equals(problem.getProblemStatus(), 3))
            .count());
        dashboardDTO.setActiveMedicationPlanCount(countActiveMedicationPlans(profile.getMemberId()));

        // 复查任务和专项看板同屏返回，用户能直接看到是否已经安排过下一次复盘。
        HealthOperationTaskEntity activeReviewTask = loadActiveReviewTask(profile.getOwnerUserId(), profile.getProfileId());
        dashboardDTO.setReviewTask(activeReviewTask == null ? null : buildReviewTaskDTO(activeReviewTask));

        dashboardDTO.setDashboardSummary(buildDashboardSummary(profileDTO, dashboardDTO));
        return dashboardDTO;
    }

    /**
     * 一键创建慢病专项复查任务。
     *
     * <p>复查任务复用首页 `operation_task`，而不是单独新增慢病任务表。这样首页待跟进、
     * 已读/忽略/完成操作和 App 统一跳转都能沿用既有任务流。
     *
     * <p>同一个慢病档案只保留一条当前仍有效的复查任务：用户重复点击时更新原任务日期和内容，
     * 避免首页出现多条语义相同的慢病复查提醒。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicDiseaseReviewTaskDTO createReviewTask(Long profileId, CreateChronicDiseaseReviewTaskCommand command,
        Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(profile.getMemberId(), currentUserId);

        Date reviewDate = command == null || command.getReviewDate() == null
            ? DateUtil.offsetDay(new Date(), DEFAULT_REVIEW_AFTER_DAYS)
            : command.getReviewDate();
        Date reviewStartTime = DateUtil.beginOfDay(reviewDate);
        Date reviewEndTime = DateUtil.endOfDay(DateUtil.offsetDay(reviewStartTime, REVIEW_TASK_VISIBLE_DAYS));
        ChronicDiseaseTypeDTO diseaseTypeDTO = loadRequiredDiseaseType(profile.getDiseaseCode());
        String memberName = StrUtil.blankToDefault(resolveMemberName(profile.getMemberId()), "家庭成员");

        HealthOperationTaskEntity task = loadActiveReviewTask(profile.getOwnerUserId(), profile.getProfileId());
        if (task == null) {
            task = new HealthOperationTaskEntity();
            task.setOwnerUserId(profile.getOwnerUserId());
            task.setMemberId(profile.getMemberId());
            task.setTargetBizId(profile.getProfileId());
            task.setTargetBizType(TARGET_BIZ_TYPE_CHRONIC_REVIEW);
        }
        fillReviewOperationTask(task, profile, diseaseTypeDTO, memberName, reviewStartTime, reviewEndTime,
            command == null ? null : command.getRemark());

        if (task.getOperationTaskId() == null) {
            operationTaskService.save(task);
        } else {
            operationTaskService.updateById(task);
        }
        return buildReviewTaskDTO(task);
    }

    /**
     * 取消慢病专项复查任务。
     *
     * <p>复查任务本质上是挂在首页待跟进流里的提醒，取消时只把对应 `operation_task` 关闭。
     * 这里同时校验 profileId、operationTaskId、成员和归属账号，避免用户用一个可访问档案去关闭其他任务。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelReviewTask(Long profileId, Long operationTaskId, CancelChronicDiseaseReviewTaskCommand command,
        Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(profile.getMemberId(), currentUserId);

        HealthOperationTaskEntity task = loadRequiredReviewTask(profile, operationTaskId);

        Date now = new Date();
        task.setStatus(StatusEnum.DISABLE.getValue());
        task.setEndTime(now);
        task.setRemark(limitLength(buildReviewTaskCancelRemark(task.getRemark(), command), 500));
        task.setUpdaterId(currentUserId);
        task.setUpdateTime(now);
        operationTaskService.updateById(task);
    }

    /**
     * 删除慢病专项复查任务。
     *
     * <p>删除和取消的业务含义不同：取消表示这次提醒不再需要，但任务记录仍作为普通历史状态保留；
     * 删除用于用户明确想移除误建的复查提醒。这里走 `operation_task` 的逻辑删除，既不再出现在看板和首页，
     * 又保留数据库侧审计字段，便于后续排查误操作。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReviewTask(Long profileId, Long operationTaskId, Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(profile.getMemberId(), currentUserId);

        HealthOperationTaskEntity task = loadRequiredReviewTask(profile, operationTaskId);
        Date now = new Date();
        task.setStatus(StatusEnum.DISABLE.getValue());
        task.setEndTime(now);
        task.setRemark(limitLength(buildReviewTaskDeleteRemark(task.getRemark()), 500));
        task.setUpdaterId(currentUserId);
        task.setUpdateTime(now);
        operationTaskService.updateById(task);
        operationTaskService.removeById(task.getOperationTaskId());
    }

    /**
     * 查询慢病专项已维护的指标目标范围。
     *
     * <p>目标范围属于慢病档案的扩展配置，读取前仍要校验成员访问权限，
     * 避免只凭 profileId 猜测到其他家庭成员的目标数据。
     */
    public List<ChronicIndicatorTargetDTO> listIndicatorTargets(Long profileId, Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.getRequiredAccessContext(profile.getMemberId(), currentUserId);
        return indicatorTargetService.lambdaQuery()
            .eq(HealthChronicIndicatorTargetEntity::getProfileId, profileId)
            .orderByDesc(HealthChronicIndicatorTargetEntity::getStatus)
            .orderByAsc(HealthChronicIndicatorTargetEntity::getIndicatorName)
            .orderByAsc(HealthChronicIndicatorTargetEntity::getTargetId)
            .list()
            .stream()
            .map(ChronicIndicatorTargetDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * 新增或更新慢病专项指标目标范围。
     *
     * <p>同一专项下同一指标只保留一条当前目标。重复保存时更新原记录，
     * 这样 App 从趋势卡快速调整目标时不会产生多条互相冲突的范围配置。
     */
    @Transactional(rollbackFor = Exception.class)
    public ChronicIndicatorTargetDTO saveIndicatorTarget(Long profileId, SaveChronicIndicatorTargetCommand command,
        Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(profile.getMemberId(), currentUserId);
        validateIndicatorTargetCommand(command);

        String indicatorCode = normalizeIndicatorCode(command.getIndicatorCode());
        HealthChronicIndicatorTargetEntity entity = indicatorTargetService.lambdaQuery()
            .eq(HealthChronicIndicatorTargetEntity::getProfileId, profileId)
            .eq(HealthChronicIndicatorTargetEntity::getIndicatorCode, indicatorCode)
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
        if (entity == null) {
            entity = new HealthChronicIndicatorTargetEntity();
            entity.setOwnerUserId(profile.getOwnerUserId());
            entity.setMemberId(profile.getMemberId());
            entity.setProfileId(profileId);
        }
        fillIndicatorTargetEntity(entity, command, indicatorCode);
        if (entity.getTargetId() == null) {
            indicatorTargetService.save(entity);
        } else {
            indicatorTargetService.updateById(entity);
        }
        return new ChronicIndicatorTargetDTO(entity);
    }

    /**
     * 删除慢病专项指标目标范围。
     *
     * <p>删除只移除目标配置，不会改动体检报告原始指标，也不会影响病种默认关注指标。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndicatorTarget(Long profileId, Long targetId, Long currentUserId) {
        HealthChronicDiseaseProfileEntity profile = loadRequiredProfile(profileId);
        familyMemberAccessService.checkCanEditMember(profile.getMemberId(), currentUserId);
        HealthChronicIndicatorTargetEntity entity = indicatorTargetService.getById(targetId);
        if (entity == null || !Objects.equals(entity.getProfileId(), profileId)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, targetId, "指标目标范围");
        }
        indicatorTargetService.removeById(targetId);
    }

    /**
     * 首页慢病专项轻量摘要。
     *
     * <p>首页不直接计算完整指标趋势，因为详情看板已经会扫描报告明细。
     * 这里仅统计档案数量、重点风险和待复查任务数量，避免首页摘要因为慢病专项增加明显负担。
     */
    public ChronicDiseaseHomeSummaryDTO getHomeSummary(Long ownerUserId) {
        List<HealthChronicDiseaseProfileEntity> profiles = chronicDiseaseProfileService.lambdaQuery()
            .eq(HealthChronicDiseaseProfileEntity::getOwnerUserId, ownerUserId)
            .ne(HealthChronicDiseaseProfileEntity::getProfileStatus, 3)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getLastReviewDate)
            .orderByDesc(HealthChronicDiseaseProfileEntity::getProfileId)
            .list();

        ChronicDiseaseHomeSummaryDTO summaryDTO = new ChronicDiseaseHomeSummaryDTO();
        summaryDTO.setActiveProfileCount(profiles.size());
        summaryDTO.setHighRiskProfileCount((int) profiles.stream()
            .filter(profile -> Objects.equals(profile.getRiskLevel(), HealthFollowUpRiskLevelEnum.HIGH.getValue()))
            .count());
        summaryDTO.setReviewTaskCount(countPendingReviewTasks(ownerUserId));
        if (profiles.isEmpty()) {
            summaryDTO.setSummary("暂未建立慢病专项档案，可先为需要长期关注的成员添加专项管理。");
            return summaryDTO;
        }

        HealthChronicDiseaseProfileEntity featuredProfile = selectFeaturedHomeProfile(profiles);
        Map<String, ChronicDiseaseTypeDTO> diseaseTypeMap = loadDiseaseTypeDTOMap();
        Map<Long, String> memberNameMap = loadMemberNameMap(profiles.stream()
            .map(HealthChronicDiseaseProfileEntity::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
        ChronicDiseaseTypeDTO diseaseTypeDTO = diseaseTypeMap.get(featuredProfile.getDiseaseCode());
        String memberName = memberNameMap.get(featuredProfile.getMemberId());

        summaryDTO.setLatestProfileId(featuredProfile.getProfileId());
        summaryDTO.setLatestMemberId(featuredProfile.getMemberId());
        summaryDTO.setLatestMemberName(memberName);
        summaryDTO.setLatestDiseaseName(diseaseTypeDTO == null
            ? featuredProfile.getDiseaseNameSnapshot()
            : diseaseTypeDTO.getDiseaseName());
        summaryDTO.setSummary(buildHomeSummaryText(summaryDTO));
        return summaryDTO;
    }

    private HealthOperationTaskEntity loadActiveReviewTask(Long ownerUserId, Long profileId) {
        Date now = new Date();
        return operationTaskService.lambdaQuery()
            .eq(HealthOperationTaskEntity::getOwnerUserId, ownerUserId)
            .eq(HealthOperationTaskEntity::getTargetBizType, TARGET_BIZ_TYPE_CHRONIC_REVIEW)
            .eq(HealthOperationTaskEntity::getTargetBizId, profileId)
            .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue())
            .and(wrapper -> wrapper.isNull(HealthOperationTaskEntity::getEndTime)
                .or()
                .ge(HealthOperationTaskEntity::getEndTime, now))
            .orderByDesc(HealthOperationTaskEntity::getOperationTaskId)
            .page(new Page<>(1, 1))
            .getRecords()
            .stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * 读取并校验慢病专项复查任务。
     *
     * <p>复查任务和普通首页任务共用 `operation_task`，所以任何取消、删除动作都必须同时校验：
     * 任务类型、慢病档案、家庭成员和归属账号。这样即使前端传入了其他任务 ID，也无法越权操作。
     */
    private HealthOperationTaskEntity loadRequiredReviewTask(HealthChronicDiseaseProfileEntity profile,
        Long operationTaskId) {
        HealthOperationTaskEntity task = operationTaskService.getById(operationTaskId);
        if (task == null || !Objects.equals(task.getTargetBizType(), TARGET_BIZ_TYPE_CHRONIC_REVIEW)
            || !Objects.equals(task.getTargetBizId(), profile.getProfileId())
            || !Objects.equals(task.getMemberId(), profile.getMemberId())
            || !Objects.equals(task.getOwnerUserId(), profile.getOwnerUserId())) {
            throw new ApiException(ErrorCode.Business.HEALTH_OPERATION_TASK_NOT_FOUND);
        }
        return task;
    }

    private void fillReviewOperationTask(HealthOperationTaskEntity task, HealthChronicDiseaseProfileEntity profile,
        ChronicDiseaseTypeDTO diseaseTypeDTO, String memberName, Date reviewStartTime, Date reviewEndTime, String remark) {
        String diseaseName = diseaseTypeDTO == null
            ? StrUtil.blankToDefault(profile.getDiseaseNameSnapshot(), "慢病专项")
            : diseaseTypeDTO.getDiseaseName();
        String riskLevel = resolveReviewTaskRiskLevel(profile.getRiskLevel());
        String targetPageCode = HealthFollowUpTargetPageEnum.WORKSPACE_CHRONIC_DISEASE.getValue();
        String targetAnchorCode = HealthFollowUpTargetAnchorEnum.CHRONIC_DISEASE_DASHBOARD.getValue();

        task.setTaskTitle(StrUtil.format("{}的{}复查提醒", memberName, diseaseName));
        task.setTaskContent(StrUtil.format("建议在{}前后复盘{}，查看重点指标趋势并按需安排复查。",
            DateUtil.format(reviewStartTime, "yyyy-MM-dd"), diseaseName));
        task.setActionText("查看慢病专项");
        task.setRiskLevel(riskLevel);
        task.setPriorityWeight(resolveReviewTaskPriorityWeight(riskLevel));
        task.setTargetPageCode(targetPageCode);
        task.setTargetPageName(HealthAppI18n.targetPageName(targetPageCode));
        task.setTargetAnchorCode(targetAnchorCode);
        task.setTargetAnchorName(HealthAppI18n.targetAnchorName(targetAnchorCode));
        task.setStartTime(reviewStartTime);
        task.setEndTime(reviewEndTime);
        task.setStatus(StatusEnum.ENABLE.getValue());
        task.setRemark(StrUtil.trim(remark));
    }

    /**
     * 生成复查任务取消备注。
     *
     * <p>复查任务取消后会从当前待办中移除，但备注仍保存在任务记录中，
     * 方便后续排查用户是主动取消，还是任务自然结束或被重新调整。
     */
    private String buildReviewTaskCancelRemark(String oldRemark, CancelChronicDiseaseReviewTaskCommand command) {
        List<String> parts = new ArrayList<>();
        if (StrUtil.isNotBlank(oldRemark)) {
            parts.add(StrUtil.trim(oldRemark));
        }
        String remark = command == null ? null : StrUtil.trim(command.getRemark());
        parts.add(StrUtil.isBlank(remark) ? "已取消复查提醒" : "已取消复查提醒：" + remark);
        return String.join("；", parts);
    }

    private String buildReviewTaskDeleteRemark(String oldRemark) {
        List<String> parts = new ArrayList<>();
        if (StrUtil.isNotBlank(oldRemark)) {
            parts.add(StrUtil.trim(oldRemark));
        }
        parts.add("已删除复查提醒");
        return String.join("；", parts);
    }

    private String resolveReviewTaskRiskLevel(String profileRiskLevel) {
        HealthFollowUpRiskLevelEnum riskLevelEnum = HealthFollowUpRiskLevelEnum.fromValue(profileRiskLevel);
        return riskLevelEnum == null
            ? HealthFollowUpRiskLevelEnum.MEDIUM.getValue()
            : riskLevelEnum.getValue();
    }

    private int resolveReviewTaskPriorityWeight(String riskLevel) {
        if (Objects.equals(riskLevel, HealthFollowUpRiskLevelEnum.HIGH.getValue())) {
            return 30;
        }
        if (Objects.equals(riskLevel, HealthFollowUpRiskLevelEnum.MEDIUM.getValue())) {
            return 20;
        }
        return 10;
    }

    private ChronicDiseaseReviewTaskDTO buildReviewTaskDTO(HealthOperationTaskEntity task) {
        ChronicDiseaseReviewTaskDTO dto = new ChronicDiseaseReviewTaskDTO();
        dto.setOperationTaskId(task.getOperationTaskId());
        dto.setProfileId(task.getTargetBizId());
        dto.setReviewDate(task.getStartTime());
        dto.setTaskTitle(task.getTaskTitle());
        dto.setTaskContent(task.getTaskContent());
        dto.setActionText(task.getActionText());
        dto.setTargetPageCode(task.getTargetPageCode());
        dto.setTargetBizType(task.getTargetBizType());
        return dto;
    }

    private int countPendingReviewTasks(Long ownerUserId) {
        Date now = new Date();
        Long count = operationTaskService.lambdaQuery()
            .eq(HealthOperationTaskEntity::getOwnerUserId, ownerUserId)
            .eq(HealthOperationTaskEntity::getTargetBizType, TARGET_BIZ_TYPE_CHRONIC_REVIEW)
            .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue())
            .and(wrapper -> wrapper.isNull(HealthOperationTaskEntity::getEndTime)
                .or()
                .ge(HealthOperationTaskEntity::getEndTime, now))
            .count();
        return count == null ? 0 : count.intValue();
    }

    private HealthChronicDiseaseProfileEntity selectFeaturedHomeProfile(List<HealthChronicDiseaseProfileEntity> profiles) {
        return profiles.stream()
            .sorted((left, right) -> {
                int priorityCompare = Integer.compare(
                    resolveReviewTaskPriorityWeight(resolveReviewTaskRiskLevel(right.getRiskLevel())),
                    resolveReviewTaskPriorityWeight(resolveReviewTaskRiskLevel(left.getRiskLevel())));
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                Date rightReviewDate = right.getLastReviewDate();
                Date leftReviewDate = left.getLastReviewDate();
                if (!Objects.equals(leftReviewDate, rightReviewDate)) {
                    if (leftReviewDate == null) {
                        return 1;
                    }
                    if (rightReviewDate == null) {
                        return -1;
                    }
                    return rightReviewDate.compareTo(leftReviewDate);
                }
                return Long.compare(
                    Objects.requireNonNullElse(right.getProfileId(), 0L),
                    Objects.requireNonNullElse(left.getProfileId(), 0L));
            })
            .findFirst()
            .orElse(profiles.get(0));
    }

    private String buildHomeSummaryText(ChronicDiseaseHomeSummaryDTO summaryDTO) {
        List<String> parts = new ArrayList<>();
        parts.add(StrUtil.format("当前管理{}个慢病专项", summaryDTO.getActiveProfileCount()));
        if (summaryDTO.getHighRiskProfileCount() > 0) {
            parts.add(StrUtil.format("其中{}个需要高优先关注", summaryDTO.getHighRiskProfileCount()));
        }
        if (summaryDTO.getReviewTaskCount() > 0) {
            parts.add(StrUtil.format("已有{}条复查提醒待执行", summaryDTO.getReviewTaskCount()));
        }
        if (StrUtil.isNotBlank(summaryDTO.getLatestDiseaseName())) {
            parts.add(StrUtil.format("建议优先查看{}的{}专项",
                StrUtil.blankToDefault(summaryDTO.getLatestMemberName(), "家庭成员"), summaryDTO.getLatestDiseaseName()));
        }
        return String.join("；", parts) + "。";
    }
    private Set<Long> resolveAccessibleMemberIds(Long memberId, Long currentUserId) {
        if (memberId != null) {
            familyMemberAccessService.getRequiredAccessContext(memberId, currentUserId);
            return Collections.singleton(memberId);
        }
        return familyMemberAccessService.getAccessibleMemberIds(currentUserId);
    }

    private void fillProfileEntity(HealthChronicDiseaseProfileEntity entity, SaveChronicDiseaseProfileCommand command,
        ChronicDiseaseTypeDTO diseaseTypeDTO, Long ownerUserId) {
        entity.setOwnerUserId(ownerUserId);
        entity.setMemberId(command.getMemberId());
        entity.setDiseaseCode(diseaseTypeDTO.getDiseaseCode());
        entity.setDiseaseNameSnapshot(diseaseTypeDTO.getDiseaseName());
        entity.setProfileStatus(command.getProfileStatus() == null ? 1 : command.getProfileStatus());
        entity.setRiskLevel(normalizeRiskLevel(command.getRiskLevel()));
        entity.setDiagnosedDate(command.getDiagnosedDate());
        entity.setTargetSummary(StrUtil.blankToDefault(command.getTargetSummary(), diseaseTypeDTO.getTargetSummary()));
        entity.setCurrentSummary(command.getCurrentSummary());
        entity.setLastReviewDate(command.getLastReviewDate());
        entity.setRemark(command.getRemark());
    }

    private String normalizeRiskLevel(String riskLevel) {
        if (StrUtil.isBlank(riskLevel)) {
            return "LOW";
        }
        String normalized = riskLevel.trim().toUpperCase(Locale.ROOT);
        if (Objects.equals(normalized, "HIGH") || Objects.equals(normalized, "MEDIUM") || Objects.equals(normalized, "LOW")) {
            return normalized;
        }
        return "LOW";
    }

    private void checkProfileDuplicated(Long profileId, Long memberId, String diseaseCode) {
        Long duplicatedCount = chronicDiseaseProfileService.lambdaQuery()
            .eq(HealthChronicDiseaseProfileEntity::getMemberId, memberId)
            .eq(HealthChronicDiseaseProfileEntity::getDiseaseCode, diseaseCode)
            .ne(profileId != null, HealthChronicDiseaseProfileEntity::getProfileId, profileId)
            .count();
        if (duplicatedCount != null && duplicatedCount > 0) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_DISEASE_PROFILE_DUPLICATED);
        }
    }

    private HealthChronicDiseaseProfileEntity loadRequiredProfile(Long profileId) {
        HealthChronicDiseaseProfileEntity entity = chronicDiseaseProfileService.getById(profileId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_DISEASE_PROFILE_NOT_FOUND);
        }
        return entity;
    }

    private ChronicDiseaseTypeDTO loadRequiredDiseaseType(String diseaseCode) {
        if (StrUtil.isBlank(diseaseCode)) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_DISEASE_TYPE_NOT_FOUND);
        }
        HealthChronicDiseaseTypeEntity entity = chronicDiseaseTypeService.lambdaQuery()
            .eq(HealthChronicDiseaseTypeEntity::getDiseaseCode, diseaseCode.trim())
            .eq(HealthChronicDiseaseTypeEntity::getStatus, StatusEnum.ENABLE.getValue())
            .one();
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_DISEASE_TYPE_NOT_FOUND);
        }
        return buildDiseaseTypeDTO(entity);
    }

    private HealthChronicDiseaseTypeEntity loadRequiredDiseaseTypeEntity(Long typeId) {
        HealthChronicDiseaseTypeEntity entity = chronicDiseaseTypeService.getById(typeId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, typeId, "慢病病种");
        }
        return entity;
    }

    private void fillDiseaseTypeEntity(HealthChronicDiseaseTypeEntity entity, SaveChronicDiseaseTypeCommand command) {
        BeanUtil.copyProperties(command, entity, "typeId", "focusIndicatorCodes", "focusIndicatorKeywords");
        entity.setDiseaseCode(StrUtil.trim(command.getDiseaseCode()));
        entity.setDiseaseName(StrUtil.trim(command.getDiseaseName()));
        entity.setDiseaseCategory(StrUtil.blankToDefault(StrUtil.trim(command.getDiseaseCategory()), null));
        entity.setFocusIndicatorCodesJson(toJsonArrayText(command.getFocusIndicatorCodes()));
        entity.setFocusIndicatorKeywordsJson(toJsonArrayText(command.getFocusIndicatorKeywords()));
        entity.setTargetSummary(StrUtil.blankToDefault(StrUtil.trim(command.getTargetSummary()), null));
        entity.setFollowUpSuggestion(StrUtil.blankToDefault(StrUtil.trim(command.getFollowUpSuggestion()), null));
        entity.setSort(command.getSort() == null ? 0 : Math.max(0, command.getSort()));
        entity.setStatus(command.getStatus() == null ? StatusEnum.ENABLE.getValue() : command.getStatus());
        entity.setRemark(StrUtil.blankToDefault(StrUtil.trim(command.getRemark()), null));
    }

    private String toJsonArrayText(List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return "[]";
        }
        List<String> normalizedValues = values.stream()
            .map(StrUtil::trim)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());
        return JSONUtil.toJsonStr(normalizedValues);
    }

    private void checkDiseaseCodeDuplicated(Long ignoredTypeId, String diseaseCode) {
        if (StrUtil.isBlank(diseaseCode)) {
            return;
        }
        long count = chronicDiseaseTypeService.lambdaQuery()
            .eq(HealthChronicDiseaseTypeEntity::getDiseaseCode, StrUtil.trim(diseaseCode))
            .ne(ignoredTypeId != null, HealthChronicDiseaseTypeEntity::getTypeId, ignoredTypeId)
            .count();
        if (count > 0) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_DISEASE_TYPE_CODE_DUPLICATED);
        }
    }

    private Map<String, ChronicDiseaseTypeDTO> loadDiseaseTypeDTOMap() {
        return chronicDiseaseTypeService.lambdaQuery()
            .list()
            .stream()
            .map(this::buildDiseaseTypeDTO)
            .collect(Collectors.toMap(ChronicDiseaseTypeDTO::getDiseaseCode, item -> item, (left, right) -> left,
                LinkedHashMap::new));
    }

    private ChronicDiseaseTypeDTO buildDiseaseTypeDTO(HealthChronicDiseaseTypeEntity entity) {
        return new ChronicDiseaseTypeDTO(entity, parseStringArray(entity.getFocusIndicatorCodesJson()),
            parseStringArray(entity.getFocusIndicatorKeywordsJson()));
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
            // 配置 JSON 写错时不应该直接拖垮 App 页面。
            // 返回空列表后，详情页仍能展示档案本身，运营可根据日志/页面空结果修正配置。
            return Collections.emptyList();
        }
    }

    private Map<Long, String> loadMemberNameMap(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return familyMemberService.listByIds(memberIds)
            .stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, HealthFamilyMemberEntity::getMemberName,
                (left, right) -> left));
    }

    private String resolveMemberName(Long memberId) {
        HealthFamilyMemberEntity member = memberId == null ? null : familyMemberService.getById(memberId);
        return member == null ? null : member.getMemberName();
    }

    private ChronicDiseaseProfileDTO buildProfileDTO(HealthChronicDiseaseProfileEntity profile,
        Map<String, ChronicDiseaseTypeDTO> diseaseTypeMap, Map<Long, String> memberNameMap) {
        ChronicDiseaseTypeDTO diseaseTypeDTO = diseaseTypeMap.get(profile.getDiseaseCode());
        return new ChronicDiseaseProfileDTO(profile, diseaseTypeDTO, memberNameMap.get(profile.getMemberId()));
    }

    private List<HealthReportEntity> listRecentReports(Long memberId) {
        return reportService.lambdaQuery()
            .eq(HealthReportEntity::getMemberId, memberId)
            .orderByDesc(HealthReportEntity::getReportDate)
            .orderByDesc(HealthReportEntity::getReportId)
            .page(new Page<>(1, DASHBOARD_REPORT_LIMIT))
            .getRecords();
    }

    private Date resolveLatestReportDate(List<HealthReportEntity> reports) {
        if (reports == null || reports.isEmpty()) {
            return null;
        }
        return reports.get(0).getReportDate();
    }

    private List<ChronicDiseaseIndicatorTrendDTO> buildIndicatorTrends(List<HealthReportEntity> reports,
        ChronicDiseaseTypeDTO diseaseTypeDTO, Map<String, HealthChronicIndicatorTargetEntity> indicatorTargetMap) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> reportIds = reports.stream()
            .map(HealthReportEntity::getReportId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (reportIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, HealthReportEntity> reportMap = reports.stream()
            .collect(Collectors.toMap(HealthReportEntity::getReportId, item -> item, (left, right) -> left));
        List<HealthReportItemEntity> allItems = reportItemService.lambdaQuery()
            .in(HealthReportItemEntity::getReportId, reportIds)
            .orderByAsc(HealthReportItemEntity::getReportId)
            .orderByAsc(HealthReportItemEntity::getSort)
            .orderByAsc(HealthReportItemEntity::getItemId)
            .list();

        Set<String> codeSet = diseaseTypeDTO.getFocusIndicatorCodes().stream()
            .filter(StrUtil::isNotBlank)
            .map(this::normalizeMatchText)
            .collect(Collectors.toCollection(HashSet::new));
        List<String> keywords = diseaseTypeDTO.getFocusIndicatorKeywords().stream()
            .filter(StrUtil::isNotBlank)
            .map(this::normalizeMatchText)
            .collect(Collectors.toList());

        Map<String, List<HealthReportItemEntity>> matchedItemsByKey = new LinkedHashMap<>();
        for (HealthReportItemEntity item : allItems) {
            if (!matchesDiseaseFocus(item, codeSet, keywords)) {
                continue;
            }
            String indicatorKey = resolveIndicatorKey(item);
            matchedItemsByKey.computeIfAbsent(indicatorKey, key -> new ArrayList<>()).add(item);
        }

        List<ChronicDiseaseIndicatorTrendDTO> trends = new ArrayList<>();
        for (List<HealthReportItemEntity> items : matchedItemsByKey.values()) {
            items.sort(Comparator
                .comparing((HealthReportItemEntity item) -> safeReportDate(reportMap.get(item.getReportId())))
                .thenComparing(HealthReportItemEntity::getReportId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(HealthReportItemEntity::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(HealthReportItemEntity::getItemId, Comparator.nullsLast(Long::compareTo)));
            HealthChronicIndicatorTargetEntity target = indicatorTargetMap.get(
                normalizeIndicatorCode(resolveIndicatorKey(items.get(items.size() - 1))));
            trends.add(buildTrendDTO(items, reportMap, target));
        }
        return trends;
    }

    private List<HealthChronicIndicatorTargetEntity> listActiveIndicatorTargets(Long profileId) {
        if (profileId == null) {
            return Collections.emptyList();
        }
        return indicatorTargetService.lambdaQuery()
            .eq(HealthChronicIndicatorTargetEntity::getProfileId, profileId)
            .eq(HealthChronicIndicatorTargetEntity::getStatus, StatusEnum.ENABLE.getValue())
            .list();
    }

    private Map<String, HealthChronicIndicatorTargetEntity> buildIndicatorTargetMap(
        List<HealthChronicIndicatorTargetEntity> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, HealthChronicIndicatorTargetEntity> targetMap = new HashMap<>();
        for (HealthChronicIndicatorTargetEntity target : targets) {
            String indicatorCode = normalizeIndicatorCode(target.getIndicatorCode());
            if (StrUtil.isBlank(indicatorCode)) {
                continue;
            }
            targetMap.put(indicatorCode, target);
        }
        return targetMap;
    }

    private void validateIndicatorTargetCommand(SaveChronicIndicatorTargetCommand command) {
        if (command == null || StrUtil.isBlank(command.getIndicatorCode())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "指标编码不能为空");
        }
        boolean hasNumberRange = command.getTargetMin() != null || command.getTargetMax() != null;
        if (!hasNumberRange && StrUtil.isBlank(command.getTargetText())) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_INDICATOR_TARGET_EMPTY);
        }
        if (command.getTargetMin() != null && command.getTargetMax() != null
            && command.getTargetMin().compareTo(command.getTargetMax()) > 0) {
            throw new ApiException(ErrorCode.Business.APP_CHRONIC_INDICATOR_TARGET_RANGE_INVALID);
        }
    }

    private void fillIndicatorTargetEntity(HealthChronicIndicatorTargetEntity entity,
        SaveChronicIndicatorTargetCommand command, String indicatorCode) {
        entity.setIndicatorCode(indicatorCode);
        entity.setIndicatorName(limitLength(StrUtil.blankToDefault(StrUtil.trim(command.getIndicatorName()), indicatorCode), 100));
        entity.setTargetMin(command.getTargetMin());
        entity.setTargetMax(command.getTargetMax());
        entity.setTargetText(limitLength(StrUtil.trim(command.getTargetText()), 200));
        entity.setResultUnit(limitLength(StrUtil.trim(command.getResultUnit()), 50));
        entity.setStatus(command.getStatus() == null ? StatusEnum.ENABLE.getValue() : command.getStatus());
        entity.setRemark(limitLength(StrUtil.trim(command.getRemark()), 500));
    }

    /**
     * 目标范围和趋势指标都走同一套编码归一化，避免大小写、前后空格导致目标无法命中。
     */
    private String normalizeIndicatorCode(String value) {
        return StrUtil.blankToDefault(value, "").trim().toUpperCase(Locale.ROOT);
    }

    private void fillTrendTargetResult(ChronicDiseaseIndicatorTrendDTO dto, HealthReportItemEntity currentItem,
        HealthChronicIndicatorTargetEntity target) {
        if (target == null) {
            return;
        }
        ChronicIndicatorTargetDTO targetDTO = new ChronicIndicatorTargetDTO(target);
        dto.setTarget(targetDTO);
        String status = resolveTargetStatus(currentItem, target);
        dto.setTargetStatus(status);
        dto.setTargetStatusName(resolveTargetStatusName(status));
        dto.setTargetSummary(resolveTargetSummary(currentItem, target, status));
    }

    private String resolveTargetStatus(HealthReportItemEntity currentItem, HealthChronicIndicatorTargetEntity target) {
        if (target.getTargetMin() == null && target.getTargetMax() == null) {
            return "TEXT_ONLY";
        }
        Double parsedValue = parseNumber(currentItem == null ? null : currentItem.getResultValue());
        if (parsedValue == null) {
            return "UNMEASURABLE";
        }
        BigDecimal currentValue = BigDecimal.valueOf(parsedValue);
        if (target.getTargetMin() != null && currentValue.compareTo(target.getTargetMin()) < 0) {
            return "BELOW";
        }
        if (target.getTargetMax() != null && currentValue.compareTo(target.getTargetMax()) > 0) {
            return "ABOVE";
        }
        return "IN_RANGE";
    }

    private String resolveTargetStatusName(String status) {
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
            return "目标说明";
        }
        return "暂无法判断";
    }

    private String resolveTargetSummary(HealthReportItemEntity currentItem, HealthChronicIndicatorTargetEntity target,
        String status) {
        String currentText = currentItem == null
            ? "-"
            : StrUtil.format("{}{}", StrUtil.blankToDefault(currentItem.getResultValue(), "-"),
                StrUtil.blankToDefault(currentItem.getResultUnit(), ""));
        String targetText = buildTargetRangeText(target);
        if (Objects.equals(status, "IN_RANGE")) {
            return StrUtil.format("最新值 {} 已进入个人目标范围：{}。", currentText, targetText);
        }
        if (Objects.equals(status, "BELOW")) {
            return StrUtil.format("最新值 {} 低于个人目标范围：{}，建议结合医生建议确认是否需要调整。", currentText, targetText);
        }
        if (Objects.equals(status, "ABOVE")) {
            return StrUtil.format("最新值 {} 高于个人目标范围：{}，建议优先关注复查和干预执行。", currentText, targetText);
        }
        if (Objects.equals(status, "TEXT_ONLY")) {
            return StrUtil.format("该指标已维护目标说明：{}。", targetText);
        }
        return StrUtil.format("该指标已维护个人目标：{}，但最新值 {} 暂无法量化比对。", targetText, currentText);
    }

    private String buildTargetRangeText(HealthChronicIndicatorTargetEntity target) {
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

    private boolean matchesDiseaseFocus(HealthReportItemEntity item, Set<String> codeSet, List<String> keywords) {
        if (item == null) {
            return false;
        }
        String standardCode = normalizeMatchText(item.getStandardItemCode());
        String itemCode = normalizeMatchText(item.getItemCode());
        if ((!codeSet.isEmpty() && (codeSet.contains(standardCode) || codeSet.contains(itemCode)))) {
            return true;
        }
        if (keywords.isEmpty()) {
            return false;
        }
        String itemName = normalizeMatchText(item.getItemName());
        return keywords.stream().anyMatch(keyword -> itemName.contains(keyword));
    }

    private String normalizeMatchText(String value) {
        return StrUtil.blankToDefault(value, "").trim().toUpperCase(Locale.ROOT);
    }

    private String resolveIndicatorKey(HealthReportItemEntity item) {
        return StrUtil.blankToDefault(item.getStandardItemCode(),
            StrUtil.blankToDefault(item.getItemCode(), item.getItemName()));
    }

    private Date safeReportDate(HealthReportEntity report) {
        return report == null || report.getReportDate() == null ? new Date(0L) : report.getReportDate();
    }

    private ChronicDiseaseIndicatorTrendDTO buildTrendDTO(List<HealthReportItemEntity> items,
        Map<Long, HealthReportEntity> reportMap, HealthChronicIndicatorTargetEntity target) {
        HealthReportItemEntity currentItem = items.get(items.size() - 1);
        HealthReportItemEntity previousItem = items.size() > 1 ? items.get(items.size() - 2) : null;

        ChronicDiseaseIndicatorTrendDTO dto = new ChronicDiseaseIndicatorTrendDTO();
        dto.setIndicatorCode(resolveIndicatorKey(currentItem));
        dto.setIndicatorName(currentItem.getItemName());
        dto.setResultUnit(currentItem.getResultUnit());
        dto.setCurrentResultValue(currentItem.getResultValue());
        dto.setCurrentAbnormalFlag(currentItem.getAbnormalFlag());
        dto.setCurrentAbnormalFlagName(resolveAbnormalFlagName(currentItem.getAbnormalFlag()));
        dto.setChangeDirection(resolveChangeDirection(currentItem, previousItem));
        dto.setChangeSummary(resolveChangeSummary(currentItem, previousItem));
        fillTrendTargetResult(dto, currentItem, target);
        dto.setPoints(items.stream()
            .map(item -> buildTrendPointDTO(item, reportMap.get(item.getReportId())))
            .collect(Collectors.toList()));
        return dto;
    }

    private ChronicDiseaseIndicatorPointDTO buildTrendPointDTO(HealthReportItemEntity item, HealthReportEntity report) {
        ChronicDiseaseIndicatorPointDTO dto = new ChronicDiseaseIndicatorPointDTO();
        dto.setReportId(item.getReportId());
        dto.setReportDate(report == null ? null : report.getReportDate());
        dto.setResultValue(item.getResultValue());
        dto.setResultUnit(item.getResultUnit());
        dto.setAbnormalFlag(item.getAbnormalFlag());
        dto.setAbnormalFlagName(resolveAbnormalFlagName(item.getAbnormalFlag()));
        return dto;
    }

    private String resolveChangeDirection(HealthReportItemEntity currentItem, HealthReportItemEntity previousItem) {
        Double currentValue = parseNumber(currentItem == null ? null : currentItem.getResultValue());
        Double previousValue = parseNumber(previousItem == null ? null : previousItem.getResultValue());
        if (currentValue == null || previousValue == null) {
            return previousItem == null ? "NO_BASELINE" : "UNCHANGED";
        }
        if (Math.abs(currentValue - previousValue) < 0.000001D) {
            return "UNCHANGED";
        }
        return currentValue > previousValue ? "UP" : "DOWN";
    }

    private String resolveChangeSummary(HealthReportItemEntity currentItem, HealthReportItemEntity previousItem) {
        if (previousItem == null) {
            return "当前仅有一条可比报告数据，建议继续补充后续检查形成趋势。";
        }
        String direction = resolveChangeDirection(currentItem, previousItem);
        if (Objects.equals(direction, "UP")) {
            return StrUtil.format("相较上次 {}，本次 {} 呈上升趋势。", previousItem.getResultValue(), currentItem.getResultValue());
        }
        if (Objects.equals(direction, "DOWN")) {
            return StrUtil.format("相较上次 {}，本次 {} 呈下降趋势。", previousItem.getResultValue(), currentItem.getResultValue());
        }
        return StrUtil.format("相较上次 {}，本次 {} 变化不明显或暂无法量化。", previousItem.getResultValue(), currentItem.getResultValue());
    }

    private Double parseNumber(String resultValue) {
        if (StrUtil.isBlank(resultValue)) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(resultValue);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group());
        } catch (NumberFormatException ignored) {
            return null;
        }
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

    private List<ChronicDiseaseRelatedProblemDTO> listLinkedProblems(HealthChronicDiseaseProfileEntity profile) {
        List<HealthProblemChronicProfileLinkEntity> links = problemChronicProfileLinkService.lambdaQuery()
            .eq(HealthProblemChronicProfileLinkEntity::getProfileId, profile.getProfileId())
            .orderByDesc(HealthProblemChronicProfileLinkEntity::getLinkTime)
            .orderByDesc(HealthProblemChronicProfileLinkEntity::getLinkId)
            .list();
        if (links.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> problemIds = links.stream()
            .map(HealthProblemChronicProfileLinkEntity::getProblemId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (problemIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, HealthProblemEntity> problemMap = problemService.listByIds(problemIds).stream()
            .filter(problem -> Objects.equals(problem.getMemberId(), profile.getMemberId()))
            .collect(Collectors.toMap(HealthProblemEntity::getProblemId, Function.identity(), (left, right) -> left,
                LinkedHashMap::new));
        return links.stream()
            .map(link -> buildRelatedProblemDTO(link, problemMap.get(link.getProblemId())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private ChronicDiseaseRelatedProblemDTO buildRelatedProblemDTO(HealthProblemChronicProfileLinkEntity link,
        HealthProblemEntity problem) {
        if (problem == null) {
            return null;
        }
        ChronicDiseaseRelatedProblemDTO dto = new ChronicDiseaseRelatedProblemDTO();
        dto.setLinkId(link.getLinkId());
        dto.setProblemId(problem.getProblemId());
        dto.setProblemName(problem.getProblemName());
        dto.setProblemType(problem.getProblemType());
        dto.setProblemStatus(problem.getProblemStatus());
        dto.setProblemStatusName(resolveProblemStatusName(problem.getProblemStatus()));
        dto.setRiskLevel(problem.getRiskLevel());
        dto.setRiskLevelName(resolveProblemRiskLevelName(problem.getRiskLevel()));
        dto.setSummary(problem.getSummary());
        dto.setFirstFoundDate(problem.getFirstFoundDate());
        dto.setLastFollowDate(problem.getLastFollowDate());
        dto.setLinkRemark(link.getLinkRemark());
        dto.setLinkTime(link.getLinkTime());
        return dto;
    }
    private int countRelatedActiveProblems(Long memberId, ChronicDiseaseTypeDTO diseaseTypeDTO) {
        List<HealthProblemEntity> problems = problemService.lambdaQuery()
            .eq(HealthProblemEntity::getMemberId, memberId)
            .ne(HealthProblemEntity::getProblemStatus, 3)
            .list();
        if (problems.isEmpty()) {
            return 0;
        }
        Set<String> codeSet = diseaseTypeDTO.getFocusIndicatorCodes().stream()
            .filter(StrUtil::isNotBlank)
            .map(this::normalizeMatchText)
            .collect(Collectors.toSet());
        List<String> keywords = diseaseTypeDTO.getFocusIndicatorKeywords().stream()
            .filter(StrUtil::isNotBlank)
            .map(this::normalizeMatchText)
            .collect(Collectors.toList());
        long count = problems.stream()
            .filter(problem -> matchesProblemFocus(problem, codeSet, keywords))
            .count();
        return (int) count;
    }

    private boolean matchesProblemFocus(HealthProblemEntity problem, Set<String> codeSet, List<String> keywords) {
        String standardCode = normalizeMatchText(problem.getStandardItemCode());
        if (!codeSet.isEmpty() && codeSet.contains(standardCode)) {
            return true;
        }
        String problemName = normalizeMatchText(problem.getProblemName());
        return keywords.stream().anyMatch(keyword -> problemName.contains(keyword));
    }

    private int countActiveMedicationPlans(Long memberId) {
        Long count = medicationPlanService.lambdaQuery()
            .eq(HealthMedicationPlanEntity::getMemberId, memberId)
            .eq(HealthMedicationPlanEntity::getStatus, StatusEnum.ENABLE.getValue())
            .count();
        return count == null ? 0 : count.intValue();
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
        return "未知状态";
    }

    private String resolveProblemRiskLevelName(Integer riskLevel) {
        if (Objects.equals(riskLevel, 3)) {
            return "高风险";
        }
        if (Objects.equals(riskLevel, 2)) {
            return "中风险";
        }
        return "低风险";
    }
    private String buildDashboardSummary(ChronicDiseaseProfileDTO profileDTO, ChronicDiseaseDashboardDTO dashboardDTO) {
        List<String> parts = new ArrayList<>();
        parts.add(StrUtil.format("{}当前处于{}状态", profileDTO.getDiseaseName(), profileDTO.getProfileStatusName()));
        if (dashboardDTO.getFocusIndicatorCount() > 0) {
            parts.add(StrUtil.format("已关联{}项重点指标趋势", dashboardDTO.getFocusIndicatorCount()));
        } else {
            parts.add("暂未从最近报告中命中重点指标，建议补充报告或完善病种指标配置");
        }
        if (dashboardDTO.getIndicatorTargetCount() > 0) {
            parts.add(StrUtil.format("已维护{}项个人指标目标", dashboardDTO.getIndicatorTargetCount()));
        }
        if (dashboardDTO.getAbnormalProblemCount() > 0) {
            parts.add(StrUtil.format("仍有{}条相关健康问题需要持续跟进", dashboardDTO.getAbnormalProblemCount()));
        }
        if (dashboardDTO.getActiveMedicationPlanCount() > 0) {
            parts.add(StrUtil.format("该成员当前有{}条启用中的用药计划", dashboardDTO.getActiveMedicationPlanCount()));
        }
        return String.join("；", parts) + "。";
    }
    private String limitLength(String text, int maxLength) {
        if (StrUtil.isBlank(text) || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

}





