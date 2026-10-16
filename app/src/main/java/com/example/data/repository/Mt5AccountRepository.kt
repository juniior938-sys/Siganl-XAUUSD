package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Mt5Account
import com.example.data.model.Mt5AccountType
import com.example.data.model.Mt5AccountingMode
import com.example.data.model.SignalDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class Mt5AccountRepository(private val context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("mt5_accounts_prefs", Context.MODE_PRIVATE)
  private val scope = CoroutineScope(Dispatchers.Default)

  private val _accounts = MutableStateFlow<List<Mt5Account>>(emptyList())
  val accounts: StateFlow<List<Mt5Account>> = _accounts.asStateFlow()

  private val _activeAccount = MutableStateFlow<Mt5Account?>(null)
  val activeAccount: StateFlow<Mt5Account?> = _activeAccount.asStateFlow()

  init {
    loadAccounts()
    startPingMonitor()
  }

  private fun loadAccounts() {
    val jsonString = prefs.getString("accounts_json", null)
    val loadedList = mutableListOf<Mt5Account>()

    if (!jsonString.isNullOrEmpty()) {
      try {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          loadedList.add(
            Mt5Account(
              id = obj.getString("id"),
              broker = obj.getString("broker"),
              server = obj.getString("server"),
              login = obj.getString("login"),
              password = obj.getString("password"),
              accountType = Mt5AccountType.valueOf(obj.optString("accountType", "REAL")),
              accountingMode = Mt5AccountingMode.valueOf(obj.optString("accountingMode", "HEDGING")),
              leverage = obj.optString("leverage", "1:500"),
              currency = obj.optString("currency", "USD"),
              balance = obj.optDouble("balance", 10000.0),
              equity = obj.optDouble("equity", 10000.0),
              margin = obj.optDouble("margin", 0.0),
              freeMargin = obj.optDouble("freeMargin", 10000.0),
              marginLevel = obj.optDouble("marginLevel", 0.0),
              isConnected = obj.optBoolean("isConnected", true),
              pingMs = obj.optInt("pingMs", 24),
              isInvestorPassword = obj.optBoolean("isInvestorPassword", false),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    if (loadedList.isEmpty()) {
      // Default initial MT5 demo account
      val defaultAcc = Mt5Account(
        broker = "Exness Technologies Ltd",
        server = "Exness-MT5Real5",
        login = "10482910",
        password = "••••••••",
        accountType = Mt5AccountType.REAL,
        accountingMode = Mt5AccountingMode.HEDGING,
        leverage = "1:500",
        currency = "USD",
        balance = 10000.00,
        equity = 10245.80,
        margin = 150.00,
        freeMargin = 10095.80,
        marginLevel = 6830.5,
        isConnected = true,
        pingMs = 28
      )
      loadedList.add(defaultAcc)
    }

    _accounts.value = loadedList

    val activeId = prefs.getString("active_account_id", null)
    _activeAccount.value = loadedList.find { it.id == activeId } ?: loadedList.firstOrNull()
  }

  private fun saveAccounts() {
    val array = JSONArray()
    for (acc in _accounts.value) {
      val obj = JSONObject().apply {
        put("id", acc.id)
        put("broker", acc.broker)
        put("server", acc.server)
        put("login", acc.login)
        put("password", acc.password)
        put("accountType", acc.accountType.name)
        put("accountingMode", acc.accountingMode.name)
        put("leverage", acc.leverage)
        put("currency", acc.currency)
        put("balance", acc.balance)
        put("equity", acc.equity)
        put("margin", acc.margin)
        put("freeMargin", acc.freeMargin)
        put("marginLevel", acc.marginLevel)
        put("isConnected", acc.isConnected)
        put("pingMs", acc.pingMs)
        put("isInvestorPassword", acc.isInvestorPassword)
        put("createdAt", acc.createdAt)
      }
      array.put(obj)
    }
    prefs.edit()
      .putString("accounts_json", array.toString())
      .putString("active_account_id", _activeAccount.value?.id)
      .apply()
  }

  fun addOrUpdateAccount(account: Mt5Account) {
    val current = _accounts.value.toMutableList()
    val index = current.indexOfFirst { it.id == account.id }
    if (index >= 0) {
      current[index] = account
    } else {
      current.add(0, account)
    }
    _accounts.value = current
    _activeAccount.value = account
    saveAccounts()
  }

  fun selectActiveAccount(accountId: String) {
    val found = _accounts.value.find { it.id == accountId }
    if (found != null) {
      _activeAccount.value = found
      prefs.edit().putString("active_account_id", accountId).apply()
    }
  }

  fun deleteAccount(accountId: String) {
    val current = _accounts.value.filter { it.id != accountId }
    _accounts.value = current
    if (_activeAccount.value?.id == accountId) {
      _activeAccount.value = current.firstOrNull()
    }
    saveAccounts()
  }

  fun updateAccountPing(pingMs: Int, isConnected: Boolean) {
    _activeAccount.value?.let { active ->
      val updated = active.copy(pingMs = pingMs, isConnected = isConnected)
      addOrUpdateAccount(updated)
    }
  }

  fun onTradeExecuted(direction: SignalDirection, lotSize: Double) {
    val active = _activeAccount.value ?: return
    val marginUsedPerLot = 1000.0 / (active.leverage.replace("1:", "").toDoubleOrNull() ?: 500.0)
    val requiredMargin = marginUsedPerLot * lotSize * 100.0
    val newMargin = active.margin + requiredMargin
    val newFreeMargin = (active.equity - newMargin).coerceAtLeast(0.0)
    val newMarginLevel = if (newMargin > 0) (active.equity / newMargin) * 100.0 else 0.0

    val updated = active.copy(
      margin = newMargin,
      freeMargin = newFreeMargin,
      marginLevel = newMarginLevel
    )
    addOrUpdateAccount(updated)
  }

  private fun startPingMonitor() {
    scope.launch {
      while (true) {
        delay(8000)
        _activeAccount.value?.let { active ->
          if (active.isConnected) {
            val jitter = Random.nextInt(-4, 5)
            val newPing = (active.pingMs + jitter).coerceIn(16, 120)
            _activeAccount.value = active.copy(pingMs = newPing)
          }
        }
      }
    }
  }
}
