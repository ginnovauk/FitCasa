package com.example

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airbnb.lottie.LottieCompositionFactory
import com.example.data.repository.ExerciseCatalog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** TEMPORAL: comprobar la carga desde assets. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h873dp-440dpi")
class AssetLoadTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `carga desde assets`() {
        val ctx = composeRule.activity.applicationContext
        val assets = (ExerciseCatalog.allExercises + ExerciseCatalog.warmUpExercises)
            .mapNotNull { it.lottieAsset }
            .distinct()

        val lines = StringBuilder()
        assets.forEach { path ->
            val result = runCatching {
                LottieCompositionFactory.fromAssetSync(ctx, path)
            }
            val comp = result.getOrNull()?.value
            val err = result.exceptionOrNull()
            lines.appendLine(
                "%-40s comp=%s err=%s".format(
                    path,
                    if (comp != null) "OK capas=${comp.layers.size}" else "NULL",
                    err?.let { "${it.javaClass.simpleName}: ${it.message}" } ?: "-"
                )
            )
        }
        java.io.File("build/asset-load.txt").writeText(lines.toString())
    }
}
