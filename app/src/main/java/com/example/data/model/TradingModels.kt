package com.example.data.model

enum class AssetSymbol(val code: String, val displayName: String, val category: String, val decimals: Int, val pointValue: Double) {
  XAUUSD("XAUUSD", "XAUUSD", "Commodities", 2, 0.01),
  BTCUSD("BTCUSD", "BTCUSD", "Crypto", 2, 0.01),
  USDJPY("USDJPY", "USDJPY", "Forex", 3, 0.001),
  EURUSD("EURUSD", "EURUSD", "Forex", 5, 0.00001),
  GBPUSD("GBPUSD", "GBPUSD", "Forex", 5, 0.00001);

  companion object {
    fun fromCode(code: String): AssetSymbol {
      return entries.find { it.code.equals(code.replace("/", "").trim(), ignoreCase = true) } ?: XAUUSD
    }
  }
}

enum class Timeframe(val label: String, val durationMinutes: Int) {
  M1("M1", 1),
  M5("M5", 5),
  M15("M15", 15),
  M30("M30", 30),
  H1("H1", 60),
  H4("H4", 240),
  D1("D1", 1440)
}

enum class ChartStyle {
  CANDLESTICKS,
  BARS,
  LINE
}

data class Mt5Quote(
  val symbol: AssetSymbol,
  val bid: Double,
  val ask: Double,
  val spreadPoints: Int,
  val high24h: Double,
  val low24h: Double,
  val change24h: Double
) {
  val formattedBid: String get() = "%.${symbol.decimals}f".format(bid)
  val formattedAsk: String get() = "%.${symbol.decimals}f".format(ask)
  val formattedSpread: String get() = spreadPoints.toString()
}

enum class SignalDirection(val label: String, val isBuy: Boolean) {
  BUY("BUY", true),
  SELL("SELL", false),
  WAIT("WAIT", false)
}

enum class SignalStatus(val label: String) {
  ACTIVE("ATIVO"),
  TP1_HIT("TP1 ALCANÇADO"),
  TP2_HIT("TP2 ALCANÇADO"),
  TP3_HIT("TP3 ALCANÇADO"),
  SL_HIT("STOP LOSS"),
  EXPIRED("EXPIRADO")
}

data class Signal(
  val id: String,
  val symbol: AssetSymbol,
  val timeframe: Timeframe,
  val direction: SignalDirection,
  val confidence: Int,
  val entry: Double,
  val stopLoss: Double,
  val tp1: Double,
  val tp2: Double,
  val tp3: Double,
  val confluences: List<String>,
  val patternName: String,
  val timestamp: Long = System.currentTimeMillis(),
  val totalDurationSeconds: Int = 900,
  val remainingSeconds: Int = 900,
  val status: SignalStatus = SignalStatus.ACTIVE
) {
  val isHighPriority: Boolean get() = confidence >= 85
  val pipsToSl: Double get() = kotlin.math.abs(entry - stopLoss) / (symbol.pointValue * 10)
  val pipsToTp1: Double get() = kotlin.math.abs(tp1 - entry) / (symbol.pointValue * 10)
}

data class Candle(
  val timestamp: Long,
  val open: Float,
  val high: Float,
  val low: Float,
  val close: Float,
  val volume: Float
) {
  val isBullish: Boolean get() = close >= open
}

data class SMCAnnotation(
  val id: String,
  val type: AnnotationType,
  val label: String,
  val price: Float,
  val priceSecondary: Float? = null,
  val candleIndex: Int,
  val candleEndIndex: Int = candleIndex + 4
)

enum class AnnotationType {
  BOS, // Break of Structure
  OB,  // Order Block
  FVG, // Fair Value Gap
  DOUBLE_BOTTOM,
  HEAD_AND_SHOULDERS,
  ENTRY_LINE,
  SL_LINE,
  TP1_LINE,
  TP2_LINE,
  TP3_LINE
}

enum class PatternCategory(val title: String) {
  SMC("Smart Money Concepts"),
  CANDLESTICKS("Candlesticks"),
  CHART_PATTERNS("Padrões Gráficos"),
  MULTI_TIMEFRAME("Multi-Timeframe")
}

data class PatternItem(
  val id: String,
  val name: String,
  val category: PatternCategory,
  val isBullish: Boolean,
  val summary: String,
  val rules: List<String>,
  val bestTimeframes: String,
  val starRating: Int = 5,
  val diagramType: String
)

enum class SoundAlertTone(val displayName: String, val description: String) {
  MT5_ORDER_CLICK("MT5 Order Click", "Clique mecânico padrão de execução MT5"),
  MT5_BELL_ALERT("MT5 Bell Alert", "Sino de notificação clássico MetaTrader"),
  PRICE_PING("Price Ping", "Aviso cristalino de nível de preço atingido"),
  PROFIT_CHIME("Take Profit Chime", "Acorde suave de meta de lucro alcançada")
}

data class AiAnalysisResult(
  val rawResponse: String,
  val symbol: AssetSymbol,
  val timeframe: Timeframe,
  val direction: SignalDirection,
  val confidence: Int,
  val entry: Double,
  val stopLoss: Double,
  val tp1: Double,
  val tp2: Double,
  val tp3: Double,
  val confluences: List<String>,
  val timestamp: Long = System.currentTimeMillis(),
  val groundingQuery: String? = null,
  val marketRegime: String = "TENDÊNCIA DE ALTA",
  val buyScore: Int = 85,
  val sellScore: Int = 15,
  val h4Trend: String = "ALTA",
  val h1Trend: String = "ALTA",
  val m15Trend: String = "ALTA",
  val m5Trend: String = "ALTA",
  val confluenceDecision: com.example.data.engine.ConfluenceDecision? = null
) {
  val isWait: Boolean get() = direction == SignalDirection.WAIT
}
