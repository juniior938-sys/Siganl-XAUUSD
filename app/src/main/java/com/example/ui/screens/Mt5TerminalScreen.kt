package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetSymbol
import com.example.data.model.ChartStyle
import com.example.data.model.Signal
import com.example.data.model.Timeframe
import com.example.ui.components.Mt5AccountDialog
import com.example.ui.components.Mt5CandlestickChart
import com.example.ui.components.Mt5MarketWatchSidebar
import com.example.ui.components.Mt5OneClickTradeBar
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
fun Mt5TerminalScreen(
  viewModel: MegaTradingViewModel,
  onNavigateToAnalyzer: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val selectedAsset by viewModel.selectedAsset.collectAsState()
  val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
  val quotes by viewModel.quotes.collectAsState()
  val quote = quotes[selectedAsset]
  val candles by viewModel.currentCandles.collectAsState()
  val annotations by viewModel.currentAnnotations.collectAsState()
  val activeSignal by viewModel.currentSignal.collectAsState()
  val chartStyle by viewModel.chartStyle.collectAsState()
  val showGrid by viewModel.showGrid.collectAsState()
  val showVolumes by viewModel.showVolumes.collectAsState()
  val showBidAskLines by viewModel.showBidAskLines.collectAsState()
  val isAnalyzing by viewModel.isAnalyzing.collectAsState()
  val confluence by viewModel.currentConfluence.collectAsState()
  val activeAccount by viewModel.activeAccount.collectAsState()
  val accounts by viewModel.accounts.collectAsState()

  var isCrosshairActive by remember { mutableStateOf(false) }
  var isMarketWatchOpen by remember { mutableStateOf(false) }
  var isAccountDialogOpen by remember { mutableStateOf(false) }
  var assetMenuExpanded by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Mt5DarkBackground)
        .padding(horizontal = 10.dp, vertical = 6.dp)
        .verticalScroll(scrollState)
    ) {
      // 1. Top MT5 Asset and Control Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Mt5DarkSurface)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Market Watch Sidebar Button + Asset Dropdown
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { isMarketWatchOpen = true },
            modifier = Modifier
              .testTag("market_watch_toggle_button")
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
              contentDescription = "Abrir Market Watch",
              tint = Mt5BidBlue,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Asset Dropdown Selector
          Box {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { assetMenuExpanded = true }
                .padding(horizontal = 6.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = selectedAsset.code,
                color = Mt5TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(2.dp))
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Selecionar Ativo",
                tint = Mt5TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }

            DropdownMenu(
              expanded = assetMenuExpanded,
              onDismissRequest = { assetMenuExpanded = false },
              modifier = Modifier.background(Mt5DarkSurfaceElevated)
            ) {
              for (asset in AssetSymbol.entries) {
                DropdownMenuItem(
                  text = {
                    Text(
                      text = "${asset.code} (${asset.category})",
                      color = if (asset == selectedAsset) Mt5BidBlue else Mt5TextPrimary,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = if (asset == selectedAsset) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  onClick = {
                    viewModel.selectAsset(asset)
                    assetMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        // Right Actions: Crosshair Toggle, Chart Style, Grid Toggle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Dedicated MT5 Crosshair Tool Button
          IconButton(
            onClick = { isCrosshairActive = !isCrosshairActive },
            modifier = Modifier
              .testTag("crosshair_tool_button")
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isCrosshairActive) Mt5BidBlue else Mt5DarkSurfaceElevated)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Ferramenta de Mira / Crosshair",
              tint = if (isCrosshairActive) Color.White else Mt5TextPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          // Toggle Candlesticks / Bars / Line
          IconButton(
            onClick = {
              val nextStyle = when (chartStyle) {
                ChartStyle.CANDLESTICKS -> ChartStyle.BARS
                ChartStyle.BARS -> ChartStyle.LINE
                ChartStyle.LINE -> ChartStyle.CANDLESTICKS
              }
              viewModel.setChartStyle(nextStyle)
            },
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
          ) {
            Icon(
              imageVector = when (chartStyle) {
                ChartStyle.CANDLESTICKS -> Icons.Default.BarChart
                ChartStyle.BARS -> Icons.Default.BarChart
                ChartStyle.LINE -> Icons.Default.ShowChart
              },
              contentDescription = "Mudar Estilo de Gráfico",
              tint = Mt5TextPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          // Toggle Grid
          IconButton(
            onClick = { viewModel.toggleGrid() },
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
          ) {
            Icon(
              imageVector = Icons.Default.GridOn,
              contentDescription = "Grade",
              tint = if (showGrid) Mt5BidBlue else Mt5TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // MT5 Connected Account Status Bar (Corretora, Servidor, ID, Saldo e Conexão)
      Box(
        modifier = Modifier
          .testTag("mt5_account_status_bar")
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Mt5DarkSurface)
          .border(1.dp, Mt5Border, RoundedCornerShape(10.dp))
          .clickable { isAccountDialogOpen = true }
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (activeAccount?.isConnected == true) Mt5BullGreen else Mt5BearRed)
            )
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = activeAccount?.server ?: "Desconectado",
                  color = Mt5TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "• #${activeAccount?.login ?: "--"}",
                  color = Mt5BidBlue,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              Text(
                text = "${activeAccount?.broker ?: "Nenhuma conta"} • ${activeAccount?.accountType?.label ?: "Demo"}",
                color = Mt5TextSecondary,
                fontSize = 10.sp,
                maxLines = 1
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = activeAccount?.formattedBalance ?: "$0.00",
                color = Mt5BullGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
              )
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Eq: ${activeAccount?.formattedEquity ?: "$0.00"}",
                  color = Mt5TextSecondary,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "• ⚡ ${activeAccount?.pingMs ?: 0}ms",
                  color = Mt5TextSecondary,
                  fontSize = 9.sp
                )
              }
            }

            Icon(
              imageVector = Icons.Default.AccountBalance,
              contentDescription = "Gerenciar Contas MT5",
              tint = Mt5BidBlue,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. MT5 Timeframe Row (M1, M5, M15, M30, H1, H4, D1)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        for (tf in Timeframe.entries) {
          val isSelected = tf == selectedTimeframe
          Box(
            modifier = Modifier
              .testTag("timeframe_${tf.label}")
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) Mt5BidBlue else Mt5DarkSurface)
              .border(1.dp, if (isSelected) Mt5BidBlue else Mt5Border, RoundedCornerShape(8.dp))
              .clickable { viewModel.selectTimeframe(tf) }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = tf.label,
              color = if (isSelected) Color.White else Mt5TextSecondary,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Full MetaTrader 5 Canvas Chart (with interactive Crosshair Tool)
      Mt5CandlestickChart(
        candles = candles,
        quote = quote,
        symbol = selectedAsset,
        activeSignal = activeSignal,
        annotations = annotations,
        chartStyle = chartStyle,
        isCrosshairActive = isCrosshairActive,
        showVolumes = showVolumes,
        showGrid = showGrid,
        showBidAskLines = showBidAskLines
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Multi-timeframe Confluence Quick Status Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Mt5DarkSurface)
          .border(
            width = 1.dp,
            color = when (confluence.direction) {
              "BUY" -> Mt5BullGreen.copy(alpha = 0.5f)
              "SELL" -> Mt5BearRed.copy(alpha = 0.5f)
              else -> Mt5Amber.copy(alpha = 0.5f)
            },
            shape = RoundedCornerShape(10.dp)
          )
          .clickable { onNavigateToAnalyzer() }
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                  when (confluence.direction) {
                    "BUY" -> Mt5BullGreen
                    "SELL" -> Mt5BearRed
                    else -> Mt5Amber
                  }
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = confluence.direction,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Text(
              text = confluence.marketRegime.label,
              color = Mt5TextPrimary,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "H4:${confluence.h4Trend.label.take(1)} H1:${confluence.h1Trend.label.take(1)} M15:${confluence.m15Trend.label.take(1)}",
              color = Mt5TextSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Score: ${if (confluence.direction == "BUY") confluence.buyScore else if (confluence.direction == "SELL") confluence.sellScore else "${confluence.buyScore}/${confluence.sellScore}"}",
              color = if (confluence.direction == "BUY") Mt5BullGreen else if (confluence.direction == "SELL") Mt5BearRed else Mt5Amber,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. MetaTrader 5 One-Click Quick Trade Bar
      Mt5OneClickTradeBar(
        quote = quote,
        onExecuteTrade = { direction, lots ->
          viewModel.executeOneClickOrder(direction, lots)
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Active Signal & Entry Parameters (MT5 Format)
      activeSignal?.let { sig ->
        Mt5SignalInfoCard(
          signal = sig,
          quote = quote,
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = """
ORDEM MT5: ${sig.symbol.code} (${sig.timeframe.label})
Tipo: ${sig.direction.label}
Entrada: ${"%.${sig.symbol.decimals}f".format(sig.entry)}
Stop Loss: ${"%.${sig.symbol.decimals}f".format(sig.stopLoss)}
Take Profit 1: ${"%.${sig.symbol.decimals}f".format(sig.tp1)}
Take Profit 2: ${"%.${sig.symbol.decimals}f".format(sig.tp2)}
Take Profit 3: ${"%.${sig.symbol.decimals}f".format(sig.tp3)}
            """.trimIndent()
            clipboard.setPrimaryClip(ClipData.newPlainText("MT5 Order", text))
            Toast.makeText(context, "Parâmetros de ordem MT5 copiados!", Toast.LENGTH_SHORT).show()
          }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 6. Action Button: "ANALISAR COM GEMINI IA"
      Button(
        onClick = {
          viewModel.triggerGeminiAnalysis()
        },
        enabled = !isAnalyzing,
        modifier = Modifier
          .testTag("terminal_analyze_button")
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(12.dp),
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
            text = "ANALISANDO PRICE ACTION MT5...",
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

      Spacer(modifier = Modifier.height(20.dp))
    }

    // MT5 Market Watch Sliding Sidebar Panel
    Mt5MarketWatchSidebar(
      isOpen = isMarketWatchOpen,
      quotes = quotes,
      selectedAsset = selectedAsset,
      onSelectAsset = { asset ->
        viewModel.selectAsset(asset)
      },
      onClose = { isMarketWatchOpen = false }
    )

    // MT5 Connected Account Manager Dialog
    Mt5AccountDialog(
      isOpen = isAccountDialogOpen,
      onDismiss = { isAccountDialogOpen = false },
      activeAccount = activeAccount,
      accounts = accounts,
      onConnectAccount = { broker, server, login, password, type, mode, leverage ->
        viewModel.connectOrUpdateAccount(broker, server, login, password, type, mode, leverage)
      },
      onSelectAccount = { accId ->
        viewModel.switchActiveAccount(accId)
      },
      onDeleteAccount = { accId ->
        viewModel.deleteAccount(accId)
      }
    )
  }
}

@Composable
private fun Mt5SignalInfoCard(
  signal: Signal,
  quote: com.example.data.model.Mt5Quote?,
  onCopy: () -> Unit
) {
  val isBuy = signal.direction.isBuy
  val decimals = signal.symbol.decimals
  val min = signal.remainingSeconds / 60
  val sec = signal.remainingSeconds % 60
  val timeStr = "%02d:%02d".format(min, sec)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(Mt5DarkSurface)
      .border(1.dp, if (isBuy) Mt5BullGreen.copy(alpha = 0.5f) else Mt5BearRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
      .padding(12.dp)
  ) {
    Column {
      // Header: Type, Confidence, Countdown
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
            imageVector = if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = if (isBuy) Mt5BullGreen else Mt5BearRed,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "ORDEM RECOMENDADA: ${signal.direction.label}",
            color = if (isBuy) Mt5BullGreen else Mt5BearRed,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        // Live Countdown Badge
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Mt5DarkSurfaceElevated)
            .border(1.dp, Mt5Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = Mt5Amber,
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = timeStr,
            color = Mt5TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Execution Points Table (Exact MT5 Scale)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Mt5DarkSurfaceElevated)
          .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Mt5OrderRow("Preço de Entrada (Entry):", "%.${decimals}f".format(signal.entry), Mt5BidBlue)
        Mt5OrderRow("Stop Loss (SL):", "%.${decimals}f".format(signal.stopLoss), Mt5BearRed)
        Mt5OrderRow("Take Profit 1 (TP1):", "%.${decimals}f".format(signal.tp1), Mt5BullGreen)
        Mt5OrderRow("Take Profit 2 (TP2):", "%.${decimals}f".format(signal.tp2), Mt5BullGreen)
        Mt5OrderRow("Take Profit 3 (TP3):", "%.${decimals}f".format(signal.tp3), Mt5BullGreen)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Confluences
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Confluência: ${signal.confluences.firstOrNull() ?: "Price Action em suporte institucional"}",
            color = Mt5TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 11.sp
          )
          Text(
            text = "Confiança da IA: ${signal.confidence}%",
            color = Mt5BullGreen,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }

        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copiar Parâmetros",
            tint = Mt5BidBlue,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun Mt5OrderRow(label: String, value: String, color: Color) {
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
      style = MaterialTheme.typography.labelLarge,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp
    )
  }
}
