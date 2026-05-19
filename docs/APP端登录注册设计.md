# APP端登录注册设计

## 1. 设计目标

本次设计和实现只聚焦健康系统一期最基础的 App 账户能力，目标是先把 App 用户体系独立建立起来，为后续家庭成员、用药提醒、体检报告等业务提供统一的用户入口。

本次范围包括：

1. App 用户注册
2. App 用户登录
3. 获取当前登录用户
4. App token 认证链路
5. App 用户表设计


## 2. 设计原则

### 2.1 App 用户与后台用户彻底分离

当前项目中：

1. `healthtrail-admin` 是后台管理端入口
2. `healthtrail-api` 是 App 对外接口入口

因此本次实现不复用后台 `sys_user` 表，而是新增 `health_app_user` 表，避免出现以下问题：

1. 后台角色、部门、岗位等概念污染 App 用户模型
2. App 用户和后台用户共用缓存键时产生串号风险
3. 后续健康业务扩展时被后台用户体系限制

### 2.2 认证链路复用现有脚手架思路

虽然 App 用户独立建模，但认证方式仍然沿用项目现有成熟思路：

1. JWT 只存缓存主键
2. Redis 存完整登录用户对象
3. Filter 负责恢复当前登录用户上下文
4. Controller 中不重复手写 token 解析逻辑

这样做的好处是：

1. 与现有脚手架风格一致
2. 后续更容易扩展强制下线、续期、会员状态刷新
3. 开发人员更容易维护和理解


## 3. 接口设计

### 3.1 注册接口

#### 请求路径

`POST /common/app/auth/register`

#### 请求参数

1. `mobile` 手机号
2. `nickname` 昵称，可为空
3. `password` 密码
4. `confirmPassword` 确认密码

#### 处理逻辑

1. 校验手机号格式
2. 校验密码长度
3. 校验两次密码输入一致
4. 校验手机号唯一
5. 自动补全默认昵称
6. 使用 BCrypt 加密密码
7. 创建 App 用户
8. 注册成功后直接返回登录态

### 3.2 登录接口

#### 请求路径

`POST /common/app/auth/login`

#### 请求参数

1. `mobile` 手机号
2. `password` 密码

#### 处理逻辑

1. 根据手机号查询 App 用户
2. 校验用户是否存在
3. 校验密码是否正确
4. 校验账号状态是否可用
5. 更新最后登录时间和登录 IP
6. 写入 App 登录缓存
7. 生成 JWT token

### 3.3 获取当前登录用户接口

#### 请求路径

`GET /app/user/current`

#### 处理逻辑

1. 由 JWT 过滤器恢复当前 App 登录用户
2. 从认证上下文中读取当前用户 ID
3. 返回当前用户基本资料


## 4. 返回结构设计

注册和登录统一返回以下结构：

1. `token` JWT token
2. `tokenType` token 类型，当前为 `Bearer`
3. `userInfo` 当前登录用户信息

这样前端 App 在注册成功后无需再额外发起一次登录请求，能够直接进入首页。


## 5. 数据库设计

### 5.1 表名

`health_app_user`

### 5.2 核心字段

1. `user_id` App 用户 ID
2. `mobile` 手机号，唯一约束
3. `nickname` 昵称
4. `avatar` 头像
5. `password` 加密后的密码
6. `status` 状态
7. `last_login_ip` 最后登录 IP
8. `last_login_time` 最后登录时间
9. `register_source` 注册来源

### 5.3 设计说明

1. 主键字段沿用 `user_id`，便于和当前脚手架的登录态模型衔接
2. `mobile` 设置唯一索引，保证注册幂等约束
3. `register_source` 当前默认写 `APP`，后续可扩展为 `WECHAT_APP`、`H5` 等来源
4. 表保留 `BaseEntity` 需要的审计字段和逻辑删除字段，方便后续统一维护


## 6. 后端落地结构

### 6.1 `healthtrail-api`

本次新增：

1. `AppLoginService`
2. `LoginController`
3. `AppController`
4. App 端专属 `JwtTokenService`
5. App 端专属 `JwtAuthenticationFilter`

### 6.2 `healthtrail-domain`

本次新增：

1. `com.healthtrail.domain.health.user`
2. `command`
3. `dto`
4. `db`
5. `model`
6. `AppUserApplicationService`

### 6.3 `Redis` 缓存隔离

为了避免和后台管理端登录态互相影响，本次新增了 App 专属缓存前缀：

`app_login_tokens:`


## 7. 关键业务规则

### 7.1 注册成功后自动登录

这样能减少 App 端多一步接口调用，也更符合移动端注册体验。

### 7.2 不复用后台用户表

这是本次设计里最重要的边界，后续健康系统业务都应该围绕 App 用户体系继续扩展。

### 7.3 token 采用滑动续期

只要用户持续访问 App 接口，Redis 中的登录态会自动刷新有效期，降低频繁重新登录的概率。


## 8. 后续建议

基于当前登录注册能力，下一步建议按下面顺序继续推进：

1. App 用户注销登录
2. 忘记密码与短信验证码
3. 家庭成员管理
4. 用药提醒
5. 体检报告上传

