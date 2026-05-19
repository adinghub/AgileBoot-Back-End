package com.healthtrail.domain.health.dashboard.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 新增首页运营任务命令。
 */
@Data
public class AddHealthOperationTaskCommand {

    /**
     * 目标 App 用户ID。
     */
    @NotNull(message = "目标App用户ID不能为空")
    private Long ownerUserId;

    /**
     * 可选的目标家庭成员ID。
     */
    private Long memberId;

    /**
     * 任务标题。
     */
    @Size(max = 100, message = "任务标题长度不能超过100个字符")
    private String taskTitle;

    /**
     * 任务内容。
     */
    @Size(max = 500, message = "任务内容长度不能超过500个字符")
    private String taskContent;

    /**
     * 动作按钮文案。
     */
    @Size(max = 50, message = "动作按钮文案长度不能超过50个字符")
    private String actionText;

    /**
     * 风险等级，支持 LOW / MEDIUM / HIGH。
     */
    @Size(max = 20, message = "风险等级长度不能超过20个字符")
    private String riskLevel;

    /**
     * 排序权重。
     */
    private Integer priorityWeight;

    /**
     * 目标页面编码。
     */
    @Size(max = 50, message = "目标页面编码长度不能超过50个字符")
    private String targetPageCode;

    /**
     * 目标页面名称。
     */
    @Size(max = 50, message = "目标页面名称长度不能超过50个字符")
    private String targetPageName;

    /**
     * 目标业务主键ID。
     */
    private Long targetBizId;

    /**
     * 目标业务类型。
     */
    @Size(max = 30, message = "目标业务类型长度不能超过30个字符")
    private String targetBizType;

    /**
     * 目标标签编码。
     */
    @Size(max = 30, message = "目标标签编码长度不能超过30个字符")
    private String targetTabCode;

    /**
     * 目标锚点编码。
     */
    @Size(max = 50, message = "目标锚点编码长度不能超过50个字符")
    private String targetAnchorCode;

    /**
     * 目标锚点名称。
     */
    @Size(max = 50, message = "目标锚点名称长度不能超过50个字符")
    private String targetAnchorName;

    /**
     * 生效开始时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 生效结束时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 状态，默认启用。
     */
    private Integer status;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
