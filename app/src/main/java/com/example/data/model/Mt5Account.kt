package com.example.data.model

import java.util.UUID

enum class Mt5AccountType(val label: String) {
  REAL("Real"),
  DEMO("Demo")
}

enum class Mt5AccountingMode(val label: String) {
  HEDGING("Hedging"),
  NETTING("Netting")
}

data class Mt5Account(
  val id: String = UUID.randomUUID().toString(),
  val broker: String,
  val server: String,
  val login: String,
  val password: String,
  val accountType: Mt5AccountType = Mt5AccountType.REAL,
  val accountingMode: Mt5AccountingMode = Mt5AccountingMode.HEDGING,
  val leverage: String = "1:500",
  val currency: String = "USD",
  val balance: Double = 10000.0,
  val equity: Double = 10000.0,
  val margin: Double = 0.0,
  val freeMargin: Double = 10000.0,
  val marginLevel: Double = 0.0,
  val isConnected: Boolean = true,
  val pingMs: Int = 28,
  val isInvestorPassword: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
) {
  val formattedBalance: String get() = "%.2f %s".format(balance, currency)
  val formattedEquity: String get() = "%.2f %s".format(equity, currency)
  val formattedMargin: String get() = "%.2f %s".format(margin, currency)
  val formattedFreeMargin: String get() = "%.2f %s".format(freeMargin, currency)
  val formattedMarginLevel: String get() = if (margin > 0) "%.1f%%".format(marginLevel) else "--"
}

data class BrokerPreset(
  val name: String,
  val defaultServers: List<String>
)

object Mt5BrokerPresets {
  val list = listOf(
    BrokerPreset(
      name = "Exness Technologies Ltd",
      defaultServers = listOf("Exness-MT5Real", "Exness-MT5Real2", "Exness-MT5Real5", "Exness-MT5Trial")
    ),
    BrokerPreset(
      name = "IC Markets Global",
      defaultServers = listOf("ICMarketsSC-Live01", "ICMarketsSC-Live02", "ICMarketsSC-Demo")
    ),
    BrokerPreset(
      name = "XM Global Limited",
      defaultServers = listOf("XMGlobal-MT5", "XMGlobal-MT5 2", "XMGlobal-MT5 3", "XMGlobal-Demo")
    ),
    BrokerPreset(
      name = "FBS Markets Inc",
      defaultServers = listOf("FBS-Real", "FBS-Real-2", "FBS-Demo")
    ),
    BrokerPreset(
      name = "Pepperstone Group Limited",
      defaultServers = listOf("Pepperstone-MT5-Live01", "Pepperstone-MT5-Demo")
    ),
    BrokerPreset(
      name = "RoboForex Ltd",
      defaultServers = listOf("RoboForex-Pro", "RoboForex-ECN", "RoboForex-Demo")
    ),
    BrokerPreset(
      name = "MetaQuotes Software Corp.",
      defaultServers = listOf("MetaQuotes-Demo")
    )
  )
}
