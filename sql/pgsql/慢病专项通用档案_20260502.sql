-- 慢病专项通用档案
--
-- 设计目标：支持所有慢病，而不是只给高血压、糖尿病等少数病种写死代码。
-- 因此这里拆成两层：
-- 1. health_chronic_disease_type：病种配置，维护病种名称、指标编码、指标关键词和随访建议；
-- 2. health_chronic_disease_profile：用户专项档案，记录某个家庭成员正在管理的慢病。
-- 后续新增病种时优先新增 type 配置，已接入 App/后端的通用专项链路无需再次发版。

CREATE TABLE IF NOT EXISTS health_chronic_disease_type (
    type_id BIGSERIAL PRIMARY KEY,
    disease_code VARCHAR(64) NOT NULL,
    disease_name VARCHAR(100) NOT NULL,
    disease_category VARCHAR(50),
    focus_indicator_codes_json VARCHAR(1000),
    focus_indicator_keywords_json VARCHAR(1000),
    target_summary VARCHAR(500),
    follow_up_suggestion VARCHAR(1000),
    sort INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_health_chronic_disease_type_code UNIQUE (disease_code)
);

CREATE INDEX IF NOT EXISTS idx_health_chronic_disease_type_status ON health_chronic_disease_type (status);
CREATE INDEX IF NOT EXISTS idx_health_chronic_disease_type_sort ON health_chronic_disease_type (sort);

CREATE TABLE IF NOT EXISTS health_chronic_disease_profile (
    profile_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    disease_code VARCHAR(64) NOT NULL,
    disease_name_snapshot VARCHAR(100) NOT NULL,
    profile_status SMALLINT DEFAULT 1 NOT NULL,
    risk_level VARCHAR(20) DEFAULT 'LOW' NOT NULL,
    diagnosed_date DATE,
    target_summary VARCHAR(500),
    current_summary VARCHAR(1000),
    last_review_date DATE,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_health_chronic_profile_member_disease
    ON health_chronic_disease_profile (member_id, disease_code, deleted);
CREATE INDEX IF NOT EXISTS idx_health_chronic_profile_owner_user_id ON health_chronic_disease_profile (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_health_chronic_profile_member_id ON health_chronic_disease_profile (member_id);
CREATE INDEX IF NOT EXISTS idx_health_chronic_profile_status ON health_chronic_disease_profile (profile_status);
CREATE INDEX IF NOT EXISTS idx_health_chronic_profile_risk ON health_chronic_disease_profile (risk_level);

COMMENT ON TABLE health_chronic_disease_type IS '健康系统慢病病种配置表';
COMMENT ON COLUMN health_chronic_disease_type.type_id IS '慢病病种ID';
COMMENT ON COLUMN health_chronic_disease_type.disease_code IS '病种编码，稳定业务标识，例如 DIABETES';
COMMENT ON COLUMN health_chronic_disease_type.disease_name IS '病种名称';
COMMENT ON COLUMN health_chronic_disease_type.disease_category IS '病种分类，例如代谢、心血管、呼吸、肾脏';
COMMENT ON COLUMN health_chronic_disease_type.focus_indicator_codes_json IS '重点关注指标编码JSON数组，优先匹配 report_item.standard_item_code / item_code';
COMMENT ON COLUMN health_chronic_disease_type.focus_indicator_keywords_json IS '重点关注指标关键词JSON数组，用于指标标准编码缺失时匹配 item_name';
COMMENT ON COLUMN health_chronic_disease_type.target_summary IS '默认管理目标摘要';
COMMENT ON COLUMN health_chronic_disease_type.follow_up_suggestion IS '默认随访建议';
COMMENT ON COLUMN health_chronic_disease_type.sort IS '排序号';
COMMENT ON COLUMN health_chronic_disease_type.status IS '状态（1启用 0停用）';
COMMENT ON COLUMN health_chronic_disease_type.remark IS '备注';
COMMENT ON COLUMN health_chronic_disease_type.creator_id IS '创建者ID';
COMMENT ON COLUMN health_chronic_disease_type.create_time IS '创建时间';
COMMENT ON COLUMN health_chronic_disease_type.updater_id IS '更新者ID';
COMMENT ON COLUMN health_chronic_disease_type.update_time IS '更新时间';
COMMENT ON COLUMN health_chronic_disease_type.deleted IS '逻辑删除';

COMMENT ON TABLE health_chronic_disease_profile IS '健康系统用户慢病专项档案表';
COMMENT ON COLUMN health_chronic_disease_profile.profile_id IS '慢病专项档案ID';
COMMENT ON COLUMN health_chronic_disease_profile.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_chronic_disease_profile.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_chronic_disease_profile.disease_code IS '病种编码';
COMMENT ON COLUMN health_chronic_disease_profile.disease_name_snapshot IS '病种名称快照，避免病种配置改名后历史档案完全失去原始展示名';
COMMENT ON COLUMN health_chronic_disease_profile.profile_status IS '专项状态（1跟进中 2已稳定 3已关闭）';
COMMENT ON COLUMN health_chronic_disease_profile.risk_level IS '关注优先级（LOW/MEDIUM/HIGH），用于产品展示强调，不代表医学诊断等级';
COMMENT ON COLUMN health_chronic_disease_profile.diagnosed_date IS '确诊或建档日期';
COMMENT ON COLUMN health_chronic_disease_profile.target_summary IS '个性化管理目标';
COMMENT ON COLUMN health_chronic_disease_profile.current_summary IS '当前情况摘要';
COMMENT ON COLUMN health_chronic_disease_profile.last_review_date IS '最近复盘日期';
COMMENT ON COLUMN health_chronic_disease_profile.remark IS '备注';
COMMENT ON COLUMN health_chronic_disease_profile.creator_id IS '创建者ID';
COMMENT ON COLUMN health_chronic_disease_profile.create_time IS '创建时间';
COMMENT ON COLUMN health_chronic_disease_profile.updater_id IS '更新者ID';
COMMENT ON COLUMN health_chronic_disease_profile.update_time IS '更新时间';
COMMENT ON COLUMN health_chronic_disease_profile.deleted IS '逻辑删除';

INSERT INTO health_chronic_disease_type (
    disease_code, disease_name, disease_category, focus_indicator_codes_json,
    focus_indicator_keywords_json, target_summary, follow_up_suggestion, sort, status, remark
) VALUES
('HYPERTENSION', '高血压管理', '心血管', '["SBP","DBP","BLOOD_PRESSURE"]', '["血压","收缩压","舒张压"]',
 '关注血压波动、复查节奏和用药执行情况。', '建议持续记录血压，结合医生建议调整复查和用药计划。', 10, 1, '内置常见慢病配置'),
('DIABETES', '糖尿病管理', '代谢', '["GLU","FPG","HBA1C","2H_PG"]', '["血糖","葡萄糖","糖化血红蛋白","空腹血糖"]',
 '关注血糖、糖化血红蛋白、用药和饮食执行情况。', '建议按周期复查血糖和糖化血红蛋白，并维护用药提醒。', 20, 1, '内置常见慢病配置'),
('HYPERLIPIDEMIA', '血脂异常管理', '代谢', '["TC","TG","LDL_C","HDL_C"]', '["胆固醇","甘油三酯","低密度脂蛋白","高密度脂蛋白","血脂"]',
 '关注血脂指标变化、饮食运动和复查计划。', '建议结合最近血脂报告安排复查，并持续观察低密度脂蛋白变化。', 30, 1, '内置常见慢病配置'),
('HYPERURICEMIA_GOUT', '高尿酸/痛风管理', '代谢', '["UA","URIC_ACID"]', '["尿酸","痛风"]',
 '关注尿酸水平、痛风发作记录和饮食控制。', '建议持续观察尿酸趋势，必要时补充复查任务和用药提醒。', 40, 1, '内置常见慢病配置'),
('CHRONIC_KIDNEY_DISEASE', '慢性肾病管理', '肾脏', '["CREA","UREA","EGFR","UACR","PROTEINURIA"]', '["肌酐","尿素","肾小球滤过率","尿蛋白","微量白蛋白"]',
 '关注肾功能、尿蛋白和复查节奏。', '建议定期复查肾功能与尿蛋白指标，异常时及时线下就医确认。', 50, 1, '内置常见慢病配置'),
('FATTY_LIVER', '脂肪肝/肝功能管理', '肝胆', '["ALT","AST","GGT","TBIL"]', '["谷丙转氨酶","谷草转氨酶","转氨酶","GGT","胆红素","脂肪肝"]',
 '关注肝功能指标、体重管理和复查计划。', '建议结合肝功能报告和影像检查，持续跟踪转氨酶变化。', 60, 1, '内置常见慢病配置'),
('COPD_ASTHMA', '慢阻肺/哮喘管理', '呼吸', '["FEV1","FVC","FEV1_FVC","EOS"]', '["肺功能","FEV1","FVC","嗜酸性粒细胞","哮喘","慢阻肺"]',
 '关注肺功能、症状变化和长期用药执行。', '建议维护吸入药或长期用药提醒，并按医嘱安排肺功能复查。', 70, 1, '内置常见慢病配置'),
('CORONARY_HEART_DISEASE', '冠心病风险管理', '心血管', '["LDL_C","TC","TG","HBA1C","BNP","TROPONIN"]', '["低密度脂蛋白","胆固醇","肌钙蛋白","BNP","心肌酶","冠心病"]',
 '关注血脂、心肌相关指标和长期用药执行。', '建议结合医生方案维护用药提醒，异常指标需及时线下确认。', 80, 1, '内置常见慢病配置'),
('THYROID_DISEASE', '甲状腺疾病管理', '内分泌', '["TSH","FT3","FT4","T3","T4"]', '["促甲状腺激素","游离三碘甲状腺原氨酸","游离甲状腺素","甲状腺"]',
 '关注甲状腺功能指标和复查节奏。', '建议按周期复查甲功，并根据医生方案维护用药提醒。', 90, 1, '内置常见慢病配置'),
('ANEMIA', '贫血管理', '血液', '["HGB","RBC","MCV","FERRITIN","FOLATE","VB12"]', '["血红蛋白","红细胞","平均红细胞体积","铁蛋白","叶酸","维生素B12","贫血"]',
 '关注血红蛋白、红细胞和营养相关指标。', '建议结合血常规和营养指标持续观察，必要时安排复查或用药提醒。', 100, 1, '内置常见慢病配置')
ON CONFLICT (disease_code) DO UPDATE SET
    disease_name = EXCLUDED.disease_name,
    disease_category = EXCLUDED.disease_category,
    focus_indicator_codes_json = EXCLUDED.focus_indicator_codes_json,
    focus_indicator_keywords_json = EXCLUDED.focus_indicator_keywords_json,
    target_summary = EXCLUDED.target_summary,
    follow_up_suggestion = EXCLUDED.follow_up_suggestion,
    sort = EXCLUDED.sort,
    status = EXCLUDED.status,
    remark = EXCLUDED.remark,
    update_time = NOW();
