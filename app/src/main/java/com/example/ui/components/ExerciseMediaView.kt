package com.example.ui.components

import android.os.Build
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.Exercise

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(surfaceVariant.copy(alpha = 0.5f))
            .testTag("exercise_real_human_trainer_view"),
        contentAlignment = Alignment.Center
    ) {
        if (!mediaUrl.isNullOrEmpty()) {
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

        if (!mediaFailed) {
            // Top Trainer Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = surfaceColor.copy(alpha = 0.92f),
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
                        text = "Entrenador Real • Demostración HD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            // Bottom Muscle Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = surfaceColor.copy(alpha = 0.92f),
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
                        color = textSecondary
                    )
                }
            }
        }
    }
}
