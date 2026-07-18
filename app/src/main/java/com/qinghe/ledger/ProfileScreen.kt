package com.qinghe.ledger

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.qinghe.ledger.data.LedgerTransaction
import com.qinghe.ledger.ui.theme.ExpenseRed
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun ProfileScreen(
    transactions: List<LedgerTransaction>,
    budget: Long,
    hidden: Boolean,
    dark: Boolean,
    language: AppLanguage,
    baseCurrencyCode: String,
    onBudget: (Long) -> Unit,
    onHidden: (Boolean) -> Unit,
    onDark: (Boolean) -> Unit,
    onLanguage: (String) -> Unit,
    onBaseCurrency: suspend (String) -> Result<ExchangeRateQuote>,
    onClear: () -> Unit,
    exportCsv: () -> String,
    importCsv: suspend (String) -> Result<Int>,
    snackbar: SnackbarHostState
) {
    val i18n = LocalI18n.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBudget by rememberSaveable { mutableStateOf(false) }
    var showClear by rememberSaveable { mutableStateOf(false) }
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    var showBaseCurrency by rememberSaveable { mutableStateOf(false) }
    var converting by rememberSaveable { mutableStateOf(false) }
    val baseSymbol = remember(baseCurrencyCode, language) {
        runCatching { java.util.Currency.getInstance(baseCurrencyCode).getSymbol(language.locale) }.getOrDefault(baseCurrencyCode)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(exportCsv()) } }
            .onSuccess { scope.launch { snackbar.showSnackbar(i18n("账单已成功导出")) } }
            .onFailure { scope.launch { snackbar.showSnackbar(i18n("导出失败：%s", it.localizedMessage ?: i18n("未知错误"))) } }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val text = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty() }
            text.fold(
                onSuccess = { csv -> importCsv(csv).fold(onSuccess = { snackbar.showSnackbar(i18n("已导入 %s 笔，重复账单已自动跳过", it)) }, onFailure = { snackbar.showSnackbar(i18n("文件格式不正确")) }) },
                onFailure = { snackbar.showSnackbar(i18n("无法读取这个文件")) }
            )
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text(i18n("我的账本"), style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(58.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Savings, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp)) }
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(i18n("我的生活账本"), style = MaterialTheme.typography.titleLarge)
                        Text(i18n("本机保存 · %s 笔记录", transactions.size), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                    Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Text(i18n("账本设置"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            SettingsCard {
                SettingsRow(Icons.Rounded.Savings, i18n("月度预算"), moneyProfile(budget), onClick = { showBudget = true })
                HorizontalDivider(Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsSwitch(if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, i18n("隐藏金额"), i18n("在公共场合保护隐私"), hidden, onHidden)
                HorizontalDivider(Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsSwitch(if (dark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, i18n("深色模式"), i18n("夜间查看更舒适"), dark, onDark)
                HorizontalDivider(Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsRow(Icons.Rounded.Language, i18n("语言"), language.nativeName, onClick = { showLanguage = true })
                HorizontalDivider(Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsRow(Icons.Rounded.AttachMoney, i18n("惯用货币"), "$baseCurrencyCode · ${i18n("汇率和统计金额将换算为此币种")}", onClick = { showBaseCurrency = true })
            }
        }
        item { Text(i18n("数据管理"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            SettingsCard {
                SettingsRow(Icons.Rounded.FileDownload, i18n("导出 CSV"), i18n("可用 Excel 打开"), onClick = {
                    val name = "青禾账单_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.csv"
                    exportLauncher.launch(name)
                })
                HorizontalDivider(Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsRow(Icons.Rounded.FileUpload, i18n("导入 CSV"), i18n("恢复青禾账单文件"), onClick = { importLauncher.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel")) })
                HorizontalDivider(Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsRow(Icons.Rounded.DeleteOutline, i18n("清空全部账单"), i18n("此操作不可恢复"), tint = ExpenseRed, onClick = { showClear = true })
            }
        }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CloudOff, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(i18n("隐私优先"), fontWeight = FontWeight.SemiBold)
                    Text(i18n("账本只存本机；联网仅查询汇率，不上传任何账单"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
        item { Text("${i18n("青禾记账")} 1.4.0", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
    }

    if (showBudget) {
        var text by remember { mutableStateOf(BigDecimal(budget).movePointLeft(2).stripTrailingZeros().toPlainString()) }
        AlertDialog(
            onDismissRequest = { showBudget = false },
            title = { Text(i18n("设置月度预算")) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 12) text = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(i18n("预算金额")) },
                    prefix = { Text("$baseSymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    text.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }?.let {
                        onBudget(it.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact())
                        showBudget = false
                    }
                }) { Text(i18n("保存")) }
            },
            dismissButton = { TextButton(onClick = { showBudget = false }) { Text(i18n("取消")) } }
        )
    }
    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, null, tint = ExpenseRed) },
            title = { Text(i18n("清空全部账单？")) },
            text = { Text(i18n("将永久删除 %s 笔账单。建议先导出 CSV 备份。", transactions.size)) },
            confirmButton = { TextButton(onClick = { onClear(); showClear = false }) { Text(i18n("确认清空"), color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { showClear = false }) { Text(i18n("取消")) } }
        )
    }
    if (showLanguage) {
        AlertDialog(
            onDismissRequest = { showLanguage = false },
            title = { Text(i18n("选择语言")) },
            text = {
                Column {
                    AppLanguage.entries.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                                onLanguage(option.code)
                                showLanguage = false
                            }.padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.nativeName, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            if (option == language) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguage = false }) { Text(i18n("关闭")) } }
        )
    }
    if (showBaseCurrency) {
        CurrencyPickerDialog(
            selectedCode = baseCurrencyCode,
            titleKey = "选择惯用货币",
            onDismiss = { showBaseCurrency = false },
            onSelect = { option ->
                showBaseCurrency = false
                if (option.code != baseCurrencyCode) {
                    converting = true
                    scope.launch {
                        onBaseCurrency(option.code).fold(
                            onSuccess = { snackbar.showSnackbar(i18n("惯用货币已改为 %s", option.code)) },
                            onFailure = {
                                val message = if (it is UnsupportedCurrencyException) i18n("无法切换：该货币暂不支持汇率换算") else i18n("切换失败，请检查网络后重试")
                                snackbar.showSnackbar(message)
                            }
                        )
                        converting = false
                    }
                }
            }
        )
    }
    if (converting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(i18n("正在换算历史账单…")) },
            text = { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() } },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(content = { content() }) }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, tint: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = .12f), modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)) }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SettingsSwitch(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp)) }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun moneyProfile(cents: Long): String = baseMoney(cents)
