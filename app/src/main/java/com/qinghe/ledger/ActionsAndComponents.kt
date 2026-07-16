package com.qinghe.ledger

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.qinghe.ledger.data.CategoryOption
import com.qinghe.ledger.data.LedgerTransaction
import com.qinghe.ledger.data.expenseCategories
import com.qinghe.ledger.data.incomeCategories
import com.qinghe.ledger.ui.theme.ExpenseRed
import com.qinghe.ledger.ui.theme.IncomeGreen
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun SectionTitle(title: String, trailing: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
    }
}

@Composable
internal fun TransactionRow(item: LedgerTransaction, hidden: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        CategoryIcon(item.categoryIcon, item.categoryColor, 46)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(item.category, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                item.note.ifBlank { item.account },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (item.type == "EXPENSE") "− " else "+ ") + moneyPublic(item.amountCents, hidden),
                fontWeight = FontWeight.SemiBold,
                color = if (item.type == "EXPENSE") MaterialTheme.colorScheme.onSurface else IncomeGreen
            )
            val time = java.time.Instant.ofEpochMilli(item.occurredAt).atZone(ZoneId.systemDefault())
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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

private val currency = java.text.NumberFormat.getCurrencyInstance(Locale.CHINA)
private fun moneyPublic(cents: Long, hidden: Boolean = false): String = if (hidden) "¥ ••••" else currency.format(cents / 100.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (String, Long, CategoryOption, String, String, Long) -> Unit
) {
    var type by rememberSaveable { mutableStateOf("EXPENSE") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var note by rememberSaveable { mutableStateOf("") }
    var account by rememberSaveable { mutableStateOf("日常账户") }
    var dateChoice by rememberSaveable { mutableStateOf("TODAY") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = if (type == "EXPENSE") expenseCategories else incomeCategories
    val validAmount = amount.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, dragHandle = null) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().imePadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("记一笔", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    IconButton(onDismiss) { Icon(Icons.Rounded.Close, "关闭") }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
                    TypeButton("支出", type == "EXPENSE", ExpenseRed, Modifier.weight(1f)) {
                        type = "EXPENSE"; category = expenseCategories.first()
                    }
                    TypeButton("收入", type == "INCOME", IncomeGreen, Modifier.weight(1f)) {
                        type = "INCOME"; category = incomeCategories.first()
                    }
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(if (type == "EXPENSE") "支出金额" else "收入金额", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text("¥ ${amount.ifBlank { "0.00" }}", style = MaterialTheme.typography.displaySmall, color = if (type == "EXPENSE") ExpenseRed else IncomeGreen, modifier = Modifier.padding(top = 4.dp))
                }
            }
            item {
                Text("选择分类", style = MaterialTheme.typography.titleMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxWidth().height(if (categories.size > 6) 158.dp else 82.dp),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { category = item }.padding(3.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (category == item) Color(item.color) else Color(item.color).copy(alpha = .13f),
                                modifier = Modifier.size(44.dp)
                            ) { Box(contentAlignment = Alignment.Center) { Icon(iconFor(item.icon), null, tint = if (category == item) Color.White else Color(item.color), modifier = Modifier.size(22.dp)) } }
                            Text(item.name, fontSize = 11.sp, color = if (category == item) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 40) note = it },
                    label = { Text("备注（可选）") },
                    placeholder = { Text("例如：和朋友聚餐") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TODAY" to "今天", "YESTERDAY" to "昨天").forEach { choice ->
                        AssistChip(onClick = { dateChoice = choice.first }, label = { Text(choice.second) }, leadingIcon = if (dateChoice == choice.first) ({ Icon(Icons.Rounded.Check, null, Modifier.size(17.dp)) }) else null)
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 8.dp)) {
                    items(listOf("日常账户", "微信", "支付宝", "银行卡", "现金")) { item ->
                        FilterChip(selected = account == item, onClick = { account = item }, label = { Text(item) })
                    }
                }
            }
            item { NumberPad(amount, onChange = { amount = it }) }
            item {
                Button(
                    onClick = {
                        val cents = BigDecimal(amount).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
                        val date = if (dateChoice == "TODAY") LocalDate.now() else LocalDate.now().minusDays(1)
                        val now = java.time.LocalTime.now()
                        val time = date.atTime(now).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        onSave(type, cents, category, note, account, time)
                    },
                    enabled = validAmount,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (type == "EXPENSE") ExpenseRed else IncomeGreen)
                ) { Text("保存这笔账", fontWeight = FontWeight.Bold) }
            }
        }
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
private fun NumberPad(value: String, onChange: (String) -> Unit) {
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf(".", "0", "⌫"))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp).clickable {
                            when (key) {
                                "⌫" -> onChange(value.dropLast(1))
                                "." -> if (!value.contains('.')) onChange(if (value.isBlank()) "0." else "$value.")
                                else -> {
                                    val decimals = value.substringAfter('.', "")
                                    if (!value.contains('.') || decimals.length < 2) {
                                        val next = if (value == "0") key else value + key
                                        if ((next.toBigDecimalOrNull() ?: BigDecimal.ZERO) <= BigDecimal("99999999.99")) onChange(next)
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(13.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)
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
