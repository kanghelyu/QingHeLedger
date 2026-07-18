package com.qinghe.ledger.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.ColumnInfo
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amountCents: Long,
    val category: String,
    val categoryIcon: String,
    val categoryColor: Long,
    val note: String = "",
    val account: String = "日常账户",
    @ColumnInfo(defaultValue = "'CNY'") val currencyCode: String = "CNY",
    @ColumnInfo(defaultValue = "1.0") val exchangeRate: Double = 1.0,
    @ColumnInfo(defaultValue = "0") val originalAmountMinor: Long = amountCents,
    @ColumnInfo(defaultValue = "0") val excludedFromStats: Boolean = false,
    val occurredAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface LedgerDao {
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC, id DESC")
    fun observeAll(): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LedgerTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LedgerTransaction>)

    @Update
    suspend fun update(item: LedgerTransaction)

    @Query("SELECT COUNT(*) FROM transactions WHERE type = :type AND amountCents = :amountCents AND category = :category AND occurredAt = :occurredAt")
    suspend fun duplicateCount(type: String, amountCents: Long, category: String, occurredAt: Long): Int

    @Delete
    suspend fun delete(item: LedgerTransaction)

    @Query("DELETE FROM transactions")
    suspend fun clear()

    @Query("UPDATE transactions SET amountCents = CAST(ROUND(amountCents * :factor) AS INTEGER), exchangeRate = exchangeRate * :factor")
    suspend fun convertBaseCurrency(factor: Double)
}

@Database(entities = [LedgerTransaction::class], version = 2, exportSchema = false)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao

    companion object {
        @Volatile private var instance: LedgerDatabase? = null

        fun get(context: Context): LedgerDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LedgerDatabase::class.java,
                "qinghe-ledger.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'CNY'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN exchangeRate REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN originalAmountMinor INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN excludedFromStats INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE transactions SET originalAmountMinor = amountCents")
            }
        }
    }
}

data class CategoryOption(val name: String, val icon: String, val color: Long)

data class CurrencyOption(val code: String, val symbol: String, val name: String)

private val currencyPriority = listOf(
    "CNY", "USD", "EUR", "JPY", "GBP", "HKD", "KRW", "RUB",
    "AUD", "CAD", "CHF", "SGD", "NZD", "INR", "BRL", "MXN",
    "ZAR", "AED", "SAR", "TRY", "THB", "MYR", "IDR", "PHP",
    "VND", "TWD", "MOP", "PLN", "SEK", "NOK", "DKK", "CZK"
)

val supportedCurrencies: List<CurrencyOption> by lazy {
    val locale = java.util.Locale.SIMPLIFIED_CHINESE
    java.util.Currency.getAvailableCurrencies()
        .asSequence()
        .filter { it.defaultFractionDigits >= 0 }
        .map { currency ->
            CurrencyOption(
                code = currency.currencyCode,
                symbol = currency.getSymbol(locale).takeIf { it.isNotBlank() } ?: currency.currencyCode,
                name = currency.getDisplayName(locale).takeIf { it.isNotBlank() } ?: currency.currencyCode
            )
        }
        .sortedWith(compareBy<CurrencyOption> {
            currencyPriority.indexOf(it.code).let { index -> if (index < 0) Int.MAX_VALUE else index }
        }.thenBy { it.code })
        .toList()
}

val expenseCategories = listOf(
    CategoryOption("餐饮", "restaurant", 0xFFE76F51),
    CategoryOption("交通", "commute", 0xFF3D8BFF),
    CategoryOption("购物", "shopping", 0xFFF4A261),
    CategoryOption("居住", "home", 0xFF8F6ED5),
    CategoryOption("娱乐", "movie", 0xFFE05D9B),
    CategoryOption("医疗", "health", 0xFF27AE8A),
    CategoryOption("学习", "school", 0xFF5470C6),
    CategoryOption("通讯", "phone", 0xFF4E9F8E),
    CategoryOption("人情", "gift", 0xFFDD6B7B),
    CategoryOption("其他", "more", 0xFF8A938F)
)

val incomeCategories = listOf(
    CategoryOption("工资", "salary", 0xFF1E9B70),
    CategoryOption("奖金", "bonus", 0xFFF0A93B),
    CategoryOption("理财", "trend", 0xFF4B7BEC),
    CategoryOption("兼职", "work", 0xFF8F6ED5),
    CategoryOption("礼金", "gift", 0xFFE05D9B),
    CategoryOption("其他", "more", 0xFF8A938F)
)
