package com.example.roadmaputepsa

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Protege el acceso con un autenticador del sistema.
 * Nunca interpreta la ausencia de PIN o biometría como acceso autorizado.
 */
fun autenticarConBiometria(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val autenticadores = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            // En Android 10 y anteriores no se admite STRONG | DEVICE_CREDENTIAL.
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val estado = BiometricManager.from(activity).canAuthenticate(autenticadores)
        if (estado != BiometricManager.BIOMETRIC_SUCCESS) {
            onError(
                "El dispositivo no tiene un método compatible configurado. " +
                    "Configura PIN, patrón, contraseña o biometría en Ajustes."
            )
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verificación del dispositivo")
            .setSubtitle("Usa tu huella, rostro, PIN, patrón o contraseña")
            .setAllowedAuthenticators(autenticadores)
            .build()

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )
        prompt.authenticate(promptInfo)
    } catch (e: Exception) {
        // Incluye fallos en PromptInfo.build() además de authenticate().
        onError(e.localizedMessage ?: "No se pudo abrir la verificación del dispositivo.")
    }
}
