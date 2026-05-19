-- 健康系统个人药柜库存与预警增量脚本
--
-- 说明：
-- 1. 适用于已经执行过 20260423 版健康系统脚本的环境
-- 2. 本脚本补齐个人药柜库存、系统药品引入、低库存提醒所需字段和库存流水表

alter table health_drug add column source_drug_id bigint null comment '来源系统药品ID，仅个人引入系统药品时使用';
alter table health_drug add column stock_unit varchar(20) null comment '库存单位，例如片、粒、ml';
alter table health_drug add column stock_alert_threshold decimal(10, 2) null comment '库存预警阈值，仅个人药品启用库存跟踪';
alter table health_drug add column low_stock_notified tinyint(1) default 0 not null comment '当前预警周期内是否已发送过低库存提醒';
alter table health_drug add column low_stock_notify_time datetime null comment '最近一次低库存提醒发送时间';

create unique index uk_health_drug_owner_source on health_drug (owner_user_id, source_drug_id);
create index idx_health_drug_stock_alert on health_drug (owner_user_id, low_stock_notified, stock_alert_threshold);

create table health_drug_stock_batch
(
    batch_id                bigint auto_increment comment '批次库存ID'
        primary key,
    owner_user_id           bigint                   not null comment '归属App用户ID',
    drug_id                 bigint                   not null comment '药品ID',
    batch_no                varchar(50)              null comment '药品批号，可为空',
    expire_date             date                     not null comment '药品效期',
    stock_quantity          decimal(10, 2)          not null comment '当前批次库存数量',
    creator_id              bigint                   null comment '创建者ID',
    create_time             datetime                 null comment '创建时间',
    updater_id              bigint                   null comment '更新者ID',
    update_time             datetime                 null comment '更新时间',
    deleted                 tinyint(1) default 0    not null comment '逻辑删除'
)
    comment '健康系统药品批号效期库存表';

create index idx_health_drug_stock_batch_owner_user_id on health_drug_stock_batch (owner_user_id);
create index idx_health_drug_stock_batch_drug_id on health_drug_stock_batch (drug_id);
create index idx_health_drug_stock_batch_expire_date on health_drug_stock_batch (expire_date);
create index idx_health_drug_stock_batch_batch_no on health_drug_stock_batch (batch_no);

create table health_drug_stock_log
(
    log_id                   bigint auto_increment comment '库存流水ID'
        primary key,
    owner_user_id            bigint                   not null comment '归属App用户ID',
    drug_id                  bigint                   not null comment '药品ID',
    change_type              varchar(30)              not null comment '库存变更类型',
    before_quantity          decimal(10, 2)           null comment '变更前库存',
    change_quantity          decimal(10, 2)           null comment '本次变更数量，增加为正，扣减为负',
    after_quantity           decimal(10, 2)           null comment '变更后库存',
    stock_unit_snapshot      varchar(20)              null comment '库存单位快照',
    alert_threshold_snapshot decimal(10, 2)          null comment '预警阈值快照',
    related_plan_id          bigint                   null comment '关联用药计划ID',
    related_reminder_id      bigint                   null comment '关联提醒ID',
    operation_remark         varchar(255)             null comment '操作备注',
    creator_id               bigint                   null comment '创建者ID',
    create_time              datetime                 null comment '创建时间',
    updater_id               bigint                   null comment '更新者ID',
    update_time              datetime                 null comment '更新时间',
    deleted                  tinyint(1) default 0    not null comment '逻辑删除'
)
    comment '健康系统药品库存流水表';

create index idx_health_drug_stock_log_owner_user_id on health_drug_stock_log (owner_user_id);
create index idx_health_drug_stock_log_drug_id on health_drug_stock_log (drug_id);
create index idx_health_drug_stock_log_change_type on health_drug_stock_log (change_type);
create index idx_health_drug_stock_log_reminder_id on health_drug_stock_log (related_reminder_id);
