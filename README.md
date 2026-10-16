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

## Fotos: Cloudinary opcional

Se incluye la funcionalidad de subir la foto de Google a Cloudinary y
guardar la URL segura en Firestore. No se utiliza la cuenta de Cloudinary de
la docente. Crea tu propio *unsigned upload preset* en Cloudinary y agrega
estas propiedades a tu archivo local `~/.gradle/gradle.properties`
(en Windows: `C:\\Users\\TU_USUARIO\\.gradle\\gradle.properties`):

```properties
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_UPLOAD_PRESET=tu_unsigned_preset
```

No subas secretos ni API Secret a GitHub. Sin configurar Cloudinary, el
registro sigue funcionando y `fotoUrl` guarda el enlace original de Google.
La subida limita las imágenes a 5 MB y una falla de foto no bloquea el login.

## Pruebas pendientes en Android Studio

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

En Windows usa `gradlew.bat`. Verifica: dispositivo con PIN sin huella;
Google → Firestore → Home; sesión guardada tras reinicio; cierre de sesión;
cancelar y reintentar el diálogo de desbloqueo; reproductor; foto con/sin
Cloudinary. La revisión del código no sustituye una prueba real con
Firebase y Android SDK.
