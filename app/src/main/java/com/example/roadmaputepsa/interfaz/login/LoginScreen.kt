package com.example.roadmaputepsa.interfaz.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.roadmaputepsa.services.GoogleAuthHelper
import com.example.roadmaputepsa.services.PersonaService
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginStarted: () -> Unit,
    onLoginSuccess: () -> Unit,
    onLoginFailed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthHelper = remember(context) { GoogleAuthHelper(context) }
    val personaService = remember { PersonaService() }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Mi Aplicación", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))
        Button(
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                cargando = true
                mensajeError = null
                onLoginStarted()
                scope.launch {
                    try {
                        // No limpiar credenciales aquí: puede interrumpir el selector.
                        googleAuthHelper.signInWithGoogle().getOrThrow()
                        Log.d("GOOGLE_LOGIN", "Firebase Auth correcto")
                        
                        // Google Auth y Firestore son operaciones distintas.
                        // Un error de Firestore o Cloudinary no invalida el acceso.
                        try {
                            personaService.registrarPersonaDesdeGoogle().getOrThrow()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e(
                                "PERSONA_FIRESTORE",
                                "Google inició sesión, pero falló guardar el perfil",
                                e
                            )
                        }

                        Log.d("GOOGLE_LOGIN", "Acceso autorizado para el usuario")
                        onLoginSuccess()
                    } catch (e: CancellationException) {
                        onLoginFailed()
                        throw e
                    } catch (e: Exception) {
                        Log.e("GOOGLE_LOGIN", "Error iniciando sesión con Google", e)
                        mensajeError = e.localizedMessage
                            ?: "No se pudo iniciar sesión con Google."
                        onLoginFailed()
                    } finally {
                        cargando = false
                    }
                }
            }
        ) {
            Text("Continuar con Google")
        }
        if (cargando) {
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator()
            Text("Autenticando y registrando tu perfil...")
        }
        mensajeError?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
