# APP端个人药柜库存接口对接文档

## 1. 说明

本次在原有 `APP端药品与用药提醒接口` 基础上，新增“个人药柜库存”相关接口。

核心能力：

1. 手工新增个人药品时初始化库存
2. 系统药品引入个人药柜
3. 手工补库存
4. 修改库存单位与预警值
5. 标记已服药后自动扣减库存
6. 低库存消息提醒
7. 按“批号 + 效期”展示库存明细


## 2. 字段变更

以下接口返回对象 `DrugDTO` 新增字段：

1. `sourceDrugId`：来源系统药品ID
2. `importedFromSystem`：是否由系统药品引入
3. `stockQuantity`：当前库存数量，实时汇总自批次库存
4. `stockUnit`：库存单位
5. `stockAlertThreshold`：库存预警值
6. `stockTrackingEnabled`：是否启用库存跟踪
7. `stockBelowAlert`：当前是否已进入预警区间
8. `stockBatches`：按批号效期返回的库存明细


## 3. 引入系统药品

### 3.1 请求

`POST /app/drugs/{drugId}/import`

`drugId` 为系统药品ID。

请求体：

```json
{
  "targetDrugName": "阿司匹林(家用)",
  "stockQuantity": 30,
  "batchNo": null,
  "expireDate": "2026-12-31",
  "stockUnit": "片",
  "stockAlertThreshold": 10,
  "remark": "家中常备"
}
```

### 3.2 响应

返回引入后的个人药品详情：

```json
{
  "code": 0,
  "data": {
    "drugId": 101,
    "ownerUserId": 10001,
    "systemDrug": false,
    "sourceDrugId": 1,
    "importedFromSystem": true,
    "drugName": "阿司匹林(家用)",
    "stockQuantity": 30,
    "stockUnit": "片",
    "stockAlertThreshold": 10,
    "stockTrackingEnabled": true,
    "stockBelowAlert": false,
    "stockBatches": [
      {
        "batchId": 1001,
        "batchNo": null,
        "expireDate": "2026-12-31",
        "stockQuantity": 30
      }
    ]
  }
}
```


## 4. 手工补库存

### 4.1 请求

`POST /app/drugs/{drugId}/stock/increase`

请求体：

```json
{
  "increaseQuantity": 12,
  "batchNo": "BATCH-202604",
  "expireDate": "2026-10-31",
  "stockUnit": "片",
  "operationRemark": "线下购入补货"
}
```

说明：

1. `stockUnit` 只有在药品第一次启用库存跟踪时才需要传。
2. 如果药品已有库存单位，再传不同单位会被拦截。
3. `expireDate` 为必填，`batchNo` 可为空。
4. 相同药品下相同 `batchNo + expireDate` 会自动合并到同一批次库存。

### 4.2 响应

返回更新后的个人药品详情。


## 5. 修改库存配置

### 5.1 请求

`PUT /app/drugs/{drugId}/stock/config`

请求体：

```json
{
  "stockUnit": "片",
  "stockAlertThreshold": 6
}
```

说明：

1. `stockUnit` 为空时表示沿用当前单位。
2. `stockAlertThreshold` 传 `null` 表示关闭低库存提醒。

### 5.2 响应

返回更新后的个人药品详情。


## 6. 新增个人药品

原接口不变：

`POST /app/drugs`

本次支持在新增时直接初始化库存相关字段：

```json
{
  "drugName": "维生素C",
  "drugType": "OTC",
  "stockQuantity": 20,
  "batchNo": "VC-001",
  "expireDate": "2027-03-31",
  "stockUnit": "片",
  "stockAlertThreshold": 5,
  "remark": "日常备用"
}
```


## 7. 服药后自动扣减说明

原接口不变：

`POST /app/medication/reminders/{reminderId}/take`

当前自动扣减规则：

1. 提醒对应计划必须绑定个人药品
2. 该个人药品必须启用库存跟踪
3. 计划 `doseUnit` 必须与药品 `stockUnit` 一致
4. 扣减数量按提醒的 `doseAmount` 计算
5. 扣减顺序按最早效期优先
6. 若当前库存不足，则库存按 `0` 归零，但不阻断“已服药”反馈

补充说明：

1. 若后续调用 `POST /app/medication/reminders/{reminderId}/untake` 撤销已服药，系统会把本次自动扣减的库存同步回补
2. 当前回补库存会回到系统维护的补回库存桶，用于保证药柜总库存与提醒反馈状态重新一致


## 8. 低库存提醒说明

当库存满足以下条件时，会生成 `LOW_STOCK_ALERT` 场景的消息中心记录：

1. 已配置 `stockAlertThreshold`
2. 当前库存 `stockQuantity <= stockAlertThreshold`
3. 当前预警周期尚未提醒过

说明：

1. 这里的 `stockQuantity` 不再存放在药品表，而是由批次库存实时汇总得到。
2. 前端展示库存时，可直接使用返回的 `stockBatches` 数组按顺序渲染。

消息内容示例：

1. 标题：`阿司匹林(家用) 库存不足`
2. 正文：`当前库存 3片，已达到预警值 5片，建议尽快补充。`


## 9. 前端接入建议

1. 系统药品列表页增加“引入我的药柜”按钮。
2. 个人药品详情页增加按批号效期展示的库存卡片与“补库存”操作。
3. 引入系统药品和补库存弹窗都要支持录入 `batchNo` 与 `expireDate`。
4. 创建用药计划时，如果选择了启用库存的个人药品，`doseUnit` 必须和 `stockUnit` 保持一致。
5. 消息中心新增 `LOW_STOCK_ALERT` 场景展示与跳转映射。
