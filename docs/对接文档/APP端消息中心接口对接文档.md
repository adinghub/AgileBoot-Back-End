# APP端消息中心接口对接文档

## 1. 说明

本文档用于提供给 App 前端对接消息中心接口。

当前消息中心承接的业务场景：

1. 用药提醒消息
2. 报告跟进消息

消息中心接口统一前缀：

`/app/messages`


## 2. 消息数据结构

### 2.1 列表项结构

```json
{
  "messageId": 1,
  "memberId": 2,
  "memberName": "妈妈",
  "businessScene": "MEDICATION_REMINDER",
  "businessSceneName": "用药提醒",
  "businessId": 101,
  "messageTitle": "妈妈的用药提醒待处理",
  "messageContent": "阿司匹林肠溶片，计划时间 08:00，请及时确认是否已服药。",
  "readStatus": 0,
  "readTime": null,
  "sendStatus": 1,
  "sendTime": "2026-04-23 08:00:03",
  "sendRetryCount": 0,
  "sendChannel": "APP_PUSH",
  "sendResultMessage": "已派发到1个设备",
  "createTime": "2026-04-23 08:00:02",
  "navigation": {
    "targetPageCode": "MEDICATION_REMINDER_DETAIL",
    "targetPageName": "用药提醒处理页",
    "targetBizId": 101,
    "targetBizType": "REMINDER",
    "targetTabCode": "PROCESS",
    "targetAnchorCode": "REMINDER_FEEDBACK",
    "targetAnchorName": "提醒反馈区"
  },
  "recommendedAction": {
    "actionPriority": 1,
    "riskLevel": "HIGH",
    "riskLevelName": "高优先关注",
    "riskCssTag": "danger",
    "title": "优先确认本次提醒结果",
    "content": "阿司匹林肠溶片，计划时间为 2026-04-23 08:00，建议尽快确认是否已服药；如本次无法执行，可进入提醒页选择跳过并记录原因。",
    "actionText": "去处理提醒",
    "navigation": {
      "targetPageCode": "MEDICATION_REMINDER_DETAIL",
      "targetPageName": "用药提醒处理页",
      "targetBizId": 101,
      "targetBizType": "REMINDER",
      "targetTabCode": "PROCESS",
      "targetAnchorCode": "REMINDER_FEEDBACK",
      "targetAnchorName": "提醒反馈区"
    }
  }
}
```

### 2.2 字段说明

1. `messageId`：消息ID
2. `memberId`：家庭成员ID
3. `memberName`：成员名称快照
4. `businessScene`：业务场景编码
5. `businessSceneName`：业务场景名称
6. `businessId`：业务主键ID
7. `messageTitle`：消息标题
8. `messageContent`：消息正文
9. `readStatus`：已读状态，`0` 未读，`1` 已读
10. `readTime`：已读时间
11. `sendStatus`：发送状态，`0` 待发送，`1` 发送成功，`2` 发送失败
12. `sendTime`：最近一次发送时间
13. `sendRetryCount`：发送重试次数
14. `sendChannel`：最近一次发送通道
15. `sendResultMessage`：最近一次发送结果说明
16. `createTime`：消息创建时间
17. `navigation`：统一跳转参数
18. `recommendedAction`：推荐下一步动作


## 3. 消息列表

### 3.1 接口地址

`GET /app/messages`

### 3.2 请求头

```http
Authorization: Bearer {token}
```

### 3.3 查询参数

可选参数：

1. `pageNum`：页码，默认 `1`
2. `pageSize`：每页数量，默认 `10`
3. `businessScene`：业务场景筛选
4. `readStatus`：已读状态筛选

示例：

`GET /app/messages?pageNum=1&pageSize=10&readStatus=0`

### 3.4 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "messageId": 1,
        "memberId": 2,
        "memberName": "妈妈",
        "businessScene": "REPORT_FOLLOW_UP",
        "businessSceneName": "报告跟进",
        "businessId": 12,
        "messageTitle": "妈妈的报告需要跟进",
        "messageContent": "本次报告共发现2项重点关注指标：空腹血糖、白细胞计数。该成员当前已有1条启用中的用药计划，可结合提醒执行情况持续跟踪。",
        "readStatus": 0,
        "readTime": null,
        "sendStatus": 1,
        "sendTime": "2026-04-23 10:10:02",
        "sendRetryCount": 0,
        "sendChannel": "APP_PUSH",
        "sendResultMessage": "已派发到1个设备",
        "createTime": "2026-04-23 10:10:01",
        "navigation": {
          "targetPageCode": "HEALTH_REPORT_DETAIL",
          "targetPageName": "体检报告详情页",
          "targetBizId": 12,
          "targetBizType": "REPORT_ADVICE",
          "targetTabCode": "ADVICE",
          "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
          "targetAnchorName": "异常指标区"
        },
        "recommendedAction": {
          "actionPriority": 1,
          "riskLevel": "HIGH",
          "riskLevelName": "高优先关注",
          "riskCssTag": "danger",
          "title": "空腹血糖偏高，建议复查确认",
          "content": "空腹血糖当前结果为 6.2 mmol/L，已超出参考范围，建议结合原始报告和近期状态持续观察，必要时尽快复查。",
          "actionText": "优先安排复查或线下咨询",
          "navigation": {
            "targetPageCode": "HEALTH_REPORT_DETAIL",
            "targetPageName": "体检报告详情页",
            "targetBizId": 12,
            "targetBizType": "REPORT_ADVICE",
            "targetTabCode": "ADVICE",
            "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
            "targetAnchorName": "异常指标区"
          }
        }
      }
    ],
    "total": 1
  }
}
```

### 3.5 前端处理建议

1. 列表点击后可直接使用 `navigation` 跳转
2. 若需要列表卡片强化引导，可直接展示 `recommendedAction.title`
3. 建议未读状态使用本地乐观更新，再以接口结果为准


## 4. 消息详情

### 4.1 接口地址

`GET /app/messages/{messageId}`

示例：

`GET /app/messages/1`

### 4.2 成功响应

详情接口在列表字段基础上，额外返回：

`payload`

示例：

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "messageId": 1,
    "memberId": 2,
    "memberName": "妈妈",
    "businessScene": "REPORT_FOLLOW_UP",
    "businessSceneName": "报告跟进",
    "businessId": 12,
    "messageTitle": "妈妈的报告需要跟进",
    "messageContent": "本次报告共发现2项重点关注指标：空腹血糖、白细胞计数。该成员当前已有1条启用中的用药计划，可结合提醒执行情况持续跟踪。",
    "readStatus": 0,
    "readTime": null,
    "sendStatus": 1,
    "sendTime": "2026-04-23 10:10:02",
    "sendRetryCount": 0,
    "sendChannel": "APP_PUSH",
    "sendResultMessage": "已派发到1个设备",
    "createTime": "2026-04-23 10:10:01",
    "navigation": {
      "targetPageCode": "HEALTH_REPORT_DETAIL",
      "targetPageName": "体检报告详情页",
      "targetBizId": 12,
      "targetBizType": "REPORT_ADVICE",
      "targetTabCode": "ADVICE",
      "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
      "targetAnchorName": "异常指标区"
    },
    "recommendedAction": {
      "actionPriority": 1,
      "riskLevel": "HIGH",
      "riskLevelName": "高优先关注",
      "riskCssTag": "danger",
      "title": "空腹血糖偏高，建议复查确认",
      "content": "空腹血糖当前结果为 6.2 mmol/L，已超出参考范围，建议结合原始报告和近期状态持续观察，必要时尽快复查。",
      "actionText": "优先安排复查或线下咨询",
      "navigation": {
        "targetPageCode": "HEALTH_REPORT_DETAIL",
        "targetPageName": "体检报告详情页",
        "targetBizId": 12,
        "targetBizType": "REPORT_ADVICE",
        "targetTabCode": "ADVICE",
        "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
        "targetAnchorName": "异常指标区"
      }
    },
    "payload": {
      "businessScene": "REPORT_FOLLOW_UP",
      "businessId": 12,
      "title": "妈妈的报告需要跟进",
      "content": "本次报告共发现2项重点关注指标：空腹血糖、白细胞计数。该成员当前已有1条启用中的用药计划，可结合提醒执行情况持续跟踪。",
      "navigation": {
        "targetPageCode": "HEALTH_REPORT_DETAIL",
        "targetPageName": "体检报告详情页",
        "targetBizId": 12,
        "targetBizType": "REPORT_ADVICE",
        "targetTabCode": "ADVICE",
        "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
        "targetAnchorName": "异常指标区"
      },
      "recommendedAction": {
        "actionPriority": 1,
        "riskLevel": "HIGH",
        "riskLevelName": "高优先关注",
        "riskCssTag": "danger",
        "title": "空腹血糖偏高，建议复查确认",
        "content": "空腹血糖当前结果为 6.2 mmol/L，已超出参考范围，建议结合原始报告和近期状态持续观察，必要时尽快复查。",
        "actionText": "优先安排复查或线下咨询",
        "navigation": {
          "targetPageCode": "HEALTH_REPORT_DETAIL",
          "targetPageName": "体检报告详情页",
          "targetBizId": 12,
          "targetBizType": "REPORT_ADVICE",
          "targetTabCode": "ADVICE",
          "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
          "targetAnchorName": "异常指标区"
        }
      }
    }
  }
}
```


## 5. 未读消息数

### 5.1 接口地址

`GET /app/messages/unread-count`

### 5.2 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "unreadCount": 3
  }
}
```


## 6. 标记单条已读

### 6.1 接口地址

`POST /app/messages/{messageId}/read`

### 6.2 请求参数

无

### 6.3 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```


## 7. 全部标记已读

### 7.1 接口地址

`POST /app/messages/read-all`

### 7.2 请求参数

无

### 7.3 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```


## 8. 前端落地建议

1. App 首页可调用未读数接口展示角标
2. 消息详情页打开后，可立即调用单条已读接口
3. 消息列表和系统 Push 点击后的落地页，建议共用同一套 `navigation` 解析器
4. 如果 Push 发送失败，消息中心列表仍然可以正常展示这条消息，不需要前端额外兜底
