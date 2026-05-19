# APP端药品与用药提醒接口对接文档

## 1. 说明

本文档用于给 App 前端对接药品与用药提醒模块。

当前版本的重要前提是：

1. 药品采用“双层药品库”
2. 后台可以下发系统药品
3. App 用户也可以维护自己的个人药品
4. 用药计划可以选择“系统药品”“自己录入过的药品”，也可以填写自定义药名

当前覆盖接口：

1. 药品列表
2. 药品详情
3. 新增药品
4. 修改药品
5. 删除药品
6. 用药计划列表
7. 用药计划详情
8. 新增用药计划
9. 修改用药计划
10. 删除用药计划
11. 今日提醒列表
12. 标记已服药
13. 标记已跳过


## 2. 认证方式

以下所有接口都属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 药品接口

### 3.1 药品列表

#### 接口地址

`GET /app/drugs`

#### 查询参数

1. `pageNum`：页码，默认 `1`
2. `pageSize`：每页数量，默认 `10`
3. `keyword`：关键字，支持药品名称、通用名、商品名模糊搜索
4. `drugType`：药品类型，例如 `OTC`、`RX`
5. `status`：状态，建议传 `1`

#### 请求示例

`GET /app/drugs?pageNum=1&pageSize=10&keyword=阿司匹林&status=1`

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "total": 1,
    "rows": [
      {
        "drugId": 1,
        "ownerUserId": 0,
        "systemDrug": true,
        "drugName": "阿司匹林肠溶片",
        "genericName": "阿司匹林",
        "brandName": "拜阿司匹灵",
        "dosageForm": "肠溶片",
        "specification": "100mg*30片",
        "indication": "抗血小板聚集，预防心脑血管事件",
        "usageInstruction": "成人通常一次1片，一日1次，遵医嘱服用",
        "adverseReaction": "胃部不适、出血风险增加",
        "contraindication": "活动性消化道出血、阿司匹林过敏者禁用",
        "manufacturer": "拜耳医药保健有限公司",
        "drugType": "RX",
        "status": 1
      }
    ]
  }
}
```

#### 说明

当前列表返回两类药品：

1. 系统下发药品，`ownerUserId = 0`，`systemDrug = true`
2. 当前用户自己的药品，`ownerUserId = 当前用户ID`，`systemDrug = false`

其中系统药品只允许查看和选用，不允许 App 端直接编辑删除。

### 3.2 药品详情

#### 接口地址

`GET /app/drugs/{drugId}`

#### 请求示例

`GET /app/drugs/1`

#### 说明

可以查看系统药品详情，也可以查看当前用户自己的药品详情。

### 3.3 新增药品

#### 接口地址

`POST /app/drugs`

#### 请求体示例

```json
{
  "drugName": "阿托伐他汀钙片",
  "genericName": "阿托伐他汀钙",
  "brandName": "立普妥",
  "dosageForm": "片剂",
  "specification": "20mg*7片",
  "indication": "用于高胆固醇血症",
  "usageInstruction": "每日一次，遵医嘱服用",
  "adverseReaction": "肝功能异常、肌肉疼痛",
  "contraindication": "活动性肝病患者禁用",
  "manufacturer": "辉瑞制药有限公司",
  "drugType": "RX",
  "status": 1,
  "remark": "用户自己录入"
}
```

### 3.4 修改药品

#### 接口地址

`PUT /app/drugs/{drugId}`

#### 请求体

与新增药品一致。

#### 说明

只允许修改当前用户自己录入的药品；如果传入的是系统药品ID，接口会返回“对象不存在/不可操作”。

### 3.5 删除药品

#### 接口地址

`DELETE /app/drugs/{drugId}`

#### 说明

只允许删除当前用户自己录入的药品；系统药品不允许 App 端删除。


## 4. 用药计划接口

### 4.1 用药计划列表

#### 接口地址

`GET /app/medication/plans`

#### 查询参数

1. `memberId`：家庭成员ID，可选
2. `status`：状态，可选

#### 返回说明

返回数组，每个对象主要字段包括：

1. `planId`
2. `memberId`
3. `drugId`
4. `customDrugName`
5. `drugName`
6. `startDate`
7. `endDate`
8. `reminderTimes`
9. `mealTiming`
10. `doseAmount`
11. `doseUnit`
12. `frequencyType`
13. `remark`
14. `status`

### 4.2 用药计划详情

#### 接口地址

`GET /app/medication/plans/{planId}`

### 4.3 新增用药计划

#### 接口地址

`POST /app/medication/plans`

#### 请求体示例

```json
{
  "memberId": 1,
  "drugId": 1,
  "customDrugName": "",
  "startDate": "2026-04-23",
  "endDate": "2026-04-30",
  "reminderTimes": ["08:00", "20:00"],
  "mealTiming": "AFTER_MEAL",
  "doseAmount": 1,
  "doseUnit": "片",
  "frequencyType": "DAILY",
  "remark": "晚饭后服用",
  "status": 1
}
```

#### 字段说明

1. `memberId`：家庭成员ID，必填
2. `drugId`：系统药品或当前用户自己药品库中的药品ID，可选
3. `customDrugName`：自定义药名，可选；如果没有 `drugId`，则必须填写
4. `startDate`：开始日期，必填，格式 `yyyy-MM-dd`
5. `endDate`：结束日期，必填，格式 `yyyy-MM-dd`
6. `reminderTimes`：提醒时间数组，必填，格式 `HH:mm`
7. `mealTiming`：服药时机，可选
8. `doseAmount`：剂量，可选
9. `doseUnit`：剂量单位，可选
10. `frequencyType`：频率类型，可选，一期建议传 `DAILY`
11. `remark`：备注，可选
12. `status`：状态，可选，建议传 `1`

### 4.4 修改用药计划

#### 接口地址

`PUT /app/medication/plans/{planId}`

#### 请求体

与新增一致。

### 4.5 删除用药计划

#### 接口地址

`DELETE /app/medication/plans/{planId}`


## 5. 提醒接口

### 5.1 今日提醒列表

#### 接口地址

`GET /app/medication/reminders/today`

#### 查询参数

1. `memberId`：家庭成员ID，可选
2. `reminderStatus`：提醒状态，可选

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": [
    {
      "reminderId": 1,
      "planId": 1,
      "memberId": 1,
      "reminderDate": "2026-04-23 00:00:00",
      "scheduledTime": "2026-04-23 08:00:00",
      "drugName": "阿司匹林肠溶片",
      "doseAmount": 1,
      "doseUnit": "片",
      "mealTiming": "AFTER_MEAL",
      "reminderStatus": 0,
      "notifyStatus": 1,
      "notifyTime": "2026-04-23 07:59:30",
      "feedbackTime": null,
      "skipReason": null
    }
  ]
}
```

#### 提醒状态说明

1. `0`：待处理
2. `1`：已服药
3. `2`：已跳过
4. `3`：已过期

#### 发送状态说明

1. `0`：待发送
2. `1`：发送成功
3. `2`：发送失败

### 5.2 标记已服药

#### 接口地址

`POST /app/medication/reminders/{reminderId}/take`

#### 请求体

无

### 5.3 标记已跳过

#### 接口地址

`POST /app/medication/reminders/{reminderId}/skip`

#### 请求体示例

```json
{
  "skipReason": "本次医生要求暂停服用"
}
```

### 5.4 撤销已服药

#### 接口地址

`POST /app/medication/reminders/{reminderId}/untake`

#### 请求体

无

#### 说明

1. 仅允许对当前状态为 `已服药` 的提醒执行撤销
2. 撤销成功后，提醒状态会恢复为 `待处理`
3. 如果该提醒在标记已服药时触发了个人药柜自动扣减库存，则会同步回补库存


## 6. 常见错误码

| 错误码 | 含义 |
| --- | --- |
| `12006` | 药品信息不能为空，请选择药品或填写自定义药名 |
| `12007` | 用药计划的开始日期不能晚于结束日期 |
| `12008` | 提醒时间不能为空 |
| `12009` | 提醒时间格式不正确，请使用HH:mm格式 |
| `12010` | 当前提醒状态不允许执行该操作 |
| `12011` | 药品名称已存在 |


## 7. 前端对接建议

1. 药品页建议按 `systemDrug` 字段区分“系统药品”和“我的药品”，系统药品只展示查看与选用能力
2. 创建计划时，优先允许用户从系统药品和自己的药品中选择；如果都没有再允许输入自定义药名
3. 今日提醒页应按 `scheduledTime` 正序展示
4. 已服药、撤销已服药和已跳过操作成功后，建议重新拉取今日提醒列表，避免前端本地状态不一致
