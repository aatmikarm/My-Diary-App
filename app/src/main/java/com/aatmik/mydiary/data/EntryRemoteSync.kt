package com.aatmik.mydiary.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Pushes full diary entry content (title, text, mood, tags, stickers, favorite,
 * location, weather, dates) to Firestore, nested under the SAME per-install
 * document used for the onboarding profile:
 *
 *   user_profiles/{installId}/entries/{entryId}
 *
 * entry.id (Room's Long primary key) is used as the Firestore document ID, so
 * saving an edit to an existing entry overwrites the same remote document
 * instead of creating a duplicate — mirrors how Room already treats entry.id
 * as the source of truth for insert-vs-update.
 *
 * NOTE: photosJson / drawingPath hold local on-device file paths only — those
 * paths mean nothing outside this device, so this does NOT copy actual photo
 * or drawing image bytes anywhere, and none of that is included below. Syncing
 * the images themselves would need Firebase Storage uploads, which is a
 * separate, materially bigger piece of work than this text/metadata sync —
 * not something to assume is covered by this.
 */
object EntryRemoteSync {
    private const val TAG = "EntryRemoteSync"
    private const val ROOT_COLLECTION = "user_profiles"
    private const val ENTRIES_SUBCOLLECTION = "entries"

    fun pushEntry(installId: String, entry: DiaryEntry) {
        val data = hashMapOf<String, Any>(
            "title" to entry.title,
            "content" to entry.content,
            "mood" to entry.mood,
            "tagsJson" to entry.tagsJson,
            "stickersJson" to entry.stickersJson,
            "isFavorite" to entry.isFavorite,
            "location" to entry.location,
            "weather" to entry.weather,
            "dateMillis" to entry.dateMillis,
            "createdMillis" to entry.createdMillis,
            "updatedMillis" to entry.updatedMillis,
            "syncedAtServer" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection(ROOT_COLLECTION)
            .document(installId)
            .collection(ENTRIES_SUBCOLLECTION)
            .document(entry.id.toString())
            .set(data, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Entry ${entry.id} synced for install $installId") }
            .addOnFailureListener { e -> Log.e(TAG, "Entry ${entry.id} sync failed for install $installId", e) }
    }
}