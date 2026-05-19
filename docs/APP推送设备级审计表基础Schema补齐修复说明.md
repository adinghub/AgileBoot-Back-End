# APP 推送设备级审计表基础 Schema 补齐修复说明

## 1. 问题背景

低库存提醒链路在业务代码里已经分成两步：

1. 先创建 `app_message` 消息中心记录；
2. 再把本次命中的设备快照写入 `app_push_delivery_log`，用于后台审计和排障。

2026-04-28 新增设备级审计能力时，PostgreSQL 增量脚本 `健康系统_APP推送设备级审计表_20260428.sql`
已经创建了 `app_push_delivery_log`，但基础建库脚本没有同步补齐。

这会带来两个直接后果：

1. H2 集成测试环境只执行基础 schema，不执行线上增量脚本，调用 `AppPushDeliveryApplicationService`
   时会因为缺少 `app_push_delivery_log` 直接抛出数据库异常。
2. 新环境如果直接依赖主 schema 初始化，也会缺这张表，设备级发送审计与低库存提醒状态都会失真。

## 2. 触发路径

`DrugInventoryFlowTest.shouldImportSystemDrugAndAutoDeductStockAfterTakingReminder`
在库存扣减到预警值时会进入低库存提醒分支：

1. `DrugApplicationService.dispatchLowStockAlertQuietly(...)`
2. `AppPushHealthAppMessageNotifier.notify(...)`
3. `AppPushDeliveryApplicationService.recordBatchDelivery(...)`

模拟 Push 分支本身是成功的，但因为 `app_push_delivery_log` 在 H2 schema 中不存在，
第 3 步会抛异常，随后消息被回写成发送失败，`lowStockNotified` 也不会置为 `true`。

## 3. 本次修复

本次把 `app_push_delivery_log` 的最终结构补回两条基础建库路径：

1. `healthtrail-infrastructure/src/main/resources/h2sql/health_test_schema.sql`
   让 API / Domain / Admin 的 H2 集成测试直接具备这张表。
2. `sql/pgsql/健康系统_schema_20260423.sql`
   让 fresh init 的 PostgreSQL 主 schema 与后续增量脚本保持一致。

同步内容包括：

1. 表结构；
2. 发送排障需要的全部索引；
3. PostgreSQL 主 schema 内的表/字段注释。

## 4. 预期结果

修复后：

1. 低库存消息在测试环境可以正常写入设备级审计；
2. 模拟 Push 成功时，`app_message.send_status` 会回写为成功；
3. `drug.low_stock_notified` 会在达到预警值后正确置为 `true`；
4. 新环境不再依赖额外人工补执行增量脚本才能拥有设备级推送审计表。
