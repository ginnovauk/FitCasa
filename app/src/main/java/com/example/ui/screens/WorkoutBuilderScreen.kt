package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BodyPart
import com.example.data.model.Exercise
import com.example.data.model.IntensityLevel
import com.example.data.model.WorkoutPlan
import com.example.ui.components.BodyPartCard
import com.example.ui.components.DurationSelectorRow
import com.example.ui.components.ExerciseDetailSheet
import com.example.ui.components.IntensitySelectorRow
import com.example.ui.description
import com.example.ui.displayName
import com.example.ui.displayTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderScreen(
    plan: WorkoutPlan?,
    selectedBodyPart: BodyPart,
    selectedIntensity: IntensityLevel,
    selectedDurationMinutes: Int,
    onSelectBodyPart: (BodyPart) -> Unit,
    onSelectIntensity: (IntensityLevel) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onMoveExercise: (index: Int, direction: Int) -> Unit,
    onStartWorkout: (WorkoutPlan) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.builder_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("builder_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.builder_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (plan != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { onStartWorkout(plan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_workout_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (plan.exercises.any { it.isWarmUp }) {
                                    val principales = plan.exercises.count { !it.isWarmUp }
                                    stringResource(
                                        R.string.builder_start_warmup,
                                        principales,
                                        plan.targetDurationMinutes
                                    )
                                } else {
                                    stringResource(
                                        R.string.builder_start,
                                        plan.exercises.size,
                                        plan.targetDurationMinutes
                                    )
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Step 1: Zona del Cuerpo
            item {
                SectionHeader(title = stringResource(R.string.builder_step_body))
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BodyPart.values().forEach { bp ->
                        BodyPartCard(
                            bodyPart = bp,
                            isSelected = bp == selectedBodyPart,
                            onClick = { onSelectBodyPart(bp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Step 2: Nivel de Intensidad
            item {
                SectionHeader(title = stringResource(R.string.builder_step_intensity))
                Spacer(modifier = Modifier.height(8.dp))
                IntensitySelectorRow(
                    selectedIntensity = selectedIntensity,
                    onSelect = onSelectIntensity
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = selectedIntensity.description(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Step 3: Tiempo Disponible
            item {
                SectionHeader(title = stringResource(R.string.builder_step_time))
                Spacer(modifier = Modifier.height(8.dp))
                DurationSelectorRow(
                    selectedMinutes = selectedDurationMinutes,
                    onSelect = onSelectDuration
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Plan Summary Card
            if (plan != null) {
                item {
                    SectionHeader(title = stringResource(R.string.builder_plan_summary))
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = plan.displayTitle(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Anchura repartida a partes iguales: con SpaceBetween y
                            // valores grandes (25 min), el badge del descanso se salía.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBadge(
                                    icon = Icons.Default.FitnessCenter,
                                    label = stringResource(R.string.builder_metric_exercises, plan.exercises.size),
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBadge(
                                    icon = Icons.Default.LocalFireDepartment,
                                    label = stringResource(R.string.builder_metric_kcal, plan.estimatedCalories),
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBadge(
                                    icon = Icons.Default.Speed,
                                    label = stringResource(R.string.builder_metric_rest, plan.restBetweenSeconds),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.builder_sequence_hint),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(plan.exercises) { index, exercise ->
                    ExerciseListItem(
                        index = index + 1,
                        exercise = exercise,
                        canMoveUp = index > 0,
                        canMoveDown = index < plan.exercises.lastIndex,
                        onMoveUp = { onMoveExercise(index, -1) },
                        onMoveDown = { onMoveExercise(index, +1) },
                        onClick = { inspectedExercise = exercise }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    inspectedExercise?.let { exercise ->
        ExerciseDetailSheet(
            exercise = exercise,
            onDismiss = { inspectedExercise = null }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun MetricBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ExerciseListItem(
    index: Int,
    exercise: Exercise,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("exercise_item_${exercise.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$index",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.displayName(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (exercise.isWarmUp) {
                    // En su propio renglón: junto al nombre, nombres largos
                    // empujaban la etiqueta y se partía a mitad de palabra.
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = stringResource(R.string.builder_warmup_badge),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = if (exercise.isTimeBased) {
                        stringResource(R.string.unit_seconds, exercise.defaultDurationSeconds)
                    } else {
                        stringResource(R.string.unit_reps, exercise.defaultReps)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reordenar: subir/bajar sin salir de la lista
            Column {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("move_up_${exercise.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.builder_move_up, exercise.displayName()),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("move_down_${exercise.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.builder_move_down, exercise.displayName()),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.builder_see_technique),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
