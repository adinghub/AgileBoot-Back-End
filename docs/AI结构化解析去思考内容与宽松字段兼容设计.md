# AI结构化解析去思考内容与宽松字段兼容设计

## 1. 背景

最近联调里又出现了一类“模型看起来回答了，但后端最终仍然落成 0 条指标”的问题，表现主要有两种：

1. 外部模型日志里能看到 `<think>...</think>` 思考内容，真正答案被淹没；
2. 模型返回了 JSON，但字段名没有完全按约定写成 `itemName/resultValue/resultUnit/referenceText`，导致后端在最后一步把整批指标丢掉。

从业务视角看，这两类问题都会被用户感知成同一件事：

1. 明明模型像是识别到了报告；
2. 但系统最后告诉我“暂未提取到结构化指标”。


## 2. 本次目标

本次只做两件事，并且都尽量收敛在后端内部完成：

1. 不再把模型思考内容透出到业务层日志和常规返回链路；
2. 提升结构化 JSON 的消费容错，兼容常见字段别名和轻量嵌套包装。


## 3. 设计方案

### 3.0 对齐智谱 GLM-4.6V-Flash 官方多模态入参格式

在对照智谱官方文档时，我们发现当前后端有一个非常具体的兼容性偏差：

1. 现有实现把图片统一包装成 `data:image/jpeg;base64,...`
2. 但智谱 `GLM-4.6V-Flash` 官方 Java 示例中的 `image_url.url` 传的是纯 Base64 内容

这两种写法在部分 OpenAI 兼容网关里都能工作，但不能假设智谱网关一定同时接受。

考虑到线上失败表现为：

1. 请求很快返回；
2. 没有进入正常的 `responsePreview` 日志；
3. 更像是网关参数校验阶段直接拒绝；

本次将图片字段改成“按供应商兼容”的策略：

1. 默认仍可保留原始 data URL
2. 但当识别到当前走的是智谱 `open.bigmodel.cn` 链路时，自动剥掉 `data:` 前缀，只发送纯 Base64

这样做的好处是：

1. 最大程度贴近智谱官方示例；
2. 不会破坏其它供应商可能依赖 data URL 的既有行为；
3. 兼容逻辑集中在外部模型客户端，调用方无需感知差异。

### 3.1 在外部模型客户端出口统一清理 `<think>`

改动位置：

1. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/llm/HealthReportExternalLlmClient.java`

处理方式：

1. 先按原有逻辑从响应体中抽出 `message.content`；
2. 如果内容中存在 `<think>...</think>`，则在客户端出口统一去掉；
3. 日志打印 `responsePreview` 时使用去思考后的内容；
4. 返回给上层业务代码的也是清洗后的可见正文。

这样做的直接收益是：

1. 排查日志时不再先看到长段思考过程；
2. 结构化解析器、总结生成器等调用方默认拿到的是“更接近最终答案”的文本；
3. 不需要每个调用点各自复制一份 `<think>` 清洗逻辑。

注意事项：

1. 这里只清理显式包在 `<think>` 标签里的内容；
2. 如果模型除了思考内容之外没有给出可见答案，不会强行伪造空字符串，而是保留原有失败感知。


### 3.1.1 按配置请求模型关闭思考输出

仅仅在后端“收到后再删掉思考内容”，并不能解决 token 被占用的问题。

因此本次额外增加了一个可选配置：

1. `health.report.ai.external-llm.disable-thinking-output=true`

开启后，请求体会额外透传：

```json
{
  "thinking": {
    "type": "disabled"
  }
}
```

设计原则：

1. 默认关闭，不影响现有兼容网关；
2. 只有在确认当前供应商支持时才打开；
3. 打开后目标是从源头减少 reasoning 对输出 token 的占用，而不是事后再清洗。


### 3.2 对结构化指标字段做“宽松消费”

改动位置：

1. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/report/parser/HealthReportAiStructuredParser.java`

问题根因：

1. 提示词虽然要求固定 schema；
2. 但兼容网关下的不同模型仍可能输出轻微变体字段名；
3. 旧代码只认完全一致的字段名，导致模型已经识别出项目，也会在 `toParsedItem(...)` 里被全部过滤掉。

本次兼容的重点别名：

1. `itemName` 兼容 `name/item/indicatorName/projectName/testName`
2. `resultValue` 兼容 `result/value/testResult/itemValue`
3. `resultUnit` 兼容 `unit/valueUnit/itemUnit`
4. `referenceText` 兼容 `referenceRange/reference/referenceValue/range/normalRange`
5. `itemInterpretation` 兼容 `interpretation/comment/advice/summary`
6. `sort` 兼容 `order/index`

同时新增对常见数组包装层的兼容：

1. 顶层 `items`
2. 顶层 `reportItems/results/indicators/list`
3. `data.items`
4. `data.reportItems`
5. `data.results`


### 3.3 增补更可定位的问题日志

旧日志只能区分：

1. 没拿到 JSON
2. `items` 为空

但无法快速看出另一种关键场景：

1. `items` 不为空；
2. 只是每个 item 的字段没有命中我们可消费的 schema。

因此本次新增一条更细的警告日志：

1. 记录原始项目数量；
2. 记录首个原始 item 的缩略内容；
3. 让开发人员能第一时间判断是“模型没识别出来”还是“字段映射没接住”。


## 4. 预期效果

本次改动完成后，预期能稳定改善三类现象：

1. 调用日志里不再直接刷出 `<think>` 思考块；
2. 模型返回轻微 schema 变体时，不会再被整体误判成 0 条指标；
3. 再次遇到异常样本时，日志能更快指出问题卡在“响应清洗”还是“字段映射”阶段。


## 5. 验证建议

建议至少验证下面几种返回体：

1. `<think>...</think>{标准 JSON}`
2. `<think>...</think>```json ... ````
3. `items` 正常，但 item 字段名使用 `name/value/unit/referenceRange`
4. `data.items` 这类带一层包装的 JSON

如果以上场景都能正确落库，说明这次“去思考内容 + 宽松字段兼容”链路已经闭环。
