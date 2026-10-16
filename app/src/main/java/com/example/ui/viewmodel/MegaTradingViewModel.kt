package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SciFiSoundManager
import com.example.data.model.AiAnalysisResult
import com.example.data.model.AssetSymbol
import com.example.data.model.Candle
import com.example.data.model.ChartStyle
import com.example.data.model.Mt5Quote
import com.example.data.model.SMCAnnotation
import com.example.data.model.Signal
import com.example.data.model.SignalDirection
import com.example.data.model.SignalStatus
import com.example.data.model.SoundAlertTone
import com.example.data.model.Timeframe
import com.example.data.remote.GeminiApiService
import com.example.data.repository.TradingDataRepository
import com.example.data.engine.ConfluenceDecision
import com.example.data.engine.QuantitativeConfluenceEngine
import com.example.data.engine.QuantitativeEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MegaTradingViewModel(application: Application) : AndroidViewModel(application) {

  val repository = TradingDataRepository()
  private val geminiService = GeminiApiService()
  val soundManager = SciFiSoundManager(application)

  private val _selectedAsset = MutableStateFlow(AssetSymbol.XAUUSD)
  val selectedAsset: StateFlow<AssetSymbol> = _selectedAsset.asStateFlow()

  private val _selectedTimeframe = MutableStateFlow(Timeframe.M5)
  val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

  val quotes: StateFlow<Map<AssetSymbol, Mt5Quote>> = repository.quotes
  val signals: StateFlow<List<Signal>> = repository.signals

  // Quantitative Engine Configuration
  private val _quantConfig = MutableStateFlow(QuantitativeEngineConfig())
  val quantConfig: StateFlow<QuantitativeEngineConfig> = _quantConfig.asStateFlow()

  // Chart Preferences
  private val _chartStyle = MutableStateFlow(ChartStyle.CANDLESTICKS)
  val chartStyle: StateFlow<ChartStyle> = _chartStyle.asStateFlow()

  private val _showGrid = MutableStateFlow(true)
  val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

  private val _showVolumes = MutableStateFlow(true)
  val showVolumes: StateFlow<Boolean> = _showVolumes.asStateFlow()

  private val _showBidAskLines = MutableStateFlow(true)
  val showBidAskLines: StateFlow<Boolean> = _showBidAskLines.asStateFlow()

  // AI & Analysis States
  private val _isAnalyzing = MutableStateFlow(false)
  val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

  private val _lastAiAnalysis = MutableStateFlow<AiAnalysisResult?>(null)
  val lastAiAnalysis: StateFlow<AiAnalysisResult?> = _lastAiAnalysis.asStateFlow()

  // Audio Preferences
  private val _isSoundMuted = MutableStateFlow(false)
  val isSoundMuted: StateFlow<Boolean> = _isSoundMuted.asStateFlow()

  private val _selectedTone = MutableStateFlow(SoundAlertTone.MT5_ORDER_CLICK)
  val selectedTone: StateFlow<SoundAlertTone> = _selectedTone.asStateFlow()

  private val _masterVolume = MutableStateFlow(0.85f)
  val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

  // Combined current candles
  val currentCandles: StateFlow<List<Candle>> = combine(
    _selectedAsset,
    _selectedTimeframe,
    repository.candlesMap
  ) { asset, tf, map ->
    map["${asset.code}_${tf.name}"] ?: repository.getCandles(asset, tf)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  // Real-time Quantitative Confluence Decision
  val currentConfluence: StateFlow<ConfluenceDecision> = combine(
    _selectedAsset,
    _selectedTimeframe,
    currentCandles,
    quotes,
    _quantConfig
  ) { asset, tf, candles, quotesMap, cfg ->
    val curPrice = quotesMap[asset]?.bid ?: 2671.0
    QuantitativeConfluenceEngine.config = cfg
    QuantitativeConfluenceEngine.evaluate(asset, tf, candles, curPrice)
  }.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    QuantitativeConfluenceEngine.evaluate(AssetSymbol.XAUUSD, Timeframe.M5, emptyList(), 2671.0)
  )

  // Current active signal for selected asset and timeframe
  val currentSignal: StateFlow<Signal?> = combine(
    _selectedAsset,
    _selectedTimeframe,
    signals
  ) { asset, tf, list ->
    list.find { it.symbol == asset && it.timeframe == tf }
      ?: list.find { it.symbol == asset }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  // Current SMC Annotations
  val currentAnnotations: StateFlow<List<SMCAnnotation>> = combine(
    _selectedAsset,
    _selectedTimeframe,
    currentCandles
  ) { asset, tf, candles ->
    repository.getAnnotations(asset, tf, candles)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  fun selectAsset(asset: AssetSymbol) {
    _selectedAsset.value = asset
  }

  fun selectTimeframe(timeframe: Timeframe) {
    _selectedTimeframe.value = timeframe
  }

  fun setChartStyle(style: ChartStyle) {
    _chartStyle.value = style
  }

  fun toggleGrid() {
    _showGrid.value = !_showGrid.value
  }

  fun toggleVolumes() {
    _showVolumes.value = !_showVolumes.value
  }

  fun toggleBidAskLines() {
    _showBidAskLines.value = !_showBidAskLines.value
  }

  fun toggleSoundMute() {
    val newMuted = !_isSoundMuted.value
    _isSoundMuted.value = newMuted
    soundManager.setMuted(newMuted)
  }

  fun setVolume(vol: Float) {
    _masterVolume.value = vol
    soundManager.setMasterVolume(vol)
  }

  fun setSelectedTone(tone: SoundAlertTone) {
    _selectedTone.value = tone
    soundManager.setSelectedTone(tone)
  }

  fun testPlaySound(tone: SoundAlertTone) {
    soundManager.playSignalAlert(tone, triggerHaptic = true)
  }

  fun setCustomApiKey(key: String) {
    geminiService.customApiKey = key
  }

  fun executeOneClickOrder(direction: SignalDirection, lots: Double) {
    val asset = _selectedAsset.value
    val quote = quotes.value[asset] ?: return
    val execPrice = if (direction.isBuy) quote.ask else quote.bid

    // Play classic MT5 order click!
    soundManager.playSignalAlert(SoundAlertTone.MT5_ORDER_CLICK, triggerHaptic = true)

    val msg = "Ordem #${(100000..999999).random()}: ${direction.label} %.2f ${asset.code} a %.${asset.decimals}f executada com sucesso!"
      .format(lots, execPrice)
    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
  }

  fun updateQuantConfig(newConfig: QuantitativeEngineConfig) {
    _quantConfig.value = newConfig
  }

  fun triggerGeminiAnalysis(
    chartBitmap: Bitmap? = null,
    customNotes: String? = null
  ) {
    val asset = _selectedAsset.value
    val tf = _selectedTimeframe.value
    val quote = quotes.value[asset]
    val curPrice = quote?.bid ?: 2671.0
    val candles = currentCandles.value

    viewModelScope.launch {
      _isAnalyzing.value = true
      soundManager.playSignalAlert(SoundAlertTone.PRICE_PING, triggerHaptic = false)

      val result = geminiService.analyzeChart(
        symbol = asset,
        timeframe = tf,
        currentPrice = curPrice,
        candles = candles,
        imageBitmap = chartBitmap,
        userCustomNotes = customNotes
      )

      result.onSuccess { analysis ->
        _lastAiAnalysis.value = analysis

        if (!analysis.isWait) {
          val newSignal = Signal(
            id = "ai_${System.currentTimeMillis()}",
            symbol = asset,
            timeframe = tf,
            direction = analysis.direction,
            confidence = analysis.confidence,
            entry = analysis.entry,
            stopLoss = analysis.stopLoss,
            tp1 = analysis.tp1,
            tp2 = analysis.tp2,
            tp3 = analysis.tp3,
            confluences = analysis.confluences,
            patternName = "Confluência MT5 (${analysis.direction.label})",
            totalDurationSeconds = tf.durationMinutes * 60,
            remainingSeconds = tf.durationMinutes * 60,
            status = SignalStatus.ACTIVE
          )
          repository.addOrUpdateSignal(newSignal)
        }

        // Play alert sound according to decision
        if (analysis.direction == SignalDirection.WAIT) {
          soundManager.playSignalAlert(SoundAlertTone.PRICE_PING, triggerHaptic = false)
        } else if (analysis.confidence >= 75) {
          soundManager.playSignalAlert(SoundAlertTone.MT5_BELL_ALERT, triggerHaptic = true)
        }
      }

      _isAnalyzing.value = false
    }
  }
}
