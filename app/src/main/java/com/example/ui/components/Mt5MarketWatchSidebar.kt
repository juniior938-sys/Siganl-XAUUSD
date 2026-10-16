package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetSymbol
import com.example.data.model.Mt5Quote
import com.example.ui.theme.Mt5BearRed
import com.example.ui.theme.Mt5BidBlue
import com.example.ui.theme.Mt5Border
import com.example.ui.theme.Mt5BullGreen
import com.example.ui.theme.Mt5DarkBackground
import com.example.ui.theme.Mt5DarkSurface
import com.example.ui.theme.Mt5DarkSurfaceElevated
import com.example.ui.theme.Mt5TextMuted
import com.example.ui.theme.Mt5TextPrimary
import com.example.ui.theme.Mt5TextSecondary

@Composable
fun Mt5MarketWatchSidebar(
  isOpen: Boolean,
  quotes: Map<AssetSymbol, Mt5Quote>,
  selectedAsset: AssetSymbol,
  onSelectAsset: (AssetSymbol) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredSymbols = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      AssetSymbol.entries
    } else {
      AssetSymbol.entries.filter {
        it.code.contains(searchQuery.trim(), ignoreCase = true) ||
          it.category.contains(searchQuery.trim(), ignoreCase = true)
      }
    }
  }

  AnimatedVisibility(
    visible = isOpen,
    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
    modifier = modifier.fillMaxSize()
  ) {
    // Backdrop overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable { onClose() }
    ) {
      // Sidebar Panel
      Box(
        modifier = Modifier
          .testTag("mt5_market_watch_panel")
          .fillMaxHeight()
          .width(310.dp)
          .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
          .background(Mt5DarkSurface)
          .border(1.dp, Mt5Border, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
          .clickable(enabled = false) {} // Prevent click-through
          .padding(12.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Market Watch",
                color = Mt5TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Observação de Mercado MT5",
                color = Mt5TextSecondary,
                style = MaterialTheme.typography.bodySmall
              )
            }

            IconButton(
              onClick = onClose,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Fechar Market Watch",
                tint = Mt5TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar ativo...", fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Mt5TextSecondary, modifier = Modifier.size(16.dp))
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            shape = RoundedCornerShape(10.dp),
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

          Spacer(modifier = Modifier.height(12.dp))

          // Table Columns Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(Mt5DarkSurfaceElevated)
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Símbolo",
              color = Mt5TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
              Text(
                text = "Bid",
                color = Mt5TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Ask",
                color = Mt5TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Symbols List
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(filteredSymbols) { asset ->
              val q = quotes[asset]
              val isSelected = asset == selectedAsset

              Box(
                modifier = Modifier
                  .testTag("market_watch_item_${asset.code}")
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) Mt5DarkSurfaceElevated else Color.Transparent)
                  .border(1.dp, if (isSelected) Mt5BidBlue else Mt5Border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                  .clickable {
                    onSelectAsset(asset)
                    onClose()
                  }
                  .padding(horizontal = 8.dp, vertical = 8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Left: Symbol + Spread
                  Column {
                    Text(
                      text = asset.code,
                      color = if (isSelected) Mt5BidBlue else Mt5TextPrimary,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "! ${q?.formattedSpread ?: "--"} pts • ${asset.category}",
                      color = Mt5TextSecondary,
                      style = MaterialTheme.typography.bodySmall,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 9.sp
                    )
                  }

                  // Right: Bid and Ask Prices
                  Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                      text = q?.formattedBid ?: "--",
                      color = Mt5BidBlue,
                      style = MaterialTheme.typography.labelMedium,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = q?.formattedAsk ?: "--",
                      color = Mt5BearRed,
                      style = MaterialTheme.typography.labelMedium,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }

            item {
              Spacer(modifier = Modifier.height(24.dp))
            }
          }
        }
      }
    }
  }
}
