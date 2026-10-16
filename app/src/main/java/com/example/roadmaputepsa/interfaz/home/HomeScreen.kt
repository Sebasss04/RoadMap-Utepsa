package com.example.roadmaputepsa.interfaz.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.roadmaputepsa.services.PersonaService
import com.example.roadmaputepsa.services.TokenNotificaciones
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun HomeScreen(usuario: FirebaseUser, onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cerrandoSesion by remember { mutableStateOf(false) }
    var permiteNotificaciones by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permiteNotificaciones = concedido }

    // Registrar los datos y la foto después de mostrar Home.
    // Una falla o demora de Cloudinary no bloqueará el inicio de sesión.
    LaunchedEffect(usuario.uid) {
        try {
            PersonaService().registrarPersonaDesdeGoogle().getOrThrow()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("PERSONA_FIRESTORE", "No se pudo registrar el perfil después del login", e)
        }
    }

    // Firebase puede emitir el token antes de que el usuario haya iniciado sesión.
    // Volver a obtenerlo al entrar a Home permite asociarlo al documento correcto.
    LaunchedEffect(usuario.uid) {
        try {
            TokenNotificaciones.sincronizarToken(usuario.uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "No fue posible sincronizar el token FCM", e)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Bienvenido!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(usuario.displayName ?: "Usuario")
        Spacer(Modifier.height(8.dp))
        Text(usuario.email ?: "")
        Spacer(Modifier.height(24.dp))

        // En Android 13+ solicitar este permiso requiere una acción del usuario.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !permiteNotificaciones
        ) {
            Button(
                onClick = {
                    solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            ) {
                Text("Permitir notificaciones")
            }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            enabled = !cerrandoSesion,
            onClick = {
                cerrandoSesion = true
                scope.launch {
                    try {
                        // Quitar el token de este dispositivo antes de cerrar Firebase.
                        withTimeoutOrNull(4000L) {
                            TokenNotificaciones.desvincularToken(usuario.uid)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w("FCM_TOKEN", "Fallo al desvincular notificaciones", e)
                    } finally {
                        onLogout()
                    }
                }
            }
        ) {
            Text(if (cerrandoSesion) "Cerrando sesión..." else "Cerrar sesión")
        }
    }
}
