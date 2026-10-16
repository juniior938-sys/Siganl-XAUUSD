package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mt5Account
import com.example.data.model.Mt5AccountType
import com.example.data.model.Mt5AccountingMode
import com.example.data.model.Mt5BrokerPresets
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Mt5AccountDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  activeAccount: Mt5Account?,
  accounts: List<Mt5Account>,
  onConnectAccount: (
    broker: String,
    server: String,
    login: String,
    password: String,
    type: Mt5AccountType,
    mode: Mt5AccountingMode,
    leverage: String
  ) -> Unit,
  onSelectAccount: (String) -> Unit,
  onDeleteAccount: (String) -> Unit
) {
  if (!isOpen) return

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  var selectedTab by remember { mutableIntStateOf(if (accounts.isEmpty()) 1 else 0) }

  // Form fields for new account
  var brokerInput by remember { mutableStateOf("Exness Technologies Ltd") }
  var serverInput by remember { mutableStateOf("Exness-MT5Real5") }
  var loginInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var accountType by remember { mutableStateOf(Mt5AccountType.REAL) }
  var accountingMode by remember { mutableStateOf(Mt5AccountingMode.HEDGING) }
  var leverageInput by remember { mutableStateOf("1:500") }
  var isConnecting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Mt5DarkBackground,
    tonalElevation = 8.dp,
    dragHandle = null
  ) {
    Column(
      modifier = Modifier
        .testTag("mt5_account_modal")
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Top Bar: Title & Close Button
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
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Mt5BidBlue.copy(alpha = 0.15f))
              .border(1.dp, Mt5BidBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
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
              text = "Contas de Operação MT5",
              color = Mt5TextPrimary,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Conecte Corretora, Servidor, ID e Senha",
              color = Mt5TextSecondary,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp
            )
          }
        }

        IconButton(
          onClick = {
            scope.launch {
              sheetState.hide()
              onDismiss()
            }
          },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Mt5DarkSurfaceElevated)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Fechar",
            tint = Mt5TextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tabs: "Contas Conectadas" & "Nova Conexão"
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Mt5DarkSurface,
        contentColor = Mt5BidBlue,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = Mt5BidBlue,
            height = 3.dp
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          modifier = Modifier.testTag("tab_my_accounts"),
          text = {
            Text(
              text = "Minhas Contas (${accounts.size})",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 12.sp,
              color = if (selectedTab == 0) Mt5BidBlue else Mt5TextSecondary
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          modifier = Modifier.testTag("tab_add_account"),
          text = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
              Text(
                text = "Conectar Nova Conta",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (selectedTab == 1) Mt5BidBlue else Mt5TextSecondary
              )
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Content based on tab
      if (selectedTab == 0) {
        // TAB 0: List of Connected Accounts
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(accounts, key = { it.id }) { acc ->
            val isActive = acc.id == activeAccount?.id
            Mt5AccountCard(
              account = acc,
              isActive = isActive,
              onSelect = { onSelectAccount(acc.id) },
              onDelete = { onDeleteAccount(acc.id) }
            )
          }

          item {
            Button(
              onClick = { selectedTab = 1 },
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Mt5DarkSurfaceElevated,
                contentColor = Mt5BidBlue
              )
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Adicionar Outra Conta de Corretora", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      } else {
        // TAB 1: Add New MT5 Connection Form
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Preset Corretoras Rápidas
          Text(
            text = "Selecione ou Busque a Corretora:",
            color = Mt5TextSecondary,
            style = MaterialTheme.typography.labelSmall
          )

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            for (preset in Mt5BrokerPresets.list) {
              val isSelected = brokerInput == preset.name
              FilterChip(
                selected = isSelected,
                onClick = {
                  brokerInput = preset.name
                  serverInput = preset.defaultServers.firstOrNull() ?: ""
                },
                label = { Text(preset.name.substringBefore(" "), fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Mt5BidBlue,
                  selectedLabelColor = Color.White,
                  containerColor = Mt5DarkSurfaceElevated,
                  labelColor = Mt5TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                border = FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = isSelected,
                  borderColor = if (isSelected) Mt5BidBlue else Mt5Border
                )
              )
            }
          }

          // 1. Corretora (Broker Name)
          OutlinedTextField(
            value = brokerInput,
            onValueChange = { brokerInput = it },
            label = { Text("Corretora (Broker)") },
            placeholder = { Text("Ex: Exness Technologies Ltd, IC Markets") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = Mt5BidBlue)
            },
            singleLine = true,
            modifier = Modifier
              .testTag("input_broker")
              .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Mt5BidBlue,
              unfocusedBorderColor = Mt5Border,
              focusedContainerColor = Mt5DarkSurface,
              unfocusedContainerColor = Mt5DarkSurface,
              focusedTextColor = Mt5TextPrimary,
              unfocusedTextColor = Mt5TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          // 2. Servidor (Server Name)
          OutlinedTextField(
            value = serverInput,
            onValueChange = { serverInput = it },
            label = { Text("Servidor de Negociação (Server)") },
            placeholder = { Text("Ex: Exness-MT5Real5 ou ICMarketsSC-Live") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = Mt5BidBlue)
            },
            singleLine = true,
            modifier = Modifier
              .testTag("input_server")
              .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Mt5BidBlue,
              unfocusedBorderColor = Mt5Border,
              focusedContainerColor = Mt5DarkSurface,
              unfocusedContainerColor = Mt5DarkSurface,
              focusedTextColor = Mt5TextPrimary,
              unfocusedTextColor = Mt5TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          // Sugestões de servidores da corretora selecionada
          val matchingPreset = Mt5BrokerPresets.list.find { it.name == brokerInput }
          if (matchingPreset != null) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              for (srv in matchingPreset.defaultServers) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (serverInput == srv) Mt5BidBlue.copy(alpha = 0.2f) else Mt5DarkSurfaceElevated)
                    .border(1.dp, if (serverInput == srv) Mt5BidBlue else Mt5Border, RoundedCornerShape(6.dp))
                    .clickable { serverInput = srv }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = srv,
                    color = if (serverInput == srv) Mt5BidBlue else Mt5TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }

          // 3. Login / ID da Conta MT5
          OutlinedTextField(
            value = loginInput,
            onValueChange = { loginInput = it },
            label = { Text("Login (Número / ID da Conta)") },
            placeholder = { Text("Ex: 10482910") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Mt5BidBlue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .testTag("input_login")
              .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Mt5BidBlue,
              unfocusedBorderColor = Mt5Border,
              focusedContainerColor = Mt5DarkSurface,
              unfocusedContainerColor = Mt5DarkSurface,
              focusedTextColor = Mt5TextPrimary,
              unfocusedTextColor = Mt5TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          // 4. Senha de Operação / Negociação (com toggle de visualização)
          OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Senha de Operação (Master/Investidor)") },
            placeholder = { Text("Digite sua senha MT5") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Mt5BidBlue)
            },
            trailingIcon = {
              IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier.testTag("toggle_password_visibility")
              ) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha",
                  tint = Mt5TextSecondary
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
              .testTag("input_password")
              .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Mt5BidBlue,
              unfocusedBorderColor = Mt5Border,
              focusedContainerColor = Mt5DarkSurface,
              unfocusedContainerColor = Mt5DarkSurface,
              focusedTextColor = Mt5TextPrimary,
              unfocusedTextColor = Mt5TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          // 5. Configurações da Conta: Tipo e Alavancagem
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Real vs Demo
            Column(modifier = Modifier.weight(1f)) {
              Text("Tipo de Conta:", color = Mt5TextSecondary, fontSize = 11.sp)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Mt5DarkSurface)
                  .padding(2.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (accountType == Mt5AccountType.REAL) Mt5BullGreen else Color.Transparent)
                    .clickable { accountType = Mt5AccountType.REAL }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    "Real",
                    color = if (accountType == Mt5AccountType.REAL) Color.White else Mt5TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (accountType == Mt5AccountType.DEMO) Mt5Amber else Color.Transparent)
                    .clickable { accountType = Mt5AccountType.DEMO }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    "Demo",
                    color = if (accountType == Mt5AccountType.DEMO) Color.Black else Mt5TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }
              }
            }

            // Alavancagem
            Column(modifier = Modifier.weight(1f)) {
              Text("Alavancagem:", color = Mt5TextSecondary, fontSize = 11.sp)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Mt5DarkSurface)
                  .padding(2.dp)
              ) {
                listOf("1:100", "1:500", "1:1000").forEach { lev ->
                  val isSel = leverageInput == lev
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (isSel) Mt5BidBlue else Color.Transparent)
                      .clickable { leverageInput = lev }
                      .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      lev,
                      color = if (isSel) Color.White else Mt5TextSecondary,
                      fontSize = 10.sp,
                      fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                  }
                }
              }
            }
          }

          // Security notice
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Mt5DarkSurfaceElevated)
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Mt5BullGreen, modifier = Modifier.size(16.dp))
            Text(
              text = "Credenciais salvas com segurança criptografada localmente no dispositivo.",
              color = Mt5TextSecondary,
              fontSize = 10.sp
            )
          }

          errorMessage?.let { err ->
            Text(text = err, color = Mt5BearRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Botão Conectar
          Button(
            onClick = {
              if (brokerInput.isBlank() || serverInput.isBlank() || loginInput.isBlank() || passwordInput.isBlank()) {
                errorMessage = "Por favor, preencha todos os campos obrigatórios."
                return@Button
              }
              errorMessage = null
              isConnecting = true

              scope.launch {
                delay(1200) // Simula handshake MT5 seguro com o servidor da corretora
                onConnectAccount(
                  brokerInput.trim(),
                  serverInput.trim(),
                  loginInput.trim(),
                  passwordInput.trim(),
                  accountType,
                  accountingMode,
                  leverageInput
                )
                isConnecting = false
                selectedTab = 0
              }
            },
            enabled = !isConnecting,
            modifier = Modifier
              .testTag("submit_connect_account")
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Mt5BidBlue,
              contentColor = Color.White
            )
          ) {
            if (isConnecting) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("CONECTANDO AO SERVIDOR MT5...", fontWeight = FontWeight.Bold)
            } else {
              Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("CONECTAR CONTA DE OPERAÇÃO", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun Mt5AccountCard(
  account: Mt5Account,
  isActive: Boolean,
  onSelect: () -> Unit,
  onDelete: () -> Unit
) {
  Box(
    modifier = Modifier
      .testTag("account_card_${account.id}")
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (isActive) Mt5DarkSurfaceElevated else Mt5DarkSurface)
      .border(1.dp, if (isActive) Mt5BidBlue else Mt5Border, RoundedCornerShape(12.dp))
      .clickable { onSelect() }
      .padding(12.dp)
  ) {
    Column {
      // Top line: Broker & Active Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = account.broker,
            color = Mt5TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = account.server,
              color = Mt5BidBlue,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "• ID: ${account.login}",
              color = Mt5TextSecondary,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (isActive) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Mt5BullGreen.copy(alpha = 0.2f))
                .border(1.dp, Mt5BullGreen, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "ATIVA",
                color = Mt5BullGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Remover conta",
              tint = Mt5TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Bar: Connection & Ping
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Mt5DarkBackground)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (account.isConnected) Mt5BullGreen else Mt5BearRed)
          )
          Text(
            text = if (account.isConnected) "Conectado" else "Desconectado",
            color = if (account.isConnected) Mt5BullGreen else Mt5BearRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "• ${account.accountType.label} (${account.leverage})",
            color = Mt5TextSecondary,
            fontSize = 10.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = Mt5TextSecondary, modifier = Modifier.size(12.dp))
          Text(
            text = "${account.pingMs} ms",
            color = if (account.pingMs < 50) Mt5BullGreen else Mt5Amber,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Financial Metrics Grid: Saldo, Capital Próprio, Margem Livre, Nível de Margem
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Saldo (Balance)", color = Mt5TextSecondary, fontSize = 10.sp)
          Text(account.formattedBalance, color = Mt5TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column {
          Text("Capital (Equity)", color = Mt5TextSecondary, fontSize = 10.sp)
          Text(account.formattedEquity, color = Mt5BullGreen, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column {
          Text("Margem Livre", color = Mt5TextSecondary, fontSize = 10.sp)
          Text(account.formattedFreeMargin, color = Mt5TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column {
          Text("Nível Margem", color = Mt5TextSecondary, fontSize = 10.sp)
          Text(account.formattedMarginLevel, color = Mt5BidBlue, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}
