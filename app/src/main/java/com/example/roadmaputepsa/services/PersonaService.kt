package com.example.roadmaputepsa.services

import com.example.roadmaputepsa.data.Persona
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firebase.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PersonaService {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    suspend fun registrarPersonaDesdeGoogle(): Result<Unit> {

        return try {

            val user =
                auth.currentUser
                    ?: throw Exception(
                        "No existe un usuario autenticado"
                    )

            val uid =
                user.uid

            val referencia =
                firestore
                    .collection("personas")
                    .document(uid)

            val documento =
                referencia.get().await()

            if (!documento.exists()) {

                val cliente = Persona(
                    uid = uid,
                    nombre = user.displayName?: "",
                    email = user.email?: "",
                    fotoUrl = user.photoUrl?.toString() ?: "",
                    fechaRegistro = System.currentTimeMillis()
                )
                referencia.set(cliente).await()
            }

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}