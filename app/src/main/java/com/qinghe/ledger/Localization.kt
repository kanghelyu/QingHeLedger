package com.qinghe.ledger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class AppLanguage(val code: String, val nativeName: String, val tag: String) {
    ZH("zh", "中文", "zh-CN"),
    EN("en", "English", "en-US"),
    HI("hi", "हिन्दी", "hi-IN"),
    ES("es", "Español", "es-ES"),
    AR("ar", "العربية", "ar"),
    FR("fr", "Français", "fr-FR"),
    RU("ru", "Русский", "ru-RU"),
    PT("pt", "Português", "pt-BR");

    val locale: Locale get() = Locale.forLanguageTag(tag)

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: ZH
    }
}

class I18n(val language: AppLanguage) {
    private val strings = translationsFor(language.code)
    private val onboarding = onboardingStrings(language.code)

    operator fun invoke(key: String, vararg args: Any): String {
        val template = strings[key] ?: onboarding[key] ?: key
        return if (args.isEmpty()) template else runCatching {
            String.format(language.locale, template, *args)
        }.getOrDefault(template)
    }
}

val LocalI18n = staticCompositionLocalOf { I18n(AppLanguage.ZH) }
val LocalBaseCurrencyCode = staticCompositionLocalOf { "CNY" }

@Composable
fun baseMoney(cents: Long, hidden: Boolean = false): String {
    val i18n = LocalI18n.current
    val code = LocalBaseCurrencyCode.current
    val currencyObject = runCatching { Currency.getInstance(code) }.getOrNull()
    val symbol = currencyObject?.getSymbol(i18n.language.locale) ?: code
    if (hidden) return "$symbol ••••"
    return NumberFormat.getCurrencyInstance(i18n.language.locale).apply {
        currencyObject?.let { this.currency = it }
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(cents / 100.0)
}

fun localizedCurrencyName(code: String, i18n: I18n): String = runCatching {
    Currency.getInstance(code).getDisplayName(i18n.language.locale)
}.getOrDefault(code)

fun localizedCurrencySymbol(code: String, i18n: I18n): String = runCatching {
    Currency.getInstance(code).getSymbol(i18n.language.locale)
}.getOrDefault(code)

private fun onboardingStrings(code: String): Map<String, String> = when (code) {
    "en" -> mapOf(
        "欢迎使用青禾记账" to "Welcome to Qinghe Ledger", "选择你的惯用货币" to "Choose your preferred currency",
        "首次使用需要先选择界面语言" to "Choose your interface language to begin", "汇率、预算和统计都将使用此币种" to "Rates, budgets and totals will use this currency",
        "下一步" to "Next", "请选择惯用货币" to "Choose a preferred currency", "点击打开完整货币列表" to "Tap to open the full currency list",
        "无法继续：该货币暂不支持汇率换算" to "Cannot continue: this currency is not supported", "设置失败，请检查网络后重试" to "Setup failed. Check your connection and retry",
        "开始使用" to "Get started", "返回" to "Back", "账单只保存在你的设备上" to "Your entries stay on your device"
    )
    "hi" -> mapOf(
        "欢迎使用青禾记账" to "छिंगहे खाता में आपका स्वागत है", "选择你的惯用货币" to "अपनी पसंदीदा मुद्रा चुनें",
        "首次使用需要先选择界面语言" to "शुरू करने के लिए भाषा चुनें", "汇率、预算和统计都将使用此币种" to "दरें, बजट और आँकड़े इसी मुद्रा में होंगे",
        "下一步" to "आगे", "请选择惯用货币" to "पसंदीदा मुद्रा चुनें", "点击打开完整货币列表" to "पूरी मुद्रा सूची खोलें",
        "无法继续：该货币暂不支持汇率换算" to "आगे नहीं बढ़ सकते: मुद्रा समर्थित नहीं", "设置失败，请检查网络后重试" to "सेटअप विफल। नेटवर्क जाँचें",
        "开始使用" to "शुरू करें", "返回" to "वापस", "账单只保存在你的设备上" to "आपके रिकॉर्ड केवल डिवाइस पर रहते हैं"
    )
    "es" -> mapOf(
        "欢迎使用青禾记账" to "Te damos la bienvenida a Qinghe", "选择你的惯用货币" to "Elige tu moneda preferida",
        "首次使用需要先选择界面语言" to "Elige el idioma para comenzar", "汇率、预算和统计都将使用此币种" to "Los tipos, presupuestos y totales usarán esta moneda",
        "下一步" to "Siguiente", "请选择惯用货币" to "Elige una moneda preferida", "点击打开完整货币列表" to "Toca para abrir la lista completa",
        "无法继续：该货币暂不支持汇率换算" to "No se puede continuar: moneda no compatible", "设置失败，请检查网络后重试" to "Error de configuración. Revisa la conexión",
        "开始使用" to "Comenzar", "返回" to "Atrás", "账单只保存在你的设备上" to "Tus movimientos permanecen en el dispositivo"
    )
    "ar" -> mapOf(
        "欢迎使用青禾记账" to "مرحبًا بك في دفتر تشينغه", "选择你的惯用货币" to "اختر عملتك المفضلة",
        "首次使用需要先选择界面语言" to "اختر لغة الواجهة للبدء", "汇率、预算和统计都将使用此币种" to "ستستخدم الأسعار والميزانية والإجماليات هذه العملة",
        "下一步" to "التالي", "请选择惯用货币" to "اختر عملة مفضلة", "点击打开完整货币列表" to "اضغط لفتح قائمة العملات الكاملة",
        "无法继续：该货币暂不支持汇率换算" to "لا يمكن المتابعة: العملة غير مدعومة", "设置失败，请检查网络后重试" to "فشل الإعداد. تحقق من الاتصال",
        "开始使用" to "ابدأ", "返回" to "رجوع", "账单只保存在你的设备上" to "تبقى معاملاتك على جهازك"
    )
    "fr" -> mapOf(
        "欢迎使用青禾记账" to "Bienvenue dans Qinghe Budget", "选择你的惯用货币" to "Choisissez votre devise préférée",
        "首次使用需要先选择界面语言" to "Choisissez la langue pour commencer", "汇率、预算和统计都将使用此币种" to "Les taux, budgets et totaux utiliseront cette devise",
        "下一步" to "Suivant", "请选择惯用货币" to "Choisissez une devise préférée", "点击打开完整货币列表" to "Touchez pour ouvrir la liste complète",
        "无法继续：该货币暂不支持汇率换算" to "Impossible de continuer : devise non prise en charge", "设置失败，请检查网络后重试" to "Échec de la configuration. Vérifiez la connexion",
        "开始使用" to "Commencer", "返回" to "Retour", "账单只保存在你的设备上" to "Vos opérations restent sur votre appareil"
    )
    "ru" -> mapOf(
        "欢迎使用青禾记账" to "Добро пожаловать в Qinghe", "选择你的惯用货币" to "Выберите основную валюту",
        "首次使用需要先选择界面语言" to "Сначала выберите язык интерфейса", "汇率、预算和统计都将使用此币种" to "Курсы, бюджет и итоги будут в этой валюте",
        "下一步" to "Далее", "请选择惯用货币" to "Выберите основную валюту", "点击打开完整货币列表" to "Нажмите, чтобы открыть полный список",
        "无法继续：该货币暂不支持汇率换算" to "Продолжить нельзя: валюта не поддерживается", "设置失败，请检查网络后重试" to "Ошибка настройки. Проверьте сеть",
        "开始使用" to "Начать", "返回" to "Назад", "账单只保存在你的设备上" to "Ваши операции хранятся на устройстве"
    )
    "pt" -> mapOf(
        "欢迎使用青禾记账" to "Boas-vindas ao Qinghe", "选择你的惯用货币" to "Escolha sua moeda preferida",
        "首次使用需要先选择界面语言" to "Escolha o idioma para começar", "汇率、预算和统计都将使用此币种" to "Taxas, orçamento e totais usarão esta moeda",
        "下一步" to "Avançar", "请选择惯用货币" to "Escolha uma moeda preferida", "点击打开完整货币列表" to "Toque para abrir a lista completa",
        "无法继续：该货币暂不支持汇率换算" to "Não é possível continuar: moeda não compatível", "设置失败，请检查网络后重试" to "Falha na configuração. Verifique a conexão",
        "开始使用" to "Começar", "返回" to "Voltar", "账单只保存在你的设备上" to "Seus lançamentos ficam no dispositivo"
    )
    else -> emptyMap()
}

private fun translationsFor(code: String): Map<String, String> = when (code) {
    "en" -> mapOf(
        "首页" to "Home", "明细" to "Bills", "统计" to "Stats", "我的" to "Me",
        "青禾记账" to "Qinghe Ledger", "认真生活，也清楚每一笔" to "Live mindfully, know every expense",
        "记一笔" to "Add entry", "编辑账单" to "Edit entry", "关闭" to "Close", "支出" to "Expense", "收入" to "Income",
        "支出金额" to "Expense amount", "收入金额" to "Income amount", "币种" to "Currency", "选择币种" to "Choose currency",
        "换算汇率" to "Exchange rate", "查询" to "Fetch", "该货币暂不支持此功能" to "This currency is not supported yet",
        "汇率查询失败，请检查网络后重试" to "Could not fetch the rate. Check your connection and retry",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "1 %s in %s · Frankfurter API · Daily reference rate · Editable",
        "数据日期 %s" to "Data date %s", "选择分类" to "Category", "备注（可选）" to "Note (optional)",
        "例如：和朋友聚餐" to "e.g. Dinner with friends", "今天" to "Today", "昨天" to "Yesterday",
        "日常账户" to "Everyday", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "Bank card", "现金" to "Cash",
        "不计入预算/统计" to "Exclude from budget/stats", "计入预算/统计" to "Include in budget/stats",
        "保存这笔账" to "Save entry", "保存修改" to "Save changes", "搜索人民币、卢布或 RUB" to "Search dollar, ruble or RUB",
        "清空" to "Clear", "常用币种置顶 · %s 个结果" to "Popular first · %s results", "已选择" to "Selected",
        "没有找到这个币种" to "No currency found", "不计统计" to "Excluded", "退格" to "Backspace",
        "最近账单" to "Recent entries", "共 %s 笔" to "%s entries", "本月消费排行" to "Top spending this month",
        "%s年 %s月" to "%2\$s/%1\$s", "本月结余" to "Monthly balance", "本月收入" to "Monthly income", "本月支出" to "Monthly expense",
        "月度预算" to "Monthly budget", "还能花 %s" to "%s left", "已超支 %s" to "%s over budget",
        "从第一笔开始" to "Start with your first entry", "每天花十秒，月底看清钱去了哪里" to "Ten seconds a day makes your month clear",
        "账单明细" to "All entries", "搜索分类、备注或账户" to "Search category, note or account", "全部" to "All",
        "点击账单可修改，长按可删除" to "Tap to edit, press and hold to delete", "还没有账单" to "No entries yet",
        "没有找到匹配账单" to "No matching entries", "支出 %s" to "Expense %s", "删除这笔账单？" to "Delete this entry?",
        "%s · %s，删除后无法恢复。" to "%s · %s. This cannot be undone.", "删除" to "Delete", "取消" to "Cancel",
        "统计分析" to "Analytics", "总支出" to "Total expense", "总收入" to "Total income", "近 6 个月支出" to "Last 6 months",
        "分类占比" to "Category share", "%s 类" to "%s categories", "本月还没有支出数据" to "No expense data this month", "%s月" to "M%s",
        "我的账本" to "My ledger", "我的生活账本" to "My personal ledger", "本机保存 · %s 笔记录" to "On device · %s entries",
        "账本设置" to "Ledger settings", "隐藏金额" to "Hide amounts", "在公共场合保护隐私" to "Protect privacy in public",
        "深色模式" to "Dark mode", "夜间查看更舒适" to "Easier on the eyes at night", "语言" to "Language",
        "惯用货币" to "Preferred currency", "汇率和统计金额将换算为此币种" to "Rates and totals use this currency",
        "数据管理" to "Data", "导出 CSV" to "Export CSV", "可用 Excel 打开" to "Opens in Excel", "导入 CSV" to "Import CSV",
        "恢复青禾账单文件" to "Restore a Qinghe file", "清空全部账单" to "Delete all entries", "此操作不可恢复" to "This cannot be undone",
        "隐私优先" to "Privacy first", "账本只存本机；联网仅查询汇率，不上传任何账单" to "Entries stay on device; internet is used only for exchange rates",
        "设置月度预算" to "Set monthly budget", "预算金额" to "Budget amount", "保存" to "Save", "清空全部账单？" to "Delete all entries?",
        "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "This permanently deletes %s entries. Export a backup first.", "确认清空" to "Delete all",
        "选择语言" to "Choose language", "选择惯用货币" to "Choose preferred currency", "正在换算历史账单…" to "Converting saved entries…",
        "惯用货币已改为 %s" to "Preferred currency changed to %s", "无法切换：该货币暂不支持汇率换算" to "Cannot switch: this currency is not supported",
        "切换失败，请检查网络后重试" to "Switch failed. Check your connection and retry",
        "账单已成功导出" to "Entries exported", "导出失败：%s" to "Export failed: %s", "未知错误" to "Unknown error",
        "已导入 %s 笔，重复账单已自动跳过" to "Imported %s entries; duplicates skipped", "文件格式不正确" to "Invalid file format", "无法读取这个文件" to "Cannot read this file",
        "餐饮" to "Food", "交通" to "Transport", "购物" to "Shopping", "居住" to "Housing", "娱乐" to "Entertainment", "医疗" to "Health",
        "学习" to "Education", "通讯" to "Communication", "人情" to "Gifts", "其他" to "Other", "工资" to "Salary", "奖金" to "Bonus",
        "理财" to "Investments", "兼职" to "Side job", "礼金" to "Gift money"
    )
    "hi" -> mapOf(
        "首页" to "होम", "明细" to "लेन-देन", "统计" to "आँकड़े", "我的" to "मेरा", "青禾记账" to "छिंगहे खाता",
        "认真生活，也清楚每一笔" to "हर खर्च को समझें", "记一笔" to "लेन-देन जोड़ें", "编辑账单" to "लेन-देन संपादित करें", "关闭" to "बंद करें",
        "支出" to "खर्च", "收入" to "आय", "支出金额" to "खर्च राशि", "收入金额" to "आय राशि", "币种" to "मुद्रा", "选择币种" to "मुद्रा चुनें",
        "换算汇率" to "विनिमय दर", "查询" to "खोजें", "该货币暂不支持此功能" to "यह मुद्रा अभी समर्थित नहीं है",
        "汇率查询失败，请检查网络后重试" to "दर नहीं मिली। नेटवर्क जाँचकर पुनः प्रयास करें",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "1 %s का %s में मूल्य · Frankfurter API · दैनिक दर · संपादन योग्य",
        "数据日期 %s" to "डेटा तिथि %s", "选择分类" to "श्रेणी", "备注（可选）" to "नोट (वैकल्पिक)", "例如：和朋友聚餐" to "जैसे दोस्तों के साथ भोजन",
        "今天" to "आज", "昨天" to "कल", "日常账户" to "दैनिक", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "बैंक कार्ड", "现金" to "नकद",
        "不计入预算/统计" to "बजट/आँकड़ों से बाहर", "计入预算/统计" to "बजट/आँकड़ों में शामिल", "保存这笔账" to "सहेजें", "保存修改" to "बदलाव सहेजें",
        "搜索人民币、卢布或 RUB" to "मुद्रा या कोड खोजें", "清空" to "साफ़ करें", "常用币种置顶 · %s 个结果" to "लोकप्रिय पहले · %s परिणाम", "已选择" to "चयनित",
        "没有找到这个币种" to "मुद्रा नहीं मिली", "不计统计" to "शामिल नहीं", "退格" to "बैकस्पेस",
        "最近账单" to "हाल के लेन-देन", "共 %s 笔" to "%s लेन-देन", "本月消费排行" to "इस माह के शीर्ष खर्च", "%s年 %s月" to "%2\$s/%1\$s",
        "本月结余" to "मासिक शेष", "本月收入" to "मासिक आय", "本月支出" to "मासिक खर्च", "月度预算" to "मासिक बजट", "还能花 %s" to "%s शेष",
        "已超支 %s" to "बजट से %s अधिक", "从第一笔开始" to "पहला लेन-देन जोड़ें", "每天花十秒，月底看清钱去了哪里" to "हर दिन दस सेकंड में खर्च स्पष्ट रखें",
        "账单明细" to "सभी लेन-देन", "搜索分类、备注或账户" to "श्रेणी, नोट या खाता खोजें", "全部" to "सभी", "点击账单可修改，长按可删除" to "संपादन हेतु टैप, हटाने हेतु दबाएँ",
        "还没有账单" to "अभी कोई लेन-देन नहीं", "没有找到匹配账单" to "कोई मिलान नहीं", "支出 %s" to "खर्च %s", "删除这笔账单？" to "यह लेन-देन हटाएँ?",
        "%s · %s，删除后无法恢复。" to "%s · %s. इसे वापस नहीं लाया जा सकता।", "删除" to "हटाएँ", "取消" to "रद्द करें", "统计分析" to "विश्लेषण",
        "总支出" to "कुल खर्च", "总收入" to "कुल आय", "近 6 个月支出" to "पिछले 6 महीने", "分类占比" to "श्रेणी हिस्सा", "%s 类" to "%s श्रेणियाँ",
        "本月还没有支出数据" to "इस माह खर्च डेटा नहीं", "%s月" to "माह %s", "我的账本" to "मेरा खाता", "我的生活账本" to "मेरा निजी खाता",
        "本机保存 · %s 笔记录" to "डिवाइस पर · %s रिकॉर्ड", "账本设置" to "खाता सेटिंग", "隐藏金额" to "राशि छिपाएँ", "在公共场合保护隐私" to "सार्वजनिक स्थान पर गोपनीयता",
        "深色模式" to "डार्क मोड", "夜间查看更舒适" to "रात में आरामदायक", "语言" to "भाषा", "惯用货币" to "पसंदीदा मुद्रा", "汇率和统计金额将换算为此币种" to "दरें और कुल इस मुद्रा में होंगे",
        "数据管理" to "डेटा", "导出 CSV" to "CSV निर्यात", "可用 Excel 打开" to "Excel में खोलें", "导入 CSV" to "CSV आयात", "恢复青禾账单文件" to "छिंगहे फ़ाइल पुनर्स्थापित करें",
        "清空全部账单" to "सभी हटाएँ", "此操作不可恢复" to "इसे वापस नहीं किया जा सकता", "隐私优先" to "गोपनीयता पहले", "账本只存本机；联网仅查询汇率，不上传任何账单" to "रिकॉर्ड डिवाइस पर रहते हैं; इंटरनेट केवल दरों के लिए है",
        "设置月度预算" to "मासिक बजट सेट करें", "预算金额" to "बजट राशि", "保存" to "सहेजें", "清空全部账单？" to "सभी लेन-देन हटाएँ?", "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "%s लेन-देन स्थायी रूप से हटेंगे। पहले बैकअप लें।",
        "确认清空" to "सभी हटाएँ", "选择语言" to "भाषा चुनें", "选择惯用货币" to "पसंदीदा मुद्रा चुनें", "正在换算历史账单…" to "पुराने लेन-देन बदल रहे हैं…",
        "惯用货币已改为 %s" to "पसंदीदा मुद्रा %s हुई", "无法切换：该货币暂不支持汇率换算" to "बदल नहीं सकते: मुद्रा समर्थित नहीं", "切换失败，请检查网络后重试" to "बदलाव विफल। नेटवर्क जाँचें",
        "账单已成功导出" to "निर्यात सफल", "导出失败：%s" to "निर्यात विफल: %s", "未知错误" to "अज्ञात त्रुटि", "已导入 %s 笔，重复账单已自动跳过" to "%s आयात हुए; डुप्लिकेट छोड़े", "文件格式不正确" to "गलत फ़ाइल प्रारूप", "无法读取这个文件" to "फ़ाइल पढ़ी नहीं जा सकी",
        "餐饮" to "भोजन", "交通" to "यातायात", "购物" to "खरीदारी", "居住" to "आवास", "娱乐" to "मनोरंजन", "医疗" to "स्वास्थ्य", "学习" to "शिक्षा", "通讯" to "संचार", "人情" to "उपहार", "其他" to "अन्य", "工资" to "वेतन", "奖金" to "बोनस", "理财" to "निवेश", "兼职" to "अंशकालिक", "礼金" to "उपहार राशि"
    )
    "es" -> mapOf(
        "首页" to "Inicio", "明细" to "Movimientos", "统计" to "Estadísticas", "我的" to "Mi cuenta", "青禾记账" to "Qinghe Finanzas", "认真生活，也清楚每一笔" to "Vive con atención, conoce cada gasto",
        "记一笔" to "Añadir", "编辑账单" to "Editar movimiento", "关闭" to "Cerrar", "支出" to "Gasto", "收入" to "Ingreso", "支出金额" to "Importe del gasto", "收入金额" to "Importe del ingreso",
        "币种" to "Moneda", "选择币种" to "Elegir moneda", "换算汇率" to "Tipo de cambio", "查询" to "Consultar", "该货币暂不支持此功能" to "Esta moneda aún no es compatible", "汇率查询失败，请检查网络后重试" to "No se pudo consultar. Revisa la conexión",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "1 %s en %s · Frankfurter API · Tipo diario · Editable", "数据日期 %s" to "Fecha %s", "选择分类" to "Categoría", "备注（可选）" to "Nota (opcional)", "例如：和朋友聚餐" to "p. ej., cena con amigos",
        "今天" to "Hoy", "昨天" to "Ayer", "日常账户" to "Diario", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "Tarjeta", "现金" to "Efectivo", "不计入预算/统计" to "Excluir del presupuesto", "计入预算/统计" to "Incluir en el presupuesto",
        "保存这笔账" to "Guardar", "保存修改" to "Guardar cambios", "搜索人民币、卢布或 RUB" to "Buscar moneda o código", "清空" to "Borrar", "常用币种置顶 · %s 个结果" to "Populares primero · %s resultados", "已选择" to "Seleccionada", "没有找到这个币种" to "No se encontró la moneda", "不计统计" to "Excluido", "退格" to "Retroceso",
        "最近账单" to "Movimientos recientes", "共 %s 笔" to "%s movimientos", "本月消费排行" to "Mayores gastos del mes", "%s年 %s月" to "%2\$s/%1\$s", "本月结余" to "Saldo mensual", "本月收入" to "Ingresos del mes", "本月支出" to "Gastos del mes", "月度预算" to "Presupuesto mensual", "还能花 %s" to "Quedan %s", "已超支 %s" to "%s sobre el presupuesto",
        "从第一笔开始" to "Empieza con tu primer movimiento", "每天花十秒，月底看清钱去了哪里" to "Diez segundos al día para entender tu mes", "账单明细" to "Todos los movimientos", "搜索分类、备注或账户" to "Buscar categoría, nota o cuenta", "全部" to "Todos", "点击账单可修改，长按可删除" to "Toca para editar, mantén para borrar", "还没有账单" to "Aún no hay movimientos", "没有找到匹配账单" to "No hay coincidencias", "支出 %s" to "Gasto %s",
        "删除这笔账单？" to "¿Eliminar este movimiento?", "%s · %s，删除后无法恢复。" to "%s · %s. No se puede deshacer.", "删除" to "Eliminar", "取消" to "Cancelar", "统计分析" to "Análisis", "总支出" to "Gasto total", "总收入" to "Ingreso total", "近 6 个月支出" to "Últimos 6 meses", "分类占比" to "Por categoría", "%s 类" to "%s categorías", "本月还没有支出数据" to "Sin gastos este mes", "%s月" to "M%s",
        "我的账本" to "Mi libro", "我的生活账本" to "Mi libro personal", "本机保存 · %s 笔记录" to "En el dispositivo · %s registros", "账本设置" to "Ajustes", "隐藏金额" to "Ocultar importes", "在公共场合保护隐私" to "Protege tu privacidad", "深色模式" to "Modo oscuro", "夜间查看更舒适" to "Más cómodo de noche", "语言" to "Idioma", "惯用货币" to "Moneda preferida", "汇率和统计金额将换算为此币种" to "Tipos y totales usarán esta moneda",
        "数据管理" to "Datos", "导出 CSV" to "Exportar CSV", "可用 Excel 打开" to "Se abre en Excel", "导入 CSV" to "Importar CSV", "恢复青禾账单文件" to "Restaurar archivo de Qinghe", "清空全部账单" to "Eliminar todo", "此操作不可恢复" to "No se puede deshacer", "隐私优先" to "Privacidad primero", "账本只存本机；联网仅查询汇率，不上传任何账单" to "Los datos quedan en el dispositivo; internet solo consulta tipos",
        "设置月度预算" to "Definir presupuesto", "预算金额" to "Importe del presupuesto", "保存" to "Guardar", "清空全部账单？" to "¿Eliminar todos los movimientos?", "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "Se eliminarán %s movimientos. Exporta una copia primero.", "确认清空" to "Eliminar todo", "选择语言" to "Elegir idioma", "选择惯用货币" to "Elegir moneda preferida", "正在换算历史账单…" to "Convirtiendo movimientos…", "惯用货币已改为 %s" to "Moneda preferida: %s", "无法切换：该货币暂不支持汇率换算" to "No se puede cambiar: moneda no compatible", "切换失败，请检查网络后重试" to "Error al cambiar. Revisa la conexión",
        "账单已成功导出" to "Exportación completada", "导出失败：%s" to "Error al exportar: %s", "未知错误" to "Error desconocido", "已导入 %s 笔，重复账单已自动跳过" to "%s importados; duplicados omitidos", "文件格式不正确" to "Formato no válido", "无法读取这个文件" to "No se puede leer el archivo",
        "餐饮" to "Comida", "交通" to "Transporte", "购物" to "Compras", "居住" to "Vivienda", "娱乐" to "Ocio", "医疗" to "Salud", "学习" to "Educación", "通讯" to "Comunicación", "人情" to "Regalos", "其他" to "Otros", "工资" to "Sueldo", "奖金" to "Bonificación", "理财" to "Inversiones", "兼职" to "Trabajo extra", "礼金" to "Dinero de regalo"
    )
    "ar" -> mapOf(
        "首页" to "الرئيسية", "明细" to "المعاملات", "统计" to "الإحصاءات", "我的" to "حسابي", "青禾记账" to "دفتر تشينغه", "认真生活，也清楚每一笔" to "عِش بوعي واعرف كل مصروف",
        "记一笔" to "إضافة", "编辑账单" to "تعديل المعاملة", "关闭" to "إغلاق", "支出" to "مصروف", "收入" to "دخل", "支出金额" to "قيمة المصروف", "收入金额" to "قيمة الدخل", "币种" to "العملة", "选择币种" to "اختيار العملة", "换算汇率" to "سعر الصرف", "查询" to "استعلام", "该货币暂不支持此功能" to "هذه العملة غير مدعومة حاليًا", "汇率查询失败，请检查网络后重试" to "تعذر جلب السعر. تحقق من الاتصال",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "قيمة 1 %s بـ %s · Frankfurter API · سعر يومي · قابل للتعديل", "数据日期 %s" to "تاريخ البيانات %s", "选择分类" to "الفئة", "备注（可选）" to "ملاحظة (اختياري)", "例如：和朋友聚餐" to "مثال: عشاء مع الأصدقاء", "今天" to "اليوم", "昨天" to "أمس", "日常账户" to "يومي", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "بطاقة بنكية", "现金" to "نقدًا", "不计入预算/统计" to "استبعاد من الميزانية", "计入预算/统计" to "إدراج في الميزانية", "保存这笔账" to "حفظ", "保存修改" to "حفظ التعديلات",
        "搜索人民币、卢布或 RUB" to "ابحث عن عملة أو رمز", "清空" to "مسح", "常用币种置顶 · %s 个结果" to "الشائعة أولًا · %s نتيجة", "已选择" to "محددة", "没有找到这个币种" to "لم يتم العثور على العملة", "不计统计" to "مستبعد", "退格" to "حذف",
        "最近账单" to "المعاملات الأخيرة", "共 %s 笔" to "%s معاملة", "本月消费排行" to "أعلى مصروفات الشهر", "%s年 %s月" to "%2\$s/%1\$s", "本月结余" to "رصيد الشهر", "本月收入" to "دخل الشهر", "本月支出" to "مصروف الشهر", "月度预算" to "الميزانية الشهرية", "还能花 %s" to "المتبقي %s", "已超支 %s" to "تجاوزت بـ %s", "从第一笔开始" to "ابدأ بأول معاملة", "每天花十秒，月底看清钱去了哪里" to "عشر ثوانٍ يوميًا لفهم مصروفاتك", "账单明细" to "كل المعاملات", "搜索分类、备注或账户" to "ابحث في الفئة أو الملاحظة أو الحساب", "全部" to "الكل", "点击账单可修改，长按可删除" to "اضغط للتعديل واضغط مطولًا للحذف", "还没有账单" to "لا توجد معاملات", "没有找到匹配账单" to "لا توجد نتائج", "支出 %s" to "مصروف %s",
        "删除这笔账单？" to "حذف هذه المعاملة؟", "%s · %s，删除后无法恢复。" to "%s · %s. لا يمكن التراجع.", "删除" to "حذف", "取消" to "إلغاء", "统计分析" to "التحليلات", "总支出" to "إجمالي المصروف", "总收入" to "إجمالي الدخل", "近 6 个月支出" to "آخر 6 أشهر", "分类占比" to "حسب الفئة", "%s 类" to "%s فئات", "本月还没有支出数据" to "لا توجد مصروفات هذا الشهر", "%s月" to "شهر %s",
        "我的账本" to "دفتري", "我的生活账本" to "دفتري الشخصي", "本机保存 · %s 笔记录" to "على الجهاز · %s سجل", "账本设置" to "إعدادات الدفتر", "隐藏金额" to "إخفاء المبالغ", "在公共场合保护隐私" to "حماية الخصوصية في الأماكن العامة", "深色模式" to "الوضع الداكن", "夜间查看更舒适" to "أريح ليلًا", "语言" to "اللغة", "惯用货币" to "العملة المفضلة", "汇率和统计金额将换算为此币种" to "الأسعار والإجماليات بهذه العملة", "数据管理" to "البيانات", "导出 CSV" to "تصدير CSV", "可用 Excel 打开" to "يفتح في Excel", "导入 CSV" to "استيراد CSV", "恢复青禾账单文件" to "استعادة ملف تشينغه", "清空全部账单" to "حذف الكل", "此操作不可恢复" to "لا يمكن التراجع", "隐私优先" to "الخصوصية أولًا", "账本只存本机；联网仅查询汇率，不上传任何账单" to "تبقى البيانات على الجهاز؛ الإنترنت فقط لأسعار الصرف",
        "设置月度预算" to "تعيين الميزانية", "预算金额" to "قيمة الميزانية", "保存" to "حفظ", "清空全部账单？" to "حذف كل المعاملات؟", "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "سيتم حذف %s معاملة نهائيًا. صدّر نسخة أولًا.", "确认清空" to "حذف الكل", "选择语言" to "اختيار اللغة", "选择惯用货币" to "اختيار العملة المفضلة", "正在换算历史账单…" to "جارٍ تحويل المعاملات…", "惯用货币已改为 %s" to "تم تغيير العملة إلى %s", "无法切换：该货币暂不支持汇率换算" to "لا يمكن التغيير: العملة غير مدعومة", "切换失败，请检查网络后重试" to "فشل التغيير. تحقق من الاتصال",
        "账单已成功导出" to "تم التصدير", "导出失败：%s" to "فشل التصدير: %s", "未知错误" to "خطأ غير معروف", "已导入 %s 笔，重复账单已自动跳过" to "تم استيراد %s وتخطي المكرر", "文件格式不正确" to "تنسيق غير صالح", "无法读取这个文件" to "تعذرت قراءة الملف",
        "餐饮" to "طعام", "交通" to "مواصلات", "购物" to "تسوق", "居住" to "سكن", "娱乐" to "ترفيه", "医疗" to "صحة", "学习" to "تعليم", "通讯" to "اتصالات", "人情" to "هدايا", "其他" to "أخرى", "工资" to "راتب", "奖金" to "مكافأة", "理财" to "استثمارات", "兼职" to "عمل إضافي", "礼金" to "هدية مالية"
    )
    "fr" -> mapOf(
        "首页" to "Accueil", "明细" to "Opérations", "统计" to "Statistiques", "我的" to "Moi", "青禾记账" to "Qinghe Budget", "认真生活，也清楚每一笔" to "Vivez pleinement, suivez chaque dépense", "记一笔" to "Ajouter", "编辑账单" to "Modifier l’opération", "关闭" to "Fermer", "支出" to "Dépense", "收入" to "Revenu", "支出金额" to "Montant dépensé", "收入金额" to "Montant reçu", "币种" to "Devise", "选择币种" to "Choisir une devise", "换算汇率" to "Taux de change", "查询" to "Rechercher", "该货币暂不支持此功能" to "Cette devise n’est pas encore prise en charge", "汇率查询失败，请检查网络后重试" to "Taux indisponible. Vérifiez la connexion",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "1 %s en %s · Frankfurter API · Taux quotidien · Modifiable", "数据日期 %s" to "Date %s", "选择分类" to "Catégorie", "备注（可选）" to "Note (facultative)", "例如：和朋友聚餐" to "ex. dîner entre amis", "今天" to "Aujourd’hui", "昨天" to "Hier", "日常账户" to "Quotidien", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "Carte bancaire", "现金" to "Espèces", "不计入预算/统计" to "Exclure du budget", "计入预算/统计" to "Inclure dans le budget", "保存这笔账" to "Enregistrer", "保存修改" to "Enregistrer les modifications",
        "搜索人民币、卢布或 RUB" to "Rechercher une devise ou un code", "清空" to "Effacer", "常用币种置顶 · %s 个结果" to "Devises courantes · %s résultats", "已选择" to "Sélectionnée", "没有找到这个币种" to "Aucune devise trouvée", "不计统计" to "Exclu", "退格" to "Retour",
        "最近账单" to "Opérations récentes", "共 %s 笔" to "%s opérations", "本月消费排行" to "Principales dépenses du mois", "%s年 %s月" to "%2\$s/%1\$s", "本月结余" to "Solde mensuel", "本月收入" to "Revenus du mois", "本月支出" to "Dépenses du mois", "月度预算" to "Budget mensuel", "还能花 %s" to "%s restants", "已超支 %s" to "%s au-dessus du budget", "从第一笔开始" to "Ajoutez votre première opération", "每天花十秒，月底看清钱去了哪里" to "Dix secondes par jour pour comprendre votre mois", "账单明细" to "Toutes les opérations", "搜索分类、备注或账户" to "Rechercher catégorie, note ou compte", "全部" to "Toutes", "点击账单可修改，长按可删除" to "Touchez pour modifier, maintenez pour supprimer", "还没有账单" to "Aucune opération", "没有找到匹配账单" to "Aucun résultat", "支出 %s" to "Dépense %s",
        "删除这笔账单？" to "Supprimer cette opération ?", "%s · %s，删除后无法恢复。" to "%s · %s. Action irréversible.", "删除" to "Supprimer", "取消" to "Annuler", "统计分析" to "Analyse", "总支出" to "Dépenses totales", "总收入" to "Revenus totaux", "近 6 个月支出" to "6 derniers mois", "分类占比" to "Répartition", "%s 类" to "%s catégories", "本月还没有支出数据" to "Aucune dépense ce mois-ci", "%s月" to "M%s",
        "我的账本" to "Mon budget", "我的生活账本" to "Mon budget personnel", "本机保存 · %s 笔记录" to "Sur l’appareil · %s entrées", "账本设置" to "Réglages", "隐藏金额" to "Masquer les montants", "在公共场合保护隐私" to "Protéger la confidentialité", "深色模式" to "Mode sombre", "夜间查看更舒适" to "Plus confortable la nuit", "语言" to "Langue", "惯用货币" to "Devise préférée", "汇率和统计金额将换算为此币种" to "Les taux et totaux utilisent cette devise", "数据管理" to "Données", "导出 CSV" to "Exporter en CSV", "可用 Excel 打开" to "Compatible avec Excel", "导入 CSV" to "Importer un CSV", "恢复青禾账单文件" to "Restaurer un fichier Qinghe", "清空全部账单" to "Tout supprimer", "此操作不可恢复" to "Action irréversible", "隐私优先" to "Confidentialité d’abord", "账本只存本机；联网仅查询汇率，不上传任何账单" to "Les données restent sur l’appareil ; internet sert uniquement aux taux",
        "设置月度预算" to "Définir le budget", "预算金额" to "Montant du budget", "保存" to "Enregistrer", "清空全部账单？" to "Supprimer toutes les opérations ?", "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "%s opérations seront supprimées. Exportez d’abord une sauvegarde.", "确认清空" to "Tout supprimer", "选择语言" to "Choisir la langue", "选择惯用货币" to "Choisir la devise préférée", "正在换算历史账单…" to "Conversion des opérations…", "惯用货币已改为 %s" to "Devise préférée : %s", "无法切换：该货币暂不支持汇率换算" to "Impossible : devise non prise en charge", "切换失败，请检查网络后重试" to "Échec. Vérifiez la connexion",
        "账单已成功导出" to "Exportation réussie", "导出失败：%s" to "Échec de l’export : %s", "未知错误" to "Erreur inconnue", "已导入 %s 笔，重复账单已自动跳过" to "%s importées, doublons ignorés", "文件格式不正确" to "Format incorrect", "无法读取这个文件" to "Impossible de lire le fichier",
        "餐饮" to "Repas", "交通" to "Transport", "购物" to "Achats", "居住" to "Logement", "娱乐" to "Loisirs", "医疗" to "Santé", "学习" to "Études", "通讯" to "Communication", "人情" to "Cadeaux", "其他" to "Autres", "工资" to "Salaire", "奖金" to "Prime", "理财" to "Placements", "兼职" to "Travail annexe", "礼金" to "Argent offert"
    )
    "ru" -> mapOf(
        "首页" to "Главная", "明细" to "Операции", "统计" to "Статистика", "我的" to "Профиль", "青禾记账" to "Qinghe Финансы", "认真生活，也清楚每一笔" to "Живите осознанно и знайте каждый расход", "记一笔" to "Добавить", "编辑账单" to "Изменить операцию", "关闭" to "Закрыть", "支出" to "Расход", "收入" to "Доход", "支出金额" to "Сумма расхода", "收入金额" to "Сумма дохода", "币种" to "Валюта", "选择币种" to "Выбрать валюту", "换算汇率" to "Курс", "查询" to "Получить", "该货币暂不支持此功能" to "Эта валюта пока не поддерживается", "汇率查询失败，请检查网络后重试" to "Не удалось получить курс. Проверьте сеть",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "1 %s в %s · Frankfurter API · Ежедневный курс · Можно изменить", "数据日期 %s" to "Дата данных %s", "选择分类" to "Категория", "备注（可选）" to "Заметка (необязательно)", "例如：和朋友聚餐" to "например, ужин с друзьями", "今天" to "Сегодня", "昨天" to "Вчера", "日常账户" to "Повседневный", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "Банковская карта", "现金" to "Наличные", "不计入预算/统计" to "Исключить из бюджета", "计入预算/统计" to "Учесть в бюджете", "保存这笔账" to "Сохранить", "保存修改" to "Сохранить изменения",
        "搜索人民币、卢布或 RUB" to "Поиск валюты или кода", "清空" to "Очистить", "常用币种置顶 · %s 个结果" to "Популярные сверху · %s результатов", "已选择" to "Выбрано", "没有找到这个币种" to "Валюта не найдена", "不计统计" to "Исключено", "退格" to "Удалить",
        "最近账单" to "Недавние операции", "共 %s 笔" to "%s операций", "本月消费排行" to "Главные расходы месяца", "%s年 %s月" to "%2\$s/%1\$s", "本月结余" to "Баланс за месяц", "本月收入" to "Доход за месяц", "本月支出" to "Расход за месяц", "月度预算" to "Месячный бюджет", "还能花 %s" to "Осталось %s", "已超支 %s" to "Перерасход %s", "从第一笔开始" to "Добавьте первую операцию", "每天花十秒，月底看清钱去了哪里" to "Десять секунд в день — и месяц понятен", "账单明细" to "Все операции", "搜索分类、备注或账户" to "Поиск по категории, заметке или счёту", "全部" to "Все", "点击账单可修改，长按可删除" to "Нажмите для правки, удерживайте для удаления", "还没有账单" to "Операций пока нет", "没有找到匹配账单" to "Совпадений нет", "支出 %s" to "Расход %s",
        "删除这笔账单？" to "Удалить эту операцию?", "%s · %s，删除后无法恢复。" to "%s · %s. Отменить нельзя.", "删除" to "Удалить", "取消" to "Отмена", "统计分析" to "Аналитика", "总支出" to "Всего расходов", "总收入" to "Всего доходов", "近 6 个月支出" to "Последние 6 месяцев", "分类占比" to "По категориям", "%s 类" to "%s категорий", "本月还没有支出数据" to "В этом месяце расходов нет", "%s月" to "Месяц %s",
        "我的账本" to "Моя книга", "我的生活账本" to "Мои личные финансы", "本机保存 · %s 笔记录" to "На устройстве · %s записей", "账本设置" to "Настройки", "隐藏金额" to "Скрывать суммы", "在公共场合保护隐私" to "Защита данных на людях", "深色模式" to "Тёмная тема", "夜间查看更舒适" to "Комфортнее ночью", "语言" to "Язык", "惯用货币" to "Основная валюта", "汇率和统计金额将换算为此币种" to "Курсы и итоги будут в этой валюте", "数据管理" to "Данные", "导出 CSV" to "Экспорт CSV", "可用 Excel 打开" to "Открывается в Excel", "导入 CSV" to "Импорт CSV", "恢复青禾账单文件" to "Восстановить файл Qinghe", "清空全部账单" to "Удалить все", "此操作不可恢复" to "Отменить нельзя", "隐私优先" to "Конфиденциальность", "账本只存本机；联网仅查询汇率，不上传任何账单" to "Записи хранятся на устройстве; интернет нужен только для курсов",
        "设置月度预算" to "Задать бюджет", "预算金额" to "Сумма бюджета", "保存" to "Сохранить", "清空全部账单？" to "Удалить все операции?", "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "%s операций будут удалены. Сначала экспортируйте копию.", "确认清空" to "Удалить все", "选择语言" to "Выбрать язык", "选择惯用货币" to "Выбрать основную валюту", "正在换算历史账单…" to "Пересчёт операций…", "惯用货币已改为 %s" to "Основная валюта: %s", "无法切换：该货币暂不支持汇率换算" to "Нельзя выбрать: валюта не поддерживается", "切换失败，请检查网络后重试" to "Ошибка. Проверьте сеть",
        "账单已成功导出" to "Экспорт завершён", "导出失败：%s" to "Ошибка экспорта: %s", "未知错误" to "Неизвестная ошибка", "已导入 %s 笔，重复账单已自动跳过" to "Импортировано %s, дубликаты пропущены", "文件格式不正确" to "Неверный формат", "无法读取这个文件" to "Не удалось прочитать файл",
        "餐饮" to "Еда", "交通" to "Транспорт", "购物" to "Покупки", "居住" to "Жильё", "娱乐" to "Развлечения", "医疗" to "Здоровье", "学习" to "Учёба", "通讯" to "Связь", "人情" to "Подарки", "其他" to "Другое", "工资" to "Зарплата", "奖金" to "Премия", "理财" to "Инвестиции", "兼职" to "Подработка", "礼金" to "Подарочные деньги"
    )
    "pt" -> mapOf(
        "首页" to "Início", "明细" to "Lançamentos", "统计" to "Estatísticas", "我的" to "Perfil", "青禾记账" to "Qinghe Finanças", "认真生活，也清楚每一笔" to "Viva com atenção e conheça cada gasto", "记一笔" to "Adicionar", "编辑账单" to "Editar lançamento", "关闭" to "Fechar", "支出" to "Despesa", "收入" to "Receita", "支出金额" to "Valor da despesa", "收入金额" to "Valor da receita", "币种" to "Moeda", "选择币种" to "Escolher moeda", "换算汇率" to "Taxa de câmbio", "查询" to "Consultar", "该货币暂不支持此功能" to "Esta moeda ainda não é compatível", "汇率查询失败，请检查网络后重试" to "Não foi possível obter a taxa. Verifique a conexão",
        "1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改" to "1 %s em %s · Frankfurter API · Taxa diária · Editável", "数据日期 %s" to "Data %s", "选择分类" to "Categoria", "备注（可选）" to "Nota (opcional)", "例如：和朋友聚餐" to "ex.: jantar com amigos", "今天" to "Hoje", "昨天" to "Ontem", "日常账户" to "Cotidiano", "微信" to "WeChat", "支付宝" to "Alipay", "银行卡" to "Cartão", "现金" to "Dinheiro", "不计入预算/统计" to "Excluir do orçamento", "计入预算/统计" to "Incluir no orçamento", "保存这笔账" to "Salvar", "保存修改" to "Salvar alterações",
        "搜索人民币、卢布或 RUB" to "Buscar moeda ou código", "清空" to "Limpar", "常用币种置顶 · %s 个结果" to "Populares primeiro · %s resultados", "已选择" to "Selecionada", "没有找到这个币种" to "Moeda não encontrada", "不计统计" to "Excluído", "退格" to "Apagar",
        "最近账单" to "Lançamentos recentes", "共 %s 笔" to "%s lançamentos", "本月消费排行" to "Maiores gastos do mês", "%s年 %s月" to "%2\$s/%1\$s", "本月结余" to "Saldo mensal", "本月收入" to "Receita do mês", "本月支出" to "Despesa do mês", "月度预算" to "Orçamento mensal", "还能花 %s" to "Restam %s", "已超支 %s" to "%s acima do orçamento", "从第一笔开始" to "Comece pelo primeiro lançamento", "每天花十秒，月底看清钱去了哪里" to "Dez segundos por dia para entender seu mês", "账单明细" to "Todos os lançamentos", "搜索分类、备注或账户" to "Buscar categoria, nota ou conta", "全部" to "Todos", "点击账单可修改，长按可删除" to "Toque para editar, segure para excluir", "还没有账单" to "Nenhum lançamento", "没有找到匹配账单" to "Nenhum resultado", "支出 %s" to "Despesa %s",
        "删除这笔账单？" to "Excluir este lançamento?", "%s · %s，删除后无法恢复。" to "%s · %s. Não é possível desfazer.", "删除" to "Excluir", "取消" to "Cancelar", "统计分析" to "Análises", "总支出" to "Despesa total", "总收入" to "Receita total", "近 6 个月支出" to "Últimos 6 meses", "分类占比" to "Por categoria", "%s 类" to "%s categorias", "本月还没有支出数据" to "Sem despesas neste mês", "%s月" to "M%s",
        "我的账本" to "Meu livro", "我的生活账本" to "Minhas finanças", "本机保存 · %s 笔记录" to "No dispositivo · %s registros", "账本设置" to "Configurações", "隐藏金额" to "Ocultar valores", "在公共场合保护隐私" to "Proteja sua privacidade", "深色模式" to "Modo escuro", "夜间查看更舒适" to "Mais confortável à noite", "语言" to "Idioma", "惯用货币" to "Moeda preferida", "汇率和统计金额将换算为此币种" to "Taxas e totais usarão esta moeda", "数据管理" to "Dados", "导出 CSV" to "Exportar CSV", "可用 Excel 打开" to "Abre no Excel", "导入 CSV" to "Importar CSV", "恢复青禾账单文件" to "Restaurar arquivo Qinghe", "清空全部账单" to "Excluir tudo", "此操作不可恢复" to "Não é possível desfazer", "隐私优先" to "Privacidade primeiro", "账本只存本机；联网仅查询汇率，不上传任何账单" to "Os dados ficam no dispositivo; internet só para taxas",
        "设置月度预算" to "Definir orçamento", "预算金额" to "Valor do orçamento", "保存" to "Salvar", "清空全部账单？" to "Excluir todos os lançamentos?", "将永久删除 %s 笔账单。建议先导出 CSV 备份。" to "%s lançamentos serão excluídos. Exporte uma cópia antes.", "确认清空" to "Excluir tudo", "选择语言" to "Escolher idioma", "选择惯用货币" to "Escolher moeda preferida", "正在换算历史账单…" to "Convertendo lançamentos…", "惯用货币已改为 %s" to "Moeda preferida: %s", "无法切换：该货币暂不支持汇率换算" to "Não é possível: moeda não compatível", "切换失败，请检查网络后重试" to "Falha ao trocar. Verifique a conexão",
        "账单已成功导出" to "Exportação concluída", "导出失败：%s" to "Falha ao exportar: %s", "未知错误" to "Erro desconhecido", "已导入 %s 笔，重复账单已自动跳过" to "%s importados; duplicados ignorados", "文件格式不正确" to "Formato inválido", "无法读取这个文件" to "Não foi possível ler o arquivo",
        "餐饮" to "Alimentação", "交通" to "Transporte", "购物" to "Compras", "居住" to "Moradia", "娱乐" to "Lazer", "医疗" to "Saúde", "学习" to "Educação", "通讯" to "Comunicação", "人情" to "Presentes", "其他" to "Outros", "工资" to "Salário", "奖金" to "Bônus", "理财" to "Investimentos", "兼职" to "Trabalho extra", "礼金" to "Dinheiro de presente"
    )
    else -> emptyMap()
}
