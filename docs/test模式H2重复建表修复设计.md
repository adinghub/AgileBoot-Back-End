# test模式H2重复建表修复设计

## 1. 问题背景

`test` 模式下，系统使用 H2 内存数据库加载以下初始化脚本：

1. `classpath:h2sql/healthtrail_test_schema.sql`
2. `classpath:h2sql/health_test_schema.sql`
3. `classpath:h2sql/healthtrail_test_data.sql`
4. `classpath:h2sql/health_test_data.sql`

当前 `healthtrail-api`、`healthtrail-admin`、`healthtrail-domain` 三处测试配置都把数据源 URL 写成固定值：

```yaml
jdbc:h2:mem:healthtrail;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
```

当多个测试类、多个模块启动入口，或者同一 JVM 中多次创建 Spring 上下文时，会共同命中同一个 H2 内存库 `healthtrail`。

同时，`DB_CLOSE_DELAY=-1` 会让最后一个连接关闭后，数据库对象依旧保留到 JVM 退出。这样第二次初始化时，Spring 还会再次执行 `healthtrail_test_schema.sql`，于是第一条 `CREATE TABLE sys_config` 就直接失败，报错为：

```text
Table "sys_config" already exists
```

## 2. 根因分析

根因不是 `sys_config` 表结构有误，而是测试库生命周期和上下文生命周期不一致：

1. Spring 上下文关闭了，但固定库名对应的 H2 内存库没有销毁。
2. 下一次测试或启动沿用相同 URL，再次触发 `spring.sql.init`。
3. schema 脚本不是“重复执行安全”的脚本，因此第一条建表语句立即冲突。

这个问题本质上属于“测试隔离失败”，不是单表问题，后续还可能扩散到插入种子数据、重置自增列等步骤。

## 3. 设计目标

本次修复目标如下：

1. 保证每个 `test` Spring 上下文使用独立的 H2 内存库。
2. 保持现有 H2 schema/data 脚本不需要大规模改写。
3. 保持 API、Admin、Domain 三个入口的测试初始化策略一致。
4. 继续显式启用 `spring.sql.init.mode=always`，避免动态数据源包装后初始化行为不稳定。

## 4. 修复方案

### 4.1 数据源 URL 改为上下文唯一

将固定库名：

```yaml
jdbc:h2:mem:healthtrail;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
```

分别改成：

```yaml
jdbc:h2:mem:healthtrail_api_${random.uuid};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
jdbc:h2:mem:healthtrail_admin_${random.uuid};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
jdbc:h2:mem:healthtrail_domain_${random.uuid};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
```

这样每次 Spring 创建上下文时，都会解析出新的 H2 内存库名：

1. 同模块不同测试上下文不会再复用旧库。
2. 不同模块在同一 JVM 中也不会互相污染。
3. 即使保留 `DB_CLOSE_DELAY=-1`，旧库也只会残留在自己的随机名字空间里，不会阻塞下一次初始化。

### 4.2 显式保留 `spring.sql.init.mode=always`

`healthtrail-api` 已显式开启该配置，`healthtrail-admin` 和 `healthtrail-domain` 本次一并补齐。

原因是当前项目使用了动态数据源封装，Spring 对“是否为嵌入式数据库”的自动识别并不总是稳定。显式配置 `always` 可以把测试初始化行为固定下来，减少不同模块表现不一致的问题。

## 5. 为什么不优先改 SQL 脚本幂等

备选方案是把所有 `CREATE TABLE` 改成 `CREATE TABLE IF NOT EXISTS`，并把所有 `INSERT` 改成 `MERGE` 或其他幂等写法。

这次没有优先采用该方案，原因如下：

1. 现有测试脚本规模较大，批量改写 DDL/DML 成本高，且容易引入新的 H2 兼容差异。
2. 报错根因是“测试上下文共享数据库”，先修复隔离边界更直接，也更符合测试环境设计原则。
3. 即使脚本做成幂等，多个测试上下文共享同一数据库仍然可能造成数据串扰，影响断言稳定性。

因此本次优先修复“库实例隔离”，这是更底层、更可靠的方案。

## 6. 影响范围

本次仅调整以下文件中的测试数据源配置，不涉及业务表结构和业务代码：

1. `healthtrail-api/src/main/resources/application-test.yml`
2. `healthtrail-admin/src/main/resources/application-test.yml`
3. `healthtrail-domain/src/test/resources/application-test.yml`

## 7. 预期结果

修复后：

1. `test` 模式下重复启动应用上下文，不再因为 `sys_config` 等表已存在而失败。
2. 各模块测试之间的 H2 数据互不污染。
3. H2 初始化脚本仍然保持现有结构，维护成本最小。
