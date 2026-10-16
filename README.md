# Autenticador Android (repositorio RoadMap-Utepsa)

App de práctica con Kotlin, Jetpack Compose, Google/Firebase, Cloud Firestore,
biometría/PIN y un reproductor de audio Media3.
El nombre de este repositorio no implica funcionalidades universitarias.

## Flujo

1. Antes de Google o Home, el usuario desbloquea el dispositivo mediante
   huella, rostro compatible, PIN, patrón o contraseña.
2. Después de verificar el dispositivo, se muestra Google Sign-In, incluso si Firebase restauró una sesión anterior. Se confirma una cuenta para esta apertura.
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
private const val CLOUD_NAME = "r8iydbsg"
private const val UPLOAD_PRESET = "persona_android"
```

El **Cloud name** es el de tu propia cuenta que se ve en la captura. El nombre
`persona_android` es el del preset de clase: **debes crear un preset de tipo
Unsigned con ese mismo nombre en tu cuenta** para que la subida funcione.
No necesitas copiar las claves API Secret al repositorio ni a la app.

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

## Firebase Cloud Messaging (FCM)

Se incorporó Firebase Messaging a partir del último commit de la ingeniera,
adaptando el servicio a `com.example.roadmaputepsa` y a `personas/{uid}`.

- `services/AppFirebaseMessagingService.kt`: maneja los nuevos tokens FCM y
  los mensajes de datos o notificaciones recibidos con la app en primer plano.
- `services/TokenNotificaciones.kt`: asocia el token a `fcmToken` del usuario
  autenticado en Cloud Firestore. También recupera el token al entrar a Home,
  porque `onNewToken()` puede ejecutarse antes del inicio de sesión.
- `AndroidManifest.xml`: registra el servicio y el permiso
  `POST_NOTIFICATIONS`.
- `HomeScreen.kt`: botón para permitir notificaciones en Android 13+.
- `res/drawable/ic_stat_notification.xml`: icono para las notificaciones.

En Android 13 o posterior, inicia sesión y pulsa **Permitir notificaciones**.
Luego revisa `personas/{uid}.fcmToken` en Firestore y prueba una notificación
de Firebase Cloud Messaging desde la consola de **tu proyecto Firebase**.
Una notificación de tipo *notification* puede ser mostrada automáticamente
por Android cuando la app está en segundo plano, en lugar de pasar por
`onMessageReceived`. La entrega push depende de que haya conectividad y
servicios FCM disponibles en el dispositivo.

Los tokens son identificadores de entrega asociados a dispositivos:
mantén privadas las reglas de Firestore y no compartas tokens públicamente.
Para producción, implementa la desvinculación/invalidación del token al
cerrar sesión para evitar avisos del usuario anterior en dispositivos compartidos.

## Pruebas pendientes en Android Studio

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

En Windows usa `gradlew.bat`. Verifica: dispositivo con PIN sin huella;
Google → Firestore → Home; sesión guardada tras reinicio; cierre de sesión;
cancelar y reintentar el diálogo de desbloqueo; reproductor; foto con/sin
Cloudinary. La revisión del código no sustituye una prueba real con
Firebase y Android SDK.

## Ajuste del acceso con Google

- Se separa el acceso de Firebase Authentication del registro en Cloud Firestore.
  Un error al guardar el perfil o subir su fotografía se registra en Logcat,
  pero ya no provoca un cierre de sesión automático.
- `LoginScreen` deja de llamar `clearCredentialState()` antes de abrir Google;
  esa operación no es necesaria para iniciar sesión y puede fallar antes de
  presentar el selector de cuentas.
- Tras verificar el dispositivo se muestra la pantalla de Google. Una sesión
  anterior de Firebase no salta directamente a Home.
- Diagnóstico en Logcat: `GOOGLE_LOGIN` indica fallos del selector de cuentas
  o Firebase Authentication; `PERSONA_FIRESTORE` errores al registrar datos.
- Si aparece `ApiException: 10` o `DEVELOPER_ERROR`, comprueba SHA-1/SHA-256,
  OAuth web y `google-services.json` en Firebase. Este problema no puede
  repararse solo desde el código Kotlin.

Después de sincronizar: ejecutar `gradlew.bat :app:assembleDebug` y probar
Google en dispositivo. Esta rama aún no cuenta con pruebas Android reales.

## Registro de perfil después del login

`LoginScreen.kt` ahora ejecuta solo Google/Firebase Authentication y
abre Home tan pronto como obtiene un usuario autenticado. `HomeScreen.kt`
inicia el registro de Firestore y la subida de foto a Cloudinary con
`LaunchedEffect(usuario.uid)`; fallos o esperas de Cloudinary no impiden
que aparezca Home. La actualización del perfil puede tardar y se cancela
si la pantalla sale de composición. Revisar errores `PERSONA_FIRESTORE`
y `FOTO_CLOUDINARY` en Logcat. No se cambió el proyecto Firebase ni el
package Android.
