package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.DiaryEntry
import com.example.ui.components.AddTagDialog
import com.example.ui.components.FullScreenPhotoViewer
import com.example.ui.components.MoodSelectorRow
import com.example.ui.components.StickerPickerBottomSheet
import com.example.ui.components.UnsavedChangesDialog
import com.example.ui.theme.DiaryPink
import com.example.ui.theme.DiaryPinkSubtle
import com.example.util.DiaryUtils
import com.example.viewmodel.DiaryViewModel
import com.example.viewmodel.Screen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditEntryScreen(
    viewModel: DiaryViewModel,
    onSaveFinished: () -> Unit
) {
    val context = LocalContext.current
    val draft = viewModel.editEntryDraft ?: DiaryEntry(dateMillis = System.currentTimeMillis())

    var dateMillis by remember { mutableStateOf(draft.dateMillis) }
    var title by remember { mutableStateOf(draft.title) }
    var content by remember { mutableStateOf(draft.content) }
    var mood by remember { mutableStateOf(draft.mood) }

    val photos = remember {
        mutableStateListOf<String>().apply {
            addAll(DiaryUtils.parseJsonList(draft.photosJson))
        }
    }
    val stickers = remember {
        mutableStateListOf<String>().apply {
            addAll(DiaryUtils.parseJsonList(draft.stickersJson))
        }
    }
    val tags = remember {
        mutableStateListOf<String>().apply {
            addAll(DiaryUtils.parseJsonList(draft.tagsJson))
        }
    }

    var drawingPath by remember { mutableStateOf(viewModel.pendingDoodlePath ?: draft.drawingPath) }

    // Text Formatting Options
    var fontFamilyName by remember { mutableStateOf(draft.fontFamily) }
    var fontSizeSp by remember { mutableIntStateOf(draft.fontSizeSp) }
    var isBold by remember { mutableStateOf(draft.isBold) }
    var isItalic by remember { mutableStateOf(draft.isItalic) }
    var textAlignMode by remember { mutableStateOf(draft.textAlign) }

    // Dialog & Sheet states
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showFormatSheet by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }

    val isModified = remember(title, content, mood, photos.size, stickers.size, tags.size, drawingPath) {
        title != draft.title ||
                content != draft.content ||
                mood != draft.mood ||
                photos.toList() != DiaryUtils.parseJsonList(draft.photosJson) ||
                stickers.toList() != DiaryUtils.parseJsonList(draft.stickersJson) ||
                tags.toList() != DiaryUtils.parseJsonList(draft.tagsJson) ||
                drawingPath != draft.drawingPath
    }

    fun handleBack() {
        if (isModified) {
            showUnsavedDialog = true
        } else {
            viewModel.navigateBack()
        }
    }

    BackHandler { handleBack() }

    fun doSave() {
        val finalTitle = if (title.isBlank() && content.isNotBlank()) {
            content.lineSequence().firstOrNull()?.take(40)?.trim() ?: "Memory"
        } else {
            title.trim()
        }

        val updatedEntry = draft.copy(
            dateMillis = dateMillis,
            updatedMillis = System.currentTimeMillis(),
            title = finalTitle,
            content = content.trim(),
            mood = mood,
            photosJson = DiaryUtils.toJsonList(photos),
            stickersJson = DiaryUtils.toJsonList(stickers),
            tagsJson = DiaryUtils.toJsonList(tags),
            drawingPath = drawingPath,
            fontFamily = fontFamilyName,
            fontSizeSp = fontSizeSp,
            isBold = isBold,
            isItalic = isItalic,
            textAlign = textAlignMode
        )

        viewModel.saveEntry(updatedEntry) {
            viewModel.pendingDoodlePath = null
            onSaveFinished()
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            photos.add(uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d)
                                    }
                                    dateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = DiaryPinkSubtle,
                        modifier = Modifier.testTag("entry_date_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = DiaryPink,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DiaryUtils.formatShortDate(dateMillis),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = DiaryPink
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { handleBack() },
                        modifier = Modifier.testTag("create_entry_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { doSave() },
                        enabled = title.isNotBlank() || content.isNotBlank() || photos.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiaryPink,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_entry_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Photo
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("action_add_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            tint = DiaryPink
                        )
                    }

                    // Draw Doodle
                    IconButton(
                        onClick = {
                            viewModel.navigateTo(Screen.DRAWING)
                        },
                        modifier = Modifier.testTag("action_drawing")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "Draw",
                            tint = DiaryPink
                        )
                    }

                    // Add Sticker
                    IconButton(
                        onClick = { showStickerSheet = true },
                        modifier = Modifier.testTag("action_sticker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Add Sticker",
                            tint = DiaryPink
                        )
                    }

                    // Format Text
                    IconButton(
                        onClick = { showFormatSheet = true },
                        modifier = Modifier.testTag("action_format_text")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Text Formatting",
                            tint = DiaryPink
                        )
                    }

                    // Add Tag
                    IconButton(
                        onClick = { showTagDialog = true },
                        modifier = Modifier.testTag("action_add_tag")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Add Tag",
                            tint = DiaryPink
                        )
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
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Mood Selector
            Text(
                text = "How did you feel?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            MoodSelectorRow(
                selectedMood = mood,
                onMoodSelected = { mood = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Give your day a title...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DiaryPink,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = DiaryPinkSubtle.copy(alpha = 0.5f),
                    unfocusedContainerColor = DiaryPinkSubtle.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Attached Media & Badges Row
            if (photos.isNotEmpty()) {
                Text(
                    text = "Attached Photos (${photos.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos) { uriStr ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = uriStr,
                                contentDescription = "Photo thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { previewPhotoUri = uriStr }
                            )
                            // Remove photo button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { photos.remove(uriStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Attached Doodle Preview
            drawingPath?.let { doodleUri ->
                Text(
                    text = "Attached Drawing",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DiaryPinkSubtle, RoundedCornerShape(14.dp))
                        .background(Color.White)
                ) {
                    AsyncImage(
                        model = doodleUri,
                        contentDescription = "Doodle thumbnail",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable {
                                drawingPath = null
                                viewModel.pendingDoodlePath = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove drawing",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pinned Stickers
            if (stickers.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stickers.forEach { sticker ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DiaryPinkSubtle)
                                .clickable { stickers.remove(sticker) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sticker, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tags Row
            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DiaryPinkSubtle,
                            modifier = Modifier.clickable { tags.remove(tag) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DiaryPink,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    tint = DiaryPink,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Content multiline text area
            val resolvedFontFamily = when (fontFamilyName) {
                "Handwritten" -> FontFamily.Cursive
                "Classic" -> FontFamily.Serif
                "Typewriter" -> FontFamily.Monospace
                else -> FontFamily.Default
            }

            val resolvedTextAlign = when (textAlignMode) {
                "Center" -> TextAlign.Center
                "Right" -> TextAlign.End
                else -> TextAlign.Start
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        "Write your thoughts here...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                textStyle = TextStyle(
                    fontFamily = resolvedFontFamily,
                    fontSize = fontSizeSp.sp,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textAlign = resolvedTextAlign,
                    lineHeight = (fontSizeSp + 8).sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .testTag("entry_content_input")
            )

            // Word count
            if (viewModel.securityManager.showWordCount) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${DiaryUtils.countWords(content)} words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Unsaved Changes Dialog
    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedDialog = false
                doSave()
            },
            onDiscard = {
                showUnsavedDialog = false
                viewModel.navigateBack()
            },
            onCancel = {
                showUnsavedDialog = false
            }
        )
    }

    // Sticker Picker Sheet
    if (showStickerSheet) {
        StickerPickerBottomSheet(
            onDismiss = { showStickerSheet = false },
            onStickerSelected = { sticker ->
                if (!stickers.contains(sticker)) {
                    stickers.add(sticker)
                }
            }
        )
    }

    // Text Formatting Bottom Sheet
    if (showFormatSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showFormatSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Text Formatting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Font Family
                Text(text = "Font Style", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Default", "Handwritten", "Classic", "Typewriter").forEach { font ->
                        FilterChip(
                            selected = fontFamilyName == font,
                            onClick = { fontFamilyName = font },
                            label = { Text(font) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiaryPink,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Size
                Text(text = "Text Size", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Small" to 15, "Normal" to 17, "Large" to 21).forEach { (label, size) ->
                        FilterChip(
                            selected = fontSizeSp == size,
                            onClick = { fontSizeSp = size },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiaryPink,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bold, Italic, Alignment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = isBold,
                        onClick = { isBold = !isBold },
                        leadingIcon = { Icon(Icons.Default.FormatBold, contentDescription = null) },
                        label = { Text("Bold") }
                    )
                    FilterChip(
                        selected = isItalic,
                        onClick = { isItalic = !isItalic },
                        leadingIcon = { Icon(Icons.Default.FormatItalic, contentDescription = null) },
                        label = { Text("Italic") }
                    )
                    FilterChip(
                        selected = textAlignMode == "Center",
                        onClick = {
                            textAlignMode = if (textAlignMode == "Center") "Left" else "Center"
                        },
                        leadingIcon = { Icon(Icons.Default.FormatAlignCenter, contentDescription = null) },
                        label = { Text("Center") }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // Add Tag Dialog
    if (showTagDialog) {
        AddTagDialog(
            onAddTag = { newTag ->
                if (!tags.contains(newTag)) {
                    tags.add(newTag)
                }
            },
            onDismiss = { showTagDialog = false }
        )
    }

    // Full Screen Photo Viewer
    previewPhotoUri?.let { uri ->
        FullScreenPhotoViewer(photoUri = uri, onDismiss = { previewPhotoUri = null })
    }
}
