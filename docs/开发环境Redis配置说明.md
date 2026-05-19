# 开发环境 Redis 配置说明

## 1. 目的

为了让 `healthtrail-admin` 与 `healthtrail-api` 在开发环境联调时使用同一份缓存数据，
本次将两个启动模块的 `dev` profile Redis 配置统一收口到共享开发 Redis。

这样可以减少以下问题：

1. 管理后台和开放 API 分别连本地 Redis，导致登录态、验证码、缓存数据不一致
2. 某一侧改了缓存后另一侧看不到，联调问题难以复现
3. 之前遗留的本地示例端口 `36379` 和共享开发环境 `6379` 混用，造成开发人员判断困难


## 2. 当前开发环境配置

共享开发 Redis：

1. `host: 192.168.193.27`
2. `port: 6379`
3. `database: 2`
4. `password: 空`


## 3. 配置落点

本次配置统一写入以下文件：

1. `healthtrail-admin/src/main/resources/application-dev.yml`
2. `healthtrail-api/src/main/resources/application-dev.yml`


## 4. 约定说明

1. `dev` 环境统一使用 `database: 2`，便于和测试、预生产、生产或本地实验库隔离
2. 当前仓库中的开发环境 Redis 按无密码处理，因此 `password` 显式留空
3. 如果后续开发环境 Redis 启用了认证，建议通过外部配置或环境变量覆盖，不直接把真实密码提交到仓库
