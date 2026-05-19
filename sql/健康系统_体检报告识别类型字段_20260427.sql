-- 健康系统体检报告识别类型字段增量脚本
--
-- 这个字段和用户手填 report_type 分开保存：
-- 1. report_type 保留用户上传时的主观归类
-- 2. recognized_report_type 保存后台模型识别出的更准确细分类型
-- 3. 前端可据此展示“用户填写值 + 识别补充值”

ALTER TABLE health_report
    ADD COLUMN recognized_report_type varchar(50) null comment '后台识别出的报告细分类型'
    after report_type;
