package com.healthtrail.domain.system.notice.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统通知公告表，用于发布系统级通知和公告信息
 *
 * @author valarchie
 * @since 2022-10-02
 */
@Getter
@Setter
@TableName("sys_notice")
@ApiModel(value = "SysNoticeEntity对象", description = "通知公告表")
public class SysNoticeEntity extends BaseEntity<SysNoticeEntity> {

    private static final long serialVersionUID = 1L;

    /** 通知公告主键ID */
    @ApiModelProperty("公告ID")
    @TableId(value = "notice_id", type = IdType.AUTO)
    private Integer noticeId;

    /** 通知公告的标题 */
    @ApiModelProperty("公告标题")
    @TableField("notice_title")
    private String noticeTitle;

    /** 通知公告类型：1通知 2公告 */
    @ApiModelProperty("公告类型（1通知 2公告）")
    @TableField("notice_type")
    private Integer noticeType;

    /** 通知公告的文本内容 */
    @ApiModelProperty("公告内容")
    @TableField("notice_content")
    private String noticeContent;

    /** 公告状态，1正常 0关闭 */
    @ApiModelProperty("公告状态（1正常 0关闭）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.noticeId;
    }

}
