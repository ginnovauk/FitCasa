package com.example.util

/**
 * Modo de avisos durante el entrenamiento.
 *
 * Es la única fuente de verdad de los dos interruptores independientes
 * (voz del entrenador y pitidos de cuenta atrás): el usuario elige una de
 * estas cuatro opciones y ambos estados quedan ajustados a la vez.
 */
enum class AudioCueMode(val voiceOn: Boolean, val beepsOn: Boolean) {
    /** Solo voz del entrenador. */
    VOICE(voiceOn = true, beepsOn = false),

    /** Solo pitidos acústicos. */
    BEEPS(voiceOn = false, beepsOn = true),

    /** Voz y pitidos juntos. */
    BOTH(voiceOn = true, beepsOn = true),

    /** Sin avisos: ni voz ni pitidos. */
    NONE(voiceOn = false, beepsOn = false);

    companion object {
        /** Modo resultante de combinar los dos interruptores actuales. */
        fun from(voiceOn: Boolean, beepsOn: Boolean): AudioCueMode = when {
            voiceOn && beepsOn -> BOTH
            voiceOn -> VOICE
            beepsOn -> BEEPS
            else -> NONE
        }
    }
}
