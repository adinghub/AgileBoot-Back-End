# APP端首页健康看板接口对接文档

## 1. 说明

本文档用于给 App 前端对接首页健康看板接口。

当前版本已支持以下首页聚合数据：

1. 顶部总览统计
2. 家庭成员健康卡片
3. 今日提醒预览
4. 最近异常报告预览
5. 首页待跟进事项
6. 首页消息预览


## 2. 认证方式

以下接口属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口说明

### 3.1 首页健康看板

#### 接口地址

`GET /app/dashboard/home`

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "overview": {
      "memberCount": 2,
      "reportCount": 6,
      "todayReminderCount": 5,
      "todayPendingReminderCount": 2,
      "todayTakenReminderCount": 2,
      "todaySkippedReminderCount": 0,
      "todayExpiredReminderCount": 1,
      "followUpCount": 3,
      "unreadMessageCount": 2
    },
    "memberCards": [
      {
        "memberId": 1,
        "memberName": "妈妈",
        "relationType": "母亲",
        "todayReminderCount": 3,
        "todayPendingReminderCount": 1,
        "latestReportId": 12,
        "latestReportName": "2026年春季体检报告",
        "latestReportDate": "2026-04-21",
        "latestReportSummary": "共录入8项指标，其中异常2项：空腹血糖偏高、白细胞计数偏低。"
      }
    ],
    "todayReminderPreview": [
      {
        "reminderId": 101,
        "memberId": 1,
        "memberName": "妈妈",
        "scheduledTime": "2026-04-23 08:00:00",
        "drugName": "阿司匹林肠溶片",
        "doseAmount": 1,
        "doseUnit": "片",
        "mealTiming": "饭后",
        "reminderStatus": 0
      }
    ],
    "followUpItems": [
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
          "targetTabCode": "PROCESS",
          "targetAnchorCode": "REMINDER_FEEDBACK",
          "targetAnchorName": "提醒反馈区"
        }
      }
    ],
    "messagePreview": [
      {
        "messageId": 1,
        "memberId": 1,
        "memberName": "妈妈",
        "businessScene": "REPORT_FOLLOW_UP",
        "businessSceneName": "报告跟进",
        "messageTitle": "妈妈的报告需要跟进",
        "messageContent": "本次报告共发现2项重点关注指标：空腹血糖、白细胞计数。",
        "readStatus": 0,
        "createTime": "2026-04-23 10:10:01",
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
    ],
    "recentAbnormalReports": [
      {
        "reportId": 12,
        "memberId": 1,
        "memberName": "妈妈",
        "reportName": "2026年春季体检报告",
        "reportDate": "2026-04-21",
        "abnormalItemCount": 2,
        "analysisSummary": "共录入8项指标，其中异常2项：空腹血糖偏高、白细胞计数偏低。"
      }
    ]
  }
}
```


## 4. 前端对接建议

1. 首页进入后优先调用该接口完成首屏渲染
2. 成员卡片可点击进入成员详情或成员报告列表
3. `todayReminderPreview` 适合作为首页提醒卡片，完整列表仍建议进入提醒页再请求明细接口
4. `recentAbnormalReports` 可作为首页风险提示区域，点击进入报告详情
5. `followUpItems` 适合作为首页任务流区域，点击时建议直接使用 `navigation`
6. `overview.unreadMessageCount` 可直接作为首页消息入口角标
7. `messagePreview` 建议展示最近 1 到 3 条消息，点击可进入消息详情页或直接按 `navigation` 跳转
8. 首页不要把 `analysisSummary` 直接当成医学诊断结论，仍应定位为系统摘要提示
