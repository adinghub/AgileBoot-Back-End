# APP端个人药柜简化录入接口对接文档

## 1. 文档说明

本文档用于 App 端对接“个人药柜简化录入”能力。

本次对接目标是把个人药品录入从“完整药品档案录入”收敛为“轻量药柜录入”：

1. 用户新增个人药品时，主流程只要求填写药品名称和库存单位。
2. 初始库存、预警值、备注、图片为可选能力。
3. 通用名、规格、厂家、剂型等字段保留，但不作为默认必填项。
4. 库存单位由后台统一维护，App 端通过接口选择，不建议自由输入。
5. 药品名称在当前登录用户自己的个人药柜内保持唯一。


## 2. 认证方式

以下接口均为 App 登录后接口，请按 App 端现有 token 机制传递认证信息。


## 3. 药品单位列表

### 3.1 接口地址

`GET /app/drug-units`

### 3.2 查询参数

1. `keyword`：可选，支持按单位名称、编码、别名搜索

### 3.3 响应示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": [
    {
      "unitId": 1,
      "unitCode": "tablet",
      "unitName": "片",
      "unitAlias": "片剂",
      "precisionScale": 0,
      "sort": 1,
      "status": 1,
      "iconAttachmentId": null,
      "iconUrl": null,
      "remark": "常见片剂单位"
    }
  ]
}
```

### 3.4 对接说明

1. 新增个人药品、引入系统药品、首次补库存、库存设置都应复用该接口。
2. 前端推荐保存并提交 `stockUnitId`，同时提交 `stockUnit` 作为展示快照。
3. 如果历史版本只传 `stockUnit` 文本，后端仍兼容，但新版本建议优先传 `stockUnitId`。
4. 单位列表只返回启用状态单位，停用单位不应继续作为新增选择项。


## 4. 新增个人药品

### 4.1 接口地址

`POST /app/drugs`

### 4.2 最简请求体

```json
{
  "drugName": "维生素C",
  "stockUnitId": 1,
  "stockUnit": "片",
  "status": 1
}
```

### 4.3 带初始库存请求体

```json
{
  "drugName": "维生素C",
  "stockUnitId": 1,
  "stockUnit": "片",
  "stockQuantity": 20,
  "batchNo": "VC-202604",
  "expireDate": "2027-03-31",
  "stockAlertThreshold": 5,
  "remark": "家庭常备",
  "status": 1
}
```

### 4.4 可选扩展字段

以下字段保留给“补充信息”区域使用，不建议在默认录入主流程中强制填写：

1. `genericName`：通用名
2. `brandName`：商品名
3. `dosageForm`：剂型
4. `specification`：规格
5. `manufacturer`：厂家
6. `indication`：适应症
7. `usageInstruction`：用法用量
8. `adverseReaction`：不良反应
9. `contraindication`：禁忌
10. `imageAttachmentId`：药品图片附件 ID

### 4.5 校验规则

1. `drugName` 必填，最大 100 字符。
2. 当前用户个人药柜内 `drugName` 唯一。
3. 如果传了 `stockUnitId`，后端会以单位表中的 `unitName` 回填 `stockUnit`。
4. 如果填写了初始库存 `stockQuantity`，需要同时提供 `expireDate`。
5. `stockAlertThreshold` 可以为空，表示不启用低库存预警。
6. `imageAttachmentId` 可以为空；为空时响应对象会返回默认药品图地址。

### 4.6 重名错误提示

当前错误码：

`Business.HEALTH_DRUG_NAME_IS_NOT_UNIQUE`

建议 App 文案：

`药品名称已存在，请在名称中补充规格或用途区分，例如：阿莫西林(0.25g)、阿莫西林-儿童备用`


## 5. 引入系统药品到个人药柜

### 5.1 接口地址

`POST /app/drugs/{drugId}/import`

`drugId` 为系统药品 ID。

### 5.2 请求体示例

```json
{
  "targetDrugName": "阿司匹林(家用)",
  "stockQuantity": 30,
  "batchNo": "ASP-202604",
  "expireDate": "2026-12-31",
  "stockUnitId": 1,
  "stockUnit": "片",
  "stockAlertThreshold": 10,
  "remark": "家中常备",
  "imageAttachmentId": null
}
```

### 5.3 响应说明

接口返回引入后的个人药品详情 `DrugDTO`。引入后的药品：

1. `systemDrug` 为 `false`
2. `sourceDrugId` 为来源系统药品 ID
3. `importedFromSystem` 为 `true`
4. 可以继续维护库存、预警值、批次、用药计划


## 6. 药品详情返回字段

`GET /app/drugs` 和 `GET /app/drugs/{drugId}` 返回的 `DrugDTO` 包含以下简化录入相关字段：

1. `drugId`：药品 ID
2. `ownerUserId`：归属用户 ID
3. `systemDrug`：是否系统药品
4. `sourceDrugId`：来源系统药品 ID
5. `importedFromSystem`：是否从系统药品引入
6. `drugName`：药品名称
7. `stockUnitId`：库存单位 ID
8. `stockUnit`：库存单位名称快照
9. `stockQuantity`：当前库存数量，实时汇总自批次库存
10. `stockAlertThreshold`：低库存预警值
11. `stockTrackingEnabled`：是否启用库存跟踪
12. `stockBelowAlert`：是否低于预警值
13. `stockBatches`：批次库存明细
14. `imageAttachmentId`：药品图片附件 ID
15. `imageUrl`：药品图片地址，未上传时返回默认图


## 7. App 端交互建议

### 7.1 新增个人药品

默认主流程建议只展示：

1. 药品名称
2. 库存单位
3. 初始库存
4. 预警值
5. 药品图片
6. 备注

通用名、规格、厂家、剂型、适应症等字段建议放入“补充信息”折叠区。

### 7.2 库存单位选择

1. 页面打开时可先拉取常用单位列表。
2. 搜索时调用 `GET /app/drug-units?keyword=片`。
3. 提交时优先传 `stockUnitId`，并带上 `stockUnit` 名称快照。
4. 不建议继续使用自由文本输入。

### 7.3 药品图片

1. 图片上传非必填。
2. 新增药品时可以传 `imageAttachmentId`。
3. 列表和详情页优先展示 `imageUrl`。
4. 未上传图片时，后端会返回默认药品图。

