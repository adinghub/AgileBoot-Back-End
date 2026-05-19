-- 健康问题与慢病专项显式关联增量脚本
--
-- 背景：慢病专项原先只能按病种指标关键词推断“相关问题数量”，无法表达用户确认过的真实关系。
-- 本表用于保存 health_problem 与 health_chronic_disease_profile 的多对多关系，
-- 使问题中心和慢病详情页可以双向展示、绑定和解绑。

CREATE TABLE IF NOT EXISTS health_problem_chronic_profile_link (
    link_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    link_source VARCHAR(30) DEFAULT 'MANUAL' NOT NULL,
    link_remark VARCHAR(500),
    link_time TIMESTAMPTZ NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_health_problem_chronic_profile_link_pair
    ON health_problem_chronic_profile_link (problem_id, profile_id)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_health_problem_chronic_profile_link_problem
    ON health_problem_chronic_profile_link (problem_id, link_time DESC, link_id DESC)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_health_problem_chronic_profile_link_profile
    ON health_problem_chronic_profile_link (profile_id, link_time DESC, link_id DESC)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_health_problem_chronic_profile_link_member
    ON health_problem_chronic_profile_link (member_id, link_time DESC, link_id DESC)
    WHERE deleted = 0;

COMMENT ON TABLE health_problem_chronic_profile_link IS '健康问题与慢病专项关联表';
COMMENT ON COLUMN health_problem_chronic_profile_link.link_id IS '关联ID';
COMMENT ON COLUMN health_problem_chronic_profile_link.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_problem_chronic_profile_link.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_problem_chronic_profile_link.problem_id IS '健康问题ID';
COMMENT ON COLUMN health_problem_chronic_profile_link.profile_id IS '慢病专项档案ID';
COMMENT ON COLUMN health_problem_chronic_profile_link.link_source IS '关联来源，当前主要为 MANUAL';
COMMENT ON COLUMN health_problem_chronic_profile_link.link_remark IS '关联备注';
COMMENT ON COLUMN health_problem_chronic_profile_link.link_time IS '关联时间';
COMMENT ON COLUMN health_problem_chronic_profile_link.deleted IS '逻辑删除标识';