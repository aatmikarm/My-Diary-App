package com.aatmik.mydiary.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.aatmik.mydiary.BuildConfig

object SupportHelper {
    private const val SUPPORT_EMAIL = "aatmikarm@gmail.com"

    fun contactSupport(context: Context) {
        val body = buildString {
            appendLine("Please describe your issue or question below:")
            appendLine()
            appendLine()
            appendLine("---")
            appendLine("Do not remove the details below, they help us help you faster:")
            appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "My Diary — Support Request")
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found on this device", Toast.LENGTH_SHORT).show()
        }
    }
}