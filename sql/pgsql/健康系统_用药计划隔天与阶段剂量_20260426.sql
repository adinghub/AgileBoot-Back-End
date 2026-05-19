-- 用药计划支持长期计划、每几天一次和阶段剂量规则。
--
-- 说明：
-- 1. end_date 改为可空，空值表示长期计划，由后端按滚动窗口生成提醒；
-- 2. interval_days 保存“每几天一次”的自定义间隔，区别于固定“隔日”；
-- 3. dose_rule 独立保存阶段剂量规则，避免继续混在备注里影响后续结构化扩展。

ALTER TABLE medication_plan
    ALTER COLUMN end_date DROP NOT NULL;

ALTER TABLE medication_plan
    ADD COLUMN IF NOT EXISTS interval_days INT;

ALTER TABLE medication_plan
    ADD COLUMN IF NOT EXISTS dose_rule VARCHAR(1000);

COMMENT ON COLUMN medication_plan.end_date IS '结束日期，为空表示长期计划';
COMMENT ON COLUMN medication_plan.interval_days IS '每几天提醒一次，仅 frequency_type = INTERVAL_DAYS 时使用';
COMMENT ON COLUMN medication_plan.dose_rule IS '阶段剂量规则，例如前4天1片、再4天1.5片';
