package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BodyPart
import com.example.data.repository.ExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Valida la integridad del catálogo de ejercicios: identificadores únicos,
 * contenido completo y cobertura suficiente por zona del cuerpo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExerciseCatalogTest {

    private val exercises = ExerciseCatalog.allExercises + ExerciseCatalog.warmUpExercises
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `los identificadores son unicos`() {
        val ids = exercises.map { it.id }
        assertEquals(
            "Hay identificadores repetidos en el catálogo",
            ids.size,
            ids.distinct().size
        )
    }

    @Test
    fun `todos los ejercicios tienen contenido completo`() {
        exercises.forEach { exercise ->
            val instructions = context.resources.getStringArray(exercise.instructionsRes).toList()
            assertTrue(
                "${exercise.id} no tiene instrucciones suficientes",
                instructions.size >= 2
            )
            assertTrue(
                "${exercise.id} tiene una instrucción vacía",
                instructions.none { it.isBlank() }
            )
            assertTrue(
                "${exercise.id} sin consejo de respiración",
                context.getString(exercise.breathingTipsRes).isNotBlank()
            )
            assertTrue(
                "${exercise.id} sin errores comunes",
                context.getString(exercise.commonMistakesRes).isNotBlank()
            )
            assertTrue(
                "${exercise.id} tiene una duración irreal (${exercise.defaultDurationSeconds}s)",
                exercise.defaultDurationSeconds in 20..60
            )
            assertTrue(
                "${exercise.id} tiene repeticiones inválidas (${exercise.defaultReps})",
                exercise.defaultReps in 1..50
            )
            assertTrue(
                "${exercise.id} tiene calorías fuera de rango (${exercise.caloriesPerMinute})",
                exercise.caloriesPerMinute in 2f..15f
            )
        }
    }

    @Test
    fun `cada animacion empaquetada existe y tiene su lienzo`() {
        val lottieDir = File("src/main/assets/exercises/lottie")
        val canvas = File(lottieDir, "canvas.json").readText()
        val withMedia = exercises.filter { it.lottieAsset != null }

        assertTrue("ningún ejercicio tiene animación", withMedia.size >= 10)
        withMedia.forEach { exercise ->
            val path = exercise.lottieAsset!!
            assertTrue("${exercise.id} no apunta a un asset local: $path", path.startsWith("exercises/lottie/"))
            assertTrue("${exercise.id} no apunta a un json: $path", path.endsWith(".json"))

            val assetFile = File("src/main/assets/$path")
            assertTrue("Falta el archivo empaquetado $path", assetFile.exists())
            assertTrue("El archivo $path está vacío", assetFile.length() > 1_000)
            assertTrue("El archivo $path no es json", assetFile.readText().trimStart().startsWith("{"))

            val name = path.substringAfterLast('/').removeSuffix(".json")
            assertTrue("Falta el lienzo de $name en canvas.json", canvas.contains("\"$name\""))
        }
    }

    @Test
    fun `no quedan gifs empaquetados`() {
        val leftovers = File("src/main/assets/exercises").listFiles { f -> f.extension == "gif" }
        assertTrue("quedan GIF en assets: ${leftovers?.joinToString { it.name }}", leftovers.isNullOrEmpty())
    }

    @Test
    fun `brazos y espalda tienen variedad suficiente`() {
        val arms = exercises.filter { it.bodyPart == BodyPart.BRAZOS }
        val back = exercises.filter { it.bodyPart == BodyPart.ESPALDA }

        assertTrue("Solo hay ${arms.size} ejercicios de brazos", arms.size >= 8)
        assertTrue("Solo hay ${back.size} ejercicios de espalda", back.size >= 8)
        assertEquals(
            "Hay ejercicios de brazos repetidos",
            arms.size,
            arms.distinctBy { it.id }.size
        )
    }

    @Test
    fun `cada zona del cuerpo tiene al menos tres ejercicios propios`() {
        BodyPart.values().forEach { bodyPart ->
            val own = exercises.count { it.bodyPart == bodyPart }
            assertTrue(
                "${context.getString(bodyPart.nameRes)} solo tiene $own ejercicios propios",
                own >= 3
            )
        }
    }

    @Test
    fun `los ejercicios de una rutina pertenecen a la zona elegida o a cuerpo completo`() {
        BodyPart.values().forEach { bodyPart ->
            val pool = ExerciseCatalog.getExercisesByBodyPart(bodyPart)
            assertTrue("Sin ejercicios para ${context.getString(bodyPart.nameRes)}", pool.isNotEmpty())

            if (bodyPart == BodyPart.CUERPO_COMPLETO) {
                assertEquals(ExerciseCatalog.allExercises.size, pool.size)
            } else {
                assertTrue(
                    "Se colaron ejercicios de otra zona en ${context.getString(bodyPart.nameRes)}",
                    pool.all { it.bodyPart == bodyPart || it.bodyPart == BodyPart.CUERPO_COMPLETO }
                )
            }
        }
    }

    @Test
    fun `el bloque de calentamiento esta listo para cualquier rutina`() {
        val warmUp = ExerciseCatalog.warmUpExercises

        assertTrue("No hay ejercicios de calentamiento", warmUp.size in 2..4)
        warmUp.forEach { exercise ->
            assertTrue("${exercise.id} no está marcado como calentamiento", exercise.isWarmUp)
            assertTrue(
                "${exercise.id} dura demasiado para calentar (${exercise.defaultDurationSeconds}s)",
                exercise.defaultDurationSeconds <= 30
            )
            assertTrue(
                "${exercise.id} quema demasiado para ser calentamiento (${exercise.caloriesPerMinute})",
                exercise.caloriesPerMinute <= 4f
            )
        }

        val idsPrincipales = ExerciseCatalog.allExercises.map { it.id }
        warmUp.forEach { exercise ->
            assertTrue(
                "El calentamiento repite el ejercicio principal ${exercise.id}",
                exercise.id !in idsPrincipales
            )
        }
    }

    @Test
    fun `la rutina de palo solo usa ejercicios con palo`() {
        val pool = ExerciseCatalog.getExercisesByBodyPart(BodyPart.PALO)
        assertTrue("Sin ejercicios de palo", pool.isNotEmpty())
        assertTrue(
            "Se colaron ejercicios sin palo: ${pool.map { it.id }}",
            pool.all { it.bodyPart == BodyPart.PALO }
        )
    }

    @Test
    fun `se puede buscar un ejercicio por su identificador`() {
        exercises.forEach { exercise ->
            assertEquals(exercise, ExerciseCatalog.getExerciseById(exercise.id))
        }
        assertEquals(
            exercises.first(),
            ExerciseCatalog.getExerciseById(exercises.first().id.uppercase())
        )
    }
}
