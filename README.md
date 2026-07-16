# 青禾记账

>  Built by **gpt5.6 sol**

一款本地优先、无广告、无联网权限的中文记账 Android App。项目由 Kotlin、Jetpack Compose、Room、DataStore 构建，可直接用 Android Studio 打开。

## 已实现

- 支出/收入快速记账、分类、备注、账户、今天/昨天
- 月度结余、预算进度、最近流水、消费排行
- 流水搜索、收支筛选、按日分组、长按删除
- 近 6 个月柱状图、分类环形图与排行
- 深色模式、金额隐藏、月度预算
- CSV 导出/导入、系统备份与换机迁移
- Material 3 设计、自适应 App 图标、完整空状态
- Room 本地数据库；Manifest 不申请联网权限

## 打开与运行

1. 用 Android Studio 打开本目录（选择 `QingHeLedger` 文件夹）。
2. 等待 Gradle Sync 完成；确保使用 JDK 17。
3. 连接 Android 8.0（API 26）以上设备或模拟器，点击 Run。

如果生成器因网络原因没有下载到 `gradle-wrapper.jar`，在 Android Studio 终端执行一次：

```bash
gradle wrapper --gradle-version 8.11.1
```

### Maven 依赖下载失败

工程已默认配置阿里云 Google、Maven Central 与 Gradle Plugin 镜像，并保留官方仓库作为回退。如果 Android Studio 仍显示 `Could not GET repo.maven.apache.org`：

1. 确认 `Settings > Build, Execution, Deployment > Gradle` 中没有启用 Offline work。
2. 点击 `File > Invalidate Caches`，然后重新打开项目。
3. 在项目终端运行 `./gradlew --refresh-dependencies`（Windows 使用 `gradlew.bat --refresh-dependencies`）。

## 品牌定制

- 应用名：`app/src/main/res/values/strings.xml` 和 `AndroidManifest.xml`
- 包名：`app/build.gradle.kts` 的 `namespace` / `applicationId`，以及 Kotlin 目录和 package
- 主色：`ui/theme/Theme.kt`
- 图标：`res/drawable/ic_launcher_foreground.xml` 与 `ic_launcher_background.xml`

## 数据说明

金额以“分”的整数存入 Room，避免浮点误差。CSV 使用 UTF-8 BOM，Windows Excel 可直接识别中文。正式上架前请按你的主体信息补充隐私政策、应用签名、商店截图和软件著作权资料。
