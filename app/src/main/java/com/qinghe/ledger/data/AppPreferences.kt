package com.qinghe.ledger.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("qinghe_settings")

class AppPreferences(private val context: Context) {
    private object Keys {
        val darkMode = booleanPreferencesKey("dark_mode")
        val hideAmounts = booleanPreferencesKey("hide_amounts")
        val monthlyBudget = longPreferencesKey("monthly_budget_cents")
        val language = stringPreferencesKey("language")
        val baseCurrency = stringPreferencesKey("base_currency")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.darkMode] ?: false }
    val hideAmounts: Flow<Boolean> = context.dataStore.data.map { it[Keys.hideAmounts] ?: false }
    val monthlyBudget: Flow<Long> = context.dataStore.data.map { it[Keys.monthlyBudget] ?: 500_000L }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.language] ?: "zh" }
    val baseCurrency: Flow<String> = context.dataStore.data.map { it[Keys.baseCurrency] ?: "CNY" }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.onboardingCompleted] ?: false }

    suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[Keys.darkMode] = value }
    suspend fun setHideAmounts(value: Boolean) = context.dataStore.edit { it[Keys.hideAmounts] = value }
    suspend fun setMonthlyBudget(value: Long) = context.dataStore.edit { it[Keys.monthlyBudget] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[Keys.language] = value }
    suspend fun setBaseCurrency(value: String) = context.dataStore.edit { it[Keys.baseCurrency] = value }
    suspend fun setOnboardingCompleted(value: Boolean) = context.dataStore.edit { it[Keys.onboardingCompleted] = value }
}
