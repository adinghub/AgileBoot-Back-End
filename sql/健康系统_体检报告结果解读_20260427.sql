ALTER TABLE health_report
    ADD COLUMN result_interpretation varchar(2000) null comment '结果解读，结合本次结果说明主要结论和实验有效性' after analysis_summary;
