package com.aatmik.mydiary

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
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
import com.aatmik.mydiary.ui.screens.CalendarScreen
import com.aatmik.mydiary.ui.screens.CreateEditEntryScreen
import com.aatmik.mydiary.ui.screens.DrawingEditorScreen
import com.aatmik.mydiary.ui.screens.EntryDetailScreen
import com.aatmik.mydiary.ui.screens.HomeScreen
import com.aatmik.mydiary.ui.screens.PinMode
import com.aatmik.mydiary.ui.screens.PinScreen
import com.aatmik.mydiary.ui.screens.ReminderSetupScreen
import com.aatmik.mydiary.ui.screens.SearchScreen
import com.aatmik.mydiary.ui.screens.SettingsScreen
import com.aatmik.mydiary.ui.screens.SplashScreen
import com.aatmik.mydiary.ui.screens.WelcomeScreen
import com.aatmik.mydiary.ui.theme.MyApplicationTheme
import com.aatmik.mydiary.util.AdManager
import com.aatmik.mydiary.util.AnalyticsManager
import com.aatmik.mydiary.util.RemoteConfigManager
import com.aatmik.mydiary.viewmodel.DiaryViewModel
import com.aatmik.mydiary.viewmodel.Screen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK (AdMob)
        RemoteConfigManager.initialize()
        AdManager.initialize(this)
        AnalyticsManager.initialize(this)

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
            Screen.REMINDER_SETUP -> ReminderSetupScreen(viewModel = viewModel)
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
                    viewModel.editEntryDraft = viewModel.editEntryDraft?.copy(drawingPath = path)
                }
            )
            Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}

