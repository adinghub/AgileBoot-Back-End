package com.healthtrail.domain.health.chronic.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改慢病病种配置命令。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateChronicDiseaseTypeCommand extends SaveChronicDiseaseTypeCommand {

    /** 当前被修改的病种配置 ID，由后台路径写入，避免页面隐藏字段被篡改。 */
    private Long typeId;
}