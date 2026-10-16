package com.example.roadmaputepsa.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PersonaService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Datos entregados en clase: estas fotos se subirán a la cuenta indicada.
    // El upload preset debe permitir subidas sin firma ("unsigned").
    companion object {
        private const val CLOUD_NAME = "dzhwqsqqk"
        private const val UPLOAD_PRESET = "persona_android"
        private const val MAX_FOTO_BYTES = 5 * 1024 * 1024
    }

    suspend fun registrarPersonaDesdeGoogle(): Result<Unit> = try {
        val usuario = auth.currentUser
            ?: throw IllegalStateException("No existe una sesión activa en Firebase.")

        // Mantener la colección que utiliza este proyecto y sus firestore.rules.
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
        // No borrar campos previos del usuario.
        referencia.set(datos, SetOptions.merge()).await()
        Log.d("PERSONA_FIRESTORE", "Documento guardado: personas/${usuario.uid}")

        if (
            fotoGoogle.isNotBlank() &&
            !fotoAnterior.contains("res.cloudinary.com/")
        ) {
            try {
                // La descarga se hace en IO y se limita a un máximo de 5 MB.
                val bytes = descargarFotoGoogle(fotoGoogle)
                Log.d("CLOUDINARY", "Subiendo foto de Google a Cloudinary")
                val fotoSubida = subirFotoCloudinary(bytes, usuario.uid)
                referencia.update("fotoUrl", fotoSubida).await()
                Log.d("CLOUDINARY", "URL de Cloudinary guardada en Firestore")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No anular el acceso si falla solo la foto.
                Log.e("FOTO_CLOUDINARY", "No se pudo subir la foto; persona guardada", e)
            }
        } else if (fotoGoogle.isBlank()) {
            Log.w("FOTO_CLOUDINARY", "Google no proporcionó una fotografía")
        }

        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("PERSONA_FIRESTORE", "No se pudo registrar la persona", e)
        Result.failure(e)
    }

    private suspend fun descargarFotoGoogle(fotoUrl: String): ByteArray =
        withContext(Dispatchers.IO) {
            val connection = URL(fotoUrl).openConnection().apply {
                connectTimeout = 15000
                readTimeout = 15000
            }
            val bytes = connection.getInputStream().use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val cantidad = input.read(buffer)
                    if (cantidad == -1) break
                    require(output.size() + cantidad <= MAX_FOTO_BYTES) {
                        "La foto supera el límite de 5 MB"
                    }
                    output.write(buffer, 0, cantidad)
                }
                output.toByteArray()
            }
            require(bytes.isNotEmpty()) { "La imagen descargada está vacía" }
            bytes
        }

    // Misma función indicada en clase, con comprobación correcta del preset.
    private suspend fun subirFotoCloudinary(
        bytes: ByteArray,
        uid: String
    ): String = withContext(Dispatchers.IO) {
        require(
            CLOUD_NAME.isNotBlank() &&
            UPLOAD_PRESET.isNotBlank() &&
            CLOUD_NAME != "TU_CLOUD_NAME" &&
            UPLOAD_PRESET != "TU_UPLOAD_PRESET"
        ) {
            "Configura correctamente CLOUD_NAME y UPLOAD_PRESET en PersonaService"
        }

        val boundary = "----AndroidCloudinary${System.currentTimeMillis()}"
        val endpoint = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        try {
            DataOutputStream(connection.outputStream).use { output ->
                fun campo(nombre: String, valor: String) {
                    output.writeBytes("--$boundary\r\n")
                    output.writeBytes(
                        "Content-Disposition: form-data; name=\"$nombre\"\r\n\r\n"
                    )
                    output.write(valor.toByteArray(Charsets.UTF_8))
                    output.writeBytes("\r\n")
                }

                campo("upload_preset", UPLOAD_PRESET)
                campo("folder", "personas")
                campo("public_id", "${uid}_${System.currentTimeMillis()}")

                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"perfil.jpg\"\r\n"
                )
                output.writeBytes("Content-Type: application/octet-stream\r\n\r\n")
                output.write(bytes)
                output.writeBytes("\r\n--$boundary--\r\n")
                output.flush()
            }

            val status = connection.responseCode
            val responseStream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val response = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                Log.e("CLOUDINARY", "Cloudinary respondió con HTTP $status: $response")
                throw IllegalStateException("Cloudinary rechazó la imagen (HTTP $status)")
            }
            val json = JSONObject(response)
            val secureUrl = json.optString("secure_url")
            require(secureUrl.startsWith("https://")) {
                "Cloudinary no devolvió una URL HTTPS válida"
            }
            secureUrl
        } finally {
            connection.disconnect()
        }
    }
}
