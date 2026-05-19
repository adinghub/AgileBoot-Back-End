# APP端体检报告异常建议接口对接文档

## 1. 说明

本文档用于给 App 前端对接体检报告异常建议接口。

当前版本已支持：

1. 查询报告异常建议摘要
2. 查询异常项对应的建议列表
3. 返回是否已有启用中的用药计划，便于前端继续做业务引导


## 2. 认证方式

以下接口都属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口说明

### 3.1 体检报告异常建议

#### 接口地址

`GET /app/reports/{reportId}/advice`

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "reportId": 10,
    "memberId": 1,
    "memberName": "妈妈",
    "abnormalItemCount": 2,
    "hasActiveMedicationPlan": true,
    "activeMedicationPlanCount": 1,
    "summary": "本次报告共发现2项重点关注指标：空腹血糖、白细胞计数。该成员当前已有1条启用中的用药计划，可结合提醒执行情况持续跟踪。",
    "adviceItems": [
      {
        "adviceType": "RECHECK",
        "title": "空腹血糖偏高，建议复查确认",
        "content": "空腹血糖当前结果为6.2 mmol/L，已超出参考范围，建议结合原始报告和近期状态持续观察，必要时尽快复查。",
        "actionText": "优先安排复查或线下咨询",
        "sourceItemId": 1,
        "sourceItemName": "空腹血糖",
        "sourceResultValue": "6.2 mmol/L",
        "abnormalFlag": 3,
        "abnormalFlagName": "偏高",
        "priority": 1
      },
      {
        "adviceType": "MEDICATION_TRACK",
        "title": "关注血糖管理与提醒执行",
        "content": "血糖相关指标出现异常时，建议同步关注近期饮食、作息以及复查节奏。如果成员正在执行用药提醒，可结合提醒完成情况持续跟踪。",
        "actionText": "查看现有用药提醒并持续跟踪",
        "sourceItemId": 1,
        "sourceItemName": "空腹血糖",
        "sourceResultValue": "6.2 mmol/L",
        "abnormalFlag": 3,
        "abnormalFlagName": "偏高",
        "priority": 2
      }
    ]
  }
}
```


## 4. 前端对接建议

1. 报告详情页可在分析摘要下方增加“异常建议”区域
2. 建议按 `priority` 升序展示
3. 如果 `hasActiveMedicationPlan = true`，前端可增加“去看提醒”或“去看用药计划”按钮
4. 如果 `hasActiveMedicationPlan = false`，前端可引导去新增提醒或补充健康计划
5. 建议文案应定位为系统辅助提示，不应替代医生诊断
