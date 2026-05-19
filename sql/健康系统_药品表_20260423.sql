create table health_drug
(
    drug_id            bigint auto_increment comment '药品ID'
        primary key,
    drug_code          varchar(32)               null comment '药品业务编码，优先给前端展示使用',
    owner_user_id      bigint                    not null comment '归属App用户ID，0表示系统下发药品',
    drug_name          varchar(100)              not null comment '药品名称',
    generic_name       varchar(100)              null comment '通用名',
    brand_name         varchar(100)              null comment '商品名',
    dosage_form        varchar(50)               null comment '剂型',
    specification      varchar(100)              null comment '规格',
    indication         varchar(1000)             null comment '适应症',
    usage_instruction  varchar(1000)             null comment '用法用量',
    adverse_reaction   varchar(1000)             null comment '不良反应',
    contraindication   varchar(1000)             null comment '禁忌',
    manufacturer       varchar(200)              null comment '生产厂家',
    drug_type          varchar(20)               null comment '药品类型',
    source_drug_id     bigint                    null comment '来源系统药品ID，仅个人引入系统药品时使用',
    stock_unit         varchar(20)               null comment '库存单位，例如片、粒、ml',
    stock_alert_threshold decimal(10, 2)         null comment '库存预警阈值，仅个人药品启用库存跟踪',
    low_stock_notified tinyint(1)    default 0   not null comment '当前预警周期内是否已发送过低库存提醒',
    low_stock_notify_time datetime               null comment '最近一次低库存提醒发送时间',
    status             smallint      default 1   not null comment '状态（1正常 0停用）',
    remark             varchar(500)              null comment '备注',
    creator_id         bigint                    null comment '创建者ID',
    create_time        datetime                  null comment '创建时间',
    updater_id         bigint                    null comment '更新者ID',
    update_time        datetime                  null comment '更新时间',
    deleted            tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统药品表（系统药品 + 个人药柜条目）';

create unique index uk_health_drug_code on health_drug (drug_code);
create unique index uk_health_drug_owner_name on health_drug (owner_user_id, drug_name);
create unique index uk_health_drug_owner_source on health_drug (owner_user_id, source_drug_id);
create index idx_health_drug_owner_user_id on health_drug (owner_user_id);
create index idx_health_drug_type on health_drug (drug_type);
create index idx_health_drug_status on health_drug (status);
create index idx_health_drug_stock_alert on health_drug (owner_user_id, low_stock_notified, stock_alert_threshold);

create table health_drug_stock_batch
(
    batch_id           bigint auto_increment comment '批次库存ID'
        primary key,
    owner_user_id      bigint                    not null comment '归属App用户ID',
    drug_id            bigint                    not null comment '药品ID',
    batch_no           varchar(50)               null comment '药品批号，可为空',
    expire_date        date                      not null comment '药品效期',
    stock_quantity     decimal(10, 2)            not null comment '当前批次库存数量',
    creator_id         bigint                    null comment '创建者ID',
    create_time        datetime                  null comment '创建时间',
    updater_id         bigint                    null comment '更新者ID',
    update_time        datetime                  null comment '更新时间',
    deleted            tinyint(1)    default 0  not null comment '逻辑删除'
)
    comment '健康系统药品批号效期库存表';

create index idx_health_drug_stock_batch_owner_user_id on health_drug_stock_batch (owner_user_id);
create index idx_health_drug_stock_batch_drug_id on health_drug_stock_batch (drug_id);
create index idx_health_drug_stock_batch_expire_date on health_drug_stock_batch (expire_date);
create index idx_health_drug_stock_batch_batch_no on health_drug_stock_batch (batch_no);

INSERT INTO health_drug (drug_id, drug_code, owner_user_id, drug_name, generic_name, brand_name, dosage_form, specification,
                         indication, usage_instruction, adverse_reaction, contraindication, manufacturer, drug_type,
                         status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES (1, 'DRG1', 0, '阿司匹林肠溶片', '阿司匹林', '拜阿司匹灵', '肠溶片', '100mg*30片',
        '抗血小板聚集，预防心脑血管事件', '成人通常一次1片，一日1次，遵医嘱服用', '胃部不适、出血风险增加',
        '活动性消化道出血、阿司匹林过敏者禁用', '拜耳医药保健有限公司', 'RX', 1,
        '系统下发常用药示例数据', null, now(), null, now(), 0);

INSERT INTO health_drug (drug_id, drug_code, owner_user_id, drug_name, generic_name, brand_name, dosage_form, specification,
                         indication, usage_instruction, adverse_reaction, contraindication, manufacturer, drug_type,
                         status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES (2, 'DRG2', 0, '二甲双胍片', '盐酸二甲双胍', '格华止', '片剂', '0.5g*20片',
        '用于2型糖尿病血糖控制', '随餐或餐后服用，起始剂量遵医嘱', '恶心、腹泻、腹胀',
        '严重肾功能不全、代谢性酸中毒患者禁用', '中美上海施贵宝制药有限公司', 'RX', 1,
        '系统下发常用药示例数据', null, now(), null, now(), 0);

create table health_drug_stock_log
(
    log_id                  bigint auto_increment comment '库存流水ID'
        primary key,
    owner_user_id           bigint                    not null comment '归属App用户ID',
    drug_id                 bigint                    not null comment '药品ID',
    change_type             varchar(30)               not null comment '库存变更类型',
    before_quantity         decimal(10, 2)            null comment '变更前库存',
    change_quantity         decimal(10, 2)            null comment '本次变更数量，增加为正，扣减为负',
    after_quantity          decimal(10, 2)            null comment '变更后库存',
    stock_unit_snapshot     varchar(20)               null comment '库存单位快照',
    alert_threshold_snapshot decimal(10, 2)          null comment '预警阈值快照',
    related_plan_id         bigint                    null comment '关联用药计划ID',
    related_reminder_id     bigint                    null comment '关联提醒ID',
    operation_remark        varchar(255)              null comment '操作备注',
    creator_id              bigint                    null comment '创建者ID',
    create_time             datetime                  null comment '创建时间',
    updater_id              bigint                    null comment '更新者ID',
    update_time             datetime                  null comment '更新时间',
    deleted                 tinyint(1)    default 0  not null comment '逻辑删除'
)
    comment '健康系统药品库存流水表';

create index idx_health_drug_stock_log_owner_user_id on health_drug_stock_log (owner_user_id);
create index idx_health_drug_stock_log_drug_id on health_drug_stock_log (drug_id);
create index idx_health_drug_stock_log_change_type on health_drug_stock_log (change_type);
create index idx_health_drug_stock_log_reminder_id on health_drug_stock_log (related_reminder_id);
