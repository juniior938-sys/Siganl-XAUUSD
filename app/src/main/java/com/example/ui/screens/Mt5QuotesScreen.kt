package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.viewmodel.MegaTradingViewModel

@Composable
fun Mt5QuotesScreen(
  viewModel: MegaTradingViewModel,
  onNavigateToChart: (AssetSymbol) -> Unit,
  modifier: Modifier = Modifier
) {
  val quotes by viewModel.quotes.collectAsState()
  val selectedAsset by viewModel.selectedAsset.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Mt5DarkBackground)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Cotações MT5",
          color = Mt5TextPrimary,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Preços Bid / Ask em tempo real",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.bodyMedium
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = { /* Add symbol */ },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Mt5DarkSurface)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Ativo", tint = Mt5TextPrimary, modifier = Modifier.size(18.dp))
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Table Header Labels
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Mt5DarkSurfaceElevated)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "ATIVO / SPREAD",
        color = Mt5TextSecondary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold
      )
      Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
        Text(
          text = "BID",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = "ASK",
          color = Mt5TextSecondary,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quotes List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(AssetSymbol.entries) { asset ->
        val quote = quotes[asset]
        QuoteRowItem(
          asset = asset,
          quote = quote,
          isSelected = asset == selectedAsset,
          onClick = {
            viewModel.selectAsset(asset)
            onNavigateToChart(asset)
          }
        )
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun QuoteRowItem(
  asset: AssetSymbol,
  quote: Mt5Quote?,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val isUp = (quote?.change24h ?: 0.0) >= 0

  Box(
    modifier = Modifier
      .testTag("quote_row_${asset.code}")
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) Mt5DarkSurfaceElevated else Mt5DarkSurface)
      .border(1.dp, if (isSelected) Mt5BidBlue else Mt5Border, RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Asset Info (Symbol, Category, Spread)
      Column(modifier = Modifier.weight(1.1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = asset.code,
            color = Mt5TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Mt5BidBlue,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Spread: ${quote?.formattedSpread ?: "--"}",
            color = Mt5TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
          )
          Text(
            text = "${if (isUp) "+" else ""}${String.format("%.2f", quote?.change24h ?: 0.0)}%",
            color = if (isUp) Mt5BullGreen else Mt5BearRed,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
          )
        }

        Text(
          text = "B: ${quote?.low24h?.let { "%.${asset.decimals}f".format(it) } ?: "--"}  A: ${quote?.high24h?.let { "%.${asset.decimals}f".format(it) } ?: "--"}",
          color = Mt5TextMuted,
          style = MaterialTheme.typography.bodySmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp
        )
      }

      // Bid and Ask Price Blocks (Classic MT5 formatting)
      Row(
        modifier = Modifier.weight(1.3f),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // BID Price
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Mt5DarkSurfaceElevated)
            .padding(horizontal = 6.dp, vertical = 6.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          Text(
            text = quote?.formattedBid ?: "--",
            color = Mt5BidBlue,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // ASK Price
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Mt5DarkSurfaceElevated)
            .padding(horizontal = 6.dp, vertical = 6.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          Text(
            text = quote?.formattedAsk ?: "--",
            color = Mt5BearRed,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}
