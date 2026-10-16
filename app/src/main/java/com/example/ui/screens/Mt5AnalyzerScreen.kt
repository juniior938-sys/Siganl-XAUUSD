package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.MarketRegime
import com.example.data.engine.TimeframeTrend
import com.example.ui.theme.Mt5Amber
import com.example.ui.theme.Mt5BearRed
import com.example.ui.theme.Mt5BidBlue
import com.example.ui.theme.Mt5Border
import com.example.ui.theme.Mt5BullGreen
import com.example.ui.theme.Mt5DarkBackground
import com.example.ui.theme.Mt5DarkSurface
import com.example.ui.theme.Mt5DarkSurfaceElevated
import com.example.ui.theme.Mt5TextPrimary
import com.example.ui.theme.Mt5TextSecondary
import com.example.ui.viewmodel.MegaTradingViewModel

@Composable
fun Mt5AnalyzerScreen(
  viewModel: MegaTradingViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val selectedAsset by viewModel.selectedAsset.collectAsState()
  val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
  val isAnalyzing by viewModel.isAnalyzing.collectAsState()
  val lastAnalysis by viewModel.lastAiAnalysis.collectAsState()
  val confluence by viewModel.currentConfluence.collectAsState()

  var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
  var customNotes by remember { mutableStateOf("") }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          imageBitmap = BitmapFactory.decodeStream(stream)
        }
      } catch (_: Exception) {}
    }
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Mt5DarkBackground)
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .verticalScroll(scrollState)
  ) {
    // Header
    Text(
      text = "Motor Quantitativo de Confluência",
      color = Mt5TextPrimary,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Análise multitimeframe (H4, H1, M15, M5), indicadores OHLC e IA Gemini MT5",
      color = Mt5TextSecondary,
      style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Live Quantitative Confluence Radar Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Mt5DarkSurface)
        .border(
          width = 1.dp,
          color = when (confluence.direction) {
            "BUY" -> Mt5BullGreen
            "SELL" -> Mt5BearRed
            else -> Mt5Amber
          },
          shape = RoundedCornerShape(14.dp)
        )
        .padding(12.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = Mt5BidBlue,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "${selectedAsset.code} - ${selectedTimeframe.label}",
              color = Mt5TextPrimary,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          // Regime Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(
                when (confluence.marketRegime) {
                  MarketRegime.UPTREND -> Mt5BullGreen.copy(alpha = 0.2f)
                  MarketRegime.DOWNTREND -> Mt5BearRed.copy(alpha = 0.2f)
                  MarketRegime.RANGING -> Mt5Amber.copy(alpha = 0.2f)
                  MarketRegime.REGIME_TRANSITION -> Mt5BidBlue.copy(alpha = 0.2f)
                  MarketRegime.EXTREME_VOLATILITY -> Color(0xFFFF1744).copy(alpha = 0.2f)
                }
              )
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = confluence.marketRegime.label,
              color = when (confluence.marketRegime) {
                MarketRegime.UPTREND -> Mt5BullGreen
                MarketRegime.DOWNTREND -> Mt5BearRed
                MarketRegime.RANGING -> Mt5Amber
                MarketRegime.REGIME_TRANSITION -> Mt5BidBlue
                MarketRegime.EXTREME_VOLATILITY -> Color(0xFFFF1744)
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Text(
          text = confluence.marketRegime.description,
          color = Mt5TextSecondary,
          fontSize = 11.sp,
          lineHeight = 15.sp
        )

        // Multi-Timeframe Trend Matrix
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          TimeframeTrendPill("H4", confluence.h4Trend, Modifier.weight(1f))
          TimeframeTrendPill("H1", confluence.h1Trend, Modifier.weight(1f))
          TimeframeTrendPill("M15", confluence.m15Trend, Modifier.weight(1f))
          TimeframeTrendPill("M5", confluence.m5Trend, Modifier.weight(1f))
        }

        // Confluence Scores (BUY vs SELL)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Mt5DarkSurfaceElevated)
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "PONTUAÇÃO BUY: ${confluence.buyScore}/100",
              color = Mt5BullGreen,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "PONTUAÇÃO SELL: ${confluence.sellScore}/100",
              color = Mt5BearRed,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          // Progress visualizer
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            LinearProgressIndicator(
              progress = { confluence.buyScore / 100f },
              modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = Mt5BullGreen,
              trackColor = Mt5DarkBackground
            )
            LinearProgressIndicator(
              progress = { confluence.sellScore / 100f },
              modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = Mt5BearRed,
              trackColor = Mt5DarkBackground
            )
          }

          // Decision Banner
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = when (confluence.direction) {
                  "BUY" -> Icons.Default.CheckCircle
                  "SELL" -> Icons.Default.CheckCircle
                  else -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = when (confluence.direction) {
                  "BUY" -> Mt5BullGreen
                  "SELL" -> Mt5BearRed
                  else -> Mt5Amber
                },
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "DECISÃO QUANTITATIVA: ${confluence.direction}",
                color = when (confluence.direction) {
                  "BUY" -> Mt5BullGreen
                  "SELL" -> Mt5BearRed
                  else -> Mt5Amber
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Text(
              text = if (confluence.direction == "WAIT") "Dif: ${kotlin.math.abs(confluence.buyScore - confluence.sellScore)} pts" else ">= 70 pts OK",
              color = Mt5TextSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Score Breakdown Grid
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Mt5DarkSurfaceElevated)
            .padding(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Composição da Pontuação de Confluência:",
            color = Mt5TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
          ScoreRow("Tendência Macro H4/H1", "${confluence.scoreBreakdown.macroTrendScore} / 25 pts")
          ScoreRow("Estrutura e Direção M15", "${confluence.scoreBreakdown.m15StructureScore} / 15 pts")
          ScoreRow("Momentum Agrupado (RSI/MACD/CCI)", "${confluence.scoreBreakdown.momentumScore} / 15 pts")
          ScoreRow("Candles, Pavios e Rejeição", "${confluence.scoreBreakdown.candleWickScore} / 20 pts")
          ScoreRow("Suporte, Resistência e Liquidez", "${confluence.scoreBreakdown.supportResistanceScore} / 15 pts")
          ScoreRow("Volatilidade e Execução (ATR)", "${confluence.scoreBreakdown.volatilityScore} / 10 pts")
        }

        // Live Technical Indicator Telemetry
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Mt5DarkSurfaceElevated)
            .padding(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Telemetria de Indicadores em Tempo Real:",
            color = Mt5TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
          val ind = confluence.indicators
          val dec = selectedAsset.decimals
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IndicatorTag("RSI 14", "%.1f".format(ind.rsi14), if (ind.rsi14 > 50) Mt5BullGreen else Mt5BearRed)
            IndicatorTag("MACD Hist", "%.4f".format(ind.macdHist), if (ind.macdHist >= 0) Mt5BullGreen else Mt5BearRed)
            IndicatorTag("Estocástico", "%.1f".format(ind.stochK), Mt5BidBlue)
            IndicatorTag("ATR 14", "%.${dec}f".format(ind.atr14), Mt5Amber)
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IndicatorTag("EMA 9", "%.${dec}f".format(ind.ema9), Mt5TextPrimary)
            IndicatorTag("EMA 21", "%.${dec}f".format(ind.ema21), Mt5TextPrimary)
            IndicatorTag("SMA 50", "%.${dec}f".format(ind.sma50), Mt5TextSecondary)
            IndicatorTag("SMA 200", "%.${dec}f".format(ind.sma200), Mt5TextSecondary)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Multimodal Vision & Chart Upload Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Mt5DarkSurface)
        .border(1.dp, Mt5Border, RoundedCornerShape(14.dp))
        .padding(12.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Análise Multimodal com Print MT5",
            color = Mt5TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
              .border(1.dp, Mt5Border, RoundedCornerShape(8.dp))
              .clickable {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = Mt5BidBlue,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = if (imageBitmap == null) "Anexar Imagem" else "Trocar Imagem",
                color = Mt5TextPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (imageBitmap != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, Mt5BidBlue, RoundedCornerShape(8.dp))
          ) {
            Image(
              bitmap = imageBitmap!!.asImageBitmap(),
              contentDescription = "Gráfico anexado",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(80.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
              .border(1.dp, Mt5Border, RoundedCornerShape(8.dp))
              .clickable {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = Mt5TextSecondary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Toque para enviar print do seu gráfico MT5 (Opcional)",
                color = Mt5TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = customNotes,
          onValueChange = { customNotes = it },
          label = { Text("Instruções ou confluências específicas") },
          placeholder = { Text("Ex: Confirmar rompimento de LTB e Order Block no M5...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("analyzer_notes_field"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Mt5BidBlue,
            unfocusedBorderColor = Mt5Border,
            focusedTextColor = Mt5TextPrimary,
            unfocusedTextColor = Mt5TextPrimary,
            focusedContainerColor = Mt5DarkSurfaceElevated,
            unfocusedContainerColor = Mt5DarkSurfaceElevated
          ),
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = {
            viewModel.triggerGeminiAnalysis(
              chartBitmap = imageBitmap,
              customNotes = customNotes.takeIf { it.isNotBlank() }
            )
          },
          enabled = !isAnalyzing,
          modifier = Modifier
            .testTag("run_mt5_analysis_button")
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Mt5BidBlue,
            contentColor = Color.White
          )
        ) {
          if (isAnalyzing) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "PROCESSANDO CONFLUÊNCIA...",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
          } else {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ANALISAR COM GEMINI IA (${selectedAsset.code} ${selectedTimeframe.label})",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Analysis Result Report Card
    lastAnalysis?.let { analysis ->
      val decimals = analysis.symbol.decimals
      val isBuy = analysis.direction.isBuy
      val isWait = analysis.isWait

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Mt5DarkSurface)
          .border(
            width = 1.dp,
            color = if (isWait) Mt5Amber else if (isBuy) Mt5BullGreen else Mt5BearRed,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(12.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = if (isWait) Icons.Default.Warning else if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isWait) Mt5Amber else if (isBuy) Mt5BullGreen else Mt5BearRed,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "${analysis.symbol.code} - ${analysis.timeframe.label}",
                color = Mt5TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Text(
              text = "SINAL: ${analysis.direction.label}",
              color = if (isWait) Mt5Amber else if (isBuy) Mt5BullGreen else Mt5BearRed,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Regime: ${analysis.marketRegime}",
              color = Mt5TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Confiança: ${analysis.confidence}%",
              color = if (isWait) Mt5Amber else Mt5BullGreen,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Parameters Table
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
              .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            AnalyzerRow("ENTRY (Entrada MT5):", "%.${decimals}f".format(analysis.entry), Mt5BidBlue)
            AnalyzerRow("SL (Stop Loss):", "%.${decimals}f".format(analysis.stopLoss), Mt5BearRed)
            AnalyzerRow("TP1 (Primeiro Alvo):", "%.${decimals}f".format(analysis.tp1), Mt5BullGreen)
            AnalyzerRow("TP2 (Alvo Mediano):", "%.${decimals}f".format(analysis.tp2), Mt5BullGreen)
            AnalyzerRow("TP3 (Alvo Longo):", "%.${decimals}f".format(analysis.tp3), Mt5BullGreen)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Confluências e Padrões Verificados:",
            color = Mt5TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
          for (c in analysis.confluences) {
            Text(
              text = "• $c",
              color = Mt5TextPrimary,
              style = MaterialTheme.typography.bodyMedium,
              fontSize = 11.sp,
              lineHeight = 15.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              tint = Mt5BidBlue,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "Pesquisa e cotações verificadas com Google Search Grounding",
              color = Mt5TextSecondary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun TimeframeTrendPill(label: String, trend: TimeframeTrend, modifier: Modifier = Modifier) {
  val color = when (trend) {
    TimeframeTrend.BULLISH -> Mt5BullGreen
    TimeframeTrend.BEARISH -> Mt5BearRed
    TimeframeTrend.NEUTRAL -> Mt5Amber
  }
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Mt5DarkSurfaceElevated)
      .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
      .padding(vertical = 4.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = label, color = Mt5TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
      Text(text = trend.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
  }
}

@Composable
private fun ScoreRow(title: String, score: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = title, color = Mt5TextSecondary, fontSize = 10.sp)
    Text(text = score, color = Mt5TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
  }
}

@Composable
private fun IndicatorTag(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, color = Mt5TextSecondary, fontSize = 9.sp)
    Text(text = value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
  }
}

@Composable
private fun AnalyzerRow(label: String, value: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = Mt5TextSecondary,
      style = MaterialTheme.typography.bodyMedium,
      fontSize = 11.sp
    )
    Text(
      text = value,
      color = color,
      style = MaterialTheme.typography.labelMedium,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
  }
}
