create table health_app_device
(
    device_id         bigint auto_increment comment '设备ID'
        primary key,
    owner_user_id     bigint                    not null comment '归属App用户ID',
    device_code       varchar(64)               not null comment '设备唯一编码',
    push_platform     varchar(20)               not null comment '推送平台',
    device_token      varchar(255)              not null comment '设备Token',
    device_model      varchar(100)              null comment '设备型号',
    manufacturer      varchar(100)              null comment '设备厂商',
    os_version        varchar(50)               null comment '系统版本',
    app_version       varchar(50)               null comment 'App版本',
    last_active_time  datetime                  null comment '最后活跃时间',
    status            smallint      default 1   not null comment '状态（1正常 0停用）',
    creator_id        bigint                    null comment '创建者ID',
    create_time       datetime                  null comment '创建时间',
    updater_id        bigint                    null comment '更新者ID',
    update_time       datetime                  null comment '更新时间',
    deleted           tinyint(1)    default 0   not null comment '逻辑删除',
    constraint uk_health_app_device_code
        unique (device_code)
)
    comment '健康系统App设备表';

create index idx_health_app_device_owner_user_id on health_app_device (owner_user_id);
create index idx_health_app_device_status on health_app_device (status);
create index idx_health_app_device_token on health_app_device (device_token);
