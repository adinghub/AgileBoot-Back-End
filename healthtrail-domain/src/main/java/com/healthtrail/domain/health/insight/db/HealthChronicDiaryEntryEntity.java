package com.healthtrail.domain.health.insight.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 慢病日记记录表，使用结构化JSON存储各类日常健康记录
 *
 * <p>慢病日记用于承接"日常记录"场景，例如血压、血糖、症状、饮食、运动、睡眠等。
 * 这里不把不同慢病拆成不同表，也不把血压/血糖写死成固定字段，
 * 而是使用 entry_type + metric_payload_json 保存结构化扩展内容。
 * 这样后续新增任意慢病记录项时，只需要在 App 或后端配置记录表单，不需要再改表结构。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_chronic_diary_entry")
@ApiModel(value = "HealthChronicDiaryEntryEntity对象", description = "健康系统慢病日记记录表")
public class HealthChronicDiaryEntryEntity extends BaseEntity<HealthChronicDiaryEntryEntity> {

    private static final long serialVersionUID = 1L;

    /** 日记记录主键ID */
    @ApiModelProperty("慢病日记ID")
    @TableId(value = "diary_entry_id", type = IdType.AUTO)
    private Long diaryEntryId;

    /** 日记归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 可选的关联慢病档案ID */
    @ApiModelProperty("可选慢病档案ID")
    @TableField("profile_id")
    private Long profileId;

    /** 可选的病种编码 */
    @ApiModelProperty("可选病种编码")
    @TableField("disease_code")
    private String diseaseCode;

    /** 记录类型，如血压BLOOD_PRESSURE、症状SYMPTOM、饮食DIET、运动EXERCISE等 */
    @ApiModelProperty("记录类型，例如 BLOOD_PRESSURE/SYMPTOM/DIET/EXERCISE/SLEEP/GENERAL")
    @TableField("entry_type")
    private String entryType;

    /** 记录发生时间 */
    @ApiModelProperty("记录时间")
    @TableField("record_time")
    private Date recordTime;

    /** 记录标题 */
    @ApiModelProperty("记录标题")
    @TableField("entry_title")
    private String entryTitle;

    /** 记录正文，自由文本描述 */
    @ApiModelProperty("记录正文")
    @TableField("entry_content")
    private String entryContent;

    /** 结构化指标数据JSON，存储血压值、血糖值等数值化数据 */
    @ApiModelProperty("结构化指标JSON")
    @TableField("metric_payload_json")
    private String metricPayloadJson;

    /** 记录来源类型，如手动录入MANUAL、语音VOICE、导入IMPORT */
    @ApiModelProperty("来源类型：MANUAL/VOICE/IMPORT")
    @TableField("source_type")
    private String sourceType;

    /** 状态，1启用 0停用 */
    @ApiModelProperty("状态（1启用 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.diaryEntryId;
    }
}
