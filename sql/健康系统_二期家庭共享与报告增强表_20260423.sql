create table health_family_member_share
(
    share_id               bigint auto_increment comment '共享关系ID'
        primary key,
    member_id              bigint                     not null comment '家庭成员ID',
    owner_user_id          bigint                     not null comment '主账号App用户ID',
    collaborator_user_id   bigint                     not null comment '协同账号App用户ID',
    invite_id              bigint                     null comment '来源邀请ID',
    share_role             varchar(20)               not null comment '共享角色（OWNER/EDITOR/VIEWER）',
    share_status           smallint       default 1   not null comment '共享状态（0停用 1生效）',
    accepted_time          datetime                   null comment '接受共享时间',
    remark                 varchar(255)              null comment '备注',
    creator_id             bigint                     null comment '创建者ID',
    create_time            datetime                   null comment '创建时间',
    updater_id             bigint                     null comment '更新者ID',
    update_time            datetime                   null comment '更新时间',
    deleted                tinyint(1)      default 0 not null comment '逻辑删除'
)
    comment '健康系统家庭成员共享关系表';

create unique index uk_health_family_member_share_member_collaborator
    on health_family_member_share (member_id, collaborator_user_id, deleted);
create index idx_health_family_member_share_owner_user_id on health_family_member_share (owner_user_id);
create index idx_health_family_member_share_collaborator_user_id on health_family_member_share (collaborator_user_id);
create index idx_health_family_member_share_status on health_family_member_share (share_status);

create table health_family_share_invite
(
    invite_id              bigint auto_increment comment '共享邀请ID'
        primary key,
    member_id              bigint                     not null comment '家庭成员ID',
    owner_user_id          bigint                     not null comment '主账号App用户ID',
    invite_code            varchar(32)               not null comment '邀请码',
    invitee_user_id        bigint                     null comment '接受邀请的App用户ID',
    share_role             varchar(20)               not null comment '邀请角色（EDITOR/VIEWER）',
    invite_status          smallint       default 0   not null comment '邀请状态（0待接受 1已接受 2已取消 3已过期）',
    expire_time            datetime                   not null comment '邀请过期时间',
    accepted_time          datetime                   null comment '接受时间',
    remark                 varchar(255)              null comment '备注',
    creator_id             bigint                     null comment '创建者ID',
    create_time            datetime                   null comment '创建时间',
    updater_id             bigint                     null comment '更新者ID',
    update_time            datetime                   null comment '更新时间',
    deleted                tinyint(1)      default 0 not null comment '逻辑删除'
)
    comment '健康系统家庭成员共享邀请表';

create unique index uk_health_family_share_invite_invite_code
    on health_family_share_invite (invite_code, deleted);
create index idx_health_family_share_invite_member_id on health_family_share_invite (member_id);
create index idx_health_family_share_invite_owner_user_id on health_family_share_invite (owner_user_id);
create index idx_health_family_share_invite_status on health_family_share_invite (invite_status);
create index idx_health_family_share_invite_expire_time on health_family_share_invite (expire_time);

create table health_indicator_template
(
    template_id            bigint auto_increment comment '指标模板ID'
        primary key,
    report_type            varchar(50)               null comment '报告类型',
    item_code              varchar(64)               null comment '指标编码',
    item_name              varchar(100)              not null comment '指标名称',
    result_unit            varchar(50)               null comment '结果单位',
    reference_min          decimal(10, 4)            null comment '参考范围下限',
    reference_max          decimal(10, 4)            null comment '参考范围上限',
    reference_text         varchar(100)              null comment '参考范围文本',
    suggestion_template    varchar(1000)             null comment '建议模板',
    sort                   int            default 0   not null comment '排序号',
    status                 smallint       default 1   not null comment '状态（1启用 0停用）',
    creator_id             bigint                     null comment '创建者ID',
    create_time            datetime                   null comment '创建时间',
    updater_id             bigint                     null comment '更新者ID',
    update_time            datetime                   null comment '更新时间',
    deleted                tinyint(1)      default 0 not null comment '逻辑删除'
)
    comment '健康系统体检指标模板表';

create index idx_health_indicator_template_report_type on health_indicator_template (report_type);
create index idx_health_indicator_template_item_code on health_indicator_template (item_code);
create index idx_health_indicator_template_item_name on health_indicator_template (item_name);
create index idx_health_indicator_template_status on health_indicator_template (status);

alter table health_report
    add column ocr_status smallint default 0 not null comment 'OCR占位处理状态（0待处理 1处理中 2已完成 3失败）' after parse_status,
    add column ocr_text_snapshot varchar(2000) null comment 'OCR占位文本快照' after ocr_status,
    add column ocr_time datetime null comment 'OCR占位处理时间' after ocr_text_snapshot,
    add column ai_summary_status smallint default 0 not null comment 'AI总结占位状态（0待处理 1处理中 2已完成 3失败）' after ocr_time,
    add column ai_summary_content varchar(2000) null comment 'AI总结占位内容' after ai_summary_status,
    add column ai_summary_time datetime null comment 'AI总结占位处理时间' after ai_summary_content;
