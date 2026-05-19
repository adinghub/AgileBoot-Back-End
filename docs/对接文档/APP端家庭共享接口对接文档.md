# APP端家庭共享接口对接文档

## 1. 说明

本文档覆盖：

1. 家庭成员列表新增访问权限字段
2. 创建共享邀请码
3. 接受共享邀请码
4. 查询协同账号列表
5. 查询共享邀请列表

基础地址仍为：

`http://localhost:18012`

认证方式仍为：

`Authorization: Bearer {token}`

## 2. 家庭成员新增字段

接口：

`GET /app/family/members`

`GET /app/family/members/{memberId}`

新增返回字段：

| 字段 | 说明 |
| --- | --- |
| `accessSource` | 访问来源，`OWNER` / `SHARED` |
| `accessSourceName` | 访问来源名称 |
| `accessRole` | 访问角色，`OWNER` / `EDITOR` / `VIEWER` |
| `accessRoleName` | 访问角色名称 |
| `isOwner` | 是否主账号 |
| `canEdit` | 是否允许编辑 |

## 3. 创建共享邀请码

### 接口

`POST /app/family/members/{memberId}/share-invites`

### 请求参数

```json
{
  "shareRole": "EDITOR",
  "expireHours": 72,
  "remark": "App 二期家庭共享邀请码"
}
```

### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "inviteId": 1,
    "memberId": 12,
    "inviteCode": "A1B2C3D4",
    "shareRole": "EDITOR",
    "shareRoleName": "协同管理",
    "inviteStatus": 0,
    "inviteStatusName": "待接受",
    "expireTime": "2026-04-26 12:00:00"
  }
}
```

## 4. 接受共享邀请码

### 接口

`POST /app/family/members/share-invites/{inviteCode}/accept`

示例：

`POST /app/family/members/share-invites/A1B2C3D4/accept`

## 5. 协同账号列表

### 接口

`GET /app/family/members/{memberId}/collaborators`

### 返回字段

1. `shareId`
2. `collaboratorUserId`
3. `collaboratorNickname`
4. `collaboratorMobile`
5. `shareRole`
6. `shareRoleName`
7. `shareStatus`
8. `shareStatusName`
9. `acceptedTime`

## 6. 共享邀请列表

### 接口

`GET /app/family/members/{memberId}/share-invites`

### 返回字段

1. `inviteId`
2. `inviteCode`
3. `shareRole`
4. `shareRoleName`
5. `inviteStatus`
6. `inviteStatusName`
7. `expireTime`
8. `acceptedTime`
9. `inviteeNickname`

## 7. 取消邀请码

### 接口

`DELETE /app/family/members/{memberId}/share-invites/{inviteId}`

## 8. 移除协同账号

### 接口

`DELETE /app/family/members/{memberId}/collaborators/{shareId}`
