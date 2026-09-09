package com.aatmik.mydiary.util

import android.content.Context
import android.content.Intent
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
            // Fallback for simple comma delimited
            json.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { list.add(it) }
        }
        return list
    }

    fun toJsonList(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
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

    // Export single entry to PDF
    fun exportToPdf(context: Context, entry: DiaryEntry): Uri? {
        val fileName = "Diary_${formatShortDate(entry.dateMillis).replace(" ", "_")}.pdf"
        val exportFile = File(context.cacheDir, fileName)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = android.graphics.Color.rgb(255, 77, 128) // Pink accent
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = android.graphics.Color.rgb(102, 92, 95) // Gray
            textSize = 12f
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = android.graphics.Color.rgb(27, 27, 33) // Dark charcoal
            textSize = 14f
            isAntiAlias = true
        }

        var yPos = 50f
        canvas.drawText("My Diary", 50f, yPos, titlePaint)
        yPos += 24f

        canvas.drawText(
            "${formatDate(entry.dateMillis)} • ${formatTime(entry.createdMillis)} • Mood: ${getMoodEmoji(entry.mood)} ${entry.mood}",
            50f,
            yPos,
            metaPaint
        )
        yPos += 30f

        if (entry.title.isNotBlank()) {
            val entryTitlePaint = Paint().apply {
                color = android.graphics.Color.rgb(27, 27, 33)
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(entry.title, 50f, yPos, entryTitlePaint)
            yPos += 24f
        }

        val tags = parseJsonList(entry.tagsJson)
        if (tags.isNotEmpty()) {
            val tagPaint = Paint().apply {
                color = android.graphics.Color.rgb(232, 59, 108)
                textSize = 12f
                isAntiAlias = true
            }
            canvas.drawText(tags.joinToString("  "), 50f, yPos, tagPaint)
            yPos += 24f
        }

        // Divider line
        val linePaint = Paint().apply {
            color = android.graphics.Color.rgb(240, 228, 232)
            strokeWidth = 1.5f
        }
        canvas.drawLine(50f, yPos, 545f, yPos, linePaint)
        yPos += 25f

        // Multiline body
        val lines = entry.content.split("\n")
        for (line in lines) {
            // Simple line wrap
            var start = 0
            while (start < line.length) {
                val count = bodyPaint.breakText(line, start, line.length, true, 495f, null)
                val chunk = line.substring(start, start + count)
                canvas.drawText(chunk, 50f, yPos, bodyPaint)
                yPos += 20f
                start += count
                if (yPos > 790f) break
            }
            if (yPos > 790f) break
        }

        // Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.rgb(158, 149, 152)
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("Saved privately and encrypted on device • My Diary", 50f, 810f, footerPaint)

        pdfDocument.finishPage(page)
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

    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
