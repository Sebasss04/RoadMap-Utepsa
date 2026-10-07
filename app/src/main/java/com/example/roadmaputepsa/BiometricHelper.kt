package com.example.roadmaputepsa
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

    val resultado = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG
    )

    if (
        resultado != BiometricManager.BIOMETRIC_SUCCESS
    ) {
        onError("El dispositivo no tiene una autenticación disponible.")
        return
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación")
        .setSubtitle("Confirma tu identidad")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        .setNegativeButtonText("Cancelar")
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