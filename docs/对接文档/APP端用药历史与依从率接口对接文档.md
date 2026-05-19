# APP端用药历史与依从率接口对接文档

## 1. 历史服药记录

### 接口

`GET /app/medication/reminders/history`

### 查询参数

| 参数 | 说明 |
| --- | --- |
| `pageNum` | 页码 |
| `pageSize` | 每页条数 |
| `memberId` | 家庭成员ID，可选 |
| `planId` | 用药计划ID，可选 |
| `reminderStatus` | 提醒状态，可选 |
| `startDate` | 开始日期，格式 `yyyy-MM-dd` |
| `endDate` | 结束日期，格式 `yyyy-MM-dd` |

### 返回字段

1. `memberName`
2. `scheduledTime`
3. `drugName`
4. `reminderStatus`
5. `notifyStatus`
6. `feedbackTime`
7. `skipReason`

## 2. 依从率统计

### 接口

`GET /app/medication/reminders/history/statistics`

### 返回字段

1. `scheduledReminderCount`
2. `takenReminderCount`
3. `skippedReminderCount`
4. `expiredReminderCount`
5. `pendingReminderCount`
6. `adherenceRate`

### 依从率口径

`已服药 / （已服药 + 已跳过 + 已过期）`

## 3. 依从率趋势

### 接口

`GET /app/medication/reminders/history/trend`

### 返回字段

1. `reminderDate`
2. `scheduledReminderCount`
3. `takenReminderCount`
4. `skippedReminderCount`
5. `expiredReminderCount`
6. `pendingReminderCount`
7. `adherenceRate`

## 4. 前端建议

建议交互：

1. 先拉统计摘要
2. 再拉趋势列表
3. 最后按需拉历史分页明细
