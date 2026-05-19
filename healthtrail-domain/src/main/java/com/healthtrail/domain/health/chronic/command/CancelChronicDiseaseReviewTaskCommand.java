package com.healthtrail.domain.health.chronic.command;

import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 取消慢病专项复查任务命令。
 *
 * <p>取消动作只关闭已经生成的首页待跟进任务，不删除慢病专项档案、报告指标或健康问题。
 * 这样用户可以撤回一次错误安排的复查提醒，同时保留后续重新创建复查任务的能力。
 */
@Data
public class CancelChronicDiseaseReviewTaskCommand {

    /**
     * 取消备注。
     *
     * <p>备注会追加到任务备注中，便于后续从任务记录里看出该复查提醒为什么被用户取消。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
