package com.qinghe.ledger

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
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
import androidx.compose.material.icons.rounded.ReceiptLong
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.qinghe.ledger.ui.theme.QingHe
import com.qinghe.ledger.ui.theme.QingHeDeep
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

private val zone: ZoneId = ZoneId.systemDefault()
private val moneyFormat = java.text.NumberFormat.getCurrencyInstance(Locale.CHINA)
private val dateFormat = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)

private fun money(cents: Long, hidden: Boolean = false): String =
    if (hidden) "¥ ••••" else moneyFormat.format(cents / 100.0)

private fun monthRange(month: YearMonth): LongRange {
    val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    return start..end
}

private fun LedgerTransaction.localDate(): LocalDate = Instant.ofEpochMilli(occurredAt).atZone(zone).toLocalDate()

@Composable
internal fun HomeScreen(
    transactions: List<LedgerTransaction>,
    budget: Long,
    hidden: Boolean,
    onAdd: () -> Unit
) {
    var offset by rememberSaveable { mutableIntStateOf(0) }
    val selectedMonth = YearMonth.now().plusMonths(offset.toLong())
    val monthly = remember(transactions, selectedMonth) { transactions.filter { it.occurredAt in monthRange(selectedMonth) } }
    val expense = monthly.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
    val income = monthly.filter { it.type == "INCOME" }.sumOf { it.amountCents }
    val recent = transactions.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("青禾记账", style = MaterialTheme.typography.headlineMedium)
                    Text("认真生活，也清楚每一笔", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Savings, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
        item {
            MonthSwitcher(selectedMonth, offset, onOffset = { offset = it })
        }
        item {
            BalanceCard(income, expense, hidden)
        }
        item {
            BudgetCard(expense, budget, hidden)
        }
        item { SectionTitle("最近账单", if (recent.isEmpty()) null else "共 ${transactions.size} 笔") }
        if (recent.isEmpty()) {
            item { EmptyLedger(onAdd) }
        } else {
            items(recent, key = { it.id }) { TransactionRow(it, hidden) }
        }
        if (monthly.isNotEmpty()) {
            item { SectionTitle("本月消费排行", null) }
            val ranking = monthly.filter { it.type == "EXPENSE" }.groupBy { it.category }
                .mapValues { it.value.sumOf(LedgerTransaction::amountCents) }
                .entries.sortedByDescending { it.value }.take(3)
            items(ranking) { item ->
                val sample = monthly.first { it.category == item.key }
                RankingRow(item.key, item.value, expense, sample.categoryIcon, sample.categoryColor, hidden)
            }
        }
    }
}

@Composable
private fun MonthSwitcher(month: YearMonth, offset: Int, onOffset: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onOffset(offset - 1) }) { Text("‹", fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text("${month.year}年 ${month.monthValue}月", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 20.dp))
        IconButton(onClick = { onOffset(offset + 1) }, enabled = offset < 0) { Text("›", fontSize = 30.sp, color = if (offset < 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline.copy(alpha = .35f)) }
    }
}

@Composable
private fun BalanceCard(income: Long, expense: Long, hidden: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.background(Brush.linearGradient(listOf(QingHe, QingHeDeep))).padding(24.dp)
        ) {
            Text("本月结余", color = Color.White.copy(.72f), style = MaterialTheme.typography.bodyMedium)
            Text(money(income - expense, hidden), color = Color.White, style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 6.dp, bottom = 22.dp))
            Row {
                MiniTotal("本月收入", income, IncomeGreen.copy(alpha = .24f), hidden, Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                MiniTotal("本月支出", expense, ExpenseRed.copy(alpha = .28f), hidden, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniTotal(label: String, value: Long, color: Color, hidden: Boolean, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.12f)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(7.dp))
            Text(label, color = Color.White.copy(.7f), fontSize = 12.sp)
        }
        Text(money(value, hidden), color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun BudgetCard(expense: Long, budget: Long, hidden: Boolean) {
    val progress = if (budget <= 0) 0f else min(expense.toFloat() / budget, 1f)
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Savings, null, tint = MaterialTheme.colorScheme.primary) }
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("月度预算", fontWeight = FontWeight.SemiBold)
                    Text(if (expense <= budget) "还能花 ${money(budget - expense, hidden)}" else "已超支 ${money(expense - budget, hidden)}", color = if (expense <= budget) MaterialTheme.colorScheme.onSurfaceVariant else ExpenseRed, fontSize = 13.sp)
                }
                Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(8.dp).clip(CircleShape),
                color = if (expense <= budget) MaterialTheme.colorScheme.primary else ExpenseRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyLedger(onAdd: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) }
            }
            Text("从第一笔开始", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
            Text("每天花十秒，月底看清钱去了哪里", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(14.dp)) { Text("记一笔") }
        }
    }
}

@Composable
private fun RankingRow(name: String, value: Long, total: Long, icon: String, color: Long, hidden: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        CategoryIcon(icon, color, 42)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Row { Text(name, Modifier.weight(1f), fontWeight = FontWeight.Medium); Text(money(value, hidden), fontWeight = FontWeight.SemiBold) }
            LinearProgressIndicator(
                progress = { if (total == 0L) 0f else value.toFloat() / total },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(5.dp).clip(CircleShape),
                color = Color(color), trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BillsScreen(transactions: List<LedgerTransaction>, hidden: Boolean, onDelete: (LedgerTransaction) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("ALL") }
    var deleting by remember { mutableStateOf<LedgerTransaction?>(null) }
    val filtered = remember(transactions, query, type) {
        transactions.filter { (type == "ALL" || it.type == type) && (query.isBlank() || it.category.contains(query, true) || it.note.contains(query, true) || it.account.contains(query, true)) }
    }
    val groups = filtered.groupBy { it.localDate() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp)
    ) {
        item { Text("账单明细", style = MaterialTheme.typography.headlineMedium) }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("搜索分类、备注或账户") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Rounded.Close, "清除") } },
                singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 14.dp)) {
                items(listOf("ALL" to "全部", "EXPENSE" to "支出", "INCOME" to "收入")) { item ->
                    FilterChip(selected = type == item.first, onClick = { type = item.first }, label = { Text(item.second) })
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Search, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(if (transactions.isEmpty()) "还没有账单" else "没有找到匹配账单", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
        groups.forEach { (date, dayItems) ->
            stickyHeader {
                val dayExpense = dayItems.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(date.format(dateFormat), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text("支出 ${money(dayExpense, hidden)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            items(dayItems, key = { it.id }) { item ->
                Box(Modifier.combinedClickable(onClick = {}, onLongClick = { deleting = item })) { TransactionRow(item, hidden) }
            }
        }
    }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Rounded.DeleteOutline, null) },
            title = { Text("删除这笔账单？") },
            text = { Text("${item.category} · ${money(item.amountCents)}，删除后无法恢复。") },
            confirmButton = { TextButton(onClick = { onDelete(item); deleting = null }) { Text("删除", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("取消") } }
        )
    }
}

@Composable
internal fun StatsScreen(transactions: List<LedgerTransaction>, hidden: Boolean) {
    var offset by rememberSaveable { mutableIntStateOf(0) }
    val month = YearMonth.now().plusMonths(offset.toLong())
    val monthly = remember(transactions, month) { transactions.filter { it.occurredAt in monthRange(month) } }
    val expenses = monthly.filter { it.type == "EXPENSE" }
    val total = expenses.sumOf { it.amountCents }
    val income = monthly.filter { it.type == "INCOME" }.sumOf { it.amountCents }
    val grouped = expenses.groupBy { it.category }.map { entry ->
        val sample = entry.value.first()
        CategoryStat(entry.key, entry.value.sumOf { it.amountCents }, Color(sample.categoryColor), sample.categoryIcon)
    }.sortedByDescending { it.amount }
    val lastSix = (5 downTo 0).map { ago ->
        val m = YearMonth.now().minusMonths(ago.toLong())
        m.monthValue to transactions.filter { it.type == "EXPENSE" && it.occurredAt in monthRange(m) }.sumOf { it.amountCents }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("统计分析", style = MaterialTheme.typography.headlineMedium) }
        item { MonthSwitcher(month, offset, onOffset = { offset = it }) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTotalCard("总支出", total, ExpenseRed, hidden, Modifier.weight(1f))
                StatTotalCard("总收入", income, IncomeGreen, hidden, Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp)) {
                    SectionTitle("近 6 个月支出", null)
                    MonthlyBarChart(lastSix, Modifier.fillMaxWidth().height(190.dp).padding(top = 16.dp))
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp)) {
                    SectionTitle("分类占比", if (grouped.isEmpty()) null else "${grouped.size} 类")
                    if (grouped.isEmpty()) {
                        Text("本月还没有支出数据", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(vertical = 52.dp), textAlign = TextAlign.Center)
                    } else {
                        DonutChart(grouped, total, hidden, Modifier.fillMaxWidth().height(230.dp).padding(vertical = 16.dp))
                        grouped.forEach { stat -> CategoryStatRow(stat, total, hidden) }
                    }
                }
            }
        }
    }
}

private data class CategoryStat(val name: String, val amount: Long, val color: Color, val icon: String)

@Composable
private fun StatTotalCard(label: String, value: Long, color: Color, hidden: Boolean, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text(money(value, hidden), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = color, modifier = Modifier.padding(top = 7.dp))
        }
    }
}

@Composable
private fun DonutChart(stats: List<CategoryStat>, total: Long, hidden: Boolean, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(180.dp)) {
            var start = -90f
            stats.forEach { stat ->
                val sweep = if (total == 0L) 0f else stat.amount.toFloat() / total * 360f
                drawArc(stat.color, start, sweep, false, style = Stroke(30.dp.toPx(), cap = StrokeCap.Butt))
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("本月支出", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(money(total, hidden), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun MonthlyBarChart(values: List<Pair<Int, Long>>, modifier: Modifier) {
    val maxValue = values.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
    val barColor = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier) {
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
            values.forEach { (_, amount) ->
                Box(Modifier.width(24.dp).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    Box(Modifier.width(14.dp).fillMaxHeight().clip(CircleShape).background(track))
                    Box(Modifier.width(14.dp).height((150 * amount.toFloat() / maxValue).dp.coerceAtLeast(5.dp)).clip(CircleShape).background(barColor))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceAround) {
            values.forEach { (month, _) -> Text("${month}月", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center) }
        }
    }
}

@Composable
private fun CategoryStatRow(stat: CategoryStat, total: Long, hidden: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(stat.color))
        Text(stat.name, Modifier.weight(1f).padding(start = 10.dp), fontWeight = FontWeight.Medium)
        Text("${if (total == 0L) 0 else stat.amount * 100 / total}%", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
        Text(money(stat.amount, hidden), fontWeight = FontWeight.SemiBold, modifier = Modifier.width(92.dp), textAlign = TextAlign.End)
    }
}
