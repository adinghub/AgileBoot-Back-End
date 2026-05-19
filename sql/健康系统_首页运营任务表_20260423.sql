-- 健康系统首页运营任务表
--
-- 这张表承接“后台主动投放到 App 首页任务流”的运营任务数据。
-- 它和提醒、报告等自动派生任务分开存储，目的是：
-- 1. 让运营人员能够按用户 / 成员维度定向下发任务
-- 2. 继续复用首页统一的跳转协议、排序规则和任务流展示能力
-- 3. 避免后台运营配置直接耦合底层提醒、报告主数据

create table health_operation_task
(
    operation_task_id  bigint auto_increment comment '运营任务ID'
        primary key,
    owner_user_id      bigint                    not null comment '目标App用户ID',
    member_id          bigint                    null comment '可选的目标家庭成员ID',
    task_title         varchar(100)              not null comment '任务标题',
    task_content       varchar(500)              null comment '任务内容',
    action_text        varchar(50)   default '去查看' not null comment '动作按钮文案',
    risk_level         varchar(20)   default 'MEDIUM' not null comment '风险等级（LOW低 MEDIUM中 HIGH高）',
    priority_weight    int           default 0   not null comment '排序权重，值越大越靠前',
    target_page_code   varchar(50)               null comment '目标页面编码',
    target_page_name   varchar(50)               null comment '目标页面名称快照',
    target_biz_id      bigint                    null comment '目标业务主键ID',
    target_biz_type    varchar(30)               null comment '目标业务类型',
    target_tab_code    varchar(30)               null comment '目标标签编码',
    target_anchor_code varchar(50)               null comment '目标锚点编码',
    target_anchor_name varchar(50)               null comment '目标锚点名称快照',
    start_time         datetime                  null comment '生效开始时间',
    end_time           datetime                  null comment '生效结束时间',
    status             smallint      default 1   not null comment '状态（1启用 0停用）',
    remark             varchar(500)              null comment '备注',
    creator_id         bigint                    null comment '创建者ID',
    create_time        datetime                  null comment '创建时间',
    updater_id         bigint                    null comment '更新者ID',
    update_time        datetime                  null comment '更新时间',
    deleted            tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统首页运营任务表';

create index idx_health_operation_task_owner_user_id on health_operation_task (owner_user_id);
create index idx_health_operation_task_member_id on health_operation_task (member_id);
create index idx_health_operation_task_status on health_operation_task (status);
create index idx_health_operation_task_risk_level on health_operation_task (risk_level);
create index idx_health_operation_task_start_time on health_operation_task (start_time);
create index idx_health_operation_task_end_time on health_operation_task (end_time);
create index idx_health_operation_task_owner_status_time
    on health_operation_task (owner_user_id, status, start_time, end_time);
