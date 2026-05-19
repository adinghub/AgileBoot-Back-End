
-- 健康系统 H2 测试环境种子数据
--
-- 说明：
-- 1. 当前只放“系统常用药”示例数据，保持与 PostgreSQL 初始化脚本一致。
-- 2. App 用户、家庭成员、用药计划、报告等业务数据通常由测试用例或接口自行创建，
--    这样可以让测试场景更贴近真实业务流程。

INSERT INTO drug_unit (
    unit_id, unit_code, unit_name, unit_alias, precision_scale, sort, status,
    remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    (1, 'PIAN', '片', '片剂,tablet', 0, 10, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (2, 'LI', '粒', '胶囊,颗粒', 0, 20, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (3, 'ZHI', '支', '支装', 0, 30, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (4, 'DAI', '袋', '包,袋装', 0, 40, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (5, 'HE', '盒', '盒装', 0, 50, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (6, 'PING', '瓶', '瓶装', 0, 60, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (7, 'ML', 'ml', '毫升,ML', 2, 70, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    (8, 'G', 'g', '克,G', 2, 80, 1, '系统初始化药品单位', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0);

INSERT INTO drug (
    owner_user_id, drug_name, generic_name, brand_name, dosage_form, specification,
    indication, usage_instruction, adverse_reaction, contraindication, manufacturer, drug_type, stock_unit_id,
    stock_unit, image_attachment_id, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES (
    0, '阿司匹林肠溶片', '阿司匹林', '拜阿司匹灵', '肠溶片', '100mg*30片',
    '抗血小板聚集，预防心脑血管事件', '成人通常一次1片，一日1次，遵医嘱服用', '胃部不适、出血风险增加',
    '活动性消化道出血、阿司匹林过敏者禁用', '拜耳医药保健有限公司', 'RX', 1,
    '片', NULL, 1, '系统下发常用药示例数据', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0
);

INSERT INTO drug (
    owner_user_id, drug_name, generic_name, brand_name, dosage_form, specification,
    indication, usage_instruction, adverse_reaction, contraindication, manufacturer, drug_type, stock_unit_id,
    stock_unit, image_attachment_id, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES (
    0, '二甲双胍片', '盐酸二甲双胍', '格华止', '片剂', '0.5g*20片',
    '用于2型糖尿病血糖控制', '随餐或餐后服用，起始剂量遵医嘱', '恶心、腹泻、腹胀',
    '严重肾功能不全、代谢性酸中毒患者禁用', '中美上海施贵宝制药有限公司', 'RX', 1,
    '片', NULL, 1, '系统下发常用药示例数据', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0
);

INSERT INTO health_chronic_disease_type (
    disease_code, disease_name, disease_category, focus_indicator_codes_json,
    focus_indicator_keywords_json, target_summary, follow_up_suggestion, sort, status, remark,
    creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('HYPERTENSION', '高血压管理', '心血管', '["SBP","DBP","BLOOD_PRESSURE"]', '["血压","收缩压","舒张压"]',
     '关注血压波动、复查节奏和用药执行情况。', '建议持续记录血压，结合医生建议调整复查和用药计划。', 10, 1, '内置常见慢病配置',
     NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('DIABETES', '糖尿病管理', '代谢', '["GLU","FPG","HBA1C","2H_PG"]', '["血糖","葡萄糖","糖化血红蛋白","空腹血糖"]',
     '关注血糖、糖化血红蛋白、用药和饮食执行情况。', '建议按周期复查血糖和糖化血红蛋白，并维护用药提醒。', 20, 1, '内置常见慢病配置',
     NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0);

INSERT INTO member_feature (
    feature_code, feature_name, feature_type, quota_period_type, free_enabled, free_limit_value,
    feature_sort, status, is_builtin, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('AI_REPORT_PARSE', 'AI报告解析开关', 'SWITCH', NULL, 1, NULL, 10, 1, 1, '控制是否允许主动发起AI报告解析', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI_REPORT_PARSE_QUOTA', 'AI报告解析次数', 'QUOTA', 'MONTH', 1, 3, 20, 1, 1, '控制每月AI报告解析次数', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI_RESULT_INTERPRETATION', 'AI结果解读开关', 'SWITCH', NULL, 1, NULL, 30, 1, 1, '控制是否允许生成结果解读', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI_RESULT_INTERPRETATION_QUOTA', 'AI结果解读次数', 'QUOTA', 'MONTH', 1, 3, 40, 1, 1, '控制每月AI结果解读次数', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI_REPORT_SUMMARY', 'AI报告总结开关', 'SWITCH', NULL, 0, NULL, 50, 1, 1, '控制是否允许生成AI报告总结', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI_REPORT_SUMMARY_QUOTA', 'AI报告总结次数', 'QUOTA', 'MONTH', 0, 0, 60, 1, 1, '控制每月AI报告总结次数', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('MAX_FAMILY_MEMBER', '家庭成员数量上限', 'LIMIT', NULL, 1, 5, 70, 1, 1, '控制最多可创建的家庭成员数量', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('MAX_REPORT_UPLOAD_COUNT', '报告上传数量上限', 'LIMIT', NULL, 1, 50, 80, 1, 1, '控制最多可上传的报告数量', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('MAX_ACTIVE_MEDICATION_PLAN', '启用中用药计划上限', 'LIMIT', NULL, 1, 20, 90, 1, 1, '控制最多启用中的用药计划数量', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0);

INSERT INTO member_level (
    level_code, level_name, level_sort, price, duration_days, benefit_desc,
    status, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('FREE', '免费版', 10, 0, NULL, '基础健康管理功能，提供少量AI调用额度', 1, '系统默认等级', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('PLUS', '健康PLUS会员', 20, 29.90, 30, '提升AI解析次数与结果解读能力', 1, '月度PLUS会员', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('PRO', '健康PRO会员', 30, 99.90, 365, '更高AI额度与更完整的健康管理能力', 1, '年度PRO会员', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0);

INSERT INTO member_gate (
    gate_code, gate_name, gate_scope, biz_module, terminal_type, default_policy_type,
    status, is_builtin, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    ('REPORT.EXPORT', '报告导出', 'APP_ACTION', 'REPORT', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制报告导出入口', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI.ASSISTANT.OPEN', 'AI助手打开', 'APP_ACTION', 'AI', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制AI助手入口', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('AI.REPORT.EXPORT', 'AI报告导出', 'APP_ACTION', 'AI', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制AI报告导出入口', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0),
    ('TRANSACTION_PLAN.CREATE', '新增计划', 'APP_ACTION', 'TRANSACTION_PLAN', 'APP', 'ALLOW', 1, 1, '示例门禁点：控制计划创建入口', NULL, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, 0);
