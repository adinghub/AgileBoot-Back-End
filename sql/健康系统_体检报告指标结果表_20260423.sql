create table health_report_item
(
    item_id          bigint auto_increment comment '指标结果ID'
        primary key,
    report_id        bigint                     not null comment '体检报告ID',
    owner_user_id    bigint                     not null comment '归属App用户ID',
    item_code        varchar(64)                null comment '指标编码',
    item_name        varchar(100)               not null comment '指标名称',
    result_value     varchar(100)               not null comment '结果值',
    result_unit      varchar(50)                null comment '结果单位',
    reference_min    decimal(10, 4)             null comment '参考范围下限',
    reference_max    decimal(10, 4)             null comment '参考范围上限',
    reference_text   varchar(100)               null comment '参考范围原文',
    item_interpretation varchar(300)            null comment '指标解读，说明该指标主要反映什么',
    abnormal_flag    smallint       default 0   not null comment '异常标记（0待判断 1正常 2偏低 3偏高 4异常）',
    sort             int            default 0   not null comment '排序号',
    remark           varchar(255)               null comment '备注',
    creator_id       bigint                     null comment '创建者ID',
    create_time      datetime                   null comment '创建时间',
    updater_id       bigint                     null comment '更新者ID',
    update_time      datetime                   null comment '更新时间',
    deleted          tinyint(1)     default 0   not null comment '逻辑删除'
)
    comment '健康系统体检报告指标结果表';

create index idx_health_report_item_report_id on health_report_item (report_id);
create index idx_health_report_item_owner_user_id on health_report_item (owner_user_id);
create index idx_health_report_item_abnormal_flag on health_report_item (abnormal_flag);
