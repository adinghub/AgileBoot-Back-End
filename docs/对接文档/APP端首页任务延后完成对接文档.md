# APP端首页任务延后完成对接文档

## 1. 说明

本文档用于给 App 前端对接首页任务操作接口。

当前版本支持：

1. 任务中心列表
2. 任务详情
3. 延后任务
4. 完成本轮报告建议任务
5. 忽略任务
6. 恢复任务


## 2. 认证方式

以下接口属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口列表

### 3.1 任务中心列表

#### 接口地址

`GET /app/follow-up/tasks`

#### 查询参数

1. `taskType`：任务类型，可选
2. `taskStatus`：任务状态，可选

#### 说明

1. 当前主要返回“已经产生过操作记录”的任务
2. 适合前端做任务中心页、历史任务页、恢复任务页
3. 单条任务中新增 `navigation`，可直接用于跳转原始业务页

### 3.2 任务详情

#### 接口地址

`GET /app/follow-up/tasks/{taskId}`

#### 说明

1. 适合任务中心详情页使用
2. 一次返回任务信息、按钮可操作能力、操作时间线
3. 详细返回结构请参考《APP端首页任务详情对接文档》

### 3.3 延后任务

#### 接口地址

`POST /app/follow-up/tasks/delay`

#### 请求体

```json
{
  "taskType": "REMINDER",
  "sourceId": 101,
  "delayOption": "TONIGHT",
  "remark": "下班后再处理"
}
```

#### 说明

1. 当前支持 `REMINDER` 和 `REPORT_ADVICE`
2. `delayedUntil` 和 `delayOption` 至少传一个
3. 当前快捷选项支持：`AFTER_30_MINUTES`、`TONIGHT`、`TOMORROW_MORNING`
4. 如果同时传了 `delayedUntil` 和 `delayOption`，后端优先使用 `delayedUntil`
5. 延后操作不会改原始提醒时间或报告日期，只影响首页任务流再次出现时间

### 3.4 完成本轮任务

#### 接口地址

`POST /app/follow-up/tasks/complete`

#### 请求体

```json
{
  "taskType": "REPORT_ADVICE",
  "sourceId": 12,
  "remark": "已查看报告并线下沟通"
}
```

#### 说明

1. 当前只支持 `REPORT_ADVICE`
2. `REMINDER` 任务不支持直接通过该接口完成，提醒闭环应走提醒模块接口

### 3.5 忽略任务

#### 接口地址

`POST /app/follow-up/tasks/ignore`

#### 请求体

```json
{
  "taskType": "REMINDER",
  "sourceId": 101,
  "remark": "今天先不处理"
}
```

#### 说明

1. 当前支持 `REMINDER` 和 `REPORT_ADVICE`
2. 忽略表示首页暂不再展示该任务，不代表底层业务已完成
3. 对提醒任务来说，如后续仍需真正闭环，仍应进入提醒模块执行服药/跳过等动作

### 3.6 恢复任务

#### 接口地址

`POST /app/follow-up/tasks/restore`

#### 请求体

```json
{
  "taskType": "REPORT_ADVICE",
  "sourceId": 12,
  "remark": "重新放回首页待办"
}
```

#### 说明

1. 当前支持 `REMINDER` 和 `REPORT_ADVICE`
2. 恢复后任务状态会回到“待跟进”
3. 恢复后该任务会重新参与首页任务流展示


## 4. 前端联动建议

1. 首页任务卡片可提供“延后”按钮
2. 首页任务卡片可提供“忽略”按钮
3. 报告建议类任务卡片可额外提供“本轮已处理”按钮
4. 前端可增加任务中心页，通过 `GET /app/follow-up/tasks` 展示已操作任务
5. 任务中心页中对已忽略、已完成、已延后的任务可提供“恢复显示”按钮
6. 任务操作成功后直接重新请求 `GET /app/dashboard/home`
7. 对于延后操作，前端建议优先提供快捷时间选项，再补一个自定义时间选择器
