package com.healthtrail.domain.health.medication.command;

import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 跳过提醒命令对象。
 */
@Data
public class SkipMedicationReminderCommand {

    /**
     * 跳过原因。
     */
    @Size(max = 255, message = "跳过原因长度不能超过255个字符")
    private String skipReason;
}
