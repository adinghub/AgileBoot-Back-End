ALTER TABLE report
    ADD COLUMN IF NOT EXISTS result_interpretation VARCHAR(2000);

COMMENT ON COLUMN report.result_interpretation IS '结果解读，结合本次结果说明主要结论和实验有效性';
