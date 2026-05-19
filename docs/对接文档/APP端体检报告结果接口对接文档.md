# APP端体检报告结果接口对接文档

## 1. 说明

本文档用于给 App 前端对接体检报告“结构化结果与分析摘要”模块。

当前版本已支持：

1. 查询报告指标列表
2. 覆盖保存报告指标
3. 查询报告分析摘要


## 2. 认证方式

以下所有接口都属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口列表

### 3.1 查询报告指标列表

#### 接口地址

`GET /app/reports/{reportId}/items`

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": [
    {
      "itemId": 1,
      "reportId": 10,
      "itemCode": "GLU",
      "itemName": "空腹血糖",
      "resultValue": "6.2",
      "resultUnit": "mmol/L",
      "referenceMin": 3.9,
      "referenceMax": 6.1,
      "referenceText": "3.9-6.1",
      "abnormalFlag": 3,
      "abnormalFlagName": "偏高",
      "sort": 1,
      "remark": null
    }
  ]
}
```

### 3.2 覆盖保存报告指标

#### 接口地址

`PUT /app/reports/{reportId}/items`

#### 请求体

```json
{
  "items": [
    {
      "itemCode": "GLU",
      "itemName": "空腹血糖",
      "resultValue": "6.2",
      "resultUnit": "mmol/L",
      "referenceMin": 3.9,
      "referenceMax": 6.1,
      "referenceText": "3.9-6.1",
      "sort": 1,
      "remark": "晨起抽血"
    },
    {
      "itemName": "尿蛋白",
      "resultValue": "阴性",
      "referenceText": "阴性",
      "sort": 2
    }
  ]
}
```

#### 说明

1. 本接口是整份覆盖保存，不是追加保存
2. 如果想清空某份报告下的全部指标，请传 `{"items":[]}`
3. 保存后后端会自动重算异常标记和报告摘要

### 3.3 查询报告分析摘要

#### 接口地址

`GET /app/reports/{reportId}/analysis`

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "reportId": 10,
    "parseStatus": 2,
    "totalItemCount": 8,
    "normalItemCount": 5,
    "lowItemCount": 1,
    "highItemCount": 1,
    "abnormalItemCount": 2,
    "unknownItemCount": 1,
    "summary": "共录入8项指标，其中异常2项：空腹血糖偏高、白细胞计数偏低；另有1项待人工判断。",
    "abnormalItems": [
      {
        "itemId": 1,
        "reportId": 10,
        "itemCode": "GLU",
        "itemName": "空腹血糖",
        "resultValue": "6.2",
        "resultUnit": "mmol/L",
        "referenceMin": 3.9,
        "referenceMax": 6.1,
        "referenceText": "3.9-6.1",
        "abnormalFlag": 3,
        "abnormalFlagName": "偏高",
        "sort": 1,
        "remark": null
      }
    ]
  }
}
```


## 4. 状态说明

### 4.1 异常标记

1. `0`：待判断
2. `1`：正常
3. `2`：偏低
4. `3`：偏高
5. `4`：异常


## 5. 前端对接建议

1. 报告详情页先请求报告基础信息，再请求指标列表和分析摘要
2. 编辑页面建议以前端表格形式一次性维护整份指标列表
3. 对于只有原始参考范围文本的场景，可以先只传 `referenceText`，后端会尽量做轻量判断
4. `summary` 只用于摘要展示，不应直接当作医学诊断结论
