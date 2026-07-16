package com.qinghe.ledger.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
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

    @Delete
    suspend fun delete(item: LedgerTransaction)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}

@Database(entities = [LedgerTransaction::class], version = 1, exportSchema = false)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao

    companion object {
        @Volatile private var instance: LedgerDatabase? = null

        fun get(context: Context): LedgerDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LedgerDatabase::class.java,
                "qinghe-ledger.db"
            ).build().also { instance = it }
        }
    }
}

data class CategoryOption(val name: String, val icon: String, val color: Long)

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
