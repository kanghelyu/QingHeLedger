package com.qinghe.ledger

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.qinghe.ledger.data.CategoryOption
import com.qinghe.ledger.data.CurrencyOption
import com.qinghe.ledger.data.LedgerTransaction
import com.qinghe.ledger.data.expenseCategories
import com.qinghe.ledger.data.incomeCategories
import com.qinghe.ledger.data.supportedCurrencies
import com.qinghe.ledger.ui.theme.ExpenseRed
import com.qinghe.ledger.ui.theme.IncomeGreen
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun SectionTitle(title: String, trailing: String?) {
    val i18n = LocalI18n.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(i18n(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.let { Text(i18n(it), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
    }
}

@Composable
internal fun TransactionRow(item: LedgerTransaction, hidden: Boolean) {
    val i18n = LocalI18n.current
    val baseCurrency = LocalBaseCurrencyCode.current
    val baseAmountText = moneyPublic(item.amountCents)
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        CategoryIcon(item.categoryIcon, item.categoryColor, 46)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(i18n(item.category), fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                item.note.ifBlank { i18n(item.account) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (item.type == "EXPENSE") "− " else "+ ") + transactionMoney(item, hidden),
                fontWeight = FontWeight.SemiBold,
                color = if (item.type == "EXPENSE") MaterialTheme.colorScheme.onSurface else IncomeGreen
            )
            val time = java.time.Instant.ofEpochMilli(item.occurredAt).atZone(ZoneId.systemDefault())
            Text(
                buildString {
                    append(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    if (item.currencyCode != baseCurrency) append(" · ≈$baseAmountText")
                    if (item.excludedFromStats) append(" · ${i18n("不计统计")}")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
internal fun CategoryIcon(icon: String, color: Long, size: Int) {
    Surface(shape = RoundedCornerShape((size * .32f).dp), color = Color(color).copy(alpha = .14f), modifier = Modifier.size(size.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(iconFor(icon), null, tint = Color(color), modifier = Modifier.size((size * .48f).dp))
        }
    }
}

private fun iconFor(name: String): ImageVector = when (name) {
    "restaurant" -> Icons.Rounded.Restaurant
    "commute" -> Icons.Rounded.Commute
    "shopping" -> Icons.Rounded.ShoppingBag
    "home" -> Icons.Rounded.Home
    "movie" -> Icons.Rounded.Movie
    "health" -> Icons.Rounded.HealthAndSafety
    "school" -> Icons.Rounded.School
    "phone" -> Icons.Rounded.PhoneAndroid
    "gift", "bonus" -> Icons.Rounded.CardGiftcard
    "salary", "work" -> Icons.Rounded.Work
    "trend" -> Icons.Rounded.TrendingUp
    else -> Icons.Rounded.MoreHoriz
}

@Composable
private fun moneyPublic(cents: Long, hidden: Boolean = false): String = baseMoney(cents, hidden)

@Composable
private fun transactionMoney(item: LedgerTransaction, hidden: Boolean): String {
    if (hidden) return "••••"
    if (item.currencyCode == LocalBaseCurrencyCode.current) return moneyPublic(item.amountCents)
    val option = supportedCurrencies.firstOrNull { it.code == item.currencyCode }
    val amount = BigDecimal(item.originalAmountMinor).movePointLeft(2).stripTrailingZeros().toPlainString()
    return "${localizedCurrencySymbol(option?.code ?: item.currencyCode, LocalI18n.current)} $amount"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTransactionSheet(
    existing: LedgerTransaction? = null,
    onDismiss: () -> Unit,
    onFetchRate: suspend (String) -> Result<ExchangeRateQuote>,
    onSave: (String, Long, CategoryOption, String, String, Long, String, Double, Boolean) -> Unit
) {
    val i18n = LocalI18n.current
    val baseCurrency = LocalBaseCurrencyCode.current
    var type by rememberSaveable(existing?.id) { mutableStateOf(existing?.type ?: "EXPENSE") }
    val initialOriginal = existing?.originalAmountMinor?.takeIf { it > 0 } ?: existing?.amountCents ?: 0L
    var expression by rememberSaveable(existing?.id) {
        mutableStateOf(if (existing == null) "" else BigDecimal(initialOriginal).movePointLeft(2).stripTrailingZeros().toPlainString())
    }
    val initialCategories = if (type == "EXPENSE") expenseCategories else incomeCategories
    var category by remember(existing?.id) {
        mutableStateOf(
            initialCategories.firstOrNull { it.name == existing?.category }
                ?: existing?.let { CategoryOption(it.category, it.categoryIcon, it.categoryColor) }
                ?: initialCategories.first()
        )
    }
    var note by rememberSaveable(existing?.id) { mutableStateOf(existing?.note.orEmpty()) }
    var account by rememberSaveable(existing?.id) { mutableStateOf(existing?.account ?: "日常账户") }
    var dateChoice by rememberSaveable(existing?.id) { mutableStateOf(if (existing == null) "TODAY" else "KEEP") }
    var currencyCode by rememberSaveable(existing?.id) { mutableStateOf(existing?.currencyCode ?: baseCurrency) }
    var exchangeRate by rememberSaveable(existing?.id) {
        mutableStateOf(if (currencyCode == baseCurrency) "1" else existing?.exchangeRate?.toString().orEmpty())
    }
    var rateLoading by rememberSaveable(existing?.id) { mutableStateOf(false) }
    var rateDate by rememberSaveable(existing?.id) { mutableStateOf<String?>(null) }
    var rateError by rememberSaveable(existing?.id) { mutableStateOf<String?>(null) }
    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    var excluded by rememberSaveable(existing?.id) { mutableStateOf(existing?.excludedFromStats ?: false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = if (type == "EXPENSE") expenseCategories else incomeCategories
    val selectedCurrency = supportedCurrencies.firstOrNull { it.code == currencyCode }
        ?: supportedCurrencies.firstOrNull { it.code == baseCurrency }
        ?: supportedCurrencies.first()
    val calculatedAmount = evaluateExpression(expression)
    val heroAmountLabel = "${localizedCurrencySymbol(selectedCurrency.code, i18n)} ${compactAmountPreview(calculatedAmount, expression)}"
    val heroAmountFontSize = when {
        heroAmountLabel.length >= 24 -> 22.sp
        heroAmountLabel.length >= 18 -> 27.sp
        heroAmountLabel.length >= 14 -> 31.sp
        else -> 34.sp
    }
    val rateValue = if (currencyCode == baseCurrency) 1.0 else exchangeRate.toDoubleOrNull()
    val validAmount = calculatedAmount?.let { it > BigDecimal.ZERO && it <= BigDecimal("99999999.99") } == true &&
        rateValue != null && rateValue > 0 && rateValue <= 1_000_000
    val listState = rememberLazyListState()
    val showFloatingAmount by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= 3 }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, dragHandle = null) {
        Box(Modifier.fillMaxWidth().imePadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(i18n(if (existing == null) "记一笔" else "编辑账单"), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    IconButton(onDismiss) { Icon(Icons.Rounded.Close, i18n("关闭")) }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
                    TypeButton(i18n("支出"), type == "EXPENSE", ExpenseRed, Modifier.weight(1f)) {
                        type = "EXPENSE"; category = expenseCategories.first()
                    }
                    TypeButton(i18n("收入"), type == "INCOME", IncomeGreen, Modifier.weight(1f)) {
                        type = "INCOME"; category = incomeCategories.first()
                    }
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(i18n(if (type == "EXPENSE") "支出金额" else "收入金额"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text(
                        heroAmountLabel,
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = heroAmountFontSize,
                        color = if (type == "EXPENSE") ExpenseRed else IncomeGreen,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                    if (calculatedAmount != null && expression.any { it in "+-×÷" }) {
                        Text(expression, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            item {
                Text(i18n("币种"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Card(
                    onClick = { showCurrencyPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(localizedCurrencySymbol(selectedCurrency.code, i18n), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(localizedCurrencyName(selectedCurrency.code, i18n), fontWeight = FontWeight.SemiBold)
                            Text(selectedCurrency.code, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, i18n("选择币种"), tint = MaterialTheme.colorScheme.outline)
                    }
                }
                if (currencyCode != baseCurrency) {
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = exchangeRate,
                                onValueChange = { value ->
                                    val filtered = value.filter { it.isDigit() || it == '.' }
                                    if (filtered.count { it == '.' } <= 1 && filtered.length <= 10) {
                                        exchangeRate = filtered
                                        rateDate = null
                                        rateError = null
                                    }
                                },
                                label = { Text(i18n("换算汇率")) },
                                suffix = { Text(baseCurrency) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        rateLoading = true
                                        rateError = null
                                        onFetchRate(currencyCode).fold(
                                            onSuccess = { quote ->
                                                exchangeRate = BigDecimal.valueOf(quote.rate).stripTrailingZeros().toPlainString()
                                                rateDate = quote.date
                                            },
                                            onFailure = { error ->
                                                val message = i18n(if (error is UnsupportedCurrencyException) "该货币暂不支持此功能" else "汇率查询失败，请检查网络后重试")
                                                rateError = message
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        rateLoading = false
                                    }
                                },
                                enabled = !rateLoading,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                if (rateLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(i18n("查询"))
                                }
                            }
                        }
                        Text(
                            buildString {
                                append(i18n("1 %s = 多少 %s · Frankfurter API 提供 · 日更参考汇率 · 可手动修改", selectedCurrency.code, baseCurrency))
                                rateDate?.let { append(" · ${i18n("数据日期 %s", it)}") }
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 8.dp)
                        )
                        rateError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }
            item {
                Text(i18n("选择分类"), style = MaterialTheme.typography.titleMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxWidth().height(if (categories.size > 5) 190.dp else 92.dp),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().height(82.dp).clip(RoundedCornerShape(12.dp)).clickable { category = item }.padding(2.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (category == item) Color(item.color) else Color(item.color).copy(alpha = .13f),
                                modifier = Modifier.size(42.dp)
                            ) { Box(contentAlignment = Alignment.Center) { Icon(iconFor(item.icon), null, tint = if (category == item) Color.White else Color(item.color), modifier = Modifier.size(21.dp)) } }
                            Text(
                                text = i18n(item.name),
                                fontSize = if (i18n.language == AppLanguage.ZH) 10.sp else 8.sp,
                                lineHeight = if (i18n.language == AppLanguage.ZH) 12.sp else 10.sp,
                                color = if (category == item) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 40) note = it },
                    label = { Text(i18n("备注（可选）")) },
                    placeholder = { Text(i18n("例如：和朋友聚餐")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val choices = buildList {
                        if (existing != null) {
                            val oldDate = java.time.Instant.ofEpochMilli(existing.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
                            add("KEEP" to oldDate.format(DateTimeFormatter.ofPattern("MMM d", i18n.language.locale)))
                        }
                        add("TODAY" to i18n("今天"))
                        add("YESTERDAY" to i18n("昨天"))
                    }
                    choices.forEach { choice ->
                        AssistChip(onClick = { dateChoice = choice.first }, label = { Text(choice.second) }, leadingIcon = if (dateChoice == choice.first) ({ Icon(Icons.Rounded.Check, null, Modifier.size(17.dp)) }) else null)
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 8.dp)) {
                    items(listOf("日常账户", "微信", "支付宝", "银行卡", "现金")) { item ->
                        FilterChip(selected = account == item, onClick = { account = item }, label = { Text(i18n(item)) })
                    }
                }
                AssistChip(
                    onClick = { excluded = !excluded },
                    label = { Text(i18n(if (excluded) "不计入预算/统计" else "计入预算/统计")) },
                    leadingIcon = if (excluded) ({ Icon(Icons.Rounded.Check, null, Modifier.size(17.dp)) }) else null,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item { CalculatorPad(expression, onChange = { expression = it }) }
                item {
                    Button(
                    onClick = {
                        val originalMinor = calculatedAmount!!.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
                        val time = if (existing != null && dateChoice == "KEEP") existing.occurredAt else {
                            val date = if (dateChoice == "TODAY") LocalDate.now() else LocalDate.now().minusDays(1)
                            val clock = existing?.let { java.time.Instant.ofEpochMilli(it.occurredAt).atZone(ZoneId.systemDefault()).toLocalTime() } ?: java.time.LocalTime.now()
                            date.atTime(clock).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                        onSave(type, originalMinor, category, note, account, time, currencyCode, rateValue!!, excluded)
                    },
                    enabled = validAmount,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (type == "EXPENSE") ExpenseRed else IncomeGreen)
                    ) { Text(i18n(if (existing == null) "保存这笔账" else "保存修改"), fontWeight = FontWeight.Bold) }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showFloatingAmount,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .zIndex(1f)
            ) {
                FloatingAmountPreview(
                    currencySymbol = localizedCurrencySymbol(selectedCurrency.code, i18n),
                    amount = calculatedAmount,
                    expression = expression,
                    color = if (type == "EXPENSE") ExpenseRed else IncomeGreen
                )
            }
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCode = currencyCode,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { option ->
                currencyCode = option.code
                exchangeRate = if (option.code == baseCurrency) "1" else ""
                rateDate = null
                rateError = null
                showCurrencyPicker = false
            }
        )
    }
}

@Composable
private fun TypeButton(label: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) { Text(label, color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 11.dp)) }
}

@Composable
private fun FloatingAmountPreview(
    currencySymbol: String,
    amount: BigDecimal?,
    expression: String,
    color: Color
) {
    val amountText = compactAmountPreview(amount, expression)
    val amountFontSize = when {
        amountText.length >= 24 -> 14.sp
        amountText.length >= 18 -> 16.sp
        amountText.length >= 14 -> 19.sp
        else -> 22.sp
    }
    Surface(
        modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().height(63.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .86f),
        contentColor = color,
        tonalElevation = 5.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .09f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(color.copy(alpha = .82f)))
            Text(
                text = currencySymbol,
                color = color.copy(alpha = .82f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 48.dp)
            )
            Text(
                text = amountText,
                color = color,
                fontSize = amountFontSize,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun compactAmountPreview(amount: BigDecimal?, expression: String): String {
    if (amount != null) {
        val plain = amount.stripTrailingZeros().toPlainString()
        return if (plain.length <= 18) plain else amount.round(MathContext(6)).toString()
    }
    val liveExpression = expression.ifBlank { "0.00" }.replace("−", "-")
    return if (liveExpression.length <= 22) liveExpression else "…${liveExpression.takeLast(21)}"
}

@Composable
internal fun CurrencyPickerDialog(selectedCode: String, titleKey: String = "选择币种", onDismiss: () -> Unit, onSelect: (CurrencyOption) -> Unit) {
    val i18n = LocalI18n.current
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) {
        val keyword = query.trim()
        if (keyword.isBlank()) supportedCurrencies else supportedCurrencies.filter {
            it.code.contains(keyword, ignoreCase = true) ||
                it.name.contains(keyword, ignoreCase = true) ||
                localizedCurrencyName(it.code, i18n).contains(keyword, ignoreCase = true) ||
                it.symbol.contains(keyword, ignoreCase = true)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(i18n(titleKey)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(i18n("搜索人民币、卢布或 RUB")) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, i18n("清空")) } },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(i18n("常用币种置顶 · %s 个结果", results.size), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(vertical = 10.dp))
                LazyColumn(Modifier.fillMaxWidth().height(390.dp)) {
                    items(results, key = { it.code }) { option ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSelect(option) }.padding(horizontal = 8.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(localizedCurrencySymbol(option.code, i18n), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center)
                            Column(Modifier.weight(1f)) {
                                Text(localizedCurrencyName(option.code, i18n), fontWeight = FontWeight.Medium)
                                Text(option.code, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            if (option.code == selectedCode) Icon(Icons.Rounded.Check, i18n("已选择"), tint = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                    if (results.isEmpty()) {
                        item { Text(i18n("没有找到这个币种"), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(i18n("关闭")) } }
    )
}

@Composable
private fun CalculatorPad(value: String, onChange: (String) -> Unit) {
    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "−"),
        listOf("AC", "0", ".", "+"),
        listOf("00", "⌫", "=")
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    val normalizedKey = if (key == "−") "-" else key
                    val isOperator = normalizedKey in listOf("+", "-", "×", "÷", "=")
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp).clickable {
                            when (normalizedKey) {
                                "AC" -> onChange("")
                                "⌫" -> onChange(value.dropLast(1))
                                "=" -> evaluateExpression(value)?.let { onChange(it.stripTrailingZeros().toPlainString()) }
                                "+", "-", "×", "÷" -> if (value.isNotBlank()) {
                                    val next = if (value.last() in "+-×÷") value.dropLast(1) + normalizedKey else value + normalizedKey
                                    onChange(next)
                                }
                                "." -> {
                                    val segment = value.split(Regex("[+×÷-]")).lastOrNull().orEmpty()
                                    if (!segment.contains('.')) onChange(value + if (segment.isBlank()) "0." else ".")
                                }
                                else -> {
                                    val segment = value.split(Regex("[+×÷-]")).lastOrNull().orEmpty()
                                    val decimals = segment.substringAfter('.', "")
                                    val addition = if (segment.contains('.')) normalizedKey.take((2 - decimals.length).coerceAtLeast(0)) else normalizedKey
                                    if (value.length < 36 && addition.isNotEmpty()) onChange(value + addition)
                                }
                            }
                        },
                        shape = RoundedCornerShape(13.dp),
                        color = if (isOperator) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") Icon(Icons.Rounded.Backspace, "退格") else Text(key, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateExpression(raw: String): BigDecimal? = runCatching {
    val expression = raw.replace(" ", "").replace("−", "-")
    if (expression.isBlank() || expression.last() in "+-×÷") return@runCatching null
    val numberParts = expression.split(Regex("[+×÷-]"))
    if (numberParts.any { it.isBlank() }) return@runCatching null
    val operators = expression.filter { it in "+-×÷" }
    var total = BigDecimal.ZERO
    var term = numberParts.first().toBigDecimal()
    operators.forEachIndexed { index, operator ->
        val next = numberParts[index + 1].toBigDecimal()
        when (operator) {
            '×' -> term = term.multiply(next)
            '÷' -> term = term.divide(next, MathContext.DECIMAL64)
            '+' -> { total = total.add(term); term = next }
            '-' -> { total = total.add(term); term = next.negate() }
        }
    }
    total.add(term).setScale(2, RoundingMode.HALF_UP)
}.getOrNull()
