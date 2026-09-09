package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CreateEditEntryScreen
import com.example.ui.screens.DrawingEditorScreen
import com.example.ui.screens.EntryDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinMode
import com.example.ui.screens.PinScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AdManager
import com.example.viewmodel.DiaryViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK (AdMob)
        AdManager.initialize(this)

        setContent {
            val diaryViewModel: DiaryViewModel = viewModel()
            val themeMode by diaryViewModel.themeMode.collectAsState()

            val isDark = when (themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiaryApp(viewModel = diaryViewModel)
                }
            }
        }
    }
}

@Composable
fun DiaryApp(viewModel: DiaryViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(300),
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.SPLASH -> SplashScreen(viewModel = viewModel)
            Screen.WELCOME -> WelcomeScreen(viewModel = viewModel)
            Screen.PIN_SETUP -> PinScreen(viewModel = viewModel, mode = PinMode.SETUP)
            Screen.LOCK -> PinScreen(viewModel = viewModel, mode = PinMode.UNLOCK)
            Screen.HOME -> HomeScreen(viewModel = viewModel)
            Screen.CALENDAR -> CalendarScreen(viewModel = viewModel)
            Screen.SEARCH -> SearchScreen(viewModel = viewModel)
            Screen.CREATE_EDIT -> CreateEditEntryScreen(
                viewModel = viewModel,
                onSaveFinished = {
                    if (activity != null) {
                        AdManager.showInterstitialAd(activity) {
                            viewModel.navigateBack()
                        }
                    } else {
                        viewModel.navigateBack()
                    }
                }
            )
            Screen.DETAIL -> EntryDetailScreen(viewModel = viewModel)
            Screen.DRAWING -> DrawingEditorScreen(
                viewModel = viewModel,
                onDrawingSaved = { path ->
                    viewModel.pendingDoodlePath = path
                }
            )
            Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}

