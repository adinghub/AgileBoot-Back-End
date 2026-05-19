package com.healthtrail.domain.health.insight.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 创建家庭照护任务命令。
 *
 * <p>家庭照护任务复用首页运营任务表，用 `CARE_ACTION` 标识业务类型。
 * 这样手动照护事项、慢病复查任务和健康问题复查任务都能进入同一个待办流，
 * App 不需要为家庭协作再维护一套孤立的任务状态。
 */
@Data
public class CreateCareActionTaskCommand {

    @NotNull(message = "家庭成员不能为空")
    /** 家庭成员ID */
    private Long memberId;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "任务标题长度不能超过100个字符")
    /** 任务标题 */
    private String taskTitle;

    @Size(max = 500, message = "任务内容长度不能超过500个字符")
    /** 任务内容 */
    private String taskContent;

    @Size(max = 30, message = "动作文案长度不能超过30个字符")
    /** 操作按钮文案 */
    private String actionText;

    @Size(max = 20, message = "风险等级长度不能超过20个字符")
    /** 风险等级 */
    private String riskLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 生效开始时间 */
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 生效结束时间 */
    private Date endTime;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    /** 备注 */
    private String remark;
}
