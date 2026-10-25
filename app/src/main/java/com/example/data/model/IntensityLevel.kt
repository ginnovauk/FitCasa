package com.example.data.model

enum class IntensityLevel(
    val id: String,
    val displayName: String,
    val description: String,
    val restDurationSeconds: Int,
    val workRatioMultiplier: Float
) {
    PRINCIPIANTE(
        id = "principiante",
        displayName = "Principiante",
        description = "Movimientos controlados, intervalos suaves y descansos amplios para iniciar sin lesiones.",
        restDurationSeconds = 25,
        workRatioMultiplier = 0.8f
    ),
    INTERMEDIO(
        id = "intermedio",
        displayName = "Intermedio",
        description = "Ritmo dinámico, tensión muscular constante y descansos moderados para progresar.",
        restDurationSeconds = 20,
        workRatioMultiplier = 1.0f
    ),
    AVANZADO(
        id = "avanzado",
        displayName = "Avanzado",
        description = "Máxima intensidad, densidad de trabajo elevada y descansos cortos para máxima exigencia.",
        restDurationSeconds = 15,
        workRatioMultiplier = 1.25f
    )
}
