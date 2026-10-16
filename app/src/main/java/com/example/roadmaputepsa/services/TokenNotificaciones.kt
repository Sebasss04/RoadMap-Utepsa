package com.example.roadmaputepsa.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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
    /**
     * Desasocia el token actual antes del signOut.
     * En equipos sin conexión se permite igualmente salir de la app.
     */
    suspend fun desvincularToken(uid: String) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser?.uid != uid) return

        val messaging = FirebaseMessaging.getInstance()
        val token = messaging.token.await()
        if (auth.currentUser?.uid != uid) return

        val ref = FirebaseFirestore.getInstance()
            .collection("personas")
            .document(uid)

        try {
            val guardado = ref.get().await().getString("fcmToken")
            if (guardado == token && auth.currentUser?.uid == uid) {
                ref.update("fcmToken", FieldValue.delete()).await()
                Log.d(TAG, "Token FCM desvinculado de esta sesión")
            }
        } finally {
            messaging.deleteToken().await()
        }
    }

}
