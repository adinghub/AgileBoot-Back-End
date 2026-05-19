-- 首页待跟进任务表命名兼容修复脚本
--
-- 适用场景：
-- 1. 数据库曾经执行过历史拆分脚本，已经创建 health_follow_up_task / health_follow_up_task_log
-- 2. 当前 Java 实体和最终初始化脚本使用 follow_up_task / follow_up_task_log
-- 3. App 调用 markTaskRead、delayTask、ignoreTask 等接口时，MyBatis-Plus 写入最终表名并报“数据库异常”
--
-- 执行策略：
-- 1. 如果只有历史表名，则直接 rename 到最终表名，保留已有数据
-- 2. 如果最终表已经存在，则不做 rename，避免覆盖现有正式数据
-- 3. 如果历史表和最终表都不存在，则按最终 PostgreSQL 表结构补建，方便修复不完整初始化环境

DO $$
BEGIN
    IF to_regclass('public.follow_up_task') IS NULL
       AND to_regclass('public.health_follow_up_task') IS NOT NULL THEN
        ALTER TABLE health_follow_up_task RENAME TO follow_up_task;
    END IF;

    IF to_regclass('public.follow_up_task_log') IS NULL
       AND to_regclass('public.health_follow_up_task_log') IS NOT NULL THEN
        ALTER TABLE health_follow_up_task_log RENAME TO follow_up_task_log;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS follow_up_task (
    task_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    task_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    task_status SMALLINT DEFAULT 0 NOT NULL,
    read_status SMALLINT DEFAULT 0 NOT NULL,
    read_time TIMESTAMPTZ,
    delayed_until TIMESTAMPTZ,
    complete_time TIMESTAMPTZ,
    action_remark VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_follow_up_task_owner_source
    ON follow_up_task (owner_user_id, task_type, source_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_owner_user_id ON follow_up_task (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_member_id ON follow_up_task (member_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_status ON follow_up_task (task_status);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_owner_read_status ON follow_up_task (owner_user_id, read_status);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_delayed_until ON follow_up_task (delayed_until);

COMMENT ON TABLE follow_up_task IS '健康系统首页待跟进任务表';
COMMENT ON COLUMN follow_up_task.task_id IS '任务记录ID';
COMMENT ON COLUMN follow_up_task.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN follow_up_task.member_id IS '家庭成员ID';
COMMENT ON COLUMN follow_up_task.task_type IS '任务类型（REMINDER提醒任务 REPORT_ADVICE报告建议任务 OPERATION运营任务）';
COMMENT ON COLUMN follow_up_task.source_id IS '来源业务ID';
COMMENT ON COLUMN follow_up_task.task_status IS '任务状态（0待跟进 1已延后 2已完成 3已忽略）';
COMMENT ON COLUMN follow_up_task.read_status IS '已读状态（0未读 1已读）';
COMMENT ON COLUMN follow_up_task.read_time IS '已读时间';
COMMENT ON COLUMN follow_up_task.delayed_until IS '延后到期时间';
COMMENT ON COLUMN follow_up_task.complete_time IS '完成时间';
COMMENT ON COLUMN follow_up_task.action_remark IS '操作备注';
COMMENT ON COLUMN follow_up_task.creator_id IS '创建者ID';
COMMENT ON COLUMN follow_up_task.create_time IS '创建时间';
COMMENT ON COLUMN follow_up_task.updater_id IS '更新者ID';
COMMENT ON COLUMN follow_up_task.update_time IS '更新时间';
COMMENT ON COLUMN follow_up_task.deleted IS '逻辑删除';

CREATE TABLE IF NOT EXISTS follow_up_task_log (
    log_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    task_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    before_status SMALLINT,
    after_status SMALLINT,
    action_remark VARCHAR(255),
    delayed_until_snapshot TIMESTAMPTZ,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_task_id ON follow_up_task_log (task_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_owner_user_id ON follow_up_task_log (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_source ON follow_up_task_log (task_type, source_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_create_time ON follow_up_task_log (create_time);

COMMENT ON TABLE follow_up_task_log IS '健康系统首页待跟进任务操作日志表';
COMMENT ON COLUMN follow_up_task_log.log_id IS '日志ID';
COMMENT ON COLUMN follow_up_task_log.task_id IS '任务记录ID';
COMMENT ON COLUMN follow_up_task_log.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN follow_up_task_log.member_id IS '家庭成员ID';
COMMENT ON COLUMN follow_up_task_log.task_type IS '任务类型（REMINDER提醒任务 REPORT_ADVICE报告建议任务 OPERATION运营任务）';
COMMENT ON COLUMN follow_up_task_log.source_id IS '来源业务ID';
COMMENT ON COLUMN follow_up_task_log.action_type IS '操作类型（READ已读 DELAY延后 COMPLETE完成 IGNORE忽略 RESTORE恢复）';
COMMENT ON COLUMN follow_up_task_log.before_status IS '操作前状态（0待跟进 1已延后 2已完成 3已忽略）';
COMMENT ON COLUMN follow_up_task_log.after_status IS '操作后状态（0待跟进 1已延后 2已完成 3已忽略）';
COMMENT ON COLUMN follow_up_task_log.action_remark IS '操作备注';
COMMENT ON COLUMN follow_up_task_log.delayed_until_snapshot IS '延后到期时间快照';
COMMENT ON COLUMN follow_up_task_log.creator_id IS '创建者ID';
COMMENT ON COLUMN follow_up_task_log.create_time IS '创建时间';
COMMENT ON COLUMN follow_up_task_log.updater_id IS '更新者ID';
COMMENT ON COLUMN follow_up_task_log.update_time IS '更新时间';
COMMENT ON COLUMN follow_up_task_log.deleted IS '逻辑删除';
