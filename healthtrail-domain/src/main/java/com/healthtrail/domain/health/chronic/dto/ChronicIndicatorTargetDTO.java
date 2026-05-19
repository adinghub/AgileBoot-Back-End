package com.healthtrail.domain.health.chronic.dto;

import com.healthtrail.domain.health.chronic.db.HealthChronicIndicatorTargetEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 慢病专项指标目标范围返回对象。
 *
 * <p>该对象既用于目标维护动作返回，也会嵌入慢病指标趋势中，
 * 让 App 展示“最新值是否进入个人目标范围”时不需要再理解病种配置和报告解析细节。
 */
@Data
@NoArgsConstructor
public class ChronicIndicatorTargetDTO {

    /** 目标配置ID */
    private Long targetId;

    /** 所属用户ID */
    private Long ownerUserId;

    /** 家庭成员ID */
    private Long memberId;

    /** 专项档案ID */
    private Long profileId;

    /** 指标编码 */
    private String indicatorCode;

    /** 指标名称快照 */
    private String indicatorName;

    /** 目标下限 */
    private BigDecimal targetMin;

    /** 目标上限 */
    private BigDecimal targetMax;

    /** 非数值型目标说明 */
    private String targetText;

    /** 结果单位 */
    private String resultUnit;

    /** 状态 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public ChronicIndicatorTargetDTO(HealthChronicIndicatorTargetEntity entity) {
        if (entity == null) {
            return;
        }
        this.targetId = entity.getTargetId();
        this.ownerUserId = entity.getOwnerUserId();
        this.memberId = entity.getMemberId();
        this.profileId = entity.getProfileId();
        this.indicatorCode = entity.getIndicatorCode();
        this.indicatorName = entity.getIndicatorName();
        this.targetMin = entity.getTargetMin();
        this.targetMax = entity.getTargetMax();
        this.targetText = entity.getTargetText();
        this.resultUnit = entity.getResultUnit();
        this.status = entity.getStatus();
        this.remark = entity.getRemark();
        this.createTime = entity.getCreateTime();
        this.updateTime = entity.getUpdateTime();
    }
}
