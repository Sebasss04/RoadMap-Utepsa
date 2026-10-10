# RoadMap UTEPSA

Aplicación Android en Kotlin y Jetpack Compose, paquete `com.example.roadmaputepsa`.

## Autenticación integrada

Funciones adaptadas de `PatriciaAG/proyecto-final-autenticador`: Google con Firebase, bienvenida, cierre de sesión, AuthViewModel y biometría. Se conserva Firebase propio de RoadMap y Android mínimo 24.

- Primera apertura: Google autentica y muestra nombre/correo en Home.
- Con sesión Firebase guardada: Login permite desbloquear Home con biometría o PIN, patrón o contraseña del teléfono, o volver a autenticar con Google.
- Cerrar sesión deshabilita biometría hasta un nuevo acceso Google.
- Biometría verifica al usuario local; no crea una cuenta Firebase. Se permite biometría fuerte en Android 30+ y BIOMETRIC_WEAK en Android 24–29; en ambos casos se admite DEVICE_CREDENTIAL como respaldo.
- Se muestran errores y se impiden solicitudes simultáneas.

AuthPreferences y BiometricScreen están vacíos en la referencia; no aportan comportamiento que copiar. Firebase conserva la sesión y Login abre el diálogo biométrico del sistema.

## Configuración y comprobación

1. Abrir en Android Studio y sincronizar Gradle con las versiones del proyecto.
2. Mantener app/google-services.json de RoadMap. Habilitar Google en Firebase Authentication y registrar SHA-1/SHA-256 del APK utilizado. Si se modifica OAuth, descargar de nuevo el JSON.
3. Ejecutar `./gradlew :app:assembleDebug :app:testDebugUnitTest` (Windows: `gradlew.bat`).
4. Probar Google → Home → cerrar sesión; volver a entrar, cerrar/abrir la app y probar biometría, cancelación y un dispositivo con PIN/patrón pero sin huella registrada.

No se pudo compilar en el entorno de edición porque la red bloquea la descarga Gradle de services.gradle.org. Google y biometría requieren comprobación en un dispositivo.
