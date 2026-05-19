package com.healthtrail.domain.health.message.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * App消息中心表，存储站内消息快照及Push派发记录
 *
 * <p>该表承担两层职责：
 * 1. 作为 App 端消息中心/站内消息的数据来源
 * 2. 记录最近一次 Push 派发结果，方便排查设备通知是否成功送出
 *
 * <p>注意这里存的是"消息快照"，
 * 而不是业务主表本身。这样即使原始业务后续发生变化，
 * 用户仍然能在消息中心看到当时收到的标题、正文和跳转载荷。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("app_message")
@ApiModel(value = "HealthAppMessageEntity对象", description = "健康系统App消息中心表")
public class HealthAppMessageEntity extends BaseEntity<HealthAppMessageEntity> {

    private static final long serialVersionUID = 1L;

    /** 消息主键ID */
    @ApiModelProperty("消息ID")
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /** 消息归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 家庭成员姓名快照 */
    @ApiModelProperty("家庭成员名称快照")
    @TableField("member_name_snapshot")
    private String memberNameSnapshot;

    /** 业务场景编码，用于区分消息来源类型 */
    @ApiModelProperty("业务场景编码")
    @TableField("business_scene")
    private String businessScene;

    /** 关联的业务数据主键ID */
    @ApiModelProperty("业务主键ID")
    @TableField("business_id")
    private Long businessId;

    /** 消息标题 */
    @ApiModelProperty("消息标题")
    @TableField("message_title")
    private String messageTitle;

    /** 消息正文内容 */
    @ApiModelProperty("消息正文")
    @TableField("message_content")
    private String messageContent;

    /** 业务透传载荷JSON，用于前端跳转目标页面 */
    @ApiModelProperty("业务透传载荷JSON")
    @TableField("payload_json")
    private String payloadJson;

    /** 消息去重键，防止同一条业务消息重复生成 */
    @ApiModelProperty("消息去重键")
    @TableField("dedup_key")
    private String dedupKey;

    /** 用户是否已读该消息 */
    @ApiModelProperty("已读状态")
    @TableField("read_status")
    private Integer readStatus;

    /** 用户标记已读的时间 */
    @ApiModelProperty("已读时间")
    @TableField("read_time")
    private Date readTime;

    /** 最近一次Push发送状态 */
    @ApiModelProperty("最近一次发送状态")
    @TableField("send_status")
    private Integer sendStatus;

    /** 最近一次Push发送时间 */
    @ApiModelProperty("最近一次发送时间")
    @TableField("send_time")
    private Date sendTime;

    /** 最近一次发送使用的推送通道 */
    @ApiModelProperty("最近一次发送通道")
    @TableField("send_channel")
    private String sendChannel;

    /** Push发送重试次数 */
    @ApiModelProperty("发送重试次数")
    @TableField("send_retry_count")
    private Integer sendRetryCount;

    /** 最近一次Push发送的结果说明 */
    @ApiModelProperty("最近一次发送结果说明")
    @TableField("send_result_message")
    private String sendResultMessage;

    @Override
    public Serializable pkVal() {
        return this.messageId;
    }
}
