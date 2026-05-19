# APP端体检报告趋势与解析_AI接口对接文档

## 1. 报告趋势对比

### 接口

`GET /app/reports/{reportId}/trends`

### 返回结构

```json
{
  "reportId": 1001,
  "memberId": 12,
  "memberName": "妈妈",
  "reportType": "血糖",
  "comparedReportCount": 3,
  "items": [
    {
      "itemCode": "GLU",
      "itemName": "空腹血糖",
      "resultUnit": "mmol/L",
      "currentResultValue": "6.2",
      "previousResultValue": "5.8",
      "changeDirection": "UP",
      "changeSummary": "相较上次结果 5.8，本次结果 6.2 呈上升趋势。",
      "abnormalFlag": 3,
      "abnormalFlagName": "偏高",
      "trendPoints": [
        {
          "reportId": 998,
          "reportDate": "2026-01-10 00:00:00",
          "resultValue": "5.4",
          "resultUnit": "mmol/L"
        }
      ]
    }
  ]
}
```

## 2. 手动重新解析报告

### 接口

`POST /app/reports/{reportId}/parse`

### 说明

1. 该接口用于手动重跑报告解析流程，不再表示 OCR 识别
2. 文本型 PDF / 文本文件走服务端文本抽取和结构化解析
3. 图片文件仅在后端已配置 AI 视觉能力时自动解析，否则需要人工补录
4. 接口直接返回最新 `HealthReportDTO`，便于前端统一刷新详情页

## 3. AI 智能总结

### 接口

`POST /app/reports/{reportId}/ai-summary`

### 说明

1. 若后端已配置外部大模型，则优先返回外部模型增强后的智能总结
2. 若外部大模型未配置或调用失败，则自动回退到本地规则总结
3. App 无需关心具体供应商，只需根据 `summary + disclaimer` 展示结果来源说明

### 返回字段

1. `reportId`
2. `aiSummaryStatus`
3. `summary`
4. `disclaimer`
5. `processedTime`

## 4. 报告详情新增字段

报告详情接口：

`GET /app/reports/{reportId}`

新增字段：

1. `ocrStatus`
2. `ocrTextSnapshot`
3. `ocrTime`
4. `aiSummaryStatus`
5. `aiSummaryContent`
6. `aiSummaryTime`

### 建议前端展示策略

1. `ocrStatus`、`ocrTextSnapshot`、`ocrTime` 当前仍是兼容字段，前端不要再宣称为 OCR 识别结果
2. `aiSummaryContent` 为空时，展示“暂未生成 AI 智能总结”
3. `disclaimer` 建议在 AI 总结卡片底部弱提示展示，明确当前结果仅用于健康管理提醒
