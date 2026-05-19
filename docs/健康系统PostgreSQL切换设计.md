# 健康系统 PostgreSQL 切换设计

## 1. 设计目标

当前项目在“尚无正式业务数据”的前提下，直接把数据库基线从 MySQL 切换为 PostgreSQL，目标如下：

1. 统一运行时默认数据源为 PostgreSQL
2. 消除代码中的主要 MySQL 方言依赖
3. 提供可直接落地的 PostgreSQL 初始化脚本
4. 保持 App、后台管理前端接口协议不变，只调整持久化层实现

## 2. 本次已落地改动

### 2.1 运行时配置

已改动默认配置为 PostgreSQL：

1. `healthtrail-admin/src/main/resources/application-dev.yml`
2. `healthtrail-admin/src/main/resources/application-test.yml`
3. `healthtrail-domain/src/test/resources/application-test.yml`
4. `healthtrail-infrastructure/src/main/resources/application-basic.yml`

核心变化：

1. 驱动从 MySQL 改为 PostgreSQL
2. 默认连接串改为 `jdbc:postgresql://localhost:5432/healthtrail`
3. PageHelper 方言改为 `postgresql`
4. H2 测试模式改为 `MODE=PostgreSQL`
5. 测试初始化脚本切换到 `classpath:pgsql/*`

### 2.2 依赖切换

已把数据库驱动依赖从 MySQL 替换为 PostgreSQL：

1. `pom.xml`
2. `healthtrail-infrastructure/pom.xml`

### 2.3 MyBatis 方言切换

已把 MyBatisPlus 分页方言从 `DbType.MYSQL` 切换到 `DbType.POSTGRE_SQL`：

1. `healthtrail-infrastructure/src/main/java/com/healthtrail/infrastructure/config/MyBatisConfig.java`

### 2.4 代码生成器切换

已把代码生成器默认元数据查询和类型映射从 MySQL 切到 PostgreSQL：

1. `healthtrail-infrastructure/src/main/java/com/healthtrail/infrastructure/mybatisplus/CodeGenerator.java`

### 2.5 MySQL 专有 SQL 方言替换

已替换两类关键 MySQL 方言：

1. `FIND_IN_SET`
2. 反引号字段名

涉及文件包括：

1. `healthtrail-domain/src/main/java/com/healthtrail/domain/system/user/query/SearchUserQuery.java`
2. `healthtrail-domain/src/main/java/com/healthtrail/domain/system/dept/db/SysDeptServiceImpl.java`
3. 多个 Entity 中的 `@TableField("`status`")`、`@TableField("`password`")`
4. `healthtrail-domain/src/main/java/com/healthtrail/domain/health/family/query/HealthFamilyMemberAdminQuery.java`

## 3. 为什么这次切换是可控的

### 3.1 当前没有正式业务数据

因为当前没有需要保留的线上业务数据，所以本次不需要做高风险的数据迁移，仅需要：

1. 固定新库标准
2. 重建初始化脚本
3. 校正运行时方言

### 3.2 健康系统表结构相对清晰

当前健康系统表多数为标准主数据和日志表，PostgreSQL 化主要是：

1. 把 `AUTO_INCREMENT` 改为 `BIGSERIAL`
2. 把 MySQL 风格脚本改成 PostgreSQL 语法
3. 统一直接使用 PostgreSQL 默认 schema，不再维护额外 `app` schema

### 3.3 前后端协议不受影响

本次切换不会改变：

1. App 接口路径
2. 后台接口路径
3. DTO / JSON 返回结构
4. 前端页面路由和交互

因此切换影响主要集中在后端持久化层和初始化体系。

## 4. 当前新的 PostgreSQL 初始化入口

### 4.1 基础库

1. `healthtrail-infrastructure/src/main/resources/pgsql/healthtrail_schema.sql`
2. `healthtrail-infrastructure/src/main/resources/pgsql/healthtrail_data.sql`

### 4.2 健康业务库

1. `sql/pgsql/健康系统_schema_20260423.sql`

### 4.3 后台菜单

1. `sql/pgsql/健康系统_后台管理菜单权限_20260423.sql`

## 5. 当前边界

本次切换已完成“以 PostgreSQL 为默认基线”的代码和脚本落地，但仍有几个边界需要明确：

1. 仓库中历史 MySQL SQL 文件仍然保留，当前不建议再用于新环境
2. 如果后续真的需要兼容双数据库，还需要额外做更系统的方言抽象
3. 当前没有做 MySQL 存量数据自动迁移脚本，因为本次前提是不保留旧业务数据

## 6. 后续建议

如果后续继续沿 PostgreSQL 路线推进，建议下一步补齐：

1. 本地 `docker-compose.yml` 的 PostgreSQL 开发环境
2. 健康模块 PostgreSQL 初始化的自动化执行脚本
3. CI 中至少一次 PostgreSQL 真实实例编译 + 启动校验

