package com.healthtrail.common.utils.i18n;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.health.FamilyMemberAccessRoleEnum;
import com.healthtrail.common.enums.health.FamilyMemberAccessSourceEnum;
import com.healthtrail.common.enums.health.FamilyMemberShareStatusEnum;
import com.healthtrail.common.enums.health.FamilyShareInviteStatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskActionTypeEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskTypeEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import java.util.Objects;

/**
 * App 端统一国际化辅助工具。
 *
 * <p>当前项目里很多返回字段不是直接把枚举值给前端，而是会额外回填一个可展示名称。
 * 为了让这些“名称字段”在中英文环境下都保持统一口径，这里集中封装一层：
 * 1. 业务代码只关心“我现在要返回哪类名称”
 * 2. 国际化消息键、降级文案和枚举映射都统一放在这里维护
 * 3. 某个名称后续要新增多语言时，不需要再去各业务服务里逐个翻找替换
 */
public final class HealthAppI18n {

    private HealthAppI18n() {
    }

    /**
     * 读取国际化消息，缺失时优先回退到调用方给出的默认文案。
     *
     * <p>之所以做这一层 try-catch，是因为 App 端展示字段比异常消息更强调“稳定返回”，
     * 就算某个 key 漏配了，也不能让整个接口直接报错。
     */
    public static String text(String code, String fallback, Object... args) {
        try {
            return MessageUtils.message(code, args);
        } catch (Exception ex) {
            return formatFallback(fallback, args);
        }
    }

    /**
     * 已删除成员兜底名称。
     */
    public static String deletedMemberName() {
        return text("health.app.common.deletedMember", "已删除成员");
    }

    /**
     * 已删除用户兜底名称。
     */
    public static String deletedUserName() {
        return text("health.app.common.deletedUser", "已删除用户");
    }

    /**
     * 未知状态兜底名称。
     */
    public static String unknownName() {
        return text("health.app.common.unknown", "未知");
    }

    /**
     * 家庭成员兜底名称。
     */
    public static String defaultMemberName() {
        return text("health.app.common.defaultMember", "家庭成员");
    }

    /**
     * App 消息场景名称。
     */
    public static String appMessageSceneName(String sceneCode) {
        HealthAppMessageSceneEnum sceneEnum = HealthAppMessageSceneEnum.fromValue(sceneCode);
        if (sceneEnum == null) {
            return sceneCode;
        }
        if (Objects.equals(sceneEnum, HealthAppMessageSceneEnum.MEDICATION_REMINDER)) {
            return text("health.app.message.scene.medicationReminder", sceneEnum.getDescription());
        }
        if (Objects.equals(sceneEnum, HealthAppMessageSceneEnum.REPORT_FOLLOW_UP)) {
            return text("health.app.message.scene.reportFollowUp", sceneEnum.getDescription());
        }
        if (Objects.equals(sceneEnum, HealthAppMessageSceneEnum.LOW_STOCK_ALERT)) {
            return text("health.app.message.scene.lowStockAlert", sceneEnum.getDescription());
        }
        return sceneEnum.getDescription();
    }

    /**
     * App 消息发送状态名称。
     */
    public static String appMessageSendStatusName(Integer status) {
        if (status == null) {
            return unknownName();
        }
        if (Objects.equals(status, HealthAppMessageSendStatusEnum.PENDING.getValue())) {
            return text("health.app.message.sendStatus.pending", HealthAppMessageSendStatusEnum.PENDING.description());
        }
        if (Objects.equals(status, HealthAppMessageSendStatusEnum.SUCCESS.getValue())) {
            return text("health.app.message.sendStatus.success", HealthAppMessageSendStatusEnum.SUCCESS.description());
        }
        if (Objects.equals(status, HealthAppMessageSendStatusEnum.FAILED.getValue())) {
            return text("health.app.message.sendStatus.failed", HealthAppMessageSendStatusEnum.FAILED.description());
        }
        return Objects.toString(status, null);
    }

    /**
     * 首页推荐动作风险等级名称。
     */
    public static String followUpRiskLevelName(String riskLevel) {
        HealthFollowUpRiskLevelEnum riskLevelEnum = HealthFollowUpRiskLevelEnum.fromValue(riskLevel);
        if (riskLevelEnum == null) {
            return riskLevel;
        }
        if (Objects.equals(riskLevelEnum, HealthFollowUpRiskLevelEnum.HIGH)) {
            return text("health.app.followUp.risk.high", riskLevelEnum.description());
        }
        if (Objects.equals(riskLevelEnum, HealthFollowUpRiskLevelEnum.MEDIUM)) {
            return text("health.app.followUp.risk.medium", riskLevelEnum.description());
        }
        if (Objects.equals(riskLevelEnum, HealthFollowUpRiskLevelEnum.LOW)) {
            return text("health.app.followUp.risk.low", riskLevelEnum.description());
        }
        return riskLevelEnum.description();
    }

    /**
     * 用药提醒状态名称。
     */
    public static String medicationReminderStatusName(Integer status) {
        if (status == null) {
            return unknownName();
        }
        if (Objects.equals(status, MedicationReminderStatusEnum.PENDING.getValue())) {
            return text("health.app.medication.reminderStatus.pending", MedicationReminderStatusEnum.PENDING.description());
        }
        if (Objects.equals(status, MedicationReminderStatusEnum.TAKEN.getValue())) {
            return text("health.app.medication.reminderStatus.taken", MedicationReminderStatusEnum.TAKEN.description());
        }
        if (Objects.equals(status, MedicationReminderStatusEnum.SKIPPED.getValue())) {
            return text("health.app.medication.reminderStatus.skipped", MedicationReminderStatusEnum.SKIPPED.description());
        }
        if (Objects.equals(status, MedicationReminderStatusEnum.EXPIRED.getValue())) {
            return text("health.app.medication.reminderStatus.expired", MedicationReminderStatusEnum.EXPIRED.description());
        }
        return unknownName();
    }

    /**
     * 服药时机名称。
     */
    public static String mealTimingName(String mealTiming) {
        if (StrUtil.isBlank(mealTiming)) {
            return mealTiming;
        }
        if (Objects.equals("BEFORE_MEAL", mealTiming)) {
            return text("health.app.medication.mealTiming.beforeMeal", "饭前");
        }
        if (Objects.equals("AFTER_MEAL", mealTiming)) {
            return text("health.app.medication.mealTiming.afterMeal", "饭后");
        }
        if (Objects.equals("WITH_MEAL", mealTiming)) {
            return text("health.app.medication.mealTiming.withMeal", "随餐");
        }
        if (Objects.equals("BEFORE_SLEEP", mealTiming)) {
            return text("health.app.medication.mealTiming.beforeSleep", "睡前");
        }
        if (Objects.equals("NO_LIMIT", mealTiming)) {
            return text("health.app.medication.mealTiming.noLimit", "不限");
        }
        return mealTiming;
    }

    /**
     * 成员访问来源名称。
     */
    public static String familyAccessSourceName(String accessSource) {
        if (Objects.equals(accessSource, FamilyMemberAccessSourceEnum.OWNER.getValue())) {
            return text("health.app.family.accessSource.owner", FamilyMemberAccessSourceEnum.OWNER.description());
        }
        if (Objects.equals(accessSource, FamilyMemberAccessSourceEnum.SHARED.getValue())) {
            return text("health.app.family.accessSource.shared", FamilyMemberAccessSourceEnum.SHARED.description());
        }
        return accessSource;
    }

    /**
     * 成员访问角色名称。
     */
    public static String familyAccessRoleName(String accessRole) {
        if (Objects.equals(accessRole, FamilyMemberAccessRoleEnum.OWNER.getValue())) {
            return text("health.app.family.accessRole.owner", FamilyMemberAccessRoleEnum.OWNER.description());
        }
        if (Objects.equals(accessRole, FamilyMemberAccessRoleEnum.EDITOR.getValue())) {
            return text("health.app.family.accessRole.editor", FamilyMemberAccessRoleEnum.EDITOR.description());
        }
        if (Objects.equals(accessRole, FamilyMemberAccessRoleEnum.VIEWER.getValue())) {
            return text("health.app.family.accessRole.viewer", FamilyMemberAccessRoleEnum.VIEWER.description());
        }
        return accessRole;
    }

    /**
     * 成员共享状态名称。
     */
    public static String familyShareStatusName(Integer shareStatus) {
        if (shareStatus == null) {
            return null;
        }
        if (Objects.equals(shareStatus, FamilyMemberShareStatusEnum.DISABLED.getValue())) {
            return text("health.app.family.shareStatus.disabled", FamilyMemberShareStatusEnum.DISABLED.description());
        }
        if (Objects.equals(shareStatus, FamilyMemberShareStatusEnum.ENABLED.getValue())) {
            return text("health.app.family.shareStatus.enabled", FamilyMemberShareStatusEnum.ENABLED.description());
        }
        return Objects.toString(shareStatus, null);
    }

    /**
     * 成员共享邀请状态名称。
     */
    public static String familyInviteStatusName(Integer inviteStatus) {
        if (inviteStatus == null) {
            return null;
        }
        if (Objects.equals(inviteStatus, FamilyShareInviteStatusEnum.PENDING.getValue())) {
            return text("health.app.family.inviteStatus.pending", FamilyShareInviteStatusEnum.PENDING.description());
        }
        if (Objects.equals(inviteStatus, FamilyShareInviteStatusEnum.ACCEPTED.getValue())) {
            return text("health.app.family.inviteStatus.accepted", FamilyShareInviteStatusEnum.ACCEPTED.description());
        }
        if (Objects.equals(inviteStatus, FamilyShareInviteStatusEnum.CANCELED.getValue())) {
            return text("health.app.family.inviteStatus.canceled", FamilyShareInviteStatusEnum.CANCELED.description());
        }
        if (Objects.equals(inviteStatus, FamilyShareInviteStatusEnum.EXPIRED.getValue())) {
            return text("health.app.family.inviteStatus.expired", FamilyShareInviteStatusEnum.EXPIRED.description());
        }
        return Objects.toString(inviteStatus, null);
    }

    /**
     * 首页任务类型名称。
     */
    public static String followUpTaskTypeName(String taskType) {
        HealthFollowUpTaskTypeEnum taskTypeEnum = HealthFollowUpTaskTypeEnum.fromValue(taskType);
        if (taskTypeEnum == null) {
            return taskType;
        }
        if (Objects.equals(taskTypeEnum, HealthFollowUpTaskTypeEnum.REMINDER)) {
            return text("health.app.followUp.taskType.reminder", taskTypeEnum.getDescription());
        }
        if (Objects.equals(taskTypeEnum, HealthFollowUpTaskTypeEnum.REPORT_ADVICE)) {
            return text("health.app.followUp.taskType.reportAdvice", taskTypeEnum.getDescription());
        }
        if (Objects.equals(taskTypeEnum, HealthFollowUpTaskTypeEnum.OPERATION)) {
            return text("health.app.followUp.taskType.operation", taskTypeEnum.getDescription());
        }
        return taskTypeEnum.getDescription();
    }

    /**
     * 首页任务状态名称。
     */
    public static String followUpTaskStatusName(Integer taskStatus) {
        if (taskStatus == null) {
            return null;
        }
        if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.PENDING.getValue())) {
            return text("health.app.followUp.taskStatus.pending", HealthFollowUpTaskStatusEnum.PENDING.description());
        }
        if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.DELAYED.getValue())) {
            return text("health.app.followUp.taskStatus.delayed", HealthFollowUpTaskStatusEnum.DELAYED.description());
        }
        if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.COMPLETED.getValue())) {
            return text("health.app.followUp.taskStatus.completed", HealthFollowUpTaskStatusEnum.COMPLETED.description());
        }
        if (Objects.equals(taskStatus, HealthFollowUpTaskStatusEnum.IGNORED.getValue())) {
            return text("health.app.followUp.taskStatus.ignored", HealthFollowUpTaskStatusEnum.IGNORED.description());
        }
        return Objects.toString(taskStatus, null);
    }

    /**
     * 首页任务动作名称。
     */
    public static String followUpTaskActionName(String actionType) {
        HealthFollowUpTaskActionTypeEnum actionTypeEnum = HealthFollowUpTaskActionTypeEnum.fromValue(actionType);
        if (actionTypeEnum == null) {
            return actionType;
        }
        if (Objects.equals(actionTypeEnum, HealthFollowUpTaskActionTypeEnum.READ)) {
            return text("health.app.followUp.taskAction.read", actionTypeEnum.description());
        }
        if (Objects.equals(actionTypeEnum, HealthFollowUpTaskActionTypeEnum.DELAY)) {
            return text("health.app.followUp.taskAction.delay", actionTypeEnum.description());
        }
        if (Objects.equals(actionTypeEnum, HealthFollowUpTaskActionTypeEnum.COMPLETE)) {
            return text("health.app.followUp.taskAction.complete", actionTypeEnum.description());
        }
        if (Objects.equals(actionTypeEnum, HealthFollowUpTaskActionTypeEnum.IGNORE)) {
            return text("health.app.followUp.taskAction.ignore", actionTypeEnum.description());
        }
        if (Objects.equals(actionTypeEnum, HealthFollowUpTaskActionTypeEnum.RESTORE)) {
            return text("health.app.followUp.taskAction.restore", actionTypeEnum.description());
        }
        return actionTypeEnum.description();
    }

    /**
     * 跳转页面名称。
     */
    public static String targetPageName(String targetPageCode) {
        HealthFollowUpTargetPageEnum pageEnum = HealthFollowUpTargetPageEnum.fromValue(targetPageCode);
        if (pageEnum == null) {
            return targetPageCode;
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.MEDICATION_REMINDER_DETAIL)) {
            return text("health.app.followUp.targetPage.medicationReminderDetail", pageEnum.getDescription());
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL)) {
            return text("health.app.followUp.targetPage.healthReportDetail", pageEnum.getDescription());
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.MESSAGE_CENTER)) {
            return text("health.app.followUp.targetPage.messageCenter", pageEnum.getDescription());
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.WORKSPACE_PLAN_LIST)) {
            return text("health.app.followUp.targetPage.planList", pageEnum.getDescription());
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.WORKSPACE_REMINDER_LIST)) {
            return text("health.app.followUp.targetPage.reminderList", pageEnum.getDescription());
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.WORKSPACE_DRUG_CABINET)) {
            return text("health.app.followUp.targetPage.drugCabinet", pageEnum.getDescription());
        }
        if (Objects.equals(pageEnum, HealthFollowUpTargetPageEnum.WORKSPACE_CHRONIC_DISEASE)) {
            return text("health.app.followUp.targetPage.chronicDisease", pageEnum.getDescription());
        }
        return pageEnum.getDescription();
    }

    /**
     * 跳转锚点名称。
     */
    public static String targetAnchorName(String targetAnchorCode) {
        HealthFollowUpTargetAnchorEnum anchorEnum = HealthFollowUpTargetAnchorEnum.fromValue(targetAnchorCode);
        if (anchorEnum == null) {
            return targetAnchorCode;
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.REMINDER_FEEDBACK)) {
            return text("health.app.followUp.targetAnchor.reminderFeedback", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.REPORT_ADVICE)) {
            return text("health.app.followUp.targetAnchor.reportAdvice", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.REPORT_ABNORMAL_ITEMS)) {
            return text("health.app.followUp.targetAnchor.reportAbnormalItems", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.REPORT_ORIGINAL_FILE)) {
            return text("health.app.followUp.targetAnchor.reportOriginalFile", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.MESSAGE_LIST)) {
            return text("health.app.followUp.targetAnchor.messageList", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.PLAN_LIST)) {
            return text("health.app.followUp.targetAnchor.planList", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.REMINDER_LIST)) {
            return text("health.app.followUp.targetAnchor.reminderList", anchorEnum.getDescription());
        }
        if (Objects.equals(anchorEnum, HealthFollowUpTargetAnchorEnum.CHRONIC_DISEASE_DASHBOARD)) {
            return text("health.app.followUp.targetAnchor.chronicDiseaseDashboard", anchorEnum.getDescription());
        }
        return anchorEnum.getDescription();
    }

    /**
     * 报告指标异常标记名称。
     */
    public static String reportAbnormalFlagName(Integer abnormalFlag) {
        if (abnormalFlag == null) {
            return null;
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.UNKNOWN.getValue())) {
            return text("health.app.report.abnormalFlag.unknown", HealthReportItemAbnormalFlagEnum.UNKNOWN.description());
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.NORMAL.getValue())) {
            return text("health.app.report.abnormalFlag.normal", HealthReportItemAbnormalFlagEnum.NORMAL.description());
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())) {
            return text("health.app.report.abnormalFlag.low", HealthReportItemAbnormalFlagEnum.LOW.description());
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())) {
            return text("health.app.report.abnormalFlag.high", HealthReportItemAbnormalFlagEnum.HIGH.description());
        }
        if (Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue())) {
            return text("health.app.report.abnormalFlag.abnormal", HealthReportItemAbnormalFlagEnum.ABNORMAL.description());
        }
        return Objects.toString(abnormalFlag, null);
    }

    /**
     * 轻量格式化兜底文案。
     *
     * <p>这里兼容 `MessageUtils` 读取失败后的常见场景：
     * 调用方仍然可能传了 `{}` 模板和参数，因此简单按顺序替换，保证回退文案可读。
     */
    private static String formatFallback(String fallback, Object... args) {
        if (fallback == null || args == null || args.length == 0) {
            return fallback;
        }
        String formattedText = fallback;
        for (Object arg : args) {
            formattedText = formattedText.replaceFirst("\\{\\}", java.util.regex.Matcher.quoteReplacement(Objects.toString(arg, "")));
        }
        return formattedText;
    }
}
