package com.example.data.repository

import com.example.data.model.AnnotationType
import com.example.data.model.AssetSymbol
import com.example.data.model.Candle
import com.example.data.model.Mt5Quote
import com.example.data.model.SMCAnnotation
import com.example.data.model.Signal
import com.example.data.model.SignalDirection
import com.example.data.model.SignalStatus
import com.example.data.model.Timeframe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TradingDataRepository {
  private val scope = CoroutineScope(Dispatchers.Default)
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

  private val _quotes = MutableStateFlow<Map<AssetSymbol, Mt5Quote>>(emptyMap())
  val quotes: StateFlow<Map<AssetSymbol, Mt5Quote>> = _quotes.asStateFlow()

  private val _candlesMap = MutableStateFlow<Map<String, List<Candle>>>(emptyMap())
  val candlesMap: StateFlow<Map<String, List<Candle>>> = _candlesMap.asStateFlow()

  private val _signals = MutableStateFlow<List<Signal>>(emptyList())
  val signals: StateFlow<List<Signal>> = _signals.asStateFlow()

  init {
    initializeQuotes()
    initializeSignals()
    initializeCandles()
    startRealTimeMarketFeed()
  }

  private fun initializeQuotes() {
    val initial = mapOf(
      AssetSymbol.XAUUSD to Mt5Quote(AssetSymbol.XAUUSD, bid = 2671.45, ask = 2671.75, spreadPoints = 30, high24h = 2685.20, low24h = 2654.80, change24h = 1.35),
      AssetSymbol.BTCUSD to Mt5Quote(AssetSymbol.BTCUSD, bid = 67840.50, ask = 67842.00, spreadPoints = 150, high24h = 68950.00, low24h = 66420.00, change24h = 2.45),
      AssetSymbol.USDJPY to Mt5Quote(AssetSymbol.USDJPY, bid = 153.245, ask = 153.260, spreadPoints = 15, high24h = 153.880, low24h = 152.920, change24h = -0.28),
      AssetSymbol.EURUSD to Mt5Quote(AssetSymbol.EURUSD, bid = 1.08452, ask = 1.08460, spreadPoints = 8, high24h = 1.08720, low24h = 1.08110, change24h = 0.38),
      AssetSymbol.GBPUSD to Mt5Quote(AssetSymbol.GBPUSD, bid = 1.29845, ask = 1.29858, spreadPoints = 13, high24h = 1.30240, low24h = 1.29410, change24h = 0.52)
    )
    _quotes.value = initial
  }

  private fun initializeSignals() {
    val initialSignals = listOf(
      Signal(
        id = "sig_xau",
        symbol = AssetSymbol.XAUUSD,
        timeframe = Timeframe.M5,
        direction = SignalDirection.BUY,
        confidence = 94,
        entry = 2671.45,
        stopLoss = 2658.00,
        tp1 = 2685.00,
        tp2 = 2698.00,
        tp3 = 2715.00,
        confluences = listOf(
          "Rompimento de LTB com candle de expansão",
          "Reteste preciso em Order Block institucional (OB)",
          "Padrão Fundo Duplo validado na escala M5",
          "Fair Value Gap (FVG) mitigado na retração"
        ),
        patternName = "Double Bottom + Bullish Engulfing",
        totalDurationSeconds = 900,
        remainingSeconds = 540,
        status = SignalStatus.ACTIVE
      ),
      Signal(
        id = "sig_btc",
        symbol = AssetSymbol.BTCUSD,
        timeframe = Timeframe.H1,
        direction = SignalDirection.BUY,
        confidence = 91,
        entry = 67840.50,
        stopLoss = 66500.00,
        tp1 = 69200.00,
        tp2 = 70800.00,
        tp3 = 72500.00,
        confluences = listOf(
          "Quebra de estrutura BOS confirmada em H1",
          "Order Block institucional defendido",
          "Reteste de rompimento com suporte volumétrico"
        ),
        patternName = "Break of Structure (BOS)",
        totalDurationSeconds = 3600,
        remainingSeconds = 1750,
        status = SignalStatus.ACTIVE
      ),
      Signal(
        id = "sig_usdjpy",
        symbol = AssetSymbol.USDJPY,
        timeframe = Timeframe.M5,
        direction = SignalDirection.SELL,
        confidence = 88,
        entry = 153.245,
        stopLoss = 153.620,
        tp1 = 152.850,
        tp2 = 152.450,
        tp3 = 152.000,
        confluences = listOf(
          "Rejeição com Shooting Star em resistência H1",
          "Change of Character (CHoCH) no tempo M5",
          "Captura de liquidez de topo (Buy-side Sweep)"
        ),
        patternName = "Head & Shoulders + CHoCH",
        totalDurationSeconds = 600,
        remainingSeconds = 320,
        status = SignalStatus.ACTIVE
      ),
      Signal(
        id = "sig_eurusd",
        symbol = AssetSymbol.EURUSD,
        timeframe = Timeframe.H1,
        direction = SignalDirection.BUY,
        confidence = 89,
        entry = 1.08452,
        stopLoss = 1.08150,
        tp1 = 1.08900,
        tp2 = 1.09350,
        tp3 = 1.09800,
        confluences = listOf(
          "Bullish Engulfing em zona de demanda MT5",
          "Mitigação institucional de Order Block",
          "Acúmulo de liquidez de venda capturado"
        ),
        patternName = "Bullish Engulfing em OB",
        totalDurationSeconds = 3600,
        remainingSeconds = 2400,
        status = SignalStatus.ACTIVE
      ),
      Signal(
        id = "sig_gbpusd",
        symbol = AssetSymbol.GBPUSD,
        timeframe = Timeframe.M5,
        direction = SignalDirection.BUY,
        confidence = 90,
        entry = 1.29845,
        stopLoss = 1.29480,
        tp1 = 1.30300,
        tp2 = 1.30750,
        tp3 = 1.31400,
        confluences = listOf(
          "Rompimento de bandeira Bull Flag",
          "Candle de ignição superando média móvel",
          "Volume institucional 2.2x acima da média"
        ),
        patternName = "Bull Flag Breakout",
        totalDurationSeconds = 900,
        remainingSeconds = 610,
        status = SignalStatus.ACTIVE
      )
    )
    _signals.value = initialSignals
  }

  private fun initializeCandles() {
    val map = mutableMapOf<String, List<Candle>>()
    for (asset in AssetSymbol.entries) {
      for (tf in Timeframe.entries) {
        val key = "${asset.code}_${tf.name}"
        map[key] = generateRealisticMt5Candles(asset, tf)
      }
    }
    _candlesMap.value = map
  }

  private fun generateRealisticMt5Candles(asset: AssetSymbol, tf: Timeframe): List<Candle> {
    val count = 45
    val basePrice = when (asset) {
      AssetSymbol.XAUUSD -> 2665.0f
      AssetSymbol.BTCUSD -> 67200.0f
      AssetSymbol.USDJPY -> 153.150f
      AssetSymbol.EURUSD -> 1.08350f
      AssetSymbol.GBPUSD -> 1.29650f
    }
    val volatility = basePrice * 0.0018f

    val candles = mutableListOf<Candle>()
    var current = basePrice
    val now = System.currentTimeMillis()
    val stepMs = tf.durationMinutes * 60 * 1000L

    for (i in 0 until count) {
      val isWFormation = i in 15..28
      val trendFactor = if (isWFormation) {
        when (i) {
          15, 16 -> -volatility * 0.85f
          17, 18 -> volatility * 0.70f
          19, 20 -> -volatility * 0.75f
          else -> volatility * 1.15f
        }
      } else {
        (Random.nextFloat() - 0.47f) * volatility
      }

      val open = current
      val close = open + trendFactor
      val high = maxOf(open, close) + Random.nextFloat() * (volatility * 0.5f)
      val low = minOf(open, close) - Random.nextFloat() * (volatility * 0.5f)
      val volume = 150f + Random.nextFloat() * 600f

      current = close
      candles.add(
        Candle(
          timestamp = now - (count - i) * stepMs,
          open = open,
          high = high,
          low = low,
          close = close,
          volume = volume
        )
      )
    }
    return candles
  }

  fun getCandles(symbol: AssetSymbol, timeframe: Timeframe): List<Candle> {
    val key = "${symbol.code}_${timeframe.name}"
    return _candlesMap.value[key] ?: emptyList()
  }

  fun getAnnotations(symbol: AssetSymbol, timeframe: Timeframe, candles: List<Candle>): List<SMCAnnotation> {
    if (candles.size < 28) return emptyList()
    val signal = _signals.value.find { it.symbol == symbol && it.timeframe == timeframe }
      ?: _signals.value.firstOrNull { it.symbol == symbol }

    val annotations = mutableListOf<SMCAnnotation>()
    val midLowIndex = 16
    val breakoutIndex = 25

    val lowPrice = candles[midLowIndex].low
    val obTop = lowPrice + (candles[breakoutIndex].close - lowPrice) * 0.28f

    // Order Block
    annotations.add(
      SMCAnnotation(
        id = "ob_zone",
        type = AnnotationType.OB,
        label = "OB",
        price = obTop,
        priceSecondary = lowPrice,
        candleIndex = 17,
        candleEndIndex = 27
      )
    )

    // Break of Structure
    val bosLevel = candles[17].high
    annotations.add(
      SMCAnnotation(
        id = "bos_line",
        type = AnnotationType.BOS,
        label = "BOS",
        price = bosLevel,
        candleIndex = 17,
        candleEndIndex = 26
      )
    )

    // Fair Value Gap
    val fvgBottom = candles[24].high
    val fvgTop = candles[26].low
    if (fvgTop > fvgBottom) {
      annotations.add(
        SMCAnnotation(
          id = "fvg_box",
          type = AnnotationType.FVG,
          label = "FVG",
          price = fvgTop,
          priceSecondary = fvgBottom,
          candleIndex = 24,
          candleEndIndex = 30
        )
      )
    }

    // MT5 Signal Lines
    if (signal != null) {
      annotations.add(
        SMCAnnotation(
          id = "entry_line",
          type = AnnotationType.ENTRY_LINE,
          label = "BUY ${signal.entry}",
          price = signal.entry.toFloat(),
          candleIndex = 26,
          candleEndIndex = candles.size - 1
        )
      )
      annotations.add(
        SMCAnnotation(
          id = "sl_line",
          type = AnnotationType.SL_LINE,
          label = "SL ${signal.stopLoss}",
          price = signal.stopLoss.toFloat(),
          candleIndex = 26,
          candleEndIndex = candles.size - 1
        )
      )
      annotations.add(
        SMCAnnotation(
          id = "tp1_line",
          type = AnnotationType.TP1_LINE,
          label = "TP ${signal.tp1}",
          price = signal.tp1.toFloat(),
          candleIndex = 26,
          candleEndIndex = candles.size - 1
        )
      )
    }

    return annotations
  }

  fun addOrUpdateSignal(signal: Signal) {
    val current = _signals.value.toMutableList()
    val index = current.indexOfFirst { it.symbol == signal.symbol && it.timeframe == signal.timeframe }
    if (index >= 0) {
      current[index] = signal
    } else {
      current.add(0, signal)
    }
    _signals.value = current
  }

  private fun startRealTimeMarketFeed() {
    scope.launch {
      // Background loop for real live market ticks and countdowns
      while (isActive) {
        delay(1000L)

        // 1. Live Countdown ticker for active signals
        _signals.value = _signals.value.map { sig ->
          if (sig.status == SignalStatus.ACTIVE) {
            val nextSec = sig.remainingSeconds - 1
            if (nextSec <= 0) {
              sig.copy(remainingSeconds = sig.totalDurationSeconds)
            } else {
              sig.copy(remainingSeconds = nextSec)
            }
          } else {
            sig
          }
        }

        // 2. Real-time tick quotes and spread update
        val updatedQuotes = _quotes.value.toMutableMap()
        for (symbol in AssetSymbol.entries) {
          val oldQuote = updatedQuotes[symbol] ?: continue
          val pipDelta = (Random.nextDouble() - 0.49) * symbol.pointValue * 2.5
          val newBid = (oldQuote.bid + pipDelta).coerceAtLeast(0.0001)
          val spreadOffset = oldQuote.spreadPoints * (symbol.pointValue / 10.0)
          val newAsk = newBid + spreadOffset

          updatedQuotes[symbol] = oldQuote.copy(
            bid = newBid,
            ask = newAsk,
            high24h = maxOf(oldQuote.high24h, newAsk),
            low24h = minOf(oldQuote.low24h, newBid)
          )
        }
        _quotes.value = updatedQuotes

        // 3. Update current live candle close for open charts
        val currentMap = _candlesMap.value.toMutableMap()
        for (symbol in AssetSymbol.entries) {
          val currentBid = updatedQuotes[symbol]?.bid?.toFloat() ?: continue
          for (tf in Timeframe.entries) {
            val key = "${symbol.code}_${tf.name}"
            val list = currentMap[key]?.toMutableList()
            if (!list.isNullOrEmpty()) {
              val lastIdx = list.size - 1
              val last = list[lastIdx]
              val updatedLast = last.copy(
                close = currentBid,
                high = maxOf(last.high, currentBid),
                low = minOf(last.low, currentBid)
              )
              list[lastIdx] = updatedLast
              currentMap[key] = list
            }
          }
        }
        _candlesMap.value = currentMap
      }
    }

    // Try fetching real Bitcoin price from Binance public API
    scope.launch {
      while (isActive) {
        try {
          fetchRealBinanceBtc()
        } catch (_: Exception) {}
        delay(10000L)
      }
    }
  }

  private fun fetchRealBinanceBtc() {
    try {
      val req = Request.Builder()
        .url("https://api.binance.com/api/v3/ticker/24hr?symbol=BTCUSDT")
        .build()
      val res = httpClient.newCall(req).execute()
      if (res.isSuccessful) {
        val body = res.body?.string() ?: return
        val json = JSONObject(body)
        val lastPrice = json.getDouble("lastPrice")
        val bidPrice = json.getDouble("bidPrice")
        val askPrice = json.getDouble("askPrice")
        val highPrice = json.getDouble("highPrice")
        val lowPrice = json.getDouble("lowPrice")
        val priceChangePercent = json.getDouble("priceChangePercent")

        val currentQuotes = _quotes.value.toMutableMap()
        val oldBtc = currentQuotes[AssetSymbol.BTCUSD]
        if (oldBtc != null) {
          currentQuotes[AssetSymbol.BTCUSD] = oldBtc.copy(
            bid = bidPrice,
            ask = askPrice,
            high24h = highPrice,
            low24h = lowPrice,
            change24h = priceChangePercent
          )
          _quotes.value = currentQuotes
        }
      }
    } catch (_: Exception) {}
  }
}
