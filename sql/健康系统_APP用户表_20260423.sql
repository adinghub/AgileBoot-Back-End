create table health_app_user
(
    user_id         bigint auto_increment comment 'App用户ID'
        primary key,
    mobile          varchar(16)               not null comment '手机号',
    nickname        varchar(64)               not null comment '昵称',
    avatar          varchar(255)              null comment '头像地址',
    password        varchar(128)              not null comment '密码',
    status          smallint      default 1   not null comment '账号状态（1正常 0停用）',
    last_login_ip   varchar(128)              null comment '最后登录IP',
    last_login_time datetime                  null comment '最后登录时间',
    register_source varchar(32)  default 'APP' not null comment '注册来源',
    creator_id      bigint                    null comment '创建者ID',
    create_time     datetime                  null comment '创建时间',
    updater_id      bigint                    null comment '更新者ID',
    update_time     datetime                  null comment '更新时间',
    deleted         tinyint(1)    default 0   not null comment '逻辑删除',
    constraint uk_health_app_user_mobile
        unique (mobile)
)
    comment '健康系统App用户表';

create index idx_health_app_user_status on health_app_user (status);
