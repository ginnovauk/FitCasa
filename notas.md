## Novedades respecto a v0.5.1

- **Pitidos acústicos de cuenta atrás (Audio Cues / Beeps)**:
  - Señales sonoras claras en los últimos 3 segundos (3, 2, 1) durante el ejercicio activo y durante el descanso.
  - Tono acústico confirmatorio al iniciar un ejercicio, al finalizar un intervalo y al completar la rutina.
  - Síntesis de sonido con latencia cero a través de `ToneGenerator` en `STREAM_MUSIC` (altavoz del móvil, auriculares o altavoces Bluetooth).
  - Interruptor de activación y botón para probar el sonido en el panel de ajustes de audio del entrenamiento.
- **Pruebas unitarias**:
  - Tests unitarios para el reproductor de tonos y su persistencia en preferencias (`WorkoutSoundCuePlayerTest`).
- **Versión 0.5.2**.

> **Nota:** El archivo `FitCasa-debug.apk` va firmado en modo depuración. Si tienes instalada una versión previa con otra firma, desinstálala antes de instalar esta.
