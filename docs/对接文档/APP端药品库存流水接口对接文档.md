# APP端药品库存流水接口对接文档

## 1. 目标

药品详情页需要直接查看库存流水，帮助用户和开发联调时快速回答下面两个问题：

1. 当前库存为什么变了
2. 这次变化是补库存、提醒服药扣减，还是临时用药扣减

本次新增一个只读接口，返回指定药品的库存流水列表，供 App 药品详情页展示最近流水和“查看全部”页面。

## 2. 接口信息

- 方法：`GET`
- 路径：`/app/drugs/{drugId}/stock/logs`
- 鉴权：需要 App 登录态

## 3. 业务规则

1. 仅允许查询当前登录用户自己拥有的个人药品库存流水。
2. 系统药品本身不允许直接维护个人库存，因此不开放库存流水查询。
3. 返回顺序按 `createTime DESC, logId DESC` 排序，确保最近发生的流水排在最前面。
4. 当前先返回完整列表，不做分页，便于 App 详情页首版快速接入。

## 4. 返回字段

```json
[
  {
    "logId": 101,
    "changeType": "TEMPORARY_USE_DEDUCT",
    "beforeQuantity": 12,
    "changeQuantity": -1,
    "afterQuantity": 11,
    "stockUnitSnapshot": "片",
    "alertThresholdSnapshot": 5,
    "relatedPlanId": null,
    "relatedReminderId": null,
    "relatedTempMedicationId": 23,
    "operationRemark": "临时用药：感冒头痛，成员=本人，用量=1片",
    "createTime": "2026-04-27 16:35:12"
  }
]
```

## 5. changeType 枚举说明

- `CREATE_INIT`：新增个人药品时初始化库存
- `IMPORT_INIT`：系统药品引入个人药柜时初始化库存
- `MANUAL_INCREASE`：手工补库存
- `ALERT_THRESHOLD_UPDATE`：库存配置调整
- `REMINDER_TAKE_DEDUCT`：提醒服药自动扣减库存
- `REMINDER_TAKE_RESTORE`：撤销服药后回补库存
- `TEMPORARY_USE_DEDUCT`：临时用药扣减库存

## 6. App 对接建议

1. 药品详情页默认展示最近几条库存流水。
2. 点击“查看全部”进入独立页面，展示全部库存流水。
3. 对 `changeQuantity` 建议按正负号渲染，便于一眼区分“增加”还是“扣减”。
4. 如果 `operationRemark` 不为空，建议直接展示，能显著降低排障成本。
