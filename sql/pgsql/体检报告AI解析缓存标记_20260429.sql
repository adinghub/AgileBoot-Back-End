ALTER TABLE report
    ADD COLUMN IF NOT EXISTS ai_parse_cached SMALLINT DEFAULT 0 NOT NULL;

COMMENT ON COLUMN report.ai_parse_cached IS 'AI解析结果缓存标记（0未缓存 1已缓存，再次解析时可直接复用已有结构化结果）';
