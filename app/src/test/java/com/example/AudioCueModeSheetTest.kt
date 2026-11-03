package com.example

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.components.VoiceCoachSettingsSheet
import com.example.util.AudioCueMode
import com.example.util.VoiceCoachUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Comprueba el selector de cuatro modos de aviso (voz, pitidos, ambos, silencio)
 * dentro del panel de ajustes del entrenamiento.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class AudioCueModeSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun voiceState(enabled: Boolean) = VoiceCoachUiState(
        isEnabled = enabled,
        isReady = true,
        engineLabel = "Motor de voz"
    )

    private fun openSheet(
        voiceOn: Boolean,
        beepsOn: Boolean,
        onSelect: (AudioCueMode) -> Unit = {}
    ) {
        composeRule.setContent {
            VoiceCoachSettingsSheet(
                state = voiceState(voiceOn),
                isSoundCuesEnabled = beepsOn,
                onDismiss = {},
                onToggleEnabled = {},
                onSelectVoice = {},
                onSelectRate = {},
                onSelectPitch = {},
                onPreview = {},
                onSelectAudioCueMode = onSelect
            )
        }
    }

    @Test
    fun showsTheFourModes() {
        openSheet(voiceOn = true, beepsOn = true)

        AudioCueMode.entries.forEach { mode ->
            composeRule.onNodeWithTag("cue_mode_${mode.name}")
                .performScrollTo()
                .assertIsDisplayed()
        }
        composeRule.onNodeWithTag("cue_mode_BOTH")
            .performScrollTo()
            .assertIsSelected()
    }

    @Test
    fun silentMode_isSelectedWhenVoiceAndBeepsAreOff() {
        openSheet(voiceOn = false, beepsOn = false)

        composeRule.onNodeWithTag("cue_mode_NONE").assertIsSelected()
        composeRule.onNodeWithTag("cue_mode_BOTH").assertIsNotSelected()
    }

    @Test
    fun tappingAMode_reportsItToTheCaller() {
        var selected: AudioCueMode? = null
        openSheet(voiceOn = true, beepsOn = true, onSelect = { selected = it })

        composeRule.onNodeWithTag("cue_mode_BEEPS").performClick()
        composeRule.waitForIdle()

        assertEquals(AudioCueMode.BEEPS, selected)
    }
}
