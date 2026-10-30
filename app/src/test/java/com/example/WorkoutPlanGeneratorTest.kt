package com.example

import com.example.data.local.WorkoutDao
import com.example.data.local.WorkoutEntity
import com.example.data.model.BodyPart
import com.example.data.model.IntensityLevel
import com.example.data.model.UserProfile
import com.example.data.repository.ExerciseCatalog
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

private class FakeWorkoutDao : WorkoutDao {
    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList<WorkoutEntity>())
    override suspend fun getWorkoutById(id: Long): WorkoutEntity? = null
    override suspend fun insertWorkout(workout: WorkoutEntity): Long = 1L
    override suspend fun deleteWorkout(id: Long) = Unit
    override suspend fun clearAll() = Unit
    override fun getWorkoutCount(): Flow<Int> = flowOf(0)
    override fun getTotalDurationSeconds(): Flow<Int?> = flowOf(0)
    override fun getTotalCaloriesBurned(): Flow<Int?> = flowOf(0)
}

class WorkoutPlanGeneratorTest {

    private val repository = WorkoutRepository(FakeWorkoutDao())

    @Test
    fun `la rutina dura aproximadamente el tiempo elegido`() {
        val toleranciaSegundos = 180

        for (intensidad in IntensityLevel.values()) {
            for (minutos in listOf(7, 15, 25)) {
                val plan = repository.generateWorkoutPlan(
                    bodyPart = BodyPart.CUERPO_COMPLETO,
                    intensity = intensidad,
                    targetDurationMinutes = minutos
                )

                // El calentamiento no cuenta: es un bloque fijo común.
                val principales = plan.exercises.filter { !it.isWarmUp }
                val trabajo = principales.first().defaultDurationSeconds
                val duracionReal = principales.size * trabajo +
                    (principales.size - 1) * plan.restBetweenSeconds

                assertTrue(
                    "La rutina de $minutos min ($intensidad) dura ${duracionReal}s",
                    abs(duracionReal - minutos * 60) <= toleranciaSegundos
                )
            }
        }
    }

    @Test
    fun `toda rutina abre con el bloque de calentamiento`() {
        for (parte in BodyPart.values()) {
            val plan = repository.generateWorkoutPlan(
                bodyPart = parte,
                intensity = IntensityLevel.INTERMEDIO,
                targetDurationMinutes = 15
            )

            val calentamiento = plan.exercises.takeWhile { it.isWarmUp }
            assertTrue(
                "La rutina de ${parte.displayName} no empieza con calentamiento",
                calentamiento.size == ExerciseCatalog.warmUpExercises.size
            )
            assertEquals(
                "El calentamiento no respeta el orden fijo",
                ExerciseCatalog.warmUpExercises.map { it.id },
                calentamiento.map { it.id }
            )
        }
    }

    @Test
    fun `la rutina siempre tiene ejercicios y respeta los limites`() {
        for (parte in BodyPart.values()) {
            val plan = repository.generateWorkoutPlan(
                bodyPart = parte,
                intensity = IntensityLevel.PRINCIPIANTE,
                targetDurationMinutes = 15
            )

            assertTrue("Sin ejercicios para ${parte.displayName}", plan.exercises.isNotEmpty())
            assertTrue(plan.exercises.size in 4..28)
            assertEquals(parte, plan.bodyPart)
            assertTrue(plan.estimatedCalories > 0)
        }
    }

    @Test
    fun `la rutina prioriza ejercicios del nivel elegido`() {
        for (parte in BodyPart.values()) {
            val poolPropio = com.example.data.repository.ExerciseCatalog
                .getExercisesByBodyPart(parte)
                .filter { it.bodyPart == parte }

            for (nivel in IntensityLevel.values()) {
                if (poolPropio.none { it.intensity == nivel }) continue

                val plan = repository.generateWorkoutPlan(
                    bodyPart = parte,
                    intensity = nivel,
                    targetDurationMinutes = 15
                )

                assertEquals(
                    "La rutina de ${parte.displayName} ($nivel) empieza con un ejercicio de otro nivel",
                    nivel,
                    plan.exercises.first { !it.isWarmUp }.intensity
                )
            }
        }
    }

    @Test
    fun `brazos y espalda no repiten el mismo ejercicio al inicio`() {
        for (parte in listOf(BodyPart.BRAZOS, BodyPart.ESPALDA)) {
            val plan = repository.generateWorkoutPlan(
                bodyPart = parte,
                intensity = IntensityLevel.INTERMEDIO,
                targetDurationMinutes = 15
            )

            val propios = plan.exercises
                .filter { it.bodyPart == parte }
                .map { it.id }

            assertTrue(
                "${parte.displayName} usa muy pocos ejercicios propios: ${propios.distinct()}",
                propios.distinct().size >= 3
            )
        }
    }

    @Test
    fun `las calorias crecen con la duracion elegida`() {
        val porMinutos = listOf(7, 15, 25).map { minutos ->
            repository.generateWorkoutPlan(
                bodyPart = BodyPart.CUERPO_COMPLETO,
                intensity = IntensityLevel.INTERMEDIO,
                targetDurationMinutes = minutos
            ).estimatedCalories
        }

        assertTrue(
            "Las calorías no crecen con la duración: 7=${
                porMinutos[0]
            }, 15=${porMinutos[1]}, 25=${porMinutos[2]}",
            porMinutos[0] < porMinutos[1] && porMinutos[1] < porMinutos[2]
        )
    }

    @Test
    fun `los grandes grupos musculares queman mas que los brazos`() {
        // Normalizo la duración a 60 s en todos: así la única diferencia
        // entre ambas listas es el MET (calorías por minuto) de cada ejercicio.
        fun porMinuto(id: String) =
            ExerciseCatalog.getExerciseById(id)!!.copy(defaultDurationSeconds = 60)

        val piernas = listOf("squats_bodyweight", "lunges_alternating").map(::porMinuto)
        val brazos = listOf("biceps_self_resistance", "kneeling_triceps_extension").map(::porMinuto)

        val kcalPiernas = repository.estimateSessionCalories(piernas, restSecondsBetween = 20, targetDurationMinutes = 1)
        val kcalBrazos = repository.estimateSessionCalories(brazos, restSecondsBetween = 20, targetDurationMinutes = 1)

        assertTrue(
            "Piernas ($kcalPiernas kcal) no quema más que brazos ($kcalBrazos kcal)",
            kcalPiernas > kcalBrazos
        )
    }

    @Test
    fun `la estimacion no ignora el tipo de ejercicio`() {
        // Antes de calibrar con MET, un suelo plano dejaba todas las rutinas
        // (~90 kcal) casi iguales. Promedio varias muestras porque la selección
        // se baraja; comparo piernas vs brazos, donde la diferencia es nítida.
        fun mediaCalorias(parte: BodyPart): Double =
            (1..8).map {
                repository.generateWorkoutPlan(
                    bodyPart = parte,
                    intensity = IntensityLevel.INTERMEDIO,
                    targetDurationMinutes = 15
                ).estimatedCalories
            }.average()

        val piernas = mediaCalorias(BodyPart.PIERNAS)
        val brazos = mediaCalorias(BodyPart.BRAZOS)

        assertTrue(
            "Piernas (%.1f kcal) no supera a brazos (%.1f kcal)".format(piernas, brazos),
            piernas > brazos
        )
    }

    @Test
    fun `el cuerpo completo supera claramente a los brazos`() {
        // Regresión: el circuito de cuerpo completo repartía su cardio solo al
        // principio (22% de la sesión), lo que lo empataba con brazos (74 vs 73).
        // Ahora las ráfagas se distribuyen y el margen debe ser holgado.
        fun mediaCalorias(parte: BodyPart): Double =
            (1..8).map {
                repository.generateWorkoutPlan(
                    bodyPart = parte,
                    intensity = IntensityLevel.INTERMEDIO,
                    targetDurationMinutes = 15
                ).estimatedCalories
            }.average()

        val completo = mediaCalorias(BodyPart.CUERPO_COMPLETO)
        val brazos = mediaCalorias(BodyPart.BRAZOS)

        assertTrue(
            "Cuerpo completo (%.1f kcal) no supera claramente a brazos (%.1f kcal)".format(completo, brazos),
            completo >= brazos + 5
        )
    }

    @Test
    fun `el perfil ajusta las calorias de la misma lista`() {
        val ejercicios = listOf(
            ExerciseCatalog.getExerciseById("squats_bodyweight")!!
                .copy(defaultDurationSeconds = 30)
        )

        val ligero = repository.estimateSessionCalories(
            exercises = ejercicios,
            restSecondsBetween = 20,
            targetDurationMinutes = 1,
            profile = UserProfile(weightKg = 55f)
        )
        val pesado = repository.estimateSessionCalories(
            exercises = ejercicios,
            restSecondsBetween = 20,
            targetDurationMinutes = 1,
            profile = UserProfile(weightKg = 95f)
        )

        assertTrue(
            "El perfil no ajusta el gasto: 55 kg=$ligero, 95 kg=$pesado",
            pesado > ligero
        )
    }
}
