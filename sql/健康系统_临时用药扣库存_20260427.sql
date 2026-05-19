-- 临时用药扣库存能力增量脚本
--
-- 目标：
-- 1. 允许库存批次出现“无批号、无效期”的默认批次
-- 2. 增加临时用药记录表
-- 3. 增加库存流水对临时用药记录的关联字段

ALTER TABLE drug_stock_batch
    ADD COLUMN IF NOT EXISTS is_default_batch SMALLINT DEFAULT 0 NOT NULL;

ALTER TABLE drug_stock_batch
    ALTER COLUMN expire_date DROP NOT NULL;

UPDATE drug_stock_batch
SET is_default_batch = 1
WHERE deleted = 0
  AND batch_no IS NULL
  AND expire_date IS NULL;

CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_default_batch ON drug_stock_batch (is_default_batch);

ALTER TABLE drug_stock_log
    ADD COLUMN IF NOT EXISTS related_temp_medication_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_drug_stock_log_temp_medication_id
    ON drug_stock_log (related_temp_medication_id);

CREATE TABLE IF NOT EXISTS drug_temporary_medication_record (
    record_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    drug_id BIGINT NOT NULL,
    used_quantity NUMERIC(10, 2) NOT NULL,
    stock_unit_snapshot VARCHAR(20),
    use_time TIMESTAMPTZ NOT NULL,
    symptom VARCHAR(100),
    remark VARCHAR(255),
    deducted_quantity NUMERIC(10, 2) NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_owner_user_id
    ON drug_temporary_medication_record (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_member_id
    ON drug_temporary_medication_record (member_id);
CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_drug_id
    ON drug_temporary_medication_record (drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_use_time
    ON drug_temporary_medication_record (use_time);

COMMENT ON COLUMN drug_stock_batch.is_default_batch IS '是否默认批次（批号和效期都缺失时的兜底库存桶）';
COMMENT ON COLUMN drug_stock_log.related_temp_medication_id IS '关联临时用药记录ID';
COMMENT ON TABLE drug_temporary_medication_record IS '健康系统药品临时用药记录表';
COMMENT ON COLUMN drug_temporary_medication_record.record_id IS '临时用药记录ID';
COMMENT ON COLUMN drug_temporary_medication_record.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN drug_temporary_medication_record.member_id IS '家庭成员ID，可为空';
COMMENT ON COLUMN drug_temporary_medication_record.drug_id IS '药品ID';
COMMENT ON COLUMN drug_temporary_medication_record.used_quantity IS '本次使用数量';
COMMENT ON COLUMN drug_temporary_medication_record.stock_unit_snapshot IS '库存单位快照';
COMMENT ON COLUMN drug_temporary_medication_record.use_time IS '实际用药时间';
COMMENT ON COLUMN drug_temporary_medication_record.symptom IS '用途或症状';
COMMENT ON COLUMN drug_temporary_medication_record.remark IS '备注';
COMMENT ON COLUMN drug_temporary_medication_record.deducted_quantity IS '实际扣减数量';
COMMENT ON COLUMN drug_temporary_medication_record.creator_id IS '创建者ID';
COMMENT ON COLUMN drug_temporary_medication_record.create_time IS '创建时间';
COMMENT ON COLUMN drug_temporary_medication_record.updater_id IS '更新者ID';
COMMENT ON COLUMN drug_temporary_medication_record.update_time IS '更新时间';
COMMENT ON COLUMN drug_temporary_medication_record.deleted IS '逻辑删除';
