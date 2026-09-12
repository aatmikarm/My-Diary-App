package com.aatmik.mydiary.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.aatmik.mydiary.data.DiaryEntry
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MoodItem(val name: String, val emoji: String)

object DiaryUtils {
    val MOODS = listOf(
        MoodItem("Loved", "😍"),
        MoodItem("Happy", "😊"),
        MoodItem("Calm", "😌"),
        MoodItem("Okay", "😐"),
        MoodItem("Sad", "😔"),
        MoodItem("Angry", "😡"),
        MoodItem("Tired", "😴")
    )

    val STICKER_CATEGORIES = mapOf(
        "Love" to listOf("❤️", "💖", "💌", "🧸", "🌹", "✨"),
        "Happy" to listOf("🎉", "🌟", "🥳", "🌈", "🍦", "🌸"),
        "Travel" to listOf("✈️", "🗺️", "🧳", "📸", "🚂", "🏖️"),
        "Food" to listOf("🥐", "☕", "🍰", "🍓", "🍵", "🍕"),
        "Nature" to listOf("🌿", "🌻", "🍂", "🌙", "☁️", "🦋"),
        "Celebration" to listOf("🎂", "🎈", "🎁", "🥂", "🕯️", "🎆"),
        "Cute" to listOf("🐱", "🐶", "🐰", "🐥", "🐼", "🎀")
    )

    val PROMPTS = listOf(
        "🌿 Gratitude",
        "☕ Warm Drinks",
        "✨ A Small Win",
        "🚶 Evening Walk",
        "📖 Quiet Book Moment"
    )

    fun getMoodEmoji(moodName: String): String {
        return MOODS.find { it.name.equals(moodName, ignoreCase = true) }?.emoji ?: "😊"
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatMonthYear(calendar: Calendar): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun parseJsonList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {
            json.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { list.add(it) }
        }
        return list
    }

    fun toJsonList(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    fun getShareableDrawableUri(context: Context, drawableResId: Int): Uri {
        val cacheFile = File(context.cacheDir, "share_promo.png")
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, drawableResId)
        FileOutputStream(cacheFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
    }
    fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val destFile = File(context.filesDir, "photo_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun countWords(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split("\\s+".toRegex()).size
    }

    fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Export single entry to TXT
    fun exportToTxt(context: Context, entry: DiaryEntry): Uri? {
        val fileName = "Diary_${formatShortDate(entry.dateMillis).replace(" ", "_")}.txt"
        val exportFile = File(context.cacheDir, fileName)
        val text = buildString {
            appendLine("=== MY DIARY ===")
            appendLine("Date: ${formatDate(entry.dateMillis)} at ${formatTime(entry.createdMillis)}")
            appendLine("Mood: ${getMoodEmoji(entry.mood)} ${entry.mood}")
            if (entry.title.isNotBlank()) {
                appendLine("Title: ${entry.title}")
            }
            val tags = parseJsonList(entry.tagsJson)
            if (tags.isNotEmpty()) {
                appendLine("Tags: ${tags.joinToString(" ")}")
            }
            appendLine("----------------------------------------")
            appendLine(entry.content)
            appendLine("----------------------------------------")
            val stickers = parseJsonList(entry.stickersJson)
            if (stickers.isNotEmpty()) {
                appendLine("Stickers: ${stickers.joinToString(" ")}")
            }
            appendLine("Saved with My Diary (Private & Offline)")
        }
        exportFile.writeText(text)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
    }

    // Export all entries to TXT
    fun exportAllToTxt(context: Context, entries: List<DiaryEntry>): Uri? {
        val fileName = "My_Diary_Full_Backup.txt"
        val exportFile = File(context.cacheDir, fileName)
        val text = buildString {
            appendLine("========================================")
            appendLine("           MY DIARY ARCHIVE")
            appendLine("  Exported on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
            appendLine("  Total Memories: ${entries.size}")
            appendLine("========================================")
            appendLine()
            entries.sortedBy { it.dateMillis }.forEach { entry ->
                appendLine("Date: ${formatDate(entry.dateMillis)} at ${formatTime(entry.createdMillis)}")
                appendLine("Mood: ${getMoodEmoji(entry.mood)} ${entry.mood}")
                if (entry.title.isNotBlank()) {
                    appendLine("Title: ${entry.title}")
                }
                val tags = parseJsonList(entry.tagsJson)
                if (tags.isNotEmpty()) {
                    appendLine("Tags: ${tags.joinToString(" ")}")
                }
                appendLine()
                appendLine(entry.content)
                appendLine()
                appendLine("----------------------------------------")
            }
        }
        exportFile.writeText(text)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
    }

    // Export single entry to PDF — paginates automatically so long entries
    // (thousands of words) don't get silently truncated to one page.
    fun exportToPdf(context: Context, entry: DiaryEntry): Uri? {
        val fileName = "Diary_${formatShortDate(entry.dateMillis).replace(" ", "_")}.pdf"
        val exportFile = File(context.cacheDir, fileName)

        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 50f
        val marginRight = 545f
        val contentWidth = marginRight - marginLeft
        val bottomLimit = 790f

        val titlePaint = Paint().apply {
            color = android.graphics.Color.rgb(255, 77, 128)
            textSize = 22f; isFakeBoldText = true; isAntiAlias = true
        }
        val metaPaint = Paint().apply {
            color = android.graphics.Color.rgb(102, 92, 95)
            textSize = 12f; isAntiAlias = true
        }
        val entryTitlePaint = Paint().apply {
            color = android.graphics.Color.rgb(27, 27, 33)
            textSize = 18f; isFakeBoldText = true; isAntiAlias = true
        }
        val tagPaint = Paint().apply {
            color = android.graphics.Color.rgb(232, 59, 108)
            textSize = 12f; isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(27, 27, 33)
            textSize = 14f; isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            color = android.graphics.Color.rgb(158, 149, 152)
            textSize = 10f; isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.rgb(240, 228, 232)
            strokeWidth = 1.5f
        }

        // Pre-wrap the whole body once so we know exactly how many lines exist,
        // and can split them across as many pages as needed.
        val wrappedLines = mutableListOf<String>()
        entry.content.split("\n").forEach { line ->
            if (line.isEmpty()) {
                wrappedLines.add("")
            } else {
                var start = 0
                while (start < line.length) {
                    val count = bodyPaint.breakText(line, start, line.length, true, contentWidth, null)
                    wrappedLines.add(line.substring(start, start + count))
                    start += count
                }
            }
        }

        val tags = parseJsonList(entry.tagsJson)
        val pdfDocument = PdfDocument()
        var lineIndex = 0
        var pageNumber = 1

        while (lineIndex < wrappedLines.size || pageNumber == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            var yPos = 50f
            val isFirstPage = pageNumber == 1

            if (isFirstPage) {
                canvas.drawText("My Diary", marginLeft, yPos, titlePaint)
                yPos += 24f
                canvas.drawText(
                    "${formatDate(entry.dateMillis)} • ${formatTime(entry.createdMillis)} • Mood: ${getMoodEmoji(entry.mood)} ${entry.mood}",
                    marginLeft, yPos, metaPaint
                )
                yPos += 30f
                if (entry.title.isNotBlank()) {
                    canvas.drawText(entry.title, marginLeft, yPos, entryTitlePaint)
                    yPos += 24f
                }
                if (tags.isNotEmpty()) {
                    canvas.drawText(tags.joinToString("  "), marginLeft, yPos, tagPaint)
                    yPos += 24f
                }
                canvas.drawLine(marginLeft, yPos, marginRight, yPos, linePaint)
                yPos += 25f
            } else {
                canvas.drawText(
                    "${formatShortDate(entry.dateMillis)} (continued)",
                    marginLeft, yPos, metaPaint
                )
                yPos += 30f
            }

            // Fill this page with as many lines as fit before the bottom limit
            while (lineIndex < wrappedLines.size && yPos <= bottomLimit) {
                canvas.drawText(wrappedLines[lineIndex], marginLeft, yPos, bodyPaint)
                yPos += 20f
                lineIndex++
            }

            val isLastPage = lineIndex >= wrappedLines.size
            if (isLastPage) {
                canvas.drawText(
                    "Saved privately and encrypted on device • My Diary",
                    marginLeft, 810f, footerPaint
                )
            }
            canvas.drawText("Page $pageNumber", marginRight - 60f, 810f, footerPaint)

            pdfDocument.finishPage(page)
            pageNumber++

            if (isLastPage) break
        }

        FileOutputStream(exportFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
    }

    // Export single entry to one or more high-resolution images (for WhatsApp etc.)
    // Long entries are split across multiple images instead of one giant bitmap,
    // which would otherwise blow past Canvas size limits / OOM on long entries.
    fun exportToImage(context: Context, entry: DiaryEntry): List<Uri> {
        val scale = 3f // renders at 3x so pinch-zoom on WhatsApp stays sharp
        val width = (360 * scale).toInt()
        val paddingH = 20f * scale
        val contentWidth = width - (paddingH * 2)
        val lineHeight = 24f * scale
        val linesPerPage = 30 // keeps each bitmap well under Canvas/memory limits

        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(27, 27, 33)
            textSize = 16f * scale; isAntiAlias = true
        }

        val wrappedLines = mutableListOf<String>()
        entry.content.split("\n").forEach { line ->
            if (line.isEmpty()) {
                wrappedLines.add("")
            } else {
                var start = 0
                while (start < line.length) {
                    val count = bodyPaint.breakText(line, start, line.length, true, contentWidth, null)
                    wrappedLines.add(line.substring(start, start + count))
                    start += count
                }
            }
        }

        val pages = wrappedLines.chunked(linesPerPage).ifEmpty { listOf(emptyList()) }
        val tags = parseJsonList(entry.tagsJson)
        val uris = mutableListOf<Uri>()

        pages.forEachIndexed { index, pageLines ->
            val isFirstPage = index == 0
            val isLastPage = index == pages.lastIndex

            var height = (50 * scale).toInt() + (34 * scale).toInt()
            if (isFirstPage) {
                if (entry.title.isNotBlank()) height += (30 * scale).toInt()
                if (tags.isNotEmpty()) height += (30 * scale).toInt()
                height += (30 * scale).toInt()
            }
            height += (pageLines.size * lineHeight).toInt()
            height += if (isLastPage) (60 * scale).toInt() else (20 * scale).toInt()
            if (pages.size > 1) height += (30 * scale).toInt()

            val bitmap = Bitmap.createBitmap(
                width,
                height.coerceAtLeast((300 * scale).toInt()),
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)

            val titlePaint = Paint().apply {
                color = android.graphics.Color.rgb(255, 77, 128)
                textSize = 26f * scale; isFakeBoldText = true; isAntiAlias = true
            }
            val metaPaint = Paint().apply {
                color = android.graphics.Color.rgb(102, 92, 95)
                textSize = 14f * scale; isAntiAlias = true
            }
            val entryTitlePaint = Paint().apply {
                color = android.graphics.Color.rgb(27, 27, 33)
                textSize = 20f * scale; isFakeBoldText = true; isAntiAlias = true
            }
            val tagPaint = Paint().apply {
                color = android.graphics.Color.rgb(232, 59, 108)
                textSize = 14f * scale; isAntiAlias = true
            }
            val footerPaint = Paint().apply {
                color = android.graphics.Color.rgb(158, 149, 152)
                textSize = 11f * scale; isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = android.graphics.Color.rgb(240, 228, 232)
                strokeWidth = 1.5f * scale
            }

            var yPos = 50f * scale
            if (isFirstPage) {
                canvas.drawText("My Diary", paddingH, yPos, titlePaint)
                yPos += 32f * scale
                canvas.drawText(
                    "${formatDate(entry.dateMillis)} • ${formatTime(entry.createdMillis)} • ${getMoodEmoji(entry.mood)} ${entry.mood}",
                    paddingH, yPos, metaPaint
                )
                yPos += 34f * scale
                if (entry.title.isNotBlank()) {
                    canvas.drawText(entry.title, paddingH, yPos, entryTitlePaint)
                    yPos += 30f * scale
                }
                if (tags.isNotEmpty()) {
                    canvas.drawText(tags.joinToString("  "), paddingH, yPos, tagPaint)
                    yPos += 30f * scale
                }
                canvas.drawLine(paddingH, yPos, width - paddingH, yPos, linePaint)
                yPos += 30f * scale
            } else {
                canvas.drawText("${formatShortDate(entry.dateMillis)} (continued)", paddingH, yPos, metaPaint)
                yPos += 34f * scale
            }

            pageLines.forEach { line ->
                canvas.drawText(line, paddingH, yPos, bodyPaint)
                yPos += lineHeight
            }

            if (isLastPage) {
                yPos += 20f * scale
                canvas.drawText("Saved privately and encrypted on device • My Diary", paddingH, yPos, footerPaint)
            }
            if (pages.size > 1) {
                yPos += 26f * scale
                canvas.drawText("Page ${index + 1} of ${pages.size}", paddingH, yPos, footerPaint)
            }

            val fileName = "Diary_${formatShortDate(entry.dateMillis).replace(" ", "_")}_p${index + 1}.png"
            val exportFile = File(context.cacheDir, fileName)
            FileOutputStream(exportFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            uris.add(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", exportFile))
        }

        return uris
    }

    // Shares the entry as plain text directly — no file created, so it lands
    // as an actual message (not an attachment) in WhatsApp/SMS/etc.
    fun shareAsText(context: Context, entry: DiaryEntry) {
        val text = buildString {
            if (entry.title.isNotBlank()) {
                appendLine(entry.title)
                appendLine()
            }
            append(entry.content)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share as Text"))
    }
    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    // Shares one or more images — uses ACTION_SEND for a single image (matches
    // shareFile behavior) and ACTION_SEND_MULTIPLE when an entry was paginated.
    fun shareImages(context: Context, uris: List<Uri>, title: String) {
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/png"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}