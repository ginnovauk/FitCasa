package com.example

import com.example.util.AudioCueMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioCueModeTest {

    @Test
    fun from_mapsTheFourCombinations() {
        assertEquals(AudioCueMode.BOTH, AudioCueMode.from(voiceOn = true, beepsOn = true))
        assertEquals(AudioCueMode.VOICE, AudioCueMode.from(voiceOn = true, beepsOn = false))
        assertEquals(AudioCueMode.BEEPS, AudioCueMode.from(voiceOn = false, beepsOn = true))
        assertEquals(AudioCueMode.NONE, AudioCueMode.from(voiceOn = false, beepsOn = false))
    }

    @Test
    fun modeFlags_describeTheTwoSwitchesTheyEnable() {
        assertTrue(AudioCueMode.VOICE.voiceOn)
        assertFalse(AudioCueMode.VOICE.beepsOn)

        assertFalse(AudioCueMode.BEEPS.voiceOn)
        assertTrue(AudioCueMode.BEEPS.beepsOn)

        assertTrue(AudioCueMode.BOTH.voiceOn)
        assertTrue(AudioCueMode.BOTH.beepsOn)

        assertFalse(AudioCueMode.NONE.voiceOn)
        assertFalse(AudioCueMode.NONE.beepsOn)
    }

    @Test
    fun from_isConsistentWithEveryModeFlags() {
        AudioCueMode.entries.forEach { mode ->
            assertEquals(
                "El modo $mode debe reconstruirse a partir de sus dos interruptores",
                mode,
                AudioCueMode.from(mode.voiceOn, mode.beepsOn)
            )
        }
    }
}
