package com.aatmik.mydiary.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aatmik.mydiary.data.DiaryEntry
import com.aatmik.mydiary.ui.components.DeleteConfirmationDialog
import com.aatmik.mydiary.ui.components.FullScreenPhotoViewer
import com.aatmik.mydiary.ui.theme.DiaryPink
import com.aatmik.mydiary.ui.theme.DiaryPinkSubtle
import com.aatmik.mydiary.util.AnalyticsManager
import com.aatmik.mydiary.util.DiaryUtils
import com.aatmik.mydiary.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EntryDetailScreen(viewModel: DiaryViewModel) {
    val context = LocalContext.current
    val currentEntry by viewModel.currentDetailEntry.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var fullScreenPhoto by remember { mutableStateOf<String?>(null) }

    val entry = currentEntry ?: return

    val photos = DiaryUtils.parseJsonList(entry.photosJson)
    val stickers = DiaryUtils.parseJsonList(entry.stickersJson)
    val tags = DiaryUtils.parseJsonList(entry.tagsJson)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(entry) },
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (entry.isFavorite) DiaryPink else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("detail_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share High-Quality Image (.png)") },
                            onClick = {
                                showMenu = false
                                val uris = DiaryUtils.exportToImage(context, entry)
                                if (uris.isNotEmpty()) {
                                    AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                        putString("format", "image")
                                        putString("source", "export_sheet")
                                        putInt("page_count", uris.size)
                                    }
                                    DiaryUtils.shareImages(context, uris, "Share Diary Image")
                                    Toast.makeText(context, "Image created successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share as Text") },
                            onClick = {
                                showMenu = false
                                AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                    putString("format", "text")
                                    putString("source", "menu")
                                }
                                DiaryUtils.shareAsText(context, entry)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export to PDF") },
                            onClick = {
                                showMenu = false
                                val uri = DiaryUtils.exportToPdf(context, entry)
                                if (uri != null) {
                                    AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                        putString("format", "pdf")
                                        putString("source", "menu")
                                    }
                                    DiaryUtils.shareFile(context, uri, "application/pdf", "Share Diary PDF")
                                    Toast.makeText(context, "PDF created successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export to TXT") },
                            onClick = {
                                showMenu = false
                                val uri = DiaryUtils.exportToTxt(context, entry)
                                if (uri != null) {
                                    AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                        putString("format", "txt")
                                        putString("source", "menu")
                                    }
                                    DiaryUtils.shareFile(context, uri, "text/plain", "Share Diary Text")
                                    Toast.makeText(context, "TXT created successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Memory", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showExportSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_export_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export")
                    }

                    Button(
                        onClick = { viewModel.openEditEntry(entry) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("detail_edit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DiaryPink),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Memory", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Date, Mood & Time Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = DiaryUtils.getMoodEmoji(entry.mood),
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = DiaryUtils.formatDate(entry.dateMillis),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Written at ${DiaryUtils.formatTime(entry.createdMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (entry.weather.isNotBlank() || entry.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOf(entry.weather, entry.location).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = DiaryPink
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title
            if (entry.title.isNotBlank()) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            HorizontalDivider(
                color = DiaryPinkSubtle,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Body text with custom styling
            val resolvedFontFamily = when (entry.fontFamily) {
                "Handwritten" -> FontFamily.Cursive
                "Classic" -> FontFamily.Serif
                "Typewriter" -> FontFamily.Monospace
                else -> FontFamily.Default
            }

            val resolvedTextAlign = when (entry.textAlign) {
                "Center" -> TextAlign.Center
                "Right" -> TextAlign.End
                else -> TextAlign.Start
            }

            Text(
                text = entry.content,
                style = TextStyle(
                    fontFamily = resolvedFontFamily,
                    fontSize = entry.fontSizeSp.sp,
                    fontWeight = if (entry.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (entry.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textAlign = resolvedTextAlign,
                    lineHeight = (entry.fontSizeSp + 9).sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Photos Section
            if (photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Photos (${photos.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos) { photoUri ->
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Memory Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { fullScreenPhoto = photoUri }
                        )
                    }
                }
            }

            // Attached Doodle Section
            entry.drawingPath?.let { doodleUri ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Doodle",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DiaryPinkSubtle, RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .clickable { fullScreenPhoto = doodleUri }
                ) {
                    AsyncImage(
                        model = doodleUri,
                        contentDescription = "Memory Doodle",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Stickers
            if (stickers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stickers.forEach { sticker ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sticker, fontSize = 24.sp)
                        }
                    }
                }
            }

            // Tags
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DiaryPinkSubtle
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = DiaryPink,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Delete this memory?",
            description = "This memory will be permanently removed from your device.",
            onConfirm = {
                viewModel.deleteEntry(entry) {
                    viewModel.navigateBack()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Export Bottom Sheet
    if (showExportSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Export Memory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DiaryPinkSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showExportSheet = false
                            val uris = DiaryUtils.exportToImage(context, entry)
                            if (uris.isNotEmpty()) {
                                AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                    putString("format", "image")
                                    putString("source", "export_sheet")
                                    putInt("page_count", uris.size)
                                }
                                DiaryUtils.shareImages(context, uris, "Share Diary Image")
                                Toast.makeText(context, "Image created successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🖼️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "High-Quality Image (.png)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Sharp photo of this memory, ready to share",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DiaryPinkSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showExportSheet = false
                            val uri = DiaryUtils.exportToPdf(context, entry)
                            if (uri != null) {
                                AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                    putString("format", "pdf")
                                    putString("source", "export_sheet")
                                }
                                DiaryUtils.shareFile(context, uri, "application/pdf", "Share Diary PDF")
                                Toast.makeText(context, "PDF created successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📄", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "PDF Document",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Formatted personal diary page with date, mood & title",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DiaryPinkSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showExportSheet = false
                            val uri = DiaryUtils.exportToTxt(context, entry)
                            if (uri != null) {
                                AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                    putString("format", "txt")
                                    putString("source", "export_sheet")
                                }
                                DiaryUtils.shareFile(context, uri, "text/plain", "Share Diary Text")
                                Toast.makeText(context, "TXT created successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📝", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Plain Text File (.txt)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Clean readable text archive for backups",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DiaryPinkSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showExportSheet = false
                            AnalyticsManager.log(AnalyticsManager.Events.ENTRY_EXPORTED) {
                                putString("format", "text")
                                putString("source", "export_sheet")
                            }
                            DiaryUtils.shareAsText(context, entry)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Share as Text",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Sends the memory as a message, no file attached",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Full Screen Photo View
    fullScreenPhoto?.let { uri ->
        FullScreenPhotoViewer(photoUri = uri, onDismiss = { fullScreenPhoto = null })
    }
}
