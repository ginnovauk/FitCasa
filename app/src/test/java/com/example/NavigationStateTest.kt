package com.example

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.theme.FitCasaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Al girar el móvil (o al recrear la Activity) el estado de navegación tiene
 * que sobrevivir. Antes se perdía y la app volvía a Inicio mientras la sesión
 * seguía corriendo en el ViewModel.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class NavigationStateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `la pestana se conserva al restaurar el estado`() {
        val tester = StateRestorationTester(composeRule)
        tester.setContent {
            FitCasaTheme { FitCasaApp() }
        }

        composeRule.onNodeWithTag("nav_builder").performClick()
        composeRule.onNodeWithTag("builder_back_button").assertIsDisplayed()

        tester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithTag("builder_back_button").assertIsDisplayed()
    }

    @Test
    fun `sin plan no se restaura una pantalla de entrenamiento`() {
        assertEquals(ScreenState.TABS, restoredScreen(ScreenState.ACTIVE_WORKOUT, hasPlan = false))
        assertEquals(ScreenState.TABS, restoredScreen(ScreenState.SUMMARY, hasPlan = false))
        assertEquals(
            ScreenState.ACTIVE_WORKOUT,
            restoredScreen(ScreenState.ACTIVE_WORKOUT, hasPlan = true)
        )
        assertEquals(ScreenState.SUMMARY, restoredScreen(ScreenState.SUMMARY, hasPlan = true))
    }
}
