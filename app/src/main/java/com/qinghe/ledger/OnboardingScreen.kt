package com.qinghe.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qinghe.ledger.data.supportedCurrencies
import com.qinghe.ledger.ui.theme.QingHe
import com.qinghe.ledger.ui.theme.QingHeDeep
import kotlinx.coroutines.launch

@Composable
internal fun OnboardingScreen(
    language: AppLanguage,
    baseCurrencyCode: String,
    onLanguage: (String) -> Unit,
    onBaseCurrency: suspend (String) -> Result<ExchangeRateQuote>,
    onComplete: () -> Unit
) {
    val i18n = LocalI18n.current
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableStateOf(0) }
    var languageChosen by rememberSaveable { mutableStateOf(false) }
    var chosenCurrency by rememberSaveable { mutableStateOf(baseCurrencyCode) }
    var currencyChosen by rememberSaveable { mutableStateOf(false) }
    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCurrency = supportedCurrencies.firstOrNull { it.code == chosenCurrency }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background))
        ).padding(horizontal = 22.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(70.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (step == 0) Icons.Rounded.Language else Icons.Rounded.AttachMoney,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(
                    if (step == 0) i18n("欢迎使用青禾记账") else i18n("选择你的惯用货币"),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    if (step == 0) i18n("首次使用需要先选择界面语言") else i18n("汇率、预算和统计都将使用此币种"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                if (step == 0) {
                    AppLanguage.entries.chunked(2).forEach { rowLanguages ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowLanguages.forEach { option ->
                                val selected = languageChosen && option == language
                                Card(
                                    onClick = { languageChosen = true; onLanguage(option.code) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
                                    ),
                                    modifier = Modifier.weight(1f).padding(bottom = 10.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.nativeName, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                        if (selected) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            if (rowLanguages.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Button(
                        onClick = { step = 1 },
                        enabled = languageChosen,
                        modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 8.dp),
                        shape = RoundedCornerShape(17.dp)
                    ) { Text(i18n("下一步"), fontWeight = FontWeight.Bold) }
                } else {
                    Card(
                        onClick = { showCurrencyPicker = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        localizedCurrencySymbol(chosenCurrency, i18n),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(if (currencyChosen) localizedCurrencyName(chosenCurrency, i18n) else i18n("请选择惯用货币"), fontWeight = FontWeight.SemiBold)
                                Text(if (currencyChosen) chosenCurrency else i18n("点击打开完整货币列表"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Icon(Icons.Rounded.AttachMoney, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                    Button(
                        onClick = {
                            loading = true
                            error = null
                            scope.launch {
                                onBaseCurrency(chosenCurrency).fold(
                                    onSuccess = { onComplete() },
                                    onFailure = {
                                        error = if (it is UnsupportedCurrencyException) i18n("无法继续：该货币暂不支持汇率换算") else i18n("设置失败，请检查网络后重试")
                                        loading = false
                                    }
                                )
                            }
                        },
                        enabled = currencyChosen && !loading,
                        modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 18.dp),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text(i18n("开始使用"), fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { step = 0; error = null }, enabled = !loading) { Text(i18n("返回")) }
                }

                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, null, tint = QingHe, modifier = Modifier.size(16.dp))
                    Text(i18n("账单只保存在你的设备上"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCode = chosenCurrency,
            titleKey = "选择惯用货币",
            onDismiss = { showCurrencyPicker = false },
            onSelect = {
                chosenCurrency = it.code
                currencyChosen = true
                showCurrencyPicker = false
                error = null
            }
        )
    }
}
