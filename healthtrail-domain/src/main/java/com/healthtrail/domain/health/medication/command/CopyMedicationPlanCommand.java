package com.healthtrail.domain.health.medication.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 复制用药计划命令。
 *
 * <p>“复制计划”并不是直接前端把整条计划重新提交一遍，
 * 而是以后端已有计划为模板，再允许用户覆盖少量常改字段：
 * 1. 目标成员
 * 2. 起止日期
 * 3. 状态
 * 4. 备注
 *
 * <p>这样能让“常用计划复制”场景足够轻量，避免用户重复录入完整计划。
 */
@Data
public class CopyMedicationPlanCommand {

    /**
     * 复制后的目标成员ID。
     *
     * <p>为空时表示沿用原计划成员。
     */
    private Long targetMemberId;

    /**
     * 复制后的开始日期。
     *
     * <p>为空时沿用原计划开始日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 复制后的结束日期。
     *
     * <p>为空时沿用原计划结束日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 复制后的计划状态。
     *
     * <p>为空时沿用原计划状态。
     */
    private Integer status;

    /**
     * 复制后的备注。
     *
     * <p>为空时沿用原计划备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
