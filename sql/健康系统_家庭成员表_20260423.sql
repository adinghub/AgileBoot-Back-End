create table health_family_member
(
    member_id        bigint auto_increment comment '家庭成员ID'
        primary key,
    member_code      varchar(32)               null comment '家庭成员业务编码，优先给前端展示使用',
    owner_user_id    bigint                    not null comment '归属App用户ID',
    member_name      varchar(64)               not null comment '成员姓名',
    gender           smallint      default 0   not null comment '性别（0未知 1男 2女）',
    birthday         date                      null comment '生日',
    relation_type    varchar(32)               not null comment '关系类型',
    height           decimal(5, 2)             null comment '身高(cm)',
    weight           decimal(5, 2)             null comment '体重(kg)',
    blood_type       varchar(16)               null comment '血型',
    allergy_history  varchar(500)              null comment '过敏史',
    chronic_history  varchar(500)              null comment '慢病史',
    remark           varchar(500)              null comment '备注',
    status           smallint      default 1   not null comment '状态（1正常 0停用）',
    creator_id       bigint                    null comment '创建者ID',
    create_time      datetime                  null comment '创建时间',
    updater_id       bigint                    null comment '更新者ID',
    update_time      datetime                  null comment '更新时间',
    deleted          tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统家庭成员表';

create unique index uk_health_family_member_code on health_family_member (member_code);
create index idx_health_family_member_owner_user_id on health_family_member (owner_user_id);
create index idx_health_family_member_status on health_family_member (status);
