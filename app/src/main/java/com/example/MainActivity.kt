package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainAppContainerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PrayerCounterViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: PrayerCounterViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        PrayerProclamationApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun PrayerProclamationApp(
    viewModel: PrayerCounterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    Crossfade(
        targetState = showSplash,
        label = "splash_to_main_transition",
        modifier = modifier.fillMaxSize()
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(
                language = state.language,
                onLanguageChange = { viewModel.setLanguage(it) },
                onNavigateToMain = { showSplash = false }
            )
        } else {
            MainAppContainerScreen(
                viewModel = viewModel
            )
        }
    }
}
