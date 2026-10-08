package com.example.roadmaputepsa
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

fun autenticarConBiometria(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)

    val biometricManager = BiometricManager.from(activity)

    // Android 10 y anteriores no admiten STRONG | DEVICE_CREDENTIAL.
    val biometria = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.BIOMETRIC_STRONG
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    }
    val autenticadores = biometria or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val resultado = biometricManager.canAuthenticate(autenticadores)

    if (
        resultado != BiometricManager.BIOMETRIC_SUCCESS
    ) {
        onError("Configura una huella, un PIN, patrón o contraseña en los ajustes del teléfono, o inicia sesión con Google.")
        return
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación")
        .setSubtitle("Usa tu biometría, PIN, patrón o contraseña del teléfono")
        .setAllowedAuthenticators(autenticadores)
        .build()

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)

                onSuccess()
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(
                    errorCode,
                    errString
                )

                onError(errString.toString())
            }
        }
    )

    biometricPrompt.authenticate(promptInfo)
}