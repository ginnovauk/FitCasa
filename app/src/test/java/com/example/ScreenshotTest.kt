package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.WorkoutEntity
import com.example.data.model.BodyPart
import com.example.data.model.IntensityLevel
import com.example.data.model.WorkoutPlan
import com.example.data.repository.ExerciseCatalog
import com.example.ui.screens.ActiveWorkoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.WorkoutBuilderScreen
import com.example.ui.screens.WorkoutSummaryScreen
import com.example.ui.theme.FitCasaTheme
import com.example.ui.viewmodel.ActiveWorkoutUiState
import com.example.util.VoiceCoachUiState
import com.example.util.VoiceOption
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Renderiza las pantallas principales a PNG sin emulador, para revisarlas antes de instalar.
 *
 * Usa la regla de test de Compose (reloj de frames propio) en lugar de dibujar la vista a mano:
 * con el Choreographer de Robolectric, las animaciones infinitas de Compose se re-disparan de
 * forma recursiva y el test nunca termina.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // Fecha real: así las sesiones caen dentro de la semana en curso y el
    // gráfico semanal del inicio muestra barras con datos.
    private val now = System.currentTimeMillis()

    private val plan = run {
        val exercises = ExerciseCatalog.getExercisesByBodyPart(BodyPart.BRAZOS)
            .filter { it.bodyPart == BodyPart.BRAZOS }
            .take(8)
            // (para las capturas se usa el GIF real; GifDecoder dibuja en Robolectric)
            .map { it.copy(defaultDurationSeconds = 30) }

        WorkoutPlan(
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.INTERMEDIO,
            targetDurationMinutes = 15,
            exercises = exercises,
            estimatedCalories = 168,
            restBetweenSeconds = 20
        )
    }

    private val voiceState = VoiceCoachUiState(
        isEnabled = true,
        isReady = true,
        engineLabel = "Motor de voz de Google",
        voices = listOf(
            VoiceOption("es-es-x-eea-local", "Español (España) · Voz 1", "Calidad máxima", false),
            VoiceOption("es-us-x-sfb-local", "Español (EE. UU.)", "Alta calidad", false),
            VoiceOption("es-es-x-eed-network", "Español (España) · Voz 2", "Calidad estándar", true)
        ),
        selectedVoiceId = "es-es-x-eea-local",
        speechRate = 1.0f
    )

    private val history = listOf(
        WorkoutEntity(
            id = 1, dateMillis = now, workoutTitle = "Rutina de Espalda - Intermedio",
            bodyPartId = "espalda", intensityId = "intermedio", durationSeconds = 902,
            caloriesBurned = 171, exercisesCompleted = 17, totalExercises = 17
        ),
        WorkoutEntity(
            id = 2, dateMillis = now - 86_400_000L, workoutTitle = "Rutina de Brazos - Principiante",
            bodyPartId = "brazos", intensityId = "principiante", durationSeconds = 640,
            caloriesBurned = 98, exercisesCompleted = 12, totalExercises = 12
        ),
        WorkoutEntity(
            id = 3, dateMillis = now - 2 * 86_400_000L, workoutTitle = "Rutina de Cuerpo Completo - Avanzado",
            bodyPartId = "cuerpo_completo", intensityId = "avanzado", durationSeconds = 1500,
            caloriesBurned = 280, exercisesCompleted = 25, totalExercises = 25
        )
    )

    @Test
    fun `01 inicio`() = shoot("01-inicio") {
        HomeScreen(
            streakDays = 4,
            totalMinutes = 215,
            totalCalories = 1420,
            recentWorkouts = history,
            onOpenProfile = {},
            onStartQuickWorkout = {},
            onNavigateToBuilder = {},
            onNavigateToHistory = {}
        )
    }

    @Test
    fun `02 configurador`() = shoot("02-configurador") {
        WorkoutBuilderScreen(
            plan = plan,
            selectedBodyPart = BodyPart.BRAZOS,
            selectedIntensity = IntensityLevel.INTERMEDIO,
            selectedDurationMinutes = 15,
            onSelectBodyPart = {},
            onSelectIntensity = {},
            onSelectDuration = {},
            onMoveExercise = { _, _ -> },
            onStartWorkout = {},
            onBack = {}
        )
    }

    @Test
    fun `03 entrenamiento`() = shoot("03-entrenamiento") {
        ActiveWorkoutScreen(
            state = ActiveWorkoutUiState(
                plan = plan,
                currentExerciseIndex = 0,
                remainingSeconds = 22,
                totalSecondsForCurrentStep = 30,
                elapsedTotalSeconds = 128,
                exercisesCompletedCount = 2
            ),
            voiceState = voiceState,
            currentVoiceMessage = "Últimos diez segundos, ¡resiste!",
            onToggleVoiceCoach = {},
            onSelectVoice = {},
            onSelectVoiceRate = {},
            onSelectVoicePitch = {},
            onPreviewVoice = {},
            onTogglePause = {},
            onStartStep = {},
            onSkipRest = {},
            onAdd20SecondsToRest = {},
            onNextExercise = {},
            onPreviousExercise = {},
            onCancelWorkout = {}
        )
    }

    @Test
    fun `04 descanso`() = shoot("04-descanso") {
        ActiveWorkoutScreen(
            state = ActiveWorkoutUiState(
                plan = plan,
                currentExerciseIndex = 2,
                remainingSeconds = 15,
                totalSecondsForCurrentStep = 20,
                isResting = true,
                elapsedTotalSeconds = 310,
                exercisesCompletedCount = 2
            ),
            voiceState = voiceState,
            currentVoiceMessage = "Descansa 15 segundos. Ahora sigue: Remo de Pie con Toalla.",
            onToggleVoiceCoach = {},
            onSelectVoice = {},
            onSelectVoiceRate = {},
            onSelectVoicePitch = {},
            onPreviewVoice = {},
            onTogglePause = {},
            onStartStep = {},
            onSkipRest = {},
            onAdd20SecondsToRest = {},
            onNextExercise = {},
            onPreviousExercise = {},
            onCancelWorkout = {}
        )
    }

    @Test
    fun `05 resumen`() = shoot("05-resumen") {
        WorkoutSummaryScreen(
            state = ActiveWorkoutUiState(
                plan = plan,
                currentExerciseIndex = 7,
                isCompleted = true,
                elapsedTotalSeconds = 912,
                exercisesCompletedCount = 8,
                totalSecondsForCurrentStep = 30
            ),
            onFinish = {}
        )
    }

    @Test
    fun `06 lista de ejercicios`() = shoot(
        "06-lista-ejercicios",
        after = {
            // La lista es perezosa: el título puede no estar compuesto todavía.
            composeRule.onNode(hasScrollAction()).performScrollToIndex(4)
            composeRule.mainClock.advanceTimeBy(400)
        }
    ) {
        WorkoutBuilderScreen(
            plan = plan,
            selectedBodyPart = BodyPart.BRAZOS,
            selectedIntensity = IntensityLevel.INTERMEDIO,
            selectedDurationMinutes = 15,
            onSelectBodyPart = {},
            onSelectIntensity = {},
            onSelectDuration = {},
            onMoveExercise = { _, _ -> },
            onStartWorkout = {},
            onBack = {}
        )
    }

    /**
     * Una actividad nueva por captura: reutilizar el mismo ComposeView deja la capa anterior
     * traslúcida debajo de la nueva pantalla.
     */
    private fun shoot(
        name: String,
        after: () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            FitCasaTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    content()
                }
            }
        }

        // Las animaciones cargan en hilos reales y publican el resultado en el
        // main looper, que Robolectric mantiene pausado: drena y avanza frames
        // para que la captura no salga a medio componer.
        repeat(6) {
            Thread.sleep(500)
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            composeRule.mainClock.advanceTimeBy(500)
        }
        composeRule.mainClock.advanceTimeByFrame()

        after()

        val file = capture(name)
        println("Captura generada: ${file.absolutePath} (${file.length() / 1024} KB)")
    }

    private fun capture(name: String): File {
        val width = 822
        val height = 1782

        // Dibujar el decorView arrastra capas de hardware que en Robolectric quedan
        // obsoletas (una captura repite la anterior y aparecen textos fantasma).
        // El ComposeView se dibuja en software y sale limpio.
        val content = composeRule.activity.findViewById<ViewGroup>(android.R.id.content)
        val target: View = (0 until content.childCount)
            .map { content.getChildAt(it) }
            .firstOrNull { it is ComposeView } ?: content

        composeRule.runOnUiThread {
            target.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            target.layout(0, 0, width, height)
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        composeRule.runOnUiThread { target.draw(Canvas(bitmap)) }

        val outDir = File("build/screenshots").apply { mkdirs() }
        val file = File(outDir, "$name.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return file
    }
}
