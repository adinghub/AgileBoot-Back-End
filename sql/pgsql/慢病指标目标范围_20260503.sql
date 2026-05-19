-- 慢病指标目标范围个性化
--
-- 设计目标：同一慢病病种在不同成员、不同医嘱下可能有不同控制目标，
-- 因此目标范围挂在用户慢病专项档案下，而不是写进病种默认配置。
-- 该表只保存用户确认过的个人目标，不改动报告原始指标，详情看板运行时完成最新值比对。

CREATE TABLE IF NOT EXISTS health_chronic_indicator_target (
    target_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    indicator_code VARCHAR(100) NOT NULL,
    indicator_name VARCHAR(100),
    target_min NUMERIC(18, 4),
    target_max NUMERIC(18, 4),
    target_text VARCHAR(200),
    result_unit VARCHAR(50),
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT ck_health_chronic_indicator_target_range
        CHECK (target_min IS NULL OR target_max IS NULL OR target_min <= target_max)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_health_chronic_indicator_target_profile_code
    ON health_chronic_indicator_target (profile_id, indicator_code)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_health_chronic_indicator_target_owner
    ON health_chronic_indicator_target (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_health_chronic_indicator_target_member
    ON health_chronic_indicator_target (member_id);
CREATE INDEX IF NOT EXISTS idx_health_chronic_indicator_target_profile_status
    ON health_chronic_indicator_target (profile_id, status);

COMMENT ON TABLE health_chronic_indicator_target IS '健康系统慢病专项指标目标范围表';
COMMENT ON COLUMN health_chronic_indicator_target.target_id IS '指标目标ID';
COMMENT ON COLUMN health_chronic_indicator_target.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_chronic_indicator_target.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_chronic_indicator_target.profile_id IS '慢病专项档案ID';
COMMENT ON COLUMN health_chronic_indicator_target.indicator_code IS '指标编码，优先使用标准指标编码';
COMMENT ON COLUMN health_chronic_indicator_target.indicator_name IS '指标名称快照';
COMMENT ON COLUMN health_chronic_indicator_target.target_min IS '目标下限，空值表示不限制下限';
COMMENT ON COLUMN health_chronic_indicator_target.target_max IS '目标上限，空值表示不限制上限';
COMMENT ON COLUMN health_chronic_indicator_target.target_text IS '非数值型目标说明';
COMMENT ON COLUMN health_chronic_indicator_target.result_unit IS '指标单位快照';
COMMENT ON COLUMN health_chronic_indicator_target.status IS '状态（1启用 0停用）';
COMMENT ON COLUMN health_chronic_indicator_target.remark IS '备注';
COMMENT ON COLUMN health_chronic_indicator_target.creator_id IS '创建者ID';
COMMENT ON COLUMN health_chronic_indicator_target.create_time IS '创建时间';
COMMENT ON COLUMN health_chronic_indicator_target.updater_id IS '更新者ID';
COMMENT ON COLUMN health_chronic_indicator_target.update_time IS '更新时间';
COMMENT ON COLUMN health_chronic_indicator_target.deleted IS '逻辑删除';
