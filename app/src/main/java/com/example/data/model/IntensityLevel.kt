package com.example.data.model

import androidx.annotation.StringRes
import com.example.R

enum class IntensityLevel(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val restDurationSeconds: Int,
    val workRatioMultiplier: Float
) {
    PRINCIPIANTE(
        id = "principiante",
        nameRes = R.string.intensity_beginner_name,
        descriptionRes = R.string.intensity_beginner_desc,
        restDurationSeconds = 25,
        workRatioMultiplier = 0.8f
    ),
    INTERMEDIO(
        id = "intermedio",
        nameRes = R.string.intensity_intermediate_name,
        descriptionRes = R.string.intensity_intermediate_desc,
        restDurationSeconds = 20,
        workRatioMultiplier = 1.0f
    ),
    AVANZADO(
        id = "avanzado",
        nameRes = R.string.intensity_advanced_name,
        descriptionRes = R.string.intensity_advanced_desc,
        restDurationSeconds = 15,
        workRatioMultiplier = 1.25f
    );

    companion object {
        /** Busca un nivel por su identificador guardado en la base de datos. */
        fun fromId(id: String?): IntensityLevel? = values().firstOrNull { it.id == id }
    }
}
