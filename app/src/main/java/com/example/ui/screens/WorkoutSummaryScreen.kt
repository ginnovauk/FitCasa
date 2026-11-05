package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WorkoutPlan
import com.example.ui.displayTitle
import com.example.ui.viewmodel.ActiveWorkoutUiState

@Composable
fun WorkoutSummaryScreen(
    state: ActiveWorkoutUiState,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plan = state.plan
    val durationMinutes = state.elapsedTotalSeconds / 60
    val durationSeconds = state.elapsedTotalSeconds % 60
    val formattedDuration = String.format("%02d:%02d", durationMinutes, durationSeconds)

    val calories = ((plan?.estimatedCalories?.toFloat() ?: 60f) *
            (state.exercisesCompletedCount.toFloat() / (plan?.exercises?.size?.toFloat() ?: 1f)))
        .toInt()
        .coerceAtLeast(if (state.exercisesCompletedCount > 0) 15 else 0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // En horizontal, resumen a la izquierda y métricas/acción a la derecha:
        // en una sola columna el contenido no cabe en 393 dp de alto.
        if (maxWidth > maxHeight) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SummaryHeader(plan = plan, compact = true)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SummaryMetrics(
                        duration = formattedDuration,
                        calories = calories,
                        completed = state.exercisesCompletedCount,
                        total = plan?.exercises?.size ?: 0
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SummarySavedNote()
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryFinishButton(onFinish = onFinish)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                SummaryHeader(plan = plan, compact = false)
                Spacer(modifier = Modifier.height(28.dp))
                SummaryMetrics(
                    duration = formattedDuration,
                    calories = calories,
                    completed = state.exercisesCompletedCount,
                    total = plan?.exercises?.size ?: 0
                )
                Spacer(modifier = Modifier.height(24.dp))
                SummarySavedNote()
                Spacer(modifier = Modifier.height(32.dp))
                SummaryFinishButton(onFinish = onFinish)
            }
        }
    }
}

@Composable
private fun SummaryHeader(plan: WorkoutPlan?, compact: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(if (compact) 64.dp else 90.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (compact) 34.dp else 48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 20.dp))

        Text(
            text = stringResource(R.string.summary_done),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.summary_great),
            fontSize = if (compact) 22.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = plan?.displayTitle() ?: stringResource(R.string.summary_default_title),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryMetrics(
    duration: String,
    calories: Int,
    completed: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryMetric(
                icon = Icons.Default.Timer,
                label = stringResource(R.string.summary_duration),
                value = duration
            )
            SummaryMetric(
                icon = Icons.Default.LocalFireDepartment,
                label = stringResource(R.string.summary_calories),
                value = stringResource(R.string.unit_kcal, calories)
            )
            SummaryMetric(
                icon = Icons.Default.FitnessCenter,
                label = stringResource(R.string.summary_exercises),
                value = "$completed/$total"
            )
        }
    }
}

@Composable
private fun SummarySavedNote() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.summary_saved),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SummaryFinishButton(onFinish: () -> Unit) {
    Button(
        onClick = onFinish,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("summary_finish_button"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = stringResource(R.string.summary_back_home),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SummaryMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
