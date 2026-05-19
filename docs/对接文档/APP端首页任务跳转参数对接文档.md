# APP端首页任务跳转参数对接文档

## 1. 说明

本文档用于给 App 前端对接首页任务和任务中心的统一跳转参数。

本次新增字段：

`navigation`

适用范围：

1. 首页健康看板 `followUpItems`
2. 任务中心列表单条任务
3. 任务详情 `taskInfo`


## 2. 字段结构

```json
{
  "targetPageCode": "HEALTH_REPORT_DETAIL",
  "targetPageName": "体检报告详情页",
  "targetBizId": 12,
  "targetBizType": "REPORT_ADVICE",
  "targetTabCode": "ADVICE",
  "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
  "targetAnchorName": "异常指标区"
}
```


## 3. 字段说明

1. `targetPageCode`：目标页面编码
2. `targetPageName`：目标页面中文名称
3. `targetBizId`：目标业务主键ID
4. `targetBizType`：目标业务类型
5. `targetTabCode`：建议默认打开的标签页编码
6. `targetAnchorCode`：建议默认打开的锚点编码
7. `targetAnchorName`：建议默认打开的锚点名称


## 4. 当前支持值

### 4.1 用药提醒任务

返回示例：

```json
{
  "targetPageCode": "MEDICATION_REMINDER_DETAIL",
  "targetPageName": "用药提醒处理页",
  "targetBizId": 101,
  "targetBizType": "REMINDER",
  "targetTabCode": "PROCESS",
  "targetAnchorCode": "REMINDER_FEEDBACK",
  "targetAnchorName": "提醒反馈区"
}
```

### 4.2 报告建议任务

返回示例：

```json
{
  "targetPageCode": "HEALTH_REPORT_DETAIL",
  "targetPageName": "体检报告详情页",
  "targetBizId": 12,
  "targetBizType": "REPORT_ADVICE",
  "targetTabCode": "ADVICE",
  "targetAnchorCode": "REPORT_ABNORMAL_ITEMS",
  "targetAnchorName": "异常指标区"
}
```


## 5. 前端对接建议

1. 首页任务卡片点击后，优先读取 `navigation`
2. 任务中心列表点击后，优先读取单条任务中的 `navigation`
3. 任务详情页中的“查看原始业务”按钮，也建议复用 `taskInfo.navigation`
4. 如果旧版本前端仍使用 `reminderId`、`reportId`，本次仍可兼容
5. 建议前端内部维护一层 `targetPageCode -> 实际页面路由` 的映射表
6. 建议前端同时维护一层 `targetAnchorCode -> 页面内定位区域` 的映射表
