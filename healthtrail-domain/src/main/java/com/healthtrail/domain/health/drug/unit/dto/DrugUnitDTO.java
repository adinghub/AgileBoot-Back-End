package com.healthtrail.domain.health.drug.unit.dto;

import com.healthtrail.domain.health.drug.unit.db.DrugUnitEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品单位返回对象。
 */
@Data
@NoArgsConstructor
public class DrugUnitDTO {

    /** 单位ID。 */
    private Long unitId;

    /** 单位编码。 */
    private String unitCode;

    /** 单位名称。 */
    private String unitName;

    /** 单位别名 */
    private String unitAlias;

    /** 小数位精度 */
    private Integer precisionScale;

    /** 排序号 */
    private Integer sort;

    /** 状态 */
    private Integer status;

    /** 单位图标附件ID */
    private Long iconAttachmentId;

    /** 单位图标地址 */
    private String iconUrl;

    /** 备注 */
    private String remark;

    public DrugUnitDTO(DrugUnitEntity entity) {
        if (entity != null) {
            this.unitId = entity.getUnitId();
            this.unitCode = entity.getUnitCode();
            this.unitName = entity.getUnitName();
            this.unitAlias = entity.getUnitAlias();
            this.precisionScale = entity.getPrecisionScale();
            this.sort = entity.getSort();
            this.status = entity.getStatus();
            this.iconAttachmentId = entity.getIconAttachmentId();
            this.remark = entity.getRemark();
        }
    }
}
