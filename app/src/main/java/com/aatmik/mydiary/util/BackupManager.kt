package com.aatmik.mydiary.util

import android.content.Context
import android.net.Uri
import com.aatmik.mydiary.data.DiaryDatabase
import com.aatmik.mydiary.data.DiaryEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Creates and restores a full, self-contained backup of every diary entry —
 * including the actual photo and doodle files, not just the database rows.
 * The zip this produces is the only thing that needs to survive an
 * uninstall for a user's diary to come back exactly as it was.
 */
object BackupManager {

    private const val BACKUP_VERSION = 1
    private const val MANIFEST_NAME = "backup.json"
    private const val MEDIA_DIR = "media"

    data class RestoreResult(val entriesRestored: Int, val mediaRestored: Int)

    /**
     * Builds the backup zip in the app's cache dir and returns it.
     * Caller writes it to its final destination (SAF "save as", share
     * sheet, etc.) and should delete the returned file afterwards.
     */
    suspend fun createBackupFile(context: Context, entries: List<DiaryEntry>): File {
        val workDir = File(context.cacheDir, "backup_work").apply {
            deleteRecursively()
            mkdirs()
        }
        val mediaDir = File(workDir, MEDIA_DIR).apply { mkdirs() }
        val entriesJson = JSONArray()

        entries.forEach { entry ->
            val originalPhotos = DiaryUtils.parseJsonList(entry.photosJson)
            val backedUpPhotoNames = JSONArray()

            originalPhotos.forEachIndexed { index, photoPath ->
                val mediaFileName = "e${entry.id}_photo${index}_${UUID.randomUUID().toString().take(6)}.jpg"
                if (copyIntoBackup(context, photoPath, File(mediaDir, mediaFileName))) {
                    backedUpPhotoNames.put(mediaFileName)
                }
            }

            var backedUpDoodleName: String? = null
            entry.drawingPath?.let { doodlePath ->
                val mediaFileName = "e${entry.id}_doodle_${UUID.randomUUID().toString().take(6)}.png"
                if (copyIntoBackup(context, doodlePath, File(mediaDir, mediaFileName))) {
                    backedUpDoodleName = mediaFileName
                }
            }

            val entryJson = JSONObject().apply {
                put("dateMillis", entry.dateMillis)
                put("createdMillis", entry.createdMillis)
                put("updatedMillis", entry.updatedMillis)
                put("title", entry.title)
                put("content", entry.content)
                put("mood", entry.mood)
                put("photos", backedUpPhotoNames)
                put("stickersJson", entry.stickersJson)
                put("tagsJson", entry.tagsJson)
                put("doodle", backedUpDoodleName) // omitted from JSON entirely when null
                put("fontFamily", entry.fontFamily)
                put("fontSizeSp", entry.fontSizeSp)
                put("isBold", entry.isBold)
                put("isItalic", entry.isItalic)
                put("textAlign", entry.textAlign)
                put("isFavorite", entry.isFavorite)
                put("location", entry.location)
                put("weather", entry.weather)
            }
            entriesJson.put(entryJson)
        }

        val manifest = JSONObject().apply {
            put("backupVersion", BACKUP_VERSION)
            put("exportedAtMillis", System.currentTimeMillis())
            put("entryCount", entries.size)
            put("entries", entriesJson)
        }
        File(workDir, MANIFEST_NAME).writeText(manifest.toString())

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val zipFile = File(context.cacheDir, "MyDiary_Backup_$timestamp.zip")
        zipDirectory(workDir, zipFile)
        workDir.deleteRecursively()

        return zipFile
    }

    /** Copies a photo/doodle from wherever it lives now (content:// URI or a plain internal file path) into the backup's media folder. */
    private fun copyIntoBackup(context: Context, sourcePath: String, destFile: File): Boolean {
        return try {
            if (sourcePath.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(sourcePath))?.use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                } ?: return false
            } else {
                val sourceFile = File(sourcePath)
                if (!sourceFile.exists()) return false
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun zipDirectory(sourceDir: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            sourceDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(sourceDir).path
                zos.putNextEntry(ZipEntry(entryName))
                FileInputStream(file).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }

    /**
     * Reads a backup zip from any Uri the user picked (SAF, Downloads, a
     * cloud file, etc.), restores every entry as a brand-new row, and
     * copies every photo/doodle back into the app's private storage.
     * Never deletes or overwrites existing data — safe to run any time.
     */
    suspend fun restoreFromZip(context: Context, zipUri: Uri): RestoreResult {
        val workDir = File(context.cacheDir, "restore_work").apply {
            deleteRecursively()
            mkdirs()
        }

        context.contentResolver.openInputStream(zipUri)?.use { input ->
            unzipInto(input, workDir)
        } ?: throw IllegalStateException("Could not open the selected backup file")

        val manifestFile = File(workDir, MANIFEST_NAME)
        if (!manifestFile.exists()) {
            workDir.deleteRecursively()
            throw IllegalStateException("This doesn't look like a My Diary backup file")
        }

        val manifest = JSONObject(manifestFile.readText())
        val entriesJson = manifest.getJSONArray("entries")
        val mediaDir = File(workDir, MEDIA_DIR)
        val dao = DiaryDatabase.getDatabase(context).diaryDao()
        var mediaRestored = 0

        for (i in 0 until entriesJson.length()) {
            val obj = entriesJson.getJSONObject(i)

            val restoredPhotoPaths = mutableListOf<String>()
            val photoNames = obj.optJSONArray("photos") ?: JSONArray()
            for (p in 0 until photoNames.length()) {
                restoreMediaFile(context, mediaDir, photoNames.getString(p), "photo")?.let {
                    restoredPhotoPaths.add(it)
                    mediaRestored++
                }
            }

            val doodleName: String? = if (obj.isNull("doodle")) null else obj.optString("doodle")
            val restoredDoodlePath = doodleName?.let {
                restoreMediaFile(context, mediaDir, it, "doodle")?.also { mediaRestored++ }
            }

            val entry = DiaryEntry(
                id = 0, // always insert fresh — never collides with anything already on the device
                dateMillis = obj.getLong("dateMillis"),
                createdMillis = obj.optLong("createdMillis", System.currentTimeMillis()),
                updatedMillis = obj.optLong("updatedMillis", System.currentTimeMillis()),
                title = obj.optString("title", ""),
                content = obj.optString("content", ""),
                mood = obj.optString("mood", "Happy"),
                photosJson = DiaryUtils.toJsonList(restoredPhotoPaths),
                stickersJson = obj.optString("stickersJson", "[]"),
                tagsJson = obj.optString("tagsJson", "[]"),
                drawingPath = restoredDoodlePath,
                fontFamily = obj.optString("fontFamily", "Default"),
                fontSizeSp = obj.optInt("fontSizeSp", 17),
                isBold = obj.optBoolean("isBold", false),
                isItalic = obj.optBoolean("isItalic", false),
                textAlign = obj.optString("textAlign", "Left"),
                isFavorite = obj.optBoolean("isFavorite", false),
                location = obj.optString("location", ""),
                weather = obj.optString("weather", "")
            )
            dao.insertEntry(entry)
        }

        workDir.deleteRecursively()
        return RestoreResult(entriesRestored = entriesJson.length(), mediaRestored = mediaRestored)
    }

    private fun restoreMediaFile(context: Context, mediaDir: File, storedName: String, prefix: String): String? {
        val source = File(mediaDir, storedName)
        if (!source.exists()) return null
        val ext = if (prefix == "doodle") "png" else "jpg"
        val destFile = File(context.filesDir, "${prefix}_restored_${UUID.randomUUID()}.$ext")
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun unzipInto(input: InputStream, targetDir: File) {
        ZipInputStream(input).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                outFile.parentFile?.mkdirs()
                if (!entry.isDirectory) {
                    FileOutputStream(outFile).use { output -> zis.copyTo(output) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}