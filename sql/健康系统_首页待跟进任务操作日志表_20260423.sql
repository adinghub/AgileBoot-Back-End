-- 健康系统首页待跟进任务操作日志表
--
-- 该表专门保留任务行为时间线，用于：
-- 1. App 端展示任务历史动作
-- 2. 后台做行为分析、依从率统计、批量操作审计
-- 3. 区分“当前状态”和“发生过哪些操作”这两个不同维度

-- 当前项目 PostgreSQL 基线已经统一去掉 health_* 历史前缀。
-- 日志表名必须与 HealthFollowUpTaskLogEntity 的 @TableName("follow_up_task_log") 保持一致，
-- 否则任务主表保存成功后，追加 READ / DELAY / COMPLETE 等操作日志时仍会落到错误表名。
create table follow_up_task_log
(
    log_id                  bigint auto_increment comment '日志ID'
        primary key,
    task_id                 bigint                    not null comment '任务记录ID',
    owner_user_id           bigint                    not null comment '归属App用户ID',
    member_id               bigint                    null comment '家庭成员ID',
    task_type               varchar(30)               not null comment '任务类型（REMINDER提醒任务 REPORT_ADVICE报告建议任务 OPERATION运营任务）',
    source_id               bigint                    not null comment '来源业务ID',
    action_type             varchar(30)               not null comment '操作类型（READ已读 DELAY延后 COMPLETE完成 IGNORE忽略 RESTORE恢复）',
    before_status           smallint                  null comment '操作前状态（0待跟进 1已延后 2已完成 3已忽略）',
    after_status            smallint                  null comment '操作后状态（0待跟进 1已延后 2已完成 3已忽略）',
    action_remark           varchar(255)              null comment '操作备注',
    delayed_until_snapshot  datetime                  null comment '延后到期时间快照',
    creator_id              bigint                    null comment '创建者ID',
    create_time             datetime                  null comment '创建时间',
    updater_id              bigint                    null comment '更新者ID',
    update_time             datetime                  null comment '更新时间',
    deleted                 tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统首页待跟进任务操作日志表';

create index idx_follow_up_task_log_task_id on follow_up_task_log (task_id);
create index idx_follow_up_task_log_owner_user_id on follow_up_task_log (owner_user_id);
create index idx_follow_up_task_log_source on follow_up_task_log (task_type, source_id);
create index idx_follow_up_task_log_create_time on follow_up_task_log (create_time);
