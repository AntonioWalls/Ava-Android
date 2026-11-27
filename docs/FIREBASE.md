# Firebase en Ava

La app conectada inicia por defecto con el proyecto ficticio `demo-ava-local` y usa Authentication, Firestore y Storage del Emulator Suite. Se pueden registrar cuentas y probar el foro y el chat. La demo `:demo` funciona de forma independiente.

## Preparar un entorno de desarrollo

1. En un proyecto de Firebase de desarrollo, registra la aplicación Android con ID `com.antoniowalls.ava` y habilita Email/Password.
2. Conserva la configuración de cliente en `app/google-services.json` y activa la conexión con `./gradlew :app:assembleDebug -PavaCloudFirebase=true`.
3. Valida el esquema y las reglas en ese entorno antes de habilitar el acceso de usuarios. Guarda cualquier cuenta de servicio en el entorno del servidor.

`Users/{uid}` contiene nombre completo, teléfono y correo del propietario. El directorio autenticado consulta `PublicProfiles/{uid}`, que contiene el identificador, el alias y la foto. Los mensajes se guardan bajo una conversación con dos participantes. El foro guarda cada comentario y su contador en una transacción. Los archivos de perfil y publicaciones se almacenan bajo rutas con el identificador de su propietario.

Las reglas incluidas son un prototipo probado con datos sintéticos y requieren revisión antes de desplegarse para usuarios reales. Si se reutiliza un proyecto con datos, respalda y prepara la migración en un entorno separado. Hay que generar los perfiles del directorio a partir de los tres campos permitidos y asociar las conversaciones previas a los dos usuarios correctos. Las reglas y la migración se pueden revisar antes de tomar cualquier decisión sobre el backend real.

## Comprobar reglas localmente

```sh
firebase emulators:start --project demo-ava-local --only auth,firestore,storage
node tools/check-backend.mjs
```

El script comprueba que el dueño puede guardar su teléfono, que el directorio solo devuelve alias y foto, que el comentario y el contador coinciden y que el contenido del chat permanece entre sus participantes. Las cuentas que crea son de prueba.
