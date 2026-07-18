# Qinghe Ledger · 青禾记账

<div align="center">

**认真生活，也清楚每一笔。**  
**Live mindfully. Understand every transaction.**

A local-first, ad-free and multilingual Android expense tracker built with Kotlin and Jetpack Compose.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Version](https://img.shields.io/badge/version-1.5.0-0B7A66)

[中文](#中文) · [English](#english) · [हिन्दी](#हिन्दी) · [Español](#español) · [العربية](#العربية) · [Français](#français) · [Русский](#русский) · [Português](#português)

</div>

---

## 中文

### 项目简介

青禾记账是一款本地优先、无广告的多语言 Android 记账应用，专注于快速、清晰且尊重隐私的日常收支管理。应用采用 Kotlin、Jetpack Compose、Material 3、Room 和 DataStore 构建，支持完整的记账、预算、统计、多币种与数据迁移流程。

账单默认仅保存在你的设备上。应用不会上传账单内容，联网仅用于向 Frankfurter API 查询日更参考汇率。

### 核心亮点

- **轻扰型悬浮金额预览**：向下滚动记账表单时，当前金额会以轻量悬浮条持续显示，在不遮挡主要内容的前提下保持金额可见。
- **快速记账**：支持支出与收入、四则运算计算器、分类、备注、账户以及今天/昨天快捷日期。
- **灵活编辑**：点击账单即可修改，长按可删除；可以搜索流水、筛选收支并按日期分组查看。
- **全球币种**：读取设备支持的完整 ISO 4217 币种，可按中文名、货币代码或符号搜索，并将常用币种优先展示。
- **参考汇率**：通过 Frankfurter API 查询日更汇率，查询结果可继续手动修改；接口未覆盖的币种仍可使用手动汇率。
- **惯用货币**：可选择账本的基础货币，历史账单、预算和统计金额会随之统一换算。
- **预算与统计**：提供月度结余、收入与支出汇总、预算进度、消费排行、近 6 个月趋势以及分类占比。
- **特殊账单处理**：报销、退款或代付记录可标记为“不计入预算/统计”。
- **隐私显示**：支持一键隐藏金额，适合在公共场合查看账本。
- **数据迁移**：支持 CSV 导入与导出、重复账单自动跳过，以及 Android 系统备份与换机迁移。
- **多语言与 RTL**：内置中文、英语、印地语、西班牙语、阿拉伯语、法语、俄语和葡萄牙语；阿拉伯语自动使用 RTL 布局。
- **个性化体验**：支持深色模式、首次启动语言与惯用货币引导、Material 3 界面和自适应应用图标。

### 技术栈

| 模块 | 技术 |
| --- | --- |
| 开发语言 | Kotlin 2.1.20 |
| 界面 | Jetpack Compose、Material 3 |
| 本地数据库 | Room 2.7.2 |
| 偏好设置 | DataStore Preferences 1.1.7 |
| 页面导航 | Navigation Compose 2.9.0 |
| 异步与状态 | Kotlin Coroutines、Flow、ViewModel |
| 汇率数据 | Frankfurter API |
| 构建环境 | Android Gradle Plugin 8.9.2、Gradle 8.11.1、JDK 17 |
| 最低系统 | Android 8.0 / API 26 |

### 生成、打开与运行

如果仓库中提供的是项目生成脚本，请先运行：

```bash
python3 generate_qinghe_ledger_v6.py
```

随后：

1. 使用 Android Studio 打开生成的 `QingHeLedger` 文件夹。
2. 确认 Gradle 使用 JDK 17，等待 Gradle Sync 完成。
3. 连接 Android 8.0 以上的设备或启动模拟器。
4. 点击 **Run** 构建并安装应用。

如果未能自动下载 `gradle-wrapper.jar`，可在项目目录执行：

```bash
gradle wrapper --gradle-version 8.11.1
```

### 数据与隐私

- 金额以最小货币单位的整数形式保存在 Room 中，避免常见的浮点精度问题。
- CSV 文件使用 UTF-8 BOM 编码，可由 Windows Excel 正确识别中文。
- 账单、备注、账户和统计数据不会上传到服务器。
- 联网请求仅包含汇率查询所需的货币代码；汇率为日更参考数据，不应视为交易或投资报价。
- 正式发布前，请根据实际发行主体补充隐私政策、应用签名、商店素材和所需的软件著作权信息。

---

## English

### Overview

Qinghe Ledger is a local-first, ad-free, multilingual Android expense tracker designed for fast entry, clear insights, and privacy-conscious personal finance management. It is built with Kotlin, Jetpack Compose, Material 3, Room, and DataStore, providing a complete workflow for transactions, budgets, analytics, multiple currencies, and data migration.

Your ledger stays on your device by default. Transaction details are never uploaded; internet access is used only to request daily reference exchange rates from the Frankfurter API.

### Highlights

- **Non-intrusive floating amount preview**: keeps the current amount visible while you scroll through the entry form without blocking the main content.
- **Fast transaction entry**: record income or expenses with a built-in calculator, categories, notes, accounts, and quick date options.
- **Easy editing**: tap a transaction to edit it, long-press to delete it, search the ledger, filter by type, and browse entries grouped by date.
- **Global currency selection**: uses the full ISO 4217 currency list available on the device, with search by localized name, code, or symbol.
- **Daily reference rates**: fetch rates from the Frankfurter API and adjust them manually when needed; unsupported currencies can still use manual rates.
- **Preferred base currency**: change the ledger currency and consistently convert historical entries, budgets, and analytics.
- **Budgets and insights**: view monthly balance, income, expenses, budget progress, top spending, a six-month trend, and category breakdowns.
- **Flexible statistics**: exclude reimbursements, refunds, or pass-through payments from budget and analytics calculations.
- **Privacy controls**: hide amounts instantly when viewing the app in public.
- **Portable data**: import and export CSV files, skip duplicate entries automatically, and use Android backup for device migration.
- **Eight interface languages**: Chinese, English, Hindi, Spanish, Arabic, French, Russian, and Portuguese, including automatic RTL layout for Arabic.
- **Modern Android experience**: Material 3 design, dark mode, guided first-run setup, responsive layouts, and an adaptive app icon.

### Technology

| Area | Technology |
| --- | --- |
| Language | Kotlin 2.1.20 |
| UI | Jetpack Compose, Material 3 |
| Local database | Room 2.7.2 |
| Preferences | DataStore Preferences 1.1.7 |
| Navigation | Navigation Compose 2.9.0 |
| State and async work | Kotlin Coroutines, Flow, ViewModel |
| Exchange rates | Frankfurter API |
| Build toolchain | Android Gradle Plugin 8.9.2, Gradle 8.11.1, JDK 17 |
| Minimum Android version | Android 8.0 / API 26 |

### Generate, Build, and Run

If this repository contains the project generator, run:

```bash
python3 generate_qinghe_ledger_v6.py
```

Then:

1. Open the generated `QingHeLedger` folder in Android Studio.
2. Make sure Gradle uses JDK 17 and wait for Gradle Sync to finish.
3. Connect a device running Android 8.0 or later, or start an emulator.
4. Click **Run** to build and install the app.

If `gradle-wrapper.jar` could not be downloaded automatically, run this command in the generated project directory:

```bash
gradle wrapper --gradle-version 8.11.1
```

### Data and Privacy

- Monetary values are stored as integers in their smallest currency unit to avoid common floating-point errors.
- CSV exports use UTF-8 with BOM for reliable Chinese text support in Windows Excel.
- Transactions, notes, accounts, and analytics remain on the device.
- Online requests contain only the currency codes required for exchange-rate lookups.
- Exchange rates are daily reference values and should not be treated as trading or investment quotes.
- Before publishing a production build, add the appropriate privacy policy, signing configuration, store assets, and legal information for your distribution entity.

---

## हिन्दी

Qinghe Ledger एक स्थानीय-प्रथम, विज्ञापन-मुक्त और बहुभाषी Android खर्च प्रबंधक है। इसमें तेज़ लेन-देन प्रविष्टि, कैलकुलेटर, हल्का फ्लोटिंग राशि पूर्वावलोकन, बजट, आँकड़े, कई मुद्राएँ, CSV आयात/निर्यात और डार्क मोड शामिल हैं। आपका डेटा डिवाइस पर रहता है; इंटरनेट का उपयोग केवल दैनिक संदर्भ विनिमय दरों के लिए किया जाता है।

---

## Español

Qinghe Ledger es un gestor de finanzas para Android, multilingüe, sin anuncios y centrado en la privacidad. Incluye registro rápido con calculadora, vista flotante discreta del importe, presupuestos, estadísticas, múltiples monedas, importación y exportación CSV y modo oscuro. Los movimientos permanecen en el dispositivo; internet solo se utiliza para consultar tipos de cambio diarios de referencia.

---

## العربية

<div dir="rtl">

Qinghe Ledger هو تطبيق أندرويد محلي ومتعدد اللغات لإدارة المصروفات دون إعلانات. يوفّر إدخالًا سريعًا للمعاملات مع آلة حاسبة، ومعاينة عائمة وغير مزعجة للمبلغ، وميزانيات وإحصاءات وعملات متعددة واستيراد وتصدير CSV ووضعًا داكنًا. تبقى بياناتك على الجهاز، ويُستخدم الإنترنت فقط لجلب أسعار صرف مرجعية يومية.

</div>

---

## Français

Qinghe Ledger est une application Android multilingue, sans publicité et axée sur la confidentialité. Elle propose une saisie rapide avec calculatrice, un aperçu flottant discret du montant, des budgets, des statistiques, plusieurs devises, l’import/export CSV et un mode sombre. Les opérations restent sur l’appareil ; internet sert uniquement à récupérer des taux de change quotidiens de référence.

---

## Русский

Qinghe Ledger — локальное многоязычное Android-приложение для учёта личных финансов без рекламы. В нём есть быстрый ввод с калькулятором, ненавязчивое плавающее отображение суммы, бюджеты, статистика, разные валюты, импорт и экспорт CSV и тёмная тема. Данные остаются на устройстве, а интернет используется только для получения ежедневных справочных курсов валют.

---

## Português

Qinghe Ledger é um gerenciador financeiro para Android, multilíngue, sem anúncios e focado em privacidade. Ele oferece registro rápido com calculadora, prévia flutuante e discreta do valor, orçamentos, estatísticas, várias moedas, importação e exportação CSV e modo escuro. Os lançamentos permanecem no dispositivo; a internet é usada apenas para consultar taxas de câmbio diárias de referência.

---

<div align="center">

**Qinghe Ledger · Clear records, calmer finances.**

</div>
