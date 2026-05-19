# HealthTrail命名规范与改造设计

## 1. 背景

当前后端仓库已经完成核心工程标识收口，但在资源脚本、说明文档、本地初始化脚本和历史种子数据中，仍然存在若干旧脚手架命名残留。

这些残留虽然不一定会直接阻塞编译，却会持续制造三类问题：

1. 新同事检索仓库时，很难判断哪些名字才是当前正式口径。
2. 初始化数据库、跑测试脚本、复制说明文档时，容易把旧名字再次带回新环境。
3. 仓库对外展示的品牌已经是 `HealthTrail`，但技术资源名继续混用旧标识，会降低整体一致性。

## 2. 目标

本轮命名规范收口的目标是：

1. 对外展示名统一为 `HealthTrail`
2. 本地工程/资源路径统一为 `healthtrail*`
3. Java 包统一为 `com.healthtrail.*`
4. H2 / PostgreSQL 基础资源脚本统一为 `healthtrail_*.sql`
5. README、设计文档、脚本注释中的旧脚手架文本全部清理

同时保留两个明确边界：

1. 真实历史远程仓库 URL 不改
2. 演示域名不改

## 3. 命名原则

### 3.1 展示名与技术名分层

1. 展示给开发人员、用户、协作者的品牌统一使用 `HealthTrail`
2. 真正参与路径、包名、artifactId、脚本文件名的技术标识统一使用 `healthtrail`

### 3.2 资源脚本与运行代码保持同口径

如果代码、配置和仓库目录都已经收口到 `healthtrail`，那么测试初始化脚本、SQL 总脚本、组合脚本也必须同步改名。否则后续维护时依旧会出现“代码是新名，脚本是旧名”的分叉。

### 3.3 历史地址保留，但只保留地址本身

远程仓库真实 URL、演示域名属于外部事实，不适合在这轮本地命名收口中强改。

因此：

1. URL 本身保留
2. 文档里的链接标题、说明文字统一改成 HealthTrail 语义

## 4. 本轮范围

### 4.1 已纳入

1. Maven 根工程与模块标识
2. Java 包名与启动类品牌名
3. 本地仓库目录名
4. H2 / PostgreSQL 基础 schema、data、test 脚本文件名
5. SQL 合并脚本输出名与注释
6. README、设计文档、初始化说明中的旧脚手架文本
7. 历史种子数据中的品牌名、示例邮箱名

### 4.2 明确保留

1. 真实历史远程仓库 URL
2. 演示站点域名
3. 业务数据库中的既有业务表语义

## 5. 最终口径

本仓库最终统一采用以下命名：

1. 品牌：`HealthTrail`
2. Maven 根坐标：`com.healthtrail:healthtrail`
3. 子模块：`healthtrail-admin / healthtrail-api / healthtrail-common / healthtrail-domain / healthtrail-infrastructure`
4. Java 包：`com.healthtrail.*`
5. 基础资源脚本：`healthtrail_schema.sql`、`healthtrail_data.sql`、`healthtrail_test_schema.sql`、`healthtrail_test_data.sql`

## 6. 验证要求

改造完成后至少需要确认：

1. `mvn -q -DskipTests compile` 可以通过
2. `application-test.yml` 中引用的 H2 初始化路径全部指向 `healthtrail_*.sql`
3. 全仓再次搜索旧脚手架命名时，仅剩真实远程仓库 URL 或演示域名
