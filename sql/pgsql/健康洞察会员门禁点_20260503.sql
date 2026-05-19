-- 健康洞察高级能力会员门禁点
--
-- 本增量只登记可被会员策略控制的业务动作，默认策略为放行。
-- 后台后续可把这些动作绑定到具体会员权益或等级要求。

INSERT INTO member_gate (
    gate_code, gate_name, gate_scope, biz_module, terminal_type, default_policy_type,
    status, is_builtin, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('HEALTH.VISIT_PACKAGE.EXPORT', '就医资料包导出', 'APP_ACTION', 'HEALTH_INSIGHT', 'APP', 'ALLOW', 1, 1, '控制健康洞察里的就医资料包导出动作', NULL, NOW(), NULL, NOW(), 0),
    ('HEALTH.PROBLEM.REVIEW_TASK', '健康问题复查提醒', 'APP_ACTION', 'HEALTH_INSIGHT', 'APP', 'ALLOW', 1, 1, '控制健康问题详情里的复查提醒创建动作', NULL, NOW(), NULL, NOW(), 0),
    ('HEALTH.PERIODIC_REPORT.VIEW', '健康周报月报查看', 'APP_ACTION', 'HEALTH_INSIGHT', 'APP', 'ALLOW', 1, 1, '控制健康洞察里的周报月报查看动作', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (gate_code) DO NOTHING;