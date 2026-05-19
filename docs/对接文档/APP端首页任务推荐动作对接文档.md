# APP端首页任务推荐动作对接文档

## 1. 说明

本文档用于给 App 前端对接任务详情页中的“推荐下一步动作”。

本次扩展接口：

`GET /app/follow-up/tasks/{taskId}`

新增字段：

`recommendedAction`


## 2. 返回结构示例

```json
{
  "recommendedAction": {
    "actionPriority": 1,
    "riskLevel": "HIGH",
    "riskLevelName": "高优先关注",
    "riskCssTag": "danger",
    "title": "优先查看本次报告建议",
    "content": "空腹血糖当前结果为 7.1 mmol/L，已超出参考范围，建议结合原始报告和近期状态持续观察，必要时尽快复查。",
    "actionText": "优先安排复查或线下咨询",
    "navigation": {
      "targetPageCode": "HEALTH_REPORT_DETAIL",
      "targetPageName": "体检报告详情页",
      "targetBizId": 12,
      "targetBizType": "REPORT_ADVICE",
      "targetTabCode": "ADVICE"
    }
  }
}
```


## 3. 字段说明

1. `actionPriority`：推荐动作优先级，数值越小越优先
2. `riskLevel`：风险等级编码，当前支持 `HIGH`、`MEDIUM`、`LOW`
3. `riskLevelName`：风险等级中文名称
4. `riskCssTag`：风险等级样式标记
5. `title`：推荐动作标题
6. `content`：推荐动作说明文案
7. `actionText`：推荐按钮文案
8. `navigation`：推荐动作对应的跳转参数


## 4. 前端对接建议

1. 任务详情页头部信息下方可单独展示“推荐下一步”卡片
2. 卡片颜色建议优先使用 `riskCssTag`
3. 如果同页有多个推荐动作区域，可按 `actionPriority` 排序
4. 按钮文案建议直接使用 `actionText`
5. 按钮点击后建议优先使用 `recommendedAction.navigation`
6. 如果 `navigation = null`，建议只展示文案，不展示跳转按钮
7. 当来源记录已失效时，建议展示只读态推荐卡片
