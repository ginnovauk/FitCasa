package com.example.data.model

data class Exercise(
    val id: String,
    val name: String,
    val bodyPart: BodyPart,
    val intensity: IntensityLevel,
    val defaultDurationSeconds: Int,
    val isTimeBased: Boolean = true,
    val defaultReps: Int = 12,
    val caloriesPerMinute: Float = 7.5f,
    val instructions: List<String>,
    val breathingTips: String,
    val commonMistakes: String,
    /** Animación vectorial (Lottie) dentro de assets. Null si el ejercicio no tiene demostración. */
    val lottieAsset: String? = null,
    val trainerName: String = "Entrenador Certificado",
    /** Ejercicio de movilidad que abre la rutina; no cuenta como trabajo principal. */
    val isWarmUp: Boolean = false
)
