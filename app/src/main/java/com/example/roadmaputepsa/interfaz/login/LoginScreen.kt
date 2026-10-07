package com.example.roadmaputepsa.interfaz.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.roadmaputepsa.services.GoogleAuthHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val googleAuthHelper = remember(context) {
        GoogleAuthHelper(context)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "RoadMap Utepsa",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMensaje = null
                cargando = true
                scope.launch {
                    try {
                        val result = googleAuthHelper.signInWithGoogle()
                        result
                            .onSuccess { nombre ->
                                println(
                                    "Login exitoso: $nombre"
                                )
                                onLoginSuccess()
                            }
                            .onFailure { error ->
                                errorMensaje = error.message ?: "No se pudo iniciar sesión"
                            }
                    } finally {
                        cargando = false
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(if (cargando) "Iniciando sesión…" else "Continuar con Google")
        }

        errorMensaje?.let { mensaje ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = mensaje, color = MaterialTheme.colorScheme.error)
        }
    }
}