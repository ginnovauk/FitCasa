package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WorkoutDatabase
import com.example.data.local.WorkoutEntity
import com.example.data.local.UserProfileStore
import com.example.data.model.BodyPart
import com.example.data.model.Exercise
import com.example.data.model.IntensityLevel
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutPlan
import com.example.data.repository.WorkoutRepository
import com.example.util.VoiceCoachUiState
import com.example.util.WorkoutVoiceCoach
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val plan: WorkoutPlan? = null,
    val currentExerciseIndex: Int = 0,
    val remainingSeconds: Int = 30,
    val totalSecondsForCurrentStep: Int = 30,
    val isResting: Boolean = false,
    val isPaused: Boolean = false,
    val isWaitingToStart: Boolean = false,
    val isCompleted: Boolean = false,
    val elapsedTotalSeconds: Int = 0,
    val exercisesCompletedCount: Int = 0,
    val soundCueCounter: Int = 0
) {
    val currentExercise: Exercise?
        get() = plan?.exercises?.getOrNull(currentExerciseIndex)

    val nextExercise: Exercise?
        get() = plan?.exercises?.getOrNull(currentExerciseIndex + 1)

    val progressFraction: Float
        get() {
            val total = plan?.exercises?.size ?: 1
            return (currentExerciseIndex.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
}

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoutRepository
    private var timerJob: Job? = null
    private val vibrator: Vibrator?
    private val voiceCoach: WorkoutVoiceCoach

    init {
        val database = WorkoutDatabase.getDatabase(application)
        repository = WorkoutRepository(database.workoutDao())
        voiceCoach = WorkoutVoiceCoach(application)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val workoutHistory: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWorkouts: StateFlow<Int> = repository.totalWorkoutsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMinutes: StateFlow<Int> = repository.totalMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCalories: StateFlow<Int> = repository.totalCalories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val streakDays: StateFlow<Int> = repository.currentStreakDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val voiceCoachState: StateFlow<VoiceCoachUiState> = voiceCoach.uiState
    val currentVoiceMessage: StateFlow<String?> = voiceCoach.currentVoiceMessage

    fun toggleVoiceCoach() {
        voiceCoach.toggleVoice()
    }

    fun selectVoiceOption(voiceId: String) {
        voiceCoach.selectVoice(voiceId)
    }

    fun setVoiceRate(rate: Float) {
        voiceCoach.setSpeechRate(rate)
    }

    fun setVoicePitch(pitch: Float) {
        voiceCoach.setSpeechPitch(pitch)
    }

    fun previewVoice() {
        voiceCoach.previewVoice()
    }

    private val _activeWorkoutState = MutableStateFlow(ActiveWorkoutUiState())
    val activeWorkoutState: StateFlow<ActiveWorkoutUiState> = _activeWorkoutState.asStateFlow()

    // Configurator selection state
    private val _selectedBodyPart = MutableStateFlow(BodyPart.CUERPO_COMPLETO)
    val selectedBodyPart: StateFlow<BodyPart> = _selectedBodyPart.asStateFlow()

    private val _selectedIntensity = MutableStateFlow(IntensityLevel.PRINCIPIANTE)
    val selectedIntensity: StateFlow<IntensityLevel> = _selectedIntensity.asStateFlow()

    private val _selectedDurationMinutes = MutableStateFlow(15)
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    // Previews configured plan
    private val _configuredPlan = MutableStateFlow<WorkoutPlan?>(null)
    val configuredPlan: StateFlow<WorkoutPlan?> = _configuredPlan.asStateFlow()

    // Perfil del usuario: ajusta las kcal de los planes nuevos.
    private val profileStore = UserProfileStore(application)
    private val _profile = MutableStateFlow(profileStore.load())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    init {
        updateConfiguredPlan()
    }

    fun selectBodyPart(bodyPart: BodyPart) {
        _selectedBodyPart.value = bodyPart
        updateConfiguredPlan()
    }

    fun selectIntensity(intensity: IntensityLevel) {
        _selectedIntensity.value = intensity
        updateConfiguredPlan()
    }

    fun selectDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes
        updateConfiguredPlan()
    }

    fun updateConfiguredPlan() {
        val plan = repository.generateWorkoutPlan(
            bodyPart = _selectedBodyPart.value,
            intensity = _selectedIntensity.value,
            targetDurationMinutes = _selectedDurationMinutes.value,
            profile = _profile.value
        )
        _configuredPlan.value = plan
    }

    /**
     * Guarda el perfil y regenera el plan visible para que las kcal reflejen
     * el nuevo cuerpo. El orden manual de los ejercicios se pierde al regenerar.
     */
    fun saveProfile(profile: UserProfile) {
        val clean = profile.sanitized()
        _profile.value = clean
        profileStore.save(clean)
        updateConfiguredPlan()
    }

    /**
     * Mueve un ejercicio del plan visible una posición arriba (-1) o abajo (+1).
     * Trabaja sobre el plan ya generado: no lo regenera, para no perder el orden elegido.
     */
    fun moveExercise(index: Int, direction: Int) {
        val current = _configuredPlan.value ?: return
        val target = index + direction
        if (index !in current.exercises.indices || target !in current.exercises.indices) return

        val reordered = current.exercises.toMutableList().apply {
            val moved = removeAt(index)
            add(target, moved)
        }
        _configuredPlan.value = current.copy(exercises = reordered)
    }

    fun startWorkout(plan: WorkoutPlan) {
        val firstExercise = plan.exercises.firstOrNull() ?: return
        timerJob?.cancel()

        _activeWorkoutState.value = ActiveWorkoutUiState(
            plan = plan,
            currentExerciseIndex = 0,
            remainingSeconds = firstExercise.defaultDurationSeconds,
            totalSecondsForCurrentStep = firstExercise.defaultDurationSeconds,
            isResting = false,
            isPaused = false,
            isWaitingToStart = true,
            isCompleted = false,
            elapsedTotalSeconds = 0,
            exercisesCompletedCount = 0
        )

        // El entrenador avisa del ejercicio y espera: así da tiempo a colocarse
        // antes de que empiece a correr el tiempo.
        vibrateShort(120)
        voiceCoach.speakPrepareNext(firstExercise.name)
        startTimerLoop()
    }

    /** El usuario ya está en posición: empieza a contar el ejercicio actual. */
    fun startCurrentStep() {
        val state = _activeWorkoutState.value
        if (!state.isWaitingToStart) return
        val exercise = state.currentExercise ?: return
        val repOrDuration = if (exercise.isTimeBased) {
            "${exercise.defaultDurationSeconds} segundos"
        } else {
            "${exercise.defaultReps} repeticiones"
        }
        voiceCoach.speakExerciseStart(exercise.name, repOrDuration)
        _activeWorkoutState.update {
            it.copy(
                isWaitingToStart = false,
                isResting = false,
                remainingSeconds = exercise.defaultDurationSeconds,
                totalSecondsForCurrentStep = exercise.defaultDurationSeconds
            )
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Los segundos se cuentan contra un reloj monótono: cada vuelta del
            // bucle tardaba algo más de un segundo y la cuenta atrás se retrasaba,
            // así que el "tres, dos, uno" de la voz no cuadraba con la pantalla.
            var nextTick = SystemClock.elapsedRealtime() + TICK_MS
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                // Si el móvil estuvo bloqueado, no se aceleran los segundos de golpe.
                if (now - nextTick > 2 * TICK_MS) {
                    nextTick = now + TICK_MS
                }
                delay((nextTick - SystemClock.elapsedRealtime()).coerceAtLeast(0))
                nextTick += TICK_MS
                val currentState = _activeWorkoutState.value
                if (currentState.isPaused || currentState.isCompleted ||
                    currentState.isWaitingToStart || currentState.plan == null
                ) {
                    continue
                }

                val newRemaining = currentState.remainingSeconds - 1
                val newElapsed = currentState.elapsedTotalSeconds + 1

                // Voice cues during active workout
                if (!currentState.isResting) {
                    val halfTime = currentState.totalSecondsForCurrentStep / 2
                    // En pasos cortos el aviso de la mitad chocaría con el de los
                    // últimos diez segundos: solo se dice en pasos largos.
                    if (newRemaining == halfTime && halfTime > 20) {
                        voiceCoach.speakHalfway()
                    } else if (newRemaining == 10 && currentState.totalSecondsForCurrentStep > 15) {
                        voiceCoach.speakTenSecondsLeft()
                    } else if (newRemaining in 1..3) {
                        vibrateShort(60)
                        voiceCoach.speakCountdownNumber(newRemaining)
                    }
                } else {
                    // Voice cues during rest
                    if (newRemaining == 4) {
                        val nextEx = currentState.currentExercise
                        if (nextEx != null) {
                            voiceCoach.speakPrepareNext(nextEx.name)
                        }
                    } else if (newRemaining in 1..3) {
                        vibrateShort(50)
                    }
                }

                if (newRemaining <= 0) {
                    handleStepFinished(currentState, newElapsed)
                } else {
                    _activeWorkoutState.update {
                        it.copy(
                            remainingSeconds = newRemaining,
                            elapsedTotalSeconds = newElapsed
                        )
                    }
                }
            }
        }
    }

    private fun handleStepFinished(state: ActiveWorkoutUiState, updatedElapsed: Int) {
        val plan = state.plan ?: return

        if (state.isResting) {
            // Descanso terminado: se avisa del ejercicio y se espera al play.
            val exercise = plan.exercises[state.currentExerciseIndex]
            vibrateDouble()
            voiceCoach.speakPrepareNext(exercise.name)
            _activeWorkoutState.update {
                it.copy(
                    isResting = false,
                    isWaitingToStart = true,
                    remainingSeconds = exercise.defaultDurationSeconds,
                    totalSecondsForCurrentStep = exercise.defaultDurationSeconds,
                    elapsedTotalSeconds = updatedElapsed
                )
            }
        } else {
            // Finished active exercise
            val newCompletedCount = state.exercisesCompletedCount + 1
            val nextIdx = state.currentExerciseIndex + 1

            if (nextIdx < plan.exercises.size) {
                // Enter rest period
                vibrateShort(150)
                val nextExercise = plan.exercises[nextIdx]
                voiceCoach.speakRestStart(plan.restBetweenSeconds, nextExercise.name)
                _activeWorkoutState.update {
                    it.copy(
                        currentExerciseIndex = nextIdx,
                        isResting = true,
                        remainingSeconds = plan.restBetweenSeconds,
                        totalSecondsForCurrentStep = plan.restBetweenSeconds,
                        elapsedTotalSeconds = updatedElapsed,
                        exercisesCompletedCount = newCompletedCount
                    )
                }
            } else {
                // Workout completed!
                finishWorkout(state, updatedElapsed, newCompletedCount)
            }
        }
    }

    private fun finishWorkout(state: ActiveWorkoutUiState, totalSeconds: Int, completedExercises: Int) {
        timerJob?.cancel()
        vibrateTriple()

        val plan = state.plan ?: return
        val calculatedCalories = (plan.estimatedCalories * (completedExercises.toFloat() / plan.exercises.size.toFloat()))
            .toInt()
            .coerceAtLeast(15)

        val record = WorkoutEntity(
            dateMillis = System.currentTimeMillis(),
            workoutTitle = plan.title,
            bodyPartId = plan.bodyPart.id,
            intensityId = plan.intensity.id,
            durationSeconds = totalSeconds,
            caloriesBurned = calculatedCalories,
            exercisesCompleted = completedExercises,
            totalExercises = plan.exercises.size
        )

        viewModelScope.launch {
            repository.saveWorkout(record)
        }

        val minutes = (totalSeconds / 60).coerceAtLeast(1)
        voiceCoach.speakWorkoutCompleted(calculatedCalories, minutes)

        _activeWorkoutState.update {
            it.copy(
                isCompleted = true,
                remainingSeconds = 0,
                elapsedTotalSeconds = totalSeconds,
                exercisesCompletedCount = completedExercises
            )
        }
    }

    fun togglePause() {
        // Mientras espera al play no hay nada que pausar.
        if (_activeWorkoutState.value.isWaitingToStart) return
        val willPause = !_activeWorkoutState.value.isPaused
        _activeWorkoutState.update { it.copy(isPaused = willPause) }
        if (willPause) {
            voiceCoach.speakPaused()
        } else {
            voiceCoach.speakResumed()
        }
    }

    fun skipRest() {
        val state = _activeWorkoutState.value
        val plan = state.plan ?: return
        if (state.isResting && state.currentExerciseIndex < plan.exercises.size) {
            val nextExercise = plan.exercises[state.currentExerciseIndex]
            voiceCoach.speakPrepareNext(nextExercise.name)
            _activeWorkoutState.update {
                it.copy(
                    isResting = false,
                    isWaitingToStart = true,
                    remainingSeconds = nextExercise.defaultDurationSeconds,
                    totalSecondsForCurrentStep = nextExercise.defaultDurationSeconds
                )
            }
        }
    }

    fun add20SecondsToRest() {
        _activeWorkoutState.update {
            if (it.isResting) {
                val newSeconds = it.remainingSeconds + 20
                it.copy(
                    remainingSeconds = newSeconds,
                    totalSecondsForCurrentStep = it.totalSecondsForCurrentStep + 20
                )
            } else it
        }
    }

    fun nextExercise() {
        val state = _activeWorkoutState.value
        val plan = state.plan ?: return
        val nextIdx = state.currentExerciseIndex + 1
        if (nextIdx < plan.exercises.size) {
            val ex = plan.exercises[nextIdx]
            voiceCoach.speakPrepareNext(ex.name)
            _activeWorkoutState.update {
                it.copy(
                    currentExerciseIndex = nextIdx,
                    isResting = false,
                    isWaitingToStart = true,
                    remainingSeconds = ex.defaultDurationSeconds,
                    totalSecondsForCurrentStep = ex.defaultDurationSeconds
                )
            }
        } else {
            finishWorkout(state, state.elapsedTotalSeconds, state.exercisesCompletedCount + 1)
        }
    }

    fun previousExercise() {
        val state = _activeWorkoutState.value
        val plan = state.plan ?: return
        val prevIdx = (state.currentExerciseIndex - 1).coerceAtLeast(0)
        val ex = plan.exercises[prevIdx]
        voiceCoach.speakPrepareNext(ex.name)
        _activeWorkoutState.update {
            it.copy(
                currentExerciseIndex = prevIdx,
                isResting = false,
                isWaitingToStart = true,
                remainingSeconds = ex.defaultDurationSeconds,
                totalSecondsForCurrentStep = ex.defaultDurationSeconds
            )
        }
    }

    fun cancelWorkout() {
        timerJob?.cancel()
        voiceCoach.stop()
        _activeWorkoutState.value = ActiveWorkoutUiState()
    }

    fun deleteWorkoutRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }

    private fun vibrateShort(millis: Long = 80) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateDouble() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 150), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateTriple() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100, 100, 200), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        voiceCoach.shutdown()
    }
}

private const val TICK_MS = 1000L
