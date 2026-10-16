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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
      text = "Análise IA de Mercado",
      color = Mt5TextPrimary,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Motor Gemini IA com Search Grounding e leitura de Price Action MT5",
      color = Mt5TextSecondary,
      style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Chart Upload Card
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
            text = "Captura de Gráfico MT5 / TradingView",
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
              .height(90.dp)
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
                modifier = Modifier.size(26.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Toque para enviar print do seu gráfico MT5",
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
          label = { Text("Instruções adicionais ou confluências desejadas") },
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
          maxLines = 3
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
              text = "PROCESSANDO PRICE ACTION...",
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

    // Analysis Result Card
    lastAnalysis?.let { analysis ->
      val decimals = analysis.symbol.decimals
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Mt5DarkSurface)
          .border(1.dp, if (analysis.direction.isBuy) Mt5BullGreen else Mt5BearRed, RoundedCornerShape(14.dp))
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
                imageVector = if (analysis.direction.isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (analysis.direction.isBuy) Mt5BullGreen else Mt5BearRed,
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
              color = if (analysis.direction.isBuy) Mt5BullGreen else Mt5BearRed,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Nível de Confiança: ${analysis.confidence}%",
            color = Mt5BullGreen,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

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
            AnalyzerRow("ENTRY (Entrada):", "%.${decimals}f".format(analysis.entry), Mt5BidBlue)
            AnalyzerRow("SL (Stop Loss):", "%.${decimals}f".format(analysis.stopLoss), Mt5BearRed)
            AnalyzerRow("TP1 (Primeiro Alvo):", "%.${decimals}f".format(analysis.tp1), Mt5BullGreen)
            AnalyzerRow("TP2 (Alvo Mediano):", "%.${decimals}f".format(analysis.tp2), Mt5BullGreen)
            AnalyzerRow("TP3 (Alvo Longo):", "%.${decimals}f".format(analysis.tp3), Mt5BullGreen)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Confluências Técnicas Identificadas:",
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
              text = "Pesquisa e dados em tempo real verificados com Google Search Grounding",
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
