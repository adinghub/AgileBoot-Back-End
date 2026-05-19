package com.healthtrail.domain.health.insight.dto;

import com.healthtrail.domain.health.insight.db.HealthChronicDiaryEntryEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 慢病日记返回对象。
 */
@Data
@NoArgsConstructor
public class ChronicDiaryEntryDTO {

    /** 日记记录ID */
    private Long diaryEntryId;

    /** 所属用户ID */
    private Long ownerUserId;

    /** 家庭成员ID */
    private Long memberId;

    /** 家庭成员姓名 */
    private String memberName;

    /** 慢病专项档案ID */
    private Long profileId;

    /** 慢病病种编码 */
    private String diseaseCode;

    /** 记录类型 */
    private String entryType;

    /** 记录时间 */
    private Date recordTime;

    /** 记录标题 */
    private String entryTitle;

    /** 记录正文 */
    private String entryContent;

    /** 结构化指标JSON数据 */
    private String metricPayloadJson;

    /** 数据来源类型 */
    private String sourceType;

    public ChronicDiaryEntryDTO(HealthChronicDiaryEntryEntity entity, String memberName) {
        if (entity == null) {
            return;
        }
        this.diaryEntryId = entity.getDiaryEntryId();
        this.ownerUserId = entity.getOwnerUserId();
        this.memberId = entity.getMemberId();
        this.memberName = memberName;
        this.profileId = entity.getProfileId();
        this.diseaseCode = entity.getDiseaseCode();
        this.entryType = entity.getEntryType();
        this.recordTime = entity.getRecordTime();
        this.entryTitle = entity.getEntryTitle();
        this.entryContent = entity.getEntryContent();
        this.metricPayloadJson = entity.getMetricPayloadJson();
        this.sourceType = entity.getSourceType();
    }
}
