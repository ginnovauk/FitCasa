package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.WorkoutSoundCuePlayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutSoundCuePlayerTest {

    private lateinit var context: Context
    private lateinit var player: WorkoutSoundCuePlayer

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Limpiamos preferencias previas
        context.getSharedPreferences("fitcasa_sound_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        player = WorkoutSoundCuePlayer(context)
    }

    @Test
    fun defaultState_isEnabledIsTrue() {
        assertTrue("Los pitidos deben estar activos por defecto", player.isEnabled)
    }

    @Test
    fun toggleSoundCues_persistsState() {
        player.isEnabled = false
        assertFalse(player.isEnabled)

        val newPlayerInstance = WorkoutSoundCuePlayer(context)
        assertFalse("El estado desactivado debe persistir en SharedPreferences", newPlayerInstance.isEnabled)

        newPlayerInstance.isEnabled = true
        assertTrue(newPlayerInstance.isEnabled)
    }

    @Test
    fun soundMethods_doNotCrashInHeadlessEnvironment() {
        // En entornos sin hardware de audio (como tests unitarios o Robolectric),
        // las funciones deben ejecutarse de forma segura sin lanzar excepciones.
        player.playCountdownBeep()
        player.playStartOrCompleteTone()
        player.previewBeep()
        player.release()
    }
}
