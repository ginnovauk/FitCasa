package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.local.WorkoutEntity
import com.example.data.model.BodyPart
import com.example.data.model.Exercise
import com.example.data.model.IntensityLevel
import com.example.data.model.Sex
import com.example.data.model.WorkoutPlan
import java.util.Locale

/**
 * Resolución de los textos que viven en recursos.
 *
 * Los modelos guardan `@StringRes`/`@ArrayRes` en vez de texto para que el
 * idioma se decida al dibujar, no al construir el catálogo. Así un cambio de
 * idioma se refleja en todo sin recrear datos.
 */

@Composable
fun Exercise.displayName(): String = stringResource(nameRes)

@Composable
fun Exercise.instructions(): List<String> = stringArrayResource(instructionsRes).toList()

@Composable
fun Exercise.breathingTips(): String = stringResource(breathingTipsRes)

@Composable
fun Exercise.commonMistakes(): String = stringResource(commonMistakesRes)

@Composable
fun BodyPart.displayName(): String = stringResource(nameRes)

@Composable
fun BodyPart.subtitle(): String = stringResource(subtitleRes)

@Composable
fun IntensityLevel.displayName(): String = stringResource(nameRes)

@Composable
fun IntensityLevel.description(): String = stringResource(descriptionRes)

@Composable
fun Sex.displayName(): String = stringResource(nameRes)

/** Título legible de la rutina, compuesto a partir de la zona y el nivel. */
@Composable
fun WorkoutPlan.displayTitle(): String =
    stringResource(R.string.plan_title, bodyPart.displayName(), intensity.displayName())

/**
 * Título de una sesión guardada. Se reconstruye desde los identificadores para
 * que las sesiones antiguas también aparezcan en el idioma actual; si el
 * registro no los tiene, se usa el texto que se guardó en su momento.
 */
@Composable
fun WorkoutEntity.displayTitle(): String {
    val bodyPart = BodyPart.fromId(bodyPartId)
    val intensity = IntensityLevel.fromId(intensityId)
    return if (bodyPart != null && intensity != null) {
        stringResource(R.string.plan_title, bodyPart.displayName(), intensity.displayName())
    } else {
        workoutTitle
    }
}

/** Idioma activo de la app (respeta el selector de idioma por aplicación). */
@Composable
fun rememberAppLocale(): Locale = LocalConfiguration.current.locales[0]
