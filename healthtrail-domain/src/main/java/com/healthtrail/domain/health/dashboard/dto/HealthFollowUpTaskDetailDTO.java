package com.healthtrail.domain.health.dashboard.dto;

import java.util.List;
import lombok.Data;

/**
 * 首页待跟进任务详情 DTO。
 *
 * <p>任务中心详情页与任务中心列表页关注点不同：
 * 1. 列表页主要看“有哪些任务”
 * 2. 详情页需要看“这条任务当前信息 + 可执行操作 + 历史轨迹”
 *
 * <p>因此这里单独抽一个详情 DTO，
 * 避免前端再调用多个接口后自行拼装。
 */
@Data
public class HealthFollowUpTaskDetailDTO {

    /**
     * 任务基础信息。
     *
     * <p>这里直接复用列表 DTO，确保列表页和详情页对同一条任务的核心字段解释保持一致。
     */
    private HealthFollowUpTaskDTO taskInfo;

    /**
     * 原始来源记录当前是否仍然存在且可访问。
     *
     * <p>例如：
     * 1. 某条提醒后来被删除
     * 2. 某份报告后来被删除
     *
     * <p>这种情况下虽然历史任务记录仍然保留，
     * 但部分动作按钮应该禁用，避免前端继续提交无效操作。
     */
    private Boolean sourceAvailable;

    /**
     * 当前是否允许执行延后。
     */
    private Boolean canDelay;

    /**
     * 当前是否允许执行完成。
     *
     * <p>当前只有报告建议任务允许从首页任务流直接标记完成。
     */
    private Boolean canComplete;

    /**
     * 当前是否允许执行忽略。
     */
    private Boolean canIgnore;

    /**
     * 当前是否允许执行恢复。
     */
    private Boolean canRestore;

    /**
     * 推荐下一步动作。
     *
     * <p>详情页可优先展示该对象，
     * 帮助用户快速理解“现在最建议先做什么”。
     */
    private HealthFollowUpRecommendedActionDTO recommendedAction;

    /**
     * 任务操作时间线。
     */
    private List<HealthFollowUpTaskLogDTO> taskLogs;
}
