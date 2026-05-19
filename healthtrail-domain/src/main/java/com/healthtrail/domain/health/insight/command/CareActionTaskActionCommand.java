package com.healthtrail.domain.health.insight.command;

import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 家庭照护任务处理命令。
 *
 * <p>照护任务仍然使用首页任务流承载，完成或取消时只更新任务状态和备注。
 * 这里不新建处理日志表，是为了先让二期闭环可用；后续如果需要多人协作审计，再单独扩展历史表。
 */
@Data
public class CareActionTaskActionCommand {

    @Size(max = 500, message = "处理说明长度不能超过500个字符")
    /** 备注。 */
    private String remark;
}
