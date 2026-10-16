package com.example.roadmaputepsa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadmaputepsa.interfaz.biometric.BiometricScreen
import com.example.roadmaputepsa.interfaz.home.HomeScreen
import com.example.roadmaputepsa.interfaz.login.LoginScreen
import com.example.roadmaputepsa.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val usuario = authViewModel.usuario

    // Primero verificar el dispositivo y después mostrar el acceso con Google.
    // No enviar automáticamente a Home solo porque Firebase restauró otra sesión.
    var dispositivoVerificado by remember { mutableStateOf(false) }
    var accesoGoogleConfirmado by remember { mutableStateOf(false) }
    var procesandoLogin by remember { mutableStateOf(false) }

    when {
        !dispositivoVerificado -> BiometricScreen(
            onBiometricSuccess = { dispositivoVerificado = true }
        )

        !accesoGoogleConfirmado || usuario == null || procesandoLogin -> LoginScreen(
            onLoginStarted = { procesandoLogin = true },
            onLoginSuccess = {
                accesoGoogleConfirmado = true
                procesandoLogin = false
            },
            onLoginFailed = {
                // Nunca cerrar Firebase por fallos de Firestore o del selector.
                // Conservar el login visible hasta que Google se valide.
                accesoGoogleConfirmado = false
                procesandoLogin = false
            }
        )

        else -> HomeScreen(
            usuario = usuario,
            onLogout = {
                authViewModel.cerrarSesion()
                accesoGoogleConfirmado = false
                dispositivoVerificado = false
                procesandoLogin = false
            }
        )
    }
}
