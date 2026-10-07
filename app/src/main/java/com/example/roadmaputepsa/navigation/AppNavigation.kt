package com.example.roadmaputepsa.navigation

import androidx.compose.runtime.*
import com.example.roadmaputepsa.interfaz.home.HomeScreen
import com.example.roadmaputepsa.interfaz.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val auth = remember { FirebaseAuth.getInstance() }
    var usuario by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { usuario = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val usuarioActual = usuario
    if (usuarioActual == null) {
        LoginScreen(onLoginSuccess = { usuario = auth.currentUser })
    } else {
        HomeScreen(usuario = usuarioActual, onLogout = { auth.signOut() })
    }
}
