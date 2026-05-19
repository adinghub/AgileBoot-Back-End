-- 健康系统体检报告指标解读增量脚本
--
-- 设计目标：
-- 1. 在 report_item 行级保存“指标解读”，让用户查看某个指标时能直接知道它主要代表什么；
-- 2. 解读关注的是“指标含义”，不是对本次结果做诊断；
-- 3. 字段长度控制在 300 内，既能保证表达完整，也避免模型输出长段落。

ALTER TABLE health_report_item
    ADD COLUMN item_interpretation varchar(300) null comment '指标解读，说明该指标主要反映什么' after reference_text;
