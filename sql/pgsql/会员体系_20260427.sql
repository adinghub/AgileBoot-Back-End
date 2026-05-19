-- 健康系统会员体系 PostgreSQL 增量脚本
--
-- 说明：
-- 1. 本脚本只面向 PostgreSQL 环境
-- 2. 目标是先落“会员权益 / 等级 / 等级权益矩阵 / 用户会员 / 兑换码 / 次数消耗 / 订单 / 订阅”基础结构
-- 3. 首期不强依赖真实支付，但先把订阅与订单骨架表建好，方便后续升级

CREATE TABLE IF NOT EXISTS member_feature (
    member_feature_id BIGSERIAL PRIMARY KEY,
    feature_code VARCHAR(64) NOT NULL,
    feature_name VARCHAR(100) NOT NULL,
    feature_type VARCHAR(20) NOT NULL,
    quota_period_type VARCHAR(20),
    free_enabled SMALLINT DEFAULT 0 NOT NULL,
    free_limit_value INT,
    feature_sort INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    is_builtin SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_feature_code ON member_feature (feature_code);
CREATE INDEX IF NOT EXISTS idx_member_feature_status ON member_feature (status);
CREATE INDEX IF NOT EXISTS idx_member_feature_sort ON member_feature (feature_sort);

CREATE TABLE IF NOT EXISTS member_level (
    member_level_id BIGSERIAL PRIMARY KEY,
    level_code VARCHAR(32) NOT NULL,
    level_name VARCHAR(100) NOT NULL,
    level_sort INT DEFAULT 0 NOT NULL,
    price NUMERIC(10, 2) DEFAULT 0 NOT NULL,
    duration_days INT,
    benefit_desc VARCHAR(1000),
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_level_code ON member_level (level_code);
CREATE INDEX IF NOT EXISTS idx_member_level_status ON member_level (status);
CREATE INDEX IF NOT EXISTS idx_member_level_sort ON member_level (level_sort);

CREATE TABLE IF NOT EXISTS member_level_feature (
    member_level_feature_id BIGSERIAL PRIMARY KEY,
    member_level_id BIGINT NOT NULL,
    member_feature_id BIGINT NOT NULL,
    enabled SMALLINT DEFAULT 0 NOT NULL,
    limit_value INT,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_level_feature_level_feature
    ON member_level_feature (member_level_id, member_feature_id, deleted);
CREATE INDEX IF NOT EXISTS idx_member_level_feature_level_id ON member_level_feature (member_level_id);
CREATE INDEX IF NOT EXISTS idx_member_level_feature_feature_id ON member_level_feature (member_feature_id);

CREATE TABLE IF NOT EXISTS user_member (
    user_member_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    member_level_id BIGINT NOT NULL,
    effective_start_time TIMESTAMPTZ NOT NULL,
    effective_end_time TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_user_id ON user_member (user_id, deleted);
CREATE INDEX IF NOT EXISTS idx_user_member_level_id ON user_member (member_level_id);
CREATE INDEX IF NOT EXISTS idx_user_member_status ON user_member (status);
CREATE INDEX IF NOT EXISTS idx_user_member_effective_end_time ON user_member (effective_end_time);

CREATE TABLE IF NOT EXISTS member_redeem_code (
    member_redeem_code_id BIGSERIAL PRIMARY KEY,
    batch_no VARCHAR(64) NOT NULL,
    redeem_code VARCHAR(64) NOT NULL,
    member_level_id BIGINT NOT NULL,
    level_code_snapshot VARCHAR(32) NOT NULL,
    level_name_snapshot VARCHAR(100) NOT NULL,
    price_snapshot NUMERIC(10, 2) DEFAULT 0 NOT NULL,
    duration_days_snapshot INT,
    code_status VARCHAR(20) NOT NULL,
    redeemed_user_id BIGINT,
    redeemed_time TIMESTAMPTZ,
    expire_time TIMESTAMPTZ,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_redeem_code_code ON member_redeem_code (redeem_code);
CREATE INDEX IF NOT EXISTS idx_member_redeem_code_batch_no ON member_redeem_code (batch_no);
CREATE INDEX IF NOT EXISTS idx_member_redeem_code_status ON member_redeem_code (code_status);
CREATE INDEX IF NOT EXISTS idx_member_redeem_code_level_id ON member_redeem_code (member_level_id);

CREATE TABLE IF NOT EXISTS member_feature_quota_usage (
    member_feature_quota_usage_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    member_feature_id BIGINT NOT NULL,
    feature_code_snapshot VARCHAR(64) NOT NULL,
    quota_period_type VARCHAR(20) NOT NULL,
    period_key VARCHAR(16) NOT NULL,
    used_count INT DEFAULT 0 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_feature_quota_usage_user_feature_period
    ON member_feature_quota_usage (user_id, member_feature_id, period_key, deleted);
CREATE INDEX IF NOT EXISTS idx_member_feature_quota_usage_feature_id ON member_feature_quota_usage (member_feature_id);
CREATE INDEX IF NOT EXISTS idx_member_feature_quota_usage_period_key ON member_feature_quota_usage (period_key);

CREATE TABLE IF NOT EXISTS user_member_order (
    user_member_order_id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    subscription_id BIGINT,
    user_id BIGINT NOT NULL,
    member_level_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    order_amount NUMERIC(10, 2) DEFAULT 0 NOT NULL,
    pay_time TIMESTAMPTZ,
    effective_start_time TIMESTAMPTZ,
    effective_end_time TIMESTAMPTZ,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_order_order_no ON user_member_order (order_no);
CREATE INDEX IF NOT EXISTS idx_user_member_order_user_id ON user_member_order (user_id);
CREATE INDEX IF NOT EXISTS idx_user_member_order_level_id ON user_member_order (member_level_id);
CREATE INDEX IF NOT EXISTS idx_user_member_order_status ON user_member_order (order_status);
CREATE INDEX IF NOT EXISTS idx_user_member_order_subscription_id ON user_member_order (subscription_id);

CREATE TABLE IF NOT EXISTS user_member_subscription (
    subscription_id BIGSERIAL PRIMARY KEY,
    subscription_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    member_level_id BIGINT NOT NULL,
    subscription_status VARCHAR(20) NOT NULL,
    auto_renew SMALLINT DEFAULT 0 NOT NULL,
    current_period_start_time TIMESTAMPTZ,
    current_period_end_time TIMESTAMPTZ,
    next_renew_time TIMESTAMPTZ,
    last_renew_time TIMESTAMPTZ,
    failed_renew_count INT DEFAULT 0 NOT NULL,
    cancel_time TIMESTAMPTZ,
    cancel_reason VARCHAR(500),
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_subscription_no ON user_member_subscription (subscription_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_subscription_user_id ON user_member_subscription (user_id, deleted);
CREATE INDEX IF NOT EXISTS idx_user_member_subscription_level_id ON user_member_subscription (member_level_id);
CREATE INDEX IF NOT EXISTS idx_user_member_subscription_status ON user_member_subscription (subscription_status);

COMMENT ON TABLE member_feature IS '会员权益定义表';
COMMENT ON COLUMN member_feature.member_feature_id IS '会员权益ID';
COMMENT ON COLUMN member_feature.feature_code IS '权益编码';
COMMENT ON COLUMN member_feature.feature_name IS '权益名称';
COMMENT ON COLUMN member_feature.feature_type IS '权益类型 SWITCH/LIMIT/QUOTA';
COMMENT ON COLUMN member_feature.quota_period_type IS '次数周期 DAY/MONTH';
COMMENT ON COLUMN member_feature.free_enabled IS '免费默认是否启用 0否1是';
COMMENT ON COLUMN member_feature.free_limit_value IS '免费默认限制值，null 表示不限';
COMMENT ON COLUMN member_feature.feature_sort IS '排序值';
COMMENT ON COLUMN member_feature.status IS '状态 0停用1启用';
COMMENT ON COLUMN member_feature.is_builtin IS '是否内置 0否1是';
COMMENT ON COLUMN member_feature.remark IS '备注';
COMMENT ON COLUMN member_feature.creator_id IS '创建者ID';
COMMENT ON COLUMN member_feature.create_time IS '创建时间';
COMMENT ON COLUMN member_feature.updater_id IS '更新者ID';
COMMENT ON COLUMN member_feature.update_time IS '更新时间';
COMMENT ON COLUMN member_feature.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_level IS '会员等级表';
COMMENT ON COLUMN member_level.member_level_id IS '会员等级ID';
COMMENT ON COLUMN member_level.level_code IS '会员等级编码';
COMMENT ON COLUMN member_level.level_name IS '会员等级名称';
COMMENT ON COLUMN member_level.level_sort IS '排序值';
COMMENT ON COLUMN member_level.price IS '价格';
COMMENT ON COLUMN member_level.duration_days IS '默认时长，单位天；为空表示不自动推导到期时间';
COMMENT ON COLUMN member_level.benefit_desc IS '权益说明';
COMMENT ON COLUMN member_level.status IS '状态 0停用1启用';
COMMENT ON COLUMN member_level.remark IS '备注';
COMMENT ON COLUMN member_level.creator_id IS '创建者ID';
COMMENT ON COLUMN member_level.create_time IS '创建时间';
COMMENT ON COLUMN member_level.updater_id IS '更新者ID';
COMMENT ON COLUMN member_level.update_time IS '更新时间';
COMMENT ON COLUMN member_level.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_level_feature IS '会员等级权益矩阵表';
COMMENT ON COLUMN member_level_feature.member_level_feature_id IS '会员等级权益ID';
COMMENT ON COLUMN member_level_feature.member_level_id IS '会员等级ID';
COMMENT ON COLUMN member_level_feature.member_feature_id IS '会员权益ID';
COMMENT ON COLUMN member_level_feature.enabled IS '是否启用 0否1是';
COMMENT ON COLUMN member_level_feature.limit_value IS '等级覆盖额度，null 表示不限';
COMMENT ON COLUMN member_level_feature.remark IS '备注';
COMMENT ON COLUMN member_level_feature.creator_id IS '创建者ID';
COMMENT ON COLUMN member_level_feature.create_time IS '创建时间';
COMMENT ON COLUMN member_level_feature.updater_id IS '更新者ID';
COMMENT ON COLUMN member_level_feature.update_time IS '更新时间';
COMMENT ON COLUMN member_level_feature.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE user_member IS '用户会员关系表';
COMMENT ON COLUMN user_member.user_member_id IS '用户会员ID';
COMMENT ON COLUMN user_member.user_id IS '用户ID';
COMMENT ON COLUMN user_member.member_level_id IS '会员等级ID';
COMMENT ON COLUMN user_member.effective_start_time IS '会员开始时间';
COMMENT ON COLUMN user_member.effective_end_time IS '会员结束时间，为空表示长期有效';
COMMENT ON COLUMN user_member.status IS '会员状态 ACTIVE/EXPIRED/DISABLED';
COMMENT ON COLUMN user_member.source_type IS '来源类型 SUBSCRIPTION/REDEEM_CODE/ADMIN_GRANT';
COMMENT ON COLUMN user_member.source_id IS '来源记录ID';
COMMENT ON COLUMN user_member.remark IS '备注';
COMMENT ON COLUMN user_member.creator_id IS '创建者ID';
COMMENT ON COLUMN user_member.create_time IS '创建时间';
COMMENT ON COLUMN user_member.updater_id IS '更新者ID';
COMMENT ON COLUMN user_member.update_time IS '更新时间';
COMMENT ON COLUMN user_member.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_redeem_code IS '会员兑换码表';
COMMENT ON COLUMN member_redeem_code.member_redeem_code_id IS '会员兑换码ID';
COMMENT ON COLUMN member_redeem_code.batch_no IS '生成批次号';
COMMENT ON COLUMN member_redeem_code.redeem_code IS '兑换码';
COMMENT ON COLUMN member_redeem_code.member_level_id IS '会员等级ID';
COMMENT ON COLUMN member_redeem_code.level_code_snapshot IS '等级编码快照';
COMMENT ON COLUMN member_redeem_code.level_name_snapshot IS '等级名称快照';
COMMENT ON COLUMN member_redeem_code.price_snapshot IS '价格快照';
COMMENT ON COLUMN member_redeem_code.duration_days_snapshot IS '时长快照，单位天；为空表示长期有效';
COMMENT ON COLUMN member_redeem_code.code_status IS '兑换码状态 AVAILABLE/REDEEMED/EXPIRED/DISABLED';
COMMENT ON COLUMN member_redeem_code.redeemed_user_id IS '兑换用户ID';
COMMENT ON COLUMN member_redeem_code.redeemed_time IS '兑换时间';
COMMENT ON COLUMN member_redeem_code.expire_time IS '过期时间';
COMMENT ON COLUMN member_redeem_code.remark IS '备注';
COMMENT ON COLUMN member_redeem_code.creator_id IS '创建者ID';
COMMENT ON COLUMN member_redeem_code.create_time IS '创建时间';
COMMENT ON COLUMN member_redeem_code.updater_id IS '更新者ID';
COMMENT ON COLUMN member_redeem_code.update_time IS '更新时间';
COMMENT ON COLUMN member_redeem_code.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_feature_quota_usage IS '会员权益次数使用记录表';
COMMENT ON COLUMN member_feature_quota_usage.member_feature_quota_usage_id IS '使用记录ID';
COMMENT ON COLUMN member_feature_quota_usage.user_id IS '用户ID';
COMMENT ON COLUMN member_feature_quota_usage.member_feature_id IS '会员权益ID';
COMMENT ON COLUMN member_feature_quota_usage.feature_code_snapshot IS '权益编码快照';
COMMENT ON COLUMN member_feature_quota_usage.quota_period_type IS '周期类型 DAY/MONTH';
COMMENT ON COLUMN member_feature_quota_usage.period_key IS '周期键，例如 202604 或 20260427';
COMMENT ON COLUMN member_feature_quota_usage.used_count IS '当前周期已使用次数';
COMMENT ON COLUMN member_feature_quota_usage.remark IS '备注';
COMMENT ON COLUMN member_feature_quota_usage.creator_id IS '创建者ID';
COMMENT ON COLUMN member_feature_quota_usage.create_time IS '创建时间';
COMMENT ON COLUMN member_feature_quota_usage.updater_id IS '更新者ID';
COMMENT ON COLUMN member_feature_quota_usage.update_time IS '更新时间';
COMMENT ON COLUMN member_feature_quota_usage.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE user_member_order IS '用户会员订单表';
COMMENT ON COLUMN user_member_order.user_member_order_id IS '用户会员订单ID';
COMMENT ON COLUMN user_member_order.order_no IS '订单号';
COMMENT ON COLUMN user_member_order.subscription_id IS '订阅ID';
COMMENT ON COLUMN user_member_order.user_id IS '用户ID';
COMMENT ON COLUMN user_member_order.member_level_id IS '会员等级ID';
COMMENT ON COLUMN user_member_order.order_type IS '订单类型 OPEN/RENEW/GRANT/REDEEM';
COMMENT ON COLUMN user_member_order.order_status IS '订单状态 SUCCESS/FAILED/CANCELED';
COMMENT ON COLUMN user_member_order.source_type IS '来源类型 APP_USER/AUTO_RENEW/ADMIN/REDEEM_CODE';
COMMENT ON COLUMN user_member_order.order_amount IS '订单金额';
COMMENT ON COLUMN user_member_order.pay_time IS '支付时间';
COMMENT ON COLUMN user_member_order.effective_start_time IS '会员开始时间';
COMMENT ON COLUMN user_member_order.effective_end_time IS '会员结束时间';
COMMENT ON COLUMN user_member_order.remark IS '备注';
COMMENT ON COLUMN user_member_order.creator_id IS '创建者ID';
COMMENT ON COLUMN user_member_order.create_time IS '创建时间';
COMMENT ON COLUMN user_member_order.updater_id IS '更新者ID';
COMMENT ON COLUMN user_member_order.update_time IS '更新时间';
COMMENT ON COLUMN user_member_order.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE user_member_subscription IS '用户会员订阅表';
COMMENT ON COLUMN user_member_subscription.subscription_id IS '订阅ID';
COMMENT ON COLUMN user_member_subscription.subscription_no IS '订阅编号';
COMMENT ON COLUMN user_member_subscription.user_id IS '用户ID';
COMMENT ON COLUMN user_member_subscription.member_level_id IS '会员等级ID';
COMMENT ON COLUMN user_member_subscription.subscription_status IS '订阅状态 ACTIVE/CANCELED/EXPIRED/FAILED';
COMMENT ON COLUMN user_member_subscription.auto_renew IS '是否自动续费 0否1是';
COMMENT ON COLUMN user_member_subscription.current_period_start_time IS '当前周期开始时间';
COMMENT ON COLUMN user_member_subscription.current_period_end_time IS '当前周期结束时间';
COMMENT ON COLUMN user_member_subscription.next_renew_time IS '下次续费时间';
COMMENT ON COLUMN user_member_subscription.last_renew_time IS '最近续费时间';
COMMENT ON COLUMN user_member_subscription.failed_renew_count IS '连续失败次数';
COMMENT ON COLUMN user_member_subscription.cancel_time IS '取消时间';
COMMENT ON COLUMN user_member_subscription.cancel_reason IS '取消原因';
COMMENT ON COLUMN user_member_subscription.remark IS '备注';
COMMENT ON COLUMN user_member_subscription.creator_id IS '创建者ID';
COMMENT ON COLUMN user_member_subscription.create_time IS '创建时间';
COMMENT ON COLUMN user_member_subscription.updater_id IS '更新者ID';
COMMENT ON COLUMN user_member_subscription.update_time IS '更新时间';
COMMENT ON COLUMN user_member_subscription.deleted IS '删除标志（0存在 1删除）';

INSERT INTO member_feature (
    feature_code, feature_name, feature_type, quota_period_type, free_enabled, free_limit_value,
    feature_sort, status, is_builtin, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('AI_REPORT_PARSE', 'AI报告解析开关', 'SWITCH', NULL, 1, NULL, 10, 1, 1, '控制是否允许主动发起AI报告解析', NULL, NOW(), NULL, NOW(), 0),
    ('AI_REPORT_PARSE_QUOTA', 'AI报告解析次数', 'QUOTA', 'MONTH', 1, 3, 20, 1, 1, '控制每月AI报告解析次数', NULL, NOW(), NULL, NOW(), 0),
    ('AI_RESULT_INTERPRETATION', 'AI结果解读开关', 'SWITCH', NULL, 1, NULL, 30, 1, 1, '控制是否允许生成结果解读', NULL, NOW(), NULL, NOW(), 0),
    ('AI_RESULT_INTERPRETATION_QUOTA', 'AI结果解读次数', 'QUOTA', 'MONTH', 1, 3, 40, 1, 1, '控制每月AI结果解读次数', NULL, NOW(), NULL, NOW(), 0),
    ('AI_REPORT_SUMMARY', 'AI报告总结开关', 'SWITCH', NULL, 0, NULL, 50, 1, 1, '控制是否允许生成AI报告总结', NULL, NOW(), NULL, NOW(), 0),
    ('AI_REPORT_SUMMARY_QUOTA', 'AI报告总结次数', 'QUOTA', 'MONTH', 0, 0, 60, 1, 1, '控制每月AI报告总结次数', NULL, NOW(), NULL, NOW(), 0),
    ('MAX_FAMILY_MEMBER', '家庭成员数量上限', 'LIMIT', NULL, 1, 5, 70, 1, 1, '控制最多可创建的家庭成员数量', NULL, NOW(), NULL, NOW(), 0),
    ('MAX_REPORT_UPLOAD_COUNT', '报告上传数量上限', 'LIMIT', NULL, 1, 50, 80, 1, 1, '控制最多可上传的报告数量', NULL, NOW(), NULL, NOW(), 0),
    ('MAX_ACTIVE_MEDICATION_PLAN', '启用中用药计划上限', 'LIMIT', NULL, 1, 20, 90, 1, 1, '控制最多启用中的用药计划数量', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (feature_code) DO NOTHING;

INSERT INTO member_level (
    level_code, level_name, level_sort, price, duration_days, benefit_desc,
    status, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('FREE', '免费版', 10, 0, NULL, '基础健康管理功能，提供少量AI调用额度', 1, '系统默认等级', NULL, NOW(), NULL, NOW(), 0),
    ('PLUS', '健康PLUS会员', 20, 29.90, 30, '提升AI解析次数与结果解读能力', 1, '月度PLUS会员', NULL, NOW(), NULL, NOW(), 0),
    ('PRO', '健康PRO会员', 30, 99.90, 365, '更高AI额度与更完整的健康管理能力', 1, '年度PRO会员', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (level_code) DO NOTHING;
