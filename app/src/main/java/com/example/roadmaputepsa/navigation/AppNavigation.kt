package com.example.roadmaputepsa.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.roadmaputepsa.interfaz.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AppNavigation() {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    // No usamos auth.currentUser como estado inicial porque Firebase puede
    // conservar una sesión anterior y saltarse la pantalla de login.
    var loginValidado by remember {
        mutableStateOf(false)
    }

    var usuario: FirebaseUser? by remember {
        mutableStateOf(null)
    }

    if (!loginValidado) {

        LoginScreen(
            onLoginSuccess = {
                usuario = auth.currentUser

                if (usuario != null) {
                    loginValidado = true
                }
            }
        )

    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Sesión iniciada correctamente"
            )

            Text(
                text = usuario?.displayName
                    ?: usuario?.email
                    ?: "Usuario autenticado"
            )

            Button(
                onClick = {
                    auth.signOut()
                    usuario = null
                    loginValidado = false
                }
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
