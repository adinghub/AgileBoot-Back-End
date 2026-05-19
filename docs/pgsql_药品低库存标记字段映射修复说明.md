# PostgreSQL 药品低库存标记字段映射修复说明

## 1. 背景

App 端新增个人药品时，后端在写入 `drug.low_stock_notified` 字段时出现了 PostgreSQL 类型异常：

1. 数据库列类型是 `SMALLINT`
2. Java 实体字段是 `Boolean`
3. MyBatis 默认按 `boolean` 参数写入 PostgreSQL
4. PostgreSQL 不会自动把 `boolean` 隐式转换成 `smallint`

最终报错为：

`字段 "low_stock_notified" 的类型为 smallint, 但表达式的类型为 boolean`


## 2. 修复方案

本次修复不改业务层对 `lowStockNotified` 的 Boolean 语义，而是在 MyBatis 映射层补一层显式转换：

1. 新增 `BooleanSmallintTypeHandler`
2. 写库时统一把 `true / false` 转成 `1 / 0`
3. 读库时再把 `0 / 1` 回转成 `Boolean`
4. 在 `DrugEntity.lowStockNotified` 上显式绑定该 type handler
5. 打开 `@TableName(autoResultMap = true)`，确保查询回填时同样走自定义映射


## 3. 为什么这样修

这样处理有几个直接好处：

1. 应用服务、领域模型、DTO 仍然保持最自然的布尔语义，调用方不用跟着改成 `0/1`
2. PostgreSQL、H2、MySQL 三套环境都能复用同一份实体定义
3. 修复点集中在 ORM 边界，后续如果还有类似 `SMALLINT 表示布尔` 的历史字段，也可以复用同一个 type handler


## 4. 回归覆盖

本次补充了后端集成测试，覆盖“App 直接新增个人药品”这条路径，重点校验：

1. 新增药品可以正常落库
2. 标准单位 ID / 单位名称能够正确回填
3. `low_stock_notified` 会以“未提醒”状态正确写入，不再因为 PostgreSQL 类型不匹配而失败
