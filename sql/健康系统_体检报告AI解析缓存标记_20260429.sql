ALTER TABLE health_report
    ADD COLUMN ai_parse_cached smallint default 0 not null comment 'AI解析结果缓存标记（0未缓存 1已缓存，再次解析时可直接复用已有结构化结果）' after parse_status;
