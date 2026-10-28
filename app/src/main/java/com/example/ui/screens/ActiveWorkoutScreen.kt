package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.ui.components.CircularTimerView
import com.example.ui.components.ExerciseDetailSheet
import com.example.ui.components.ExerciseGuideCard
import com.example.ui.components.ExerciseMediaView
import com.example.ui.components.RestIntervalOverlay
import com.example.ui.components.VoiceCoachSettingsSheet
import com.example.ui.viewmodel.ActiveWorkoutUiState
import com.example.util.VoiceCoachUiState

@Composable
fun ActiveWorkoutScreen(
    state: ActiveWorkoutUiState,
    voiceState: VoiceCoachUiState,
    currentVoiceMessage: String?,
    onToggleVoiceCoach: () -> Unit,
    onSelectVoice: (String) -> Unit,
    onSelectVoiceRate: (Float) -> Unit,
    onPreviewVoice: () -> Unit,
    onTogglePause: () -> Unit,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
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
                        contentDescription = "Salir de la rutina",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "${(state.currentExerciseIndex + 1).coerceAtMost(plan?.exercises?.size ?: 1)} de ${plan?.exercises?.size ?: 1}",
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
                            contentDescription = if (voiceState.isEnabled) "Silenciar Entrenador por Voz" else "Activar Entrenador por Voz",
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
                            contentDescription = "Configurar la voz del entrenador",
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
                            contentDescription = "Instrucciones de ejercicio",
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
                            text = "La voz necesita configuración. Toca para revisarla.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exercise Title & Reps/Duration Info
            if (currentEx != null) {
                if (currentEx.isWarmUp) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "CALENTAMIENTO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = currentEx.name,
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
                        text = if (currentEx.isTimeBased) "${currentEx.defaultDurationSeconds} seg sugeridos" else "${currentEx.defaultReps} repeticiones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Demostración vectorial: ocupa el espacio libre entre el título y
            // el temporizador, sin empujar los controles fuera. Los ejercicios
            // sin animación dejan ese hueco para que el temporizador no baile.
            val currentExercise = currentEx
            if (currentExercise != null && currentExercise.lottieAsset != null) {
                ExerciseMediaView(
                    exercise = currentExercise,
                    isPaused = state.isPaused,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 160.dp)
                        .testTag("exercise_media_container")
                )
            } else if (currentExercise != null) {
                // Sin animación: los pasos escritos ocupan el mismo hueco, así
                // la pantalla no queda vacía y el temporizador no se mueve.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ExerciseGuideCard(
                        exercise = currentExercise,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_guide_card")
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Circular Countdown Timer
            CircularTimerView(
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSecondsForCurrentStep,
                isPaused = state.isPaused,
                modifier = Modifier.testTag("circular_timer")
            )

            // Controls Row (Previous, Play/Pause, Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Exercise
                FilledIconButton(
                    onClick = onPreviousExercise,
                    enabled = state.currentExerciseIndex > 0,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("prev_exercise_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(26.dp))
                }

                // Main Play/Pause Button
                FilledIconButton(
                    onClick = onTogglePause,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("toggle_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (state.isPaused) "Reanudar" else "Pausar",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Exercise
                FilledIconButton(
                    onClick = onNextExercise,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("next_exercise_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(26.dp))
                }
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
                onDismiss = { showVoiceSettings = false },
                onToggleEnabled = onToggleVoiceCoach,
                onSelectVoice = onSelectVoice,
                onSelectRate = onSelectVoiceRate,
                onPreview = onPreviewVoice
            )
        }

        // Exit Workout Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("¿Deseas pausar o salir?") },
                text = { Text("Si abandonas el entrenamiento ahora, tu sesión actual no quedará registrada en el historial de estadísticas.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onCancelWorkout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Salir de todas formas")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Continuar entrenando")
                    }
                }
            )
        }
    }
}
