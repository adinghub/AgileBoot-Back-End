package com.healthtrail.domain.health.chronic.dto;

import java.util.Date;
import lombok.Data;

/**
 * 慢病专项复查任务返回对象。
 */
@Data
public class ChronicDiseaseReviewTaskDTO {

    /** 待跟进任务ID */
    private Long operationTaskId;

    /** 专项档案ID */
    private Long profileId;

    /** 复查提醒日期 */
    private Date reviewDate;

    /** 任务标题 */
    private String taskTitle;

    /** 任务内容 */
    private String taskContent;

    /** 操作按钮文案 */
    private String actionText;

    /** 目标页面编码 */
    private String targetPageCode;

    /** 目标业务类型 */
    private String targetBizType;
}
