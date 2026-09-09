package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiaryPink
import com.example.ui.theme.DiaryPinkSubtle
import com.example.util.DiaryUtils
import com.example.viewmodel.DiaryViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: DiaryViewModel) {
    val allEntries by viewModel.allEntries.collectAsState()
    val selectedDateMillis by viewModel.selectedCalendarDateMillis.collectAsState()
    val entriesForDate by viewModel.entriesForSelectedDate.collectAsState()

    var currentCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedDateMillis })
    }

    // Days with entries in current month
    val daysWithEntries = remember(allEntries, currentCalendar) {
        val set = mutableSetOf<Int>()
        val cal = Calendar.getInstance()
        allEntries.forEach { entry ->
            cal.timeInMillis = entry.dateMillis
            if (cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
            ) {
                set.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        set
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("calendar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val today = Calendar.getInstance()
                            currentCalendar = today
                            viewModel.setSelectedCalendarDate(today.timeInMillis)
                        },
                        modifier = Modifier.testTag("calendar_today_button")
                    ) {
                        Text("Today", color = DiaryPink, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month Navigation Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val newCal = currentCalendar.clone() as Calendar
                                    newCal.add(Calendar.MONTH, -1)
                                    currentCalendar = newCal
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month"
                                )
                            }

                            Text(
                                text = DiaryUtils.formatMonthYear(currentCalendar),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(
                                onClick = {
                                    val newCal = currentCalendar.clone() as Calendar
                                    newCal.add(Calendar.MONTH, 1)
                                    currentCalendar = newCal
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Weekdays header
                        val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weekdays.forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.size(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Month Days Grid calculation
                        val cal = currentCalendar.clone() as Calendar
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
                        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                        val todayCal = Calendar.getInstance()
                        val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

                        val totalCells = ((firstDayOfWeek + maxDays + 6) / 7) * 7
                        val rows = totalCells / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (col in 0..6) {
                                        val dayIndex = row * 7 + col
                                        val dayNumber = dayIndex - firstDayOfWeek + 1

                                        if (dayNumber in 1..maxDays) {
                                            val isSelected = (
                                                selectedCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                                                selectedCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                                selectedCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                            )
                                            val isToday = (
                                                todayCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                                                todayCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                                todayCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                            )
                                            val hasMemory = daysWithEntries.contains(dayNumber)

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) DiaryPink else Color.Transparent)
                                                    .border(
                                                        width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                        color = if (isToday && !isSelected) DiaryPink else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        val targetCal = currentCalendar.clone() as Calendar
                                                        targetCal.set(Calendar.DAY_OF_MONTH, dayNumber)
                                                        viewModel.setSelectedCalendarDate(targetCal.timeInMillis)
                                                    },
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> DiaryPink
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                // Dot for day with memories
                                                if (hasMemory) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else DiaryPink)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.size(4.dp))
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.size(38.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Date Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DiaryUtils.formatDate(selectedDateMillis),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DiaryPinkSubtle
                    ) {
                        Text(
                            text = "${entriesForDate.size} ${if (entriesForDate.size == 1) "memory" else "memories"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DiaryPink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Entries for selected date or write prompt
            if (entriesForDate.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nothing written yet for this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.openCreateEntry(forDateMillis = selectedDateMillis)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DiaryPink),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.testTag("calendar_write_day_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Write for this day", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(entriesForDate) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        onClick = { viewModel.openEntryDetail(entry) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
