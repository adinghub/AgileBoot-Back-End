-- 健康系统会员门禁配置化 PostgreSQL 增量脚本
--
-- 说明：
-- 1. 本脚本在现有 member_feature / member_level / user_member 体系之上新增“业务门禁点”层
-- 2. 业务代码以后只需要稳定埋 gate_code，具体受什么会员规则限制由后台配置决定
-- 3. 本次先落门禁点表与规则表，后续如果要增加审计表，可以在这个基础上继续扩展

CREATE TABLE IF NOT EXISTS member_gate (
    member_gate_id BIGSERIAL PRIMARY KEY,
    gate_code VARCHAR(64) NOT NULL,
    gate_name VARCHAR(100) NOT NULL,
    gate_scope VARCHAR(30) NOT NULL,
    biz_module VARCHAR(50) NOT NULL,
    terminal_type VARCHAR(20) NOT NULL,
    default_policy_type VARCHAR(20) DEFAULT 'ALLOW' NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    is_builtin SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_member_gate_code UNIQUE (gate_code)
);

-- PostgreSQL 的 `INSERT ... ON CONFLICT (gate_code)` 需要能匹配到唯一约束或唯一索引。
-- 这里把唯一性直接放进表约束，避免只建普通索引时某些环境执行 upsert 报错。
CREATE UNIQUE INDEX IF NOT EXISTS uk_member_gate_code ON member_gate (gate_code);
CREATE INDEX IF NOT EXISTS idx_member_gate_scope ON member_gate (gate_scope);
CREATE INDEX IF NOT EXISTS idx_member_gate_biz_module ON member_gate (biz_module);
CREATE INDEX IF NOT EXISTS idx_member_gate_terminal_type ON member_gate (terminal_type);
CREATE INDEX IF NOT EXISTS idx_member_gate_status ON member_gate (status);

CREATE TABLE IF NOT EXISTS member_gate_rule (
    member_gate_rule_id BIGSERIAL PRIMARY KEY,
    member_gate_id BIGINT NOT NULL,
    policy_type VARCHAR(20) NOT NULL,
    feature_code VARCHAR(64),
    allowed_level_codes_json TEXT,
    deny_client_mode VARCHAR(20) DEFAULT 'DIALOG' NOT NULL,
    deny_title VARCHAR(100),
    deny_message VARCHAR(500),
    guide_member_page SMALLINT DEFAULT 1 NOT NULL,
    priority INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    version_no INT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_gate_rule_gate_id ON member_gate_rule (member_gate_id, deleted);
CREATE INDEX IF NOT EXISTS idx_member_gate_rule_policy_type ON member_gate_rule (policy_type);
CREATE INDEX IF NOT EXISTS idx_member_gate_rule_feature_code ON member_gate_rule (feature_code);
CREATE INDEX IF NOT EXISTS idx_member_gate_rule_status ON member_gate_rule (status);

COMMENT ON TABLE member_gate IS '会员功能门禁点表';
COMMENT ON COLUMN member_gate.member_gate_id IS '门禁点ID';
COMMENT ON COLUMN member_gate.gate_code IS '门禁点编码';
COMMENT ON COLUMN member_gate.gate_name IS '门禁点名称';
COMMENT ON COLUMN member_gate.gate_scope IS '门禁点作用域 APP_PAGE/APP_ACTION/API/DOMAIN_ACTION';
COMMENT ON COLUMN member_gate.biz_module IS '所属业务模块';
COMMENT ON COLUMN member_gate.terminal_type IS '终端类型 APP/ADMIN/SERVER/ALL';
COMMENT ON COLUMN member_gate.default_policy_type IS '默认策略类型 ALLOW/FEATURE';
COMMENT ON COLUMN member_gate.status IS '状态 0停用1启用';
COMMENT ON COLUMN member_gate.is_builtin IS '是否内置 0否1是';
COMMENT ON COLUMN member_gate.remark IS '备注';
COMMENT ON COLUMN member_gate.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_gate_rule IS '会员功能门禁规则表';
COMMENT ON COLUMN member_gate_rule.member_gate_rule_id IS '门禁规则ID';
COMMENT ON COLUMN member_gate_rule.member_gate_id IS '门禁点ID';
COMMENT ON COLUMN member_gate_rule.policy_type IS '策略类型 ALLOW/FEATURE';
COMMENT ON COLUMN member_gate_rule.feature_code IS '绑定权益编码';
COMMENT ON COLUMN member_gate_rule.allowed_level_codes_json IS '等级白名单JSON';
COMMENT ON COLUMN member_gate_rule.deny_client_mode IS '前端失败展示方式 DIALOG/DISABLE/HIDE';
COMMENT ON COLUMN member_gate_rule.deny_title IS '自定义失败标题';
COMMENT ON COLUMN member_gate_rule.deny_message IS '自定义失败文案';
COMMENT ON COLUMN member_gate_rule.guide_member_page IS '是否引导去会员中心 0否1是';
COMMENT ON COLUMN member_gate_rule.priority IS '优先级';
COMMENT ON COLUMN member_gate_rule.status IS '状态 0停用1启用';
COMMENT ON COLUMN member_gate_rule.version_no IS '规则版本号';
COMMENT ON COLUMN member_gate_rule.remark IS '备注';
COMMENT ON COLUMN member_gate_rule.deleted IS '删除标志（0存在 1删除）';

INSERT INTO member_gate (
    gate_code, gate_name, gate_scope, biz_module, terminal_type, default_policy_type,
    status, is_builtin, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('REPORT.EXPORT', '报告导出', 'APP_ACTION', 'REPORT', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制报告导出入口', NULL, NOW(), NULL, NOW(), 0),
    ('AI.ASSISTANT.OPEN', 'AI助手打开', 'APP_ACTION', 'AI', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制AI助手入口', NULL, NOW(), NULL, NOW(), 0),
    ('AI.REPORT.EXPORT', 'AI报告导出', 'APP_ACTION', 'AI', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制AI报告导出入口', NULL, NOW(), NULL, NOW(), 0),
    ('TRANSACTION_PLAN.CREATE', '新增计划', 'APP_ACTION', 'TRANSACTION_PLAN', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制计划创建入口', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (gate_code) DO NOTHING;
