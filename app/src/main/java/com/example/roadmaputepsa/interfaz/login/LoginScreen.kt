package com.example.roadmaputepsa.interfaz.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.roadmaputepsa.services.GoogleAuthHelper
import com.example.roadmaputepsa.services.PersonaService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val googleAuthHelper = remember {
        GoogleAuthHelper(context)
    }

    val personaService = remember {
        PersonaService()
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Mi Aplicación",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = !isLoading,
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null

                    googleAuthHelper
                        .signInWithGoogle()
                        .onSuccess { nombre ->

                            println("Login exitoso: $nombre")

                            personaService
                                .registrarPersonaDesdeGoogle()
                                .onSuccess {
                                    println("Persona registrada en Firestore")
                                }
                                .onFailure { error ->
                                    println(
                                        "No se pudo guardar la persona en Firestore: ${error.message}"
                                    )
                                }

                            // El login ya fue correcto. Firestore no debe
                            // bloquear el acceso a la app si sus reglas fallan.
                            onLoginSuccess()
                        }
                        .onFailure { error ->
                            errorMessage =
                                "Error Google: ${error.message}"
                        }

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Continuar con Google")
            }
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}