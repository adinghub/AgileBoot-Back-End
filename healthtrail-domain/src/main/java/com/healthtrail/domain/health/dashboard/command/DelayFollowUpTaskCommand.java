package com.healthtrail.domain.health.dashboard.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 延后首页待跟进任务命令。
 *
 * <p>延后操作不会直接修改原始提醒时间或报告日期，
 * 只会影响该任务在首页任务流中的再次出现时间。
 */
@Data
public class DelayFollowUpTaskCommand {

    /**
     * 任务类型。
     * 当前支持：
     * 1. REMINDER
     * 2. REPORT_ADVICE
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    /**
     * 来源业务ID。
     * 例如提醒ID、报告ID。
     */
    @NotNull(message = "来源业务ID不能为空")
    private Long sourceId;

    /**
     * 延后到什么时间再提醒。
     *
     * <p>如果前端希望自己精确指定时间，可以直接传这个字段；
     * 如果前端只想使用固定快捷选项，则可以不传该字段，改传 `delayOption`。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delayedUntil;

    /**
     * 快捷延后选项。
     * 当前支持：
     * 1. AFTER_30_MINUTES
     * 2. TONIGHT
     * 3. TOMORROW_MORNING
     */
    private String delayOption;

    /**
     * 操作备注。
     */
    private String remark;
}
