package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val createdMillis: Long = System.currentTimeMillis(),
    val updatedMillis: Long = System.currentTimeMillis(),
    val title: String = "",
    val content: String = "",
    val mood: String = "Happy",
    val photosJson: String = "[]", // JSON array of string URIs or paths
    val stickersJson: String = "[]", // JSON array of sticker names/emojis
    val tagsJson: String = "[]", // JSON array of tag strings e.g. ["#Peaceful", "#Walk"]
    val drawingPath: String? = null, // Local internal storage path for doodle image
    val fontFamily: String = "Default", // "Default", "Handwritten", "Classic", "Typewriter"
    val fontSizeSp: Int = 17,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val textAlign: String = "Left", // "Left", "Center", "Right"
    val isFavorite: Boolean = false,
    val location: String = "",
    val weather: String = ""
)
