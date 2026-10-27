package com.example.ui.components

import android.content.Context
import android.os.Build
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.ImageRequest
import coil.request.Options
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.data.model.Exercise
import org.json.JSONObject

/** Carpeta con las animaciones vectoriales (Lottie) dentro de assets. */
private const val LOTTIE_ASSET_DIR = "exercises/lottie"

/** Metadatos: lienzo claro u oscuro recomendado para cada animación. */
private const val LOTTIE_CANVAS_ASSET = "exercises/lottie/canvas.json"

/**
 * Asset Lottie equivalente al GIF del ejercicio: mismo nombre de fichero con
 * extensión .json. Si existe, se usa en lugar del GIF.
 */
private fun lottieAssetFor(mediaUrl: String?): String? =
    mediaUrl
        ?.substringAfterLast('/')
        ?.takeIf { it.endsWith(".gif") }
        ?.removeSuffix(".gif")
        ?.let { "$LOTTIE_ASSET_DIR/$it.json" }

private fun assetExists(context: Context, path: String): Boolean =
    runCatching { context.assets.open(path).close() }.isSuccess

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

private fun lottieDisplayFor(context: Context, assetPath: String?): LottieDisplay? {
    val name = assetPath?.substringAfterLast('/')?.removeSuffix(".json") ?: return null
    return runCatching {
        val text = context.assets.open(LOTTIE_CANVAS_ASSET).bufferedReader().use { it.readText() }
        val entry = JSONObject(text).optJSONObject(name) ?: return null
        LottieDisplay(
            canvasIsDark = entry.optString("canvas", "light").equals("dark", ignoreCase = true),
            zoom = entry.optDouble("zoom", 1.0).toFloat().coerceIn(1f, 3f),
            anchor = entry.optString("anchor", "center")
        )
    }.getOrNull()
}

/**
 * Robolectric (los tests JVM) no puede dibujar [android.graphics.drawable.AnimatedImageDrawable]
 * y [android.graphics.Movie] devuelve null, así que en ese entorno los GIF se
 * decodifican como primer fotograma estático con BitmapFactory, que sí funciona.
 * Solo afecta a las capturas de tests; en el teléfono real no se registra.
 */
private val isRobolectricEnvironment: Boolean =
    Build.FINGERPRINT.contains("robolectric", ignoreCase = true)

/**
 * Decoder que decodifica el primer fotograma de un GIF como imagen estática.
 * Solo se registra bajo Robolectric (ver [isRobolectricEnvironment]).
 */
private class FirstFrameGifDecoder(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val bytes = source.source().readByteArray()
        if (bytes.size < 6) return null
        val header = bytes.copyOfRange(0, 6).toString(Charsets.US_ASCII)
        if (header != "GIF87a" && header != "GIF89a") return null
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        return DecodeResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = false
        )
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder = FirstFrameGifDecoder(result.source, options)
    }
}

@Composable
fun ExerciseMediaView(
    exercise: Exercise,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // El crossfade se dibuja con el reloj de Compose, que en los tests está
    // congelado y deja la imagen en alpha 0 (captura en blanco): en Robolectric
    // se desactiva; en el teléfono real no cambia nada.
    val crossfadeEnabled = !isRobolectricEnvironment

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (isRobolectricEnvironment) {
                    add(FirstFrameGifDecoder.Factory())
                } else if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(crossfadeEnabled)
            .build()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val targetMuscleName = when (exercise.bodyPart) {
        com.example.data.model.BodyPart.PECHO -> "Pectorales y Tríceps"
        com.example.data.model.BodyPart.PIERNAS -> "Cuádriceps, Femoral y Glúteos"
        com.example.data.model.BodyPart.ABDOMEN -> "Core, Recto Abdominal y Oblicuos"
        com.example.data.model.BodyPart.BRAZOS -> "Bíceps y Tríceps"
        com.example.data.model.BodyPart.ESPALDA -> "Dorsales, Trapecios y Lumbares"
        com.example.data.model.BodyPart.CUERPO_COMPLETO -> "Resistencia Integral y Cardio"
    }

    val mediaUrl = exercise.demonstratorMediaUrl
    // Si el GIF no está disponible (sin conexión, error de red o sin URL),
    // se muestra la animación biomecánica generada en el propio teléfono.
    var mediaFailed by remember(mediaUrl) { mutableStateOf(mediaUrl.isNullOrEmpty()) }

    // Animación vectorial (Lottie) si hay JSON para este ejercicio. Robolectric
    // no rasteriza Lottie, así que en las capturas de test se sigue usando el GIF.
    val lottieAsset = remember(mediaUrl) { lottieAssetFor(mediaUrl) }
    val useLottie = remember(lottieAsset) {
        lottieAsset != null && !isRobolectricEnvironment && assetExists(context, lottieAsset)
    }

    // Lienzo que garantiza contraste con la animación, sea cual sea el tema.
    val lottieDisplay = remember(useLottie, lottieAsset) {
        if (useLottie) lottieDisplayFor(context, lottieAsset) else null
    }
    val canvasIsDark = lottieDisplay?.canvasIsDark
    val canvasColor = when (canvasIsDark) {
        true -> Color(0xFF141A18)
        false -> Color(0xFFF2F6F4)
        null -> surfaceVariant.copy(alpha = 0.5f)
    }
    val badgeColor = when (canvasIsDark) {
        true -> Color(0xFF0A0E0C).copy(alpha = 0.8f)
        false -> Color.White.copy(alpha = 0.92f)
        null -> surfaceColor.copy(alpha = 0.92f)
    }
    val badgeText = when (canvasIsDark) {
        true -> Color(0xFFEDF3F0)
        false -> Color(0xFF16201B)
        null -> textPrimary
    }
    val badgeTextSoft = when (canvasIsDark) {
        true -> Color(0xFFC2CDC8)
        false -> Color(0xFF47564F)
        null -> textSecondary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(canvasColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("exercise_real_human_trainer_view"),
        contentAlignment = Alignment.Center
    ) {
        if (useLottie) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.Asset(lottieAsset!!)
            )
            val zoom = lottieDisplay?.zoom ?: 1f
            val anchor = lottieDisplay?.anchor ?: "center"

            // La animación se mueve con el reloj de fotogramas, no con la
            // escala de animaciones del sistema: hay teléfonos con esa escala
            // a 0 (ahorro de batería o accesibilidad) y Lottie se quedaría
            // congelado en el primer fotograma.
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
                val originY = when (anchor) {
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
                            scaleX = zoom
                            scaleY = zoom
                            transformOrigin = TransformOrigin(0.5f, originY)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        } else if (!mediaUrl.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(mediaUrl)
                    .crossfade(crossfadeEnabled)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "Demostración de ${exercise.name}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Fit,
                onSuccess = { mediaFailed = false },
                onError = { mediaFailed = true },
                loading = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(38.dp),
                            color = primaryColor,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Cargando técnica del entrenador...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondary
                        )
                    }
                },
                error = {
                    ExerciseAnimationView(
                        animationType = exercise.animationType,
                        isPaused = isPaused,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        } else {
            ExerciseAnimationView(
                animationType = exercise.animationType,
                isPaused = isPaused,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (useLottie || !mediaFailed) {
            // Top Trainer Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeColor,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (useLottie) {
                            "Animación vectorial • HD"
                        } else {
                            "Entrenador Real • Demostración HD"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }
            }

            // Bottom Muscle Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeColor,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Músculo: $targetMuscleName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeTextSoft
                    )
                }
            }
        }
    }
}
