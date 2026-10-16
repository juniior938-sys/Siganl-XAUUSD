package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.Mt5AnalyzerScreen
import com.example.ui.screens.Mt5QuotesScreen
import com.example.ui.screens.Mt5SettingsScreen
import com.example.ui.screens.Mt5TerminalScreen
import com.example.ui.theme.Mt5BidBlue
import com.example.ui.theme.Mt5Border
import com.example.ui.theme.Mt5DarkBackground
import com.example.ui.theme.Mt5DarkSurface
import com.example.ui.theme.Mt5DarkSurfaceElevated
import com.example.ui.theme.Mt5TextPrimary
import com.example.ui.theme.Mt5TextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MegaTradingViewModel

enum class Mt5Tab(val label: String, val icon: ImageVector, val tag: String) {
  QUOTES("Cotações", Icons.AutoMirrored.Filled.FormatListBulleted, "tab_quotes"),
  CHART("Gráfico", Icons.Default.ShowChart, "tab_chart"),
  ANALYZER("Análise IA", Icons.Default.AutoAwesome, "tab_analyzer"),
  SETTINGS("Ajustes", Icons.Default.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {
  private val viewModel: MegaTradingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Mt5AppContent(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun Mt5AppContent(viewModel: MegaTradingViewModel) {
  var currentTab by remember { mutableStateOf(Mt5Tab.CHART) }

  if (currentTab != Mt5Tab.CHART) {
    BackHandler {
      currentTab = Mt5Tab.CHART
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .border(width = 1.dp, color = Mt5Border)
          .height(68.dp),
        containerColor = Mt5DarkSurface,
        tonalElevation = 6.dp
      ) {
        for (tab in Mt5Tab.entries) {
          val isSelected = tab == currentTab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            modifier = Modifier.testTag(tab.tag),
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = if (isSelected) Mt5BidBlue else Mt5TextSecondary,
                modifier = Modifier.size(20.dp)
              )
            },
            label = {
              Text(
                text = tab.label,
                color = if (isSelected) Mt5TextPrimary else Mt5TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Mt5BidBlue,
              selectedTextColor = Mt5TextPrimary,
              unselectedIconColor = Mt5TextSecondary,
              unselectedTextColor = Mt5TextSecondary,
              indicatorColor = Mt5DarkSurfaceElevated
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Mt5DarkBackground)
        .padding(innerPadding)
    ) {
      when (currentTab) {
        Mt5Tab.QUOTES -> {
          Mt5QuotesScreen(
            viewModel = viewModel,
            onNavigateToChart = { asset ->
              currentTab = Mt5Tab.CHART
            }
          )
        }
        Mt5Tab.CHART -> {
          Mt5TerminalScreen(
            viewModel = viewModel,
            onNavigateToAnalyzer = { currentTab = Mt5Tab.ANALYZER }
          )
        }
        Mt5Tab.ANALYZER -> {
          Mt5AnalyzerScreen(viewModel = viewModel)
        }
        Mt5Tab.SETTINGS -> {
          Mt5SettingsScreen(viewModel = viewModel)
        }
      }
    }
  }
}
