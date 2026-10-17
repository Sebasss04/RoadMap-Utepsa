package com.example.roadmaputepsa.interfaz.biometric

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.roadmaputepsa.autenticarConBiometria

@Composable
fun BiometricScreen(onBiometricSuccess: () -> Unit) {
    val activity = LocalContext.current as? FragmentActivity
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    fun solicitarVerificacion() {
        if (activity == null) {
            mensajeError = "No se pudo acceder a la actividad de Android."
            return
        }
        cargando = true
        mensajeError = null
        autenticarConBiometria(
            activity = activity,
            onSuccess = {
                cargando = false
                onBiometricSuccess()
            },
            onError = {
                cargando = false
                mensajeError = it
            }
        )
    }

    LaunchedEffect(activity) {
        solicitarVerificacion()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verificación de identidad",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        Text("Usa tu huella, rostro, PIN, patrón o contraseña del dispositivo.")
        Spacer(Modifier.height(24.dp))

        if (cargando) {
            CircularProgressIndicator()
        }
        mensajeError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { solicitarVerificacion() }) {
                Text("Intentar de nuevo")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    try {
                        activity?.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    } catch (e: ActivityNotFoundException) {
                        mensajeError = "Abre Ajustes y configura el bloqueo de pantalla."
                    } catch (e: SecurityException) {
                        mensajeError = "Abre Ajustes manualmente y configura el bloqueo."
                    }
                }
            ) {
                Text("Configurar PIN o patrón")
            }
        }
    }
}
