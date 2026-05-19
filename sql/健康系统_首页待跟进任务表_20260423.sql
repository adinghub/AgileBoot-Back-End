-- 健康系统首页待跟进任务表
--
-- 这张表保存的是“首页任务流的当前覆盖状态”，不是底层业务主表。
-- 例如某条提醒、报告建议或运营任务进入首页后：
-- 1. 可以在这里记录它当前是否已读
-- 2. 可以记录它当前是否被延后、完成或忽略
-- 3. 这样既不污染原始业务表，又能让首页任务中心按统一口径排序和展示

-- 当前项目 PostgreSQL 基线已经统一去掉 health_* 历史前缀。
-- 这里保留独立脚本，是为了方便开发人员单独核对首页待跟进任务结构；
-- 表名必须与 Java 实体 HealthFollowUpTaskEntity 的 @TableName("follow_up_task") 保持一致，
-- 否则 markTaskRead / delayTask 等保存链路会写入不存在的表并抛出“数据库异常”。
create table follow_up_task
(
    task_id         bigint auto_increment comment '任务记录ID'
        primary key,
    owner_user_id   bigint                    not null comment '归属App用户ID',
    member_id       bigint                    null comment '家庭成员ID',
    task_type       varchar(30)               not null comment '任务类型（REMINDER提醒任务 REPORT_ADVICE报告建议任务 OPERATION运营任务）',
    source_id       bigint                    not null comment '来源业务ID',
    task_status     smallint      default 0   not null comment '任务状态（0待跟进 1已延后 2已完成 3已忽略）',
    read_status     smallint      default 0   not null comment '已读状态（0未读 1已读）',
    read_time       datetime                  null comment '已读时间',
    delayed_until   datetime                  null comment '延后到期时间',
    complete_time   datetime                  null comment '完成时间',
    action_remark   varchar(255)              null comment '操作备注',
    creator_id      bigint                    null comment '创建者ID',
    create_time     datetime                  null comment '创建时间',
    updater_id      bigint                    null comment '更新者ID',
    update_time     datetime                  null comment '更新时间',
    deleted         tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统首页待跟进任务表';

create index idx_follow_up_task_owner_user_id on follow_up_task (owner_user_id);
create unique index uk_follow_up_task_owner_source on follow_up_task (owner_user_id, task_type, source_id);
create index idx_follow_up_task_member_id on follow_up_task (member_id);
create index idx_follow_up_task_status on follow_up_task (task_status);
create index idx_follow_up_task_owner_read_status on follow_up_task (owner_user_id, read_status);
create index idx_follow_up_task_delayed_until on follow_up_task (delayed_until);
