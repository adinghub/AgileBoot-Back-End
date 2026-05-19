# APP端会员接口对接文档

## 1. 模块范围

App 端会员模块首期覆盖以下能力：

1. 查看当前会员状态
2. 查看当前权益快照
3. 查看可开通等级
4. 查看最近会员订单
5. 兑换会员兑换码
6. 预留订阅开通 / 自动续费接口

## 2. App 接口清单

### 2.1 当前会员

- `GET /app/member-subscription/current`

### 2.2 当前权益快照

- `GET /app/member-entitlements/current`

### 2.3 可开通等级列表

- `GET /app/member-subscription/levels`

### 2.4 会员订单列表

- `GET /app/member-subscription/orders`

### 2.5 开通会员（首期可做模拟成功）

- `POST /app/member-subscription/subscribe`

### 2.6 自动续费开关

- `POST /app/member-subscription/auto-renew`

### 2.7 兑换码兑换

- `POST /app/member-redeem-codes/redeem`

## 3. 当前会员

`GET /app/member-subscription/current`

返回关键字段：

- `hasActiveMember`
- `memberLevelId`
- `levelCode`
- `levelName`
- `status`
- `effectiveStartTime`
- `effectiveEndTime`
- `remainingDays`
- `autoRenew`
- `subscriptionStatus`
- `nextRenewTime`
- `cancelReason`

说明：

1. 如果当前无有效会员，`hasActiveMember = false`。
2. `effectiveEndTime = null` 表示长期有效。

## 4. 当前权益快照

`GET /app/member-entitlements/current`

返回关键字段：

- `userId`
- `currentLevelCode`
- `currentLevelName`
- `hasActiveMember`
- `effectiveStartTime`
- `effectiveEndTime`
- `items`

`items` 中每条权益包含：

- `featureCode`
- `featureName`
- `featureType`
- `entitlementSource`
- `enabled`
- `limitValue`
- `quotaPeriodType`
- `periodKey`
- `usedCount`
- `remainingCount`
- `message`

前端使用建议：

1. 是否展示 AI 解析按钮，由 `enabled` 决定。
2. 次数型能力的剩余额度，直接使用 `remainingCount`。
3. 若某功能不可用，优先展示后端返回的 `message`，不要自己推断文案。

## 5. 可开通等级列表

`GET /app/member-subscription/levels`

返回关键字段：

- `memberLevelId`
- `levelCode`
- `levelName`
- `price`
- `durationDays`
- `benefitDesc`
- `status`
- `currentLevel`
- `canSubscribe`
- `unavailableReason`

说明：

1. `canSubscribe = false` 时，前端按钮应置灰。
2. `unavailableReason` 用于解释为什么当前等级不能开通。

## 6. 会员订单列表

`GET /app/member-subscription/orders`

查询参数：

- `pageNum`
- `pageSize`

返回关键字段：

- `userMemberOrderId`
- `orderNo`
- `memberLevelId`
- `levelCode`
- `levelName`
- `orderType`
- `orderStatus`
- `sourceType`
- `orderAmount`
- `payTime`
- `effectiveStartTime`
- `effectiveEndTime`
- `remark`

## 7. 开通会员

`POST /app/member-subscription/subscribe`

请求体：

```json
{
  "memberLevelId": 2,
  "remark": "App主动开通"
}
```

首期处理建议：

1. 当前版本可先不接第三方支付。
2. 后端直接模拟一笔成功订单，生成或更新用户会员关系。
3. App 在交互上可明确提示“当前为内测版本，确认后直接开通成功”。

## 8. 自动续费开关

`POST /app/member-subscription/auto-renew`

请求体：

```json
{
  "enabled": 1,
  "remark": "用户主动开启自动续费"
}
```

说明：

1. 若当前还没有订阅合同，可由后端自动补建订阅记录。
2. 若当前用户不是订阅型会员来源，后端可拒绝并返回业务提示。

## 9. 兑换码兑换

`POST /app/member-redeem-codes/redeem`

请求体：

```json
{
  "redeemCode": "A8KQ2M9X7P4R"
}
```

成功返回建议：

- `levelCode`
- `levelName`
- `effectiveStartTime`
- `effectiveEndTime`
- `message`

兑换规则：

1. 每个兑换码只能兑换一次。
2. 若已有同等级有效会员，则顺延有效期。
3. 若已有其他等级有效会员，首期后端直接拒绝。
4. 兑换成功后，App 应主动刷新：
   - 当前会员
   - 当前权益快照
   - 最近订单

## 10. 与 AI 解析按钮的联动建议

App 报告页不要自行判断会员等级名称，而是统一读取 `GET /app/member-entitlements/current`。

推荐联动：

1. `AI_REPORT_PARSE.enabled = false`
   则按钮隐藏或置灰，并展示后端 `message`
2. `AI_REPORT_PARSE_QUOTA.remainingCount = 0`
   则提示“当前周期次数已用完”
3. `AI_REPORT_SUMMARY.enabled = true`
   才展示 AI 总结能力

## 11. 推荐页面结构

“我的”页进入会员中心，包含：

1. 当前会员卡片
2. 权益快照区
3. 可开通等级列表
4. 兑换码入口
5. 最近订单列表

这样后续即使会员规则调整，App 也只需要消费后端返回结果，无需重写本地判断逻辑。
