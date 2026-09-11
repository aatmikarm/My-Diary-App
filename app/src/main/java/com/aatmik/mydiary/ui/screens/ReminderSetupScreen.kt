package com.aatmik.mydiary.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aatmik.mydiary.data.ReminderManager
import com.aatmik.mydiary.data.ReminderPreset
import com.aatmik.mydiary.ui.components.ReminderPreferenceSelector
import com.aatmik.mydiary.ui.theme.DiaryPink
import com.aatmik.mydiary.viewmodel.DiaryViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ReminderSetupScreen(viewModel: DiaryViewModel) {
    val context = LocalContext.current
    var selectedPreset by remember { mutableStateOf(ReminderManager.DEFAULT_PRESET) } // Evening pre-selected
    var customHour by remember { mutableIntStateOf(ReminderManager.DEFAULT_HOUR) }
    var customMinute by remember { mutableIntStateOf(ReminderManager.DEFAULT_MINUTE) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Whether granted or not, proceed — Android just won't show the notification without it
        viewModel.finishReminderSetup(selectedPreset, customHour, customMinute)
    }

    fun confirmAndContinue() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                selectedPreset != ReminderPreset.OFF &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.finishReminderSetup(selectedPreset, customHour, customMinute)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "When should we remind you to write?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We'll nudge you once a day. Change this anytime in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            ReminderPreferenceSelector(
                selectedPreset = selectedPreset,
                customHour = customHour,
                customMinute = customMinute,
                onPresetSelected = { selectedPreset = it },
                onCustomTimeChanged = { h, m -> customHour = h; customMinute = m }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { confirmAndContinue() },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("reminder_continue_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiaryPink, contentColor = Color.White)
            ) {
                Text(text = "Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}