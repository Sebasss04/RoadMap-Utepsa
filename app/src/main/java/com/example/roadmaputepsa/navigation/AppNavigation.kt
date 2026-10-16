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
    // No persistir el desbloqueo tras reiniciar el proceso.
    var dispositivoVerificado by remember { mutableStateOf(false) }
    var procesandoLogin by remember { mutableStateOf(false) }

    when {
        !dispositivoVerificado -> BiometricScreen(
            onBiometricSuccess = { dispositivoVerificado = true }
        )

        usuario == null || procesandoLogin -> LoginScreen(
            onLoginStarted = { procesandoLogin = true },
            onLoginSuccess = { procesandoLogin = false },
            onLoginFailed = {
                procesandoLogin = false
                // Evitar Home si falla guardar la persona tras Firebase Auth.
                authViewModel.cerrarSesion()
            }
        )

        else -> HomeScreen(
            usuario = usuario,
            onLogout = {
                authViewModel.cerrarSesion()
                dispositivoVerificado = false
                procesandoLogin = false
            }
        )
    }
}
