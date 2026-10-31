package com.example.data.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class BodyPart(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector
) {
    CUERPO_COMPLETO(
        id = "cuerpo_completo",
        nameRes = R.string.body_part_full_name,
        subtitleRes = R.string.body_part_full_subtitle,
        icon = Icons.Default.Whatshot
    ),
    PECHO(
        id = "pecho",
        nameRes = R.string.body_part_chest_name,
        subtitleRes = R.string.body_part_chest_subtitle,
        icon = Icons.Default.Shield
    ),
    ABDOMEN(
        id = "abdomen",
        nameRes = R.string.body_part_core_name,
        subtitleRes = R.string.body_part_core_subtitle,
        icon = Icons.Default.AccessibilityNew
    ),
    PIERNAS(
        id = "piernas",
        nameRes = R.string.body_part_legs_name,
        subtitleRes = R.string.body_part_legs_subtitle,
        icon = Icons.Default.DirectionsRun
    ),
    BRAZOS(
        id = "brazos",
        nameRes = R.string.body_part_arms_name,
        subtitleRes = R.string.body_part_arms_subtitle,
        icon = Icons.Default.FitnessCenter
    ),
    ESPALDA(
        id = "espalda",
        nameRes = R.string.body_part_back_name,
        subtitleRes = R.string.body_part_back_subtitle,
        icon = Icons.Default.SportsGymnastics
    ),
    PALO(
        id = "palo",
        nameRes = R.string.body_part_stick_name,
        subtitleRes = R.string.body_part_stick_subtitle,
        icon = Icons.Default.Straighten
    );

    companion object {
        /** Busca una zona por su identificador guardado en la base de datos. */
        fun fromId(id: String?): BodyPart? = values().firstOrNull { it.id == id }
    }
}
