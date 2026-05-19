package com.healthtrail.domain.health.insight.command;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 更新健康问题状态命令。
 *
 * <p>健康问题中心不是疾病诊断系统，它只维护“关注主题”的跟进状态。
 * 这里仅允许把问题置为跟进中、已缓解、已关闭，避免 App 传入任意状态值。
 */
@Data
public class UpdateHealthProblemStatusCommand {

    @NotNull(message = "问题状态不能为空")
    @Min(value = 1, message = "问题状态不正确")
    @Max(value = 3, message = "问题状态不正确")
    /** 问题状态。 */
    private Integer problemStatus;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    /** 备注。 */
    private String remark;
}
