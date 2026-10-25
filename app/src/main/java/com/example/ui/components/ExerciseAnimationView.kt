package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseAnimationType
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class NonLinearExerciseState(
    val depthProgress: Float,
    val phaseName: String,
    val glowIntensity: Float
)

@Composable
fun ExerciseAnimationView(
    animationType: ExerciseAnimationType,
    modifier: Modifier = Modifier,
    isPaused: Boolean = false
) {
    var speedMultiplier by remember { mutableFloatStateOf(1f) }

    // Natural human rep duration: ~2.4 seconds per rep (2-1-1 tempo)
    val baseDuration = when (animationType) {
        ExerciseAnimationType.JUMPING_JACKS, ExerciseAnimationType.HIGH_KNEES -> 900
        ExerciseAnimationType.MOUNTAIN_CLIMBERS, ExerciseAnimationType.BICYCLE_CRUNCHES -> 1000
        ExerciseAnimationType.BURPEES -> 1800
        ExerciseAnimationType.BICEPS_CURL -> 1900
        ExerciseAnimationType.PLANK, ExerciseAnimationType.WALL_SIT -> 2400
        else -> 2200
    }

    val cycleDuration = (baseDuration / speedMultiplier).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "biomechanic_cycle")
    // Runs on continuous 0..1 loop for authentic 4-phase tempo
    val rawCycleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuous_cycle"
    )

    val cycleTime = if (isPaused) 0.5f else rawCycleTime

    // Calculate non-linear biomechanical curves (eccentric, pause, explosive concentric, reset)
    val state = remember(cycleTime, animationType, isPaused) {
        if (isPaused) {
            NonLinearExerciseState(0.5f, "Pausado", 0.5f)
        } else {
            calculateNonLinearTempo(cycleTime, animationType)
        }
    }

    // Color tokens for solid 2D athletic character
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    // Character palette: realistic athletic wear + skin tones + depth shading
    val skinToneNear = Color(0xFFF3C7A8) // Foreground healthy athletic tone
    val skinToneFar = Color(0xFFD4A788)  // Background shadow tone for 3D parallax depth
    val shirtColor = primaryColor        // Athletic compression jersey
    val shirtDark = primaryColor.copy(alpha = 0.75f)
    val shortsColor = Color(0xFF1E293B)  // Athletic dark navy shorts
    val shortsDark = Color(0xFF0F172A)
    val shoeColor = Color(0xFF0284C7)    // Cyan athletic sneakers
    val soleColor = Color(0xFFFFFFFF)    // White rubber sneaker sole
    val hairColor = Color(0xFF27272A)    // Dark athletic styled hair

    val activeMuscleName = when (animationType) {
        ExerciseAnimationType.PUSH_UP, ExerciseAnimationType.TRICEPS_DIPS -> "Pectorales Mayores y Tríceps"
        ExerciseAnimationType.SQUAT, ExerciseAnimationType.LUNGES, ExerciseAnimationType.WALL_SIT -> "Cuádriceps y Glúteo Mayor"
        ExerciseAnimationType.PLANK, ExerciseAnimationType.CRUNCHES, ExerciseAnimationType.BICYCLE_CRUNCHES -> "Recto Abdominal y Core"
        ExerciseAnimationType.GLUTE_BRIDGE -> "Glúteo Mayor e Isquios"
        ExerciseAnimationType.SUPERMAN, ExerciseAnimationType.COBRA_STRETCH -> "Erectores Espinales y Dorsales"
        ExerciseAnimationType.TRICEPS_EXTENSION -> "Tríceps y Deltoides Posterior"
        ExerciseAnimationType.BICEPS_CURL -> "Bíceps Braquial y Antebrazo"
        ExerciseAnimationType.BODYWEIGHT_ROW -> "Dorsal Ancho, Romboides y Trapecio"
        ExerciseAnimationType.PRONE_ELBOW_LIFT -> "Romboides y Trapecio Inferior"
        ExerciseAnimationType.JUMPING_JACKS, ExerciseAnimationType.MOUNTAIN_CLIMBERS,
        ExerciseAnimationType.HIGH_KNEES, ExerciseAnimationType.BURPEES -> "Cuerpo Completo y Cardio"
        ExerciseAnimationType.NONE -> "Movilidad Articular"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        surfaceVariant.copy(alpha = 0.45f),
                        surfaceColor.copy(alpha = 0.98f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val groundY = height * 0.84f

            // 1. Gym Workout Mat
            val matWidth = width * 0.88f
            val matHeight = 14.dp.toPx()
            val matLeft = (width - matWidth) / 2f
            val matTop = groundY - 4.dp.toPx()

            // Mat depth drop-shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.06f),
                topLeft = Offset(matLeft + 2.dp.toPx(), matTop + 4.dp.toPx()),
                size = Size(matWidth, matHeight),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
            )
            // Mat base
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        secondaryColor.copy(alpha = 0.20f),
                        primaryColor.copy(alpha = 0.25f),
                        secondaryColor.copy(alpha = 0.20f)
                    )
                ),
                topLeft = Offset(matLeft, matTop),
                size = Size(matWidth, matHeight),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
            )

            // Render solid anatomical character per exercise
            when (animationType) {
                ExerciseAnimationType.NONE -> {
                    // Sin figura vectorial: la demostración de estos ejercicios
                    // es el GIF empaquetado, no la animación de respaldo.
                }
                ExerciseAnimationType.PUSH_UP -> renderSolidPushUp(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.SQUAT -> renderSolidSquat(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.PLANK -> renderSolidPlank(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.JUMPING_JACKS -> renderSolidJumpingJacks(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.CRUNCHES -> renderSolidCrunches(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.MOUNTAIN_CLIMBERS -> renderSolidMountainClimbers(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.LUNGES -> renderSolidLunges(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.GLUTE_BRIDGE -> renderSolidGluteBridge(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.TRICEPS_DIPS -> renderSolidTricepsDips(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.HIGH_KNEES -> renderSolidHighKnees(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.WALL_SIT -> renderSolidWallSit(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.SUPERMAN -> renderSolidSuperman(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.COBRA_STRETCH -> renderSolidCobra(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.BICYCLE_CRUNCHES -> renderSolidBicycleCrunches(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.BURPEES -> renderSolidBurpees(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.TRICEPS_EXTENSION -> renderSolidTricepsExtension(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.BICEPS_CURL -> renderSolidBicepsCurl(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.BODYWEIGHT_ROW -> renderSolidBodyweightRow(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
                ExerciseAnimationType.PRONE_ELBOW_LIFT -> renderSolidProneElbowLift(
                    centerX, groundY, state.depthProgress, state.glowIntensity,
                    skinToneNear, skinToneFar, shirtColor, shortsColor, shoeColor, soleColor, hairColor, tertiaryColor
                )
            }
        }

        // Top Info & Speed Switcher
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Non-linear Phase Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = primaryColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) tertiaryColor else primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.phaseName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                // Interactive Speed Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = surfaceColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .clickable {
                            speedMultiplier = when (speedMultiplier) {
                                1f -> 0.75f
                                0.75f -> 1.35f
                                else -> 1f
                            }
                        }
                        .testTag("animation_speed_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (speedMultiplier) {
                                0.75f -> "0.75x (Técnica)"
                                1.35f -> "1.35x (Intenso)"
                                else -> "1.0x (Normal)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                    }
                }
            }

            // Target Muscle Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = surfaceColor.copy(alpha = 0.90f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "Músculo activo: $activeMuscleName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// NON-LINEAR TEMPO ENGINE
// -----------------------------------------------------------------------------------------
private fun calculateNonLinearTempo(t: Float, type: ExerciseAnimationType): NonLinearExerciseState {
    return when (type) {
        ExerciseAnimationType.JUMPING_JACKS,
        ExerciseAnimationType.HIGH_KNEES,
        ExerciseAnimationType.MOUNTAIN_CLIMBERS,
        ExerciseAnimationType.BICYCLE_CRUNCHES -> {
            // Rapid continuous rhythmic cadence
            val sinVal = (sin(t * Math.PI.toFloat() * 2f - (Math.PI.toFloat() / 2f)) + 1f) / 2f
            val phase = if (sinVal > 0.5f) "Impulso Máximo" else "Recuperación"
            NonLinearExerciseState(sinVal, phase, sinVal)
        }
        ExerciseAnimationType.PLANK,
        ExerciseAnimationType.WALL_SIT -> {
            // Isometric hold with rhythmic breathing wave
            val breath = (sin(t * Math.PI.toFloat() * 4f) + 1f) / 2f
            NonLinearExerciseState(0.5f + (breath * 0.08f), "Tensión Isométrica Máxima", 0.7f + breath * 0.3f)
        }
        else -> {
            // Authentic 4-Phase Strength Rep (Descent 45%, Peak hold 10%, Explosive push 30%, Reset 15%)
            when {
                t < 0.45f -> {
                    val sub = t / 0.45f
                    // Smooth ease-in-out descent
                    val eased = (1f - cos(sub * Math.PI.toFloat())) / 2f
                    NonLinearExerciseState(eased, "Fase Excéntrica (Bajada Controlada)", 0.3f + eased * 0.4f)
                }
                t < 0.55f -> {
                    // Maximum contraction isometric bottom hold
                    NonLinearExerciseState(1f, "Punto Máximo (Tensión)", 1.0f)
                }
                t < 0.85f -> {
                    // Explosive concentric drive up
                    val sub = (t - 0.55f) / 0.30f
                    val eased = 1f - sin(sub * (Math.PI.toFloat() / 2f))
                    NonLinearExerciseState(eased, "Fase Concéntrica (Empuje Explosivo)", 0.85f - (1f - eased) * 0.45f)
                }
                else -> {
                    // Lockout & inhalation
                    NonLinearExerciseState(0f, "Bloqueo y Respiración", 0.2f)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// SOLID 2D ANATOMICAL RENDERING ENGINE
// -----------------------------------------------------------------------------------------

/**
 * PUSH-UP: Solid anatomical athlete with compression shirt, shorts, muscular arms and shoes.
 */
private fun DrawScope.renderSolidPushUp(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val descent = depth * 34.dp.toPx()
    val headX = centerX + 60.dp.toPx()
    val chestY = groundY - 50.dp.toPx() + descent
    val shoulderX = headX - 16.dp.toPx()
    val handX = centerX + 36.dp.toPx()
    val handY = groundY - 4.dp.toPx()
    val toesX = centerX - 75.dp.toPx()
    val hipX = centerX - 12.dp.toPx()
    val hipY = groundY - 36.dp.toPx() + (descent * 0.45f)

    // Floor Contact Shadow (grows larger as chest nears mat)
    drawOval(
        color = Color.Black.copy(alpha = 0.08f + (depth * 0.15f)),
        topLeft = Offset(centerX - 45.dp.toPx(), groundY - 2.dp.toPx()),
        size = Size(110.dp.toPx(), 8.dp.toPx())
    )

    // 1. Far Arm (Shadow layer)
    val farElbowX = handX - 22.dp.toPx() - (depth * 14.dp.toPx())
    val farElbowY = groundY - 26.dp.toPx() + (descent * 0.65f)
    drawSolidMuscularLimb(Offset(shoulderX, chestY), Offset(farElbowX, farElbowY), Offset(handX - 8.dp.toPx(), handY), 10.dp.toPx(), 8.dp.toPx(), skinFar)

    // 2. Far Leg & Shoe
    drawSolidSneaker(Offset(toesX - 4.dp.toPx(), groundY - 8.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(centerX - 44.dp.toPx(), groundY - 22.dp.toPx() + (descent * 0.2f)), Offset(toesX, groundY - 6.dp.toPx()), 14.dp.toPx(), 11.dp.toPx(), skinFar)

    // 3. Near Leg & Shoe
    drawSolidSneaker(Offset(toesX, groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(centerX - 42.dp.toPx(), groundY - 20.dp.toPx() + (descent * 0.2f)), Offset(toesX, groundY - 4.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    // 4. Athletic Shorts (Solid filled polygon)
    drawSolidShorts(Offset(hipX - 12.dp.toPx(), hipY - 2.dp.toPx()), Offset(hipX + 16.dp.toPx(), hipY), Offset(centerX - 30.dp.toPx(), groundY - 26.dp.toPx() + (descent * 0.25f)), shorts)

    // 5. Solid Muscular Torso with Compression Jersey
    drawSolidTorso(
        topCenter = Offset(shoulderX, chestY),
        bottomCenter = Offset(hipX, hipY),
        widthTop = 26.dp.toPx(),
        widthBottom = 20.dp.toPx(),
        fillColor = shirt
    )

    // Pectoral Muscle Activation Glow
    if (glow > 0.3f) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(highlight.copy(alpha = 0.7f * glow), Color.Transparent),
                center = Offset(shoulderX - 4.dp.toPx(), chestY),
                radius = 26.dp.toPx()
            ),
            radius = 26.dp.toPx(),
            center = Offset(shoulderX - 4.dp.toPx(), chestY)
        )
    }

    // 6. Sculpted Head with Hair and Athletic Profile
    drawSolidHead(Offset(headX + 4.dp.toPx(), chestY - 14.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // 7. Near Arm (Foreground layer with defined bicep/forearm)
    val nearElbowX = handX - 14.dp.toPx() - (depth * 14.dp.toPx())
    val nearElbowY = groundY - 22.dp.toPx() + (descent * 0.65f)
    drawSolidMuscularLimb(Offset(shoulderX, chestY), Offset(nearElbowX, nearElbowY), Offset(handX, handY), 12.dp.toPx(), 10.dp.toPx(), skinNear)
    drawCircle(skinNear, radius = 6.dp.toPx(), center = Offset(handX, handY))
}

/**
 * SQUAT: Solid 2D squatter with hip hinge, muscular quads, forward arms, sneaker soles flat on floor.
 */
private fun DrawScope.renderSolidSquat(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val sink = depth * 44.dp.toPx()
    val hipX = centerX - 20.dp.toPx() * (1f + depth * 0.35f)
    val hipY = groundY - 80.dp.toPx() + sink
    val torsoTopY = hipY - 54.dp.toPx() + (sink * 0.12f)
    val shoulderX = centerX - 6.dp.toPx()
    val headY = torsoTopY - 16.dp.toPx()

    // Ground Contact Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.14f + (depth * 0.08f)),
        topLeft = Offset(centerX - 45.dp.toPx(), groundY - 2.dp.toPx()),
        size = Size(90.dp.toPx(), 8.dp.toPx())
    )

    // Knees tracking forward (hip hinge mechanics)
    val kneeX = centerX + 12.dp.toPx() + (depth * 16.dp.toPx())
    val kneeY = groundY - 38.dp.toPx() + (sink * 0.6f)
    val footLX = centerX - 26.dp.toPx()
    val footRX = centerX + 20.dp.toPx()

    // 1. Far Leg (Background depth)
    drawSolidSneaker(Offset(footLX, groundY - 4.dp.toPx()), shoe.copy(alpha = 0.75f), sole)
    drawSolidMuscularLimb(Offset(hipX - 6.dp.toPx(), hipY), Offset(kneeX - 8.dp.toPx(), kneeY), Offset(footLX, groundY - 4.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinFar)

    // 2. Near Leg (Foreground quads)
    drawSolidSneaker(Offset(footRX, groundY - 4.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(kneeX, kneeY), Offset(footRX, groundY - 4.dp.toPx()), 16.dp.toPx(), 13.dp.toPx(), skinNear)

    // Quadriceps Muscle Glow
    if (glow > 0.25f) {
        val quadCenter = Offset((hipX + kneeX) / 2f, (hipY + kneeY) / 2f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(highlight.copy(alpha = 0.75f * glow), Color.Transparent),
                center = quadCenter,
                radius = 28.dp.toPx()
            ),
            radius = 28.dp.toPx(),
            center = quadCenter
        )
    }

    // 3. Athletic Shorts
    drawSolidShorts(Offset(hipX - 10.dp.toPx(), hipY - 6.dp.toPx()), Offset(hipX + 14.dp.toPx(), hipY - 6.dp.toPx()), Offset(kneeX - 12.dp.toPx(), kneeY - 8.dp.toPx()), shorts)

    // 4. Solid Torso with Athletic Jersey
    drawSolidTorso(Offset(shoulderX, torsoTopY), Offset(hipX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)

    // 5. Head
    drawSolidHead(Offset(shoulderX + 2.dp.toPx(), headY), 13.dp.toPx(), skinNear, hair)

    // 6. Balanced Forward Arms
    val armEndX = shoulderX + 46.dp.toPx()
    val armEndY = torsoTopY + 4.dp.toPx() + (depth * 8.dp.toPx())
    drawSolidMuscularLimb(Offset(shoulderX, torsoTopY + 4.dp.toPx()), Offset(shoulderX + 24.dp.toPx(), armEndY - 4.dp.toPx()), Offset(armEndX, armEndY), 11.dp.toPx(), 8.dp.toPx(), skinNear)
}

/**
 * PLANK: Solid isometric body plank with pulsing abdominal core tension.
 */
private fun DrawScope.renderSolidPlank(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val breath = (depth - 0.5f) * 6.dp.toPx()
    val headX = centerX + 65.dp.toPx()
    val feetX = centerX - 75.dp.toPx()
    val plankY = groundY - 36.dp.toPx() + breath
    val elbowX = centerX + 34.dp.toPx()
    val hipX = centerX - 12.dp.toPx()

    // Shadow
    drawOval(color = Color.Black.copy(alpha = 0.12f), topLeft = Offset(centerX - 60.dp.toPx(), groundY - 2.dp.toPx()), size = Size(130.dp.toPx(), 7.dp.toPx()))

    // Shoes
    drawSolidSneaker(Offset(feetX, groundY - 6.dp.toPx()), shoe, sole)

    // Legs
    drawSolidMuscularLimb(Offset(hipX, plankY), Offset(centerX - 45.dp.toPx(), plankY + 2.dp.toPx()), Offset(feetX, groundY - 6.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    // Shorts
    drawSolidShorts(Offset(hipX - 14.dp.toPx(), plankY - 4.dp.toPx()), Offset(hipX + 12.dp.toPx(), plankY - 4.dp.toPx()), Offset(hipX - 22.dp.toPx(), plankY + 8.dp.toPx()), shorts)

    // Torso
    drawSolidTorso(Offset(headX - 16.dp.toPx(), plankY), Offset(hipX, plankY), 26.dp.toPx(), 20.dp.toPx(), shirt)

    // Core Tension Glow
    val coreCenter = Offset(centerX - 6.dp.toPx(), plankY + 2.dp.toPx())
    drawCircle(
        brush = Brush.radialGradient(
            listOf(highlight.copy(alpha = 0.8f * glow), Color.Transparent),
            center = coreCenter,
            radius = 30.dp.toPx()
        ),
        radius = 30.dp.toPx(),
        center = coreCenter
    )

    // Head
    drawSolidHead(Offset(headX, plankY - 10.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Forearm resting solid on mat
    drawSolidMuscularLimb(Offset(headX - 16.dp.toPx(), plankY), Offset(elbowX, groundY - 6.dp.toPx()), Offset(elbowX + 28.dp.toPx(), groundY - 6.dp.toPx()), 12.dp.toPx(), 9.dp.toPx(), skinNear)
}

/**
 * JUMPING JACKS: Explosive jump, full solid figure arcing arms overhead.
 */
private fun DrawScope.renderSolidJumpingJacks(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val jumpHeight = depth * 22.dp.toPx()
    val legSpread = depth * 46.dp.toPx()
    val hipY = groundY - 76.dp.toPx() - jumpHeight
    val torsoY = hipY - 50.dp.toPx()
    val headY = torsoY - 16.dp.toPx()

    // Shrinking dynamic jump shadow
    val shadowW = (80.dp.toPx() * (1f - depth * 0.4f)).coerceAtLeast(28.dp.toPx())
    drawOval(color = Color.Black.copy(alpha = 0.16f * (1f - depth * 0.45f)), topLeft = Offset(centerX - shadowW / 2f, groundY - 2.dp.toPx()), size = Size(shadowW, 6.dp.toPx()))

    // Legs
    val footLX = centerX - 10.dp.toPx() - legSpread
    val footRX = centerX + 10.dp.toPx() + legSpread
    val footY = groundY - 6.dp.toPx() - jumpHeight

    drawSolidSneaker(Offset(footLX, footY), shoe, sole)
    drawSolidSneaker(Offset(footRX, footY), shoe, sole)

    drawSolidMuscularLimb(Offset(centerX, hipY), Offset((centerX + footLX) / 2f, (hipY + footY) / 2f), Offset(footLX, footY), 15.dp.toPx(), 12.dp.toPx(), skinNear)
    drawSolidMuscularLimb(Offset(centerX, hipY), Offset((centerX + footRX) / 2f, (hipY + footY) / 2f), Offset(footRX, footY), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    // Shorts
    drawSolidShorts(Offset(centerX - 16.dp.toPx(), hipY - 6.dp.toPx()), Offset(centerX + 16.dp.toPx(), hipY - 6.dp.toPx()), Offset(centerX, hipY + 14.dp.toPx()), shorts)

    // Torso & Head
    drawSolidTorso(Offset(centerX, torsoY), Offset(centerX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(centerX, headY), 13.dp.toPx(), skinNear, hair)

    // Arcing overhead arms
    val angle = depth * 135f
    val rad = Math.toRadians(angle.toDouble() + 45.0)
    val armLen = 42.dp.toPx()
    val armLX = centerX - (cos(rad).toFloat() * armLen)
    val armLY = (torsoY + 8.dp.toPx()) - (sin(rad).toFloat() * armLen)
    val armRX = centerX + (cos(rad).toFloat() * armLen)
    val armRY = armLY

    drawSolidMuscularLimb(Offset(centerX - 10.dp.toPx(), torsoY + 8.dp.toPx()), Offset(armLX * 0.7f + centerX * 0.3f, armLY * 0.7f + torsoY * 0.3f), Offset(armLX, armLY), 11.dp.toPx(), 8.dp.toPx(), skinNear)
    drawSolidMuscularLimb(Offset(centerX + 10.dp.toPx(), torsoY + 8.dp.toPx()), Offset(armRX * 0.7f + centerX * 0.3f, armRY * 0.7f + torsoY * 0.3f), Offset(armRX, armRY), 11.dp.toPx(), 8.dp.toPx(), skinNear)
}

/**
 * CRUNCHES: Solid lying athlete rolling up spine, six-pack core highlight.
 */
private fun DrawScope.renderSolidCrunches(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val curl = depth * 32.dp.toPx()
    val hipX = centerX - 12.dp.toPx()
    val kneeX = centerX + 40.dp.toPx()
    val footX = centerX + 64.dp.toPx()
    val headX = hipX - 54.dp.toPx() + (curl * 0.45f)
    val headY = groundY - 14.dp.toPx() - curl

    drawOval(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(centerX - 50.dp.toPx(), groundY - 2.dp.toPx()), size = Size(110.dp.toPx(), 7.dp.toPx()))

    // Bent legs
    drawSolidSneaker(Offset(footX, groundY - 4.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, groundY - 12.dp.toPx()), Offset(kneeX, groundY - 48.dp.toPx()), Offset(footX, groundY - 4.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    // Shorts
    drawSolidShorts(Offset(hipX - 10.dp.toPx(), groundY - 20.dp.toPx()), Offset(hipX + 12.dp.toPx(), groundY - 20.dp.toPx()), Offset(hipX + 16.dp.toPx(), groundY - 32.dp.toPx()), shorts)

    // Curled Spine Torso
    drawSolidTorso(Offset(headX + 12.dp.toPx(), headY + 8.dp.toPx()), Offset(hipX, groundY - 12.dp.toPx()), 24.dp.toPx(), 18.dp.toPx(), shirt)

    // Core Highlight
    if (glow > 0.25f) {
        val coreC = Offset(hipX - 18.dp.toPx(), groundY - 18.dp.toPx() - curl * 0.5f)
        drawCircle(brush = Brush.radialGradient(listOf(highlight.copy(alpha = 0.75f * glow), Color.Transparent), center = coreC, radius = 24.dp.toPx()), radius = 24.dp.toPx(), center = coreC)
    }

    // Hands supporting head
    drawSolidMuscularLimb(Offset(headX + 10.dp.toPx(), headY + 10.dp.toPx()), Offset(headX + 2.dp.toPx(), headY - 10.dp.toPx()), Offset(headX - 6.dp.toPx(), headY - 4.dp.toPx()), 10.dp.toPx(), 8.dp.toPx(), skinNear)
    drawSolidHead(Offset(headX, headY), 13.dp.toPx(), skinNear, hair)
}

/**
 * MOUNTAIN CLIMBERS: Solid high-cadence sprint in plank.
 */
private fun DrawScope.renderSolidMountainClimbers(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val drive = depth * 38.dp.toPx()
    val headX = centerX + 56.dp.toPx()
    val handsX = centerX + 38.dp.toPx()
    val hipsX = centerX - 14.dp.toPx()
    val hipsY = groundY - 44.dp.toPx()

    // Far leg (extended)
    drawSolidSneaker(Offset(centerX - 70.dp.toPx(), groundY - 6.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(Offset(hipsX, hipsY), Offset(centerX - 42.dp.toPx(), groundY - 24.dp.toPx()), Offset(centerX - 70.dp.toPx(), groundY - 6.dp.toPx()), 14.dp.toPx(), 11.dp.toPx(), skinFar)

    // Torso & Head
    drawSolidTorso(Offset(headX - 14.dp.toPx(), hipsY), Offset(hipsX, hipsY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(headX, hipsY - 10.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Supporting arms
    drawSolidMuscularLimb(Offset(headX - 14.dp.toPx(), hipsY), Offset(handsX + 6.dp.toPx(), groundY - 22.dp.toPx()), Offset(handsX, groundY - 4.dp.toPx()), 12.dp.toPx(), 10.dp.toPx(), skinNear)

    // Near driven knee
    val drivenKneeX = hipsX + 22.dp.toPx() + drive
    val drivenKneeY = hipsY + 12.dp.toPx()
    val drivenFootX = hipsX + drive
    drawSolidSneaker(Offset(drivenFootX, groundY - 10.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipsX, hipsY), Offset(drivenKneeX, drivenKneeY), Offset(drivenFootX, groundY - 10.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinNear)
}

/**
 * LUNGES: 90-90 solid split lunge.
 */
private fun DrawScope.renderSolidLunges(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val drop = depth * 34.dp.toPx()
    val hipX = centerX - 6.dp.toPx()
    val hipY = groundY - 68.dp.toPx() + drop
    val torsoY = hipY - 50.dp.toPx()

    val frontFootX = centerX + 38.dp.toPx()
    val frontKneeX = frontFootX
    val frontKneeY = groundY - 42.dp.toPx() + (drop * 0.5f)

    val backToeX = centerX - 50.dp.toPx()
    val backKneeX = centerX - 26.dp.toPx()
    val backKneeY = groundY - 12.dp.toPx() + (drop * 0.3f)

    // Back leg
    drawSolidSneaker(Offset(backToeX, groundY - 6.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(backKneeX, backKneeY), Offset(backToeX, groundY - 6.dp.toPx()), 14.dp.toPx(), 11.dp.toPx(), skinFar)

    // Front leg
    drawSolidSneaker(Offset(frontFootX, groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(frontKneeX, frontKneeY), Offset(frontFootX, groundY - 6.dp.toPx()), 16.dp.toPx(), 13.dp.toPx(), skinNear)

    // Shorts & Torso
    drawSolidShorts(Offset(hipX - 12.dp.toPx(), hipY - 6.dp.toPx()), Offset(hipX + 12.dp.toPx(), hipY - 6.dp.toPx()), Offset(hipX, hipY + 14.dp.toPx()), shorts)
    drawSolidTorso(Offset(hipX, torsoY), Offset(hipX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(hipX, torsoY - 15.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Hands on hips
    drawSolidMuscularLimb(Offset(hipX, torsoY + 10.dp.toPx()), Offset(hipX - 16.dp.toPx(), torsoY + 26.dp.toPx()), Offset(hipX, hipY), 10.dp.toPx(), 8.dp.toPx(), skinNear)
}

/**
 * GLUTE BRIDGE: Driving hips up, glute firing highlight.
 */
private fun DrawScope.renderSolidGluteBridge(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val lift = depth * 34.dp.toPx()
    val shoulderX = centerX - 54.dp.toPx()
    val hipX = centerX
    val kneeX = centerX + 48.dp.toPx()
    val footX = centerX + 58.dp.toPx()
    val hipY = groundY - 18.dp.toPx() - lift

    drawOval(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(shoulderX - 10.dp.toPx(), groundY - 2.dp.toPx()), size = Size(120.dp.toPx(), 7.dp.toPx()))

    // Bridge legs
    drawSolidSneaker(Offset(footX, groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(kneeX, groundY - 44.dp.toPx()), Offset(footX, groundY - 6.dp.toPx()), 16.dp.toPx(), 13.dp.toPx(), skinNear)

    // Shorts & Torso
    drawSolidShorts(Offset(hipX - 12.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 12.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 8.dp.toPx(), hipY + 16.dp.toPx()), shorts)
    drawSolidTorso(Offset(hipX, hipY), Offset(shoulderX, groundY - 12.dp.toPx()), 26.dp.toPx(), 20.dp.toPx(), shirt)

    // Glute activation glow
    if (glow > 0.3f) {
        drawCircle(brush = Brush.radialGradient(listOf(highlight.copy(alpha = 0.8f * glow), Color.Transparent), center = Offset(hipX, hipY), radius = 26.dp.toPx()), radius = 26.dp.toPx(), center = Offset(hipX, hipY))
    }

    // Hands & Head on mat
    drawSolidMuscularLimb(Offset(shoulderX, groundY - 12.dp.toPx()), Offset(shoulderX + 18.dp.toPx(), groundY - 8.dp.toPx()), Offset(shoulderX + 38.dp.toPx(), groundY - 6.dp.toPx()), 10.dp.toPx(), 8.dp.toPx(), skinNear)
    drawSolidHead(Offset(shoulderX - 16.dp.toPx(), groundY - 12.dp.toPx()), 13.dp.toPx(), skinNear, hair)
}

/**
 * TRICEPS DIPS
 */
private fun DrawScope.renderSolidTricepsDips(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val dip = depth * 32.dp.toPx()
    val benchX = centerX - 42.dp.toPx()
    val benchY = groundY - 38.dp.toPx()
    val hipX = centerX - 14.dp.toPx()
    val hipY = groundY - 46.dp.toPx() + dip

    // Bench
    drawRoundRect(shorts.copy(alpha = 0.4f), topLeft = Offset(benchX - 22.dp.toPx(), benchY), size = Size(26.dp.toPx(), 38.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))

    // Extended legs
    drawSolidSneaker(Offset(centerX + 54.dp.toPx(), groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(centerX + 22.dp.toPx(), groundY - 16.dp.toPx() + (dip * 0.3f)), Offset(centerX + 54.dp.toPx(), groundY - 6.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    // Shorts & Torso
    val shoulderY = hipY - 46.dp.toPx()
    drawSolidShorts(Offset(hipX - 12.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 12.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 8.dp.toPx(), hipY + 12.dp.toPx()), shorts)
    drawSolidTorso(Offset(hipX, shoulderY), Offset(hipX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(hipX, shoulderY - 15.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Triceps & Arms
    val elbowX = benchX + 4.dp.toPx()
    val elbowY = benchY - 16.dp.toPx() + (dip * 0.7f)
    drawSolidMuscularLimb(Offset(hipX, shoulderY + 8.dp.toPx()), Offset(elbowX, elbowY), Offset(benchX, benchY), 12.dp.toPx(), 10.dp.toPx(), skinNear)
}

/**
 * HIGH KNEES
 */
private fun DrawScope.renderSolidHighKnees(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val bounce = depth * 14.dp.toPx()
    val hipY = groundY - 72.dp.toPx() - bounce
    val torsoY = hipY - 48.dp.toPx()

    // Standing leg
    drawSolidSneaker(Offset(centerX - 10.dp.toPx(), groundY - 6.dp.toPx()), shoe.copy(alpha = 0.75f), sole)
    drawSolidMuscularLimb(Offset(centerX, hipY), Offset(centerX - 8.dp.toPx(), groundY - 38.dp.toPx()), Offset(centerX - 10.dp.toPx(), groundY - 6.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinFar)

    // Driven high knee
    val highFootX = centerX + 28.dp.toPx()
    drawSolidSneaker(Offset(highFootX, groundY - 26.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(centerX, hipY), Offset(centerX + 30.dp.toPx(), hipY - 6.dp.toPx()), Offset(highFootX, groundY - 26.dp.toPx()), 16.dp.toPx(), 13.dp.toPx(), skinNear)

    // Torso & Head
    drawSolidShorts(Offset(centerX - 14.dp.toPx(), hipY - 6.dp.toPx()), Offset(centerX + 14.dp.toPx(), hipY - 6.dp.toPx()), Offset(centerX, hipY + 14.dp.toPx()), shorts)
    drawSolidTorso(Offset(centerX, torsoY), Offset(centerX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(centerX, torsoY - 15.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Pumping arms
    drawSolidMuscularLimb(Offset(centerX, torsoY + 12.dp.toPx()), Offset(centerX - 18.dp.toPx(), hipY - 10.dp.toPx()), Offset(centerX - 26.dp.toPx(), hipY - 16.dp.toPx()), 10.dp.toPx(), 8.dp.toPx(), skinNear)
    drawSolidMuscularLimb(Offset(centerX, torsoY + 12.dp.toPx()), Offset(centerX + 18.dp.toPx(), hipY + 8.dp.toPx()), Offset(centerX + 26.dp.toPx(), hipY + 14.dp.toPx()), 10.dp.toPx(), 8.dp.toPx(), skinNear)
}

/**
 * WALL SIT
 */
private fun DrawScope.renderSolidWallSit(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val wallX = centerX - 30.dp.toPx()
    val hipX = wallX + 8.dp.toPx()
    val hipY = groundY - 46.dp.toPx()
    val torsoTop = hipY - 52.dp.toPx()
    val kneeX = hipX + 46.dp.toPx()

    // Wall bar
    drawLine(shorts.copy(alpha = 0.35f), start = Offset(wallX, groundY - 120.dp.toPx()), end = Offset(wallX, groundY), strokeWidth = 6.dp.toPx())

    // 90 degree legs
    drawSolidSneaker(Offset(kneeX, groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(kneeX, hipY), Offset(kneeX, groundY - 6.dp.toPx()), 16.dp.toPx(), 13.dp.toPx(), skinNear)

    // Quads pulse
    val pulse = depth * 5.dp.toPx()
    drawCircle(brush = Brush.radialGradient(listOf(highlight.copy(alpha = 0.75f * glow), Color.Transparent), center = Offset((hipX + kneeX) / 2f, hipY), radius = 24.dp.toPx() + pulse), radius = 24.dp.toPx() + pulse, center = Offset((hipX + kneeX) / 2f, hipY))

    // Torso flat against wall
    drawSolidShorts(Offset(hipX - 6.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 14.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 16.dp.toPx(), hipY + 10.dp.toPx()), shorts)
    drawSolidTorso(Offset(hipX, torsoTop), Offset(hipX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(hipX + 2.dp.toPx(), torsoTop - 15.dp.toPx()), 13.dp.toPx(), skinNear, hair)
}

/**
 * SUPERMAN
 */
private fun DrawScope.renderSolidSuperman(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val lift = depth * 24.dp.toPx()
    val hipX = centerX
    val hipY = groundY - 14.dp.toPx()
    val chestX = centerX + 40.dp.toPx()
    val chestY = groundY - 14.dp.toPx() - lift
    val feetX = centerX - 50.dp.toPx()
    val feetY = groundY - 14.dp.toPx() - (lift * 0.8f)

    drawSolidSneaker(Offset(feetX, feetY), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset((hipX + feetX) / 2f, (hipY + feetY) / 2f), Offset(feetX, feetY), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    drawSolidShorts(Offset(hipX - 12.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX + 12.dp.toPx(), hipY - 4.dp.toPx()), Offset(hipX - 14.dp.toPx(), hipY + 6.dp.toPx()), shorts)
    drawSolidTorso(Offset(chestX, chestY), Offset(hipX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(chestX + 12.dp.toPx(), chestY - 12.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Reaching arms
    drawSolidMuscularLimb(Offset(chestX, chestY), Offset(chestX + 18.dp.toPx(), chestY - 6.dp.toPx()), Offset(chestX + 34.dp.toPx(), chestY - 10.dp.toPx()), 11.dp.toPx(), 8.dp.toPx(), skinNear)
}

/**
 * COBRA STRETCH
 */
private fun DrawScope.renderSolidCobra(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val stretch = depth * 28.dp.toPx()
    val hipX = centerX - 26.dp.toPx()
    val chestX = centerX + 28.dp.toPx()
    val chestY = groundY - 46.dp.toPx() - stretch

    // Legs on floor
    drawSolidSneaker(Offset(centerX - 70.dp.toPx(), groundY - 10.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, groundY - 10.dp.toPx()), Offset(centerX - 48.dp.toPx(), groundY - 10.dp.toPx()), Offset(centerX - 70.dp.toPx(), groundY - 10.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinNear)

    // Torso arch
    drawSolidTorso(Offset(chestX, chestY), Offset(hipX, groundY - 10.dp.toPx()), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(chestX + 6.dp.toPx(), chestY - 15.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Arms pushing mat
    val handX = chestX + 6.dp.toPx()
    drawSolidMuscularLimb(Offset(chestX, chestY + 6.dp.toPx()), Offset(handX - 2.dp.toPx(), groundY - 26.dp.toPx()), Offset(handX, groundY - 6.dp.toPx()), 11.dp.toPx(), 9.dp.toPx(), skinNear)
}

/**
 * BICYCLE CRUNCHES
 */
private fun DrawScope.renderSolidBicycleCrunches(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val twist = depth * 26.dp.toPx()
    val hipX = centerX
    val hipY = groundY - 22.dp.toPx()
    val headX = hipX - 46.dp.toPx()
    val headY = groundY - 38.dp.toPx()

    // Alternating legs
    drawSolidSneaker(Offset(hipX + 56.dp.toPx() + twist, groundY - 32.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(hipX + 30.dp.toPx(), groundY - 24.dp.toPx()), Offset(hipX + 56.dp.toPx() + twist, groundY - 32.dp.toPx()), 15.dp.toPx(), 12.dp.toPx(), skinFar)

    val kneeX = hipX + 26.dp.toPx() - twist
    drawSolidSneaker(Offset(hipX + 50.dp.toPx() - twist, groundY - 24.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(Offset(hipX, hipY), Offset(kneeX, groundY - 50.dp.toPx()), Offset(hipX + 50.dp.toPx() - twist, groundY - 24.dp.toPx()), 16.dp.toPx(), 13.dp.toPx(), skinNear)

    // Torso & Rotating elbow
    drawSolidTorso(Offset(headX + 12.dp.toPx(), headY), Offset(hipX, hipY), 24.dp.toPx(), 18.dp.toPx(), shirt)
    drawSolidHead(Offset(headX, headY), 13.dp.toPx(), skinNear, hair)
    drawSolidMuscularLimb(Offset(headX + 8.dp.toPx(), headY + 4.dp.toPx()), Offset(hipX - 10.dp.toPx() + twist, headY - 8.dp.toPx()), Offset(hipX + twist * 0.5f, headY), 10.dp.toPx(), 8.dp.toPx(), skinNear)
}

/**
 * BURPEES
 */
private fun DrawScope.renderSolidBurpees(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    if (depth < 0.5f) {
        renderSolidPushUp(centerX, groundY, depth * 2f, glow, skinNear, skinFar, shirt, shorts, shoe, sole, hair, highlight)
    } else {
        renderSolidJumpingJacks(centerX, groundY, (depth - 0.5f) * 2f, glow, skinNear, skinFar, shirt, shorts, shoe, sole, hair, highlight)
    }
}

/**
 * TRICEPS EXTENSION (cuerpo a cuerpo, de rodillas)
 */
private fun DrawScope.renderSolidTricepsExtension(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val drop = depth * 26.dp.toPx()
    val handX = centerX + 42.dp.toPx()
    val handY = groundY - 4.dp.toPx()
    val shoulderX = centerX + 18.dp.toPx()
    val shoulderY = groundY - 54.dp.toPx() + drop
    val hipX = centerX - 18.dp.toPx()
    val hipY = groundY - 40.dp.toPx() + (drop * 0.45f)
    val kneeX = centerX - 46.dp.toPx()
    val kneeY = groundY - 22.dp.toPx()

    drawOval(
        color = Color.Black.copy(alpha = 0.07f + depth * 0.12f),
        topLeft = Offset(centerX - 30.dp.toPx(), groundY - 2.dp.toPx()),
        size = Size(96.dp.toPx(), 8.dp.toPx())
    )

    // Brazo lejano
    drawSolidMuscularLimb(
        Offset(shoulderX, shoulderY),
        Offset(handX - 18.dp.toPx(), handY - 20.dp.toPx() + drop * 0.3f),
        Offset(handX - 6.dp.toPx(), handY),
        9.dp.toPx(), 7.dp.toPx(), skinFar
    )

    // Pierna lejana con la espinilla apoyada
    drawSolidSneaker(Offset(centerX - 62.dp.toPx(), groundY - 12.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset(kneeX, kneeY),
        Offset(centerX - 62.dp.toPx(), groundY - 12.dp.toPx()),
        14.dp.toPx(), 11.dp.toPx(), skinFar
    )

    // Pierna cercana
    drawSolidSneaker(Offset(centerX - 58.dp.toPx(), groundY - 8.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset(kneeX + 4.dp.toPx(), kneeY + 2.dp.toPx()),
        Offset(centerX - 58.dp.toPx(), groundY - 8.dp.toPx()),
        15.dp.toPx(), 12.dp.toPx(), skinNear
    )

    // Shorts, torso y cabeza
    drawSolidShorts(
        Offset(hipX - 12.dp.toPx(), hipY - 2.dp.toPx()),
        Offset(hipX + 12.dp.toPx(), hipY),
        Offset(centerX - 26.dp.toPx(), groundY - 16.dp.toPx()),
        shorts
    )
    drawSolidTorso(Offset(shoulderX, shoulderY), Offset(hipX, hipY), 25.dp.toPx(), 19.dp.toPx(), shirt)
    drawSolidHead(Offset(shoulderX + 13.dp.toPx(), shoulderY - 12.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Activación del tríceps
    if (glow > 0.3f) {
        val glowCenter = Offset((shoulderX + handX) / 2f, (shoulderY + handY) / 2f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(highlight.copy(alpha = 0.65f * glow), Color.Transparent),
                center = glowCenter,
                radius = 22.dp.toPx()
            ),
            radius = 22.dp.toPx(),
            center = glowCenter
        )
    }

    // Brazo cercano
    drawSolidMuscularLimb(
        Offset(shoulderX, shoulderY),
        Offset(handX - 12.dp.toPx(), handY - 22.dp.toPx() + drop * 0.3f),
        Offset(handX, handY),
        11.dp.toPx(), 9.dp.toPx(), skinNear
    )
    drawCircle(skinNear, radius = 6.dp.toPx(), center = Offset(handX, handY))
}

/**
 * BICEPS CURL con auto-resistencia
 */
private fun DrawScope.renderSolidBicepsCurl(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val curl = depth
    val hipX = centerX - 4.dp.toPx()
    val hipY = groundY - 82.dp.toPx()
    val shoulderX = centerX + 2.dp.toPx()
    val shoulderY = groundY - 134.dp.toPx()

    drawOval(
        color = Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(centerX - 34.dp.toPx(), groundY - 2.dp.toPx()),
        size = Size(70.dp.toPx(), 7.dp.toPx())
    )

    // Pierna lejana
    drawSolidSneaker(Offset(centerX + 16.dp.toPx(), groundY - 6.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset(centerX + 12.dp.toPx(), groundY - 42.dp.toPx()),
        Offset(centerX + 16.dp.toPx(), groundY - 6.dp.toPx()),
        15.dp.toPx(), 12.dp.toPx(), skinFar
    )

    // Pierna cercana
    drawSolidSneaker(Offset(centerX - 6.dp.toPx(), groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset(centerX - 2.dp.toPx(), groundY - 40.dp.toPx()),
        Offset(centerX - 6.dp.toPx(), groundY - 6.dp.toPx()),
        16.dp.toPx(), 13.dp.toPx(), skinNear
    )

    // Shorts, torso y cabeza
    drawSolidShorts(
        Offset(hipX - 14.dp.toPx(), hipY - 4.dp.toPx()),
        Offset(hipX + 14.dp.toPx(), hipY - 4.dp.toPx()),
        Offset(hipX + 2.dp.toPx(), hipY + 12.dp.toPx()),
        shorts
    )
    drawSolidTorso(Offset(shoulderX, shoulderY), Offset(hipX, hipY), 27.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(shoulderX + 4.dp.toPx(), shoulderY - 16.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Brazo superior pegado a las costillas
    val elbow = Offset(shoulderX + 6.dp.toPx(), shoulderY + 40.dp.toPx())
    drawSolidMuscularLimb(
        Offset(shoulderX, shoulderY + 4.dp.toPx()), elbow,
        Offset(elbow.x + 2.dp.toPx(), elbow.y + 6.dp.toPx()),
        11.dp.toPx(), 9.dp.toPx(), skinNear
    )

    // El antebrazo sube desde la extensión hasta la flexión completa
    val extendedHand = Offset(elbow.x + 6.dp.toPx(), elbow.y + 34.dp.toPx())
    val flexedHand = Offset(elbow.x + 24.dp.toPx(), elbow.y - 6.dp.toPx())
    val hand = Offset(
        extendedHand.x + (flexedHand.x - extendedHand.x) * curl,
        extendedHand.y + (flexedHand.y - extendedHand.y) * curl
    )
    drawSolidMuscularLimb(
        elbow,
        Offset((elbow.x + hand.x) / 2f + 3.dp.toPx(), (elbow.y + hand.y) / 2f),
        hand,
        10.dp.toPx(), 8.dp.toPx(), skinNear
    )
    drawCircle(skinNear, radius = 5.5.dp.toPx(), center = hand)

    // Pico del bíceps en la flexión máxima
    if (glow > 0.3f) {
        val peak = Offset(elbow.x + 2.dp.toPx(), elbow.y - 6.dp.toPx())
        drawCircle(
            brush = Brush.radialGradient(
                listOf(highlight.copy(alpha = 0.7f * glow), Color.Transparent),
                center = peak,
                radius = 18.dp.toPx()
            ),
            radius = 18.dp.toPx(),
            center = peak
        )
    }
}

/**
 * BODYWEIGHT ROW: remo de pie con el propio peso o una toalla
 */
private fun DrawScope.renderSolidBodyweightRow(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val pull = depth
    val hipX = centerX - 10.dp.toPx()
    val hipY = groundY - 86.dp.toPx()
    val shoulderX = centerX + 16.dp.toPx()
    val shoulderY = groundY - 132.dp.toPx() + (pull * 4.dp.toPx())

    drawOval(
        color = Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(centerX - 34.dp.toPx(), groundY - 2.dp.toPx()),
        size = Size(70.dp.toPx(), 7.dp.toPx())
    )

    // Pierna lejana
    drawSolidSneaker(Offset(centerX + 16.dp.toPx(), groundY - 6.dp.toPx()), shoe.copy(alpha = 0.7f), sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset(centerX + 8.dp.toPx(), groundY - 44.dp.toPx()),
        Offset(centerX + 16.dp.toPx(), groundY - 6.dp.toPx()),
        15.dp.toPx(), 12.dp.toPx(), skinFar
    )

    // Pierna cercana
    drawSolidSneaker(Offset(centerX - 4.dp.toPx(), groundY - 6.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset(centerX - 2.dp.toPx(), groundY - 42.dp.toPx()),
        Offset(centerX - 4.dp.toPx(), groundY - 6.dp.toPx()),
        16.dp.toPx(), 13.dp.toPx(), skinNear
    )

    // Torso inclinado hacia adelante
    drawSolidShorts(
        Offset(hipX - 14.dp.toPx(), hipY - 4.dp.toPx()),
        Offset(hipX + 14.dp.toPx(), hipY - 4.dp.toPx()),
        Offset(hipX + 4.dp.toPx(), hipY + 12.dp.toPx()),
        shorts
    )
    drawSolidTorso(Offset(shoulderX, shoulderY), Offset(hipX, hipY), 27.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(shoulderX + 14.dp.toPx(), shoulderY - 12.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Los codos viajan desde delante del cuerpo hasta detrás de las costillas
    fun handPoint(extX: Float, extY: Float, flexX: Float, flexY: Float): Offset {
        val x = extX + (flexX - extX) * pull
        val y = extY + (flexY - extY) * pull
        return Offset(x, y)
    }

    // Brazo lejano
    val farElbow = handPoint(shoulderX + 30.dp.toPx(), shoulderY + 12.dp.toPx(), shoulderX - 10.dp.toPx(), shoulderY + 30.dp.toPx())
    val farHand = handPoint(shoulderX + 56.dp.toPx(), shoulderY + 18.dp.toPx(), shoulderX + 4.dp.toPx(), shoulderY + 38.dp.toPx())
    drawSolidMuscularLimb(Offset(shoulderX - 4.dp.toPx(), shoulderY + 6.dp.toPx()), farElbow, farHand, 9.dp.toPx(), 7.dp.toPx(), skinFar)

    // Brazo cercano
    val nearElbow = handPoint(shoulderX + 32.dp.toPx(), shoulderY + 16.dp.toPx(), shoulderX - 8.dp.toPx(), shoulderY + 34.dp.toPx())
    val nearHand = handPoint(shoulderX + 58.dp.toPx(), shoulderY + 22.dp.toPx(), shoulderX + 6.dp.toPx(), shoulderY + 42.dp.toPx())
    drawSolidMuscularLimb(Offset(shoulderX, shoulderY + 8.dp.toPx()), nearElbow, nearHand, 11.dp.toPx(), 9.dp.toPx(), skinNear)
    drawCircle(skinNear, radius = 5.5.dp.toPx(), center = nearHand)

    // Apriete de escápulas en el punto de máxima contracción
    if (glow > 0.3f) {
        val midBack = Offset((shoulderX + hipX) / 2f, (shoulderY + hipY) / 2f - 4.dp.toPx())
        drawCircle(
            brush = Brush.radialGradient(
                listOf(highlight.copy(alpha = 0.6f * glow), Color.Transparent),
                center = midBack,
                radius = 26.dp.toPx()
            ),
            radius = 26.dp.toPx(),
            center = midBack
        )
    }
}

/**
 * PRONE ELBOW LIFT: elevación de codos boca abajo
 */
private fun DrawScope.renderSolidProneElbowLift(
    centerX: Float, groundY: Float, depth: Float, glow: Float,
    skinNear: Color, skinFar: Color, shirt: Color, shorts: Color,
    shoe: Color, sole: Color, hair: Color, highlight: Color
) {
    val lift = depth * 18.dp.toPx()
    val hipX = centerX - 14.dp.toPx()
    val hipY = groundY - 16.dp.toPx()
    val chestX = centerX + 34.dp.toPx()
    val chestY = groundY - 18.dp.toPx() - lift
    val feetX = centerX - 58.dp.toPx()

    drawOval(
        color = Color.Black.copy(alpha = 0.07f),
        topLeft = Offset(centerX - 60.dp.toPx(), groundY - 2.dp.toPx()),
        size = Size(126.dp.toPx(), 8.dp.toPx())
    )

    // Piernas extendidas sobre la esterilla
    drawSolidSneaker(Offset(feetX, groundY - 10.dp.toPx()), shoe, sole)
    drawSolidMuscularLimb(
        Offset(hipX, hipY), Offset((hipX + feetX) / 2f, hipY + 3.dp.toPx()),
        Offset(feetX, groundY - 10.dp.toPx()),
        15.dp.toPx(), 12.dp.toPx(), skinNear
    )

    // Shorts, torso y cabeza
    drawSolidShorts(
        Offset(hipX - 12.dp.toPx(), hipY - 4.dp.toPx()),
        Offset(hipX + 12.dp.toPx(), hipY - 4.dp.toPx()),
        Offset(hipX - 14.dp.toPx(), hipY + 8.dp.toPx()),
        shorts
    )
    drawSolidTorso(Offset(chestX, chestY), Offset(hipX, hipY), 26.dp.toPx(), 20.dp.toPx(), shirt)
    drawSolidHead(Offset(chestX + 14.dp.toPx(), chestY - 8.dp.toPx()), 13.dp.toPx(), skinNear, hair)

    // Codos flexionados pegados al cuerpo, manos bajo los hombros
    val handX = chestX - 2.dp.toPx()
    val handY = groundY - 5.dp.toPx()
    val elbow = Offset(handX - 22.dp.toPx(), handY - 14.dp.toPx() - lift * 0.4f)
    drawSolidMuscularLimb(
        Offset(chestX - 6.dp.toPx(), chestY + 6.dp.toPx()), elbow,
        Offset(handX, handY),
        11.dp.toPx(), 9.dp.toPx(), skinNear
    )
    drawCircle(skinNear, radius = 5.5.dp.toPx(), center = Offset(handX, handY))

    // Contracción entre las escápulas
    if (glow > 0.3f) {
        val squeeze = Offset(chestX - 8.dp.toPx(), chestY + 2.dp.toPx())
        drawCircle(
            brush = Brush.radialGradient(
                listOf(highlight.copy(alpha = 0.7f * glow), Color.Transparent),
                center = squeeze,
                radius = 20.dp.toPx()
            ),
            radius = 20.dp.toPx(),
            center = squeeze
        )
    }
}

// -----------------------------------------------------------------------------------------
// GEOMETRIC SHAPE BUILDERS (Fills, Curvatures & Athletic Apparel)
// -----------------------------------------------------------------------------------------

/**
 * Draws a solid anatomical limb (2 segments: thigh/calf or bicep/forearm) with muscular curvature.
 */
private fun DrawScope.drawSolidMuscularLimb(
    p1: Offset, p2: Offset, p3: Offset,
    width1: Float, width2: Float, color: Color
) {
    // Segment 1 (P1 to P2)
    drawSolidBone(p1, p2, width1, width1 * 0.85f, color)
    // Joint cap (Knee / Elbow)
    drawCircle(color, radius = (width1 * 0.45f), center = p2)
    // Segment 2 (P2 to P3)
    drawSolidBone(p2, p3, width1 * 0.85f, width2 * 0.7f, color)
}

private fun DrawScope.drawSolidBone(
    start: Offset, end: Offset, w1: Float, w2: Float, color: Color
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = hypot(dx, dy)
    if (len < 1f) return

    val nx = -dy / len
    val ny = dx / len

    val p1L = Offset(start.x + nx * (w1 / 2f), start.y + ny * (w1 / 2f))
    val p1R = Offset(start.x - nx * (w1 / 2f), start.y - ny * (w1 / 2f))
    val p2L = Offset(end.x + nx * (w2 / 2f), end.y + ny * (w2 / 2f))
    val p2R = Offset(end.x - nx * (w2 / 2f), end.y - ny * (w2 / 2f))

    val path = Path().apply {
        moveTo(p1L.x, p1L.y)
        lineTo(p2L.x, p2L.y)
        lineTo(p2R.x, p2R.y)
        lineTo(p1R.x, p1R.y)
        close()
    }
    drawPath(path, color, style = Fill)
}

/**
 * Draws a solid athletic torso polygon wearing a sporty fitted jersey.
 */
private fun DrawScope.drawSolidTorso(
    topCenter: Offset, bottomCenter: Offset,
    widthTop: Float, widthBottom: Float, fillColor: Color
) {
    val dx = bottomCenter.x - topCenter.x
    val dy = bottomCenter.y - topCenter.y
    val len = hypot(dx, dy)
    if (len < 1f) return

    val nx = -dy / len
    val ny = dx / len

    val topL = Offset(topCenter.x + nx * (widthTop / 2f), topCenter.y + ny * (widthTop / 2f))
    val topR = Offset(topCenter.x - nx * (widthTop / 2f), topCenter.y - ny * (widthTop / 2f))
    val botL = Offset(bottomCenter.x + nx * (widthBottom / 2f), bottomCenter.y + ny * (widthBottom / 2f))
    val botR = Offset(bottomCenter.x - nx * (widthBottom / 2f), bottomCenter.y - ny * (widthBottom / 2f))

    val torsoPath = Path().apply {
        moveTo(topL.x, topL.y)
        lineTo(botL.x, botL.y)
        lineTo(botR.x, botR.y)
        lineTo(topR.x, topR.y)
        close()
    }
    drawPath(torsoPath, fillColor, style = Fill)
}

/**
 * Draws solid athletic shorts.
 */
private fun DrawScope.drawSolidShorts(
    waistL: Offset, waistR: Offset, legBottom: Offset, color: Color
) {
    val path = Path().apply {
        moveTo(waistL.x, waistL.y)
        lineTo(legBottom.x - 8.dp.toPx(), legBottom.y)
        lineTo(legBottom.x + 8.dp.toPx(), legBottom.y)
        lineTo(waistR.x, waistR.y)
        close()
    }
    drawPath(path, color, style = Fill)
}

/**
 * Draws a solid athletic sneaker with white rubber sole.
 */
private fun DrawScope.drawSolidSneaker(pos: Offset, upperColor: Color, soleColor: Color) {
    // Upper sneaker
    drawRoundRect(
        color = upperColor,
        topLeft = Offset(pos.x - 8.dp.toPx(), pos.y - 7.dp.toPx()),
        size = Size(18.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    // White rubber sole
    drawRoundRect(
        color = soleColor,
        topLeft = Offset(pos.x - 8.dp.toPx(), pos.y),
        size = Size(18.dp.toPx(), 3.5.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
}

/**
 * Draws sculpted head with styled hair and athletic profile.
 */
private fun DrawScope.drawSolidHead(center: Offset, radius: Float, skin: Color, hair: Color) {
    // Face & Neck base
    drawCircle(skin, radius = radius, center = center)
    // Athletic hair cap
    val hairPath = Path().apply {
        moveTo(center.x - radius, center.y)
        quadraticBezierTo(center.x, center.y - radius * 1.35f, center.x + radius, center.y - radius * 0.2f)
        lineTo(center.x + radius * 0.7f, center.y + radius * 0.3f)
        lineTo(center.x - radius * 0.8f, center.y + radius * 0.1f)
        close()
    }
    drawPath(hairPath, hair, style = Fill)
}
