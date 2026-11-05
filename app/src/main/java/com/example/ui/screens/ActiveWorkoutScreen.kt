package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Exercise
import com.example.ui.components.CircularTimerView
import com.example.ui.components.ExerciseDetailSheet
import com.example.ui.components.ExerciseGuideCard
import com.example.ui.components.ExerciseMediaView
import com.example.ui.components.RestIntervalOverlay
import com.example.ui.components.VoiceCoachSettingsSheet
import com.example.ui.displayName
import com.example.ui.viewmodel.ActiveWorkoutUiState
import com.example.util.AudioCueMode
import com.example.util.VoiceCoachUiState

@Composable
fun ActiveWorkoutScreen(
    state: ActiveWorkoutUiState,
    voiceState: VoiceCoachUiState,
    currentVoiceMessage: String?,
    isSoundCuesEnabled: Boolean = true,
    onToggleSoundCues: () -> Unit = {},
    onPreviewSoundCue: () -> Unit = {},
    onSelectAudioCueMode: (AudioCueMode) -> Unit = {},
    onToggleVoiceCoach: () -> Unit,
    onSelectVoice: (String) -> Unit,
    onSelectVoiceRate: (Float) -> Unit,
    onSelectVoicePitch: (Float) -> Unit,
    onPreviewVoice: () -> Unit,
    onTogglePause: () -> Unit,
    onStartStep: () -> Unit,
    onSkipRest: () -> Unit,
    onAdd20SecondsToRest: () -> Unit,
    onNextExercise: () -> Unit,
    onPreviousExercise: () -> Unit,
    onCancelWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showVoiceSettings by remember { mutableStateOf(false) }
    var inspectedExercise by remember { mutableStateOf<Exercise?>(null) }

    // La pantalla debe permanecer encendida durante toda la rutina.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Intercept back button to confirm exit
    BackHandler {
        showExitDialog = true
    }

    val currentEx = state.currentExercise
    val plan = state.plan

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // En horizontal el contenido se reparte en dos columnas: la
        // demostración a la izquierda y el cronómetro con los controles a la
        // derecha. En vertical se mantiene la columna de siempre.
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = if (isLandscape) 6.dp else 14.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Nav Row: Exit, Title, Voice Coach Toggle, Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.testTag("exit_workout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.workout_exit),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = stringResource(
                        R.string.workout_counter,
                        (state.currentExerciseIndex + 1).coerceAtMost(plan?.exercises?.size ?: 1),
                        plan?.exercises?.size ?: 1
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Voice Coach Toggle Button
                    IconButton(
                        onClick = onToggleVoiceCoach,
                        modifier = Modifier.testTag("toggle_voice_coach_button")
                    ) {
                        Icon(
                            imageVector = if (voiceState.isEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (voiceState.isEnabled) {
                                stringResource(R.string.workout_mute_voice)
                            } else {
                                stringResource(R.string.workout_unmute_voice)
                            },
                            tint = if (voiceState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Voice Coach Settings (motor, voz y velocidad)
                    IconButton(
                        onClick = { showVoiceSettings = true },
                        modifier = Modifier.testTag("voice_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsVoice,
                            contentDescription = stringResource(R.string.workout_voice_settings),
                            tint = if (voiceState.errorMessage != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // Technique Info Sheet
                    IconButton(
                        onClick = { inspectedExercise = currentEx },
                        modifier = Modifier.testTag("exercise_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.workout_exercise_info),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { state.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // Aviso cuando la voz está activada pero el dispositivo no tiene voces en español
            if (voiceState.isEnabled && voiceState.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { showVoiceSettings = true }
                        .testTag("voice_issue_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsVoice,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.workout_voice_issue_banner),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        WorkoutHeader(exercise = currentEx)
                        Spacer(modifier = Modifier.height(6.dp))
                        WorkoutMedia(
                            exercise = currentEx,
                            isPaused = state.isPaused,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .heightIn(min = 90.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        WaitingHint(visible = state.isWaitingToStart)
                        CircularTimerView(
                            remainingSeconds = state.remainingSeconds,
                            totalSeconds = state.totalSecondsForCurrentStep,
                            isPaused = state.isPaused || state.isWaitingToStart,
                            diameter = 150.dp,
                            modifier = Modifier.testTag("circular_timer")
                        )
                        WorkoutControls(
                            state = state,
                            onPreviousExercise = onPreviousExercise,
                            onTogglePause = onTogglePause,
                            onStartStep = onStartStep,
                            onNextExercise = onNextExercise,
                            compact = true
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                WorkoutHeader(exercise = currentEx)
                Spacer(modifier = Modifier.height(10.dp))
                WorkoutMedia(
                    exercise = currentEx,
                    isPaused = state.isPaused,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 160.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                WaitingHint(visible = state.isWaitingToStart)
                CircularTimerView(
                    remainingSeconds = state.remainingSeconds,
                    totalSeconds = state.totalSecondsForCurrentStep,
                    isPaused = state.isPaused || state.isWaitingToStart,
                    modifier = Modifier.testTag("circular_timer")
                )
                WorkoutControls(
                    state = state,
                    onPreviousExercise = onPreviousExercise,
                    onTogglePause = onTogglePause,
                    onStartStep = onStartStep,
                    onNextExercise = onNextExercise,
                    compact = false
                )
            }
        }

        // Rest Interval Overlay (when resting between exercises)
        AnimatedVisibility(
            visible = state.isResting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            RestIntervalOverlay(
                remainingSeconds = state.remainingSeconds,
                nextExercise = state.currentExercise,
                onAdd20Seconds = onAdd20SecondsToRest,
                onSkipRest = onSkipRest
            )
        }

        // Exercise Technique Detail Sheet
        inspectedExercise?.let { exercise ->
            ExerciseDetailSheet(
                exercise = exercise,
                onDismiss = { inspectedExercise = null }
            )
        }

        // Voice Coach Settings Sheet
        if (showVoiceSettings) {
            VoiceCoachSettingsSheet(
                state = voiceState,
                isSoundCuesEnabled = isSoundCuesEnabled,
                onToggleSoundCues = onToggleSoundCues,
                onPreviewSoundCue = onPreviewSoundCue,
                onSelectAudioCueMode = onSelectAudioCueMode,
                onDismiss = { showVoiceSettings = false },
                onToggleEnabled = onToggleVoiceCoach,
                onSelectVoice = onSelectVoice,
                onSelectRate = onSelectVoiceRate,
                onSelectPitch = onSelectVoicePitch,
                onPreview = onPreviewVoice
            )
        }

        // Exit Workout Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(stringResource(R.string.workout_exit_title)) },
                text = { Text(stringResource(R.string.workout_exit_body)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onCancelWorkout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.workout_exit_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text(stringResource(R.string.workout_exit_cancel))
                    }
                }
            )
        }
    }
}

/** Badge de calentamiento, nombre del ejercicio y duración/reps sugeridas. */
@Composable
private fun WorkoutHeader(exercise: Exercise?) {
    if (exercise == null) return

    if (exercise.isWarmUp) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_warmup),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }

    Text(
        text = exercise.displayName(),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(4.dp))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ) {
        Text(
            text = if (exercise.isTimeBased) {
                stringResource(R.string.workout_seconds_suggested, exercise.defaultDurationSeconds)
            } else {
                stringResource(R.string.unit_reps, exercise.defaultReps)
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Demostración vectorial (o los pasos escritos si no hay animación). Ocupa el
 * espacio libre entre la cabecera y el cronómetro.
 */
@Composable
private fun WorkoutMedia(
    exercise: Exercise?,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    if (exercise == null) {
        Spacer(modifier = modifier)
        return
    }

    if (exercise.lottieAsset != null) {
        ExerciseMediaView(
            exercise = exercise,
            isPaused = isPaused,
            modifier = modifier.testTag("exercise_media_container")
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            ExerciseGuideCard(
                exercise = exercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_guide_card")
            )
        }
    }
}

/** Aviso de que el cronómetro espera al play del usuario. */
@Composable
private fun WaitingHint(visible: Boolean) {
    if (!visible) return

    Text(
        text = stringResource(R.string.workout_waiting_hint),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(bottom = 6.dp)
            .testTag("waiting_to_start_hint")
    )
}

/** Fila de controles: anterior, play/pausa y siguiente. */
@Composable
private fun WorkoutControls(
    state: ActiveWorkoutUiState,
    onPreviousExercise: () -> Unit,
    onTogglePause: () -> Unit,
    onStartStep: () -> Unit,
    onNextExercise: () -> Unit,
    compact: Boolean
) {
    val sideSize = if (compact) 46.dp else 54.dp
    val mainSize = if (compact) 60.dp else 72.dp
    val sideIcon = if (compact) 22.dp else 26.dp
    val mainIcon = if (compact) 30.dp else 36.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (compact) 2.dp else 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Exercise
        FilledIconButton(
            onClick = onPreviousExercise,
            enabled = state.currentExerciseIndex > 0,
            modifier = Modifier
                .size(sideSize)
                .testTag("prev_exercise_button"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.workout_prev),
                modifier = Modifier.size(sideIcon)
            )
        }

        // Main Play/Pause Button
        FilledIconButton(
            onClick = if (state.isWaitingToStart) onStartStep else onTogglePause,
            modifier = Modifier
                .size(mainSize)
                .testTag("toggle_pause_button"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (state.isPaused || state.isWaitingToStart) {
                    Icons.Default.PlayArrow
                } else {
                    Icons.Default.Pause
                },
                contentDescription = when {
                    state.isWaitingToStart -> stringResource(R.string.workout_start_exercise)
                    state.isPaused -> stringResource(R.string.workout_resume)
                    else -> stringResource(R.string.workout_pause)
                },
                modifier = Modifier.size(mainIcon)
            )
        }

        // Next Exercise
        FilledIconButton(
            onClick = onNextExercise,
            modifier = Modifier
                .size(sideSize)
                .testTag("next_exercise_button"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.workout_next),
                modifier = Modifier.size(sideIcon)
            )
        }
    }
}
