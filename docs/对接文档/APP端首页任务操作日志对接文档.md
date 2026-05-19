# APP端首页任务操作日志对接文档

## 1. 说明

本文档用于给 App 前端对接首页待跟进任务的操作时间线接口。

适用场景：

1. 任务中心详情页
2. 任务操作记录页
3. 用户回顾“这条任务为什么被隐藏或恢复”


## 2. 认证方式

以下接口属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口地址

`GET /app/follow-up/tasks/{taskId}/logs`


## 4. 路径参数

1. `taskId`：任务记录ID，必填


## 5. 返回示例

```json
[
  {
    "logId": 23,
    "taskId": 8,
    "actionType": "RESTORE",
    "actionTypeName": "恢复任务",
    "beforeStatus": 3,
    "beforeStatusName": "已忽略",
    "afterStatus": 0,
    "afterStatusName": "待跟进",
    "actionRemark": "重新放回首页待办",
    "delayedUntilSnapshot": null,
    "actionTime": "2026-04-23 14:30:00"
  },
  {
    "logId": 21,
    "taskId": 8,
    "actionType": "IGNORE",
    "actionTypeName": "忽略任务",
    "beforeStatus": 1,
    "beforeStatusName": "已延后",
    "afterStatus": 3,
    "afterStatusName": "已忽略",
    "actionRemark": "今天先不处理",
    "delayedUntilSnapshot": null,
    "actionTime": "2026-04-23 11:10:00"
  },
  {
    "logId": 19,
    "taskId": 8,
    "actionType": "DELAY",
    "actionTypeName": "延后任务",
    "beforeStatus": 0,
    "beforeStatusName": "待跟进",
    "afterStatus": 1,
    "afterStatusName": "已延后",
    "actionRemark": "晚上吃完饭再确认",
    "delayedUntilSnapshot": "2026-04-23 20:00:00",
    "actionTime": "2026-04-23 09:10:00"
  }
]
```


## 6. 字段说明

1. `logId`：日志ID
2. `taskId`：所属任务记录ID
3. `actionType`：操作类型，当前支持 `DELAY`、`COMPLETE`、`IGNORE`、`RESTORE`
4. `actionTypeName`：操作类型中文名称
5. `beforeStatus`：操作前状态
6. `beforeStatusName`：操作前状态中文名称
7. `afterStatus`：操作后状态
8. `afterStatusName`：操作后状态中文名称
9. `actionRemark`：用户本次填写的备注
10. `delayedUntilSnapshot`：延后时间快照，仅延后操作通常有值
11. `actionTime`：操作发生时间


## 7. 前端对接建议

1. 建议按接口返回顺序直接展示，即最新记录在最上方
2. `actionType = DELAY` 时可突出显示 `delayedUntilSnapshot`
3. `beforeStatusName -> afterStatusName` 可直接拼装成状态变化文案
4. 当列表为空时，可展示“暂无操作记录”
5. 任务中心列表页点击某条任务后，可先展示任务摘要，再拉取该日志接口
