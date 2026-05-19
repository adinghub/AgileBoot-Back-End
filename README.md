# HealthTrail Back End

HealthTrail 后端服务，面向家庭健康管理场景，提供管理后台接口、App 接口、健康业务领域服务和基础设施集成。

## 功能范围

- 家庭成员与家庭共享
- 体检报告、报告指标、报告解析与趋势分析
- 慢病档案与慢病类型配置
- 药品、个人药柜、库存预警、近效期提醒
- 用药计划、用药提醒、用药历史
- App 用户、设备登记、消息中心、推送派发审计
- 首页待跟进任务、运营任务、系统用户与权限管理

## 技术栈

- JDK 17
- Spring Boot 2.7.x
- Spring Security + JWT
- MyBatis Plus
- PostgreSQL / H2
- Redis
- Maven

## 模块结构

```text
healthtrail-back-end/
  healthtrail-admin/           管理后台接口模块
  healthtrail-api/             App / 开放接口模块
  healthtrail-common/          通用常量、工具、返回模型和基础类
  healthtrail-domain/          健康业务与系统业务领域模块
  healthtrail-infrastructure/  基础设施、缓存、安全、异常、日志和中间件集成
  sql/                         初始化脚本和增量脚本
```

## 本地启动

1. 安装 JDK 17、Maven、PostgreSQL 和 Redis。
2. 创建数据库，并按需导入 `sql` 目录下的初始化脚本和健康业务增量脚本。
3. 修改 `healthtrail-admin/src/main/resources/application-dev.yml` 中的数据库和 Redis 配置。
4. 在后端根目录执行：

```bash
mvn clean install
```

5. 启动管理后台：

```bash
mvn -pl healthtrail-admin -am spring-boot:run
```

6. 启动 App 接口服务：

```bash
mvn -pl healthtrail-api -am spring-boot:run
```

## 打包

```bash
mvn clean package
```

默认测试开关由父 POM 的 `skipTests` 属性控制。需要显式运行测试时可执行：

```bash
mvn test -DskipTests=false
```

## 开发说明

- 管理后台启动类：`com.healthtrail.admin.HealthTrailAdminApplication`
- App 接口启动类：`com.healthtrail.api.HealthTrailApiApplication`
- 管理后台 Swagger / OpenAPI 地址：`http://localhost:18011/v3/api-docs`
- 新增健康业务优先放入 `healthtrail-domain/src/main/java/com/healthtrail/domain/health`
- 新增后台接口优先放入 `healthtrail-admin/src/main/java/com/healthtrail/admin/controller/health`
- 新增 App 接口优先放入 `healthtrail-api/src/main/java/com/healthtrail/api/controller`

## 配套项目

- `../healthtrail-front-end`：Vue3 管理后台
- `../healthtrail`：Flutter App
- `../medication-reminder-push-demo`：用药提醒 Push 后端学习 Demo
- `../medication_reminder_push_demo_app`：用药提醒 Push Flutter 学习 Demo
