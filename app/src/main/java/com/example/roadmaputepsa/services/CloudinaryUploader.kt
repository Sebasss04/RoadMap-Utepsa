package com.example.roadmaputepsa.services

import com.example.roadmaputepsa.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Subida opcional a Cloudinary mediante un unsigned upload preset propio. */
internal class CloudinaryUploader {
    private val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    val estaConfigurado: Boolean
        get() = cloudName.isNotBlank() && uploadPreset.isNotBlank()

    suspend fun subirFotoDesdeGoogle(fotoGoogle: String, uid: String): String =
        withContext(Dispatchers.IO) {
            require(estaConfigurado) { "Cloudinary no está configurado." }

            val origen = URL(fotoGoogle).openConnection().apply {
                connectTimeout = 15000
                readTimeout = 15000
            }
            val maxBytes = 5 * 1024 * 1024
            val bytes = origen.getInputStream().use { input ->
                val destino = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val leidos = input.read(buffer)
                    if (leidos == -1) break
                    require(destino.size() + leidos <= maxBytes) {
                        "La foto supera el límite de 5 MB."
                    }
                    destino.write(buffer, 0, leidos)
                }
                destino.toByteArray()
            }
            require(bytes.isNotEmpty()) { "La foto de Google está vacía." }

            val boundary = "----AndroidUpload${System.currentTimeMillis()}"
            val conexion = (
                URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .openConnection() as HttpURLConnection
            ).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            try {
                DataOutputStream(conexion.outputStream).use { salida ->
                    fun campo(nombre: String, valor: String) {
                        salida.writeBytes("--$boundary\r\n")
                        salida.writeBytes(
                            "Content-Disposition: form-data; name=\"$nombre\"\r\n\r\n"
                        )
                        salida.write(valor.toByteArray(Charsets.UTF_8))
                        salida.writeBytes("\r\n")
                    }
                    campo("upload_preset", uploadPreset)
                    campo("folder", "personas")
                    campo("public_id", "${uid}_${System.currentTimeMillis()}")
                    salida.writeBytes("--$boundary\r\n")
                    salida.writeBytes(
                        "Content-Disposition: form-data; name=\"file\"; filename=\"perfil.jpg\"\r\n"
                    )
                    salida.writeBytes("Content-Type: application/octet-stream\r\n\r\n")
                    salida.write(bytes)
                    salida.writeBytes("\r\n--$boundary--\r\n")
                    salida.flush()
                }
                val status = conexion.responseCode
                val respuesta = (
                    if (status in 200..299) conexion.inputStream else conexion.errorStream
                )?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (status !in 200..299) {
                    throw IllegalStateException("Cloudinary rechazó la imagen (HTTP $status).")
                }
                val seguro = JSONObject(respuesta).optString("secure_url")
                require(seguro.startsWith("https://")) {
                    "Cloudinary no devolvió una URL segura."
                }
                seguro
            } finally {
                conexion.disconnect()
            }
        }
}
