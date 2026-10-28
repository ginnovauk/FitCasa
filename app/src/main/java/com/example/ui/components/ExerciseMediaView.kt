package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.withFrameNanos
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.data.model.Exercise
import org.json.JSONObject

/** Metadatos: lienzo claro u oscuro recomendado para cada animación. */
private const val LOTTIE_CANVAS_ASSET = "exercises/lottie/canvas.json"

/**
 * Ajuste de presentación de cada animación: lienzo con contraste, zoom y
 * punto de anclaje. Las animaciones vienen diseñadas sobre un fondo concreto
 * (unas con trazos oscuros, otras con colores claros), así que el lienzo se
 * elige por animación y no por el tema de la app: así se ven igual de bien en
 * modo claro y en modo oscuro.
 */
private data class LottieDisplay(
    val canvasIsDark: Boolean,
    val zoom: Float,
    val anchor: String
)

private val defaultLottieDisplay = LottieDisplay(canvasIsDark = false, zoom = 1f, anchor = "center")

private fun lottieDisplayFor(context: Context, assetPath: String): LottieDisplay {
    val name = assetPath.substringAfterLast('/').removeSuffix(".json")
    return runCatching {
        val text = context.assets.open(LOTTIE_CANVAS_ASSET).bufferedReader().use { it.readText() }
        val entry = JSONObject(text).optJSONObject(name) ?: return defaultLottieDisplay
        LottieDisplay(
            canvasIsDark = entry.optString("canvas", "light").equals("dark", ignoreCase = true),
            zoom = entry.optDouble("zoom", 1.0).toFloat().coerceIn(0.5f, 3f),
            anchor = entry.optString("anchor", "center")
        )
    }.getOrDefault(defaultLottieDisplay)
}

/**
 * Demostración del ejercicio con su animación vectorial (Lottie).
 *
 * La animación va sola, sin etiquetas encima: cualquier texto tapaba la figura
 * y se repetía con el nombre del ejercicio que ya aparece en la ficha.
 *
 * Los ejercicios sin animación no dibujan nada: la pantalla de entrenamiento y
 * la ficha saltan este bloque para no dejar un hueco vacío.
 */
@Composable
fun ExerciseMediaView(
    exercise: Exercise,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    val lottieAsset = exercise.lottieAsset ?: return
    val context = LocalContext.current

    val display = remember(lottieAsset) { lottieDisplayFor(context, lottieAsset) }
    val canvasColor = if (display.canvasIsDark) Color(0xFF141A18) else Color(0xFFF2F6F4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(canvasColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset(lottieAsset))

        // La animación se mueve con el reloj de fotogramas, no con la escala de
        // animaciones del sistema: hay teléfonos con esa escala a 0 (ahorro de
        // batería o accesibilidad) y Lottie se quedaría congelado.
        val durationMs = (composition?.duration ?: 0f).toDouble()
        var progress by remember(composition) { mutableFloatStateOf(0f) }
        var elapsedMs by remember(composition) { mutableDoubleStateOf(0.0) }
        LaunchedEffect(composition, isPaused, durationMs) {
            if (composition == null || durationMs <= 0.0 || isPaused) return@LaunchedEffect
            var lastFrame = withFrameNanos { it }
            while (true) {
                withFrameNanos { now ->
                    elapsedMs += (now - lastFrame) / 1_000_000.0
                    lastFrame = now
                    progress = ((elapsedMs % durationMs) / durationMs).toFloat()
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            // La animación se dibuja en un cuadrado centrado; el punto de
            // anclaje se calcula sobre ese cuadrado para ampliar sin
            // descolocar la figura.
            val side = minOf(maxWidth.value, maxHeight.value)
            val originY = when (display.anchor) {
                "bottom" -> 0.5f + 0.5f * side / maxHeight.value
                "top" -> 0.5f - 0.5f * side / maxHeight.value
                else -> 0.5f
            }.coerceIn(0f, 1f)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = display.zoom
                        scaleY = display.zoom
                        transformOrigin = TransformOrigin(0.5f, originY)
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}
