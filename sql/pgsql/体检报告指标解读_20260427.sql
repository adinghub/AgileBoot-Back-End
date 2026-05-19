-- 体检报告指标解读增量脚本
--
-- 目标：
-- 1. 为 report_item 增加 item_interpretation 字段；
-- 2. 让 AI / 后台解析后的每个指标都可以单独保存“这个指标主要反映什么”的简明解释；
-- 3. 该解释是指标含义说明，不是诊断结论。

ALTER TABLE report_item
    ADD COLUMN IF NOT EXISTS item_interpretation VARCHAR(300);

COMMENT ON COLUMN report_item.item_interpretation IS '指标解读，说明该指标主要反映什么';
