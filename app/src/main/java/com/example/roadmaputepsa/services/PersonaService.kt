package com.example.roadmaputepsa.services

import com.example.roadmaputepsa.data.Persona
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class PersonaService {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registrarPersonaDesdeGoogle(): Result<Unit> {
        return try {

            val user = auth.currentUser
                ?: throw Exception("No existe un usuario autenticado")

            val persona = Persona(
                uid = user.uid,
                nombre = user.displayName ?: "",
                email = user.email ?: "",
                fotoUrl = user.photoUrl?.toString() ?: "",
                fechaRegistro = System.currentTimeMillis()
            )

            firestore
                .collection("personas")
                .document(user.uid)
                .set(persona, SetOptions.merge())
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}