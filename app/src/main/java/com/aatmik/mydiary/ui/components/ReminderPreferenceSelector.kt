package com.aatmik.mydiary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aatmik.mydiary.data.ReminderPreset
import com.aatmik.mydiary.ui.theme.DiaryPink

private data class ReminderOptionUi(val preset: ReminderPreset, val title: String, val subtitle: String)

private val options = listOf(
    ReminderOptionUi(ReminderPreset.MORNING, "Morning", "8:00 AM — set intentions for the day"),
    ReminderOptionUi(ReminderPreset.AFTERNOON, "Afternoon", "1:00 PM — a midday check-in"),
    ReminderOptionUi(ReminderPreset.EVENING, "Evening", "9:00 PM — reflect before bed"),
    ReminderOptionUi(ReminderPreset.CUSTOM, "Custom time", "Pick your own time"),
    ReminderOptionUi(ReminderPreset.OFF, "Don't remind me", "Turn off diary reminders")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPreferenceSelector(
    selectedPreset: ReminderPreset,
    customHour: Int,
    customMinute: Int,
    onPresetSelected: (ReminderPreset) -> Unit,
    onCustomTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedPreset == option.preset
            Card(
                onClick = { onPresetSelected(option.preset) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onPresetSelected(option.preset) },
                        colors = RadioButtonDefaults.colors(selectedColor = DiaryPink)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(option.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            option.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Inline expansion — same screen, no dialog, no extra navigation
            AnimatedVisibility(visible = isSelected && option.preset == ReminderPreset.CUSTOM) {
                val timeState = rememberTimePickerState(
                    initialHour = customHour,
                    initialMinute = customMinute,
                    is24Hour = false
                )
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    TimePicker(state = timeState)
                    LaunchedEffect(timeState.hour, timeState.minute) {
                        onCustomTimeChanged(timeState.hour, timeState.minute)
                    }
                }
            }
        }
    }
}