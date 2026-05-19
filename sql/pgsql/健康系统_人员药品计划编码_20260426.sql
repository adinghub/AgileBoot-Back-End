-- 健康系统“人员 / 药品 / 计划”业务编码补齐脚本
--
-- 说明：
-- 1. 本脚本用于 PostgreSQL 增量升级环境
-- 2. 编码规则统一为“固定前缀 + 主键原值”
-- 3. 历史数据会按主键一次性回填，后续新增数据由应用层自动补写

ALTER TABLE family_member
    ADD COLUMN IF NOT EXISTS member_code VARCHAR(32);
COMMENT ON COLUMN family_member.member_code IS '家庭成员业务编码，优先给前端展示使用';

UPDATE family_member
SET member_code = 'MBR' || member_id::text
WHERE member_code IS NULL OR BTRIM(member_code) = '';

CREATE UNIQUE INDEX IF NOT EXISTS uk_family_member_code
    ON family_member (member_code);

ALTER TABLE drug
    ADD COLUMN IF NOT EXISTS drug_code VARCHAR(32);
COMMENT ON COLUMN drug.drug_code IS '药品业务编码，优先给前端展示使用';

UPDATE drug
SET drug_code = 'DRG' || drug_id::text
WHERE drug_code IS NULL OR BTRIM(drug_code) = '';

CREATE UNIQUE INDEX IF NOT EXISTS uk_drug_code
    ON drug (drug_code);

ALTER TABLE medication_plan
    ADD COLUMN IF NOT EXISTS plan_code VARCHAR(32);
COMMENT ON COLUMN medication_plan.plan_code IS '用药计划业务编码，优先给前端展示使用';

UPDATE medication_plan
SET plan_code = 'PLN' || plan_id::text
WHERE plan_code IS NULL OR BTRIM(plan_code) = '';

CREATE UNIQUE INDEX IF NOT EXISTS uk_medication_plan_code
    ON medication_plan (plan_code);
