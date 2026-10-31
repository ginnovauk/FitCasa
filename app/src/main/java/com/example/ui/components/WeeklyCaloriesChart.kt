package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.WorkoutEntity
import com.example.ui.displayTitle
import java.util.Calendar
import java.util.Locale

/** Resumen de un día dentro de la semana. */
data class DaySummary(
    val kcal: Int,
    val minutes: Int,
    val workouts: List<WorkoutEntity>
)

/** Resumen de la semana en curso (lunes a domingo) y la anterior, para comparar. */
data class WeekSummary(
    val days: List<DaySummary>,
    val totalKcal: Int,
    val previousWeekKcal: Int
)

/**
 * Agrega el historial en los 7 días de la semana en curso (lunes a domingo,
 * como es habitual en España). Las entradas anteriores al lunes de esta
 * semana se acumulan en el total de la semana pasada, para la comparación.
 */
fun buildWeekSummary(history: List<WorkoutEntity>, nowMillis: Long): WeekSummary {
    fun startOfDay(millis: Long): Calendar =
        Calendar.getInstance(Locale.getDefault()).apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    // Lunes de esta semana: hoy menos su desplazamiento dentro de la semana
    // (lunes = 0 ... domingo = 6). Más fiable que set(DAY_OF_WEEK), cuyo
    // resultado depende del WEEK_OF_* ya cargado en el Calendar.
    val today = startOfDay(nowMillis)
    val dayOfWeek = today.get(Calendar.DAY_OF_WEEK) // domingo = 1, lunes = 2 ... sábado = 7
    val sinceMonday = (dayOfWeek + 5) % 7
    val thisMonday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -sinceMonday) }
    val prevMonday = (thisMonday.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -7) }

    fun kcalBetween(from: Calendar, to: Calendar): Int = history
        .filter { it.dateMillis >= from.timeInMillis && it.dateMillis < to.timeInMillis }
        .sumOf { it.caloriesBurned }

    val days = (0..6).map { offset ->
        val start = (thisMonday.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val inDay = history.filter {
            it.dateMillis >= start.timeInMillis && it.dateMillis < end.timeInMillis
        }
        DaySummary(
            kcal = inDay.sumOf { it.caloriesBurned },
            minutes = inDay.sumOf { it.durationSeconds } / 60,
            workouts = inDay
        )
    }

    return WeekSummary(
        days = days,
        totalKcal = days.sumOf { it.kcal },
        previousWeekKcal = kcalBetween(prevMonday, thisMonday)
    )
}

/**
 * Gráfico semanal de kilocalorías, tipo app de fitness:
 * una barra por día, total de la semana con comparación contra la anterior
 * y detalle del día seleccionado al tocar cualquier barra.
 */
@Composable
fun WeeklyCaloriesChart(
    history: List<WorkoutEntity>,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null
) {
    val now = remember { System.currentTimeMillis() }
    val week = remember(history, now) { buildWeekSummary(history, now) }
    val todayIndex = todayIndexOf(now)
    var selectedIndex by remember { mutableIntStateOf(todayIndex) }
    val selected = week.days[selectedIndex]
    val dayInitials = stringArrayResource(R.array.weekday_initials)
    val dayNames = stringArrayResource(R.array.weekday_names)
    val dayName = if (selectedIndex == todayIndex) {
        stringResource(R.string.chart_today)
    } else {
        dayNames[selectedIndex]
    }
    val detail = if (selected.kcal == 0) {
        stringResource(R.string.chart_no_workout, dayName)
    } else {
        stringResource(R.string.chart_day_detail, dayName, selected.kcal, selected.minutes)
    }
    val subtitle = selected.workouts.map { it.displayTitle() }.distinct().joinToString(" · ")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.chart_week_kcal),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${week.totalKcal}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.chart_kcal_unit),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                if (onSeeAll != null) {
                    Text(
                        text = stringResource(R.string.chart_see_all),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onSeeAll)
                            .padding(top = 2.dp)
                    )
                }
            }

            TrendBadge(total = week.totalKcal, previous = week.previousWeekKcal)

            // Sin actividad la tarjeta se queda corta para el hueco del gráfico:
            // 130 dp de barras vacías la empujaban fuera de la ventana.
            val hasActivity = week.totalKcal > 0
            Spacer(modifier = Modifier.height(if (hasActivity) 14.dp else 10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (hasActivity) 130.dp else 58.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                week.days.forEachIndexed { index, day ->
                    DayBar(
                        kcal = day.kcal,
                        maxKcal = week.days.maxOfOrNull { it.kcal } ?: 0,
                        label = dayInitials[index],
                        selected = index == selectedIndex,
                        isToday = index == todayIndex,
                        animationDelayMillis = index * 60,
                        onClick = { selectedIndex = index },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = detail,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Chip compacto de tendencia contra la semana anterior. */
@Composable
private fun TrendBadge(total: Int, previous: Int) {
    if (previous == 0 && total == 0) return
    val diff = total - previous
    val up = diff >= 0
    val text = when {
        previous == 0 -> stringResource(R.string.chart_trend_first)
        diff == 0 -> stringResource(R.string.chart_trend_same)
        else -> stringResource(
            R.string.chart_trend_diff,
            if (up) "+" else "−",
            kotlin.math.abs(diff)
        )
    }
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (up) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (up) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Una barra del gráfico: altura animada proporcional al máximo de la semana. */
@Composable
private fun DayBar(
    kcal: Int,
    maxKcal: Int,
    label: String,
    selected: Boolean,
    isToday: Boolean,
    animationDelayMillis: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction by animateFloatAsState(
        targetValue = if (maxKcal > 0) kcal.toFloat() / maxKcal else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = animationDelayMillis),
        label = "barra-$label"
    )
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (kcal == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            val alpha = if (selected) 1f else 0.55f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((6 + 90 * fraction).dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                primary.copy(alpha = alpha),
                                tertiary.copy(alpha = alpha)
                            )
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/** Índice (0 = lunes) del día de hoy dentro de la semana (lunes a domingo). */
private fun todayIndexOf(nowMillis: Long): Int {
    val cal = Calendar.getInstance(Locale.getDefault()).apply { timeInMillis = nowMillis }
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        else -> 6
    }
}
