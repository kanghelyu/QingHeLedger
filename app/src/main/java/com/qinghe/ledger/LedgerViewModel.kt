package com.qinghe.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qinghe.ledger.data.AppPreferences
import com.qinghe.ledger.data.CategoryOption
import com.qinghe.ledger.data.LedgerDatabase
import com.qinghe.ledger.data.LedgerTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = LedgerDatabase.get(application).ledgerDao()
    private val preferences = AppPreferences(application)

    val transactions = dao.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val darkMode = preferences.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val hideAmounts = preferences.hideAmounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val monthlyBudget = preferences.monthlyBudget.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 500_000L)

    fun addTransaction(
        type: String,
        amountCents: Long,
        category: CategoryOption,
        note: String,
        account: String,
        occurredAt: Long
    ) = viewModelScope.launch(Dispatchers.IO) {
        dao.insert(
            LedgerTransaction(
                type = type,
                amountCents = amountCents,
                category = category.name,
                categoryIcon = category.icon,
                categoryColor = category.color,
                note = note.trim(),
                account = account,
                occurredAt = occurredAt
            )
        )
    }

    fun deleteTransaction(item: LedgerTransaction) = viewModelScope.launch(Dispatchers.IO) { dao.delete(item) }
    fun clearAll() = viewModelScope.launch(Dispatchers.IO) { dao.clear() }
    fun setDarkMode(value: Boolean) = viewModelScope.launch { preferences.setDarkMode(value) }
    fun setHideAmounts(value: Boolean) = viewModelScope.launch { preferences.setHideAmounts(value) }
    fun setMonthlyBudget(cents: Long) = viewModelScope.launch { preferences.setMonthlyBudget(cents) }

    fun exportCsv(): String = buildString {
        append('\uFEFF')
        appendLine("类型,金额,分类,备注,账户,发生时间,图标,色值")
        transactions.value.forEach { item ->
            val typeName = if (item.type == "EXPENSE") "支出" else "收入"
            val amount = BigDecimal(item.amountCents).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY)
            appendLine(listOf(typeName, amount, item.category, item.note, item.account, item.occurredAt, item.categoryIcon, item.categoryColor).joinToString(",") { csvEscape(it.toString()) })
        }
    }

    suspend fun importCsv(text: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = text.removePrefix("\uFEFF").lineSequence().drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
                val cells = parseCsvLine(line)
                if (cells.size < 8) return@mapNotNull null
                val cents = BigDecimal(cells[1]).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
                LedgerTransaction(
                    type = if (cells[0] == "支出") "EXPENSE" else "INCOME",
                    amountCents = cents,
                    category = cells[2],
                    note = cells[3],
                    account = cells[4],
                    occurredAt = cells[5].toLong(),
                    categoryIcon = cells[6],
                    categoryColor = cells[7].toLong()
                )
            }.toList()
            dao.insertAll(rows)
            rows.size
        }
    }

    private fun csvEscape(value: String): String = if (value.any { it == ',' || it == '"' || it == '\n' }) {
        "\"${value.replace("\"", "\"\"")}\""
    } else value

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> { cell.append('"'); i++ }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> { result += cell.toString(); cell.clear() }
                else -> cell.append(c)
            }
            i++
        }
        result += cell.toString()
        return result
    }
}
