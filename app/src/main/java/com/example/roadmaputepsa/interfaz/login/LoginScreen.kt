package com.example.roadmaputepsa.interfaz.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.roadmaputepsa.autenticarConBiometria
import com.example.roadmaputepsa.services.GoogleAuthHelper
import com.example.roadmaputepsa.services.PersonaService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    sesionDisponible: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val googleAuthHelper = remember(context) {
        GoogleAuthHelper(context)
    }

    val personaService = remember {
        PersonaService()
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "RoadMap UTEPSA",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(40.dp))

        Text(
            "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        Button(
            enabled = !cargando,
            onClick = {
                cargando = true
                error = null

                scope.launch {
                    try {
                        googleAuthHelper.clearCredentialState()

                        googleAuthHelper
                            .signInWithGoogle()
                            .onSuccess {

                                personaService
                                    .registrarPersonaDesdeGoogle()
                                    .onFailure { firestoreError ->
                                        println(
                                            "Firestore: ${firestoreError.message}"
                                        )
                                    }

                                onLoginSuccess()
                            }
                            .onFailure {
                                error =
                                    it.localizedMessage
                                        ?: "No se pudo iniciar sesión"
                            }

                    } catch (e: CancellationException) {
                        throw e

                    } catch (e: Exception) {
                        error =
                            e.localizedMessage
                                ?: "No se pudo iniciar sesión"

                    } finally {
                        cargando = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            enabled = sesionDisponible && !cargando,
            onClick = {

                val activity =
                    context as? FragmentActivity

                if (activity == null) {

                    error =
                        "No se puede abrir la autenticación biométrica"

                } else {

                    cargando = true
                    error = null

                    autenticarConBiometria(
                        activity = activity,

                        onSuccess = {
                            cargando = false
                            onLoginSuccess()
                        },

                        onError = {
                            cargando = false
                            error = it
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Desbloquear con biometría o PIN"
            )
        }

        if (!sesionDisponible) {
            Text(
                "Primero inicia sesión con Google para usar el desbloqueo del teléfono."
            )
        }

        if (cargando) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        error?.let {
            Spacer(Modifier.height(16.dp))

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}