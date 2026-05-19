# App 刷新令牌设计

## 背景

此前 App 登录态只有一张 access token，服务端再配合 Redis 登录缓存维持会话。

这套方案存在两个问题：

1. 如果 access token 或登录缓存失效，客户端只能直接回到登录页；
2. 即使我们已经把 App 登录缓存放宽到 30 天，仍然缺少一条“无感续签”的标准链路。

对移动端来说，用户通常预期是：

- 登录后长期可用；
- access token 失效时尽量静默恢复；
- 只有 refresh token 真正失效、主动退出、服务端踢下线时，才要求重新登录。

## 目标

将 App 认证链路升级为标准双令牌模型：

- access token：用于日常接口访问，短周期有效
- refresh token：用于 access token 失效后的静默续签，长周期有效

并满足以下规则：

1. 登录或注册成功时，同时签发新的 access token 和 refresh token；
2. refresh token 每次登录都会续期并轮换；
3. refresh 成功后，旧 access 会话和旧 refresh 会话同时失效；
4. 客户端在业务接口收到未授权后，先尝试 refresh，再决定是否打回登录页。

## 后端设计

### 1. JWT 增加 token_scene 区分 access / refresh

为了避免 refresh token 被错误拿去访问业务接口，JWT claims 中增加：

- `token_scene=access`
- `token_scene=refresh`

同时保留各自独立的 key：

- access token 使用 `login_user_key`
- refresh token 使用 `refresh_token_key`

### 2. Redis 增加 refresh 会话缓存

新增 `APP_REFRESH_TOKEN_KEY("app_refresh_tokens:", 30, TimeUnit.DAYS)`。

refresh token 除了自身 JWT 过期时间外，还会在 Redis 中保留一份可主动删除的服务端会话，内容包括：

- `userId`
- `refreshTokenKey`
- `accessCachedKey`

这样 refresh 成功时，后端可以先删除旧 access 登录缓存，再删除旧 refresh 会话，最后签发新的令牌对，形成完整轮换。

### 3. access token 与 refresh token 的职责

- access token：
  - 用在 `Authorization` 请求头
  - 默认 2 小时 JWT 过期
  - 仍可配合 Redis 登录缓存做滑动续期

- refresh token：
  - 只用于 `/common/app/auth/refresh`
  - 默认 30 天 JWT 过期
  - Redis refresh 会话同样保持 30 天

### 4. 登录、刷新、退出行为

#### 登录 / 注册

- 生成新的 access token
- 生成新的 refresh token
- 返回给客户端

#### refresh

- 校验 refresh token JWT
- 校验 Redis refresh 会话
- 删除旧 access 缓存
- 删除旧 refresh 会话
- 重新签发新的 access / refresh 令牌对

#### logout

- 删除当前 access 登录缓存
- 删除当前 access 绑定的 refresh 会话

## 客户端设计

### 1. 本地持久化 refresh token

App 端在原有：

- `authToken`
- `authTokenType`

基础上，新增：

- `refreshToken`

### 2. 业务请求收到未授权后的处理

App 网络层在业务接口收到 `106/107/108` 时，不再立刻清空登录态，而是先判断：

- 当前请求是否允许尝试 refresh
- 本地是否存在 refresh token

如果满足条件，则：

1. 调用 `/common/app/auth/refresh`
2. 用返回的新 access / refresh token 覆盖本地缓存
3. 自动重试原请求一次

只有 refresh 也失败时，才真正清空登录态并要求重新登录。

### 3. 并发 refresh 收口

为了避免多个接口同时收到未授权后并发打出多次 refresh，请求侧增加单飞控制：

- 第一个请求负责真正发起 refresh
- 其他并发请求等待同一个 refresh Future 结果

这样可以减少重复 refresh 和令牌被反复覆盖的问题。

## 影响范围

### 后端

- `healthtrail-api/.../JwtTokenService.java`
- `healthtrail-api/.../AppLoginService.java`
- `healthtrail-api/.../LoginController.java`
- `healthtrail-domain/.../AppLoginDTO.java`
- `healthtrail-domain/.../AppRefreshTokenCommand.java`
- `healthtrail-domain/.../RedisCacheService.java`
- `healthtrail-infrastructure/.../CacheKeyEnum.java`
- `healthtrail-infrastructure/.../AppLoginUser.java`
- `healthtrail-infrastructure/.../AppRefreshTokenSession.java`
- `healthtrail-common/.../Constants.java`

### App

- `lib/features/auth/app_auth_controller.dart`
- `lib/features/auth/data/app_auth_repository.dart`
- `lib/features/auth/models/auth_result.dart`
- `lib/core/network/app_api_client.dart`
- `lib/core/network/dio_client.dart`
- `lib/core/constants/app_storage_keys.dart`

## 结果

升级完成后，App 登录体验会更接近成熟移动应用：

- access token 失效时优先静默续签；
- refresh token 每次登录都会续期；
- 真正需要用户重新登录的时机被收敛到 refresh token 失效、主动退出、服务端失效会话等少数场景。
