package com.healthtrail.domain.health.drug.unit.command;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 新增药品单位命令。
 *
 * <p>药品单位由后台统一维护，前端只负责搜索和选择，
 * 因此这里把单位名称、别名、精度、图标等主数据字段集中在一起定义。
 */
@Data
public class AddDrugUnitCommand {

    /**
     * 单位编码。
     *
     * <p>当前允许为空，后端会自动回退为单位名称，
     * 这样可以兼顾管理便利性和数据稳定识别能力。
     */
    @Size(max = 50, message = "单位编码长度不能超过50个字符")
    private String unitCode;

    /**
     * 单位名称。
     */
    @NotBlank(message = "单位名称不能为空")
    @Size(max = 20, message = "单位名称长度不能超过20个字符")
    private String unitName;

    /**
     * 单位别名，多个值可用逗号分隔。
     */
    @Size(max = 200, message = "单位别名长度不能超过200个字符")
    private String unitAlias;

    /**
     * 数量小数位精度。
     *
     * <p>例如：
     * 1. 片、粒通常为 0
     * 2. ml 可配成 1 或 2
     */
    @Min(value = 0, message = "单位精度不能小于0")
    @Max(value = 4, message = "单位精度不能大于4")
    private Integer precisionScale;

    /**
     * 排序值。
     */
    private Integer sort;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 单位图标附件ID。
     */
    private Long iconAttachmentId;

    /**
     * 备注。
     */
    @Size(max = 500, message = "单位备注长度不能超过500个字符")
    private String remark;
}
