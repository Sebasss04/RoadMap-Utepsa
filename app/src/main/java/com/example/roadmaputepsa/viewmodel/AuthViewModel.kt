package com.example.roadmaputepsa.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    var usuario by mutableStateOf(auth.currentUser)
        private set
    private val listener = FirebaseAuth.AuthStateListener { usuario = it.currentUser }

    init { auth.addAuthStateListener(listener) }
    fun cerrarSesion() { auth.signOut() }
    fun usuarioActual() = usuario

    override fun onCleared() {
        auth.removeAuthStateListener(listener)
        super.onCleared()
    }
}
