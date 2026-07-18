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
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL

data class ExchangeRateQuote(val rate: Double, val date: String)

class UnsupportedCurrencyException : Exception("该货币暂不支持此功能")

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
    val language = preferences.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "zh")
    val baseCurrency = preferences.baseCurrency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "CNY")
    val onboardingCompleted = preferences.onboardingCompleted.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    suspend fun fetchExchangeRate(sourceCurrency: String, targetCurrency: String = baseCurrency.value): Result<ExchangeRateQuote> = withContext(Dispatchers.IO) {
        runCatching {
            if (sourceCurrency == targetCurrency) {
                return@runCatching ExchangeRateQuote(1.0, java.time.LocalDate.now().toString())
            }
            val connection = (URL("https://api.frankfurter.dev/v2/rate/$sourceCurrency/$targetCurrency").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "QingHeLedger/1.3")
            }
            try {
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code in listOf(400, 404, 422)) throw UnsupportedCurrencyException()
                if (code !in 200..299) {
                    val detail = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
                    error(detail.ifBlank { "汇率服务返回错误 $code" })
                }
                val json = JSONObject(body)
                val rate = json.getDouble("rate")
                val date = json.getString("date")
                require(rate > 0) { "汇率数据无效" }
                ExchangeRateQuote(rate = rate, date = date)
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun changeBaseCurrency(newCurrency: String): Result<ExchangeRateQuote> = withContext(Dispatchers.IO) {
        runCatching {
            val oldCurrency = baseCurrency.value
            if (oldCurrency == newCurrency) return@runCatching ExchangeRateQuote(1.0, java.time.LocalDate.now().toString())
            val quote = fetchExchangeRate(oldCurrency, newCurrency).getOrThrow()
            dao.convertBaseCurrency(quote.rate)
            val convertedBudget = BigDecimal(monthlyBudget.value).multiply(BigDecimal.valueOf(quote.rate))
                .setScale(0, RoundingMode.HALF_UP).longValueExact()
            preferences.setMonthlyBudget(convertedBudget)
            preferences.setBaseCurrency(newCurrency)
            quote
        }
    }

    fun saveTransaction(
        existing: LedgerTransaction?,
        type: String,
        originalAmountMinor: Long,
        category: CategoryOption,
        note: String,
        account: String,
        occurredAt: Long,
        currencyCode: String,
        exchangeRate: Double,
        excludedFromStats: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        val baseAmountCents = BigDecimal(originalAmountMinor).multiply(BigDecimal.valueOf(exchangeRate))
            .setScale(0, RoundingMode.HALF_UP).longValueExact()
        val item = if (existing == null) {
            LedgerTransaction(
                type = type,
                amountCents = baseAmountCents,
                category = category.name,
                categoryIcon = category.icon,
                categoryColor = category.color,
                note = note.trim(),
                account = account,
                currencyCode = currencyCode,
                exchangeRate = exchangeRate,
                originalAmountMinor = originalAmountMinor,
                excludedFromStats = excludedFromStats,
                occurredAt = occurredAt
            )
        } else {
            existing.copy(
                type = type,
                amountCents = baseAmountCents,
                category = category.name,
                categoryIcon = category.icon,
                categoryColor = category.color,
                note = note.trim(),
                account = account,
                currencyCode = currencyCode,
                exchangeRate = exchangeRate,
                originalAmountMinor = originalAmountMinor,
                excludedFromStats = excludedFromStats,
                occurredAt = occurredAt
            )
        }
        if (existing == null) dao.insert(item) else dao.update(item)
    }

    fun deleteTransaction(item: LedgerTransaction) = viewModelScope.launch(Dispatchers.IO) { dao.delete(item) }
    fun clearAll() = viewModelScope.launch(Dispatchers.IO) { dao.clear() }
    fun setDarkMode(value: Boolean) = viewModelScope.launch { preferences.setDarkMode(value) }
    fun setHideAmounts(value: Boolean) = viewModelScope.launch { preferences.setHideAmounts(value) }
    fun setMonthlyBudget(cents: Long) = viewModelScope.launch { preferences.setMonthlyBudget(cents) }
    fun setLanguage(code: String) = viewModelScope.launch { preferences.setLanguage(code) }
    fun completeOnboarding() = viewModelScope.launch { preferences.setOnboardingCompleted(true) }

    fun exportCsv(): String = buildString {
        append('\uFEFF')
        appendLine("类型,基础货币金额,分类,备注,账户,发生时间,图标,色值,币种,原币金额,汇率,不计统计")
        transactions.value.forEach { item ->
            val typeName = if (item.type == "EXPENSE") "支出" else "收入"
            val amount = BigDecimal(item.amountCents).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY)
            val original = BigDecimal(item.originalAmountMinor.takeIf { it > 0 } ?: item.amountCents).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY)
            appendLine(listOf(typeName, amount, item.category, item.note, item.account, item.occurredAt, item.categoryIcon, item.categoryColor, item.currencyCode, original, item.exchangeRate, item.excludedFromStats).joinToString(",") { csvEscape(it.toString()) })
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
                    categoryColor = cells[7].toLong(),
                    currencyCode = cells.getOrNull(8).orEmpty().ifBlank { "CNY" },
                    originalAmountMinor = cells.getOrNull(9)?.toBigDecimalOrNull()?.movePointRight(2)?.setScale(0, RoundingMode.HALF_UP)?.longValueExact() ?: cents,
                    exchangeRate = cells.getOrNull(10)?.toDoubleOrNull() ?: 1.0,
                    excludedFromStats = cells.getOrNull(11)?.toBooleanStrictOrNull() ?: false
                )
            }.toList()
            var imported = 0
            rows.forEach { item ->
                if (dao.duplicateCount(item.type, item.amountCents, item.category, item.occurredAt) == 0) {
                    dao.insert(item)
                    imported++
                }
            }
            imported
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
