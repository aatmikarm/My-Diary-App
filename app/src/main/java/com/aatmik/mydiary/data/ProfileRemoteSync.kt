package com.aatmik.mydiary.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * One-time push of the full onboarding profile to Firestore — one document per
 * install, keyed by [ProfileManager.getOrCreateInstallId].
 *
 * This is a deliberate alternative to sending this data through Firebase Analytics:
 * Analytics event params are capped (~25 per event, ~100-char string values) and the
 * reports are built for aggregate/funnel analysis, not for storing a clean per-user
 * record you'd want to read back out. Firestore gives one real document per user
 * with real field types, an authoritative server timestamp, and nothing to register
 * as a "custom dimension."
 *
 * COST: at one write per install (this only ever runs once, gated by
 * ProfileManager.isProfileSetupDone), this stays comfortably inside the Firestore
 * Spark (free) plan's daily quota (50K reads / 20K writes / 20K deletes per day,
 * 1 GiB stored) unless you're seeing tens of thousands of installs completing
 * onboarding on the same day. Worth a glance at Firebase Console → Usage if install
 * volume ever grows a lot, but this is not something to worry about at current scale.
 *
 * SECURITY: since this app has no login, Firestore rules for this collection must
 * allow unauthenticated writes — make sure Firebase App Check enforcement is turned
 * ON for Firestore in the Console (App Check → Firestore → Enforce). This project
 * already ships the App Check SDK for other services; without enforcing it here too,
 * the write endpoint is open to anyone who extracts the API key, not just your app.
 */
object ProfileRemoteSync {
    private const val TAG = "ProfileRemoteSync"
    private const val COLLECTION = "user_profiles"

    fun pushProfile(
        installId: String,
        name: String,
        age: Int?,
        birthdayYear: Int?,
        birthdayMonth: Int?,
        birthdayDay: Int?,
        gender: String,
        goals: Set<String>
    ) {
        val now = Date()
        val humanReadable = SimpleDateFormat("EEE, d MMM yyyy 'at' h:mm a", Locale.getDefault())
            .format(now)

        val data = hashMapOf<String, Any>(
            "name" to name,
            "gender" to gender,
            "goals" to goals.toList(),
            "submittedAtHuman" to humanReadable,
            // Authoritative server-side clock — trust this over submittedAtHuman,
            // which uses the device's local clock and can be wrong/spoofed.
            "submittedAtServer" to FieldValue.serverTimestamp()
        )
        age?.let { data["age"] = it }
        birthdayYear?.let { data["birthdayYear"] = it }
        birthdayMonth?.let { data["birthdayMonth"] = it }
        birthdayDay?.let { data["birthdayDay"] = it }
        if (birthdayYear != null && birthdayMonth != null && birthdayDay != null) {
            data["birthday"] = "%04d-%02d-%02d".format(birthdayYear, birthdayMonth, birthdayDay)
        }

        FirebaseFirestore.getInstance()
            .collection(COLLECTION)
            .document(installId)
            .set(data)
            .addOnSuccessListener { Log.d(TAG, "Profile synced for install $installId") }
            .addOnFailureListener { e -> Log.e(TAG, "Profile sync failed for install $installId", e) }
    }
}