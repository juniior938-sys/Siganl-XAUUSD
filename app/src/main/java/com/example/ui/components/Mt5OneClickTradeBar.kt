package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.Mt5Quote
import com.example.data.model.SignalDirection
import com.example.ui.theme.Mt5BearRed
import com.example.ui.theme.Mt5BidBlue
import com.example.ui.theme.Mt5Border
import com.example.ui.theme.Mt5DarkSurface
import com.example.ui.theme.Mt5DarkSurfaceElevated
import com.example.ui.theme.Mt5TextPrimary
import com.example.ui.theme.Mt5TextSecondary

@Composable
fun Mt5OneClickTradeBar(
  quote: Mt5Quote?,
  onExecuteTrade: (SignalDirection, Double) -> Unit,
  modifier: Modifier = Modifier
) {
  var lotSize by remember { mutableDoubleStateOf(0.10) }

  Row(
    modifier = modifier
      .testTag("mt5_one_click_trade_bar")
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Mt5DarkSurface)
      .border(1.dp, Mt5Border, RoundedCornerShape(12.dp))
      .padding(6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // SELL Button (Red)
    Box(
      modifier = Modifier
        .testTag("mt5_sell_button")
        .weight(1f)
        .height(46.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Mt5BearRed)
        .clickable { onExecuteTrade(SignalDirection.SELL, lotSize) }
        .padding(horizontal = 6.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "SELL",
          color = Color.White,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Text(
          text = quote?.formattedBid ?: "--",
          color = Color.White,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        )
      }
    }

    // Lot Size Selector (Center)
    Column(
      modifier = Modifier
        .weight(0.9f)
        .height(46.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Mt5DarkSurfaceElevated)
        .border(1.dp, Mt5Border, RoundedCornerShape(10.dp))
        .padding(horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "-",
          color = Mt5TextSecondary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .clickable {
              if (lotSize > 0.01) lotSize = ((lotSize - 0.01) * 100).toInt() / 100.0
            }
            .padding(horizontal = 6.dp)
        )
        Text(
          text = "%.2f".format(lotSize),
          color = Mt5TextPrimary,
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "+",
          color = Mt5TextSecondary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .clickable {
              lotSize = ((lotSize + 0.01) * 100).toInt() / 100.0
            }
            .padding(horizontal = 6.dp)
        )
      }
      Text(
        text = "Spread: ${quote?.formattedSpread ?: "--"}",
        color = Mt5TextSecondary,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    // BUY Button (Blue)
    Box(
      modifier = Modifier
        .testTag("mt5_buy_button")
        .weight(1f)
        .height(46.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Mt5BidBlue)
        .clickable { onExecuteTrade(SignalDirection.BUY, lotSize) }
        .padding(horizontal = 6.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "BUY",
          color = Color.White,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Text(
          text = quote?.formattedAsk ?: "--",
          color = Color.White,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        )
      }
    }
  }
}
