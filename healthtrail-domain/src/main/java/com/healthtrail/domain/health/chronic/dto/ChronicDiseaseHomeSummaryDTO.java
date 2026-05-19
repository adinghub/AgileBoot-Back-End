package com.healthtrail.domain.health.chronic.dto;

import lombok.Data;

/**
 * 首页慢病专项摘要。
 *
 * <p>首页只需要让用户快速知道“有没有慢病专项需要关注”，
 * 因此这里返回轻量统计和一个推荐打开的专项 ID，完整趋势仍放在专项详情接口中。
 */
@Data
public class ChronicDiseaseHomeSummaryDTO {

    /** 活跃专项数量 */
    private int activeProfileCount;

    /** 高风险专项数量 */
    private int highRiskProfileCount;

    /** 待复查任务数量 */
    private int reviewTaskCount;

    /** 最新专项档案ID */
    private Long latestProfileId;

    /** 最新专项成员ID */
    private Long latestMemberId;

    /** 最新专项成员姓名 */
    private String latestMemberName;

    /** 最新专项病种名称 */
    private String latestDiseaseName;

    /** 摘要。 */
    private String summary;
}
