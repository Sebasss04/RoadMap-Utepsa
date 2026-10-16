# Autenticador Android (repositorio RoadMap-Utepsa)

App de práctica con Kotlin, Jetpack Compose, Google/Firebase, Cloud Firestore,
biometría/PIN y un reproductor de audio Media3.
El nombre de este repositorio no implica funcionalidades universitarias.

## Flujo

1. Antes de Google o Home, el usuario desbloquea el dispositivo mediante
   huella, rostro compatible, PIN, patrón o contraseña.
2. Si no existe sesión de Firebase, se muestra Google Sign-In.
3. `PersonaService` registra/actualiza `personas/{uid}` en Firestore. Se
   conserva la fecha original de registro y se respetan otros campos.
4. El Home muestra nombre/correo y permite cerrar sesión.
5. Cerrar sesión devuelve al bloqueo local. El reproductor Media3 continúa
   disponible en la parte superior.

La biometría/PIN verifica localmente al usuario; no equivale a iniciar sesión
en Firebase. El teléfono necesita tener configurada una medida de bloqueo.

## Configuración de Firebase

Se conserva `com.example.roadmaputepsa` y el `app/google-services.json`
propio del repositorio. Activa Google en Firebase Authentication,
registra las huellas SHA-1/SHA-256 y comprueba OAuth.
La colección usada es `personas` (plural), coherente con
`firestore.rules`. Esas reglas deben **publicarse en Firebase**; dejarlas
en el repositorio no las despliega.

## Cloudinary (código de la ingeniera)

La implementación de clase está ahora **en `services/PersonaService.kt`**,
con el `companion object` y las funciones `descargarFotoGoogle` y
`subirFotoCloudinary`. No hay que pegar nada adicional en Gradle ni
configurar un segundo uploader. `CloudinaryUploader.kt` se eliminó
para evitar mantener dos implementaciones.

Los valores proporcionados en clase son:

```kotlin
private const val CLOUD_NAME = "dzhwqsqqk"
private const val UPLOAD_PRESET = "persona_android"
```

**Advertencia:** estos identificadores apuntan a la cuenta de Cloudinary
entregada en clase. No son claves secretas, pero las fotografías que subas
se enviarán a esa cuenta. Utilízala solo si tienes autorización de la
propietaria; para usar tu propia cuenta, sustituye ambos valores.

Durante un login de Google, `PersonaService` registra el usuario en
`personas/{uid}` de **tu** Firestore, descarga la foto de Google (máximo 5 MB),
la sube mediante HTTP multipart y guarda la URL HTTPS devuelta en `fotoUrl`.
Para mantener la coherencia con `firestore.rules`, se usa la colección
`personas` en plural (en el proyecto de la ingeniera se llama `persona`).
Las reglas de Firestore deben estar publicadas, no solo guardadas en GitHub.

Si Cloudinary rechaza el preset o la imagen, se registra el error en Logcat
bajo `FOTO_CLOUDINARY`/`CLOUDINARY`, pero se conserva la cuenta y el enlace
original de Google. El preset debe existir y estar habilitado para subida
unsigned. No hay prueba de extremo a extremo sin iniciar sesión en el teléfono.

## Pruebas pendientes en Android Studio

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

En Windows usa `gradlew.bat`. Verifica: dispositivo con PIN sin huella;
Google → Firestore → Home; sesión guardada tras reinicio; cierre de sesión;
cancelar y reintentar el diálogo de desbloqueo; reproductor; foto con/sin
Cloudinary. La revisión del código no sustituye una prueba real con
Firebase y Android SDK.
