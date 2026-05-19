-- 健康系统用药计划与提醒表
--
-- 说明：
-- 1. `health_medication_plan` 保存用户配置的计划主数据
-- 2. `health_medication_reminder` 保存按计划展开后的具体提醒实例
-- 3. 当前脚本已对齐“每周几提醒”“每几小时提醒”“每几天提醒”和阶段剂量规则字段

create table health_medication_plan
(
    plan_id              bigint auto_increment comment '用药计划ID'
        primary key,
    plan_code            varchar(32)               null comment '用药计划业务编码，优先给前端展示使用',
    owner_user_id        bigint                    not null comment '归属App用户ID',
    member_id            bigint                    not null comment '家庭成员ID',
    drug_id              bigint                    null comment '药品ID',
    custom_drug_name     varchar(100)              null comment '自定义药名',
    start_date           date                      not null comment '开始日期',
    end_date             date                      null comment '结束日期，为空表示长期计划',
    reminder_times_json  varchar(500)              not null comment '提醒时间点JSON',
    meal_timing          varchar(30)   default 'NO_LIMIT' not null comment '服药时机',
    dose_amount          decimal(10, 2)            null comment '每次剂量',
    dose_unit            varchar(20)               null comment '剂量单位',
    frequency_type       varchar(30)   default 'DAILY' not null comment '频率类型（DAILY每日 EVERY_OTHER_DAY隔日 WEEKLY每周 INTERVAL_HOURS每几小时 INTERVAL_DAYS每几天）',
    weekly_days_json     varchar(100)              null comment '每周提醒星期JSON，仅 frequency_type = WEEKLY 时使用',
    interval_hours       int                       null comment '每几小时提醒一次，仅 frequency_type = INTERVAL_HOURS 时使用',
    interval_days        int                       null comment '每几天提醒一次，仅 frequency_type = INTERVAL_DAYS 时使用',
    dose_rule            varchar(1000)             null comment '阶段剂量规则，例如前4天1片、再4天1.5片',
    remark               varchar(500)              null comment '备注',
    status               smallint      default 1   not null comment '状态（1正常 0停用）',
    creator_id           bigint                    null comment '创建者ID',
    create_time          datetime                  null comment '创建时间',
    updater_id           bigint                    null comment '更新者ID',
    update_time          datetime                  null comment '更新时间',
    deleted              tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统用药计划表';

create unique index uk_health_medication_plan_code on health_medication_plan (plan_code);
create index idx_health_medication_plan_owner_user_id on health_medication_plan (owner_user_id);
create index idx_health_medication_plan_member_id on health_medication_plan (member_id);
create index idx_health_medication_plan_frequency_type on health_medication_plan (frequency_type);
create index idx_health_medication_plan_status on health_medication_plan (status);

create table health_medication_reminder
(
    reminder_id          bigint auto_increment comment '提醒记录ID'
        primary key,
    owner_user_id        bigint                    not null comment '归属App用户ID',
    plan_id              bigint                    not null comment '用药计划ID',
    member_id            bigint                    not null comment '家庭成员ID',
    reminder_date        date                      not null comment '提醒日期',
    scheduled_time       datetime                  not null comment '计划提醒时间',
    drug_name_snapshot   varchar(100)              not null comment '药品名称快照',
    dose_amount          decimal(10, 2)            null comment '每次剂量',
    dose_unit            varchar(20)               null comment '剂量单位',
    meal_timing          varchar(30)               null comment '服药时机',
    reminder_status      smallint      default 0   not null comment '提醒状态（0待处理 1已服药 2已跳过 3已过期）',
    notify_status        smallint      default 0   not null comment '发送状态（0待发送 1发送成功 2发送失败）',
    notify_time          datetime                  null comment '最近一次发送时间',
    notify_fail_reason   varchar(255)              null comment '发送失败原因',
    notify_retry_count   int           default 0   not null comment '发送重试次数',
    feedback_time        datetime                  null comment '反馈时间',
    skip_reason          varchar(255)              null comment '跳过原因',
    creator_id           bigint                    null comment '创建者ID',
    create_time          datetime                  null comment '创建时间',
    updater_id           bigint                    null comment '更新者ID',
    update_time          datetime                  null comment '更新时间',
    deleted              tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统用药提醒记录表';

create index idx_health_medication_reminder_owner_user_id on health_medication_reminder (owner_user_id);
create index idx_health_medication_reminder_plan_id on health_medication_reminder (plan_id);
create index idx_health_medication_reminder_plan_time on health_medication_reminder (plan_id, scheduled_time);
create index idx_health_medication_reminder_member_id on health_medication_reminder (member_id);
create index idx_health_medication_reminder_scheduled_time on health_medication_reminder (scheduled_time);
create index idx_health_medication_reminder_status on health_medication_reminder (reminder_status);
create index idx_health_medication_reminder_notify_status on health_medication_reminder (notify_status);
