package com.example.roadmaputepsa.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Vincula el token FCM a la cuenta que ha iniciado sesión.
 * La colección es personas (plural) para coincidir con PersonaService y firestore.rules.
 */
object TokenNotificaciones {
    private const val TAG = "FCM_TOKEN"

    suspend fun sincronizarToken(uid: String) {
        if (FirebaseAuth.getInstance().currentUser?.uid != uid) return

        val token = FirebaseMessaging.getInstance().token.await()
        if (FirebaseAuth.getInstance().currentUser?.uid != uid || token.isBlank()) return

        FirebaseFirestore.getInstance()
            .collection("personas")
            .document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .await()

        Log.d(TAG, "Token del dispositivo vinculado al usuario actual")
    }

    fun registrarTokenNuevo(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (token.isBlank()) return

        FirebaseFirestore.getInstance()
            .collection("personas")
            .document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Token FCM actualizado") }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error actualizando token FCM", error)
            }
    }
}
