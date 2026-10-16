package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.BuildConfig
import com.example.data.model.SoundAlertTone
import com.example.ui.theme.Mt5BidBlue
import com.example.ui.theme.Mt5Border
import com.example.ui.theme.Mt5DarkBackground
import com.example.ui.theme.Mt5DarkSurface
import com.example.ui.theme.Mt5DarkSurfaceElevated
import com.example.ui.theme.Mt5TextPrimary
import com.example.ui.theme.Mt5TextSecondary
import com.example.ui.viewmodel.MegaTradingViewModel

@Composable
fun Mt5SettingsScreen(
  viewModel: MegaTradingViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isMuted by viewModel.isSoundMuted.collectAsState()
  val currentTone by viewModel.selectedTone.collectAsState()
  val currentVolume by viewModel.masterVolume.collectAsState()
  val showGrid by viewModel.showGrid.collectAsState()
  val showVolumes by viewModel.showVolumes.collectAsState()
  val showBidAskLines by viewModel.showBidAskLines.collectAsState()

  var apiKeyInput by remember {
    mutableStateOf(BuildConfig.GEMINI_API_KEY.ifEmpty { "AIzaSyC8IO8UiQqIoTff8diAntauFTQQIM6jQLM" })
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
      text = "Configurações do Terminal",
      color = Mt5TextPrimary,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Preferências de áudio MT5, visualização de gráfico e conexão de API",
      color = Mt5TextSecondary,
      style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 1. Audio Alerts Card
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
              contentDescription = null,
              tint = if (isMuted) Color(0xFFFF5252) else Mt5BidBlue,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Sons de Notificação MT5",
              color = Mt5TextPrimary,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Switch(
            checked = !isMuted,
            onCheckedChange = { viewModel.toggleSoundMute() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = Mt5BidBlue,
              uncheckedThumbColor = Mt5TextSecondary,
              uncheckedTrackColor = Mt5DarkSurfaceElevated
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Selecione o Som de Execução:",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        for (tone in SoundAlertTone.entries) {
          val isSelected = tone == currentTone
          Box(
            modifier = Modifier
              .testTag("mt5_tone_${tone.name}")
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) Mt5DarkSurfaceElevated else Mt5DarkSurface)
              .border(1.dp, if (isSelected) Mt5BidBlue else Mt5Border, RoundedCornerShape(8.dp))
              .clickable { viewModel.setSelectedTone(tone) }
              .padding(horizontal = 10.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = tone.displayName,
                  color = if (isSelected) Mt5BidBlue else Mt5TextPrimary,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = tone.description,
                  color = Mt5TextSecondary,
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 10.sp
                )
              }

              Button(
                onClick = { viewModel.testPlaySound(tone) },
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) Mt5BidBlue else Mt5DarkSurfaceElevated,
                  contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Ouvir", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Volume:", color = Mt5TextSecondary, style = MaterialTheme.typography.labelSmall)
          Text(text = "${(currentVolume * 100).toInt()}%", color = Mt5BidBlue, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
        }

        Slider(
          value = currentVolume,
          onValueChange = { viewModel.setVolume(it) },
          colors = SliderDefaults.colors(
            thumbColor = Mt5BidBlue,
            activeTrackColor = Mt5BidBlue,
            inactiveTrackColor = Mt5Border
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Chart Display Toggles Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Mt5DarkSurface)
        .border(1.dp, Mt5Border, RoundedCornerShape(14.dp))
        .padding(12.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Visualização do Gráfico",
          color = Mt5TextPrimary,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Grade de Preços (Grid)", color = Mt5TextPrimary, style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = showGrid,
            onCheckedChange = { viewModel.toggleGrid() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Mt5BidBlue)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Histograma de Volumes", color = Mt5TextPrimary, style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = showVolumes,
            onCheckedChange = { viewModel.toggleVolumes() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Mt5BidBlue)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Linhas de Bid / Ask em Tempo Real", color = Mt5TextPrimary, style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = showBidAskLines,
            onCheckedChange = { viewModel.toggleBidAskLines() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Mt5BidBlue)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. API Key Card
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
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Mt5BidBlue, modifier = Modifier.size(18.dp))
          Text(text = "Chave de Acesso Gemini API", color = Mt5TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Configuração do endpoint Gemini Flash para análise de Price Action e Search Grounding.",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = apiKeyInput,
          onValueChange = { apiKeyInput = it },
          label = { Text("Chave da API") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("api_key_settings_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Mt5BidBlue,
            unfocusedBorderColor = Mt5Border,
            focusedTextColor = Mt5TextPrimary,
            unfocusedTextColor = Mt5TextPrimary,
            focusedContainerColor = Mt5DarkSurfaceElevated,
            unfocusedContainerColor = Mt5DarkSurfaceElevated
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = {
            viewModel.setCustomApiKey(apiKeyInput)
            Toast.makeText(context, "Chave atualizada com sucesso!", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Mt5BidBlue, contentColor = Color.White)
        ) {
          Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Salvar Chave", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
