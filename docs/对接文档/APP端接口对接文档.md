# APP端接口对接文档

## 1. 文档说明

本文档用于提供给 App 前端进行接口对接，当前覆盖已经完成的以下模块：

1. App 用户注册
2. App 用户登录
3. App 用户退出登录
4. 获取当前登录用户
5. 家庭成员管理
6. 其他健康业务已拆分到本目录下的独立对接文档

例如：

1. `APP端药品与用药提醒接口对接文档.md`
2. `APP端体检报告接口对接文档.md`
3. `APP端首页健康看板接口对接文档.md`
4. `APP端消息中心接口对接文档.md`


## 2. 服务信息

### 2.1 开发环境默认端口

当前 `healthtrail-api` 模块默认端口为：

`18012`

因此本地开发环境基础地址通常为：

`http://localhost:18012`

### 2.2 接口前缀说明

当前 App 端接口分成两类：

1. 匿名接口：`/common/**`
2. 登录后接口：`/app/**`

说明：

1. 登录、注册属于匿名接口
2. 当前用户信息、退出登录、家庭成员管理属于登录后接口


## 3. 认证方式

### 3.1 Token 获取

登录或注册成功后，接口会返回：

1. `token`
2. `tokenType`

当前 `tokenType` 为：

`Bearer`

### 3.2 请求头传递方式

后续调用登录后接口时，请在请求头中传：

```http
Authorization: Bearer {token}
```

示例：

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 3.3 退出登录说明

调用退出登录接口后，服务端会删除 Redis 中的登录缓存。

前端也应同步清理本地保存的：

1. token
2. 用户信息缓存
3. 登录态标记


## 4. 通用返回结构

所有接口统一返回如下结构：

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {}
}
```

字段说明：

1. `code`：业务状态码，`0` 表示成功，其他值表示失败
2. `msg`：返回消息
3. `data`：业务数据，部分接口可能为 `null`

### 4.1 成功示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "token": "xxxxx",
    "tokenType": "Bearer",
    "userInfo": {
      "userId": 1,
      "mobile": "13800138000",
      "nickname": "健康用户8000",
      "avatar": null,
      "status": 1,
      "lastLoginTime": "2026-04-23 10:30:00",
      "registerSource": "APP"
    }
  }
}
```

### 4.2 常见失败示例

```json
{
  "code": 12003,
  "msg": "手机号或密码错误",
  "data": null
}
```


## 5. 状态值与字段约定

### 5.1 用户状态

1. `1`：正常
2. `0`：停用

### 5.2 性别

1. `0`：未知
2. `1`：男
3. `2`：女

### 5.3 日期格式

当前接口中涉及日期时，建议前端按以下格式传递：

1. 日期：`yyyy-MM-dd`
2. 日期时间：`yyyy-MM-dd HH:mm:ss`

例如：

1. `2026-04-23`
2. `2026-04-23 10:30:00`


## 6. 认证接口

### 6.1 App 用户注册

#### 接口地址

`POST /common/app/auth/register`

#### 请求头

`Content-Type: application/json`

#### 请求参数

```json
{
  "mobile": "13800138000",
  "nickname": "张三",
  "password": "123456",
  "confirmPassword": "123456"
}
```

字段说明：

1. `mobile`：手机号，必填，11 位大陆手机号
2. `nickname`：昵称，非必填，不传时后端会自动生成默认昵称
3. `password`：密码，必填，长度 6 到 20 位
4. `confirmPassword`：确认密码，必填，需与 `password` 一致

#### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "userInfo": {
      "userId": 1,
      "mobile": "13800138000",
      "nickname": "张三",
      "avatar": null,
      "status": 1,
      "lastLoginTime": "2026-04-23 10:30:00",
      "registerSource": "APP"
    }
  }
}
```

#### 前端处理建议

注册成功后，前端直接：

1. 保存 `token`
2. 保存 `userInfo`
3. 跳转首页

不需要再额外调用一次登录接口。

#### 常见错误

1. `12001`：两次输入的密码不一致
2. `12002`：该手机号已被注册
3. `103`：请求参数异常


### 6.2 App 用户登录

#### 接口地址

`POST /common/app/auth/login`

#### 请求参数

```json
{
  "mobile": "13800138000",
  "password": "123456"
}
```

字段说明：

1. `mobile`：手机号，必填
2. `password`：密码，必填

#### 成功响应

返回结构与注册成功一致。

#### 常见错误

1. `12003`：手机号或密码错误
2. `12004`：该 App 账号已被停用
3. `103`：请求参数异常


### 6.3 App 用户退出登录

#### 接口地址

`POST /app/auth/logout`

#### 请求头

```http
Authorization: Bearer {token}
```

#### 请求参数

无

#### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

#### 前端处理建议

退出成功后，前端应立即清空本地登录态：

1. 删除 token
2. 删除用户信息
3. 返回登录页


## 7. 当前用户接口

### 7.1 获取当前登录用户

#### 接口地址

`GET /app/user/current`

#### 请求头

```http
Authorization: Bearer {token}
```

#### 请求参数

无

#### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "mobile": "13800138000",
    "nickname": "张三",
    "avatar": null,
    "status": 1,
    "lastLoginTime": "2026-04-23 10:30:00",
    "registerSource": "APP"
  }
}
```

字段说明：

1. `userId`：App 用户 ID
2. `mobile`：手机号
3. `nickname`：昵称
4. `avatar`：头像地址
5. `status`：状态
6. `lastLoginTime`：最后登录时间
7. `registerSource`：注册来源，当前固定为 `APP`


## 8. 家庭成员接口

### 8.1 家庭成员列表

#### 接口地址

`GET /app/family/members`

#### 请求头

```http
Authorization: Bearer {token}
```

#### 查询参数

可选参数：

1. `memberName`：成员姓名模糊搜索
2. `status`：状态筛选
3. `orderColumn`：排序字段，可选
4. `orderDirection`：排序方向，`ascending` 或 `descending`

示例：

`GET /app/family/members?memberName=妈&status=1`

#### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": [
    {
      "memberId": 1,
      "ownerUserId": 1,
      "memberName": "妈妈",
      "gender": 2,
      "birthday": "1968-05-01 00:00:00",
      "relationType": "母亲",
      "height": 158.00,
      "weight": 55.50,
      "bloodType": "A",
      "allergyHistory": "无",
      "chronicHistory": "高血压",
      "remark": "需要长期服药",
      "status": 1
    }
  ]
}
```


### 8.2 家庭成员详情

#### 接口地址

`GET /app/family/members/{memberId}`

示例：

`GET /app/family/members/1`

#### 成功响应

返回单个 `FamilyMemberDTO`，字段与列表项一致。

#### 说明

当前接口只允许查看当前登录用户自己的家庭成员。


### 8.3 新增家庭成员

#### 接口地址

`POST /app/family/members`

#### 请求参数

```json
{
  "memberName": "妈妈",
  "gender": 2,
  "birthday": "1968-05-01",
  "relationType": "母亲",
  "height": 158.00,
  "weight": 55.50,
  "bloodType": "A",
  "allergyHistory": "无",
  "chronicHistory": "高血压",
  "remark": "需要长期服药",
  "status": 1
}
```

字段说明：

1. `memberName`：成员姓名，必填，最长 30 字符
2. `gender`：性别，必填
3. `birthday`：生日，非必填，格式 `yyyy-MM-dd`
4. `relationType`：关系，必填，例如本人、父亲、母亲、孩子
5. `height`：身高，非必填，单位 cm
6. `weight`：体重，非必填，单位 kg
7. `bloodType`：血型，非必填
8. `allergyHistory`：过敏史，非必填
9. `chronicHistory`：慢病史，非必填
10. `remark`：备注，非必填
11. `status`：状态，非必填，建议传 `1`

#### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```


### 8.4 修改家庭成员

#### 接口地址

`PUT /app/family/members/{memberId}`

#### 请求参数

与新增家庭成员一致。

示例：

```json
{
  "memberName": "妈妈",
  "gender": 2,
  "birthday": "1968-05-01",
  "relationType": "母亲",
  "height": 159.00,
  "weight": 54.80,
  "bloodType": "A",
  "allergyHistory": "青霉素过敏",
  "chronicHistory": "高血压",
  "remark": "近期需要复查",
  "status": 1
}
```

#### 说明

1. `memberId` 通过路径传递
2. 只能修改当前登录用户自己的家庭成员


### 8.5 删除家庭成员

#### 接口地址

`DELETE /app/family/members/{memberId}`

示例：

`DELETE /app/family/members/1`

#### 成功响应

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": null
}
```

#### 说明

当前为逻辑删除。


## 9. 常见错误码

当前 App 对接过程中常见错误码如下：

| 错误码 | 说明 |
| --- | --- |
| `0` | 操作成功 |
| `103` | 请求参数异常 |
| `106` | 用户未授权，通常是未登录或 token 丢失 |
| `107` | token 异常 |
| `108` | token 处理失败 |
| `12001` | 两次输入的密码不一致 |
| `12002` | 该手机号已被注册 |
| `12003` | 手机号或密码错误 |
| `12004` | 该 App 账号已被停用 |
| `12005` | App 用户不存在 |


## 10. 前端接入建议

### 10.1 登录态建议存储

建议前端至少缓存：

1. `token`
2. `userInfo`

### 10.2 请求封装建议

建议前端统一封装请求拦截器：

1. 自动拼接 `Authorization`
2. 当收到 `106`、`107`、`108` 时，统一跳转登录页
3. 统一提取 `data` 字段

### 10.3 家庭成员页建议流程

建议页面进入顺序：

1. 先调用登录或读取本地 token
2. 调用 `/app/user/current` 获取当前用户
3. 调用 `/app/family/members` 获取成员列表
4. 从成员详情进入后，再调用详情接口


## 11. 其他业务文档入口

当前健康业务的对接文档已按功能拆分到本目录下维护。

前端对接时建议优先阅读对应专题文档，而不是只依赖本总览文档。
