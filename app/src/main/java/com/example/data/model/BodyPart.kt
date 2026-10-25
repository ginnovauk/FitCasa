package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

enum class BodyPart(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val icon: ImageVector
) {
    CUERPO_COMPLETO(
        id = "cuerpo_completo",
        displayName = "Cuerpo Completo",
        subtitle = "Quema calórica total y resistencia integral",
        icon = Icons.Default.Whatshot
    ),
    PECHO(
        id = "pecho",
        displayName = "Pecho",
        subtitle = "Fuerza pectoral, deltoides y empuje",
        icon = Icons.Default.Shield
    ),
    ABDOMEN(
        id = "abdomen",
        displayName = "Abdomen y Core",
        subtitle = "Definición, estabilidad y postura",
        icon = Icons.Default.AccessibilityNew
    ),
    PIERNAS(
        id = "piernas",
        displayName = "Piernas y Glúteos",
        subtitle = "Potencia de tren inferior y equilibrio",
        icon = Icons.Default.DirectionsRun
    ),
    BRAZOS(
        id = "brazos",
        displayName = "Brazos",
        subtitle = "Bíceps, tríceps y antebrazos",
        icon = Icons.Default.FitnessCenter
    ),
    ESPALDA(
        id = "espalda",
        displayName = "Espalda y Hombros",
        subtitle = "Corrección postural y cadena posterior",
        icon = Icons.Default.SportsGymnastics
    )
}
