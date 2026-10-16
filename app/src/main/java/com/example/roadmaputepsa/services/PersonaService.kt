package com.example.roadmaputepsa.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class PersonaService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val cloudinary = CloudinaryUploader()

    suspend fun registrarPersonaDesdeGoogle(): Result<Unit> = try {
        val usuario = auth.currentUser
            ?: throw IllegalStateException("No hay una sesión activa en Firebase.")

        // Mantener la colección ya utilizada por la aplicación y firestore.rules.
        val referencia = firestore.collection("personas").document(usuario.uid)
        val anterior = referencia.get().await()
        val fotoAnterior = anterior.getString("fotoUrl").orEmpty()
        val fotoGoogle = usuario.photoUrl?.toString().orEmpty()

        val datos = mutableMapOf<String, Any>(
            "uid" to usuario.uid,
            "nombre" to (usuario.displayName ?: ""),
            "email" to (usuario.email ?: ""),
            "fechaRegistro" to (
                anterior.getLong("fechaRegistro") ?: System.currentTimeMillis()
            )
        )
        if (fotoAnterior.isBlank() && fotoGoogle.isNotBlank()) {
            datos["fotoUrl"] = fotoGoogle
        }
        // merge() evita borrar campos preexistentes, como apellido o teléfono.
        referencia.set(datos, SetOptions.merge()).await()

        // Cloudinary es opcional: sin configurarlo se conserva la foto de Google.
        if (
            fotoGoogle.isNotBlank() &&
            cloudinary.estaConfigurado &&
            !fotoAnterior.contains("res.cloudinary.com/")
        ) {
            try {
                val fotoSubida = cloudinary.subirFotoDesdeGoogle(
                    fotoGoogle, usuario.uid
                )
                referencia.update("fotoUrl", fotoSubida).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("PERSONA", "Perfil guardado, pero falló subir la foto.", e)
            }
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("PERSONA", "Error guardando persona en Firestore.", e)
        Result.failure(e)
    }
}
