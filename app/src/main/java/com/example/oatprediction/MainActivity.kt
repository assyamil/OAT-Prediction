package com.example.oatprediction

import android.os.Bundle
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.example.oatprediction.viewmodel.ThemeViewModel
import com.example.oatprediction.viewmodel.StartupState
import com.example.oatprediction.data.preferences.DataStoreThemePreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oatprediction.data.preferences.DataStoreIntroductionPreferences
import com.example.oatprediction.ui.introduction.StartupContent
import com.example.oatprediction.viewmodel.StartupViewModel
import com.example.oatprediction.navigation.OatNavigation
import com.example.oatprediction.ui.theme.OATPredictionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearance: ThemeViewModel = viewModel(factory = ThemeViewModel.factory(DataStoreThemePreferences.from(applicationContext)))
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !appearance.dark
                    isAppearanceLightNavigationBars = !appearance.dark
                }
            }
            OATPredictionTheme(darkTheme = appearance.dark) {
                val startup: StartupViewModel = viewModel(
                    factory = StartupViewModel.factory(DataStoreIntroductionPreferences.from(applicationContext)),
                )
                StartupContent(if (appearance.loading) StartupState.Loading else startup.state, startup::start, startup::retryRead) {
                    OatNavigation(darkTheme = appearance.dark, themeSaving = appearance.saving,
                        onToggleTheme = appearance::toggle, themeError = appearance.error,
                        onThemeErrorShown = appearance::dismissError)
                }
            }
        }
    }
}
