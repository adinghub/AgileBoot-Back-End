-- 健康洞察二期闭环能力索引增量
--
-- 本增量不新增业务表，只补充健康问题中心、就医资料包导出、慢病专项模板
-- 高频查询所需索引。全部使用 PostgreSQL `IF NOT EXISTS`，可重复执行。

CREATE INDEX IF NOT EXISTS idx_health_problem_member_status_risk
    ON health_problem (member_id, problem_status, risk_level DESC);

CREATE INDEX IF NOT EXISTS idx_health_problem_member_last_follow
    ON health_problem (member_id, last_follow_date DESC, problem_id DESC);

CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_problem_date
    ON health_problem_evidence (problem_id, evidence_date DESC, evidence_id DESC);

CREATE INDEX IF NOT EXISTS idx_health_chronic_disease_type_status_sort
    ON health_chronic_disease_type (status, sort, type_id);