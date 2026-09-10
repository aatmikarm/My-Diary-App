package com.aatmik.mydiary.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.aatmik.mydiary.ui.theme.DiaryPink
import com.aatmik.mydiary.ui.theme.DiaryPinkSubtle
import com.aatmik.mydiary.viewmodel.DiaryViewModel

enum class PinMode {
    SETUP,
    UNLOCK,
    CHANGE_PIN
}

@Composable
fun PinScreen(
    viewModel: DiaryViewModel,
    mode: PinMode
) {
    var targetLength by remember {
        mutableStateOf(if (mode == PinMode.UNLOCK) viewModel.securityManager.pinLength else 4)
    }

    // Step 1: Initial pin, Step 2: Confirm pin (for setup / change)
    var setupStep by remember { mutableStateOf(1) }
    var firstEnteredPin by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBiometricPrompt by remember { mutableStateOf(false) }

    val title = when (mode) {
        PinMode.UNLOCK -> "Welcome back"
        PinMode.SETUP, PinMode.CHANGE_PIN -> {
            if (setupStep == 1) "Create your diary PIN" else "Confirm your PIN"
        }
    }

    val subtitle = when (mode) {
        PinMode.UNLOCK -> "Enter your PIN to open your diary"
        PinMode.SETUP, PinMode.CHANGE_PIN -> {
            if (setupStep == 1) "Choose a $targetLength-digit PIN" else "Re-enter the same PIN to confirm"
        }
    }

    fun onDigitPress(digit: String) {
        if (currentPinInput.length < targetLength) {
            val updated = currentPinInput + digit
            currentPinInput = updated
            errorMessage = null

            if (updated.length == targetLength) {
                // Completed entering digits
                when (mode) {
                    PinMode.UNLOCK -> {
                        val ok = viewModel.unlockWithPin(updated)
                        if (!ok) {
                            errorMessage = "Incorrect PIN. Try again."
                            currentPinInput = ""
                        }
                    }
                    PinMode.SETUP, PinMode.CHANGE_PIN -> {
                        if (setupStep == 1) {
                            firstEnteredPin = updated
                            currentPinInput = ""
                            setupStep = 2
                        } else {
                            if (updated == firstEnteredPin) {
                                // Match!
                                showBiometricPrompt = true
                            } else {
                                errorMessage = "PINs did not match. Let's start over."
                                currentPinInput = ""
                                firstEnteredPin = ""
                                setupStep = 1
                            }
                        }
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (currentPinInput.isNotEmpty()) {
            currentPinInput = currentPinInput.dropLast(1)
            errorMessage = null
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = DiaryPink,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Length toggle for setup
                if (mode == PinMode.SETUP && setupStep == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = targetLength == 4,
                            onClick = {
                                targetLength = 4
                                currentPinInput = ""
                            },
                            label = { Text("4 Digits") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiaryPink,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = targetLength == 6,
                            onClick = {
                                targetLength = 6
                                currentPinInput = ""
                            },
                            label = { Text("6 Digits") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiaryPink,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // PIN indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until targetLength) {
                        val isFilled = i < currentPinInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) DiaryPink else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) DiaryPink else DiaryPink.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            when (key) {
                                "DEL" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable { onBackspace() }
                                            .testTag("pin_backspace"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                "BIO" -> {
                                    if (mode == PinMode.UNLOCK && viewModel.securityManager.isBiometricEnabled) {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.unlockBiometric()
                                                }
                                                .testTag("pin_biometric"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biometric Unlock",
                                                tint = DiaryPink,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(72.dp))
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { onDigitPress(key) }
                                            .testTag("pin_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Biometric prompt dialog on finishing setup
    if (showBiometricPrompt) {
        AlertDialog(
            onDismissRequest = {
                viewModel.completeFirstLaunch(pin = firstEnteredPin, biometric = false)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = DiaryPink,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Enable Biometric Unlock?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Unlock your diary instantly using your fingerprint or face recognition alongside your PIN.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeFirstLaunch(pin = firstEnteredPin, biometric = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DiaryPink),
                    modifier = Modifier.testTag("enable_biometrics_button")
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.completeFirstLaunch(pin = firstEnteredPin, biometric = false)
                    }
                ) {
                    Text("Not Now")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
