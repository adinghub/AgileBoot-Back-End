package com.healthtrail.domain.health.dashboard;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.dashboard.command.AddHealthOperationTaskCommand;
import com.healthtrail.domain.health.dashboard.command.UpdateHealthOperationTaskCommand;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskEntity;
import com.healthtrail.domain.health.dashboard.db.HealthOperationTaskService;
import com.healthtrail.domain.health.dashboard.dto.HealthOperationTaskDTO;
import com.healthtrail.domain.health.dashboard.query.HealthOperationTaskQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 首页运营任务应用服务。
 *
 * <p>该服务面向后台管理端，负责维护首页任务流中的运营型任务。
 * 运营任务和提醒、报告任务不同，它不是由底层业务自动派生出来的，
 * 而是由后台人工配置后，在首页按生效时间窗口投放给指定用户。
 */
@Service
@RequiredArgsConstructor
public class HealthOperationTaskApplicationService {

    /** 首页运营任务数据库服务 */
    private final HealthOperationTaskService operationTaskService;

    /**
     * 分页查询运营任务列表。
     */
    public PageDTO<HealthOperationTaskDTO> getTaskPage(HealthOperationTaskQuery query) {
        Page<HealthOperationTaskEntity> page = operationTaskService.page(query.toPage(), query.toQueryWrapper());
        List<HealthOperationTaskDTO> records = page.getRecords().stream()
            .map(HealthOperationTaskDTO::new)
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 查询单个运营任务详情。
     */
    public HealthOperationTaskDTO getTaskInfo(Long operationTaskId) {
        return new HealthOperationTaskDTO(loadOperationTask(operationTaskId));
    }

    /**
     * 新增运营任务。
     */
    public void addTask(AddHealthOperationTaskCommand command) {
        HealthOperationTaskEntity entity = new HealthOperationTaskEntity();
        BeanUtil.copyProperties(command, entity, "operationTaskId");
        fillDefaultFields(entity);
        validateFields(entity);
        operationTaskService.save(entity);
    }

    /**
     * 修改运营任务。
     */
    public void updateTask(UpdateHealthOperationTaskCommand command) {
        HealthOperationTaskEntity entity = loadOperationTask(command.getOperationTaskId());
        BeanUtil.copyProperties(command, entity, "operationTaskId");
        fillDefaultFields(entity);
        validateFields(entity);
        operationTaskService.updateById(entity);
    }

    /**
     * 删除运营任务。
     */
    public void removeTask(Long operationTaskId) {
        loadOperationTask(operationTaskId);
        operationTaskService.removeById(operationTaskId);
    }

    /**
     * 查询指定用户当前生效中的运营任务。
     *
     * <p>这里专门提供给首页聚合服务使用，只返回“当前时刻可被首页消费”的任务，
     * 避免首页自己再重复拼接时间窗口和启用状态判断。
     */
    public List<HealthOperationTaskEntity> listActiveTasks(Long ownerUserId) {
        Date now = new Date();
        return operationTaskService.lambdaQuery()
            .eq(HealthOperationTaskEntity::getOwnerUserId, ownerUserId)
            .eq(HealthOperationTaskEntity::getStatus, StatusEnum.ENABLE.getValue())
            .le(HealthOperationTaskEntity::getStartTime, now)
            .and(wrapper -> wrapper.isNull(HealthOperationTaskEntity::getEndTime)
                .or()
                .ge(HealthOperationTaskEntity::getEndTime, now))
            .orderByDesc(HealthOperationTaskEntity::getPriorityWeight)
            .orderByDesc(HealthOperationTaskEntity::getOperationTaskId)
            .list();
    }

    private HealthOperationTaskEntity loadOperationTask(Long operationTaskId) {
        HealthOperationTaskEntity entity = operationTaskService.getById(operationTaskId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.HEALTH_OPERATION_TASK_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 填充默认字段。
     */
    private void fillDefaultFields(HealthOperationTaskEntity entity) {
        entity.setTaskTitle(limitLength(StrUtil.trim(entity.getTaskTitle()), 100));
        entity.setTaskContent(limitLength(StrUtil.trim(entity.getTaskContent()), 500));
        entity.setActionText(limitLength(StrUtil.blankToDefault(StrUtil.trim(entity.getActionText()), "去查看"), 50));
        entity.setRiskLevel(resolveRiskLevel(entity.getRiskLevel()));
        entity.setPriorityWeight(entity.getPriorityWeight() == null ? 0 : entity.getPriorityWeight());
        entity.setTargetPageCode(limitLength(StrUtil.trim(entity.getTargetPageCode()), 50));
        entity.setTargetBizType(limitLength(StrUtil.trim(entity.getTargetBizType()), 30));
        entity.setTargetTabCode(limitLength(StrUtil.trim(entity.getTargetTabCode()), 30));
        entity.setTargetAnchorCode(limitLength(StrUtil.trim(entity.getTargetAnchorCode()), 50));
        entity.setRemark(limitLength(StrUtil.trim(entity.getRemark()), 500));
        entity.setStatus(entity.getStatus() == null ? StatusEnum.ENABLE.getValue() : entity.getStatus());
        fillNavigationDisplayName(entity);
    }

    /**
     * 运营任务字段校验。
     */
    private void validateFields(HealthOperationTaskEntity entity) {
        if (StrUtil.isBlank(entity.getTaskTitle())) {
            throw new ApiException(ErrorCode.Business.HEALTH_OPERATION_TASK_TITLE_REQUIRED);
        }
        if (entity.getStartTime() != null && entity.getEndTime() != null
            && entity.getStartTime().after(entity.getEndTime())) {
            throw new ApiException(ErrorCode.Business.HEALTH_OPERATION_TASK_TIME_RANGE_INVALID);
        }
    }

    /**
     * 回填页面名称和锚点名称。
     *
     * <p>后台表中同时保存编码和值名称，目的是：
     * 1. 首页接口可以直接把中文快照返回给 App，降低前端字典维护成本
     * 2. 即使后续枚举描述调整，历史任务也仍然保留当时的展示文案
     */
    private void fillNavigationDisplayName(HealthOperationTaskEntity entity) {
        if (StrUtil.isBlank(entity.getTargetPageName())) {
            HealthFollowUpTargetPageEnum pageEnum = HealthFollowUpTargetPageEnum.fromValue(entity.getTargetPageCode());
        entity.setTargetPageName(pageEnum == null ? null : HealthAppI18n.targetPageName(pageEnum.getValue()));
        }
        if (StrUtil.isBlank(entity.getTargetAnchorName())) {
            HealthFollowUpTargetAnchorEnum anchorEnum = HealthFollowUpTargetAnchorEnum.fromValue(entity.getTargetAnchorCode());
        entity.setTargetAnchorName(anchorEnum == null ? null : HealthAppI18n.targetAnchorName(anchorEnum.getValue()));
        }
        if (entity.getStartTime() == null) {
            entity.setStartTime(DateUtil.beginOfMinute(new Date()));
        }
    }

    private String resolveRiskLevel(String riskLevel) {
        HealthFollowUpRiskLevelEnum riskLevelEnum = HealthFollowUpRiskLevelEnum.fromValue(riskLevel);
        return riskLevelEnum == null ? HealthFollowUpRiskLevelEnum.MEDIUM.getValue() : riskLevelEnum.getValue();
    }

    private String limitLength(String text, int maxLength) {
        if (StrUtil.isBlank(text) || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
