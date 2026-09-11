package com.aatmik.mydiary.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewHelper {
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.aatmik.mydiary"
    private const val FOCUS_CHECK_DELAY_MS = 800L

    fun requestReview(context: Context) {
        val activity = context as? Activity ?: return
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)

                // Google never tells us if the dialog actually showed or was
                // already used up for this user — so we check window focus
                // shortly after. If nothing opened on top of us, the window
                // never lost focus, meaning the flow silently no-op'd.
                Handler(Looper.getMainLooper()).postDelayed({
                    if (activity.hasWindowFocus()) {
                        openPlayStoreListing(activity)
                    }
                }, FOCUS_CHECK_DELAY_MS)
            } else {
                openPlayStoreListing(activity)
            }
        }
    }

    fun onDiaryEntrySaved(context: Context) {
        val prefs = context.getSharedPreferences("diary_prefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("entry_count", 0) + 1
        val alreadyPrompted = prefs.getBoolean("review_prompted_at_3", false)
        prefs.edit().putInt("entry_count", count).apply()

        if (count == 3 && !alreadyPrompted) {
            prefs.edit().putBoolean("review_prompted_at_3", true).apply()
            requestReview(context)
        }
    }

    private fun openPlayStoreListing(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)))
        } catch (e: ActivityNotFoundException) {
            // No browser available — extremely unlikely, nothing more we can do
        }
    }
}