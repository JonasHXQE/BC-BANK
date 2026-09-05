package com.example.ui.util

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthManager {

    private const val TAG = "BiometricAuthManager"

    /**
     * Checks if the user has any secure lock method configured on their device
     * (biometrics like fingerprint/face, or device credentials like PIN, pattern, or password).
     */
    fun isDeviceSecurityConfigured(context: Context): Boolean {
        try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (keyguardManager?.isDeviceSecure == true) {
                return true
            }

            val biometricManager = BiometricManager.from(context)

            val canBioStrongOrCred = biometricManager.canAuthenticate(
                Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL
            )
            if (canBioStrongOrCred == BiometricManager.BIOMETRIC_SUCCESS) {
                return true
            }

            val canBioWeakOrCred = biometricManager.canAuthenticate(
                Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
            )
            if (canBioWeakOrCred == BiometricManager.BIOMETRIC_SUCCESS) {
                return true
            }

            val canBioStrong = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)
            if (canBioStrong == BiometricManager.BIOMETRIC_SUCCESS) {
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking device security: ${e.message}")
        }
        return false
    }

    /**
     * Checks if biometric sensors (fingerprint, face, iris) specifically are enrolled and ready.
     */
    fun hasBiometricsEnrolled(context: Context): Boolean {
        try {
            val biometricManager = BiometricManager.from(context)
            val strong = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)
            val weak = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_WEAK)
            return strong == BiometricManager.BIOMETRIC_SUCCESS || weak == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            Log.w(TAG, "Error checking biometric enrollment: ${e.message}")
            return false
        }
    }

    /**
     * Human-readable description of the security options available on this phone.
     */
    fun getSecurityDescription(context: Context): String {
        return when {
            hasBiometricsEnrolled(context) ->
                "Huella dactilar, Face ID o bloqueo de pantalla configurado"
            isDeviceSecurityConfigured(context) ->
                "Bloqueo seguro del dispositivo (PIN / Patrón de pantalla)"
            else ->
                "Sin método de bloqueo configurado en este teléfono"
        }
    }

    /**
     * Opens system settings so the user can configure a screen lock or fingerprint.
     */
    fun openDeviceSecuritySettings(context: Context) {
        val intentsToTry = listOf(
            Intent(Settings.ACTION_SECURITY_SETTINGS),
            Intent(Settings.ACTION_BIOMETRIC_ENROLL),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intentsToTry) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next intent
            }
        }
    }

    /**
     * Triggers the biometric or device lock prompt using standard AndroidX BiometricPrompt.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Desbloqueo de BC-BANK",
        subtitle: String = "Usa tu huella, Face ID o bloqueo de tu teléfono",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        if (!isDeviceSecurityConfigured(activity)) {
            onError(
                BiometricPrompt.ERROR_NO_BIOMETRICS,
                "No hay un método de bloqueo configurado en este dispositivo."
            )
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Biometric/Device authentication succeeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.w(TAG, "Biometric error [$errorCode]: $errString")
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(TAG, "Biometric authentication attempt failed")
                onFailed()
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        // Attempt primary: Biometrics + Device Credential (PIN, pattern, password)
        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            prompt.authenticate(promptInfo)
            return
        } catch (e: Exception) {
            Log.w(TAG, "Primary prompt failed (${e.message}), attempting fallback")
        }

        // Fallback 1: DEVICE_CREDENTIAL only (if supported by OS)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            try {
                @Suppress("DEPRECATION")
                val fallbackPromptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDeviceCredentialAllowed(true)
                    .build()
                prompt.authenticate(fallbackPromptInfo)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Fallback 1 failed: ${e.message}")
            }
        }

        // Fallback 2: Biometric strong only with negative button
        try {
            val fallbackPromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Usar PIN de la app")
                .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK)
                .build()
            prompt.authenticate(fallbackPromptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "All biometric prompt attempts failed: ${e.message}")
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, e.message ?: "No fue posible iniciar el sensor biométrico.")
        }
    }
}
