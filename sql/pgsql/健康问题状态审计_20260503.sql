-- 健康问题状态变更审计增量脚本
--
-- 健康问题主表 health_problem 只保存当前状态，方便问题中心快速查询；
-- 状态变更过程单独落到 health_problem_status_log，便于 App 展示处理历史和后续问题追溯。
-- 本脚本全部使用 PostgreSQL IF NOT EXISTS，可重复执行。

CREATE TABLE IF NOT EXISTS health_problem_status_log (
    log_id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    action_type VARCHAR(30) DEFAULT 'STATUS_CHANGE' NOT NULL,
    before_status SMALLINT,
    after_status SMALLINT NOT NULL,
    action_remark VARCHAR(500),
    action_time TIMESTAMPTZ NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_problem_status_log_problem_time
    ON health_problem_status_log (problem_id, action_time DESC, log_id DESC);

CREATE INDEX IF NOT EXISTS idx_health_problem_status_log_member_time
    ON health_problem_status_log (member_id, action_time DESC, log_id DESC);

COMMENT ON TABLE health_problem_status_log IS '健康系统健康问题状态变更审计表';
COMMENT ON COLUMN health_problem_status_log.log_id IS '日志ID';
COMMENT ON COLUMN health_problem_status_log.problem_id IS '健康问题ID';
COMMENT ON COLUMN health_problem_status_log.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_problem_status_log.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_problem_status_log.operator_user_id IS '操作人用户ID';
COMMENT ON COLUMN health_problem_status_log.action_type IS '操作类型，当前主要为 STATUS_CHANGE';
COMMENT ON COLUMN health_problem_status_log.before_status IS '变更前状态，1跟进中 2已缓解 3已关闭';
COMMENT ON COLUMN health_problem_status_log.after_status IS '变更后状态，1跟进中 2已缓解 3已关闭';
COMMENT ON COLUMN health_problem_status_log.action_remark IS '操作备注';
COMMENT ON COLUMN health_problem_status_log.action_time IS '操作发生时间';
COMMENT ON COLUMN health_problem_status_log.deleted IS '逻辑删除标识';