package com.aatmik.mydiary.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.android.play.core.review.ReviewManagerFactory
import android.util.Log

object ReviewHelper {
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.aatmik.mydiary"
    private const val FOCUS_CHECK_DELAY_MS = 800L

    fun requestReview(context: Context, fallbackToPlayStore: Boolean = true) {
        val activity = context as? Activity ?: return
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
                if (fallbackToPlayStore) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (activity.hasWindowFocus()) {
                            openPlayStoreListing(activity)
                        }
                    }, FOCUS_CHECK_DELAY_MS)
                }
            } else if (fallbackToPlayStore) {
                openPlayStoreListing(activity)
            }
        }
    }

    private fun openPlayStoreListing(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)))
        } catch (e: ActivityNotFoundException) {
            // No browser available — nothing more we can do
        }
    }

    fun checkAndShowPendingReview(context: Context) {
        val prefs = context.getSharedPreferences("diary_prefs", Context.MODE_PRIVATE)
        val pending = prefs.getBoolean("review_pending", false)
        if (pending) {
            prefs.edit().putBoolean("review_pending", false).apply()
            requestReview(context, fallbackToPlayStore = false)
        }
    }
}