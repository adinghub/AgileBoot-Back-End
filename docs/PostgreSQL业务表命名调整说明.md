# PostgreSQL业务表命名调整说明

## 1. 背景

当前项目数据库已确定使用 PostgreSQL，表直接落在默认 schema 下，不再额外维护 `app` schema。

根据最新确认的口径：

1. 业务表名直接使用功能语义名
2. 不再额外增加 `health_*` 业务前缀
3. 当前数据库尚未正式部署、没有业务数据，因此直接按最终表名落地


## 2. 命名原则

统一遵循以下规则：

1. 表名使用小写下划线风格
2. 主表优先使用单数业务名，例如 `drug`、`report`、`app_user`
3. 明细、日志、关系表使用明确后缀，例如 `_item`、`_log`、`_share`、`_invite`、`_batch`
4. 公共能力表也直接使用业务名，例如 `attachment`、`drug_unit`
5. 索引和唯一约束命名与最终表名保持一致，例如 `uk_drug_owner_name`、`idx_drug_unit_status`
6. 当前仍采用逻辑关联，不强制增加数据库物理外键


## 3. 业务表命名映射

| 旧表名 | 新表名 | 说明 |
| --- | --- | --- |
| `health_app_user` | `app_user` | App 用户 |
| `health_family_member` | `family_member` | 家庭成员 |
| `health_drug` | `drug` | 药品主表 |
| `health_drug_stock_batch` | `drug_stock_batch` | 药品批号效期库存 |
| `health_drug_stock_log` | `drug_stock_log` | 药品库存变更日志 |
| `health_medication_plan` | `medication_plan` | 用药计划 |
| `health_medication_reminder` | `medication_reminder` | 用药提醒 |
| `health_report` | `report` | 体检报告 |
| `health_report_item` | `report_item` | 体检报告指标明细 |
| `health_app_device` | `app_device` | App 设备 |
| `health_app_message` | `app_message` | App 消息中心 |
| `health_follow_up_task` | `follow_up_task` | 首页待跟进任务 |
| `health_follow_up_task_log` | `follow_up_task_log` | 首页待跟进任务日志 |
| `health_operation_task` | `operation_task` | 首页运营任务 |
| `health_family_member_share` | `family_member_share` | 家庭成员共享关系 |
| `health_family_share_invite` | `family_share_invite` | 家庭共享邀请 |
| `health_indicator_template` | `indicator_template` | 指标模板 |
| `health_drug_unit` | `drug_unit` | 药品单位主数据 |
| `health_drug_attachment` | `attachment` | 统一附件主数据 |


## 4. 药品相关命名补充

药品模块当前统一使用以下表名组合：

1. `drug`
2. `drug_unit`
3. `attachment`
4. `drug_stock_batch`
5. `drug_stock_log`

对应关系：

1. `drug` 保存药品主档、单位快照、图片附件 ID、预警值等主数据
2. `drug` 不保存汇总库存
3. `drug_stock_batch` 保存批号、效期、库存数量
4. `drug_stock_log` 保存库存变更轨迹
5. `drug_unit` 保存后台统一维护的药品单位
6. `attachment` 保存药品图片和药品单位图标等附件元数据

特别说明：

1. 当前不再保留 `drug_attachment` 这一专用表名
2. 附件表统一抽象为 `attachment`，便于后续复用到更多业务场景


## 5. 推荐索引与约束命名方式

建议统一采用以下命名风格：

1. 唯一约束或唯一索引：`uk_表名_业务字段`
2. 普通索引：`idx_表名_字段`
3. 主键使用默认主键命名即可，不再强调业务前缀

示例：

1. `uk_drug_owner_name`
2. `uk_drug_unit_code`
3. `idx_attachment_type`
4. `idx_drug_stock_batch_expire_date`
5. `idx_follow_up_task_owner_read_status`


## 6. 落地影响范围

当前直接按最终命名落地时，需要统一以下内容：

1. PostgreSQL 初始化脚本
2. H2 测试建表脚本
3. Java 实体 `@TableName`
4. Mapper 与 XML SQL
5. 文档中的表名示例

因为当前没有线上业务数据，所以不建议保留兼容表、别名表或迁移中间表，直接切到最终命名即可。


## 7. 当前执行建议

当前阶段把这份文档作为数据库命名基线即可。

落地顺序建议为：

1. 先统一 PostgreSQL 初始化 DDL
2. 再统一 H2 测试脚本
3. 再统一 Java 实体映射与查询 SQL
4. 最后统一设计文档和接口对接文档
