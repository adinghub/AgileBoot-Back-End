-- 药品近效期提醒能力增量脚本
--
-- 目标：
-- 1. 在批次表上记录近效期提醒状态
-- 2. 支持按“仍有库存 + 未来 7 天内到期”的批次做站内消息提醒

ALTER TABLE drug_stock_batch
    ADD COLUMN IF NOT EXISTS near_expire_notified SMALLINT DEFAULT 0 NOT NULL;

ALTER TABLE drug_stock_batch
    ADD COLUMN IF NOT EXISTS near_expire_notify_time TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_near_expire_notified
    ON drug_stock_batch (near_expire_notified);

COMMENT ON COLUMN drug_stock_batch.near_expire_notified IS '当前近效期提醒周期内是否已经提醒过';
COMMENT ON COLUMN drug_stock_batch.near_expire_notify_time IS '最近一次近效期提醒发送时间';
