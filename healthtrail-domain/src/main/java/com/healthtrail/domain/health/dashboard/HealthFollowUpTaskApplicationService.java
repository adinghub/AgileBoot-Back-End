package com.healthtrail.domain.health.dashboard;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageReadStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskActionTypeEnum;
import com.healthtrail.common.enums.health.HealthFollowUpDelayOptionEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskTypeEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.dashboard.command.BatchFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.CompleteFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.DelayFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.IgnoreFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.ReadFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.RestoreFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.db.HealthFollowUpTaskLogEntity;
import com.healthtrail.domain.health.dashboard.db.HealthFollowUpTaskLogService;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskService;
import com.healthtrail.domain.health.dashboard.db.HealthFollowUpTaskEntity;
import com.healthtrail.domain.health.dashboard.db.HealthFollowUpTaskService;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskActionTrendPointDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskAnalyticsDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskAnalyticsItemDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskBatchResultDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskBatchSkippedItemDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskDetailDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskLogDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpRecommendedActionDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskDTO;
import com.healthtrail.domain.health.dashboard.query.HealthFollowUpTaskQuery;
import com.healthtrail.domain.health.chronic.ChronicDiseaseApplicationService;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileEntity;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseProfileService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import com.healthtrail.domain.health.problem.db.HealthProblemEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemService;
import com.healthtrail.domain.health.problem.db.HealthProblemStatusLogEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemStatusLogService;
import com.healthtrail.domain.health.report.HealthReportApplicationService;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemService;
import com.healthtrail.domain.health.report.db.HealthReportService;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceItemDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 首页待跟进任务应用服务。
 *
 * <p>该服务负责处理首页任务流的用户操作行为，
 * 例如：
 * 1. 延后任务
 * 2. 完成本轮跟进
 *
 * <p>这些操作只作用于首页任务流本身，不会直接篡改原始提醒或报告数据。
 */
@Service
@RequiredArgsConstructor
public class HealthFollowUpTaskApplicationService {

    /**
     * 健康问题复查任务目标业务类型。
     *
     * <p>健康问题复查任务同样复用 `operation_task` 承载，完成动作需要靠这个稳定值识别
     * “当前运营任务是否应该回写健康问题最近跟进时间”。该值必须和健康洞察创建复查任务时写入的
     * `targetBizType` 保持一致。
     */
    private static final String TARGET_BIZ_TYPE_HEALTH_PROBLEM_REVIEW = "HEALTH_PROBLEM_REVIEW";

    /**
     * 健康问题复查任务完成后写入状态时间线的动作类型。
     *
     * <p>这里不会自动修改健康问题状态，只记录“用户完成了一次复查跟进”。
     * 问题从跟进中变为已缓解或已关闭仍然需要用户在健康问题模块显式操作。
     */
    private static final String PROBLEM_REVIEW_TASK_COMPLETE_ACTION = "REVIEW_TASK_COMPLETE";

    /**
     * 业务回写备注长度上限。
     *
     * <p>首页任务主记录的备注上限是 255，但慢病档案和健康问题备注字段通常更宽。
     * 这里仍保留 500 的硬边界，避免用户长备注直接撑大业务主表字段。
     */
    private static final int BUSINESS_REVIEW_REMARK_MAX_LENGTH = 500;

    /** 首页待跟进任务数据库服务 */
    private final HealthFollowUpTaskService followUpTaskService;

    /** 首页待跟进任务操作日志数据库服务 */
    private final HealthFollowUpTaskLogService followUpTaskLogService;

    /** 用药提醒数据库服务 */
    private final HealthMedicationReminderService medicationReminderService;

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 体检报告数据库服务 */
    private final HealthReportService reportService;

    /** 体检报告指标项数据库服务 */
    private final HealthReportItemService reportItemService;

    /** 体检报告应用服务 */
    private final HealthReportApplicationService healthReportApplicationService;

    /** 首页运营任务数据库服务 */
    private final HealthOperationTaskService operationTaskService;

    /** 慢病专项档案数据库服务 */
    private final HealthChronicDiseaseProfileService chronicDiseaseProfileService;

    /** 健康问题数据库服务 */
    private final HealthProblemService healthProblemService;

    /** 健康问题状态日志数据库服务 */
    private final HealthProblemStatusLogService healthProblemStatusLogService;

    /** Spring 编程式事务模板 */
    private final TransactionTemplate transactionTemplate;

    /**
     * 查询任务中心列表。
     *
     * <p>当前任务中心只展示“已经产生过操作记录”的任务，
     * 这样用户可以查看自己已经延后、忽略、完成过的任务，并选择恢复。
     */
    public List<HealthFollowUpTaskDTO> getTaskList(HealthFollowUpTaskQuery query, Long ownerUserId) {
        List<HealthFollowUpTaskEntity> taskEntities = followUpTaskService.lambdaQuery()
            .eq(HealthFollowUpTaskEntity::getOwnerUserId, ownerUserId)
            .eq(StrUtil.isNotBlank(query.getTaskType()), HealthFollowUpTaskEntity::getTaskType, query.getTaskType())
            .eq(query.getTaskStatus() != null, HealthFollowUpTaskEntity::getTaskStatus, query.getTaskStatus())
            .orderByAsc(HealthFollowUpTaskEntity::getReadStatus)
            .orderByDesc(HealthFollowUpTaskEntity::getUpdateTime)
            .orderByDesc(HealthFollowUpTaskEntity::getTaskId)
            .list();
        // 任务中心是典型的列表聚合场景。
        // 这里先把成员、提醒、报告、运营任务等来源数据按类型批量预加载，
        // 再统一构建 DTO，避免原来每条任务重复 `getById()` / `count()` 导致的 N+1 查询。
        TaskListBatchContext batchContext = buildTaskListBatchContext(taskEntities);
        return taskEntities.stream()
            .map(taskEntity -> buildTaskDTO(taskEntity, batchContext))
            .collect(Collectors.toList());
    }

    /**
     * 查询单条任务详情。
     *
     * <p>该能力面向任务中心详情页，
     * 一次性返回：
     * 1. 当前任务基础信息
     * 2. 当前还能执行哪些操作
     * 3. 历史操作时间线
     *
     * <p>这样前端无需先拉详情、再拉日志、再自己推断按钮显隐。
     */
    public HealthFollowUpTaskDetailDTO getTaskDetail(Long taskId, Long ownerUserId) {
        HealthFollowUpTaskEntity taskEntity = loadOwnedTask(taskId, ownerUserId);
        // 任务详情页原先会围绕同一条来源记录反复执行：
        // 1. buildTaskDTO -> getById
        // 2. isTaskSourceAvailable -> getById
        // 3. buildRecommendedAction -> getById / getReportAdvice
        //
        // 对提醒、报告、运营任务三种来源来说，这会把“单条详情查看”放大成多次重复查库。
        // 这里改成先把详情需要的来源快照一次性预热，再在后续 DTO 组装阶段统一复用。
        TaskDetailContext detailContext = buildTaskDetailContext(taskEntity, ownerUserId);
        HealthFollowUpTaskDTO taskDTO = buildTaskDTO(taskEntity, detailContext);
        boolean sourceAvailable = detailContext.isSourceAvailable();
        HealthFollowUpRecommendedActionDTO recommendedAction =
            buildRecommendedAction(taskEntity, taskDTO, detailContext);
        if (recommendedAction != null && recommendedAction.getNavigation() != null) {
            // 详情页里优先让基础导航与推荐动作导航保持一致，
            // 避免前端同时拿到两个不同的页面内落点建议。
            taskDTO.setNavigation(recommendedAction.getNavigation());
        }

        HealthFollowUpTaskDetailDTO detailDTO = new HealthFollowUpTaskDetailDTO();
        detailDTO.setTaskInfo(taskDTO);
        detailDTO.setSourceAvailable(sourceAvailable);
        detailDTO.setCanDelay(sourceAvailable);
        detailDTO.setCanComplete(sourceAvailable && canCompleteTaskSource(taskEntity, detailContext));
        detailDTO.setCanIgnore(sourceAvailable);
        detailDTO.setCanRestore(sourceAvailable && Boolean.TRUE.equals(taskDTO.getCanRestore()));
        detailDTO.setRecommendedAction(recommendedAction);
        detailDTO.setTaskLogs(listTaskLogs(taskEntity.getTaskId()));
        return detailDTO;
    }

    /**
     * 查询指定任务的操作日志时间线。
     *
     * <p>这里按任务记录ID查询，而不是按来源业务ID查询，原因是：
     * 1. 任务中心列表已经天然持有 `taskId`
     * 2. 后续如果任务附加更多展示属性，仍然能围绕唯一任务记录持续扩展
     */
    public List<HealthFollowUpTaskLogDTO> getTaskLogs(Long taskId, Long ownerUserId) {
        HealthFollowUpTaskEntity taskEntity = loadOwnedTask(taskId, ownerUserId);
        return listTaskLogs(taskEntity.getTaskId());
    }

    /**
     * 标记任务已读。
     *
     * <p>首页任务流的已读状态不影响底层提醒或报告状态，
     * 只用于首页排序和“用户是否已经看过这件事”的展示语义。
     */
    @Transactional(rollbackFor = Exception.class)
    public void markTaskRead(ReadFollowUpTaskCommand readCommand, Long ownerUserId) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = resolveTaskType(readCommand.getTaskType());
        TaskSourceSnapshot sourceSnapshot = loadTaskSource(taskTypeEnum, readCommand.getSourceId(), ownerUserId);
        HealthFollowUpTaskEntity taskEntity = loadOrCreateTask(ownerUserId, taskTypeEnum.getValue(), readCommand.getSourceId());
        Integer beforeStatus = taskEntity.getTaskStatus();
        Integer beforeReadStatus = taskEntity.getReadStatus();
        taskEntity.setOwnerUserId(ownerUserId);
        taskEntity.setMemberId(sourceSnapshot.getMemberId());
        taskEntity.setTaskType(taskTypeEnum.getValue());
        taskEntity.setSourceId(readCommand.getSourceId());
        if (taskEntity.getTaskStatus() == null) {
            taskEntity.setTaskStatus(HealthFollowUpTaskStatusEnum.PENDING.getValue());
        }
        taskEntity.setReadStatus(HealthAppMessageReadStatusEnum.READ.getValue());
        taskEntity.setReadTime(new Date());
        saveOrUpdateTask(taskEntity);
        if (!Objects.equals(beforeReadStatus, HealthAppMessageReadStatusEnum.READ.getValue())) {
            saveTaskLog(taskEntity, HealthFollowUpTaskActionTypeEnum.READ, beforeStatus, "任务已读");
        }
    }

    /**
     * 延后首页待跟进任务。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delayTask(DelayFollowUpTaskCommand delayCommand, Long ownerUserId) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = resolveTaskType(delayCommand.getTaskType());
        Date delayedUntil = resolveDelayedUntil(delayCommand);
        if (delayedUntil == null || !delayedUntil.after(new Date())) {
            throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_DELAY_TIME_INVALID);
        }

        TaskSourceSnapshot sourceSnapshot = loadTaskSource(taskTypeEnum, delayCommand.getSourceId(), ownerUserId);
        HealthFollowUpTaskEntity taskEntity = loadOrCreateTask(ownerUserId, taskTypeEnum.getValue(), delayCommand.getSourceId());
        Integer beforeStatus = taskEntity.getTaskStatus();
        taskEntity.setOwnerUserId(ownerUserId);
        taskEntity.setMemberId(sourceSnapshot.getMemberId());
        taskEntity.setTaskType(taskTypeEnum.getValue());
        taskEntity.setSourceId(delayCommand.getSourceId());
        taskEntity.setTaskStatus(HealthFollowUpTaskStatusEnum.DELAYED.getValue());
        taskEntity.setReadStatus(HealthAppMessageReadStatusEnum.READ.getValue());
        taskEntity.setReadTime(new Date());
        taskEntity.setDelayedUntil(delayedUntil);
        taskEntity.setCompleteTime(null);
        taskEntity.setActionRemark(limitLength(delayCommand.getRemark(), 255));
        saveOrUpdateTask(taskEntity);
        saveTaskLog(taskEntity, HealthFollowUpTaskActionTypeEnum.DELAY, beforeStatus);
    }

    /**
     * 完成本轮首页待跟进任务。
     *
     * <p>提醒任务仍然必须回到提醒模块，通过“已服药 / 已跳过”等动作闭环。
     * 报告建议任务只更新首页任务流；慢病复查和健康问题复查任务会额外回写对应业务记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(CompleteFollowUpTaskCommand completeCommand, Long ownerUserId) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = resolveTaskType(completeCommand.getTaskType());
        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_ACTION_NOT_ALLOWED);
        }

        TaskSourceSnapshot sourceSnapshot = loadTaskSource(taskTypeEnum, completeCommand.getSourceId(), ownerUserId);
        HealthFollowUpTaskEntity taskEntity = loadOrCreateTask(ownerUserId, taskTypeEnum.getValue(), completeCommand.getSourceId());
        Integer beforeStatus = taskEntity.getTaskStatus();
        taskEntity.setOwnerUserId(ownerUserId);
        taskEntity.setMemberId(sourceSnapshot.getMemberId());
        taskEntity.setTaskType(taskTypeEnum.getValue());
        taskEntity.setSourceId(completeCommand.getSourceId());
        taskEntity.setTaskStatus(HealthFollowUpTaskStatusEnum.COMPLETED.getValue());
        taskEntity.setReadStatus(HealthAppMessageReadStatusEnum.READ.getValue());
        taskEntity.setReadTime(new Date());
        taskEntity.setDelayedUntil(null);
        Date completeTime = new Date();
        taskEntity.setCompleteTime(completeTime);
        taskEntity.setActionRemark(limitLength(completeCommand.getRemark(), 255));
        saveOrUpdateTask(taskEntity);
        saveTaskLog(taskEntity, HealthFollowUpTaskActionTypeEnum.COMPLETE, beforeStatus);
        completeOperationTaskBusinessLoop(taskTypeEnum, sourceSnapshot, completeTime, taskEntity.getActionRemark(), ownerUserId);
    }

    /**
     * 忽略首页待跟进任务。
     *
     * <p>忽略比“完成”更宽松：
     * 1. 它既适用于提醒任务，也适用于报告建议任务
     * 2. 只代表首页暂不再展示，不代表底层业务已完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void ignoreTask(IgnoreFollowUpTaskCommand ignoreCommand, Long ownerUserId) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = resolveTaskType(ignoreCommand.getTaskType());

        TaskSourceSnapshot sourceSnapshot = loadTaskSource(taskTypeEnum, ignoreCommand.getSourceId(), ownerUserId);
        HealthFollowUpTaskEntity taskEntity = loadOrCreateTask(ownerUserId, taskTypeEnum.getValue(), ignoreCommand.getSourceId());
        Integer beforeStatus = taskEntity.getTaskStatus();
        taskEntity.setOwnerUserId(ownerUserId);
        taskEntity.setMemberId(sourceSnapshot.getMemberId());
        taskEntity.setTaskType(taskTypeEnum.getValue());
        taskEntity.setSourceId(ignoreCommand.getSourceId());
        taskEntity.setTaskStatus(HealthFollowUpTaskStatusEnum.IGNORED.getValue());
        taskEntity.setReadStatus(HealthAppMessageReadStatusEnum.READ.getValue());
        taskEntity.setReadTime(new Date());
        taskEntity.setDelayedUntil(null);
        taskEntity.setCompleteTime(null);
        taskEntity.setActionRemark(limitLength(ignoreCommand.getRemark(), 255));
        saveOrUpdateTask(taskEntity);
        saveTaskLog(taskEntity, HealthFollowUpTaskActionTypeEnum.IGNORE, beforeStatus);
    }

    /**
     * 恢复首页待跟进任务。
     *
     * <p>恢复后会把任务状态改回“待跟进”，并清空延后/完成时间，
     * 使其重新参与首页任务流展示。
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreTask(RestoreFollowUpTaskCommand restoreCommand, Long ownerUserId) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = resolveTaskType(restoreCommand.getTaskType());

        TaskSourceSnapshot sourceSnapshot = loadTaskSource(taskTypeEnum, restoreCommand.getSourceId(), ownerUserId);
        HealthFollowUpTaskEntity taskEntity = loadOrCreateTask(ownerUserId, taskTypeEnum.getValue(), restoreCommand.getSourceId());
        Integer beforeStatus = taskEntity.getTaskStatus();
        taskEntity.setOwnerUserId(ownerUserId);
        taskEntity.setMemberId(sourceSnapshot.getMemberId());
        taskEntity.setTaskType(taskTypeEnum.getValue());
        taskEntity.setSourceId(restoreCommand.getSourceId());
        taskEntity.setTaskStatus(HealthFollowUpTaskStatusEnum.PENDING.getValue());
        taskEntity.setReadStatus(HealthAppMessageReadStatusEnum.READ.getValue());
        taskEntity.setReadTime(new Date());
        taskEntity.setDelayedUntil(null);
        taskEntity.setCompleteTime(null);
        taskEntity.setActionRemark(limitLength(restoreCommand.getRemark(), 255));
        saveOrUpdateTask(taskEntity);
        saveTaskLog(taskEntity, HealthFollowUpTaskActionTypeEnum.RESTORE, beforeStatus);
    }

    /**
     * 查询首页任务行为分析摘要。
     *
     * <p>分析结果既服务首页轻量卡片，也服务任务中心完整分析区。
     * 因此这里一次性返回：
     * 1. 当前任务状态分布
     * 2. 累计行为次数
     * 3. 最近几天的行为趋势
     */
    public HealthFollowUpTaskAnalyticsDTO getTaskAnalytics(Long ownerUserId, Integer recentDays) {
        int safeRecentDays = recentDays == null ? 7 : Math.max(1, Math.min(recentDays, 30));
        // 旧实现会把当前账号下全部任务、全部任务日志一次性拉到内存再统计。
        // 随着历史任务和日志持续累积，这条首页链路会越来越重。
        //
        // 这里改成“数据库先聚合、服务层再组装”：
        // 1. 任务表只按 task_type/task_status/read_status 聚合
        // 2. 日志表只按 action_type 聚合累计值
        // 3. 趋势图只查询最近 N 天、且按日期 + action_type 聚合
        //
        // 这样可以把原来的“全量实体扫描”收敛成几条固定成本的聚合 SQL。
        List<Map<String, Object>> taskAggregateRows = loadTaskAggregateRows(ownerUserId);
        List<Map<String, Object>> actionAggregateRows = loadActionAggregateRows(ownerUserId);
        List<Map<String, Object>> recentTrendRows = loadRecentActionTrendRows(ownerUserId, safeRecentDays);

        HealthFollowUpTaskAnalyticsDTO analyticsDTO = new HealthFollowUpTaskAnalyticsDTO();
        fillTaskAnalyticsOverview(analyticsDTO, taskAggregateRows);
        fillTaskAnalyticsActionCounts(analyticsDTO, actionAggregateRows);
        analyticsDTO.setReadRate(calculateRate(
            analyticsDTO.getTotalTaskCount() - analyticsDTO.getUnreadTaskCount(), analyticsDTO.getTotalTaskCount()));
        analyticsDTO.setCompletionRate(calculateRate(
            analyticsDTO.getCompletedTaskCount(), analyticsDTO.getTotalTaskCount()));
        analyticsDTO.setRecentDays(safeRecentDays);
        analyticsDTO.setTaskTypeStats(buildTaskTypeStats(taskAggregateRows));
        analyticsDTO.setRecentActionTrend(buildRecentActionTrend(recentTrendRows, safeRecentDays));
        return analyticsDTO;
    }

    /**
     * 批量标记任务已读。
     */
    public HealthFollowUpTaskBatchResultDTO batchReadTasks(BatchFollowUpTaskCommand batchCommand, Long ownerUserId) {
        return executeBatch(batchCommand, ownerUserId, taskEntity -> {
            ReadFollowUpTaskCommand readCommand = new ReadFollowUpTaskCommand();
            readCommand.setTaskType(taskEntity.getTaskType());
            readCommand.setSourceId(taskEntity.getSourceId());
            markTaskRead(readCommand, ownerUserId);
        });
    }

    /**
     * 批量忽略任务。
     */
    public HealthFollowUpTaskBatchResultDTO batchIgnoreTasks(BatchFollowUpTaskCommand batchCommand, Long ownerUserId) {
        return executeBatch(batchCommand, ownerUserId, taskEntity -> {
            IgnoreFollowUpTaskCommand ignoreCommand = new IgnoreFollowUpTaskCommand();
            ignoreCommand.setTaskType(taskEntity.getTaskType());
            ignoreCommand.setSourceId(taskEntity.getSourceId());
            ignoreCommand.setRemark(batchCommand.getRemark());
            ignoreTask(ignoreCommand, ownerUserId);
        });
    }

    /**
     * 批量恢复任务。
     */
    public HealthFollowUpTaskBatchResultDTO batchRestoreTasks(BatchFollowUpTaskCommand batchCommand, Long ownerUserId) {
        return executeBatch(batchCommand, ownerUserId, taskEntity -> {
            RestoreFollowUpTaskCommand restoreCommand = new RestoreFollowUpTaskCommand();
            restoreCommand.setTaskType(taskEntity.getTaskType());
            restoreCommand.setSourceId(taskEntity.getSourceId());
            restoreCommand.setRemark(batchCommand.getRemark());
            restoreTask(restoreCommand, ownerUserId);
        });
    }

    /**
     * 批量完成任务。
     *
     * <p>当前底层仍沿用单条完成动作的规则，
     * 因此提醒类任务会自动进入“跳过项”而不是导致整批失败。
     */
    public HealthFollowUpTaskBatchResultDTO batchCompleteTasks(BatchFollowUpTaskCommand batchCommand, Long ownerUserId) {
        return executeBatch(batchCommand, ownerUserId, taskEntity -> {
            CompleteFollowUpTaskCommand completeCommand = new CompleteFollowUpTaskCommand();
            completeCommand.setTaskType(taskEntity.getTaskType());
            completeCommand.setSourceId(taskEntity.getSourceId());
            completeCommand.setRemark(batchCommand.getRemark());
            completeTask(completeCommand, ownerUserId);
        });
    }

    /**
     * 解析任务类型。
     */
    private HealthFollowUpTaskTypeEnum resolveTaskType(String taskType) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskType);
        if (taskTypeEnum == null) {
            throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_TYPE_INVALID);
        }
        return taskTypeEnum;
    }

    /**
     * 加载任务来源快照，并校验归属关系。
     */
    private TaskSourceSnapshot loadTaskSource(HealthFollowUpTaskTypeEnum taskTypeEnum, Long sourceId, Long ownerUserId) {
        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            HealthMedicationReminderEntity reminderEntity = medicationReminderService.getById(sourceId);
            if (reminderEntity == null || !ownerUserId.equals(reminderEntity.getOwnerUserId())) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, sourceId, "首页提醒任务");
            }
            return TaskSourceSnapshot.ofMember(reminderEntity.getMemberId());
        }

        if (HealthFollowUpTaskTypeEnum.REPORT_ADVICE == taskTypeEnum) {
            HealthReportEntity reportEntity = reportService.getById(sourceId);
            if (reportEntity == null || !ownerUserId.equals(reportEntity.getOwnerUserId())) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, sourceId, "首页报告任务");
            }
            return TaskSourceSnapshot.ofMember(reportEntity.getMemberId());
        }

        if (HealthFollowUpTaskTypeEnum.OPERATION == taskTypeEnum) {
            HealthOperationTaskEntity operationTaskEntity = operationTaskService.getById(sourceId);
            if (operationTaskEntity == null || !ownerUserId.equals(operationTaskEntity.getOwnerUserId())) {
                throw new ApiException(ErrorCode.Business.HEALTH_OPERATION_TASK_NOT_FOUND);
            }
            return TaskSourceSnapshot.ofOperation(operationTaskEntity);
        }

        throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_TYPE_INVALID);
    }

    /**
     * 按“用户 + 任务类型 + 来源业务ID”加载已有任务记录。
     * 如果不存在，则返回一个新的空实体，后续直接填充即可。
     */
    private HealthFollowUpTaskEntity loadOrCreateTask(Long ownerUserId, String taskType, Long sourceId) {
        HealthFollowUpTaskEntity taskEntity = followUpTaskService.lambdaQuery()
            .eq(HealthFollowUpTaskEntity::getOwnerUserId, ownerUserId)
            .eq(HealthFollowUpTaskEntity::getTaskType, taskType)
            .eq(HealthFollowUpTaskEntity::getSourceId, sourceId)
            .one();
        return taskEntity == null ? new HealthFollowUpTaskEntity() : taskEntity;
    }

    /**
     * 按任务ID加载当前用户拥有的任务记录。
     *
     * <p>该校验主要用于任务日志查询场景，防止用户越权查看别人的任务时间线。
     */
    private HealthFollowUpTaskEntity loadOwnedTask(Long taskId, Long ownerUserId) {
        HealthFollowUpTaskEntity taskEntity = followUpTaskService.lambdaQuery()
            .eq(HealthFollowUpTaskEntity::getTaskId, taskId)
            .eq(HealthFollowUpTaskEntity::getOwnerUserId, ownerUserId)
            .one();
        if (taskEntity == null) {
            throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_NOT_FOUND);
        }
        return taskEntity;
    }

    /**
     * 查询任务日志列表。
     */
    private List<HealthFollowUpTaskLogDTO> listTaskLogs(Long taskId) {
        List<HealthFollowUpTaskLogEntity> logEntities = followUpTaskLogService.lambdaQuery()
            .eq(HealthFollowUpTaskLogEntity::getTaskId, taskId)
            .orderByDesc(HealthFollowUpTaskLogEntity::getCreateTime)
            .orderByDesc(HealthFollowUpTaskLogEntity::getLogId)
            .list();
        return logEntities.stream().map(this::buildTaskLogDTO).collect(Collectors.toList());
    }

    /**
     * 统一保存或更新任务记录。
     */
    private void saveOrUpdateTask(HealthFollowUpTaskEntity taskEntity) {
        if (taskEntity.getTaskStatus() == null) {
            taskEntity.setTaskStatus(HealthFollowUpTaskStatusEnum.PENDING.getValue());
        }
        if (taskEntity.getReadStatus() == null) {
            taskEntity.setReadStatus(HealthAppMessageReadStatusEnum.UNREAD.getValue());
        }
        if (taskEntity.getTaskId() == null) {
            taskEntity.setDeleted(0);
            followUpTaskService.save(taskEntity);
            return;
        }
        followUpTaskService.updateById(taskEntity);
    }

    /**
     * 记录首页任务操作日志。
     *
     * <p>日志使用“追加写入”方式保存，
     * 这样任何一次延后、忽略、恢复、完成都能被完整回溯。
     */
    private void saveTaskLog(HealthFollowUpTaskEntity taskEntity, HealthFollowUpTaskActionTypeEnum actionTypeEnum,
        Integer beforeStatus) {
        saveTaskLog(taskEntity, actionTypeEnum, beforeStatus, taskEntity.getActionRemark());
    }

    /**
     * 记录首页任务操作日志，并允许单独指定本次日志备注。
     *
     * <p>之所以保留这个重载，是因为“已读”行为不会改写任务主记录里的 `actionRemark`，
     * 如果直接沿用主记录备注，就会把历史延后 / 忽略备注错误带入“已读”日志。
     */
    private void saveTaskLog(HealthFollowUpTaskEntity taskEntity, HealthFollowUpTaskActionTypeEnum actionTypeEnum,
        Integer beforeStatus, String actionRemark) {
        HealthFollowUpTaskLogEntity logEntity = new HealthFollowUpTaskLogEntity();
        logEntity.setTaskId(taskEntity.getTaskId());
        logEntity.setOwnerUserId(taskEntity.getOwnerUserId());
        logEntity.setMemberId(taskEntity.getMemberId());
        logEntity.setTaskType(taskEntity.getTaskType());
        logEntity.setSourceId(taskEntity.getSourceId());
        logEntity.setActionType(actionTypeEnum.getValue());
        logEntity.setBeforeStatus(beforeStatus);
        logEntity.setAfterStatus(taskEntity.getTaskStatus());
        logEntity.setActionRemark(actionRemark);
        logEntity.setDelayedUntilSnapshot(taskEntity.getDelayedUntil());
            logEntity.setDeleted(0);
        followUpTaskLogService.save(logEntity);
    }

    /**
     * 完成复查类运营任务后的业务回写。
     *
     * <p>首页任务流负责“用户已经处理了这个待办”，但慢病专项和健康问题还需要自己的业务时间字段。
     * 只有完成动作会触发这里，延后、忽略、恢复都只改变首页任务状态，避免用户只是暂缓处理时误改业务档案。
     */
    private void completeOperationTaskBusinessLoop(HealthFollowUpTaskTypeEnum taskTypeEnum,
        TaskSourceSnapshot sourceSnapshot, Date completeTime, String actionRemark, Long ownerUserId) {
        if (HealthFollowUpTaskTypeEnum.OPERATION != taskTypeEnum || sourceSnapshot == null) {
            return;
        }
        HealthOperationTaskEntity operationTask = sourceSnapshot.getOperationTaskEntity();
        if (!isReviewOperationTask(operationTask)) {
            return;
        }
        completeChronicReviewIfNeeded(operationTask, completeTime, actionRemark, ownerUserId);
        completeHealthProblemReviewIfNeeded(operationTask, completeTime, actionRemark, ownerUserId);
        disableReviewOperationTaskAfterCompletion(operationTask, completeTime, ownerUserId);
    }

    /**
     * 慢病复查完成后回写专项最近复盘时间。
     *
     * <p>这里不自动修改专项状态，原因是“已复查”不等于“病情稳定”或“专项关闭”。
     * 用户如果需要变更专项状态，仍然应在慢病专项页显式调整。
     */
    private void completeChronicReviewIfNeeded(HealthOperationTaskEntity operationTask, Date completeTime,
        String actionRemark, Long ownerUserId) {
        if (!Objects.equals(operationTask.getTargetBizType(), ChronicDiseaseApplicationService.TARGET_BIZ_TYPE_CHRONIC_REVIEW)
            || operationTask.getTargetBizId() == null) {
            return;
        }
        HealthChronicDiseaseProfileEntity profile = chronicDiseaseProfileService.getById(operationTask.getTargetBizId());
        if (profile == null || !Objects.equals(profile.getOwnerUserId(), ownerUserId)) {
            return;
        }
        if (operationTask.getMemberId() != null && !Objects.equals(operationTask.getMemberId(), profile.getMemberId())) {
            return;
        }
        profile.setLastReviewDate(completeTime);
        if (StrUtil.isNotBlank(actionRemark)) {
            profile.setRemark(limitLength(actionRemark, BUSINESS_REVIEW_REMARK_MAX_LENGTH));
        }
        chronicDiseaseProfileService.updateById(profile);
    }

    /**
     * 健康问题复查完成后回写最近跟进时间，并补一条处理时间线。
     *
     * <p>状态时间线使用前后相同的状态值，明确表达“这是一条复查完成记录”，
     * 而不是把健康问题自动从跟进中改成已缓解。
     */
    private void completeHealthProblemReviewIfNeeded(HealthOperationTaskEntity operationTask, Date completeTime,
        String actionRemark, Long ownerUserId) {
        if (!Objects.equals(operationTask.getTargetBizType(), TARGET_BIZ_TYPE_HEALTH_PROBLEM_REVIEW)
            || operationTask.getTargetBizId() == null) {
            return;
        }
        HealthProblemEntity problem = healthProblemService.getById(operationTask.getTargetBizId());
        if (problem == null || !Objects.equals(problem.getOwnerUserId(), ownerUserId)) {
            return;
        }
        if (operationTask.getMemberId() != null && !Objects.equals(operationTask.getMemberId(), problem.getMemberId())) {
            return;
        }
        Integer beforeStatus = problem.getProblemStatus();
        problem.setLastFollowDate(completeTime);
        if (StrUtil.isNotBlank(actionRemark)) {
            problem.setRemark(limitLength(actionRemark, BUSINESS_REVIEW_REMARK_MAX_LENGTH));
        }
        healthProblemService.updateById(problem);
        saveHealthProblemReviewCompleteLog(problem, beforeStatus, actionRemark, completeTime, ownerUserId);
    }

    /**
     * 写入健康问题复查完成时间线。
     */
    private void saveHealthProblemReviewCompleteLog(HealthProblemEntity problem, Integer beforeStatus,
        String actionRemark, Date completeTime, Long ownerUserId) {
        HealthProblemStatusLogEntity log = new HealthProblemStatusLogEntity();
        log.setProblemId(problem.getProblemId());
        log.setOwnerUserId(problem.getOwnerUserId());
        log.setMemberId(problem.getMemberId());
        log.setOperatorUserId(ownerUserId);
        log.setActionType(PROBLEM_REVIEW_TASK_COMPLETE_ACTION);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(beforeStatus);
        log.setActionRemark(StrUtil.blankToDefault(StrUtil.trim(actionRemark), "复查任务已完成"));
        log.setActionTime(completeTime);
        log.setDeleted(0);
        healthProblemStatusLogService.save(log);
    }

    /**
     * 复查任务完成后停用原运营任务。
     *
     * <p>首页展示会同时读取 `follow_up_task` 和 `operation_task`。如果只把首页任务记录标记为完成，
     * 慢病专项页仍可能从启用中的运营任务里读到这条复查提醒。停用源任务可以避免同一轮复查反复出现，
     * 也允许用户后续重新创建下一轮复查任务。
     */
    private void disableReviewOperationTaskAfterCompletion(HealthOperationTaskEntity operationTask, Date completeTime,
        Long ownerUserId) {
        operationTask.setStatus(StatusEnum.DISABLE.getValue());
        operationTask.setEndTime(completeTime);
        operationTask.setUpdaterId(ownerUserId);
        operationTask.setUpdateTime(completeTime);
        operationTaskService.updateById(operationTask);
    }

    /**
     * 统一执行批量任务操作。
     *
     * <p>这里使用“逐条单独事务”策略，确保：
     * 1. 单条失败不会影响其它任务
     * 2. 每条成功任务仍然保留完整日志
     * 3. 前端能够拿到明确的成功 / 跳过结果
     */
    private HealthFollowUpTaskBatchResultDTO executeBatch(BatchFollowUpTaskCommand batchCommand, Long ownerUserId,
        BatchTaskExecutor executor) {
        List<Long> taskIds = normalizeTaskIds(batchCommand == null ? null : batchCommand.getTaskIds());
        List<Long> successTaskIds = new ArrayList<>();
        List<HealthFollowUpTaskBatchSkippedItemDTO> skippedItems = new ArrayList<>();

        for (Long taskId : taskIds) {
            HealthFollowUpTaskEntity taskEntity;
            try {
                taskEntity = loadOwnedTask(taskId, ownerUserId);
            } catch (ApiException ex) {
                skippedItems.add(buildSkippedItem(taskId, ex.getMessage()));
                continue;
            }

            try {
                transactionTemplate.executeWithoutResult(status -> executor.execute(taskEntity));
                successTaskIds.add(taskId);
            } catch (ApiException ex) {
                skippedItems.add(buildSkippedItem(taskId, ex.getMessage()));
            } catch (Exception ex) {
                skippedItems.add(buildSkippedItem(taskId, "批量操作执行失败"));
            }
        }

        HealthFollowUpTaskBatchResultDTO resultDTO = new HealthFollowUpTaskBatchResultDTO();
        resultDTO.setRequestedCount(taskIds.size());
        resultDTO.setSuccessCount(successTaskIds.size());
        resultDTO.setSkippedCount(skippedItems.size());
        resultDTO.setSuccessTaskIds(successTaskIds);
        resultDTO.setSkippedItems(skippedItems);
        return resultDTO;
    }

    /**
     * 去重并过滤空值任务ID。
     */
    private List<Long> normalizeTaskIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(taskIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * 构建批量跳过项。
     */
    private HealthFollowUpTaskBatchSkippedItemDTO buildSkippedItem(Long taskId, String reason) {
        HealthFollowUpTaskBatchSkippedItemDTO skippedItemDTO = new HealthFollowUpTaskBatchSkippedItemDTO();
        skippedItemDTO.setTaskId(taskId);
        skippedItemDTO.setReason(StrUtil.blankToDefault(reason, "批量操作未执行"));
        return skippedItemDTO;
    }

    /**
     * 解析真正生效的延后时间。
     *
     * <p>当前优先级规则：
     * 1. 如果前端直接传 `delayedUntil`，优先使用该时间
     * 2. 否则尝试从 `delayOption` 解析快捷时间
     */
    private Date resolveDelayedUntil(DelayFollowUpTaskCommand delayCommand) {
        if (delayCommand.getDelayedUntil() != null) {
            return delayCommand.getDelayedUntil();
        }
        if (StrUtil.isBlank(delayCommand.getDelayOption())) {
            throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_DELAY_PARAM_REQUIRED);
        }

        HealthFollowUpDelayOptionEnum delayOptionEnum = HealthFollowUpDelayOptionEnum.fromValue(delayCommand.getDelayOption());
        if (delayOptionEnum == null) {
            throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_DELAY_OPTION_INVALID);
        }

        Date now = new Date();
        if (HealthFollowUpDelayOptionEnum.AFTER_30_MINUTES == delayOptionEnum) {
            return DateUtil.offsetMinute(now, 30);
        }
        if (HealthFollowUpDelayOptionEnum.TONIGHT == delayOptionEnum) {
            Date tonight = DateUtil.parse(DateUtil.formatDate(now) + " 20:00:00");
            return tonight.after(now) ? tonight : DateUtil.offsetDay(tonight, 1);
        }
        if (HealthFollowUpDelayOptionEnum.TOMORROW_MORNING == delayOptionEnum) {
            Date tomorrow = DateUtil.offsetDay(now, 1);
            return DateUtil.parse(DateUtil.formatDate(tomorrow) + " 08:00:00");
        }
        throw new ApiException(ErrorCode.Business.APP_FOLLOW_UP_TASK_DELAY_OPTION_INVALID);
    }

    /**
     * 对操作备注做长度保护。
     */
    private String limitLength(String remark, int maxLength) {
        if (StrUtil.isBlank(remark) || remark.length() <= maxLength) {
            return remark;
        }
        return remark.substring(0, maxLength);
    }

    /**
     * 构建任务中心 DTO。
     *
     * <p>这里会按任务类型回填来源对象的标题和摘要，
     * 让任务中心不仅能看状态，也能看懂当前到底是哪条任务。
     */
    private HealthFollowUpTaskDTO buildTaskDTO(HealthFollowUpTaskEntity taskEntity, TaskDetailContext detailContext) {
        HealthFollowUpTaskDTO taskDTO = new HealthFollowUpTaskDTO();
        taskDTO.setTaskId(taskEntity.getTaskId());
        taskDTO.setTaskType(taskEntity.getTaskType());
        taskDTO.setTaskTypeName(resolveTaskTypeName(taskEntity.getTaskType()));
        taskDTO.setSourceId(taskEntity.getSourceId());
        taskDTO.setTaskStatus(taskEntity.getTaskStatus());
        taskDTO.setTaskStatusName(resolveTaskStatusName(taskEntity.getTaskStatus()));
        taskDTO.setMemberId(taskEntity.getMemberId());
        taskDTO.setMemberCode(resolveMemberCode(detailContext.getMemberMap(), taskEntity.getMemberId()));
        taskDTO.setMemberName(resolveMemberName(detailContext.getMemberMap(), taskEntity.getMemberId()));
        taskDTO.setActionRemark(taskEntity.getActionRemark());
        taskDTO.setReadStatus(taskEntity.getReadStatus());
        taskDTO.setReadTime(taskEntity.getReadTime());
        taskDTO.setDelayedUntil(taskEntity.getDelayedUntil());
        taskDTO.setCompleteTime(taskEntity.getCompleteTime());
        taskDTO.setCanRestore(!Objects.equals(taskEntity.getTaskStatus(), HealthFollowUpTaskStatusEnum.PENDING.getValue()));
        taskDTO.setSortReason(Objects.equals(taskEntity.getReadStatus(), HealthAppMessageReadStatusEnum.UNREAD.getValue())
            ? "当前任务尚未阅读，列表中优先展示"
            : "当前任务已读，排序会适当后移");

        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskEntity.getTaskType());
        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            taskDTO.setNavigation(buildNavigation(taskEntity.getTaskType(), taskEntity.getSourceId(), null));
            fillReminderTaskDTO(taskDTO, detailContext.getReminderEntity());
        } else if (HealthFollowUpTaskTypeEnum.REPORT_ADVICE == taskTypeEnum) {
            taskDTO.setNavigation(buildNavigation(taskEntity.getTaskType(), taskEntity.getSourceId(), null));
            fillReportTaskDTO(taskDTO, detailContext.getReportEntity(), detailContext.getReportAbnormalCount());
        } else if (HealthFollowUpTaskTypeEnum.OPERATION == taskTypeEnum) {
            HealthOperationTaskEntity operationTaskEntity = detailContext.getOperationTaskEntity();
            taskDTO.setNavigation(buildOperationNavigation(operationTaskEntity, taskEntity.getSourceId()));
            fillOperationTaskDTO(taskDTO, operationTaskEntity);
        } else {
            taskDTO.setNavigation(buildNavigation(taskEntity.getTaskType(), taskEntity.getSourceId(), null));
        }
        return taskDTO;
    }

    /**
     * 构建任务中心 DTO。
     *
     * <p>这个重载方法专门服务列表场景：
     * 调用方会先把所需的成员、提醒、报告、运营任务一次性查出，
     * 这里再从批量上下文里读取对应快照，避免 DTO 构建过程中产生额外 SQL。
     */
    private HealthFollowUpTaskDTO buildTaskDTO(HealthFollowUpTaskEntity taskEntity, TaskListBatchContext batchContext) {
        HealthFollowUpTaskDTO taskDTO = new HealthFollowUpTaskDTO();
        taskDTO.setTaskId(taskEntity.getTaskId());
        taskDTO.setTaskType(taskEntity.getTaskType());
        taskDTO.setTaskTypeName(resolveTaskTypeName(taskEntity.getTaskType()));
        taskDTO.setSourceId(taskEntity.getSourceId());
        taskDTO.setTaskStatus(taskEntity.getTaskStatus());
        taskDTO.setTaskStatusName(resolveTaskStatusName(taskEntity.getTaskStatus()));
        taskDTO.setMemberId(taskEntity.getMemberId());
        taskDTO.setMemberCode(resolveMemberCode(batchContext.getMemberMap(), taskEntity.getMemberId()));
        taskDTO.setMemberName(resolveMemberName(batchContext.getMemberMap(), taskEntity.getMemberId()));
        taskDTO.setActionRemark(taskEntity.getActionRemark());
        taskDTO.setReadStatus(taskEntity.getReadStatus());
        taskDTO.setReadTime(taskEntity.getReadTime());
        taskDTO.setDelayedUntil(taskEntity.getDelayedUntil());
        taskDTO.setCompleteTime(taskEntity.getCompleteTime());
        taskDTO.setCanRestore(!Objects.equals(taskEntity.getTaskStatus(), HealthFollowUpTaskStatusEnum.PENDING.getValue()));
        taskDTO.setSortReason(Objects.equals(taskEntity.getReadStatus(), HealthAppMessageReadStatusEnum.UNREAD.getValue())
            ? "当前任务尚未阅读，列表中优先展示"
            : "当前任务已读，排序会适当后移");

        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskEntity.getTaskType());
        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            taskDTO.setNavigation(buildNavigation(taskEntity.getTaskType(), taskEntity.getSourceId(), null));
            fillReminderTaskDTO(taskDTO, batchContext.getReminderMap().get(taskEntity.getSourceId()));
        } else if (HealthFollowUpTaskTypeEnum.REPORT_ADVICE == taskTypeEnum) {
            taskDTO.setNavigation(buildNavigation(taskEntity.getTaskType(), taskEntity.getSourceId(), null));
            fillReportTaskDTO(taskDTO,
                batchContext.getReportMap().get(taskEntity.getSourceId()),
                batchContext.getReportAbnormalCountMap().getOrDefault(taskEntity.getSourceId(), 0L));
        } else if (HealthFollowUpTaskTypeEnum.OPERATION == taskTypeEnum) {
            HealthOperationTaskEntity operationTaskEntity = batchContext.getOperationTaskMap().get(taskEntity.getSourceId());
            taskDTO.setNavigation(buildOperationNavigation(operationTaskEntity, taskEntity.getSourceId()));
            fillOperationTaskDTO(taskDTO, operationTaskEntity);
        } else {
            taskDTO.setNavigation(buildNavigation(taskEntity.getTaskType(), taskEntity.getSourceId(), null));
        }
        return taskDTO;
    }

    /**
     * 构建任务日志 DTO。
     *
     * <p>任务中心详情页只需要稳定的展示数据，
     * 因此这里把枚举说明在服务端直接翻译好，减少 App 端自行维护文案映射。
     */
    private HealthFollowUpTaskLogDTO buildTaskLogDTO(HealthFollowUpTaskLogEntity logEntity) {
        HealthFollowUpTaskLogDTO logDTO = new HealthFollowUpTaskLogDTO();
        logDTO.setLogId(logEntity.getLogId());
        logDTO.setTaskId(logEntity.getTaskId());
        logDTO.setActionType(logEntity.getActionType());
        logDTO.setActionTypeName(resolveActionTypeName(logEntity.getActionType()));
        logDTO.setBeforeStatus(logEntity.getBeforeStatus());
        logDTO.setBeforeStatusName(resolveTaskStatusName(logEntity.getBeforeStatus()));
        logDTO.setAfterStatus(logEntity.getAfterStatus());
        logDTO.setAfterStatusName(resolveTaskStatusName(logEntity.getAfterStatus()));
        logDTO.setActionRemark(logEntity.getActionRemark());
        logDTO.setDelayedUntilSnapshot(logEntity.getDelayedUntilSnapshot());
        logDTO.setActionTime(logEntity.getCreateTime());
        return logDTO;
    }

    /**
     * 回填提醒任务来源信息。
     */
    private void fillReminderTaskDTO(HealthFollowUpTaskDTO taskDTO, Long reminderId) {
        fillReminderTaskDTO(taskDTO, medicationReminderService.getById(reminderId));
    }

    /**
     * 用已经预加载好的提醒实体回填任务摘要。
     */
    private void fillReminderTaskDTO(HealthFollowUpTaskDTO taskDTO, HealthMedicationReminderEntity reminderEntity) {
        if (reminderEntity == null) {
            taskDTO.setTitle("原提醒已不存在");
            taskDTO.setContent("该提醒来源记录已不存在，仅保留任务操作记录。");
            return;
        }
        taskDTO.setTitle(StrUtil.blankToDefault(reminderEntity.getDrugNameSnapshot(), "用药提醒"));
        taskDTO.setContent(StrUtil.format("计划时间 {}，当前提醒状态为 {}。",
            reminderEntity.getScheduledTime() == null ? "未设置" : DateUtil.format(reminderEntity.getScheduledTime(), "yyyy-MM-dd HH:mm"),
            resolveReminderStatusName(reminderEntity.getReminderStatus())));
        taskDTO.setSourceTime(reminderEntity.getScheduledTime());
    }

    /**
     * 回填报告建议任务来源信息。
     */
    private void fillReportTaskDTO(HealthFollowUpTaskDTO taskDTO, Long reportId) {
        HealthReportEntity reportEntity = reportService.getById(reportId);
        long abnormalCount = 0L;
        if (reportEntity != null) {
            abnormalCount = reportItemService.lambdaQuery()
                .eq(HealthReportItemEntity::getReportId, reportId)
                .in(HealthReportItemEntity::getAbnormalFlag,
                    HealthReportItemAbnormalFlagEnum.LOW.getValue(),
                    HealthReportItemAbnormalFlagEnum.HIGH.getValue(),
                    HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue())
                .count();
        }
        fillReportTaskDTO(taskDTO, reportEntity, abnormalCount);
    }

    /**
     * 用已经预加载好的报告实体和异常数量回填任务摘要。
     */
    private void fillReportTaskDTO(HealthFollowUpTaskDTO taskDTO, HealthReportEntity reportEntity, long abnormalCount) {
        if (reportEntity == null) {
            taskDTO.setTitle("原报告已不存在");
            taskDTO.setContent("该报告来源记录已不存在，仅保留任务操作记录。");
            return;
        }
        taskDTO.setTitle(StrUtil.blankToDefault(reportEntity.getReportName(), "体检报告跟进"));
        taskDTO.setContent(StrUtil.isBlank(reportEntity.getAnalysisSummary())
            ? StrUtil.format("该报告存在 {} 项异常指标，建议继续跟进。", abnormalCount)
            : reportEntity.getAnalysisSummary());
        taskDTO.setSourceTime(reportEntity.getReportDate() == null ? reportEntity.getCreateTime() : reportEntity.getReportDate());
    }

    /**
     * 回填运营任务来源信息。
     */
    private void fillOperationTaskDTO(HealthFollowUpTaskDTO taskDTO, Long operationTaskId) {
        fillOperationTaskDTO(taskDTO, operationTaskService.getById(operationTaskId));
        if (taskDTO.getNavigation() == null) {
            taskDTO.setNavigation(buildNavigation(HealthFollowUpTaskTypeEnum.OPERATION.getValue(), operationTaskId, null));
        }
    }

    /**
     * 用已经预加载好的运营任务实体回填任务摘要。
     */
    private void fillOperationTaskDTO(HealthFollowUpTaskDTO taskDTO, HealthOperationTaskEntity operationTaskEntity) {
        if (operationTaskEntity == null) {
            taskDTO.setTitle("原运营任务已不存在");
            taskDTO.setContent("该运营任务来源记录已不存在，仅保留任务操作记录。");
            return;
        }
        taskDTO.setTitle(StrUtil.blankToDefault(operationTaskEntity.getTaskTitle(), "首页运营任务"));
        taskDTO.setContent(StrUtil.blankToDefault(operationTaskEntity.getTaskContent(), "请根据任务引导查看对应业务页。"));
        taskDTO.setSourceTime(operationTaskEntity.getStartTime());
    }

    /**
     * 回填任务类型名称。
     */
    private String resolveTaskTypeName(String taskType) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskType);
        return taskTypeEnum == null ? null : HealthAppI18n.followUpTaskTypeName(taskTypeEnum.getValue());
    }

    /**
     * 判断当前任务是否为报告建议任务。
     */
    private boolean isTaskTypeReportAdvice(String taskType) {
        return Objects.equals(taskType, HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue());
    }

    /**
     * 判断任务详情页是否可以展示“本轮已处理”。
     *
     * <p>提醒任务必须走提醒模块自己的服药/跳过动作，不能在任务中心直接完成。
     * 这里额外放开复查类运营任务，是因为它们本身就是“安排一次复查”的任务，
     * 用户点击完成后需要推动慢病档案或健康问题进入下一轮跟进状态。
     */
    private boolean canCompleteTaskSource(HealthFollowUpTaskEntity taskEntity, TaskDetailContext detailContext) {
        if (taskEntity == null) {
            return false;
        }
        if (isTaskTypeReportAdvice(taskEntity.getTaskType())) {
            return true;
        }
        if (!Objects.equals(taskEntity.getTaskType(), HealthFollowUpTaskTypeEnum.OPERATION.getValue())) {
            return false;
        }
        return detailContext != null && isReviewOperationTask(detailContext.getOperationTaskEntity());
    }

    /**
     * 判断运营任务是否属于复查闭环。
     *
     * <p>后台仍然可以投放普通运营任务，这些任务只需要进入业务页查看，不应该因为用户点了完成
     * 就写入慢病或健康问题主表。因此这里只识别两个明确的复查目标类型。
     */
    private boolean isReviewOperationTask(HealthOperationTaskEntity operationTask) {
        if (operationTask == null) {
            return false;
        }
        return Objects.equals(operationTask.getTargetBizType(), ChronicDiseaseApplicationService.TARGET_BIZ_TYPE_CHRONIC_REVIEW)
            || Objects.equals(operationTask.getTargetBizType(), TARGET_BIZ_TYPE_HEALTH_PROBLEM_REVIEW);
    }

    /**
     * 回填任务状态名称。
     */
    private String resolveTaskStatusName(Integer taskStatus) {
        if (taskStatus == null) {
            return null;
        }
        for (HealthFollowUpTaskStatusEnum statusEnum : HealthFollowUpTaskStatusEnum.values()) {
            if (Objects.equals(statusEnum.getValue(), taskStatus)) {
        return HealthAppI18n.followUpTaskStatusName(statusEnum.getValue());
            }
        }
        return null;
    }

    /**
     * 回填操作类型名称。
     */
    private String resolveActionTypeName(String actionType) {
        HealthFollowUpTaskActionTypeEnum actionTypeEnum = HealthFollowUpTaskActionTypeEnum.fromValue(actionType);
        return actionTypeEnum == null ? null : HealthAppI18n.followUpTaskActionName(actionTypeEnum.getValue());
    }

    /**
     * 统计未读任务数量。
     */
    private int countUnreadTasks(List<HealthFollowUpTaskEntity> taskEntities) {
        return (int) taskEntities.stream()
            .filter(taskEntity -> Objects.equals(taskEntity.getReadStatus(), HealthAppMessageReadStatusEnum.UNREAD.getValue()))
            .count();
    }

    /**
     * 统计指定状态的任务数量。
     */
    private int countTaskStatus(List<HealthFollowUpTaskEntity> taskEntities, Integer taskStatus) {
        return (int) taskEntities.stream()
            .filter(taskEntity -> Objects.equals(taskEntity.getTaskStatus(), taskStatus))
            .count();
    }

    /**
     * 统计指定动作出现次数。
     */
    private int countActionType(List<HealthFollowUpTaskLogEntity> taskLogEntities,
        HealthFollowUpTaskActionTypeEnum actionTypeEnum) {
        return (int) taskLogEntities.stream()
            .filter(logEntity -> Objects.equals(logEntity.getActionType(), actionTypeEnum.getValue()))
            .count();
    }

    /**
     * 构建按任务类型聚合的统计结果。
     */
    private List<HealthFollowUpTaskAnalyticsItemDTO> buildTaskTypeStats(List<Map<String, Object>> taskAggregateRows) {
        if (taskAggregateRows == null || taskAggregateRows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, HealthFollowUpTaskAnalyticsItemDTO> taskTypeStatsMap = new HashMap<>();
        for (Map<String, Object> row : taskAggregateRows) {
            String taskType = toStringValue(row.get("taskType"));
            int taskCount = toIntValue(row.get("taskCount"));
            Integer readStatus = toIntegerValue(row.get("readStatus"));
            Integer taskStatus = toIntegerValue(row.get("taskStatus"));
            HealthFollowUpTaskAnalyticsItemDTO itemDTO = taskTypeStatsMap.computeIfAbsent(taskType, key -> {
                HealthFollowUpTaskAnalyticsItemDTO dto = new HealthFollowUpTaskAnalyticsItemDTO();
                dto.setTaskType(key);
                dto.setTaskTypeName(resolveTaskTypeName(key));
                dto.setTaskCount(0);
                dto.setUnreadCount(0);
                dto.setPendingCount(0);
                dto.setDelayedCount(0);
                dto.setCompletedCount(0);
                dto.setIgnoredCount(0);
                return dto;
            });
            itemDTO.setTaskCount(itemDTO.getTaskCount() + taskCount);
            if (Objects.equals(readStatus, HealthAppMessageReadStatusEnum.UNREAD.getValue())) {
                itemDTO.setUnreadCount(itemDTO.getUnreadCount() + taskCount);
            }
            if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.PENDING.getValue())) {
                itemDTO.setPendingCount(itemDTO.getPendingCount() + taskCount);
            } else if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.DELAYED.getValue())) {
                itemDTO.setDelayedCount(itemDTO.getDelayedCount() + taskCount);
            } else if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.COMPLETED.getValue())) {
                itemDTO.setCompletedCount(itemDTO.getCompletedCount() + taskCount);
            } else if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.IGNORED.getValue())) {
                itemDTO.setIgnoredCount(itemDTO.getIgnoredCount() + taskCount);
            }
        }
        return taskTypeStatsMap.values().stream()
            .sorted((left, right) -> Integer.compare(
                Objects.requireNonNullElse(right.getTaskCount(), 0),
                Objects.requireNonNullElse(left.getTaskCount(), 0)))
            .collect(Collectors.toList());
    }

    /**
     * 构建最近若干天的行为趋势。
     *
     * <p>这里即使某一天没有任何操作，也会补一条 0 值记录，
     * 这样前端图表渲染时不会出现日期断层。
     */
    private List<HealthFollowUpTaskActionTrendPointDTO> buildRecentActionTrend(List<Map<String, Object>> trendRows,
        int recentDays) {
        if (recentDays <= 0) {
            return Collections.emptyList();
        }

        // 先把最近 N 天的骨架补齐成连续日期，确保前端图表始终不会断层。
        Map<String, HealthFollowUpTaskActionTrendPointDTO> trendPointMap = new HashMap<>();
        List<HealthFollowUpTaskActionTrendPointDTO> trendPoints = new ArrayList<>();
        for (int offset = recentDays - 1; offset >= 0; offset--) {
            Date statDate = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -offset));
            HealthFollowUpTaskActionTrendPointDTO pointDTO = new HealthFollowUpTaskActionTrendPointDTO();
            pointDTO.setStatDate(DateUtil.formatDate(statDate));
            pointDTO.setReadActionCount(0);
            pointDTO.setDelayActionCount(0);
            pointDTO.setCompleteActionCount(0);
            pointDTO.setIgnoreActionCount(0);
            pointDTO.setRestoreActionCount(0);
            pointDTO.setTotalActionCount(0);
            trendPoints.add(pointDTO);
            trendPointMap.put(pointDTO.getStatDate(), pointDTO);
        }

        if (trendRows == null || trendRows.isEmpty()) {
            return trendPoints;
        }
        for (Map<String, Object> row : trendRows) {
            String statDate = normalizeStatDate(row.get("statDate"));
            HealthFollowUpTaskActionTrendPointDTO pointDTO = trendPointMap.get(statDate);
            if (pointDTO == null) {
                continue;
            }
            int actionCount = toIntValue(row.get("actionCount"));
            String actionType = toStringValue(row.get("actionType"));
            pointDTO.setTotalActionCount(pointDTO.getTotalActionCount() + actionCount);
            if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.READ.getValue())) {
                pointDTO.setReadActionCount(pointDTO.getReadActionCount() + actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.DELAY.getValue())) {
                pointDTO.setDelayActionCount(pointDTO.getDelayActionCount() + actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.COMPLETE.getValue())) {
                pointDTO.setCompleteActionCount(pointDTO.getCompleteActionCount() + actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.IGNORE.getValue())) {
                pointDTO.setIgnoreActionCount(pointDTO.getIgnoreActionCount() + actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.RESTORE.getValue())) {
                pointDTO.setRestoreActionCount(pointDTO.getRestoreActionCount() + actionCount);
            }
        }
        return trendPoints;
    }

    /**
     * 读取任务统计聚合行。
     */
    private List<Map<String, Object>> loadTaskAggregateRows(Long ownerUserId) {
        QueryWrapper<HealthFollowUpTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("task_type as taskType",
                "task_status as taskStatus",
                "read_status as readStatus",
                "count(*) as taskCount")
            .eq("owner_user_id", ownerUserId)
            .groupBy("task_type", "task_status", "read_status");
        return followUpTaskService.listMaps(queryWrapper);
    }

    /**
     * 读取动作累计统计聚合行。
     */
    private List<Map<String, Object>> loadActionAggregateRows(Long ownerUserId) {
        QueryWrapper<HealthFollowUpTaskLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("action_type as actionType", "count(*) as actionCount")
            .eq("owner_user_id", ownerUserId)
            .groupBy("action_type");
        return followUpTaskLogService.listMaps(queryWrapper);
    }

    /**
     * 读取最近 N 天的行为趋势聚合行。
     */
    private List<Map<String, Object>> loadRecentActionTrendRows(Long ownerUserId, int recentDays) {
        if (recentDays <= 0) {
            return Collections.emptyList();
        }
        Date beginTime = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -(recentDays - 1)));
        Date endTime = DateUtil.endOfDay(new Date());
        QueryWrapper<HealthFollowUpTaskLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(create_time) as statDate",
                "action_type as actionType",
                "count(*) as actionCount")
            .eq("owner_user_id", ownerUserId)
            .ge("create_time", beginTime)
            .le("create_time", endTime)
            .groupBy("DATE(create_time)", "action_type");
        return followUpTaskLogService.listMaps(queryWrapper);
    }

    /**
     * 用聚合行回填任务总览统计。
     */
    private void fillTaskAnalyticsOverview(HealthFollowUpTaskAnalyticsDTO analyticsDTO,
        List<Map<String, Object>> taskAggregateRows) {
        int totalTaskCount = 0;
        int unreadTaskCount = 0;
        int pendingTaskCount = 0;
        int delayedTaskCount = 0;
        int completedTaskCount = 0;
        int ignoredTaskCount = 0;
        if (taskAggregateRows != null) {
            for (Map<String, Object> row : taskAggregateRows) {
                int taskCount = toIntValue(row.get("taskCount"));
                Integer readStatus = toIntegerValue(row.get("readStatus"));
                Integer taskStatus = toIntegerValue(row.get("taskStatus"));
                totalTaskCount += taskCount;
                if (Objects.equals(readStatus, HealthAppMessageReadStatusEnum.UNREAD.getValue())) {
                    unreadTaskCount += taskCount;
                }
                if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.PENDING.getValue())) {
                    pendingTaskCount += taskCount;
                } else if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.DELAYED.getValue())) {
                    delayedTaskCount += taskCount;
                } else if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.COMPLETED.getValue())) {
                    completedTaskCount += taskCount;
                } else if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.IGNORED.getValue())) {
                    ignoredTaskCount += taskCount;
                }
            }
        }
        analyticsDTO.setTotalTaskCount(totalTaskCount);
        analyticsDTO.setUnreadTaskCount(unreadTaskCount);
        analyticsDTO.setPendingTaskCount(pendingTaskCount);
        analyticsDTO.setDelayedTaskCount(delayedTaskCount);
        analyticsDTO.setCompletedTaskCount(completedTaskCount);
        analyticsDTO.setIgnoredTaskCount(ignoredTaskCount);
    }

    /**
     * 用聚合行回填动作累计统计。
     */
    private void fillTaskAnalyticsActionCounts(HealthFollowUpTaskAnalyticsDTO analyticsDTO,
        List<Map<String, Object>> actionAggregateRows) {
        analyticsDTO.setReadActionCount(0);
        analyticsDTO.setDelayActionCount(0);
        analyticsDTO.setCompleteActionCount(0);
        analyticsDTO.setIgnoreActionCount(0);
        analyticsDTO.setRestoreActionCount(0);
        if (actionAggregateRows == null) {
            return;
        }
        for (Map<String, Object> row : actionAggregateRows) {
            String actionType = toStringValue(row.get("actionType"));
            int actionCount = toIntValue(row.get("actionCount"));
            if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.READ.getValue())) {
                analyticsDTO.setReadActionCount(actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.DELAY.getValue())) {
                analyticsDTO.setDelayActionCount(actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.COMPLETE.getValue())) {
                analyticsDTO.setCompleteActionCount(actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.IGNORE.getValue())) {
                analyticsDTO.setIgnoreActionCount(actionCount);
            } else if (Objects.equals(actionType, HealthFollowUpTaskActionTypeEnum.RESTORE.getValue())) {
                analyticsDTO.setRestoreActionCount(actionCount);
            }
        }
    }

    /**
     * 把聚合查询里的计数字段统一转成 int。
     */
    private int toIntValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /**
     * 把聚合查询里的整数字段统一转成 Integer。
     */
    private Integer toIntegerValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    /**
     * 把聚合查询里的文本字段统一转成字符串。
     */
    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 统一归一趋势统计日期。
     */
    private String normalizeStatDate(Object statDateValue) {
        if (statDateValue instanceof Date) {
            return DateUtil.formatDate((Date) statDateValue);
        }
        String statDateText = toStringValue(statDateValue);
        if (StrUtil.isBlank(statDateText)) {
            return null;
        }
        return statDateText.length() >= 10 ? statDateText.substring(0, 10) : statDateText;
    }

    /**
     * 统一计算百分比，保留两位小数。
     */
    private Double calculateRate(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round(numerator * 10000D / denominator) / 100D;
    }

    /**
     * 回填成员名称。
     */
    private String resolveMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 从预加载成员映射中回填成员名称。
     */
    private String resolveMemberName(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = memberMap == null ? null : memberMap.get(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 回填成员编码。
     */
    private String resolveMemberCode(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? null : memberEntity.getMemberCode();
    }

    /**
     * 从预加载成员映射中回填成员编码。
     */
    private String resolveMemberCode(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = memberMap == null ? null : memberMap.get(memberId);
        return memberEntity == null ? null : memberEntity.getMemberCode();
    }

    /**
     * 批量预加载任务中心列表需要的来源数据。
     *
     * <p>这里刻意把“任务主记录”和“来源快照”的加载拆成两个阶段：
     * 1. 先按任务类型收集 sourceId
     * 2. 再按类型一次性批量查库
     *
     * 这样后面 DTO 映射阶段就能完全走内存映射，避免列表场景出现 N+1。
     */
    private TaskListBatchContext buildTaskListBatchContext(List<HealthFollowUpTaskEntity> taskEntities) {
        if (taskEntities == null || taskEntities.isEmpty()) {
            return TaskListBatchContext.empty();
        }
        Set<Long> memberIds = taskEntities.stream()
            .map(HealthFollowUpTaskEntity::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Set<Long> reminderIds = collectSourceIdsByTaskType(taskEntities, HealthFollowUpTaskTypeEnum.REMINDER);
        Set<Long> reportIds = collectSourceIdsByTaskType(taskEntities, HealthFollowUpTaskTypeEnum.REPORT_ADVICE);
        Set<Long> operationTaskIds = collectSourceIdsByTaskType(taskEntities, HealthFollowUpTaskTypeEnum.OPERATION);
        return new TaskListBatchContext(
            loadMemberMap(memberIds),
            loadReminderMap(reminderIds),
            loadReportMap(reportIds),
            loadReportAbnormalCountMap(reportIds),
            loadOperationTaskMap(operationTaskIds));
    }

    /**
     * 按任务类型收集来源主键集合。
     */
    private Set<Long> collectSourceIdsByTaskType(List<HealthFollowUpTaskEntity> taskEntities,
        HealthFollowUpTaskTypeEnum taskTypeEnum) {
        if (taskEntities == null || taskEntities.isEmpty() || taskTypeEnum == null) {
            return Collections.emptySet();
        }
        return taskEntities.stream()
            .filter(taskEntity -> Objects.equals(taskEntity.getTaskType(), taskTypeEnum.getValue()))
            .map(HealthFollowUpTaskEntity::getSourceId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
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
     * 批量加载提醒来源。
     */
    private Map<Long, HealthMedicationReminderEntity> loadReminderMap(Set<Long> reminderIds) {
        if (reminderIds == null || reminderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return medicationReminderService.lambdaQuery()
            .in(HealthMedicationReminderEntity::getReminderId, reminderIds)
            .list()
            .stream()
            .collect(Collectors.toMap(HealthMedicationReminderEntity::getReminderId, reminderEntity -> reminderEntity,
                (left, right) -> left));
    }

    /**
     * 批量加载报告来源。
     */
    private Map<Long, HealthReportEntity> loadReportMap(Set<Long> reportIds) {
        if (reportIds == null || reportIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reportService.lambdaQuery()
            .in(HealthReportEntity::getReportId, reportIds)
            .list()
            .stream()
            .collect(Collectors.toMap(HealthReportEntity::getReportId, reportEntity -> reportEntity,
                (left, right) -> left));
    }

    /**
     * 批量统计报告异常项数量。
     */
    private Map<Long, Long> loadReportAbnormalCountMap(Set<Long> reportIds) {
        if (reportIds == null || reportIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> abnormalCountMap = new HashMap<>();
        reportItemService.lambdaQuery()
            .in(HealthReportItemEntity::getReportId, reportIds)
            .in(HealthReportItemEntity::getAbnormalFlag,
                HealthReportItemAbnormalFlagEnum.LOW.getValue(),
                HealthReportItemAbnormalFlagEnum.HIGH.getValue(),
                HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue())
            .list()
            .forEach(item -> {
                if (item.getReportId() != null) {
                    abnormalCountMap.merge(item.getReportId(), 1L, Long::sum);
                }
            });
        return abnormalCountMap;
    }

    /**
     * 批量加载运营任务来源。
     */
    private Map<Long, HealthOperationTaskEntity> loadOperationTaskMap(Set<Long> operationTaskIds) {
        if (operationTaskIds == null || operationTaskIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return operationTaskService.lambdaQuery()
            .in(HealthOperationTaskEntity::getOperationTaskId, operationTaskIds)
            .list()
            .stream()
            .collect(Collectors.toMap(HealthOperationTaskEntity::getOperationTaskId, operationTaskEntity -> operationTaskEntity,
                (left, right) -> left));
    }

    /**
     * 基于已经预加载的运营任务实体构建跳转参数。
     */
    private HealthFollowUpNavigationDTO buildOperationNavigation(HealthOperationTaskEntity operationTaskEntity, Long sourceId) {
        if (operationTaskEntity == null) {
            return null;
        }
        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetPageCode(operationTaskEntity.getTargetPageCode());
        navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(operationTaskEntity.getTargetPageCode()));
        navigationDTO.setTargetBizType(StrUtil.blankToDefault(operationTaskEntity.getTargetBizType(),
            HealthFollowUpTaskTypeEnum.OPERATION.getValue()));
        navigationDTO.setTargetBizId(operationTaskEntity.getTargetBizId());
        navigationDTO.setTargetTabCode(operationTaskEntity.getTargetTabCode());
        navigationDTO.setTargetAnchorCode(operationTaskEntity.getTargetAnchorCode());
        navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(operationTaskEntity.getTargetAnchorCode()));
        if (navigationDTO.getTargetBizId() == null) {
            // 运营任务通常会配置真实业务主键。
            // 若历史数据尚未补齐，则退回任务来源主键，至少保证跳转结构不为空。
            navigationDTO.setTargetBizId(sourceId);
        }
        return navigationDTO;
    }

    /**
     * 任务中心列表批量预加载上下文。
     */
    private static final class TaskListBatchContext {

        private final Map<Long, HealthFamilyMemberEntity> memberMap;

        private final Map<Long, HealthMedicationReminderEntity> reminderMap;

        private final Map<Long, HealthReportEntity> reportMap;

        private final Map<Long, Long> reportAbnormalCountMap;

        private final Map<Long, HealthOperationTaskEntity> operationTaskMap;

        private TaskListBatchContext(Map<Long, HealthFamilyMemberEntity> memberMap,
            Map<Long, HealthMedicationReminderEntity> reminderMap,
            Map<Long, HealthReportEntity> reportMap,
            Map<Long, Long> reportAbnormalCountMap,
            Map<Long, HealthOperationTaskEntity> operationTaskMap) {
            this.memberMap = memberMap == null ? Collections.emptyMap() : memberMap;
            this.reminderMap = reminderMap == null ? Collections.emptyMap() : reminderMap;
            this.reportMap = reportMap == null ? Collections.emptyMap() : reportMap;
            this.reportAbnormalCountMap = reportAbnormalCountMap == null ? Collections.emptyMap() : reportAbnormalCountMap;
            this.operationTaskMap = operationTaskMap == null ? Collections.emptyMap() : operationTaskMap;
        }

        private static TaskListBatchContext empty() {
            return new TaskListBatchContext(Collections.emptyMap(), Collections.emptyMap(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }

        private Map<Long, HealthFamilyMemberEntity> getMemberMap() {
            return memberMap;
        }

        private Map<Long, HealthMedicationReminderEntity> getReminderMap() {
            return reminderMap;
        }

        private Map<Long, HealthReportEntity> getReportMap() {
            return reportMap;
        }

        private Map<Long, Long> getReportAbnormalCountMap() {
            return reportAbnormalCountMap;
        }

        private Map<Long, HealthOperationTaskEntity> getOperationTaskMap() {
            return operationTaskMap;
        }
    }

    /**
     * 预热单条任务详情所需的来源快照。
     *
     * <p>任务详情和任务中心列表不同，它只服务单条任务，
     * 但仍然会同时依赖：
     * 1. 成员展示信息
     * 2. 任务来源实体
     * 3. 报告建议摘要（仅报告任务）
     *
     * <p>这里把这几类数据一次性预热出来，后续详情 DTO、推荐动作、来源可用性判断都直接复用，
     * 避免围绕同一条 sourceId 反复查库。
     */
    private TaskDetailContext buildTaskDetailContext(HealthFollowUpTaskEntity taskEntity, Long ownerUserId) {
        if (taskEntity == null) {
            return TaskDetailContext.empty();
        }
        Set<Long> memberIds = taskEntity.getMemberId() == null
            ? Collections.emptySet()
            : Collections.singleton(taskEntity.getMemberId());
        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(memberIds);
        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskEntity.getTaskType());
        if (taskTypeEnum == null) {
            return new TaskDetailContext(memberMap, null, null, null, null, 0L, false);
        }

        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            HealthMedicationReminderEntity reminderEntity = medicationReminderService.getById(taskEntity.getSourceId());
            boolean sourceAvailable = reminderEntity != null && ownerUserId.equals(reminderEntity.getOwnerUserId());
            return new TaskDetailContext(memberMap, sourceAvailable ? reminderEntity : null, null, null, null, 0L,
                sourceAvailable);
        }

        if (HealthFollowUpTaskTypeEnum.REPORT_ADVICE == taskTypeEnum) {
            HealthReportEntity reportEntity = reportService.getById(taskEntity.getSourceId());
            boolean sourceAvailable = reportEntity != null && ownerUserId.equals(reportEntity.getOwnerUserId());
            HealthReportAdviceDTO reportAdviceDTO = null;
            long reportAbnormalCount = 0L;
            if (sourceAvailable) {
                // 详情页只关心当前这一份报告的建议结果，
                // 这里直接复用报告服务现有的批量建议能力，避免重新走
                // “loadById -> 权限校验 -> listReportItems -> countActiveMedicationPlans”整条串行链路。
                reportAdviceDTO = healthReportApplicationService
                    .buildAdviceMapForOwner(Collections.singletonList(reportEntity), ownerUserId)
                    .get(reportEntity.getReportId());
                reportAbnormalCount = reportAdviceDTO == null || reportAdviceDTO.getAbnormalItemCount() == null
                    ? 0L
                    : reportAdviceDTO.getAbnormalItemCount().longValue();
            }
            return new TaskDetailContext(memberMap, null, sourceAvailable ? reportEntity : null, null, reportAdviceDTO,
                reportAbnormalCount, sourceAvailable);
        }

        if (HealthFollowUpTaskTypeEnum.OPERATION == taskTypeEnum) {
            HealthOperationTaskEntity operationTaskEntity = operationTaskService.getById(taskEntity.getSourceId());
            boolean sourceAvailable = operationTaskEntity != null && ownerUserId.equals(operationTaskEntity.getOwnerUserId());
            return new TaskDetailContext(memberMap, null, null, sourceAvailable ? operationTaskEntity : null, null, 0L,
                sourceAvailable);
        }

        return new TaskDetailContext(memberMap, null, null, null, null, 0L, false);
    }

    /**
     * 单条任务详情预热上下文。
     */
    private static final class TaskDetailContext {

        private final Map<Long, HealthFamilyMemberEntity> memberMap;

        private final HealthMedicationReminderEntity reminderEntity;

        private final HealthReportEntity reportEntity;

        private final HealthOperationTaskEntity operationTaskEntity;

        private final HealthReportAdviceDTO reportAdviceDTO;

        private final long reportAbnormalCount;

        private final boolean sourceAvailable;

        private TaskDetailContext(Map<Long, HealthFamilyMemberEntity> memberMap,
            HealthMedicationReminderEntity reminderEntity,
            HealthReportEntity reportEntity,
            HealthOperationTaskEntity operationTaskEntity,
            HealthReportAdviceDTO reportAdviceDTO,
            long reportAbnormalCount,
            boolean sourceAvailable) {
            this.memberMap = memberMap == null ? Collections.emptyMap() : memberMap;
            this.reminderEntity = reminderEntity;
            this.reportEntity = reportEntity;
            this.operationTaskEntity = operationTaskEntity;
            this.reportAdviceDTO = reportAdviceDTO;
            this.reportAbnormalCount = reportAbnormalCount;
            this.sourceAvailable = sourceAvailable;
        }

        private static TaskDetailContext empty() {
            return new TaskDetailContext(Collections.emptyMap(), null, null, null, null, 0L, false);
        }

        private Map<Long, HealthFamilyMemberEntity> getMemberMap() {
            return memberMap;
        }

        private HealthMedicationReminderEntity getReminderEntity() {
            return reminderEntity;
        }

        private HealthReportEntity getReportEntity() {
            return reportEntity;
        }

        private HealthOperationTaskEntity getOperationTaskEntity() {
            return operationTaskEntity;
        }

        private HealthReportAdviceDTO getReportAdviceDTO() {
            return reportAdviceDTO;
        }

        private long getReportAbnormalCount() {
            return reportAbnormalCount;
        }

        private boolean isSourceAvailable() {
            return sourceAvailable;
        }
    }

    /**
     * 回填提醒状态名称。
     */
    private String resolveReminderStatusName(Integer reminderStatus) {
        if (reminderStatus == null) {
            return "未知";
        }
        if (Objects.equals(reminderStatus, com.healthtrail.common.enums.health.MedicationReminderStatusEnum.PENDING.getValue())) {
            return HealthAppI18n.medicationReminderStatusName(
                com.healthtrail.common.enums.health.MedicationReminderStatusEnum.PENDING.getValue());
        }
        if (Objects.equals(reminderStatus, com.healthtrail.common.enums.health.MedicationReminderStatusEnum.TAKEN.getValue())) {
            return HealthAppI18n.medicationReminderStatusName(
                com.healthtrail.common.enums.health.MedicationReminderStatusEnum.TAKEN.getValue());
        }
        if (Objects.equals(reminderStatus, com.healthtrail.common.enums.health.MedicationReminderStatusEnum.SKIPPED.getValue())) {
            return HealthAppI18n.medicationReminderStatusName(
                com.healthtrail.common.enums.health.MedicationReminderStatusEnum.SKIPPED.getValue());
        }
        if (Objects.equals(reminderStatus, com.healthtrail.common.enums.health.MedicationReminderStatusEnum.EXPIRED.getValue())) {
            return HealthAppI18n.medicationReminderStatusName(
                com.healthtrail.common.enums.health.MedicationReminderStatusEnum.EXPIRED.getValue());
        }
        return HealthAppI18n.unknownName();
    }

    /**
     * 构建任务跳转参数。
     *
     * <p>这里统一由服务端按任务类型给出跳转目标，
     * 避免前端在多个页面分别维护相同的判断逻辑。
     */
    private HealthFollowUpNavigationDTO buildNavigation(String taskType, Long sourceId, HealthReportAdviceItemDTO adviceItemDTO) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskType);
        if (taskTypeEnum == null || sourceId == null) {
            return null;
        }

        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetBizId(sourceId);
        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            navigationDTO.setTargetPageCode(HealthFollowUpTargetPageEnum.MEDICATION_REMINDER_DETAIL.getValue());
            navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(
                HealthFollowUpTargetPageEnum.MEDICATION_REMINDER_DETAIL.getValue()));
            navigationDTO.setTargetBizType(HealthFollowUpTaskTypeEnum.REMINDER.getValue());
            navigationDTO.setTargetTabCode("PROCESS");
            navigationDTO.setTargetAnchorCode(HealthFollowUpTargetAnchorEnum.REMINDER_FEEDBACK.getValue());
            navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(
                HealthFollowUpTargetAnchorEnum.REMINDER_FEEDBACK.getValue()));
            return navigationDTO;
        }

        if (HealthFollowUpTaskTypeEnum.REPORT_ADVICE == taskTypeEnum) {
            navigationDTO.setTargetPageCode(HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue());
            navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(
                HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue()));
            navigationDTO.setTargetBizType(HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue());
            navigationDTO.setTargetTabCode("ADVICE");
            HealthFollowUpTargetAnchorEnum anchorEnum = resolveReportAnchor(adviceItemDTO);
            navigationDTO.setTargetAnchorCode(anchorEnum.getValue());
            navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(anchorEnum.getValue()));
            return navigationDTO;
        }
        if (HealthFollowUpTaskTypeEnum.OPERATION == taskTypeEnum) {
            HealthOperationTaskEntity operationTaskEntity = operationTaskService.getById(sourceId);
            if (operationTaskEntity == null) {
                return null;
            }
            navigationDTO.setTargetPageCode(operationTaskEntity.getTargetPageCode());
            navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(operationTaskEntity.getTargetPageCode()));
            navigationDTO.setTargetBizType(StrUtil.blankToDefault(operationTaskEntity.getTargetBizType(),
                HealthFollowUpTaskTypeEnum.OPERATION.getValue()));
            navigationDTO.setTargetBizId(operationTaskEntity.getTargetBizId());
            navigationDTO.setTargetTabCode(operationTaskEntity.getTargetTabCode());
            navigationDTO.setTargetAnchorCode(operationTaskEntity.getTargetAnchorCode());
            navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(operationTaskEntity.getTargetAnchorCode()));
            return navigationDTO;
        }
        return null;
    }

    /**
     * 构建推荐下一步动作。
     *
     * <p>该方法会结合任务类型、来源状态以及现有业务摘要，
     * 输出详情页最值得优先展示的一条动作建议。
     */
    private HealthFollowUpRecommendedActionDTO buildRecommendedAction(HealthFollowUpTaskEntity taskEntity,
        HealthFollowUpTaskDTO taskDTO, TaskDetailContext detailContext) {
        if (detailContext == null || !detailContext.isSourceAvailable()) {
            HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
            fillRiskLevel(actionDTO, HealthFollowUpRiskLevelEnum.LOW);
            actionDTO.setActionPriority(99);
            actionDTO.setTitle("来源记录已失效");
            actionDTO.setContent("该任务对应的原始提醒或报告记录已不存在，当前建议仅查看历史任务信息与操作记录。");
            actionDTO.setActionText("查看历史记录");
            actionDTO.setNavigation(null);
            return actionDTO;
        }

        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskEntity.getTaskType());
        if (HealthFollowUpTaskTypeEnum.REMINDER == taskTypeEnum) {
            return buildReminderRecommendedAction(detailContext.getReminderEntity(), taskDTO.getNavigation());
        }
        if (HealthFollowUpTaskTypeEnum.REPORT_ADVICE == taskTypeEnum) {
            return buildReportRecommendedAction(taskEntity.getSourceId(), taskDTO.getNavigation(),
                detailContext.getReportAdviceDTO());
        }
        if (HealthFollowUpTaskTypeEnum.OPERATION == taskTypeEnum) {
            return buildOperationRecommendedAction(detailContext.getOperationTaskEntity(), taskDTO.getNavigation());
        }

        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        fillRiskLevel(actionDTO, HealthFollowUpRiskLevelEnum.LOW);
        actionDTO.setActionPriority(50);
        actionDTO.setTitle("查看任务详情");
        actionDTO.setContent("当前任务可继续查看详情并根据页面提示完成后续处理。");
        actionDTO.setActionText("查看详情");
        actionDTO.setNavigation(taskDTO.getNavigation());
        return actionDTO;
    }

    /**
     * 构建运营任务推荐动作。
     *
     * <p>运营任务的内容本身就是后台人工投放的引导语，
     * 因此这里优先复用运营任务表中配置好的标题、正文和动作文案。
     */
    private HealthFollowUpRecommendedActionDTO buildOperationRecommendedAction(HealthOperationTaskEntity operationTaskEntity,
        HealthFollowUpNavigationDTO navigationDTO) {
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        if (operationTaskEntity == null) {
            fillRiskLevel(actionDTO, HealthFollowUpRiskLevelEnum.LOW);
            actionDTO.setActionPriority(90);
            actionDTO.setTitle("查看历史运营任务");
            actionDTO.setContent("该运营任务原始配置已不存在，建议仅查看当前保留的历史任务记录。");
            actionDTO.setActionText("查看历史记录");
            actionDTO.setNavigation(null);
            return actionDTO;
        }

        HealthFollowUpRiskLevelEnum riskLevelEnum = HealthFollowUpRiskLevelEnum.fromValue(operationTaskEntity.getRiskLevel());
        fillRiskLevel(actionDTO, riskLevelEnum == null ? HealthFollowUpRiskLevelEnum.MEDIUM : riskLevelEnum);
        actionDTO.setActionPriority(operationTaskEntity.getPriorityWeight() == null
            ? 10 : Math.max(1, 20 - operationTaskEntity.getPriorityWeight()));
        actionDTO.setTitle(StrUtil.blankToDefault(operationTaskEntity.getTaskTitle(), "查看运营任务"));
        actionDTO.setContent(StrUtil.blankToDefault(operationTaskEntity.getTaskContent(), "建议进入对应业务页继续处理。"));
        actionDTO.setActionText(StrUtil.blankToDefault(operationTaskEntity.getActionText(), "去查看"));
        actionDTO.setNavigation(navigationDTO);
        return actionDTO;
    }

    /**
     * 构建提醒类任务推荐动作。
     */
    private HealthFollowUpRecommendedActionDTO buildReminderRecommendedAction(HealthMedicationReminderEntity reminderEntity,
        HealthFollowUpNavigationDTO navigationDTO) {
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        if (reminderEntity == null) {
            actionDTO.setTitle("查看历史提醒记录");
            actionDTO.setContent("该提醒来源记录已不存在，当前仅可回顾历史任务操作记录。");
            actionDTO.setActionText("查看历史记录");
            actionDTO.setNavigation(null);
            fillRiskLevel(actionDTO, HealthFollowUpRiskLevelEnum.LOW);
            actionDTO.setActionPriority(99);
            return actionDTO;
        }

        boolean isOverduePending = reminderEntity.getScheduledTime() != null && !reminderEntity.getScheduledTime().after(new Date());
        String drugName = StrUtil.blankToDefault(reminderEntity.getDrugNameSnapshot(), "本次用药提醒");

        fillRiskLevel(actionDTO, isOverduePending ? HealthFollowUpRiskLevelEnum.HIGH : HealthFollowUpRiskLevelEnum.MEDIUM);
        actionDTO.setActionPriority(isOverduePending ? 1 : 2);
        actionDTO.setTitle(isOverduePending ? "优先确认本次提醒结果" : "按计划处理本次提醒");
        actionDTO.setContent(StrUtil.format("{}{}，建议尽快确认是否已服药；如本次无法执行，也可在提醒页选择跳过并记录原因。",
            drugName,
            reminderEntity.getScheduledTime() == null
                ? ""
                : StrUtil.format("，计划时间为 {}", DateUtil.format(reminderEntity.getScheduledTime(), "yyyy-MM-dd HH:mm"))));
        actionDTO.setActionText("去处理提醒");
        actionDTO.setNavigation(navigationDTO);
        return actionDTO;
    }

    /**
     * 构建报告建议类任务推荐动作。
     *
     * <p>这里优先复用报告建议能力里已经生成好的摘要与建议项，
     * 避免任务详情和报告详情出现两套互相打架的建议文案。
     */
    private HealthFollowUpRecommendedActionDTO buildReportRecommendedAction(Long reportId,
        HealthFollowUpNavigationDTO navigationDTO, HealthReportAdviceDTO adviceDTO) {
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        if (adviceDTO == null) {
            fillRiskLevel(actionDTO, HealthFollowUpRiskLevelEnum.MEDIUM);
            actionDTO.setActionPriority(3);
            actionDTO.setTitle("查看报告建议");
            actionDTO.setContent("建议先查看本次体检报告的异常分析结果，并结合近期状态决定是否继续跟进。");
            actionDTO.setActionText("查看报告建议");
            actionDTO.setNavigation(navigationDTO);
            return actionDTO;
        }

        HealthReportAdviceItemDTO firstAdvice = adviceDTO.getAdviceItems() == null || adviceDTO.getAdviceItems().isEmpty()
            ? null
            : adviceDTO.getAdviceItems().get(0);
        actionDTO.setNavigation(buildNavigation(HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue(), reportId, firstAdvice));
        fillRiskLevel(actionDTO, resolveReportRiskLevel(firstAdvice));
        actionDTO.setActionPriority(firstAdvice == null || firstAdvice.getPriority() == null ? 3 : firstAdvice.getPriority());
        actionDTO.setTitle(firstAdvice == null ? "优先查看本次报告建议" : firstAdvice.getTitle());
        actionDTO.setContent(firstAdvice == null || StrUtil.isBlank(firstAdvice.getContent())
            ? StrUtil.blankToDefault(adviceDTO.getSummary(), "建议先查看本次报告的异常指标与后续跟进建议。")
            : firstAdvice.getContent());
        actionDTO.setActionText(firstAdvice == null || StrUtil.isBlank(firstAdvice.getActionText())
            ? "查看报告建议"
            : firstAdvice.getActionText());
        return actionDTO;
    }

    /**
     * 解析报告任务推荐落点锚点。
     *
     * <p>当前规则尽量贴近用户真正想先看的区域：
     * 1. 如果动作明确提到查看原始报告，则优先落到原始报告区
     * 2. 复查/跟进类建议优先落到异常指标区
     * 3. 其他建议默认落到建议区
     */
    private HealthFollowUpTargetAnchorEnum resolveReportAnchor(HealthReportAdviceItemDTO adviceItemDTO) {
        if (adviceItemDTO == null) {
            return HealthFollowUpTargetAnchorEnum.REPORT_ADVICE;
        }
        if (StrUtil.containsAnyIgnoreCase(StrUtil.blankToDefault(adviceItemDTO.getActionText(), ""), "原始报告", "完整报告")) {
            return HealthFollowUpTargetAnchorEnum.REPORT_ORIGINAL_FILE;
        }
        if (Objects.equals(adviceItemDTO.getAdviceType(), "RECHECK")
            || Objects.equals(adviceItemDTO.getAdviceType(), "FOLLOW_UP")) {
            return HealthFollowUpTargetAnchorEnum.REPORT_ABNORMAL_ITEMS;
        }
        return HealthFollowUpTargetAnchorEnum.REPORT_ADVICE;
    }

    /**
     * 解析报告任务推荐动作风险等级。
     *
     * <p>当前优先复用首条建议关联的异常标记：
     * 1. 偏高 / 异常 -> 高优先关注
     * 2. 偏低 -> 中优先关注
     * 3. 其他 -> 低优先关注
     */
    private HealthFollowUpRiskLevelEnum resolveReportRiskLevel(HealthReportAdviceItemDTO adviceItemDTO) {
        if (adviceItemDTO == null || adviceItemDTO.getAbnormalFlag() == null) {
            return HealthFollowUpRiskLevelEnum.MEDIUM;
        }
        if (Objects.equals(adviceItemDTO.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.HIGH.getValue())
            || Objects.equals(adviceItemDTO.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue())) {
            return HealthFollowUpRiskLevelEnum.HIGH;
        }
        if (Objects.equals(adviceItemDTO.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.LOW.getValue())) {
            return HealthFollowUpRiskLevelEnum.MEDIUM;
        }
        return HealthFollowUpRiskLevelEnum.LOW;
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
     * 批量任务执行器。
     */
    @FunctionalInterface
    private interface BatchTaskExecutor {

        void execute(HealthFollowUpTaskEntity taskEntity);
    }

    /**
     * 任务来源快照。
     *
     * <p>多数任务只需要成员ID回填首页任务主记录；运营任务在完成时还需要读取 targetBizType/targetBizId，
     * 因此这里把完整运营任务实体一并保留，避免完成动作里再次按 sourceId 查一次 `operation_task`。
     */
    @lombok.Getter
    private static class TaskSourceSnapshot {

        private final Long memberId;

        private final HealthOperationTaskEntity operationTaskEntity;

        private TaskSourceSnapshot(Long memberId, HealthOperationTaskEntity operationTaskEntity) {
            this.memberId = memberId;
            this.operationTaskEntity = operationTaskEntity;
        }

        private static TaskSourceSnapshot ofMember(Long memberId) {
            return new TaskSourceSnapshot(memberId, null);
        }

        private static TaskSourceSnapshot ofOperation(HealthOperationTaskEntity operationTaskEntity) {
            Long memberId = operationTaskEntity == null ? null : operationTaskEntity.getMemberId();
            return new TaskSourceSnapshot(memberId, operationTaskEntity);
        }
    }
}
