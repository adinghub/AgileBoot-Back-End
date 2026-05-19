-- 健康洞察与慢病日记
--
-- 本增量为“复查闭环、指标预警、就医资料包、慢病日记、照护看板、
-- 用药安全、AI周报、语音录入、健康时间线”提供最小新增持久化能力。
-- 其中大多数功能复用现有表；只有“慢病日记/语音记录”需要新增通用记录表。

CREATE TABLE IF NOT EXISTS health_chronic_diary_entry (
    diary_entry_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    profile_id BIGINT,
    disease_code VARCHAR(64),
    entry_type VARCHAR(40) DEFAULT 'GENERAL' NOT NULL,
    record_time TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    entry_title VARCHAR(100),
    entry_content VARCHAR(2000),
    metric_payload_json VARCHAR(4000),
    source_type VARCHAR(30) DEFAULT 'MANUAL' NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_chronic_diary_owner_user_id
    ON health_chronic_diary_entry (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_health_chronic_diary_member_time
    ON health_chronic_diary_entry (member_id, record_time DESC);
CREATE INDEX IF NOT EXISTS idx_health_chronic_diary_profile_time
    ON health_chronic_diary_entry (profile_id, record_time DESC);
CREATE INDEX IF NOT EXISTS idx_health_chronic_diary_disease_code
    ON health_chronic_diary_entry (disease_code);
CREATE INDEX IF NOT EXISTS idx_health_chronic_diary_entry_type
    ON health_chronic_diary_entry (entry_type);
CREATE INDEX IF NOT EXISTS idx_health_chronic_diary_source_type
    ON health_chronic_diary_entry (source_type);

COMMENT ON TABLE health_chronic_diary_entry IS '健康系统慢病日记记录表';
COMMENT ON COLUMN health_chronic_diary_entry.diary_entry_id IS '慢病日记ID';
COMMENT ON COLUMN health_chronic_diary_entry.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_chronic_diary_entry.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_chronic_diary_entry.profile_id IS '可选慢病专项档案ID';
COMMENT ON COLUMN health_chronic_diary_entry.disease_code IS '可选病种编码';
COMMENT ON COLUMN health_chronic_diary_entry.entry_type IS '记录类型，例如 BLOOD_PRESSURE/SYMPTOM/DIET/EXERCISE/SLEEP/GENERAL';
COMMENT ON COLUMN health_chronic_diary_entry.record_time IS '记录时间';
COMMENT ON COLUMN health_chronic_diary_entry.entry_title IS '记录标题';
COMMENT ON COLUMN health_chronic_diary_entry.entry_content IS '记录正文';
COMMENT ON COLUMN health_chronic_diary_entry.metric_payload_json IS '结构化指标JSON';
COMMENT ON COLUMN health_chronic_diary_entry.source_type IS '来源类型：MANUAL/VOICE/IMPORT';
COMMENT ON COLUMN health_chronic_diary_entry.status IS '状态：1启用 0停用';
COMMENT ON COLUMN health_chronic_diary_entry.remark IS '备注';
COMMENT ON COLUMN health_chronic_diary_entry.creator_id IS '创建者ID';
COMMENT ON COLUMN health_chronic_diary_entry.create_time IS '创建时间';
COMMENT ON COLUMN health_chronic_diary_entry.updater_id IS '更新者ID';
COMMENT ON COLUMN health_chronic_diary_entry.update_time IS '更新时间';
COMMENT ON COLUMN health_chronic_diary_entry.deleted IS '逻辑删除';