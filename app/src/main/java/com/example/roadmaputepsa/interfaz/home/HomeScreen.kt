package com.example.roadmaputepsa.interfaz.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(usuario: FirebaseUser, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Bienvenido a RoadMap Utepsa!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(usuario.displayName ?: "Usuario")
        Spacer(Modifier.height(8.dp))
        Text(usuario.email ?: "")
        Spacer(Modifier.height(32.dp))
        Button(onClick = onLogout) { Text("Cerrar sesión") }
    }
}
