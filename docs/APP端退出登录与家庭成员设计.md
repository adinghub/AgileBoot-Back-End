# APP端退出登录与家庭成员设计

## 1. 功能范围

本次在 App 登录注册完成后，继续补齐以下两个能力：

1. App 用户退出登录
2. 家庭成员管理

这两个能力是健康系统继续往下发展的关键基础。

原因如下：

1. 退出登录是完整账户体系必备能力
2. 家庭成员是后续用药提醒、体检报告、健康档案的核心归属对象


## 2. App 退出登录设计

### 2.1 接口路径

`POST /app/auth/logout`

### 2.2 设计思路

当前 App 端采用的是：

1. JWT 保存登录缓存主键
2. Redis 保存完整登录用户信息

因此退出登录时，不需要维护复杂的 token 黑名单，一期只需要删除 Redis 中的 App 登录缓存即可。

这样处理后，即使客户端本地仍然保存着 JWT：

1. 后端也无法再从 Redis 中恢复出有效登录态
2. 后续请求会被识别为未授权

### 2.3 优点

1. 实现简单
2. 与现有脚手架后台登录设计思路一致
3. 后续如果需要设备管理或强制下线，也容易扩展


## 3. 家庭成员模块设计

### 3.1 模块定位

家庭成员不是简单的“联系人”功能，而是整个健康系统的核心业务主体。

后续以下数据都建议挂到家庭成员维度：

1. 体检报告
2. 用药计划
3. 用药提醒记录
4. 健康档案
5. 慢病记录

### 3.2 一期支持能力

一期先做最基础可用的 CRUD：

1. 家庭成员列表
2. 家庭成员详情
3. 新增家庭成员
4. 修改家庭成员
5. 删除家庭成员

### 3.3 接口设计

#### 列表

`GET /app/family/members`

#### 详情

`GET /app/family/members/{memberId}`

#### 新增

`POST /app/family/members`

#### 修改

`PUT /app/family/members/{memberId}`

#### 删除

`DELETE /app/family/members/{memberId}`


## 4. 权限边界设计

### 4.1 只允许操作自己的家庭成员

这是 App 端家庭成员模块最重要的权限规则。

无论是查看、修改还是删除，都必须校验：

1. 当前 `memberId` 是否存在
2. 该成员是否归属于当前登录用户

如果不属于当前用户，则直接按“对象不存在”处理，不暴露他人数据。

### 4.2 为什么不直接信任前端传的 ownerUserId

因为 `ownerUserId` 是强安全边界字段，必须完全由后端通过登录态确定，不能相信前端传值。

因此本次设计中：

1. Controller 每次都从认证上下文读取当前 App 用户ID
2. 再将该用户ID注入查询或命令流程


## 5. 数据库设计

### 5.1 表名

`health_family_member`

### 5.2 核心字段

1. `member_id` 家庭成员ID
2. `owner_user_id` 归属App用户ID
3. `member_name` 成员姓名
4. `gender` 性别
5. `birthday` 生日
6. `relation_type` 关系类型
7. `height` 身高
8. `weight` 体重
9. `blood_type` 血型
10. `allergy_history` 过敏史
11. `chronic_history` 慢病史
12. `remark` 备注
13. `status` 状态

### 5.3 设计说明

1. `owner_user_id` 用于表达该成员归属于哪个 App 账户
2. `relation_type` 一期先用字符串存储，便于前端灵活展示
3. `status` 统一沿用项目现有状态值语义：`1正常`、`0停用`
4. 表中仍保留审计字段和逻辑删除字段，便于后续扩展


## 6. 领域分层设计

### 6.1 `healthtrail-domain`

本次新增家庭成员业务目录：

`com.healthtrail.domain.health.family`

目录结构继续遵循项目既有风格：

1. `command`
2. `query`
3. `dto`
4. `db`
5. `model`
6. `FamilyMemberApplicationService`

### 6.2 `api` 模块

本次新增：

1. `FamilyMemberController`
2. `AppAuthController`


## 7. 审计字段填充设计

当前项目原本主要服务后台管理端，因此自动填充 `creatorId`、`updaterId` 时优先读取后台登录用户。

为了让 App 端新增和修改家庭成员时也能自动记录操作者，本次同时补充了：

1. 后台用户读取失败时，继续尝试读取 App 登录用户

这样后续 App 业务的审计字段就能正常落库。


## 8. 后续建议

在当前退出登录和家庭成员能力完成后，建议按下面顺序继续推进：

1. 家庭成员头像与健康档案补充
2. 用药提醒计划
3. 用药提醒记录
4. 体检报告上传
5. 体检报告分析

