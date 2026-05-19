-- 体检报告识别类型字段增量脚本
--
-- 目标：
-- 1. 保留用户原始填写的 report_type
-- 2. 新增 recognized_report_type 存储模型识别出的更细粒度报告类型
-- 3. 供 App 端展示“血液检查（血常规）”这类补充文案

ALTER TABLE report
    ADD COLUMN IF NOT EXISTS recognized_report_type VARCHAR(50);

COMMENT ON COLUMN report.recognized_report_type IS '后台识别出的报告细分类型';
