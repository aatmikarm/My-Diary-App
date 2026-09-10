package com.aatmik.mydiary.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aatmik.mydiary.ui.theme.DiaryPink
import com.aatmik.mydiary.ui.theme.DiaryPinkSubtle
import com.aatmik.mydiary.util.AnalyticsManager
import com.aatmik.mydiary.viewmodel.DiaryViewModel
import java.io.File
import java.io.FileOutputStream

data class DrawingPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingEditorScreen(
    viewModel: DiaryViewModel,
    onDrawingSaved: (String) -> Unit
) {
    val context = LocalContext.current

    val paths = remember { mutableStateListOf<DrawingPath>() }
    val undonePaths = remember { mutableStateListOf<DrawingPath>() }

    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentColor by remember { mutableStateOf(Color(0xFF211A1D)) }
    var currentStrokeWidth by remember { mutableFloatStateOf(10f) }
    var isEraser by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val palette = listOf(
        Color(0xFF211A1D), // Dark charcoal
        DiaryPink,         // Pink
        Color(0xFF8E44AD), // Plum
        Color(0xFF3498DB), // Sky blue
        Color(0xFF2ECC71), // Mint
        Color(0xFFF39C12), // Honey
        Color(0xFFE67E22), // Peach
        Color(0xFF95A5A6)  // Slate
    )

    fun handleBack() {
        if (paths.isNotEmpty()) {
            showDiscardDialog = true
        } else {
            viewModel.navigateBack()
        }
    }

    BackHandler { handleBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Drawing",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { handleBack() },
                        modifier = Modifier.testTag("drawing_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                val last = paths.removeAt(paths.size - 1)
                                undonePaths.add(last)
                            }
                        },
                        enabled = paths.isNotEmpty(),
                        modifier = Modifier.testTag("drawing_undo_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }

                    IconButton(
                        onClick = {
                            if (undonePaths.isNotEmpty()) {
                                val restored = undonePaths.removeAt(undonePaths.size - 1)
                                paths.add(restored)
                            }
                        },
                        enabled = undonePaths.isNotEmpty(),
                        modifier = Modifier.testTag("drawing_redo_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }

                    Button(
                        onClick = {
                            // Render to internal file
                            try {
                                val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                canvas.drawColor(android.graphics.Color.WHITE)

                                val paint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeCap = android.graphics.Paint.Cap.ROUND
                                    strokeJoin = android.graphics.Paint.Join.ROUND
                                }

                                paths.forEach { p ->
                                    paint.strokeWidth = p.strokeWidth
                                    if (p.isEraser) {
                                        paint.color = android.graphics.Color.WHITE
                                    } else {
                                        paint.color = android.graphics.Color.argb(
                                            (p.color.alpha * 255).toInt(),
                                            (p.color.red * 255).toInt(),
                                            (p.color.green * 255).toInt(),
                                            (p.color.blue * 255).toInt()
                                        )
                                    }
                                    canvas.drawPath(p.path.asAndroidPath(), paint)
                                }

                                val file = File(context.filesDir, "doodle_${System.currentTimeMillis()}.png")
                                FileOutputStream(file).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                }
                                AnalyticsManager.log(AnalyticsManager.Events.DRAWING_SAVED)
                                onDrawingSaved(file.absolutePath)
                                viewModel.navigateBack()
                            } catch (_: Exception) {
                                viewModel.navigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DiaryPink),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("drawing_done_button")
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            // Drawing Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, DiaryPinkSubtle, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                undonePaths.clear()
                            },
                            onDrag = { change, _ ->
                                currentPath?.lineTo(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                currentPath?.let {
                                    paths.add(
                                        DrawingPath(
                                            path = it,
                                            color = currentColor,
                                            strokeWidth = currentStrokeWidth,
                                            isEraser = isEraser
                                        )
                                    )
                                }
                                currentPath = null
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    paths.forEach { dp ->
                        drawPath(
                            path = dp.path,
                            color = if (dp.isEraser) Color.White else dp.color,
                            style = Stroke(
                                width = dp.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                    currentPath?.let {
                        drawPath(
                            path = it,
                            color = if (isEraser) Color.White else currentColor,
                            style = Stroke(
                                width = currentStrokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Bottom Tool controls
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Palette row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palette.forEach { col ->
                            val isSelected = !isEraser && currentColor == col
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) DiaryPink else Color.Black.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        currentColor = col
                                        isEraser = false
                                    }
                            )
                        }

                        // Eraser tool button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isEraser) DiaryPink else DiaryPinkSubtle)
                                .clickable { isEraser = !isEraser },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Eraser",
                                tint = if (isEraser) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Clear all button
                        IconButton(
                            onClick = {
                                paths.clear()
                                undonePaths.clear()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stroke width slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Size",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = currentStrokeWidth,
                            onValueChange = { currentStrokeWidth = it },
                            valueRange = 4f..36f,
                            colors = SliderDefaults.colors(
                                thumbColor = DiaryPink,
                                activeTrackColor = DiaryPink
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard drawing?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to discard your doodle?") },
            confirmButton = {
                Button(
                    onClick = {
                        AnalyticsManager.log(AnalyticsManager.Events.DRAWING_DISCARDED)
                        showDiscardDialog = false
                        viewModel.navigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Drawing")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
