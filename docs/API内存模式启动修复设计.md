# API 内存模式启动修复设计

## 1. 背景

当前工程同时存在两个独立启动入口：

1. `healthtrail-admin`
2. `healthtrail-api`

此前 `test` 内存模式的 H2 兼容方案，已经在 `admin` 模块和领域测试资源中落地，但 API 模块作为独立启动入口，并没有自己的 `application-test.yml`。

这会导致一个很隐蔽的问题：

1. 开发人员在 IDEA 中直接运行 `HealthTrailApiApplication`
2. Spring 仍然显示激活了 `basic,test`
3. 但 API 运行时 classpath 里没有属于 API 启动入口自己的 test 数据源配置
4. dynamic-datasource 最终加载到 0 个数据源
5. 应用虽然能先启动起来，但提醒定时任务第一次访问数据库时，就会抛出 `dynamic-datasource can not find primary datasource`

## 2. 问题根因

根因不是 H2 脚本缺失，而是 **Profile 配置文件的归属模块不对**。

本次日志已经给出两个直接信号：

1. `dynamic-datasource initial loaded [0] datasource`
2. 随后定时任务执行数据库查询时报 `CannotFindDataSourceException`

说明：

1. H2 数据源压根没有创建成功
2. 失败发生在 SQL 执行之前
3. 因此优先修复点应该是 API 模块的 `application-test.yml` 装配，而不是 Mapper 或 SQL 本身

## 3. 设计目标

本次修复目标如下：

1. 让 `HealthTrailApiApplication` 在 `basic,test` 下可以独立以 H2 内存库启动
2. 继续复用 infrastructure 模块现有的 H2 兼容脚本，不重新造一套脚本
3. 让用药提醒相关定时任务在 test 模式下能够正常访问数据库
4. 为 API 启动入口补一条专属回归测试，避免以后再次出现“admin 能跑、api 不能跑”的分叉问题

## 4. 方案设计

### 4.1 在 API 模块补充独立的 test Profile 文件

新增文件：

1. `healthtrail-api/src/main/resources/application-test.yml`

核心设计点：

1. 把 H2 内存数据源直接配置到 API 启动模块自己的 main resources
2. 保持 dynamic-datasource 的 `primary=master`
3. 继续引用 `h2sql/healthtrail_test_schema.sql`
4. 继续引用 `h2sql/health_test_schema.sql`
5. 继续引用对应的种子数据脚本
6. 显式声明 `spring.sql.init.mode=always`，避免路由数据源包装后影响 Spring 对嵌入式数据库的自动识别

### 4.2 补充 API 侧回归测试

新增文件：

1. `healthtrail-api/src/test/java/com/healthtrail/api/config/ApiTestProfileH2InitializationTest.java`

测试关注点：

1. API 上下文能在 `basic,test` 下启动
2. `sys_user`、`sys_menu` 等基础表存在
3. `health_drug`、`health_medication_plan`、`health_medication_reminder` 等提醒链路关键表存在
4. 至少有一部分基础种子数据和健康种子数据被正确加载

此外，测试中把提醒调度 cron 改成极低触发频率，避免单测执行时被定时任务插入噪音。

### 4.3 补齐数据库异常国际化键

补充：

1. `Internal.DB_INTERNAL_ERROR=数据库异常`

这样即使未来再次出现数据库问题，日志也能直接给出有效中文错误信息，而不是先报“找不到 i18n key”。

## 5. 为什么不直接复用 admin 的 application-test.yml

因为 `healthtrail-admin` 和 `healthtrail-api` 是两个独立启动模块。

在 IDEA 或 Maven 以 `HealthTrailApiApplication` 为入口启动时，开发人员真正依赖的是 API 模块自己的运行时 classpath，而不是“仓库里某个别的模块也正好存在同名 profile 文件”。

换句话说：

1. `admin` 里的 test 配置能够保证 `admin` 自己启动
2. 但它不能天然保证 `api` 的启动链路也完整

所以这次修复不把问题继续寄托在模块间的隐式资源共享上，而是把 API 启动所需的 test 配置显式放回 API 模块自己名下。

## 6. 维护约束

后续如果继续维护 test 内存模式，需要遵循下面约束：

1. 任何独立启动模块，只要支持 `test` Profile，都要在自己的启动资源中保证 profile 配置可达
2. H2 兼容脚本新增表、字段或种子数据时，API/ADMIN 的启动回归测试都要同步验证
3. 如果未来准备抽成共享配置文件，也要先确认 Spring Boot 在多模块 classpath 下的加载顺序可控，再做统一

## 7. 预期结果

修复完成后，开发人员再次以 `HealthTrailApiApplication` 启动 `basic,test` 时，应满足：

1. dynamic-datasource 能加载到 `master` 主数据源
2. H2 脚本会在启动期完成初始化
3. 用药提醒定时任务不再因为“找不到主数据源”报错
4. API 启动入口拥有自己的回归测试保护网

