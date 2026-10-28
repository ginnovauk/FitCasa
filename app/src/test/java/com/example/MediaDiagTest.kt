package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.Exercise
import com.example.data.repository.ExerciseCatalog
import com.example.ui.components.ExerciseMediaView
import com.example.ui.theme.FitCasaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/** TEMPORAL: cada animacion aislada, tamano real de la tarjeta y varios fotogramas. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h873dp-440dpi")
class MediaDiagTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val withMedia: List<Exercise> = ExerciseCatalog.allExercises
        .filter { it.lottieAsset != null }
        .distinctBy { it.lottieAsset }

    private var index by mutableIntStateOf(0)

    @Test
    fun `tarjeta real`() {
        run(prefix = "card", cardSize = true)
    }

    @Test
    fun `pantalla completa`() {
        run(prefix = "full", cardSize = false)
    }

    private fun run(prefix: String, cardSize: Boolean) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            FitCasaTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    ExerciseMediaView(
                        exercise = withMedia[index],
                        modifier = if (cardSize) {
                            Modifier.size(width = 292.dp, height = 249.dp)
                        } else {
                            Modifier.size(width = 292.dp, height = 340.dp)
                        }
                    )
                }
            }
        }

        val dir = File("build/media-diag-$prefix").apply { mkdirs() }
        withMedia.forEachIndexed { i, exercise ->
            composeRule.runOnUiThread { index = i }
            repeat(4) {
                Thread.sleep(250)
                composeRule.mainClock.advanceTimeBy(220)
            }
            repeat(4) { step ->
                composeRule.mainClock.advanceTimeBy(600)
                composeRule.mainClock.advanceTimeByFrame()
                capture(File(dir, "${exercise.id}-$step.png"))
            }
        }
    }

    private fun capture(out: File) {
        val content = composeRule.activity.findViewById<ViewGroup>(android.R.id.content)
        val root: View = (0 until content.childCount)
            .map { content.getChildAt(it) }
            .firstOrNull { it is ComposeView } ?: content
        composeRule.runOnUiThread {
            root.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(2400, View.MeasureSpec.EXACTLY)
            )
            root.layout(0, 0, 1080, 2400)
        }
        val bitmap = Bitmap.createBitmap(1080, 2400, Bitmap.Config.ARGB_8888)
        composeRule.runOnUiThread { root.draw(Canvas(bitmap)) }
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
