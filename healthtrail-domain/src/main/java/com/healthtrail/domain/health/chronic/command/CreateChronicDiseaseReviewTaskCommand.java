package com.healthtrail.domain.health.chronic.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 创建慢病专项复查任务命令。
 *
 * <p>复查任务只描述“何时提醒用户回到慢病专项复盘”，
 * 不直接写报告指标或医学结论。真正的趋势和异常仍由慢病专项详情实时从报告数据聚合，
 * 这样任务不会因为后续报告更新而变成过期快照。
 */
@Data
public class CreateChronicDiseaseReviewTaskCommand {

    /**
     * 复查提醒日期。
     *
     * <p>为空时后端默认使用 30 天后，方便 App 提供“一键创建复查任务”的快捷动作。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date reviewDate;

    /**
     * 任务备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
