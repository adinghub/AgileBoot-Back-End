-- 健康系统 App Push 设备级派发审计表 MySQL 增量脚本
--
-- 用途与 PostgreSQL 脚本一致：
-- 1. 补齐设备级发送现场快照
-- 2. 支持后台设备管理与 Push 派发审计排障

CREATE TABLE IF NOT EXISTS app_push_delivery_log
(
    delivery_id BIGINT AUTO_INCREMENT COMMENT '设备级派发审计ID'
        PRIMARY KEY,
    message_id BIGINT NULL COMMENT '消息ID',
    owner_user_id BIGINT NOT NULL COMMENT '归属App用户ID',
    member_id BIGINT NULL COMMENT '家庭成员ID',
    business_scene VARCHAR(40) NOT NULL COMMENT '业务场景编码',
    business_id BIGINT NULL COMMENT '业务主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(64) NULL COMMENT '设备唯一编码快照',
    push_platform VARCHAR(20) NULL COMMENT '推送平台快照',
    device_status_snapshot SMALLINT NULL COMMENT '发送当时的设备状态快照',
    device_token_masked VARCHAR(40) NULL COMMENT '脱敏后的设备Token',
    send_channel VARCHAR(30) NULL COMMENT '发送通道',
    send_status SMALLINT NOT NULL COMMENT '设备级发送状态（1成功 2失败）',
    failure_reason_category VARCHAR(30) NULL COMMENT '失败原因分类',
    failure_reason_message VARCHAR(255) NULL COMMENT '失败原因说明',
    vendor_code VARCHAR(50) NULL COMMENT '厂商结果码',
    vendor_message VARCHAR(255) NULL COMMENT '厂商结果说明',
    retry_no INT DEFAULT 1 NOT NULL COMMENT '本条消息针对该设备的第几次尝试',
    send_time DATETIME NULL COMMENT '发送时间',
    creator_id BIGINT NULL COMMENT '创建者ID',
    create_time DATETIME NULL COMMENT '创建时间',
    updater_id BIGINT NULL COMMENT '更新者ID',
    update_time DATETIME NULL COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除'
)
    COMMENT '健康系统App Push设备级派发审计表';

CREATE INDEX idx_app_push_delivery_log_message_id ON app_push_delivery_log (message_id);
CREATE INDEX idx_app_push_delivery_log_owner_user_id ON app_push_delivery_log (owner_user_id);
CREATE INDEX idx_app_push_delivery_log_device_id ON app_push_delivery_log (device_id);
CREATE INDEX idx_app_push_delivery_log_business_scene ON app_push_delivery_log (business_scene);
CREATE INDEX idx_app_push_delivery_log_send_time ON app_push_delivery_log (send_time);
CREATE INDEX idx_app_push_delivery_log_send_status ON app_push_delivery_log (send_status);
CREATE INDEX idx_app_push_delivery_log_failure_reason_category ON app_push_delivery_log (failure_reason_category);
CREATE INDEX idx_app_push_delivery_log_message_retry ON app_push_delivery_log (message_id, retry_no);
