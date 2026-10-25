package com.example

import com.example.data.model.BodyPart
import com.example.data.repository.ExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Valida la integridad del catálogo de ejercicios: identificadores únicos,
 * contenido completo y cobertura suficiente por zona del cuerpo.
 */
class ExerciseCatalogTest {

    private val exercises = ExerciseCatalog.allExercises + ExerciseCatalog.warmUpExercises

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
            assertTrue(
                "${exercise.id} no tiene instrucciones suficientes",
                exercise.instructions.size >= 2
            )
            assertTrue(
                "${exercise.id} tiene una instrucción vacía",
                exercise.instructions.none { it.isBlank() }
            )
            assertTrue("${exercise.id} sin consejo de respiración", exercise.breathingTips.isNotBlank())
            assertTrue("${exercise.id} sin errores comunes", exercise.commonMistakes.isNotBlank())
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
    fun `cada ejercicio trae su animacion empaquetada en la app`() {
        exercises.forEach { exercise ->
            val url = exercise.demonstratorMediaUrl
            assertNotNull("${exercise.id} no tiene demostración", url)
            assertTrue(
                "${exercise.id} no apunta a un asset local: $url",
                url!!.startsWith("file:///android_asset/exercises/")
            )
            assertTrue("${exercise.id} no apunta a un gif: $url", url.endsWith(".gif"))

            val assetName = url.substringAfterLast('/')
            val assetFile = File("src/main/assets/exercises/$assetName")
            assertTrue("Falta el archivo empaquetado $assetName", assetFile.exists())
            assertTrue("El archivo $assetName está vacío", assetFile.length() > 10_000)
        }
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
                "${bodyPart.displayName} solo tiene $own ejercicios propios",
                own >= 3
            )
        }
    }

    @Test
    fun `los ejercicios de una rutina pertenecen a la zona elegida o a cuerpo completo`() {
        BodyPart.values().forEach { bodyPart ->
            val pool = ExerciseCatalog.getExercisesByBodyPart(bodyPart)
            assertTrue("Sin ejercicios para ${bodyPart.displayName}", pool.isNotEmpty())

            if (bodyPart == BodyPart.CUERPO_COMPLETO) {
                assertEquals(ExerciseCatalog.allExercises.size, pool.size)
            } else {
                assertTrue(
                    "Se colaron ejercicios de otra zona en ${bodyPart.displayName}",
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
