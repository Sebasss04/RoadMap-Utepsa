package com.example.roadmaputepsa.interfaz.home
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(
    usuario: FirebaseUser,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = usuario.displayName ?: "Usuario"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = usuario.email ?: ""
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout
        ) {

            Text("Cerrar sesión")
        }
    }
}