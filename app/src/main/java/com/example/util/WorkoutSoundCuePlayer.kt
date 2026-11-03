package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Reproductor de señales acústicas (pitidos) para los intervalos de entrenamiento.
 *
 * Utiliza ToneGenerator sobre STREAM_MUSIC para garantizar latencia cero,
 * reproducirse a través del altavoz o auriculares Bluetooth y funcionar
 * sin conexión y sin consumir recursos ni dependencias externas.
 */
class WorkoutSoundCuePlayer(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_CUES_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND_CUES_ENABLED, value).apply()
        }

    private var toneGenerator: ToneGenerator? = null

    init {
        initToneGenerator()
    }

    private fun initToneGenerator() {
        toneGenerator = runCatching {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        }.getOrNull()
    }

    /**
     * Pitido corto para los últimos 3 segundos (3, 2, 1).
     */
    fun playCountdownBeep() {
        if (!isEnabled) return
        runCatching {
            ensureToneGenerator()
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        }
    }

    /**
     * Tono más largo y agudo para indicar inicio de ejercicio o fin de descanso.
     */
    fun playStartOrCompleteTone() {
        if (!isEnabled) return
        runCatching {
            ensureToneGenerator()
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 260)
        }
    }

    /**
     * Pitido de prueba para la pantalla de ajustes.
     */
    fun previewBeep() {
        runCatching {
            ensureToneGenerator()
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
    }

    private fun ensureToneGenerator() {
        if (toneGenerator == null) {
            initToneGenerator()
        }
    }

    fun release() {
        runCatching {
            toneGenerator?.release()
            toneGenerator = null
        }
    }

    companion object {
        private const val PREFS_NAME = "fitcasa_sound_prefs"
        private const val KEY_SOUND_CUES_ENABLED = "sound_cues_enabled"
    }
}
