ALTER TABLE report_item ADD COLUMN IF NOT EXISTS standard_item_code VARCHAR(64);
CREATE INDEX IF NOT EXISTS idx_report_item_standard_item_code ON report_item (standard_item_code);

-- 增量脚本除了补字段外，也同步补齐 PostgreSQL 元数据注释，
-- 这样老环境执行完升级后，数据库客户端里能直接看到字段语义。
COMMENT ON COLUMN report_item.standard_item_code IS '标准指标编码';

CREATE TABLE IF NOT EXISTS health_problem (
    problem_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    problem_name VARCHAR(100) NOT NULL,
    problem_type VARCHAR(50) NOT NULL,
    problem_status SMALLINT DEFAULT 1 NOT NULL,
    risk_level SMALLINT DEFAULT 1 NOT NULL,
    standard_item_code VARCHAR(64),
    first_found_date DATE,
    last_follow_date DATE,
    summary VARCHAR(1000),
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_problem_member_id ON health_problem (member_id);
CREATE INDEX IF NOT EXISTS idx_health_problem_standard_code ON health_problem (standard_item_code);

COMMENT ON TABLE health_problem IS '健康系统健康问题表';
COMMENT ON COLUMN health_problem.problem_id IS '健康问题ID';
COMMENT ON COLUMN health_problem.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_problem.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_problem.problem_name IS '问题名称';
COMMENT ON COLUMN health_problem.problem_type IS '问题类型';
COMMENT ON COLUMN health_problem.problem_status IS '问题状态（1跟进中 2已缓解 3已关闭）';
COMMENT ON COLUMN health_problem.risk_level IS '风险等级（1低 2中 3高）';
COMMENT ON COLUMN health_problem.standard_item_code IS '关联的标准指标编码';
COMMENT ON COLUMN health_problem.first_found_date IS '首次发现日期';
COMMENT ON COLUMN health_problem.last_follow_date IS '最近跟进日期';
COMMENT ON COLUMN health_problem.summary IS '问题摘要';
COMMENT ON COLUMN health_problem.remark IS '备注';
COMMENT ON COLUMN health_problem.creator_id IS '创建者ID';
COMMENT ON COLUMN health_problem.create_time IS '创建时间';
COMMENT ON COLUMN health_problem.updater_id IS '更新者ID';
COMMENT ON COLUMN health_problem.update_time IS '更新时间';
COMMENT ON COLUMN health_problem.deleted IS '逻辑删除';

CREATE TABLE IF NOT EXISTS health_problem_evidence (
    evidence_id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    report_id BIGINT,
    report_item_id BIGINT,
    evidence_title VARCHAR(100),
    evidence_summary VARCHAR(1000),
    evidence_date DATE,
    confidence_level SMALLINT DEFAULT 0 NOT NULL,
    confirm_status SMALLINT DEFAULT 0 NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_problem_id ON health_problem_evidence (problem_id);
CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_report_id ON health_problem_evidence (report_id);
CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_report_item_id ON health_problem_evidence (report_item_id);

COMMENT ON TABLE health_problem_evidence IS '健康系统健康问题证据表';
COMMENT ON COLUMN health_problem_evidence.evidence_id IS '证据ID';
COMMENT ON COLUMN health_problem_evidence.problem_id IS '健康问题ID';
COMMENT ON COLUMN health_problem_evidence.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_problem_evidence.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_problem_evidence.evidence_type IS '证据类型';
COMMENT ON COLUMN health_problem_evidence.report_id IS '关联报告ID';
COMMENT ON COLUMN health_problem_evidence.report_item_id IS '关联报告指标ID';
COMMENT ON COLUMN health_problem_evidence.evidence_title IS '证据标题';
COMMENT ON COLUMN health_problem_evidence.evidence_summary IS '证据摘要';
COMMENT ON COLUMN health_problem_evidence.evidence_date IS '证据日期';
COMMENT ON COLUMN health_problem_evidence.confidence_level IS '置信等级（0未知 1低 2中 3高）';
COMMENT ON COLUMN health_problem_evidence.confirm_status IS '确认状态（0待确认 1已确认 2已忽略）';
COMMENT ON COLUMN health_problem_evidence.creator_id IS '创建者ID';
COMMENT ON COLUMN health_problem_evidence.create_time IS '创建时间';
COMMENT ON COLUMN health_problem_evidence.updater_id IS '更新者ID';
COMMENT ON COLUMN health_problem_evidence.update_time IS '更新时间';
COMMENT ON COLUMN health_problem_evidence.deleted IS '逻辑删除';
