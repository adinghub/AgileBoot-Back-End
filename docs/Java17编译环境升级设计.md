# Java 17 编译环境升级设计

## 1. 背景

当前项目父 `pom.xml` 中的 `java.version` 仍然是 `1.8`，但业务代码已经开始使用 `Objects.requireNonNullElse(...)` 这类 Java 9 之后才提供的标准库方法。

这会导致开发人员在以下场景持续遇到编译错误：

1. 本地 IDE 仍按 Java 8 语言级别编译时，直接报 `java: 找不到符号`。
2. Maven 构建机仍沿用 Java 8 时，子模块会在编译阶段失败。
3. 团队成员环境不一致时，同一份代码在不同机器上表现不一致，排查成本较高。

## 2. 目标

本次调整的目标不是对单个 API 做临时兼容，而是统一项目的基础开发运行环境：

1. 将父 POM 的 Java 版本统一升级到 `17`。
2. 将 Maven 编译插件升级到可稳定支持 Java 17 的版本。
3. 通过 `release` 配置统一约束语法级别、字节码级别和标准库可见性。
4. 同步更新仓库说明文档，避免后续开发人员继续按 JDK 8 配置环境。

## 3. 设计方案

### 3.1 父 POM 统一升级

在根目录 [pom.xml](E:\code\health\healthtrail-back-end\pom.xml) 中：

1. 将 `<java.version>` 从 `1.8` 调整为 `17`。
2. 将 `maven-compiler-plugin` 版本从 `3.1` 升级为 `3.11.0`。

这样所有继承父 POM 的模块都会自动使用 Java 17 编译配置，不需要在每个子模块分别重复修改。

### 3.2 使用 `release` 代替 `source`/`target`

原来父 POM 使用的是：

1. `<source>${java.version}</source>`
2. `<target>${java.version}</target>`

本次改为：

1. `<release>${java.version}</release>`

这样做的原因是：

1. `release` 会同时约束语言特性和 JDK 标准库可见性。
2. 可以避免“字节码目标版本改了，但仍错误引用了不匹配 JDK API”的隐性问题。
3. 对团队协作和 CI 构建更稳定，行为更接近真实运行环境。

## 4. 影响范围

本次升级直接影响以下内容：

1. 根目录 [pom.xml](E:\code\health\healthtrail-back-end\pom.xml) 的全局编译配置。
2. [README.md](E:\code\health\healthtrail-back-end\README.md) 中开发环境说明和 JDK 徽标。

间接受影响的范围包括所有 Maven 子模块，因为它们共享父 POM 的编译规则。

## 5. 开发协作说明

为了让这次升级真正生效，开发人员本地环境也需要同步到 Java 17：

1. IDE 的 Project SDK / Module SDK 需要切换到 JDK 17。
2. Maven 运行时 JDK 也需要切换到 17，而不是只安装 JDK 17。
3. 如果本地存在缓存的旧 Maven 导入信息，建议重新刷新 Maven 项目。

否则即使 `pom.xml` 已经改成 17，IDE 仍可能继续用旧 JDK 编译，从而重复出现类似错误。

## 6. 风险与回退

### 6.1 风险

1. 如果部分开发机或构建机尚未安装 JDK 17，构建会立即失败。
2. 如果某些旧脚本、Docker 基础镜像或 CI 配置仍固定使用 JDK 8，需要同步调整。

### 6.2 回退方案

如果后续确认团队短期内不能统一到 Java 17，可以回退根 POM 的版本配置，并改为在业务代码中移除 Java 9+ API，用 Java 8 兼容写法替代。

但该方案会增加后续代码维护成本，因此当前优先推荐统一升级到 Java 17。
