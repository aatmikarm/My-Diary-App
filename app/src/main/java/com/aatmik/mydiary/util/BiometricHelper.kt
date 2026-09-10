package com.aatmik.mydiary.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps the system biometric prompt. Never checks for a specific sensor type
 * (in-display fingerprint, side-button, face unlock, etc.) — Android's
 * BiometricManager abstracts that away entirely. We only ask "can this
 * device authenticate at all" and let the OS render whatever prompt fits
 * the hardware it's running on.
 */
object BiometricHelper {

    // Covers both Class 3 (strong) and Class 2 (weak) sensors — i.e. every
    // real fingerprint/face sensor Android recognizes, regardless of location.
    private const val AUTHENTICATORS = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    /**
     * True only if the device has working biometric hardware AND the user
     * has actually enrolled a fingerprint/face. False for devices with no
     * sensor, disabled hardware, or nothing enrolled — use this to hide the
     * option entirely rather than offering something that will always fail.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock My Diary",
        subtitle: String = "Use your fingerprint or face to continue",
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onFailed()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN instead")
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}