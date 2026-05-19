# APP端首页任务详情对接文档

## 1. 说明

本文档用于给 App 前端对接首页任务中心详情页。

该接口会一次性返回：

1. 任务基础信息
2. 可操作按钮能力
3. 操作时间线
4. 原始业务页跳转参数
5. 推荐下一步动作


## 2. 认证方式

以下接口属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口地址

`GET /app/follow-up/tasks/{taskId}`


## 4. 路径参数

1. `taskId`：任务记录ID，必填


## 5. 返回示例

```json
{
  "taskInfo": {
    "taskId": 8,
    "taskType": "REPORT_ADVICE",
    "taskTypeName": "报告建议任务",
    "sourceId": 12,
    "taskStatus": 1,
    "taskStatusName": "已延后",
    "memberId": 1,
    "memberName": "妈妈",
    "title": "妈妈的体检报告需要继续跟进",
    "content": "该报告存在 2 项异常指标，建议继续跟进。",
    "actionRemark": "明天上午再处理",
    "delayedUntil": "2026-04-24 08:00:00",
    "completeTime": null,
    "sourceTime": "2026-04-21 00:00:00",
    "canRestore": true,
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
  "sourceAvailable": true,
  "canDelay": true,
  "canComplete": true,
  "canIgnore": true,
  "canRestore": true,
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
  },
  "taskLogs": [
    {
      "logId": 35,
      "taskId": 8,
      "actionType": "DELAY",
      "actionTypeName": "延后任务",
      "beforeStatus": 0,
      "beforeStatusName": "待跟进",
      "afterStatus": 1,
      "afterStatusName": "已延后",
      "actionRemark": "明天上午再处理",
      "delayedUntilSnapshot": "2026-04-24 08:00:00",
      "actionTime": "2026-04-23 15:10:00"
    }
  ]
}
```


## 6. 字段说明

### 6.1 顶层字段

1. `taskInfo`：任务基础信息，字段定义与任务中心列表返回单条对象基本一致
2. `sourceAvailable`：原始来源记录是否仍可用
3. `canDelay`：是否允许延后
4. `canComplete`：是否允许完成
5. `canIgnore`：是否允许忽略
6. `canRestore`：是否允许恢复
7. `recommendedAction`：推荐下一步动作
8. `taskLogs`：任务操作时间线

### 6.2 `taskInfo.navigation`

1. `targetPageCode`：目标页面编码
2. `targetPageName`：目标页面名称
3. `targetBizId`：目标业务主键ID
4. `targetBizType`：目标业务类型
5. `targetTabCode`：建议默认打开的标签页编码
6. `targetAnchorCode`：建议默认打开的锚点编码
7. `targetAnchorName`：建议默认打开的锚点名称

### 6.3 `recommendedAction`

1. `title`：推荐动作标题
2. `actionPriority`：推荐动作优先级，数值越小越优先
3. `riskLevel`：风险等级编码
4. `riskLevelName`：风险等级中文名称
5. `riskCssTag`：风险等级样式标记
6. `content`：推荐动作说明
7. `actionText`：推荐按钮文案
8. `navigation`：推荐动作跳转参数

### 6.4 按钮控制建议

1. `canDelay = true` 时展示或启用“延后”
2. `canComplete = true` 时展示或启用“本轮已处理”
3. `canIgnore = true` 时展示或启用“忽略”
4. `canRestore = true` 时展示或启用“恢复显示”
5. `sourceAvailable = false` 时建议展示“来源记录已失效，仅保留历史记录”


## 7. 前端对接建议

1. 任务中心列表点击单条任务后，优先请求该详情接口
2. 详情页头部可展示 `taskInfo.title`、`taskInfo.content`、`taskInfo.taskStatusName`
3. 按钮区直接使用 `canDelay`、`canComplete`、`canIgnore`、`canRestore` 控制显隐
4. 头部下方可新增“推荐下一步”卡片，直接使用 `recommendedAction`
5. 时间线区域直接使用 `taskLogs` 渲染
6. “查看原始业务”按钮可直接使用 `taskInfo.navigation`
7. 如果只需要时间线而不需要详情，仍可继续调用 `GET /app/follow-up/tasks/{taskId}/logs`
