# APP端首页待跟进事项对接文档

## 1. 说明

首页待跟进事项没有新增独立接口，
而是直接并入现有首页健康看板接口：

`GET /app/dashboard/home`

前端继续使用原接口即可，只是返回结构中新增：

1. `overview.followUpCount`
2. `followUpItems`


## 2. 认证方式

以下接口属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 返回字段补充说明

### 3.1 `overview.followUpCount`

表示当前首页返回的待跟进事项数量，适合用于首页角标或卡片计数。

### 3.2 `followUpItems`

返回示例：

```json
[
  {
    "taskType": "REMINDER",
    "title": "妈妈的用药提醒待处理",
    "content": "阿司匹林肠溶片 计划时间 08:00，请及时确认是否已服药。",
    "actionText": "去处理提醒",
    "memberId": 1,
    "memberName": "妈妈",
    "reminderId": 101,
    "reportId": null,
    "priority": 1,
    "followUpTime": "2026-04-23 08:00:00",
    "navigation": {
      "targetPageCode": "MEDICATION_REMINDER_DETAIL",
      "targetPageName": "用药提醒处理页",
      "targetBizId": 101,
      "targetBizType": "REMINDER",
      "targetTabCode": "PROCESS"
    }
  },
  {
    "taskType": "REPORT_ADVICE",
    "title": "妈妈的报告需要跟进",
    "content": "本次报告共发现2项重点关注指标：空腹血糖、白细胞计数。该成员当前已有1条启用中的用药计划，可结合提醒执行情况持续跟踪。",
    "actionText": "查看报告建议与用药计划",
    "memberId": 1,
    "memberName": "妈妈",
    "reminderId": null,
    "reportId": 12,
    "priority": 3,
    "followUpTime": "2026-04-21",
    "navigation": {
      "targetPageCode": "HEALTH_REPORT_DETAIL",
      "targetPageName": "体检报告详情页",
      "targetBizId": 12,
      "targetBizType": "REPORT_ADVICE",
      "targetTabCode": "ADVICE"
    }
  }
]
```


## 4. 前端对接建议

1. 首页可在看板下方直接增加“待跟进事项”区域
2. 按 `priority` 升序展示即可
3. 点击卡片时建议优先使用 `navigation`
4. 旧版本前端仍可继续兼容 `reminderId`、`reportId`
5. 首页任务流建议控制在首屏可见范围，不建议一次性全部展开
