package com.healthtrail.domain.health.dashboard;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageReadStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskTypeEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.chronic.ChronicDiseaseApplicationService;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeAdherenceTrendPointDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeAiAdviceDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeDashboardDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeFollowUpItemDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeHealthTrendPointDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeMessageDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeMemberCardDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeOverviewDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeReminderDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthHomeReportDTO;
import com.healthtrail.domain.health.dashboard.db.HealthFollowUpTaskEntity;
import com.healthtrail.domain.health.dashboard.db.HealthFollowUpTaskService;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.medication.MedicationApplicationService;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import com.healthtrail.domain.health.message.AppMessageApplicationService;
import com.healthtrail.domain.health.message.dto.HealthAppMessageDTO;
import com.healthtrail.domain.health.message.query.HealthAppMessageQuery;
import com.healthtrail.domain.health.report.HealthReportApplicationService;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemService;
import com.healthtrail.domain.health.report.db.HealthReportService;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceDTO;
import java.util.ArrayList;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * App 首页健康看板应用服务。
 *
 * <p>首页看板的价值不在于某一张表本身，而在于把多个高频模块在首页场景下重新组织：
 * 1. 家庭成员，告诉用户当前关注对象是谁
 * 2. 今日提醒，告诉用户今天要做什么
 * 3. 最近报告，告诉用户最近发现了什么问题
 *
 * <p>因此这里采用聚合服务模式，对多个健康模块做只读聚合输出。
 */
@Service
@RequiredArgsConstructor
public class HealthDashboardApplicationService {

    /**
     * 首页提醒预览条数上限。
     * 控制返回体体积，避免首页数据一次性返回过多提醒记录。
     */
    private static final int HOME_REMINDER_PREVIEW_LIMIT = 5;

    /**
     * 首页异常报告预览条数上限。
     */
    private static final int HOME_REPORT_PREVIEW_LIMIT = 5;

    /**
     * 首页待跟进事项条数上限。
     * 这里控制在一个较小范围，避免首页任务流过长反而影响首屏可读性。
     */
    private static final int HOME_FOLLOW_UP_LIMIT = 8;

    /**
     * 首页消息预览条数上限。
     * 首页只需要最近几条消息即可，完整列表由消息中心列表负责。
     */
    private static final int HOME_MESSAGE_PREVIEW_LIMIT = 3;

    /**
     * 为了筛出“最近异常报告”，这里先拉取稍大一点的报告窗口再过滤。
     * 这样可以避免最新几份报告刚好都正常时首页完全没有异常报告可展示。
     */
    private static final int HOME_REPORT_SCAN_LIMIT = 20;

    /**
     * 首页健康趋势图最多展示的点数。
     */
    private static final int HOME_HEALTH_TREND_LIMIT = 8;

    /**
     * 首页依从率趋势统计天数。
     */
    private static final int HOME_ADHERENCE_TREND_DAYS = 7;

    /**
     * 家庭成员关系里用于标识“本人”的固定文案。
     *
     * <p>首页提醒区这次明确只展示当前账号自己的提醒，
     * 因此这里统一按成员关系中的“本人”来识别自有成员。
     */
    private static final String SELF_RELATION_TYPE = "本人";

    /** 用药管理应用服务 */
    private final MedicationApplicationService medicationApplicationService;

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 用药提醒数据库服务 */
    private final HealthMedicationReminderService medicationReminderService;

    /** 首页待跟进任务数据库服务 */
    private final HealthFollowUpTaskService followUpTaskService;

    /** 体检报告应用服务 */
    private final HealthReportApplicationService healthReportApplicationService;

    /** 体检报告数据库服务 */
    private final HealthReportService reportService;

    /** 体检报告指标项数据库服务 */
    private final HealthReportItemService reportItemService;

    /** App 消息应用服务 */
    private final AppMessageApplicationService appMessageApplicationService;

    /** 首页运营任务应用服务 */
    private final HealthOperationTaskApplicationService operationTaskApplicationService;

    /** 首页待跟进任务应用服务 */
    private final HealthFollowUpTaskApplicationService healthFollowUpTaskApplicationService;

    /** 慢病专项应用服务 */
    private final ChronicDiseaseApplicationService chronicDiseaseApplicationService;

    /**
     * 获取 App 首页健康看板。
     *
     * <p>当前口径全部基于“当前登录用户”的个人数据：
     * 1. 只看当前用户自己的家庭成员
     * 2. 只看当前用户今日提醒
     * 3. 只看当前用户自己的体检报告
     *
     * @param ownerUserId 当前登录 App 用户ID
     * @return 首页看板聚合数据
     */
    public HealthHomeDashboardDTO getHomeDashboard(Long ownerUserId) {
        // 首页是用户重新登录、重新进入 App 后最先看到的聚合页。
        // 这里先对当前账号补齐一次“当天提醒窗口”，
        // 可以兜底定时补齐任务尚未来得及执行时的空白状态，
        // 避免用户把“当天提醒记录没生成”误解为“原来的用药计划被登录操作清掉了”。
        medicationApplicationService.supplementUpcomingReminders(ownerUserId, 1);

        // 首页统计前先执行一次过期刷新，确保提醒状态口径稳定。
        medicationApplicationService.expireOverdueReminders(ownerUserId);

        List<HealthFamilyMemberEntity> members = listActiveMembers(ownerUserId);
        Map<Long, HealthFamilyMemberEntity> memberMap = members.stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, member -> member, (left, right) -> left));

        List<HealthMedicationReminderEntity> todayReminders = listTodayReminders(ownerUserId);
        Set<Long> selfMemberIds = resolveSelfMemberIds(members);
        List<HealthMedicationReminderEntity> selfTodayReminders = filterRemindersByMemberIds(todayReminders, selfMemberIds);
        List<HealthReportEntity> recentReports = listRecentReports(ownerUserId, HOME_REPORT_SCAN_LIMIT);
        Map<Long, List<HealthReportItemEntity>> reportItemMap = loadReportItemMap(recentReports);
        Map<Long, Integer> abnormalCountMap = buildAbnormalCountMap(reportItemMap);
        List<HealthOperationTaskEntity> activeOperationTasks = operationTaskApplicationService.listActiveTasks(ownerUserId);

        HealthHomeDashboardDTO dashboardDTO = new HealthHomeDashboardDTO();
        List<HealthHomeFollowUpItemDTO> allFollowUpItems = buildFollowUpItems(
            ownerUserId, selfTodayReminders, recentReports, activeOperationTasks, memberMap, abnormalCountMap);
        Long unreadMessageCount = appMessageApplicationService.getUnreadCount(ownerUserId).getUnreadCount();
        dashboardDTO.setOverview(buildOverview(members, selfTodayReminders, ownerUserId, allFollowUpItems.size(), unreadMessageCount));
        dashboardDTO.setMemberCards(buildMemberCards(members, selfTodayReminders, recentReports, reportItemMap, abnormalCountMap));
        dashboardDTO.setTodayReminderPreview(buildReminderPreview(selfTodayReminders, memberMap));
        dashboardDTO.setRecentAbnormalReports(buildRecentAbnormalReports(recentReports, memberMap, abnormalCountMap));
        dashboardDTO.setChronicDiseaseSummary(chronicDiseaseApplicationService.getHomeSummary(ownerUserId));
        dashboardDTO.setFollowUpItems(limitFollowUpItems(allFollowUpItems));
        dashboardDTO.setMessagePreview(buildMessagePreview(ownerUserId));
        dashboardDTO.setHealthTrendPoints(buildHealthTrendPoints(recentReports, abnormalCountMap));
        dashboardDTO.setAdherenceTrendPoints(buildAdherenceTrendPoints(ownerUserId));
        dashboardDTO.setAiHealthAdvice(buildAiHealthAdvice(members, selfTodayReminders, recentReports,
            activeOperationTasks, allFollowUpItems, abnormalCountMap));
        dashboardDTO.setFollowUpTaskAnalytics(healthFollowUpTaskApplicationService.getTaskAnalytics(ownerUserId, 7));
        return dashboardDTO;
    }

    /**
     * 查询当前用户启用状态的家庭成员列表。
     *
     * <p>首页默认聚焦仍在正常使用中的成员，
     * 已停用成员不再继续占用首页卡片空间。
     */
    private List<HealthFamilyMemberEntity> listActiveMembers(Long ownerUserId) {
        return familyMemberService.lambdaQuery()
            .eq(HealthFamilyMemberEntity::getOwnerUserId, ownerUserId)
            .eq(HealthFamilyMemberEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByAsc(HealthFamilyMemberEntity::getMemberId)
            .list();
    }

    /**
     * 查询当前用户今日提醒列表。
     */
    private List<HealthMedicationReminderEntity> listTodayReminders(Long ownerUserId) {
        Date beginOfDay = DateUtil.beginOfDay(new Date());
        Date endOfDay = DateUtil.endOfDay(new Date());
        return medicationReminderService.lambdaQuery()
            .eq(HealthMedicationReminderEntity::getOwnerUserId, ownerUserId)
            .ge(HealthMedicationReminderEntity::getScheduledTime, beginOfDay)
            .le(HealthMedicationReminderEntity::getScheduledTime, endOfDay)
            .orderByAsc(HealthMedicationReminderEntity::getScheduledTime)
            .list();
    }

    /**
     * 解析首页“本人”成员 ID 集合。
     *
     * <p>家庭成员支持多人协同，但首页提醒区当前只允许展示当前账号自己的提醒，
     * 因此这里先把关系为“本人”的成员单独筛出来，后续所有首页提醒统计统一复用。
     */
    private Set<Long> resolveSelfMemberIds(List<HealthFamilyMemberEntity> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
            .filter(Objects::nonNull)
            .filter(member -> StrUtil.equals(StrUtil.trim(member.getRelationType()), SELF_RELATION_TYPE))
            .map(HealthFamilyMemberEntity::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * 根据成员 ID 集合收窄首页提醒列表。
     *
     * <p>如果当前没有维护“本人”成员，则首页提醒区返回空列表，
     * 这样口径会比“误把全家人提醒继续展示在首页”更安全。
     */
    private List<HealthMedicationReminderEntity> filterRemindersByMemberIds(
        List<HealthMedicationReminderEntity> reminders, Set<Long> memberIds) {
        if (reminders == null || reminders.isEmpty() || memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reminders.stream()
            .filter(Objects::nonNull)
            .filter(reminder -> memberIds.contains(reminder.getMemberId()))
            .collect(Collectors.toList());
    }

    /**
     * 查询当前用户最近的若干份报告。
     *
     * <p>首页只需要“最近窗口”，不需要分页全量扫描。
     */
    private List<HealthReportEntity> listRecentReports(Long ownerUserId, int limit) {
        QueryWrapper<HealthReportEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_user_id", ownerUserId)
            .orderByDesc("report_date")
            .orderByDesc("report_id");
        return reportService.page(new Page<>(1, Math.max(limit, 1)), queryWrapper).getRecords();
    }

    /**
     * 构建首页顶部总览统计。
     */
    private HealthHomeOverviewDTO buildOverview(List<HealthFamilyMemberEntity> members,
        List<HealthMedicationReminderEntity> todayReminders, Long ownerUserId, int followUpCount, Long unreadMessageCount) {
        HealthHomeOverviewDTO overviewDTO = new HealthHomeOverviewDTO();
        overviewDTO.setMemberCount(members.size());
        overviewDTO.setReportCount(reportService.lambdaQuery()
            .eq(HealthReportEntity::getOwnerUserId, ownerUserId)
            .count().intValue());
        overviewDTO.setTodayReminderCount(todayReminders.size());
        overviewDTO.setTodayPendingReminderCount(countReminderByStatus(todayReminders, MedicationReminderStatusEnum.PENDING.getValue()));
        overviewDTO.setTodayTakenReminderCount(countReminderByStatus(todayReminders, MedicationReminderStatusEnum.TAKEN.getValue()));
        overviewDTO.setTodaySkippedReminderCount(countReminderByStatus(todayReminders, MedicationReminderStatusEnum.SKIPPED.getValue()));
        overviewDTO.setTodayExpiredReminderCount(countReminderByStatus(todayReminders, MedicationReminderStatusEnum.EXPIRED.getValue()));
        overviewDTO.setFollowUpCount(followUpCount);
        overviewDTO.setUnreadMessageCount(unreadMessageCount);
        return overviewDTO;
    }

    /**
     * 构建首页消息预览列表。
     *
     * <p>这里直接复用消息中心应用服务的分页能力，
     * 保证首页和消息中心对同一条消息的字段口径完全一致。
     */
    private List<HealthHomeMessageDTO> buildMessagePreview(Long ownerUserId) {
        HealthAppMessageQuery query = new HealthAppMessageQuery();
        query.setOwnerUserId(ownerUserId);
        query.setPageNum(1);
        query.setPageSize(HOME_MESSAGE_PREVIEW_LIMIT);

        PageDTO<HealthAppMessageDTO> pageDTO = appMessageApplicationService.getMessageList(query);
        if (pageDTO == null || pageDTO.getRows() == null || pageDTO.getRows().isEmpty()) {
            return Collections.emptyList();
        }

        return pageDTO.getRows().stream().map(messageDTO -> {
            HealthHomeMessageDTO homeMessageDTO = new HealthHomeMessageDTO();
            homeMessageDTO.setMessageId(messageDTO.getMessageId());
            homeMessageDTO.setMemberId(messageDTO.getMemberId());
            homeMessageDTO.setMemberName(messageDTO.getMemberName());
            homeMessageDTO.setBusinessScene(messageDTO.getBusinessScene());
            homeMessageDTO.setBusinessSceneName(messageDTO.getBusinessSceneName());
            homeMessageDTO.setBusinessId(messageDTO.getBusinessId());
            homeMessageDTO.setMessageTitle(messageDTO.getMessageTitle());
            homeMessageDTO.setMessageContent(messageDTO.getMessageContent());
            homeMessageDTO.setReadStatus(messageDTO.getReadStatus());
            homeMessageDTO.setSendTime(messageDTO.getSendTime() == null ? messageDTO.getCreateTime() : messageDTO.getSendTime());
            homeMessageDTO.setNavigation(messageDTO.getNavigation());
            homeMessageDTO.setRecommendedAction(messageDTO.getRecommendedAction());
            return homeMessageDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 构建成员健康卡片列表。
     *
     * <p>每张卡片聚合两类信息：
     * 1. 今日提醒负载
     * 2. 最近一次报告情况
     *
     * 这样首页就能先给出一个“成员维度”的健康概览。
     */
    private List<HealthHomeMemberCardDTO> buildMemberCards(List<HealthFamilyMemberEntity> members,
        List<HealthMedicationReminderEntity> todayReminders, List<HealthReportEntity> recentReports,
        Map<Long, List<HealthReportItemEntity>> reportItemMap, Map<Long, Integer> abnormalCountMap) {
        Map<Long, List<HealthMedicationReminderEntity>> reminderMap = todayReminders.stream()
            .collect(Collectors.groupingBy(HealthMedicationReminderEntity::getMemberId));
        Map<Long, HealthReportEntity> latestReportMap = recentReports.stream()
            .filter(report -> report.getMemberId() != null)
            .collect(Collectors.toMap(HealthReportEntity::getMemberId, report -> report, (left, right) -> left));
        Map<Long, List<HealthReportEntity>> memberReportMap = recentReports.stream()
            .filter(report -> report.getMemberId() != null)
            .collect(Collectors.groupingBy(HealthReportEntity::getMemberId));
        Date now = new Date();

        return members.stream().map(member -> {
            List<HealthMedicationReminderEntity> memberReminders =
                reminderMap.getOrDefault(member.getMemberId(), Collections.emptyList());
            HealthReportEntity latestReport = latestReportMap.get(member.getMemberId());
            List<HealthReportEntity> memberReports = memberReportMap.getOrDefault(member.getMemberId(), Collections.emptyList());
            List<HealthReportItemEntity> latestReportItems = latestReport == null
                ? Collections.emptyList()
                : reportItemMap.getOrDefault(latestReport.getReportId(), Collections.emptyList());
            int pendingCount = countReminderByStatus(memberReminders, MedicationReminderStatusEnum.PENDING.getValue());
            long overdueCount = memberReminders.stream()
                .filter(reminder -> Objects.equals(reminder.getReminderStatus(), MedicationReminderStatusEnum.PENDING.getValue()))
                .filter(reminder -> reminder.getScheduledTime() != null && !reminder.getScheduledTime().after(now))
                .count();
            int memberAbnormalItemCount = memberReports.stream()
                .map(HealthReportEntity::getReportId)
                .mapToInt(reportId -> abnormalCountMap.getOrDefault(reportId, 0))
                .sum();
            int abnormalReportCount = (int) memberReports.stream()
                .filter(report -> abnormalCountMap.getOrDefault(report.getReportId(), 0) > 0)
                .count();
            List<String> chronicTags = buildChronicTags(latestReportItems);
            int sortWeight = (int) overdueCount * 40 + pendingCount * 18 + abnormalReportCount * 25
                + chronicTags.size() * 8;
            int healthScore = Math.max(0, 100 - (int) overdueCount * 18 - pendingCount * 10
                - memberAbnormalItemCount * 5 - chronicTags.size() * 3);

            HealthHomeMemberCardDTO cardDTO = new HealthHomeMemberCardDTO();
            cardDTO.setMemberId(member.getMemberId());
            cardDTO.setMemberCode(member.getMemberCode());
            cardDTO.setMemberName(member.getMemberName());
            cardDTO.setRelationType(member.getRelationType());
            cardDTO.setTodayReminderCount(memberReminders.size());
            cardDTO.setTodayPendingReminderCount(pendingCount);
            cardDTO.setHealthScore(healthScore);
            cardDTO.setChronicTags(chronicTags);
            cardDTO.setSortWeight(sortWeight);
            cardDTO.setSortReason(buildMemberSortReason(overdueCount, pendingCount, abnormalReportCount, chronicTags));

            if (latestReport != null) {
                cardDTO.setLatestReportId(latestReport.getReportId());
                cardDTO.setLatestReportName(latestReport.getReportName());
                cardDTO.setLatestReportDate(latestReport.getReportDate());
                cardDTO.setLatestReportSummary(latestReport.getAnalysisSummary());
            }
            return cardDTO;
        }).sorted(Comparator.comparing(HealthHomeMemberCardDTO::getSortWeight, Comparator.nullsLast(Integer::compareTo))
            .reversed()
            .thenComparing(HealthHomeMemberCardDTO::getMemberId, Comparator.nullsLast(Long::compareTo)))
            .collect(Collectors.toList());
    }

    /**
     * 构建首页今日提醒预览列表。
     */
    private List<HealthHomeReminderDTO> buildReminderPreview(List<HealthMedicationReminderEntity> todayReminders,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        return todayReminders.stream()
            .sorted(Comparator.comparing(HealthMedicationReminderEntity::getScheduledTime,
                Comparator.nullsLast(Date::compareTo)))
            .limit(HOME_REMINDER_PREVIEW_LIMIT)
            .map(reminder -> {
                HealthHomeReminderDTO reminderDTO = new HealthHomeReminderDTO();
                reminderDTO.setReminderId(reminder.getReminderId());
                reminderDTO.setMemberId(reminder.getMemberId());
                reminderDTO.setMemberCode(resolveMemberCode(memberMap, reminder.getMemberId()));
                reminderDTO.setMemberName(resolveMemberName(memberMap, reminder.getMemberId()));
                reminderDTO.setScheduledTime(reminder.getScheduledTime());
                reminderDTO.setDrugName(reminder.getDrugNameSnapshot());
                reminderDTO.setDoseAmount(reminder.getDoseAmount());
                reminderDTO.setDoseUnit(reminder.getDoseUnit());
                reminderDTO.setMealTiming(HealthAppI18n.mealTimingName(reminder.getMealTiming()));
                reminderDTO.setReminderStatus(reminder.getReminderStatus());
                return reminderDTO;
            }).collect(Collectors.toList());
    }

    /**
     * 构建最近异常报告预览列表。
     *
     * <p>这里的“异常报告”判断口径以结构化指标结果为准：
     * 只要报告下存在偏低、偏高或异常项，就会进入首页关注列表。
     */
    private List<HealthHomeReportDTO> buildRecentAbnormalReports(List<HealthReportEntity> recentReports,
        Map<Long, HealthFamilyMemberEntity> memberMap, Map<Long, Integer> abnormalCountMap) {
        return recentReports.stream()
            .filter(report -> abnormalCountMap.containsKey(report.getReportId()))
            .limit(HOME_REPORT_PREVIEW_LIMIT)
            .map(report -> {
                HealthHomeReportDTO reportDTO = new HealthHomeReportDTO();
                reportDTO.setReportId(report.getReportId());
                reportDTO.setMemberId(report.getMemberId());
                reportDTO.setMemberName(resolveMemberName(memberMap, report.getMemberId()));
                reportDTO.setReportName(report.getReportName());
                reportDTO.setReportDate(report.getReportDate());
                reportDTO.setAbnormalItemCount(abnormalCountMap.get(report.getReportId()));
                reportDTO.setAnalysisSummary(report.getAnalysisSummary());
                return reportDTO;
            }).collect(Collectors.toList());
    }

    /**
     * 构建首页待跟进事项。
     *
     * <p>当前阶段的任务流遵循一个简单优先级：
     * 1. 今天已经到点但仍待处理的提醒，优先级最高
     * 2. 今天稍后要处理的提醒，其次
     * 3. 最近异常报告的后续跟进任务，放在提醒任务之后
     *
     * <p>这样首页能先把“立刻要做的事”放到最前面。
     */
    private List<HealthHomeFollowUpItemDTO> buildFollowUpItems(Long ownerUserId,
        List<HealthMedicationReminderEntity> todayReminders, List<HealthReportEntity> recentReports,
        List<HealthOperationTaskEntity> activeOperationTasks, Map<Long, HealthFamilyMemberEntity> memberMap,
        Map<Long, Integer> abnormalCountMap) {
        List<HealthHomeFollowUpItemDTO> followUpItems = new ArrayList<>();
        Date now = new Date();
        // 首页待跟进区经常会同时命中多份异常报告。
        // 如果继续在循环里逐条调用 `getReportAdvice(...)`，
        // 每多一份报告就会多一轮“查指标 + 查启用计划数”的 SQL。
        // 因此这里先批量把建议结果算好，后续循环里只做 O(1) 映射读取。
        Map<Long, HealthReportAdviceDTO> reportAdviceMap =
            healthReportApplicationService.buildAdviceMapForOwner(recentReports, ownerUserId);
        Map<String, HealthFollowUpTaskEntity> taskMap =
            loadTaskMap(ownerUserId, todayReminders, recentReports, activeOperationTasks);
        Map<Long, Integer> memberSortWeightMap = buildMemberSortWeightMap(todayReminders, recentReports, abnormalCountMap);

        todayReminders.stream()
            .filter(reminder -> Objects.equals(reminder.getReminderStatus(), MedicationReminderStatusEnum.PENDING.getValue()))
            .sorted(Comparator.comparing(HealthMedicationReminderEntity::getScheduledTime,
            Comparator.nullsLast(Date::compareTo)))
            .forEach(reminder -> {
                HealthFollowUpTaskEntity taskEntity =
                    taskMap.get(buildTaskKey(HealthFollowUpTaskTypeEnum.REMINDER.getValue(), reminder.getReminderId()));
                if (shouldHideTask(taskEntity, now)) {
                    return;
                }
                followUpItems.add(buildReminderFollowUpItem(reminder, memberMap, now, taskEntity,
                    memberSortWeightMap.getOrDefault(reminder.getMemberId(), 0)));
            });

        recentReports.stream()
            .filter(report -> abnormalCountMap.getOrDefault(report.getReportId(), 0) > 0)
            .forEach(report -> {
                HealthFollowUpTaskEntity taskEntity =
                    taskMap.get(buildTaskKey(HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue(), report.getReportId()));
                if (shouldHideTask(taskEntity, now)) {
                    return;
                }
                HealthReportAdviceDTO adviceDTO = reportAdviceMap.get(report.getReportId());
                followUpItems.add(buildReportFollowUpItem(report, adviceDTO, memberMap, taskEntity,
                    memberSortWeightMap.getOrDefault(report.getMemberId(), 0),
                    abnormalCountMap.getOrDefault(report.getReportId(), 0)));
            });

        activeOperationTasks.forEach(operationTask -> {
            HealthFollowUpTaskEntity taskEntity = taskMap.get(buildTaskKey(
                HealthFollowUpTaskTypeEnum.OPERATION.getValue(), operationTask.getOperationTaskId()));
            if (shouldHideTask(taskEntity, now)) {
                return;
            }
            followUpItems.add(buildOperationFollowUpItem(operationTask, memberMap, taskEntity,
                memberSortWeightMap.getOrDefault(operationTask.getMemberId(), 0)));
        });

        return followUpItems.stream()
            .sorted(this::compareFollowUpItems)
            .collect(Collectors.toList());
    }

    /**
     * 构建提醒类待跟进事项。
     *
     * <p>提醒任务的优先级会区分：
     * 1. 已经到点仍待处理
     * 2. 今天稍后即将到点
     *
     * 这样首页的任务顺序更符合用户真实处理节奏。
     */
    private HealthHomeFollowUpItemDTO buildReminderFollowUpItem(HealthMedicationReminderEntity reminder,
        Map<Long, HealthFamilyMemberEntity> memberMap, Date now, HealthFollowUpTaskEntity taskEntity,
        Integer memberSortWeight) {
        HealthHomeFollowUpItemDTO followUpItemDTO = new HealthHomeFollowUpItemDTO();
        followUpItemDTO.setTaskType("REMINDER");
        followUpItemDTO.setReminderId(reminder.getReminderId());
        followUpItemDTO.setMemberId(reminder.getMemberId());
        followUpItemDTO.setMemberCode(resolveMemberCode(memberMap, reminder.getMemberId()));
        followUpItemDTO.setMemberName(resolveMemberName(memberMap, reminder.getMemberId()));
        followUpItemDTO.setFollowUpTime(reminder.getScheduledTime());

        boolean isOverduePending = reminder.getScheduledTime() != null && !reminder.getScheduledTime().after(now);
        followUpItemDTO.setPriority(isOverduePending ? 1 : 2);
        followUpItemDTO.setSortWeight((isOverduePending ? 100 : 80) + Objects.requireNonNullElse(memberSortWeight, 0));
        followUpItemDTO.setTitle(isOverduePending
            ? StrUtil.format("{}的用药提醒待处理", followUpItemDTO.getMemberName())
            : StrUtil.format("{}稍后有用药提醒", followUpItemDTO.getMemberName()));
        followUpItemDTO.setContent(StrUtil.format("{} {}，请及时确认是否已服药。",
            StrUtil.blankToDefault(reminder.getDrugNameSnapshot(), "有一条用药提醒"),
            buildReminderTimeText(reminder.getScheduledTime())));
        followUpItemDTO.setActionText("去处理提醒");
        followUpItemDTO.setNavigation(buildReminderNavigation(reminder.getReminderId()));
        fillTaskReadSnapshot(followUpItemDTO, taskEntity);
        followUpItemDTO.setSortReason(isOverduePending
            ? "已到点且尚未处理的提醒优先展示"
            : "今日稍后即将到点的提醒保持前排展示");
        return followUpItemDTO;
    }

    /**
     * 构建报告类待跟进事项。
     *
     * <p>报告任务的核心不是重复展示异常项数量，
     * 而是把“当前应该 follow up 什么动作”提炼出来。
     */
    private HealthHomeFollowUpItemDTO buildReportFollowUpItem(HealthReportEntity report, HealthReportAdviceDTO adviceDTO,
        Map<Long, HealthFamilyMemberEntity> memberMap, HealthFollowUpTaskEntity taskEntity,
        Integer memberSortWeight, Integer abnormalItemCount) {
        HealthHomeFollowUpItemDTO followUpItemDTO = new HealthHomeFollowUpItemDTO();
        followUpItemDTO.setTaskType("REPORT_ADVICE");
        followUpItemDTO.setReportId(report.getReportId());
        followUpItemDTO.setMemberId(report.getMemberId());
        followUpItemDTO.setMemberCode(resolveMemberCode(memberMap, report.getMemberId()));
        followUpItemDTO.setMemberName(resolveMemberName(memberMap, report.getMemberId()));
        followUpItemDTO.setFollowUpTime(report.getReportDate() == null ? report.getCreateTime() : report.getReportDate());
        followUpItemDTO.setPriority(3);
        followUpItemDTO.setSortWeight(60 + Objects.requireNonNullElse(memberSortWeight, 0) + abnormalItemCount * 6);
        followUpItemDTO.setTitle(StrUtil.format("{}的报告需要跟进", followUpItemDTO.getMemberName()));
        followUpItemDTO.setContent(adviceDTO == null || StrUtil.isBlank(adviceDTO.getSummary())
            ? StrUtil.format("{}存在异常报告，建议尽快查看详情并跟进。", followUpItemDTO.getMemberName())
            : adviceDTO.getSummary());
        followUpItemDTO.setActionText(adviceDTO != null && Boolean.TRUE.equals(adviceDTO.getHasActiveMedicationPlan())
            ? "查看报告建议与用药计划"
            : "查看报告建议");
        followUpItemDTO.setNavigation(buildReportNavigation(report.getReportId()));
        fillTaskReadSnapshot(followUpItemDTO, taskEntity);
        followUpItemDTO.setSortReason(StrUtil.format("最近报告异常指标 {} 项，需要继续跟进", abnormalItemCount));
        return followUpItemDTO;
    }

    /**
     * 构建运营任务待跟进事项。
     */
    private HealthHomeFollowUpItemDTO buildOperationFollowUpItem(HealthOperationTaskEntity operationTask,
        Map<Long, HealthFamilyMemberEntity> memberMap, HealthFollowUpTaskEntity taskEntity, Integer memberSortWeight) {
        HealthHomeFollowUpItemDTO followUpItemDTO = new HealthHomeFollowUpItemDTO();
        followUpItemDTO.setTaskType(HealthFollowUpTaskTypeEnum.OPERATION.getValue());
        followUpItemDTO.setOperationTaskId(operationTask.getOperationTaskId());
        followUpItemDTO.setMemberId(operationTask.getMemberId());
        followUpItemDTO.setMemberCode(resolveMemberCode(memberMap, operationTask.getMemberId()));
        followUpItemDTO.setMemberName(resolveMemberName(memberMap, operationTask.getMemberId()));
        followUpItemDTO.setFollowUpTime(operationTask.getStartTime());
        followUpItemDTO.setPriority(resolveOperationPriority(operationTask));
        followUpItemDTO.setSortWeight(50 + Objects.requireNonNullElse(operationTask.getPriorityWeight(), 0) * 5
            + Objects.requireNonNullElse(memberSortWeight, 0));
        followUpItemDTO.setTitle(StrUtil.blankToDefault(operationTask.getTaskTitle(), "首页运营任务"));
        followUpItemDTO.setContent(StrUtil.blankToDefault(operationTask.getTaskContent(), "请查看对应业务页继续处理。"));
        followUpItemDTO.setActionText(StrUtil.blankToDefault(operationTask.getActionText(), "去查看"));
        followUpItemDTO.setNavigation(buildOperationNavigation(operationTask));
        fillTaskReadSnapshot(followUpItemDTO, taskEntity);
        followUpItemDTO.setSortReason(StrUtil.format("运营任务权重 {}，当前按投放优先级展示",
            Objects.requireNonNullElse(operationTask.getPriorityWeight(), 0)));
        return followUpItemDTO;
    }

    /**
     * 判断某份报告是否存在异常项。
     *
     * <p>首页任务流会比预览区更强调“是否需要动作”，
     * 因此这里仍以结构化异常项为准，而不是依赖摘要字符串。
     */
    /**
     * 拼接提醒时间文案。
     */
    private String buildReminderTimeText(Date scheduledTime) {
        if (scheduledTime == null) {
            return "需要及时处理";
        }
        return StrUtil.format("计划时间 {}", DateUtil.format(scheduledTime, "HH:mm"));
    }

    /**
     * 加载指定任务类型、指定来源ID集合下的任务操作记录。
     *
     * <p>首页任务流的来源数据和操作记录是分开的，
     * 因此在构建任务流前，需要先把对应的操作记录一次性查出来做合并判断。
     */
    private Map<String, HealthFollowUpTaskEntity> loadTaskMap(Long ownerUserId,
        List<HealthMedicationReminderEntity> reminders,
        List<HealthReportEntity> reports,
        List<HealthOperationTaskEntity> operationTasks) {
        Set<Long> sourceIds = new HashSet<>();
        Set<String> taskTypes = new HashSet<>();
        if (reminders != null && !reminders.isEmpty()) {
            taskTypes.add(HealthFollowUpTaskTypeEnum.REMINDER.getValue());
            reminders.stream()
                .map(HealthMedicationReminderEntity::getReminderId)
                .filter(Objects::nonNull)
                .forEach(sourceIds::add);
        }
        if (reports != null && !reports.isEmpty()) {
            taskTypes.add(HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue());
            reports.stream()
                .map(HealthReportEntity::getReportId)
                .filter(Objects::nonNull)
                .forEach(sourceIds::add);
        }
        if (operationTasks != null && !operationTasks.isEmpty()) {
            taskTypes.add(HealthFollowUpTaskTypeEnum.OPERATION.getValue());
            operationTasks.stream()
                .map(HealthOperationTaskEntity::getOperationTaskId)
                .filter(Objects::nonNull)
                .forEach(sourceIds::add);
        }
        if (sourceIds.isEmpty() || taskTypes.isEmpty()) {
            return Collections.emptyMap();
        }

        // 首页看板原先会按 REMINDER / REPORT_ADVICE / OPERATION 各查一次任务表。
        // 这里改成一次统一加载，再用 `taskType#sourceId` 做内存索引。
        //
        // 由于不同来源表的主键数值可能偶尔重叠，这条查询理论上可能带回少量“当前首页不会用到”的额外任务行；
        // 但这些额外行会在后续按组合键读取时自然被忽略，换来的是首页任务链路从 3 次查询收敛成 1 次查询。
        return followUpTaskService.lambdaQuery()
            .eq(HealthFollowUpTaskEntity::getOwnerUserId, ownerUserId)
            .in(HealthFollowUpTaskEntity::getTaskType, taskTypes)
            .in(HealthFollowUpTaskEntity::getSourceId, sourceIds)
            .list()
            .stream()
            .collect(Collectors.toMap(task -> buildTaskKey(task.getTaskType(), task.getSourceId()), task -> task,
                (left, right) -> left));
    }

    /**
     * 判断当前任务是否应该从首页任务流中隐藏。
     *
     * <p>当前隐藏规则如下：
     * 1. 已完成任务直接隐藏
     * 2. 已延后且延后时间还没到的任务先隐藏
     * 3. 已延后但已到重新出现时间的任务重新显示
     */
    private boolean shouldHideTask(HealthFollowUpTaskEntity taskEntity, Date now) {
        if (taskEntity == null || taskEntity.getTaskStatus() == null) {
            return false;
        }
        if (Objects.equals(taskEntity.getTaskStatus(), HealthFollowUpTaskStatusEnum.COMPLETED.getValue())) {
            return true;
        }
        if (Objects.equals(taskEntity.getTaskStatus(), HealthFollowUpTaskStatusEnum.IGNORED.getValue())) {
            return true;
        }
        return Objects.equals(taskEntity.getTaskStatus(), HealthFollowUpTaskStatusEnum.DELAYED.getValue())
            && taskEntity.getDelayedUntil() != null
            && taskEntity.getDelayedUntil().after(now);
    }

    /**
     * 构造任务映射键。
     */
    private String buildTaskKey(String taskType, Long sourceId) {
        return taskType + "#" + sourceId;
    }

    /**
     * 统一把任务读态快照回填到首页待跟进事项。
     */
    private void fillTaskReadSnapshot(HealthHomeFollowUpItemDTO followUpItemDTO, HealthFollowUpTaskEntity taskEntity) {
        Integer readStatus = taskEntity == null || taskEntity.getReadStatus() == null
            ? HealthAppMessageReadStatusEnum.UNREAD.getValue()
            : taskEntity.getReadStatus();
        followUpItemDTO.setReadStatus(readStatus);
        followUpItemDTO.setReadTime(taskEntity == null ? null : taskEntity.getReadTime());
    }

    /**
     * 统计提醒列表中某个状态的数量。
     */
    private int countReminderByStatus(List<HealthMedicationReminderEntity> reminders, Integer reminderStatus) {
        return (int) reminders.stream()
            .filter(reminder -> Objects.equals(reminder.getReminderStatus(), reminderStatus))
            .count();
    }

    /**
     * 根据成员ID回填成员姓名。
     *
     * <p>首页展示尽量做到“即使成员后续被删，也不至于整块数据为空”，
     * 因此这里统一做一个温和兜底。
     */
    private String resolveMemberName(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = memberMap.get(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 根据成员ID回填成员编码。
     *
     * <p>首页多处都可能需要展示成员标识，
     * 因此统一从成员快照 Map 中读取，避免各处重复判空。
     */
    private String resolveMemberCode(Map<Long, HealthFamilyMemberEntity> memberMap, Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = memberMap.get(memberId);
        return memberEntity == null ? null : memberEntity.getMemberCode();
    }

    /**
     * 统一截取首页待跟进事项预览窗口。
     */
    private List<HealthHomeFollowUpItemDTO> limitFollowUpItems(List<HealthHomeFollowUpItemDTO> followUpItems) {
        if (followUpItems == null || followUpItems.isEmpty()) {
            return Collections.emptyList();
        }
        return followUpItems.stream()
            .limit(HOME_FOLLOW_UP_LIMIT)
            .collect(Collectors.toList());
    }

    /**
     * 构建提醒任务跳转参数。
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
     * 构建报告任务跳转参数。
     */
    private HealthFollowUpNavigationDTO buildReportNavigation(Long reportId) {
        if (reportId == null) {
            return null;
        }
        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetPageCode(HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue());
        navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(
            HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue()));
        navigationDTO.setTargetBizId(reportId);
        navigationDTO.setTargetBizType(HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue());
        navigationDTO.setTargetTabCode("ADVICE");
        navigationDTO.setTargetAnchorCode(HealthFollowUpTargetAnchorEnum.REPORT_ADVICE.getValue());
        navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(
            HealthFollowUpTargetAnchorEnum.REPORT_ADVICE.getValue()));
        return navigationDTO;
    }

    /**
     * 构建运营任务跳转参数。
     */
    private HealthFollowUpNavigationDTO buildOperationNavigation(HealthOperationTaskEntity operationTask) {
        if (operationTask == null) {
            return null;
        }
        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetPageCode(operationTask.getTargetPageCode());
        navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(operationTask.getTargetPageCode()));
        navigationDTO.setTargetBizId(operationTask.getTargetBizId());
        navigationDTO.setTargetBizType(StrUtil.blankToDefault(operationTask.getTargetBizType(),
            HealthFollowUpTaskTypeEnum.OPERATION.getValue()));
        navigationDTO.setTargetTabCode(operationTask.getTargetTabCode());
        navigationDTO.setTargetAnchorCode(operationTask.getTargetAnchorCode());
        navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(operationTask.getTargetAnchorCode()));
        return navigationDTO;
    }

    /**
     * 一次性加载报告指标结果映射，避免首页在成员卡片、趋势图、异常报告预览三个区域重复查库。
     */
    private Map<Long, List<HealthReportItemEntity>> loadReportItemMap(List<HealthReportEntity> reports) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> reportIds = reports.stream()
            .map(HealthReportEntity::getReportId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (reportIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reportItemService.lambdaQuery()
            .in(HealthReportItemEntity::getReportId, reportIds)
            .orderByAsc(HealthReportItemEntity::getSort)
            .orderByAsc(HealthReportItemEntity::getItemId)
            .list()
            .stream()
            .collect(Collectors.groupingBy(HealthReportItemEntity::getReportId));
    }

    /**
     * 一次性统计报告异常项数量。
     */
    private Map<Long, Integer> buildAbnormalCountMap(Map<Long, List<HealthReportItemEntity>> reportItemMap) {
        if (reportItemMap == null || reportItemMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> abnormalCountMap = new HashMap<>();
        for (Map.Entry<Long, List<HealthReportItemEntity>> entry : reportItemMap.entrySet()) {
            int abnormalCount = (int) entry.getValue().stream()
                .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
                .count();
            if (abnormalCount > 0) {
                abnormalCountMap.put(entry.getKey(), abnormalCount);
            }
        }
        return abnormalCountMap;
    }

    /**
     * 判断异常标记是否属于首页关注范围。
     */
    private boolean isAbnormalFlag(Integer abnormalFlag) {
        return Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())
            || Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())
            || Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue());
    }

    /**
     * 构建成员排序说明。
     */
    private String buildMemberSortReason(long overdueCount, int pendingCount, int abnormalReportCount, List<String> chronicTags) {
        List<String> reasons = new ArrayList<>();
        if (overdueCount > 0) {
            reasons.add("存在已到点未处理提醒");
        }
        if (pendingCount > 0) {
            reasons.add("今日仍有待处理提醒");
        }
        if (abnormalReportCount > 0) {
            reasons.add("最近存在异常报告");
        }
        if (chronicTags != null && !chronicTags.isEmpty()) {
            reasons.add("命中慢病关注标签");
        }
        return reasons.isEmpty() ? "近期健康状态相对平稳" : StrUtil.join("，", reasons);
    }

    /**
     * 构建慢病/慢病风险标签。
     *
     * <p>当前首页标签采用“可解释的规则映射”：
     * 只根据最近报告里的异常指标名称做轻量归类，不直接做医学诊断。
     */
    private List<String> buildChronicTags(List<HealthReportItemEntity> reportItems) {
        if (reportItems == null || reportItems.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> tags = new HashSet<>();
        for (HealthReportItemEntity item : reportItems) {
            if (!isAbnormalFlag(item.getAbnormalFlag())) {
                continue;
            }
            String itemName = StrUtil.blankToDefault(item.getItemName(), "");
            if (StrUtil.containsAnyIgnoreCase(itemName, "血糖", "糖化", "葡萄糖")) {
                tags.add("糖代谢关注");
            }
            if (StrUtil.containsAnyIgnoreCase(itemName, "血压", "收缩压", "舒张压")) {
                tags.add("血压关注");
            }
            if (StrUtil.containsAnyIgnoreCase(itemName, "胆固醇", "甘油三酯", "低密度", "高密度", "血脂")) {
                tags.add("血脂关注");
            }
            if (StrUtil.containsAnyIgnoreCase(itemName, "尿酸")) {
                tags.add("尿酸关注");
            }
            if (StrUtil.containsAnyIgnoreCase(itemName, "肌酐", "尿素", "肾")) {
                tags.add("肾功能关注");
            }
            if (StrUtil.containsAnyIgnoreCase(itemName, "转氨酶", "谷丙", "谷草", "肝")) {
                tags.add("肝功能关注");
            }
            if (StrUtil.containsAnyIgnoreCase(itemName, "甲状腺", "TSH", "FT3", "FT4")) {
                tags.add("甲状腺关注");
            }
        }
        return tags.stream().sorted().limit(3).collect(Collectors.toList());
    }

    /**
     * 构建成员级排序权重映射，用于首页任务流个性化排序。
     */
    private Map<Long, Integer> buildMemberSortWeightMap(List<HealthMedicationReminderEntity> todayReminders,
        List<HealthReportEntity> recentReports, Map<Long, Integer> abnormalCountMap) {
        Map<Long, Integer> memberSortWeightMap = new HashMap<>();
        Date now = new Date();
        todayReminders.forEach(reminder -> {
            if (reminder.getMemberId() == null) {
                return;
            }
            int weight = Objects.equals(reminder.getReminderStatus(), MedicationReminderStatusEnum.PENDING.getValue())
                ? 12 : 0;
            if (reminder.getScheduledTime() != null && !reminder.getScheduledTime().after(now)
                && Objects.equals(reminder.getReminderStatus(), MedicationReminderStatusEnum.PENDING.getValue())) {
                weight += 20;
            }
            memberSortWeightMap.merge(reminder.getMemberId(), weight, Integer::sum);
        });
        recentReports.forEach(report -> {
            if (report.getMemberId() == null) {
                return;
            }
            memberSortWeightMap.merge(report.getMemberId(),
                abnormalCountMap.getOrDefault(report.getReportId(), 0) * 6, Integer::sum);
        });
        return memberSortWeightMap;
    }

    /**
     * 首页待跟进事项比较器。
     */
    private int compareFollowUpItems(HealthHomeFollowUpItemDTO left, HealthHomeFollowUpItemDTO right) {
        int leftReadSort = Objects.equals(left.getReadStatus(), HealthAppMessageReadStatusEnum.READ.getValue()) ? 1 : 0;
        int rightReadSort = Objects.equals(right.getReadStatus(), HealthAppMessageReadStatusEnum.READ.getValue()) ? 1 : 0;
        if (leftReadSort != rightReadSort) {
            return Integer.compare(leftReadSort, rightReadSort);
        }

        int sortWeightCompare = Comparator.nullsLast(Integer::compareTo)
            .compare(right.getSortWeight(), left.getSortWeight());
        if (sortWeightCompare != 0) {
            return sortWeightCompare;
        }

        int priorityCompare = Comparator.nullsLast(Integer::compareTo)
            .compare(left.getPriority(), right.getPriority());
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        return Comparator.nullsLast(Date::compareTo).compare(left.getFollowUpTime(), right.getFollowUpTime());
    }

    /**
     * 解析运营任务优先级。
     */
    private int resolveOperationPriority(HealthOperationTaskEntity operationTask) {
        if (operationTask == null) {
            return 4;
        }
        String riskLevel = StrUtil.blankToDefault(operationTask.getRiskLevel(), "MEDIUM");
        if (Objects.equals(riskLevel, "HIGH")) {
            return 2;
        }
        if (Objects.equals(riskLevel, "LOW")) {
            return 4;
        }
        return 3;
    }

    /**
     * 构建首页健康趋势图数据。
     */
    private List<HealthHomeHealthTrendPointDTO> buildHealthTrendPoints(List<HealthReportEntity> recentReports,
        Map<Long, Integer> abnormalCountMap) {
        if (recentReports == null || recentReports.isEmpty()) {
            return Collections.emptyList();
        }
        // 趋势图只需要最近 N 份报告，而首页前面已经按更大的窗口把 recentReports 查出来了。
        // 这里直接截取并复用，避免同一次首页请求再补一条“最近报告”查询。
        List<HealthReportEntity> trendReports = recentReports.stream()
            .limit(HOME_HEALTH_TREND_LIMIT)
            .collect(Collectors.toList());
        List<HealthReportEntity> ascReports = new ArrayList<>(trendReports);
        Collections.reverse(ascReports);
        return ascReports.stream().map(report -> {
            HealthHomeHealthTrendPointDTO pointDTO = new HealthHomeHealthTrendPointDTO();
            Date statDate = report.getReportDate() == null ? report.getCreateTime() : report.getReportDate();
            pointDTO.setStatDate(statDate == null ? "-" : DateUtil.formatDate(statDate));
            int abnormalCount = abnormalCountMap.getOrDefault(report.getReportId(), 0);
            pointDTO.setAbnormalItemCount(abnormalCount);
            pointDTO.setAbnormalReportCount(abnormalCount > 0 ? 1 : 0);
            pointDTO.setHealthScore(Math.max(0D, 100D - abnormalCount * 12D));
            return pointDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 构建首页依从率趋势数据。
     */
    private List<HealthHomeAdherenceTrendPointDTO> buildAdherenceTrendPoints(Long ownerUserId) {
        Date endDate = DateUtil.endOfDay(new Date());
        Date startDate = DateUtil.beginOfDay(DateUtil.offsetDay(endDate, -(HOME_ADHERENCE_TREND_DAYS - 1)));
        List<HealthMedicationReminderEntity> reminders = medicationReminderService.lambdaQuery()
            .eq(HealthMedicationReminderEntity::getOwnerUserId, ownerUserId)
            .ge(HealthMedicationReminderEntity::getReminderDate, startDate)
            .le(HealthMedicationReminderEntity::getReminderDate, endDate)
            .orderByAsc(HealthMedicationReminderEntity::getReminderDate)
            .list();
        Map<Long, List<HealthMedicationReminderEntity>> groupedReminders = reminders.stream()
            .filter(item -> item.getReminderDate() != null)
            .collect(Collectors.groupingBy(item -> DateUtil.beginOfDay(item.getReminderDate()).getTime()));

        List<HealthHomeAdherenceTrendPointDTO> trendPoints = new ArrayList<>();
        Date loopDate = startDate;
        while (!loopDate.after(endDate)) {
            List<HealthMedicationReminderEntity> dayReminders =
                groupedReminders.getOrDefault(DateUtil.beginOfDay(loopDate).getTime(), Collections.emptyList());
            int takenCount = countReminderByStatus(dayReminders, MedicationReminderStatusEnum.TAKEN.getValue());
            int skippedCount = countReminderByStatus(dayReminders, MedicationReminderStatusEnum.SKIPPED.getValue());
            int expiredCount = countReminderByStatus(dayReminders, MedicationReminderStatusEnum.EXPIRED.getValue());
            int denominator = takenCount + skippedCount + expiredCount;

            HealthHomeAdherenceTrendPointDTO pointDTO = new HealthHomeAdherenceTrendPointDTO();
            pointDTO.setStatDate(DateUtil.formatDate(loopDate));
            pointDTO.setTakenReminderCount(takenCount);
            pointDTO.setSkippedReminderCount(skippedCount);
            pointDTO.setExpiredReminderCount(expiredCount);
            pointDTO.setAdherenceRate(denominator <= 0 ? 0D : roundRate(takenCount * 100D / denominator));
            trendPoints.add(pointDTO);
            loopDate = DateUtil.offsetDay(loopDate, 1);
        }
        return trendPoints;
    }

    /**
     * 构建首页 AI 健康建议。
     *
     * <p>当前阶段先采用规则化建议聚合：
     * 1. 稳定可解释
     * 2. 不依赖额外外部服务
     * 3. 后续如需切换到外部大模型，可在此基础上平滑替换摘要生成逻辑
     */
    private HealthHomeAiAdviceDTO buildAiHealthAdvice(List<HealthFamilyMemberEntity> members,
        List<HealthMedicationReminderEntity> todayReminders, List<HealthReportEntity> recentReports,
        List<HealthOperationTaskEntity> operationTasks, List<HealthHomeFollowUpItemDTO> followUpItems,
        Map<Long, Integer> abnormalCountMap) {
        int overdueReminderCount = (int) todayReminders.stream()
            .filter(reminder -> Objects.equals(reminder.getReminderStatus(), MedicationReminderStatusEnum.PENDING.getValue()))
            .filter(reminder -> reminder.getScheduledTime() != null && !reminder.getScheduledTime().after(new Date()))
            .count();
        int abnormalReportCount = (int) recentReports.stream()
            .filter(report -> abnormalCountMap.getOrDefault(report.getReportId(), 0) > 0)
            .count();
        int unreadFollowUpCount = (int) followUpItems.stream()
            .filter(item -> !Objects.equals(item.getReadStatus(), HealthAppMessageReadStatusEnum.READ.getValue()))
            .count();

        List<String> suggestions = new ArrayList<>();
        if (overdueReminderCount > 0) {
            suggestions.add(StrUtil.format("优先处理 {} 条已到点未确认的用药提醒，避免影响今日依从率。", overdueReminderCount));
        }
        if (abnormalReportCount > 0) {
            suggestions.add(StrUtil.format("最近有 {} 份异常报告需要继续跟进，建议先查看报告建议并确认是否需要线下复查。", abnormalReportCount));
        }
        if (!operationTasks.isEmpty()) {
            suggestions.add(StrUtil.format("首页当前还有 {} 条运营任务引导，建议按优先级逐条查看。", operationTasks.size()));
        }
        if (suggestions.isEmpty()) {
            suggestions.add("今日暂无高风险提醒，建议继续保持当前的服药与体检管理节奏。");
        }

        HealthHomeAiAdviceDTO adviceDTO = new HealthHomeAiAdviceDTO();
        adviceDTO.setTitle(resolveAiAdviceTitle(overdueReminderCount, abnormalReportCount, unreadFollowUpCount));
        adviceDTO.setSummary(buildAiAdviceSummary(members.size(), overdueReminderCount, abnormalReportCount, unreadFollowUpCount));
        fillAiRiskLevel(adviceDTO, overdueReminderCount, abnormalReportCount, unreadFollowUpCount);
        adviceDTO.setSourceType("RULE_ENGINE");
        adviceDTO.setSourceName("首页规则引擎");
        adviceDTO.setSuggestions(suggestions);
        adviceDTO.setGeneratedTime(new Date());
        return adviceDTO;
    }

    private String resolveAiAdviceTitle(int overdueReminderCount, int abnormalReportCount, int unreadFollowUpCount) {
        if (overdueReminderCount > 0 || abnormalReportCount > 1) {
            return "优先处理高关注健康事项";
        }
        if (unreadFollowUpCount > 0 || abnormalReportCount > 0) {
            return "建议尽快完成今日健康跟进";
        }
        return "今日健康状态整体平稳";
    }

    private String buildAiAdviceSummary(int memberCount, int overdueReminderCount, int abnormalReportCount,
        int unreadFollowUpCount) {
        return StrUtil.format("当前共关注 {} 位家庭成员，已到点未处理提醒 {} 条，最近异常报告 {} 份，未读待跟进事项 {} 条。"
                + "以下建议仅用于日常健康管理提醒，不代表医疗诊断结论。",
            memberCount, overdueReminderCount, abnormalReportCount, unreadFollowUpCount);
    }

    private void fillAiRiskLevel(HealthHomeAiAdviceDTO adviceDTO, int overdueReminderCount, int abnormalReportCount,
        int unreadFollowUpCount) {
        if (overdueReminderCount > 0 || abnormalReportCount > 1) {
            adviceDTO.setRiskLevel("HIGH");
            adviceDTO.setRiskLevelName(HealthAppI18n.followUpRiskLevelName("HIGH"));
            return;
        }
        if (abnormalReportCount > 0 || unreadFollowUpCount > 0) {
            adviceDTO.setRiskLevel("MEDIUM");
            adviceDTO.setRiskLevelName(HealthAppI18n.followUpRiskLevelName("MEDIUM"));
            return;
        }
        adviceDTO.setRiskLevel("LOW");
        adviceDTO.setRiskLevelName(HealthAppI18n.followUpRiskLevelName("LOW"));
    }

    private double roundRate(double rate) {
        return Math.round(rate * 100D) / 100D;
    }
}
