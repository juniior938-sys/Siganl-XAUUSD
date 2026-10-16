package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnnotationType
import com.example.data.model.AssetSymbol
import com.example.data.model.Candle
import com.example.data.model.ChartStyle
import com.example.data.model.Mt5Quote
import com.example.data.model.SMCAnnotation
import com.example.data.model.Signal
import com.example.ui.theme.Mt5Amber
import com.example.ui.theme.Mt5AskRed
import com.example.ui.theme.Mt5BearRed
import com.example.ui.theme.Mt5BidBlue
import com.example.ui.theme.Mt5Border
import com.example.ui.theme.Mt5BullGreen
import com.example.ui.theme.Mt5Crosshair
import com.example.ui.theme.Mt5DarkBackground
import com.example.ui.theme.Mt5DarkSurfaceElevated
import com.example.ui.theme.Mt5GridLine
import com.example.ui.theme.Mt5Purple
import com.example.ui.theme.Mt5TextPrimary
import com.example.ui.theme.Mt5TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Mt5CandlestickChart(
  candles: List<Candle>,
  quote: Mt5Quote?,
  symbol: AssetSymbol,
  activeSignal: Signal?,
  annotations: List<SMCAnnotation>,
  chartStyle: ChartStyle = ChartStyle.CANDLESTICKS,
  isCrosshairActive: Boolean = false,
  showVolumes: Boolean = true,
  showGrid: Boolean = true,
  showBidAskLines: Boolean = true,
  modifier: Modifier = Modifier
) {
  var crosshairIndex by remember { mutableStateOf<Int?>(null) }
  var crosshairY by remember { mutableStateOf<Float?>(null) }
  val textMeasurer = rememberTextMeasurer()
  val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
  val fullDateTimeFormat = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }

  Box(
    modifier = modifier
      .testTag("mt5_candlestick_chart")
      .fillMaxWidth()
      .height(390.dp)
      .background(Mt5DarkBackground)
  ) {
    if (candles.isNotEmpty()) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(candles, isCrosshairActive) {
            detectTapGestures(
              onTap = { offset ->
                val candleWidth = (size.width - 64.dp.toPx()) / candles.size
                val idx = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                crosshairIndex = idx
                crosshairY = offset.y
              }
            )
          }
          .pointerInput(candles, isCrosshairActive) {
            detectDragGestures(
              onDragStart = { offset ->
                val candleWidth = (size.width - 64.dp.toPx()) / candles.size
                crosshairIndex = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                crosshairY = offset.y
              },
              onDrag = { change, _ ->
                val candleWidth = (size.width - 64.dp.toPx()) / candles.size
                crosshairIndex = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                crosshairY = change.position.y
              },
              onDragEnd = {}
            )
          }
      ) {
        val width = size.width
        val height = size.height

        val paddingRight = 64.dp.toPx()
        val paddingBottom = 22.dp.toPx()
        val paddingTop = 26.dp.toPx()
        val volumeHeight = if (showVolumes) 45.dp.toPx() else 0f

        val chartWidth = width - paddingRight
        val chartHeight = height - paddingTop - paddingBottom - volumeHeight

        // Min and Max price calculations
        var minPrice = candles.minOf { it.low }
        var maxPrice = candles.maxOf { it.high }

        quote?.let { q ->
          minPrice = minOf(minPrice, q.bid.toFloat(), q.ask.toFloat())
          maxPrice = maxOf(maxPrice, q.bid.toFloat(), q.ask.toFloat())
        }

        activeSignal?.let { sig ->
          minPrice = minOf(minPrice, sig.stopLoss.toFloat(), sig.entry.toFloat())
          maxPrice = maxOf(maxPrice, sig.tp3.toFloat(), sig.tp1.toFloat())
        }

        val range = (maxPrice - minPrice).coerceAtLeast(0.0001f)
        val expandedMin = minPrice - range * 0.04f
        val expandedMax = maxPrice + range * 0.05f
        val effectiveRange = expandedMax - expandedMin

        fun priceToY(price: Float): Float {
          val norm = (price - expandedMin) / effectiveRange
          return paddingTop + chartHeight * (1f - norm)
        }

        fun yToPrice(y: Float): Float {
          val norm = 1f - ((y - paddingTop) / chartHeight)
          return expandedMin + norm * effectiveRange
        }

        val gridSteps = 6
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)

        // 1. Grid Lines and Right Price Scale (MT5 standard)
        for (g in 0..gridSteps) {
          val y = paddingTop + (chartHeight / gridSteps) * g
          val priceAtY = expandedMax - (effectiveRange / gridSteps) * g

          if (showGrid) {
            drawLine(
              color = Mt5GridLine,
              start = Offset(0f, y),
              end = Offset(chartWidth, y),
              strokeWidth = 1f,
              pathEffect = dashedEffect
            )
          }

          // Right Price Scale Number (MetaTrader 5 style)
          drawText(
            textMeasurer = textMeasurer,
            text = formatMt5Price(priceAtY.toDouble(), symbol.decimals),
            topLeft = Offset(chartWidth + 5.dp.toPx(), y - 6.dp.toPx()),
            style = TextStyle(
              color = Mt5TextSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          )
        }

        // Vertical Grid lines & Time marks
        val candleCount = candles.size
        val candleSpacing = chartWidth / candleCount
        val candleBarWidth = (candleSpacing * 0.68f).coerceAtLeast(3f)

        val timeSteps = 5
        val stepIdx = (candleCount / timeSteps).coerceAtLeast(1)
        for (t in 0 until candleCount step stepIdx) {
          val x = t * candleSpacing + candleSpacing / 2f
          if (showGrid) {
            drawLine(
              color = Mt5GridLine,
              start = Offset(x, paddingTop),
              end = Offset(x, chartHeight + paddingTop),
              strokeWidth = 1f,
              pathEffect = dashedEffect
            )
          }

          val timeStr = timeFormat.format(Date(candles[t].timestamp))
          drawText(
            textMeasurer = textMeasurer,
            text = timeStr,
            topLeft = Offset(x - 14.dp.toPx(), height - 16.dp.toPx()),
            style = TextStyle(
              color = Mt5TextSecondary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          )
        }

        // 2. Right Axis Separator Line and Bottom Axis Line
        drawLine(
          color = Mt5Border,
          start = Offset(chartWidth, 0f),
          end = Offset(chartWidth, height),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = Mt5Border,
          start = Offset(0f, height - paddingBottom),
          end = Offset(width, height - paddingBottom),
          strokeWidth = 1.dp.toPx()
        )

        // 3. Volume Sub-Chart at Bottom
        if (showVolumes) {
          val maxVol = candles.maxOfOrNull { it.volume } ?: 100f
          val volBaseY = height - paddingBottom

          for (i in candles.indices) {
            val candle = candles[i]
            val x = i * candleSpacing + candleSpacing / 2f
            val vHeight = (candle.volume / maxVol) * volumeHeight * 0.85f
            val volColor = if (candle.isBullish) Mt5BullGreen.copy(alpha = 0.45f) else Mt5BearRed.copy(alpha = 0.45f)

            drawRect(
              color = volColor,
              topLeft = Offset(x - candleBarWidth / 2f, volBaseY - vHeight),
              size = Size(candleBarWidth, vHeight)
            )
          }
        }

        // 4. SMC Zones (Order Block, FVG, BOS)
        for (annotation in annotations) {
          when (annotation.type) {
            AnnotationType.OB -> {
              val topY = priceToY(annotation.price)
              val bottomY = priceToY(annotation.priceSecondary ?: (annotation.price * 0.999f))
              val leftX = annotation.candleIndex * candleSpacing
              val rightX = (annotation.candleEndIndex.coerceAtMost(candleCount - 1)) * candleSpacing

              drawRect(
                color = Mt5Purple.copy(alpha = 0.18f),
                topLeft = Offset(leftX, minOf(topY, bottomY)),
                size = Size(rightX - leftX, kotlin.math.abs(bottomY - topY))
              )
              drawRect(
                color = Mt5Purple.copy(alpha = 0.8f),
                topLeft = Offset(leftX, minOf(topY, bottomY)),
                size = Size(rightX - leftX, kotlin.math.abs(bottomY - topY)),
                style = Stroke(width = 1.dp.toPx())
              )
              drawText(
                textMeasurer = textMeasurer,
                text = "OB",
                topLeft = Offset(leftX + 2.dp.toPx(), minOf(topY, bottomY) + 1.dp.toPx()),
                style = TextStyle(color = Mt5Purple, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              )
            }
            AnnotationType.FVG -> {
              val topY = priceToY(annotation.price)
              val bottomY = priceToY(annotation.priceSecondary ?: (annotation.price * 0.9995f))
              val leftX = annotation.candleIndex * candleSpacing
              val rightX = (annotation.candleEndIndex.coerceAtMost(candleCount - 1)) * candleSpacing

              drawRect(
                color = Mt5Amber.copy(alpha = 0.20f),
                topLeft = Offset(leftX, minOf(topY, bottomY)),
                size = Size(rightX - leftX, kotlin.math.abs(bottomY - topY))
              )
              drawText(
                textMeasurer = textMeasurer,
                text = "FVG",
                topLeft = Offset(leftX + 2.dp.toPx(), minOf(topY, bottomY) + 1.dp.toPx()),
                style = TextStyle(color = Mt5Amber, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              )
            }
            AnnotationType.BOS -> {
              val lineY = priceToY(annotation.price)
              val startX = annotation.candleIndex * candleSpacing
              val endX = (annotation.candleEndIndex.coerceAtMost(candleCount - 1)) * candleSpacing

              drawLine(
                color = Mt5BidBlue,
                start = Offset(startX, lineY),
                end = Offset(endX, lineY),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
              )
              drawText(
                textMeasurer = textMeasurer,
                text = "BOS",
                topLeft = Offset(startX + (endX - startX) / 2 - 8.dp.toPx(), lineY - 12.dp.toPx()),
                style = TextStyle(color = Mt5BidBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              )
            }
            else -> {}
          }
        }

        // 5. Draw Price Action Series (Candlesticks / Bars / Line)
        when (chartStyle) {
          ChartStyle.CANDLESTICKS -> {
            for (i in candles.indices) {
              val candle = candles[i]
              val x = i * candleSpacing + candleSpacing / 2f
              val isBull = candle.isBullish
              val color = if (isBull) Mt5BullGreen else Mt5BearRed

              val openY = priceToY(candle.open)
              val closeY = priceToY(candle.close)
              val highY = priceToY(candle.high)
              val lowY = priceToY(candle.low)

              // Wick
              drawLine(
                color = color,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 1.2.dp.toPx()
              )

              // Body
              val bodyTop = minOf(openY, closeY)
              val bodyBottom = maxOf(openY, closeY)
              val bodyH = (bodyBottom - bodyTop).coerceAtLeast(1.5.dp.toPx())

              drawRect(
                color = color,
                topLeft = Offset(x - candleBarWidth / 2f, bodyTop),
                size = Size(candleBarWidth, bodyH)
              )
            }
          }
          ChartStyle.BARS -> {
            for (i in candles.indices) {
              val candle = candles[i]
              val x = i * candleSpacing + candleSpacing / 2f
              val isBull = candle.isBullish
              val color = if (isBull) Mt5BullGreen else Mt5BearRed

              val openY = priceToY(candle.open)
              val closeY = priceToY(candle.close)
              val highY = priceToY(candle.high)
              val lowY = priceToY(candle.low)

              // Vertical High-Low Bar
              drawLine(
                color = color,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 1.5.dp.toPx()
              )
              // Open tick (left)
              drawLine(
                color = color,
                start = Offset(x - candleBarWidth / 2f, openY),
                end = Offset(x, openY),
                strokeWidth = 1.5.dp.toPx()
              )
              // Close tick (right)
              drawLine(
                color = color,
                start = Offset(x, closeY),
                end = Offset(x + candleBarWidth / 2f, closeY),
                strokeWidth = 1.5.dp.toPx()
              )
            }
          }
          ChartStyle.LINE -> {
            for (i in 0 until candles.size - 1) {
              val x1 = i * candleSpacing + candleSpacing / 2f
              val y1 = priceToY(candles[i].close)
              val x2 = (i + 1) * candleSpacing + candleSpacing / 2f
              val y2 = priceToY(candles[i + 1].close)

              drawLine(
                color = Mt5BidBlue,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.dp.toPx()
              )
            }
          }
        }

        // 6. Draw MT5 Order Lines (Entry, SL, TP)
        activeSignal?.let { sig ->
          val entryY = priceToY(sig.entry.toFloat())
          val slY = priceToY(sig.stopLoss.toFloat())
          val tpY = priceToY(sig.tp1.toFloat())

          // Entry line
          drawLine(
            color = Mt5BullGreen,
            start = Offset(chartWidth * 0.35f, entryY),
            end = Offset(chartWidth, entryY),
            strokeWidth = 1.2.dp.toPx()
          )
          drawText(
            textMeasurer = textMeasurer,
            text = "BUY 0.10",
            topLeft = Offset(chartWidth - 56.dp.toPx(), entryY - 12.dp.toPx()),
            style = TextStyle(color = Mt5BullGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )

          // Stop Loss line (Red Dashed)
          drawLine(
            color = Mt5BearRed,
            start = Offset(chartWidth * 0.35f, slY),
            end = Offset(chartWidth, slY),
            strokeWidth = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
          )
          drawText(
            textMeasurer = textMeasurer,
            text = "SL ${formatMt5Price(sig.stopLoss, symbol.decimals)}",
            topLeft = Offset(chartWidth - 62.dp.toPx(), slY - 12.dp.toPx()),
            style = TextStyle(color = Mt5BearRed, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )

          // Take Profit line (Green Dashed)
          drawLine(
            color = Mt5BullGreen,
            start = Offset(chartWidth * 0.35f, tpY),
            end = Offset(chartWidth, tpY),
            strokeWidth = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
          )
          drawText(
            textMeasurer = textMeasurer,
            text = "TP ${formatMt5Price(sig.tp1, symbol.decimals)}",
            topLeft = Offset(chartWidth - 62.dp.toPx(), tpY - 12.dp.toPx()),
            style = TextStyle(color = Mt5BullGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )
        }

        // 7. Live Real-Time MT5 Bid and Ask Lines
        if (showBidAskLines && quote != null) {
          val bidY = priceToY(quote.bid.toFloat())
          val askY = priceToY(quote.ask.toFloat())

          // Bid Line (Solid Blue)
          drawLine(
            color = Mt5BidBlue,
            start = Offset(0f, bidY),
            end = Offset(chartWidth, bidY),
            strokeWidth = 1.dp.toPx()
          )

          // Ask Line (Dotted Red)
          drawLine(
            color = Mt5AskRed,
            start = Offset(0f, askY),
            end = Offset(chartWidth, askY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
          )

          // Right Price Badges for Bid and Ask
          drawRect(
            color = Mt5BidBlue,
            topLeft = Offset(chartWidth + 1.dp.toPx(), bidY - 8.dp.toPx()),
            size = Size(paddingRight - 2.dp.toPx(), 16.dp.toPx())
          )
          drawText(
            textMeasurer = textMeasurer,
            text = quote.formattedBid,
            topLeft = Offset(chartWidth + 4.dp.toPx(), bidY - 6.dp.toPx()),
            style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )

          drawRect(
            color = Mt5AskRed,
            topLeft = Offset(chartWidth + 1.dp.toPx(), askY - 8.dp.toPx()),
            size = Size(paddingRight - 2.dp.toPx(), 16.dp.toPx())
          )
          drawText(
            textMeasurer = textMeasurer,
            text = quote.formattedAsk,
            topLeft = Offset(chartWidth + 4.dp.toPx(), askY - 6.dp.toPx()),
            style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )
        }

        // 8. Crosshair Cursor (with exact price & timestamp at intersection point)
        val activeCrosshairIndex = crosshairIndex ?: if (isCrosshairActive) candleCount - 5 else null
        if (activeCrosshairIndex != null) {
          val candle = candles[activeCrosshairIndex.coerceIn(0, candleCount - 1)]
          val cX = activeCrosshairIndex * candleSpacing + candleSpacing / 2f
          val cY = crosshairY ?: priceToY(candle.close)
          val priceAtCursor = yToPrice(cY)
          val timeAtCursor = fullDateTimeFormat.format(Date(candle.timestamp))

          // Vertical & Horizontal crosshair lines across entire chart
          drawLine(
            color = Mt5Crosshair,
            start = Offset(cX, 0f),
            end = Offset(cX, height - paddingBottom),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
          )
          drawLine(
            color = Mt5Crosshair,
            start = Offset(0f, cY),
            end = Offset(chartWidth, cY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
          )

          // Center Intersection Reticle
          drawCircle(
            color = Mt5BidBlue,
            radius = 4.dp.toPx(),
            center = Offset(cX, cY)
          )
          drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = Offset(cX, cY)
          )

          // Floating Intersection Tooltip Badge (Exact Price + Exact Timestamp at intersection)
          val badgeText = "${formatMt5Price(priceAtCursor.toDouble(), symbol.decimals)} | $timeAtCursor"
          val badgeWidth = 148.dp.toPx()
          val badgeHeight = 22.dp.toPx()
          val badgeX = if (cX + badgeWidth + 10.dp.toPx() < chartWidth) cX + 10.dp.toPx() else cX - badgeWidth - 10.dp.toPx()
          val badgeY = if (cY - badgeHeight - 8.dp.toPx() > paddingTop) cY - badgeHeight - 8.dp.toPx() else cY + 10.dp.toPx()

          drawRoundRect(
            color = Color(0xEE1E222D),
            topLeft = Offset(badgeX, badgeY),
            size = Size(badgeWidth, badgeHeight),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
          )
          drawRoundRect(
            color = Mt5BidBlue,
            topLeft = Offset(badgeX, badgeY),
            size = Size(badgeWidth, badgeHeight),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 1.2.dp.toPx())
          )
          drawText(
            textMeasurer = textMeasurer,
            text = badgeText,
            topLeft = Offset(badgeX + 6.dp.toPx(), badgeY + 4.dp.toPx()),
            style = TextStyle(
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          )

          // Right axis crosshair price tag
          drawRect(
            color = Color.White,
            topLeft = Offset(chartWidth + 1.dp.toPx(), cY - 8.dp.toPx()),
            size = Size(paddingRight - 2.dp.toPx(), 16.dp.toPx())
          )
          drawText(
            textMeasurer = textMeasurer,
            text = formatMt5Price(priceAtCursor.toDouble(), symbol.decimals),
            topLeft = Offset(chartWidth + 4.dp.toPx(), cY - 6.dp.toPx()),
            style = TextStyle(color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )

          // Bottom axis crosshair time tag
          drawRect(
            color = Color.White,
            topLeft = Offset(cX - 24.dp.toPx(), height - paddingBottom + 2.dp.toPx()),
            size = Size(48.dp.toPx(), 16.dp.toPx())
          )
          drawText(
            textMeasurer = textMeasurer,
            text = timeFormat.format(Date(candle.timestamp)),
            topLeft = Offset(cX - 20.dp.toPx(), height - paddingBottom + 4.dp.toPx()),
            style = TextStyle(color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          )
        }

        // 9. MT5 Top-Left OHLC Readout
        val activeCandle = activeCrosshairIndex?.let { candles[it.coerceIn(0, candleCount - 1)] } ?: candles.last()
        val ohlcText = "O: ${formatMt5Price(activeCandle.open.toDouble(), symbol.decimals)}  H: ${formatMt5Price(activeCandle.high.toDouble(), symbol.decimals)}  L: ${formatMt5Price(activeCandle.low.toDouble(), symbol.decimals)}  C: ${formatMt5Price(activeCandle.close.toDouble(), symbol.decimals)}"
        val ohlcColor = if (activeCandle.isBullish) Mt5BullGreen else Mt5BearRed

        drawText(
          textMeasurer = textMeasurer,
          text = ohlcText,
          topLeft = Offset(8.dp.toPx(), 6.dp.toPx()),
          style = TextStyle(
            color = ohlcColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
          )
        )
      }
    }
  }
}

private fun formatMt5Price(price: Double, decimals: Int): String {
  return "%.${decimals}f".format(price)
}
