package com.example.data.model

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

/**
 * Ejercicio del catálogo. Los textos son referencias a recursos para poder
 * traducirlos; se resuelven en la capa de interfaz con `stringResource`.
 */
data class Exercise(
    val id: String,
    @StringRes val nameRes: Int,
    val bodyPart: BodyPart,
    val intensity: IntensityLevel,
    val defaultDurationSeconds: Int,
    val isTimeBased: Boolean = true,
    val defaultReps: Int = 12,
    val caloriesPerMinute: Float = 7.5f,
    @ArrayRes val instructionsRes: Int,
    @StringRes val breathingTipsRes: Int,
    @StringRes val commonMistakesRes: Int,
    /** Animación vectorial (Lottie) dentro de assets. Null si el ejercicio no tiene demostración. */
    val lottieAsset: String? = null,
    /** Ejercicio de movilidad que abre la rutina; no cuenta como trabajo principal. */
    val isWarmUp: Boolean = false
)
