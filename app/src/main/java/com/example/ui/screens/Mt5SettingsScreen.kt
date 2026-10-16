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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.ui.components.Mt5AccountDialog
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

  val activeAccount by viewModel.activeAccount.collectAsState()
  val accounts by viewModel.accounts.collectAsState()
  var isAccountDialogOpen by remember { mutableStateOf(false) }

  var apiKeyInput by remember {
    mutableStateOf(BuildConfig.GEMINI_API_KEY.ifEmpty { "AIzaSyC8IO8UiQqIoTff8diAntauFTQQIM6jQLM" })
  }
  var isApiKeyVisible by remember { mutableStateOf(false) }

  val quantConfig by viewModel.quantConfig.collectAsState()
  var emaFast by remember(quantConfig) { mutableStateOf(quantConfig.emaFastPeriod.toFloat()) }
  var emaSlow by remember(quantConfig) { mutableStateOf(quantConfig.emaSlowPeriod.toFloat()) }
  var rsiPeriod by remember(quantConfig) { mutableStateOf(quantConfig.rsiPeriod.toFloat()) }
  var atrPeriod by remember(quantConfig) { mutableStateOf(quantConfig.atrPeriod.toFloat()) }

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

    // 0. MT5 Connected Trading Account Card
    Box(
      modifier = Modifier
        .testTag("mt5_settings_account_card")
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Mt5DarkSurface)
        .border(1.dp, Mt5BidBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        .padding(14.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Mt5BidBlue.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                tint = Mt5BidBlue,
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Text(
                text = "Conta de Operação MT5",
                color = Mt5TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = activeAccount?.broker ?: "Nenhuma corretora conectada",
                color = Mt5TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (activeAccount?.isConnected == true) Mt5BullGreen.copy(alpha = 0.2f) else Mt5BearRed.copy(alpha = 0.2f))
              .border(1.dp, if (activeAccount?.isConnected == true) Mt5BullGreen else Mt5BearRed, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (activeAccount?.isConnected == true) Mt5BullGreen else Mt5BearRed)
              )
              Text(
                text = if (activeAccount?.isConnected == true) "CONECTADO" else "DESCONECTADO",
                color = if (activeAccount?.isConnected == true) Mt5BullGreen else Mt5BearRed,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }

        // Account Details Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Mt5DarkBackground)
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Servidor (Server)", color = Mt5TextSecondary, fontSize = 10.sp)
            Text(activeAccount?.server ?: "--", color = Mt5TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
          }
          Column {
            Text("Login ID", color = Mt5TextSecondary, fontSize = 10.sp)
            Text(activeAccount?.login ?: "--", color = Mt5BidBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
          }
          Column {
            Text("Saldo (Balance)", color = Mt5TextSecondary, fontSize = 10.sp)
            Text(activeAccount?.formattedBalance ?: "$0.00", color = Mt5BullGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
          }
          Column {
            Text("Latência", color = Mt5TextSecondary, fontSize = 10.sp)
            Text("⚡ ${activeAccount?.pingMs ?: 0} ms", color = Mt5TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        Button(
          onClick = { isAccountDialogOpen = true },
          modifier = Modifier
            .testTag("manage_accounts_button")
            .fillMaxWidth()
            .height(42.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Mt5BidBlue,
            contentColor = Color.White
          )
        ) {
          Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("GERENCIAR / CONECTAR CONTA DE CORRETORA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

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

    // 3. API Key Card (Oculta por padrão para segurança)
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Mt5BidBlue, modifier = Modifier.size(18.dp))
            Text(text = "Chave de Acesso Gemini API", color = Mt5TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          }

          Text(
            text = if (isApiKeyVisible) "VISÍVEL" else "OCULTA",
            color = if (isApiKeyVisible) Mt5BidBlue else Mt5TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Endpoint protegido para cálculo de Price Action e análise quantitativa multimodal.",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = apiKeyInput,
          onValueChange = { apiKeyInput = it },
          label = { Text("Chave da API (Oculta)") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("api_key_settings_input"),
          visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(
              onClick = { isApiKeyVisible = !isApiKeyVisible },
              modifier = Modifier.testTag("toggle_api_key_visibility")
            ) {
              Icon(
                imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isApiKeyVisible) "Ocultar Chave" else "Mostrar Chave",
                tint = Mt5BidBlue
              )
            }
          },
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
            Toast.makeText(context, "Chave atualizada e armazenada com segurança!", Toast.LENGTH_SHORT).show()
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

    Spacer(modifier = Modifier.height(14.dp))

    // 4. Quantitative Engine Parameters Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Mt5DarkSurface)
        .border(1.dp, Mt5Border, RoundedCornerShape(14.dp))
        .padding(12.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Mt5BidBlue, modifier = Modifier.size(18.dp))
          Text(
            text = "Motor Quantitativo Multitimeframe",
            color = Mt5TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Text(
          text = "Calibração dos indicadores técnicos: MACD, Ichimoku, Bollinger, RSI, ATR e Médias Móveis.",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.bodySmall
        )

        // EMA Fast
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "EMA Rápida (Sinal Curto):", color = Mt5TextPrimary, style = MaterialTheme.typography.bodySmall)
          Text(text = "${emaFast.toInt()}", color = Mt5BidBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
          value = emaFast,
          onValueChange = { emaFast = it },
          valueRange = 5f..25f,
          steps = 19,
          colors = SliderDefaults.colors(thumbColor = Mt5BidBlue, activeTrackColor = Mt5BidBlue, inactiveTrackColor = Mt5Border)
        )

        // EMA Slow
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "EMA Lenta (Tendência Micro):", color = Mt5TextPrimary, style = MaterialTheme.typography.bodySmall)
          Text(text = "${emaSlow.toInt()}", color = Mt5BidBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
          value = emaSlow,
          onValueChange = { emaSlow = it },
          valueRange = 15f..50f,
          steps = 34,
          colors = SliderDefaults.colors(thumbColor = Mt5BidBlue, activeTrackColor = Mt5BidBlue, inactiveTrackColor = Mt5Border)
        )

        // RSI Period
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Período RSI (Momentum):", color = Mt5TextPrimary, style = MaterialTheme.typography.bodySmall)
          Text(text = "${rsiPeriod.toInt()}", color = Mt5BidBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
          value = rsiPeriod,
          onValueChange = { rsiPeriod = it },
          valueRange = 7f..28f,
          steps = 20,
          colors = SliderDefaults.colors(thumbColor = Mt5BidBlue, activeTrackColor = Mt5BidBlue, inactiveTrackColor = Mt5Border)
        )

        // ATR Period
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Período ATR (Volatilidade):", color = Mt5TextPrimary, style = MaterialTheme.typography.bodySmall)
          Text(text = "${atrPeriod.toInt()}", color = Mt5BidBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Slider(
          value = atrPeriod,
          onValueChange = { atrPeriod = it },
          valueRange = 7f..30f,
          steps = 22,
          colors = SliderDefaults.colors(thumbColor = Mt5BidBlue, activeTrackColor = Mt5BidBlue, inactiveTrackColor = Mt5Border)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              emaFast = 9f
              emaSlow = 21f
              rsiPeriod = 14f
              atrPeriod = 14f
              viewModel.updateQuantConfig(com.example.data.engine.QuantitativeEngineConfig())
              Toast.makeText(context, "Parâmetros padrão MT5 restaurados!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .weight(1f)
              .height(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mt5DarkSurfaceElevated, contentColor = Mt5TextPrimary)
          ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Padrão", fontSize = 11.sp)
          }

          Button(
            onClick = {
              viewModel.updateQuantConfig(
                quantConfig.copy(
                  emaFastPeriod = emaFast.toInt(),
                  emaSlowPeriod = emaSlow.toInt(),
                  rsiPeriod = rsiPeriod.toInt(),
                  atrPeriod = atrPeriod.toInt()
                )
              )
              Toast.makeText(context, "Parâmetros quantitativos salvos!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .weight(1f)
              .height(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Mt5BidBlue, contentColor = Color.White)
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Aplicar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // Dialog de Conexão de Conta MT5
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
