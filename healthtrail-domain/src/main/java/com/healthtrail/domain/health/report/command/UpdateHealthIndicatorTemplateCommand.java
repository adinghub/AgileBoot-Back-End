package com.healthtrail.domain.health.report.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改指标模板命令。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateHealthIndicatorTemplateCommand extends AddHealthIndicatorTemplateCommand {

    /** templateId。 */
    private Long templateId;
}
