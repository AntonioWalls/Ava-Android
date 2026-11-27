# Ava Demo

La demo abre «Sistema de Meditación» y enlaza la enseñanza con «Seguimiento de la respiración». La lectura, el audio y el progreso funcionan sin cuenta y en modo avión. La demo y la app conectada comparten el lector de `:lessons`. Un servicio de Media3 mantiene la reproducción con la pantalla bloqueada. [Guía de reproducción en segundo plano de Android](https://developer.android.com/media/media3/session/background-playback)

## Compilar e instalar

Usa JDK 21 y Android SDK Platform 35. Ejecuta estos comandos desde la raíz del repositorio.

```powershell
.\gradlew.bat :demo:assembleDebug
adb install demo/build/outputs/apk/debug/demo-debug.apk
```

En macOS o Linux, ejecuta `./gradlew`. Para compilar `:demo` basta con el código fuente. El APK usa el paquete `com.antoniowalls.ava.demo` y puede instalarse junto con la app conectada.

## Verificar la práctica sin red

1. Activa modo avión y desactiva Wi-Fi en un emulador o dispositivo.
2. Abre Ava Demo, lee «Sistema de Meditación» y pulsa **Escuchar práctica**.
3. Pulsa **Reproducir**. El audio dura dos minutos. Pausa y mueve la posición con el deslizador.
4. Reanuda y bloquea la pantalla unos segundos. Al volver, verifica que la posición avanzó. Desde la notificación puedes reabrir la práctica.
5. Pausa, cierra Ava Demo y ábrela otra vez. Toca **Continuar desde…**. El reproductor debe mostrar el tiempo guardado y esperar a que pulses **Reproducir**.
6. Llega al final y vuelve a abrir la app. La enseñanza debe mostrar **Práctica completada** y **Repetir práctica**. Al completar la práctica otra vez se conserva una sola marca.

El avance se guarda en el teléfono al pausar, al mover la posición y cada cinco segundos mientras suena el audio. Al finalizar, Ava guarda la marca de completada y vuelve a preparar el inicio para una próxima práctica.

La suite instrumentada comprueba lectura, reproducción, pausa, cambio de posición, finalización y recuperación tras recrear la actividad. También solicita el foco de audio de forma transitoria y verifica que la reproducción cede y continúa al recuperarlo. Con un emulador conectado, ejecuta `:demo:connectedDebugAndroidTest`.

En un emulador con Android 16 y en modo avión se forzó el cierre del proceso de Ava. Al abrirlo de nuevo se recuperó la posición en pausa y, después de dos finales, quedó una sola marca de completada. También se comprobó la reproducción con pantalla bloqueada y la salida de error con una pista dañada. El APK debug pesa 11.6 MB y carece de permisos de Internet y de estado de red.

## Pista de respiración

`breathing_cues.ogg` es una pista original de señales de tono, generada con ondas senoidales. Se puede regenerar con Python y FFmpeg con soporte para Vorbis mediante `python demo/tools/generate_breathing_audio.py`. El archivo listo para reproducir ya está incluido en el repositorio.
