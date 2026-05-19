package com.healthtrail.domain.health.report.command;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 覆盖保存体检报告指标命令。
 *
 * <p>当前接口采用“整份覆盖”的提交方式，
 * 因此前端每次保存时提交这份报告当前完整的指标列表即可。
 */
@Data
public class SaveHealthReportItemsCommand {

    /**
     * 指标列表。
     *
     * <p>这里允许传空数组，表示清空当前报告下的全部指标结果。
     * 但不允许整个字段缺失，以避免前端因为漏传字段导致意外清空或无法识别意图。
     */
    @Valid
    @NotNull(message = "指标列表不能为空")
    private List<HealthReportItemCommand> items;
}
