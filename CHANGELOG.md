# 更新日志

所有项目的显著变更都将记录在此文件中。

格式基于[Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循[语义化版本](https://semver.org/lang/zh-CN/)。

---

## [v0.0.1-alpha.5] - 2026-06-10

> 项目统一命名为 LuminCore，优化 PR 构建策略，完善 README 文档。

### 新增 (Added)

- 完善 CNB 流水线安全构建与签名验证
- 恢复 PR 构建测试并添加 `ifModify` 文件变更过滤条件，避免不必要的构建资源浪费
- 新增 README.md 项目文件目录结构

### 修复 (Fixed)

- 修复 GitHub Actions 构建中 `apksigner` 命令未找到的问题
- 修复当前步骤中 `apksigner` 命令未找到的问题

### 变更 (Changed)

- 统一项目命名为 LuminCore，修复文档中的命名不一致
- 统一源码和文档中项目命名为 LuminCore
- 统一源码中 MyApplication 主题命名为 LuminCore
- 更新 README 文档，修正版本信息与技术栈描述，补充已实现功能清单
- 将 README 中 APP 上架计划时间从 2026 年修改为 2028 年

### CI/CD

- 移除同步仓库时的强制推送参数
- 移除 main 分支 push 构建，新增 PR 构建测试
- 移除构建流水线中冗余的 Gradle Wrapper 校验步骤
- 同步至 Gitcode 和 Gitee 时启用强制推送
- 移除 PR 构建测试触发器，避免每个 PR 都触发构建浪费资源

---

## [v0.0.1-alpha.4] - 2026-06-03

> 升级构建发布流水线，添加 APK 签名校验和中文 Release 说明，同步最新 APP 功能源码。

### 新增 (Added)

- 从 LuminCore 同步最新 APP 功能源码
- 升级 GitHub Actions 构建发布流水线，添加 APK 签名校验和中文 Release 说明

### 修复 (Fixed)

- 精简 Release 说明仅保留用户可见的 feat/fix 变更

### 变更 (Changed)

- 更新代码同步工作流推送分支为 main

### 杂项 (Chores)

- 移除同步到 codeup.aliyun.com 和 codehub.devcloud.cn-north-4.huaweicloud.com 的工作流

---

## [v0.0.1-alpha.3] - 2026-05-31

> 完善多平台代码同步流水线，修复 GitHub Actions 构建与发布流程中的多项问题。

### 新增 (Added)

- 在 GitHub Actions 构建流程中新增 CNB 流水线同款密钥库下载流程
- 添加 main 分支同步到 GitHub 的流水线
- 更新 .cnb.yml 流水线配置，增强构建和发布流程

### 修复 (Fixed)

- 修复所有图标资源文件被 UTF-8 编码损坏的问题
- 修复 git:release 任务因无效参数 title 导致 404 错误
- 更新 .cnb.yml 构建配置，完善 Release 和附件上传逻辑
- 恢复 .github/DISCUSSION_TEMPLATE 目录，仅移除 .github/discussions
- 修复 APK 文件名中 versionCode 前多余的 b 前缀
- 修复 build-and-release.yml YAML 语法错误

### 变更 (Changed)

- 移除 GitHub Actions 中同步到 CNB 的工作流配置
- 同步 GitHub LuminCore 自定义启动图标资源

### 杂项 (Chores)

- 修改 APP 版本号为 0.0.1
- 移除 GitHub Discussions 模板文件
- 移除 GitHub Discussions 模板目录
- 将所有 GitHub 工作流触发分支从 master 改为 main
- 升级 GitHub Actions 构建发布流水线

---

## [v0.0.1-alpha.2] - 2026-05-29

> CI/CD 全面修复阶段，解决 Gradle 构建、签名配置、密钥管理等多项问题。

### 修复 (Fixed)

- 修复 docker 配置错误导致构建失败
- 修复 settings.gradle.kts 中 dependencyResolution API 不兼容 Gradle 8.11.1 的问题
- 修复 web_trigger.yml 缺少 branch 顶层键导致按钮无法匹配分支
- 修复 settings.gradle.kts 中缺少 `include(":app")` 导致 assembleRelease 任务未找到
- 升级 Gradle wrapper 至 9.3.1 以满足 AGP 9.1.1 最低版本要求
- 修复 build.gradle.kts 签名配置中 `val cannot be reassigned` 错误
- 修复 Secrets Gradle Plugin 构建失败，添加缺失的 .env.example
- 修正 release.keystore 下载路径为 app/ 目录
- 为 GEMINI_API_KEY 提供安全默认值以防止 CI 构建失败
- 修复 GEMINI_API_KEY BuildConfig 在 CI 中的非法表达式
- 修复 Kotlin DSL 嵌套引号语法错误
- 修复 CI 签名密钥变量映射到 Gradle 签名配置
- 添加 RELEASE_STORE_FILE 环境变量以解决 CI 签名密钥路径缺失问题
- 修复 CI 签名密钥变量读取优先级问题

### 新增 (Added)

- 从 LuminCore 仓库同步 APP 源码
- 新增手动触发流水线构建 APK 功能
- 新增下载 release.keystore 到流水线构建流程

### 重构 (Refactor)

- 移除未使用的数据模型（BaseEntity、CervicalRecord、PeriodRecord、PregnancyRecord）

### 杂项 (Chores)

- 将 release.keystore 添加到 .gitignore

---

## [v0.0.1-alpha.1] - 2026-03-15

> 项目初始化阶段，完成基础框架搭建、项目文档体系构建和开发计划制定。

### 新增 (Added)

- 初始化项目基础配置
- 添加 Android 女性健康应用主程序
- 添加 Gradle 构建工具链
- 将 LuminCore - 女性健康助手统一重命名为 LuminCore - 健康助手
- 整合项目文档体系，添加项目文档总览索引和文档使用指南
- 创建女性健康管理系统开发计划和核心数据模型
- 从 LuminCore 仓库同步 APP 源码

### 修复 (Fixed)

- 将所有 LuminCore - 女性健康助手重命名为 LuminCore - 健康助手
- 格式化 plans 文件夹下的乱码文件名

### 文档 (Docs)

- 添加项目文档和基础说明
- 添加本地配置示例和开发计划文档
- 根据开发计划更新页面开发需求文档

### CI/CD

- 添加 GitHub Actions 和模板配置

### 杂项 (Chores)

- 移除重复的中文开发文档

---

[v0.0.1-alpha.5]: https://github.com/XMZZUZHI/womenhealth-app-kaifa/compare/v0.0.1-alpha.4...v0.0.1-alpha.5
[v0.0.1-alpha.4]: https://github.com/XMZZUZHI/womenhealth-app-kaifa/compare/v0.0.1-alpha.3...v0.0.1-alpha.4
[v0.0.1-alpha.3]: https://github.com/XMZZUZHI/womenhealth-app-kaifa/compare/v0.0.1-alpha.2...v0.0.1-alpha.3
[v0.0.1-alpha.2]: https://github.com/XMZZUZHI/womenhealth-app-kaifa/compare/v0.0.1-alpha.1...v0.0.1-alpha.2
[v0.0.1-alpha.1]: https://github.com/XMZZUZHI/womenhealth-app-kaifa/releases/tag/v0.0.1-alpha.1

最后更新日期: 2026年06月10日
文档维护人：祁筱欣
