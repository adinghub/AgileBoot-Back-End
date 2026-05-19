# APP端推送消息业务载荷对接文档

## 1. 说明

本文档用于约定 App 端接收健康系统 Push 通知时的业务透传载荷结构。

同一份载荷也会出现在消息中心详情接口中，
前端可复用同一套解析逻辑。

当前已接入场景：

1. 用药提醒 Push
2. 报告建议 Push

本次目标不是新增接口，
而是统一“通知点击后如何进入业务页”的透传协议。


## 2. 载荷结构

```json
{
  "businessScene": "MEDICATION_REMINDER",
  "businessId": 101,
  "title": "妈妈的用药提醒待处理",
  "content": "阿司匹林肠溶片，计划时间 08:00，请及时确认是否已服药。",
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


## 3. 字段说明

1. `businessScene`：业务场景编码，当前用药提醒为 `MEDICATION_REMINDER`
2. `businessId`：业务主键ID
3. `title`：通知业务标题
4. `content`：通知业务正文
5. `navigation`：点击通知后统一跳转参数
6. `recommendedAction`：通知落地后的推荐动作


## 4. 前端对接建议

1. 用户点击系统通知后，优先读取 `navigation`
2. 如果进入通知详情页，可直接复用 `recommendedAction`
3. 建议前端把“通知点击”和“首页任务点击”共用同一套路由解析逻辑
4. 后续如果接到新的 `businessScene`，建议按场景扩展映射，而不是新增独立解析器

## 5. 报告建议 Push 示例

```json
{
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
```
