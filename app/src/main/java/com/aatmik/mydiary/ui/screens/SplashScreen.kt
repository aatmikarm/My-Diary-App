package com.aatmik.mydiary.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatmik.mydiary.R
import com.aatmik.mydiary.ui.theme.DiaryPink
import com.aatmik.mydiary.ui.theme.DiaryPinkSubtle
import com.aatmik.mydiary.viewmodel.DiaryViewModel
import com.aatmik.mydiary.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: DiaryViewModel) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(800) // Brief graceful pause
        val security = viewModel.securityManager
        when {
            security.isFirstLaunch -> viewModel.navigateTo(Screen.WELCOME)
            security.isAppLockEnabled -> viewModel.navigateTo(Screen.LOCK)
            else -> viewModel.navigateTo(Screen.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.diary_icon),
                contentDescription = "My Diary Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My Diary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your private space for your memories.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}