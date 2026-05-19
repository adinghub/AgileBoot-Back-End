create table health_app_message
(
    message_id            bigint auto_increment comment '消息ID'
        primary key,
    owner_user_id         bigint                    not null comment '归属App用户ID',
    member_id             bigint                    null comment '家庭成员ID',
    member_name_snapshot  varchar(50)               null comment '家庭成员名称快照',
    business_scene        varchar(40)               not null comment '业务场景编码（MEDICATION_REMINDER用药提醒 REPORT_FOLLOW_UP报告跟进）',
    business_id           bigint                    not null comment '业务主键ID',
    message_title         varchar(100)              not null comment '消息标题',
    message_content       varchar(500)              null comment '消息正文',
    payload_json          text                      null comment '统一业务透传载荷JSON',
    dedup_key             varchar(100)              null comment '消息去重键',
    read_status           smallint      default 0   not null comment '已读状态（0未读 1已读）',
    read_time             datetime                  null comment '已读时间',
    send_status           smallint      default 0   not null comment '最近一次发送状态（0待发送 1发送成功 2发送失败）',
    send_time             datetime                  null comment '最近一次发送时间',
    send_channel          varchar(30)               null comment '最近一次发送通道',
    send_retry_count      int           default 0   not null comment '发送重试次数',
    send_result_message   varchar(255)              null comment '最近一次发送结果说明',
    creator_id            bigint                    null comment '创建者ID',
    create_time           datetime                  null comment '创建时间',
    updater_id            bigint                    null comment '更新者ID',
    update_time           datetime                  null comment '更新时间',
    deleted               tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统App消息中心表';

create index idx_health_app_message_owner_user_id on health_app_message (owner_user_id);
create index idx_health_app_message_owner_read_status on health_app_message (owner_user_id, read_status);
create index idx_health_app_message_business_scene on health_app_message (business_scene);
create index idx_health_app_message_business_id on health_app_message (business_id);
create unique index uk_health_app_message_owner_dedup on health_app_message (owner_user_id, dedup_key);
