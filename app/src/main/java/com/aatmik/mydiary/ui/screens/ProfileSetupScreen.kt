package com.aatmik.mydiary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatmik.mydiary.data.ProfileManager
import com.aatmik.mydiary.ui.theme.DiaryPink
import com.aatmik.mydiary.ui.theme.DiaryPinkSubtle
import com.aatmik.mydiary.viewmodel.DiaryViewModel

private val GENDER_OPTIONS = listOf("Female", "Male", "Non-binary", "Prefer not to say")

private val GOAL_OPTIONS = listOf(
    "Track my mood",
    "Capture memories",
    "Process my feelings",
    "Build a gratitude habit",
    "Just for fun"
)

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(viewModel: DiaryViewModel) {
    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var selectedGoals by remember { mutableStateOf(setOf<String>()) }

    var monthExpanded by remember { mutableStateOf(false) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var dayText by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }

    val liveAge = remember(selectedMonthIndex, dayText, yearText) {
        ProfileManager.calculateAgeFrom(
            year = yearText.toIntOrNull(),
            month = selectedMonthIndex?.let { it + 1 },
            day = dayText.toIntOrNull()
        )
    }

    fun toggleGoal(goal: String) {
        selectedGoals = if (selectedGoals.contains(goal)) selectedGoals - goal else selectedGoals + goal
    }

    fun continueSetup() {
        viewModel.completeProfileSetup(
            name = name,
            birthdayYear = yearText.toIntOrNull(),
            birthdayMonth = selectedMonthIndex?.let { it + 1 },
            birthdayDay = dayText.toIntOrNull(),
            gender = selectedGender ?: "",
            goals = selectedGoals
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Let's personalize your diary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This stays on your device. Takes 30 seconds — skip anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))

            SectionLabel("Your name")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g. Aatmik") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("Your birthday (optional)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dayText,
                    onValueChange = {
                        if (it.length <= 2) {
                            val filtered = it.filter { c -> c.isDigit() }
                            val num = filtered.toIntOrNull()
                            if (filtered.isEmpty() || (num != null && num in 1..31)) dayText = filtered
                        }
                    },
                    placeholder = { Text("Day") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier
                        .width(70.dp)
                        .testTag("profile_day_input")
                )

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedMonthIndex?.let { MONTHS[it] } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        placeholder = { Text("Birth month") },
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = DiaryPinkSubtle,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_month_field")
                    )
                    // Transparent overlay so taps open the menu even though the field above is disabled
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { monthExpanded = true }
                    )
                    DropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        MONTHS.forEachIndexed { index, monthName ->
                            DropdownMenuItem(
                                text = { Text(monthName) },
                                onClick = {
                                    selectedMonthIndex = index
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = yearText,
                    onValueChange = { if (it.length <= 4) yearText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Year") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier
                        .width(90.dp)
                        .testTag("profile_year_input")
                )
            }

            if (liveAge != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "You're $liveAge \uD83C\uDF89",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DiaryPink,
                    modifier = Modifier.testTag("profile_age_preview")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("How should we refer to you? (optional)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GENDER_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = selectedGender == option,
                        onClick = { selectedGender = if (selectedGender == option) null else option },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DiaryPink,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("profile_gender_${option.replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("Why are you journaling? (pick any)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GOAL_OPTIONS.forEach { goal ->
                    FilterChip(
                        selected = selectedGoals.contains(goal),
                        onClick = { toggleGoal(goal) },
                        label = { Text(goal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DiaryPink,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("profile_goal_${goal.replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { continueSetup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("profile_continue_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiaryPink, contentColor = Color.White)
            ) {
                Text(text = "Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { viewModel.skipProfileSetup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_skip_button")
            ) {
                Text(
                    text = "Skip for now",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DiaryPink,
    unfocusedBorderColor = DiaryPinkSubtle
)