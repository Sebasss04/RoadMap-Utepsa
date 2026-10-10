package com.example.roadmaputepsa.navigation

import androidx.compose.runtime.*
import com.example.roadmaputepsa.interfaz.home.HomeScreen
import com.example.roadmaputepsa.interfaz.login.LoginScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadmaputepsa.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val usuario = authViewModel.usuario
    // Firebase conserva la sesión; cada apertura requiere desbloquearla.
    var desbloqueado by remember { mutableStateOf(false) }
    LaunchedEffect(usuario) {
        if (usuario == null) desbloqueado = false
    }

    val actual = usuario
    if (actual != null && desbloqueado) {
        HomeScreen(usuario = actual, onLogout = {
            desbloqueado = false
            authViewModel.cerrarSesion()
        })
    } else {
        LoginScreen(
            sesionDisponible = actual != null,
            onLoginSuccess = { desbloqueado = true }
        )
    }
}
