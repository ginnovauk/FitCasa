# FitCasa

<p align="center">
  <img src="docs/fitcasa-icon.svg" alt="Icono de FitCasa" width="160">
</p>

App Android de entrenamiento en casa **sin equipamiento**: rutinas por zona del cuerpo, nivel y tiempo disponible, temporizador con descansos, demostraciones de técnica y seguimiento local del progreso.

## English

FitCasa is a home workout app for Android with **no equipment and no account**: choose a body part, an intensity level and how much time you have, and it builds a routine with a timer, rests, technique demonstrations and a voice coach. Everything runs offline and your history stays on the phone.

- **Home**: streak, minutes and calories, plus shortcuts to each body part.
- **Routines**: three-step setup, generated plan and exercise list with step-by-step instructions.
- **Exercise library**: 46 bodyweight moves (10 of them with a broomstick) with instructions, breathing tips, common mistakes and their own animation.
- **Active workout**: circular timer, skippable rests, vibration, screen kept on and a voice coach that speaks the app language.
- **Progress**: local history (Room), no sign-up, sessions can be deleted.
- **Languages**: English and Spanish. Switch inside the app from the profile icon (System / Español / English); on Android 13+ it also appears in the system language settings.

Download the latest APK from [Releases](https://github.com/ginnovauk/FitCasa/releases).

## Capturas

<table>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/01-inicio.png" width="240" height="521" alt="Inicio con recomendación del día y gráfico semanal de kilocalorías"><br><sub>Inicio</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/02-configurador.png" width="240" height="521" alt="Configurador de entrenamiento: grupo muscular, intensidad y duración"><br><sub>Configurar</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/03-entrenamiento.png" width="240" height="521" alt="Pantalla de entrenamiento con temporizador y demostración de técnica"><br><sub>Entrenamiento</sub></td>
  </tr>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/04-descanso.png" width="240" height="521" alt="Pantalla de descanso con opción de saltar o añadir 20 segundos"><br><sub>Descanso</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/05-resumen.png" width="240" height="521" alt="Resumen al completar el entrenamiento: duración, calorías y ejercicios"><br><sub>Resumen</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/06-lista-ejercicios.png" width="240" height="521" alt="Lista de ejercicios del plan con orden y duración"><br><sub>Lista de ejercicios</sub></td>
  </tr>
</table>

## Qué incluye

- **Inicio**: racha de días, minutos y calorías acumuladas, acceso rápido a zonas del cuerpo e historial reciente.
- **Rutinas**: configuración en 3 pasos (zona, intensidad y duración), generación del plan y lista de ejercicios con instrucciones.
- **Entrenamiento activo**: cronómetro circular, descansos con opción de saltar o añadir 20 s, vibración, pantalla siempre encendida y entrenador por voz.
- **Progreso**: historial local con Room (sin registro ni cuenta) y borrado de sesiones.

## Catálogo de ejercicios

46 ejercicios sin equipamiento (los de palo solo necesitan un bastón o una escoba): 10 de espalda, 10 con palo, 9 de brazos, 7 de cuerpo completo, 4 de piernas, 3 de pecho y 3 de abdomen.

Los 46 tienen demostración propia en formato vectorial (Lottie) empaquetada en la app, así que funcionan sin conexión y se ven nítidas en cualquier pantalla. Cada una se acompaña de las instrucciones paso a paso, la respiración y los errores comunes.

**Créditos:** parte de las animaciones provienen de LottieFiles bajo la *Lottie Simple License* (permite uso comercial) y el resto están dibujadas para FitCasa, sin material de terceros. Ver `app/src/main/assets/exercises/lottie/NOTICE.txt`.

## Estimación de calorías

Cada ejercicio tiene un valor **MET** (Compendium of Physical Activities) convertido a kcal/min con un peso de referencia de 70 kg. La estimación de la sesión suma el tiempo activo al ritmo de cada ejercicio más un ritmo suave de recuperación durante los descansos. Así, entrenar piernas o cuerpo completo estima más gasto que entrenar brazos o core, en lugar de dar el mismo número genérico para todo.

El perfil (peso, estatura, edad y sexo, desde el icono de perfil de Inicio) ajusta esas tasas: se calcula el metabolismo basal con Mifflin-St Jeor y se escala el gasto respecto al cuerpo de referencia. Pesar o medir más sube la estimación; cumplir años la baja. Las sesiones ya guardadas conservan las kcal con las que se registraron.

Es una estimación orientativa; sirve para comparar sesiones entre sí, no sustituye una medición.

## Idiomas

La app está en inglés y español. El inglés es el idioma por defecto: si el móvil usa otro idioma, FitCasa se abre en inglés en lugar de quedarse a medias. Cambia el idioma desde Inicio › icono de perfil › Sistema / Español / English, sin reiniciar. En Android 13 o superior también aparece en **Ajustes del teléfono › Idiomas**.

## Voz del entrenador

- Usa el motor de voz de Google si está instalado y, si no, el motor del sistema.
- Elige automáticamente la mejor voz del idioma de la app (prefiere voces instaladas, sin conexión y de mayor calidad).
- Atenúa la música del teléfono mientras habla.
- Durante el entrenamiento, el icono de ajustes de voz permite cambiar de voz, ajustar la velocidad (pausada / natural / enérgica) y probarla.
- La preferencia de voz, velocidad y activación se guarda entre sesiones.
- Si faltan las voces del idioma, la app ofrece un botón para descargarlas.

Para que suene bien, instala las voces del idioma desde **Ajustes del teléfono › Idiomas y entrada › Salida de texto a voz**.

## Cómo ejecutarlo

**Requisitos:** Android Studio (JDK y SDK de Android).

1. Abre Android Studio y elige **Open** sobre la carpeta del proyecto.
2. Deja que Gradle sincronice y corrija incompatibilidades si hace falta.
3. Ejecuta en un emulador o dispositivo con Android 7.0 (API 24) o superior.

### Instalar el APK sin Android Studio

El APK de depuración se genera con:

```
gradlew assembleDebug
```

Queda en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo en el teléfono: copia el archivo, ábrelo y acepta la instalación de orígenes desconocidos. Si ya tenías instalada la versión de AI Studio, desinstálala primero porque la firma es distinta.

> Cada quien compila con su propia firma: si no tienes keystore de depuración, Android Studio/Gradle genera uno automáticamente. La app de ejemplo se firma con `debug.keystore` incluido en la configuración; puedes eliminar la línea `signingConfig = signingConfigs.getByName("debugConfig")` del bloque `debug` de `app/build.gradle.kts` para usar el keystore por defecto de tu máquina.

## Sin servicios externos

La app **no usa Firebase, ni cuentas, ni claves de API**: todo funciona en el teléfono y sin conexión (rutinas, animaciones, voz, historial). La única parte que necesita internet es la descarga de voces en español del sistema.

## Estructura

```
app/src/main/java/com/example/
├── data/           modelos, catálogo de ejercicios, Room y repositorios
├── ui/             pantallas, componentes, tema y ViewModels
└── util/           entrenador por voz (WorkoutVoiceCoach)
```
