package com.example.data.repository

import com.example.data.local.WorkoutDao
import com.example.data.local.WorkoutEntity
import com.example.data.model.BodyPart
import com.example.data.model.Exercise
import com.example.data.model.IntensityLevel
import com.example.data.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

class WorkoutRepository(
    private val workoutDao: WorkoutDao
) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    val totalWorkoutsCount: Flow<Int> = workoutDao.getWorkoutCount()

    val totalMinutes: Flow<Int> = workoutDao.getTotalDurationSeconds().map { totalSecs ->
        (totalSecs ?: 0) / 60
    }

    val totalCalories: Flow<Int> = workoutDao.getTotalCaloriesBurned().map { it ?: 0 }

    val currentStreakDays: Flow<Int> = allWorkouts.map { list ->
        calculateStreak(list)
    }

    suspend fun saveWorkout(record: WorkoutEntity): Long {
        return workoutDao.insertWorkout(record)
    }

    suspend fun deleteWorkout(id: Long) {
        workoutDao.deleteWorkout(id)
    }

    fun generateWorkoutPlan(
        bodyPart: BodyPart,
        intensity: IntensityLevel,
        targetDurationMinutes: Int
    ): WorkoutPlan {
        val pool = ExerciseCatalog.getExercisesByBodyPart(bodyPart)
            .filter { !it.isWarmUp }
        
        // Target time in seconds
        val targetSeconds = targetDurationMinutes * 60
        
        // Exercise time depends on intensity multiplier
        val workSec = (30 * intensity.workRatioMultiplier).roundToInt().coerceAtLeast(20)
        val restSec = intensity.restDurationSeconds
        val totalPerExercise = workSec + restSec

        // Resolvemos cuántos ejercicios caben en el tiempo elegido:
        // n * trabajo + (n - 1) * descanso = objetivo  ->  n = (objetivo + descanso) / (trabajo + descanso)
        val exerciseCount = ((targetSeconds + restSec) / totalPerExercise).coerceIn(4, 28)
        
        // Select exercises balancing intensity and variety
        val selectedExercises = mutableListOf<Exercise>()
        // Ordenamos por cercanía al nivel elegido (mismo nivel primero) y
        // aleatorizamos dentro de cada grupo para que la rutina no sea siempre igual.
        val primaryMatches = pool.filter { it.bodyPart == bodyPart }
            .groupBy { abs(it.intensity.ordinal - intensity.ordinal) }
            .toSortedMap()
            .values
            .flatMap { it.shuffled() }
        val fallbackMatches = pool.filter { it.bodyPart != bodyPart }
            .groupBy { abs(it.intensity.ordinal - intensity.ordinal) }
            .toSortedMap()
            .values
            .flatMap { it.shuffled() }

        val orderedPool = primaryMatches + fallbackMatches
        
        for (i in 0 until exerciseCount) {
            val exerciseTemplate = orderedPool[i % orderedPool.size]
            val adjusted = exerciseTemplate.copy(
                defaultDurationSeconds = workSec,
                defaultReps = (exerciseTemplate.defaultReps * intensity.workRatioMultiplier).roundToInt()
            )
            selectedExercises.add(adjusted)
        }

        // Bloque de calentamiento común, en orden fijo, antes del trabajo principal.
        val warmUp = ExerciseCatalog.warmUpExercises

        // Gasto calórico (Opción A): valores MET calibrados por ejercicio.
        // Solo el trabajo principal; el calentamiento de movilidad aporta muy
        // poco y no debe inflar la estimación.
        val estimatedCalories = estimateSessionCalories(
            exercises = selectedExercises,
            restSecondsBetween = restSec,
            targetDurationMinutes = targetDurationMinutes
        )

        val title = "Rutina de ${bodyPart.displayName} - ${intensity.displayName}"

        return WorkoutPlan(
            title = title,
            bodyPart = bodyPart,
            intensity = intensity,
            targetDurationMinutes = targetDurationMinutes,
            exercises = warmUp + selectedExercises,
            estimatedCalories = estimatedCalories,
            restBetweenSeconds = restSec
        )
    }

    /**
     * Estima el gasto calórico de una sesión (Opción A: valores MET por ejercicio).
     *
     * Suma el tiempo activo con el ritmo kcal/min ya calibrado de cada ejercicio
     * (derivado de MET a 70 kg de referencia) más un ritmo suave de recuperación
     * durante los descansos. El nivel de intensidad actúa sobre la duración de
     * cada bloque (workSec) al generar el plan, así que aquí no se vuelve a
     * multiplicar: hacerlo contaría el mismo esfuerzo dos veces.
     *
     * @param exercises solo el trabajo principal, sin el bloque de calentamiento
     * @param restSecondsBetween duración de cada descanso entre ejercicios
     * @param targetDurationMinutes duración objetivo, usada solo para el suelo de seguridad
     */
    fun estimateSessionCalories(
        exercises: List<Exercise>,
        restSecondsBetween: Int,
        targetDurationMinutes: Int
    ): Int {
        if (exercises.isEmpty()) return 0

        // Tiempo activo con el ritmo de cada ejercicio.
        val activeKcal = exercises.sumOf { ex ->
            (ex.defaultDurationSeconds / 60.0) * ex.caloriesPerMinute.toDouble()
        }

        // Recuperación entre series: sigue quemando algo, y evita que rutinas
        // con mucho descanso queden artificialmente por debajo de su duración.
        val restCount = (exercises.size - 1).coerceAtLeast(0)
        val restMinutes = restCount * restSecondsBetween / 60.0
        val restKcal = restMinutes * REST_KCAL_PER_MINUTE

        // Suelo de seguridad: solo evita estimaciones absurdamente bajas en
        // planes muy cortos, sin ahogar la diferencia entre grupos musculares.
        return (activeKcal + restKcal)
            .roundToInt()
            .coerceAtLeast(targetDurationMinutes * MIN_KCAL_PER_MINUTE)
    }

    private companion object {
        /** Ritmo de recuperación durante los descansos, kcal/min (≈2.5 MET a 70 kg). */
        const val REST_KCAL_PER_MINUTE = 2.9

        /** Suelo de seguridad en kcal por minuto objetivo. */
        const val MIN_KCAL_PER_MINUTE = 3
    }

    private fun calculateStreak(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0
        
        val daysWithWorkouts = workouts.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.dateMillis
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()

        val cal = Calendar.getInstance()
        var streak = 0
        
        // Check if user worked out today
        val todayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        val workedOutToday = daysWithWorkouts.contains(todayKey)

        if (!workedOutToday) {
            // Check yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            if (!daysWithWorkouts.contains(yesterdayKey)) {
                return 0
            }
        }

        // Reset and walk backward
        val walker = Calendar.getInstance()
        if (!workedOutToday) {
            walker.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = "${walker.get(Calendar.YEAR)}-${walker.get(Calendar.DAY_OF_YEAR)}"
            if (daysWithWorkouts.contains(key)) {
                streak++
                walker.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }
}
