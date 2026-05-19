# App 登录态 30 天设计

## 背景

当前 App 登录态缓存使用的是 `APP_LOGIN_USER_KEY("app_login_tokens:", 30, TimeUnit.MINUTES)`。

这意味着：

- 用户持续活跃访问时，会因为滑动续期而保持在线；
- 但只要一段时间没有接口访问，登录缓存就可能在约 30 分钟后失效；
- 对移动端用户来说，这个空闲超时窗口明显偏短，容易出现“隔一会儿回来就要重新登录”的体验。

相比之下，普通消费型 App 的用户预期通常是：

- 登录后可以维持较长时间；
- 除非主动退出、切换账号、服务端风控、缓存被清理，否则不应频繁要求重新登录。

## 目标

将 App 端登录态缓存从 30 分钟调整为 30 天。

注意这里调整的是：

- App 端 Redis 登录态缓存的有效期

不调整的是：

- 后台管理端登录态时长
- token header / secret 规则
- 主动退出登录逻辑
- token 失效后的未授权处理逻辑

## 实现方案

### 1. 单独放宽 App 登录缓存

在 `CacheKeyEnum` 中将：

- `APP_LOGIN_USER_KEY("app_login_tokens:", 30, TimeUnit.MINUTES)`

调整为：

- `APP_LOGIN_USER_KEY("app_login_tokens:", 30, TimeUnit.DAYS)`

这样只影响 App 用户，不影响后台管理端的登录态策略。

### 2. 修正本地 Guava 缓存时间单位

`RedisCacheTemplate` 原先在本地 Guava 层使用了：

- `.expireAfterWrite(redisRedisEnum.expiration(), TimeUnit.MINUTES)`

这会造成一个隐藏问题：

- 即使 Redis 主缓存已经改成“30 天”，本地 Guava 层仍会按“30 分钟”先过期；
- 虽然数据最终还能从 Redis 再读回来，但缓存语义前后不一致，容易让后续维护者误判。

因此本次同步调整为：

- `.expireAfterWrite(redisRedisEnum.expiration(), redisRedisEnum.timeUnit())`

让本地缓存与 Redis 主缓存保持同一时间单位。

## 结果影响

调整后，App 登录体验会更接近常见移动应用：

- 用户短时间不打开 App，不会轻易被要求重新登录；
- 用户仍然可以通过“退出登录”立即失效当前会话；
- 如果 Redis 缓存被删除、重置，或服务端主动清理登录态，当前 token 仍会失效。

也就是说，本次并不是把登录态改成“永久有效”，而是把“纯空闲超时”从 30 分钟放宽到 30 天。

## 影响范围

- `healthtrail-infrastructure/src/main/java/com/healthtrail/infrastructure/cache/redis/CacheKeyEnum.java`
- `healthtrail-infrastructure/src/main/java/com/healthtrail/infrastructure/cache/redis/RedisCacheTemplate.java`
