-- 健康系统 App Push 设备级派发审计表 PostgreSQL 增量脚本
--
-- 设计目的：
-- 1. app_message 只能看到“整条消息最近一次整体发送结果”
-- 2. 后台排障还需要看到“具体命中了哪些设备、每台设备结果怎样”
-- 3. 因此新增 app_push_delivery_log 作为设备级发送现场快照

CREATE TABLE IF NOT EXISTS app_push_delivery_log (
    delivery_id BIGSERIAL PRIMARY KEY,
    message_id BIGINT,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    business_scene VARCHAR(40) NOT NULL,
    business_id BIGINT,
    device_id BIGINT NOT NULL,
    device_code VARCHAR(64),
    push_platform VARCHAR(20),
    device_status_snapshot SMALLINT,
    device_token_masked VARCHAR(40),
    send_channel VARCHAR(30),
    send_status SMALLINT NOT NULL,
    failure_reason_category VARCHAR(30),
    failure_reason_message VARCHAR(255),
    vendor_code VARCHAR(50),
    vendor_message VARCHAR(255),
    retry_no INT DEFAULT 1 NOT NULL,
    send_time TIMESTAMPTZ,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

COMMENT ON TABLE app_push_delivery_log IS '健康系统App Push设备级派发审计表';
COMMENT ON COLUMN app_push_delivery_log.delivery_id IS '设备级派发审计ID';
COMMENT ON COLUMN app_push_delivery_log.message_id IS '消息ID';
COMMENT ON COLUMN app_push_delivery_log.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN app_push_delivery_log.member_id IS '家庭成员ID';
COMMENT ON COLUMN app_push_delivery_log.business_scene IS '业务场景编码';
COMMENT ON COLUMN app_push_delivery_log.business_id IS '业务主键ID';
COMMENT ON COLUMN app_push_delivery_log.device_id IS '设备ID';
COMMENT ON COLUMN app_push_delivery_log.device_code IS '设备唯一编码快照';
COMMENT ON COLUMN app_push_delivery_log.push_platform IS '推送平台快照';
COMMENT ON COLUMN app_push_delivery_log.device_status_snapshot IS '发送当时的设备状态快照';
COMMENT ON COLUMN app_push_delivery_log.device_token_masked IS '脱敏后的设备Token';
COMMENT ON COLUMN app_push_delivery_log.send_channel IS '发送通道';
COMMENT ON COLUMN app_push_delivery_log.send_status IS '设备级发送状态（1成功 2失败）';
COMMENT ON COLUMN app_push_delivery_log.failure_reason_category IS '失败原因分类';
COMMENT ON COLUMN app_push_delivery_log.failure_reason_message IS '失败原因说明';
COMMENT ON COLUMN app_push_delivery_log.vendor_code IS '厂商结果码';
COMMENT ON COLUMN app_push_delivery_log.vendor_message IS '厂商结果说明';
COMMENT ON COLUMN app_push_delivery_log.retry_no IS '本条消息针对该设备的第几次尝试';
COMMENT ON COLUMN app_push_delivery_log.send_time IS '发送时间';

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_message_id
    ON app_push_delivery_log (message_id);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_owner_user_id
    ON app_push_delivery_log (owner_user_id);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_device_id
    ON app_push_delivery_log (device_id);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_business_scene
    ON app_push_delivery_log (business_scene);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_send_time
    ON app_push_delivery_log (send_time);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_send_status
    ON app_push_delivery_log (send_status);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_failure_reason_category
    ON app_push_delivery_log (failure_reason_category);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_message_retry
    ON app_push_delivery_log (message_id, retry_no);
