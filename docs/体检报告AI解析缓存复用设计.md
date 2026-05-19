# 体检报告 AI 解析缓存复用设计

## 1. 需求背景

当前体检报告支持用户手动再次点击：

1. `解析报告`

但对一份已经成功解析过的报告来说，重复解析会继续触发：

1. 报告结构化大模型调用
2. 指标解读生成
3. 报告级结果解读生成

这会带来两个问题：

1. 同一份报告反复消耗大模型费用
2. 用户只是想“再点一次确认”，系统却重复做同样的高成本工作

用户本次要求是：

1. 增加一个 `AI 解析标记`
2. 如果之前解析成功了，再次解析就不再调用大模型
3. 但用户侧仍然要保持“正在解析 -> 解析成功”的既有体验

## 2. 设计目标

本次优化的核心目标有三点：

1. 对已经形成稳定结构化结果的报告，重复解析时直接复用旧结果
2. 前端和 App 仍然继续复用现有 `parseStatus` 显示“处理中/成功”
3. 不破坏现有报告详情、摘要、结果解读、指标列表展示

## 3. 方案设计

### 3.1 新增主表缓存标记

在 `report` 主表新增：

1. `ai_parse_cached`

含义不是“这次请求是否正在跑 AI”，而是：

1. 这份报告是否已经形成过可复用的结构化解析结果

取值：

1. `0`：尚未形成可复用结果，后续重新解析仍走完整链路
2. `1`：已经形成可复用结果，后续重新解析可直接复用

### 3.2 重新解析仍然走原有状态流转

用户点击“解析报告”后，后端仍然保持原来两步：

1. 先同步把 `parseStatus` 改成 `PROCESSING`
2. 再异步进入后台解析任务

这样 App 端立即补一次详情查询时，仍然会看到：

1. `正在重新解析`

符合用户原有心智。

### 3.3 命中缓存后直接完成，不再调用大模型

后台任务真正开始执行时，如果满足：

1. `ai_parse_cached = 1`
2. 当前报告下仍然存在结构化指标明细

则本次解析直接走缓存成功路径：

1. 不再调用文件视觉结构化模型
2. 不再重新生成指标解读
3. 不再重新生成报告级结果解读
4. 直接把状态从 `PROCESSING` 切回 `COMPLETED`

并继续保留现有结构化指标、分析摘要、结果解读、解析快照。

### 3.4 只在真正成功产出结构化指标后才写缓存标记

不是所有“解析流程跑完”都应该缓存。

本次规则是：

1. 只有本次解析成功产出了结构化指标，才把 `ai_parse_cached` 写成 `1`
2. 如果解析完成但仍然没有结构化指标，保持 `0`

这样可以避免把以下场景错误缓存：

1. 首次解析完成但没有提取到任何指标
2. 外部模型短暂异常后只留下空结果
3. 脏数据迁移导致主表状态和明细结果不一致

## 4. 影响范围

代码影响：

1. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/HealthReportApplicationService.java`
2. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/db/HealthReportEntity.java`
3. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/model/HealthReportModel.java`
4. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/parser/HealthReportResultInterpretationGenerator.java`

数据库脚本：

1. `sql/pgsql/健康系统_schema_20260423.sql`
2. `sql/pgsql/体检报告AI解析缓存标记_20260429.sql`
3. `sql/健康系统_体检报告表_20260423.sql`
4. `sql/健康系统_体检报告AI解析缓存标记_20260429.sql`
5. `healthtrail-infrastructure/src/main/resources/h2sql/health_test_schema.sql`

## 5. 验证重点

调整后重点验证：

1. 首次成功解析后，主表 `ai_parse_cached` 被写为 `1`
2. 再次点击“解析报告”时，App 仍然先看到 `正在重新解析`
3. 后台任务完成后，状态会回到 `已解析`
4. 再次解析过程中不会重复调用大模型
5. 旧的分析摘要、结果解读、结构化指标不会被清空或覆盖成排队占位文案
