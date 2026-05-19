package com.healthtrail.domain.health.drug.command;

import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 个人药品批量导入命令。
 *
 * <p>当前版本优先服务 App 端“文本批量导入”场景，
 * 因此命令对象只保留：
 * 1. 原始多行文本
 * 2. 默认药品类型
 * 3. 默认状态
 *
 * <p>这样既能覆盖最常见的“从别处粘贴一批药名”场景，
 * 也能为后续升级成 Excel / 结构化导入预留空间。
 */
@Data
public class ImportDrugCommand {

    /**
     * 多行导入文本。
     *
     * <p>每行一条药品，支持使用 `|` 分隔附加字段。
     */
    @Size(max = 10000, message = "导入文本长度不能超过10000个字符")
    private String rawText;

    /**
     * 默认药品类型。
     *
     * <p>当某一行没有显式填写药品类型时，使用这里的默认值。
     */
    @Size(max = 20, message = "默认药品类型长度不能超过20个字符")
    private String defaultDrugType;

    /**
     * 默认状态。
     *
     * <p>如果前端未传，服务端会按启用状态处理。
     */
    private Integer defaultStatus;
}
