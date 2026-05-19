# 体检报告 AI 智能总结异常症状提示增强设计

## 1. 需求背景

当前体检报告 AI 智能总结已经能够覆盖：

1. 报告总体情况
2. 重点异常指标
3. 趋势变化
4. 后续跟进建议

但用户继续提出新的明确要求：

1. `ai 智能总结 提示词增加 异常指标 可能导致什么症状的提示词`

这说明现有总结虽然能告诉用户“哪里异常”，但还不够回答：

1. 这类异常通常可能带来什么不适
2. 用户在日常观察时应该留意哪些常见表现

## 2. 设计目标

本次只增强 AI 智能总结的提示词，不改接口结构：

1. 让 AI 总结在合适时补充异常指标可能对应的常见症状提示
2. 保持“健康管理提示”定位，不把症状写成已经发生的事实
3. 保持克制语气，避免把总结写成诊断结论

## 3. 方案设计

### 3.1 同时增强 system prompt 与 user prompt

为了避免只改一层后被另一层覆盖，本次同时调整：

1. `HealthReportApplicationService` 里的默认 `systemPrompt`
2. `buildExternalAiSummaryUserPrompt(...)` 的输出要求
3. `application-basic-dev/pre/test/prod.yml` 中的 `system-prompt`

这样可以保证：

1. 未自定义环境时，代码默认值生效
2. 已使用 YAML 配置覆盖时，配置里的提示词也同步具备症状提示要求

### 3.2 增加“症状提示”但强约束语气

新增要求不是简单让模型“多写症状”，而是加两层约束：

1. 对明确异常的重点指标，在医学上能够合理推断时，可补充 1 到 2 个常见可能症状或不适表现
2. 必须使用 `可能出现 / 如伴有 / 若近期存在` 这类风险提示语气

这样做的原因是：

1. 用户需要更易理解的健康观察提示
2. 症状类文本比结果值更容易被模型说得过头
3. 必须防止模型把“常见可能症状”写成“用户已经出现的事实”

### 3.3 明确禁止症状臆断

在 user prompt 中额外写死两条限制：

1. 不能把症状写成已经发生的事实
2. 如果无法从当前结构化数据稳定推断症状，就不要强行补症状

这一步是为了防止出现以下偏差：

1. 明明只有指标异常，却被模型写成“患者已头晕、乏力、心悸”
2. 某些关联性较弱的异常被模型过度展开成大段症状列表

## 4. 影响范围

代码影响：

1. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/HealthReportApplicationService.java`

配置影响：

1. `healthtrail-infrastructure/src/main/resources/application-basic-dev.yml`
2. `healthtrail-infrastructure/src/main/resources/application-basic-pre.yml`
3. `healthtrail-infrastructure/src/main/resources/application-basic-test.yml`
4. `healthtrail-infrastructure/src/main/resources/application-basic-prod.yml`

文档新增：

1. `docs/体检报告AI智能总结异常症状提示增强设计.md`

## 5. 验证重点

调整后重点验证：

1. AI 总结会在重点异常后补充“可能出现的常见症状/不适表现”提示
2. 症状表述使用 `可能 / 如伴有 / 若近期存在` 等风险提示语气
3. 不会把症状写成已经发生的事实
4. 无法稳定推断症状时，不会强行编造症状
5. 原有总体情况、趋势变化、后续建议结构仍然保留
