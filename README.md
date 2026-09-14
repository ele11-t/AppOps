# AppOps Manager

一个轻量级的 Android 工具应用，配合 Shizuku 批量管理第三方应用的后台权限（AppOps）。

## 核心功能

- **Shizuku 集成**：支持最新版 Shizuku (v11+)，通过 Binder 建立安全连接。
- **应用列表**：自动扫描并展示所有第三方应用，支持图标、应用名、包名显示。
- **实时搜索**：支持按应用名称或包名实时过滤。
- **状态追踪**：自动检测每个应用的当前后台权限状态（Allowed/Ignored）。
- **批量操作**：
  - **一键禁用**：将选定应用的后台相关权限（包括 RUN_IN_BACKGROUND, WAKE_LOCK, GPS, FINE_LOCATION 等）批量设为 `deny` 或 `ignore`。
  - **一键恢复**：将权限批量改回 `allow`。
- **现代化 UI**：基于 Jetpack Compose 和 Material 3 设计，支持加载进度提示。

## 权限覆盖范围

本工具主要针对以下后台调度和隐私相关权限进行批量管理：
- `RUN_IN_BACKGROUND`
- `RUN_ANY_IN_BACKGROUND`
- `WAKE_LOCK`
- `BOOT_COMPLETED`
- `START_FOREGROUND`
- `FINE_LOCATION` / `COARSE_LOCATION` / `GPS`
- `BLUETOOTH_SCAN` / `CONNECT`
- `SYSTEM_ALERT_WINDOW`
- 等共计 20 余项底层标识符。

## 使用前提

1. 手机需安装并启动 [Shizuku](https://shizuku.moe/) 管理器。
2. 在 App 内点击“申请授权”并允许 Shizuku 权限。

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose (Material 3)
- **架构**：MVVM + StateFlow + Coroutines
- **核心依赖**：`dev.rikka.shizuku:api` & `provider` (13.1.5)

## 免责声明

本工具通过 Shizuku 直接修改系统 AppOps 设置。请谨慎操作，禁用某些核心应用的后台权限可能会导致通知延迟或功能异常。
