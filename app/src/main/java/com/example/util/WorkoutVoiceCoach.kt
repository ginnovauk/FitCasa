package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.annotation.StringRes
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Una voz concreta disponible en el motor de texto a voz del dispositivo.
 * El identificador es el nombre interno del motor (por ejemplo "es-es-x-eea-local").
 */
data class VoiceOption(
    val id: String,
    val displayName: String,
    val qualityLabel: String,
    val requiresNetwork: Boolean
)

/**
 * Estado que consume la interfaz para mostrar el panel de configuración de la voz.
 */
data class VoiceCoachUiState(
    val isEnabled: Boolean = true,
    val isReady: Boolean = false,
    val engineLabel: String = "",
    val voices: List<VoiceOption> = emptyList(),
    val selectedVoiceId: String? = null,
    val speechRate: Float = DEFAULT_RATE,
    val speechPitch: Float = DEFAULT_PITCH,
    val needsVoiceData: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        const val DEFAULT_RATE = 1.0f
        const val DEFAULT_PITCH = 1.0f
        val RATE_OPTIONS = listOf(0.9f, 1.0f, 1.12f)
        val PITCH_OPTIONS = listOf(0.9f, 1.0f, 1.1f)
    }
}

/**
 * Entrenador por voz basado en el motor TTS de Android.
 *
 * Mejoras respecto a la primera versión:
 *  - Usa explícitamente el motor de voz de Google cuando está instalado, con vuelta atrás al motor del sistema.
 *  - Selecciona automáticamente la mejor voz en español instalada (sin conexión, de mayor calidad y de España).
 *  - Envía el audio como "voz" a través de atributos de audio, con foco temporal que atenúa la música del usuario.
 *  - Permite elegir otra voz y la velocidad, y recuerda la preferencia entre sesiones.
 *  - Ajusta también el tono (grave/natural/agudo) y varía las frases de ánimo.
 *  - Si una voz de red falla, cambia sola a una voz local para no quedarse mudo.
 *  - Evita que los subtítulos desaparezcan cuando una frase interrumpe a la anterior.
 *  - Avisa cuando faltan las voces en español para poder descargarlas.
 */
class WorkoutVoiceCoach(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    /** Idioma de la app (respeta el selector de idioma por aplicación). */
    private val appLocale: Locale = appContext.resources.configuration.locales[0]

    private val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var usingGoogleEngine = false
    private var triedDefaultEngine = false
    private var pendingPreview = false
    private var encouragementIndex = 0
    private var networkFallbackDone = false
    private var lastCueAtMillis = 0L

    private var hasAudioFocus = false
    private var audioFocusRequest: AudioFocusRequest? = null

    private val utteranceCounter = AtomicInteger(0)
    private var currentUtteranceId: String? = null

    private val _uiState = MutableStateFlow(
        VoiceCoachUiState(
            isEnabled = prefs.getBoolean(KEY_ENABLED, true),
            engineLabel = appContext.getString(R.string.voice_engine_detecting),
            speechRate = prefs.getFloat(KEY_RATE, VoiceCoachUiState.DEFAULT_RATE),
            speechPitch = prefs.getFloat(KEY_PITCH, VoiceCoachUiState.DEFAULT_PITCH)
        )
    )
    val uiState: StateFlow<VoiceCoachUiState> = _uiState.asStateFlow()

    private val _currentVoiceMessage = MutableStateFlow<String?>(null)
    val currentVoiceMessage: StateFlow<String?> = _currentVoiceMessage.asStateFlow()

    private val voiceComparator = compareByDescending<Voice> {
        it.locale.country.equals(appLocale.country, ignoreCase = true)
    }
        // Las voces de red del motor de Google son las que suenan naturales;
        // si fallan (sin datos), fallbackToLocalVoice cambia a una local.
        .thenByDescending { it.isNetworkConnectionRequired }
        .thenByDescending { it.quality }
        .thenBy { it.name }

    // ---------------------------------------------------------------------------------
    // Inicialización del motor
    // ---------------------------------------------------------------------------------

    private fun createEngine(preferGoogle: Boolean) {
        tts?.shutdown()
        tts = null
        isInitialized = false
        usingGoogleEngine = preferGoogle
        tts = TextToSpeech(
            appContext,
            this,
            if (preferGoogle) GOOGLE_TTS_ENGINE else null
        )
    }

    @Suppress("DEPRECATION")
    private fun isGoogleEngineInstalled(): Boolean = try {
        appContext.packageManager.getPackageInfo(GOOGLE_TTS_ENGINE, 0)
        true
    } catch (_: Exception) {
        false
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            // Si el motor de Google está instalado pero rechaza la inicialización,
            // reintentamos una sola vez con el motor predeterminado del sistema.
            if (usingGoogleEngine && !triedDefaultEngine) {
                triedDefaultEngine = true
                Log.w(TAG, "El motor de Google no respondió; usando el motor del sistema.")
                createEngine(preferGoogle = false)
                return
            }

            Log.w(TAG, "No se pudo iniciar el motor de voz (status=$status)")
            _uiState.update {
                it.copy(
                    isReady = false,
                    engineLabel = appContext.getString(R.string.voice_engine_missing),
                    needsVoiceData = true,
                    errorMessage = appContext.getString(R.string.voice_error_no_engine)
                )
            }
            return
        }

        val engine = tts ?: return
        isInitialized = true

        engine.setAudioAttributes(audioAttributes)
        engine.setPitch(_uiState.value.speechPitch)
        engine.setSpeechRate(_uiState.value.speechRate)
        engine.setOnUtteranceProgressListener(progressListener)

        val needsVoiceData = configureVoice(engine)

        _uiState.update {
            it.copy(
                isReady = true,
                engineLabel = appContext.getString(
                    if (usingGoogleEngine) R.string.voice_engine_google else R.string.voice_engine_system
                ),
                needsVoiceData = needsVoiceData,
                errorMessage = if (needsVoiceData) {
                    appContext.getString(R.string.voice_error_missing_data)
                } else {
                    null
                }
            )
        }

        if (pendingPreview) {
            pendingPreview = false
            previewVoice()
        }
    }

    /**
     * Aplica la voz guardada o la mejor voz del idioma de la app disponible.
     * Devuelve true cuando el dispositivo no tiene datos de voz en ese idioma.
     */
    private fun configureVoice(engine: TextToSpeech): Boolean {
        val storedVoiceId = prefs.getString(KEY_VOICE_ID, null)

        val languageVoices = engine.voices.orEmpty()
            .filter { it.locale.language.equals(appLocale.language, ignoreCase = true) }
            .filterNot { it.isMissingData() }
            // El motor expone entradas "es-ES-language" que no son voces reales:
            // al elegirlas la calidad cae y el entrenador suena metálico.
            .filterNot { it.name.endsWith("-language", ignoreCase = true) }
            .sortedWith(voiceComparator)

        val chosenVoice = languageVoices.firstOrNull { it.name == storedVoiceId }
            ?: languageVoices.firstOrNull()

        Log.d(
            TAG,
            "Voces en ${appLocale.language}: " + languageVoices.joinToString { voice ->
                "${voice.name} (calidad ${voice.quality}, " +
                    if (voice.isNetworkConnectionRequired) "red)" else "sin conexión)"
            }
        )

        if (chosenVoice != null && engine.setVoice(chosenVoice) == TextToSpeech.SUCCESS) {
            publishVoices(engine, languageVoices)
            return false
        }

        // Vuelta atrás: pedimos el idioma en lugar de una voz concreta.
        val result = engine.setLanguage(appLocale)
        val needsVoiceData = result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED

        if (needsVoiceData) {
            engine.setLanguage(Locale(appLocale.language))
        }

        publishVoices(engine, languageVoices)
        return needsVoiceData
    }

    private fun publishVoices(engine: TextToSpeech, voices: List<Voice>) {
        val options = voices.map { voice ->
            VoiceOption(
                id = voice.name,
                displayName = displayNameFor(voice, voices),
                qualityLabel = qualityLabel(voice.quality),
                requiresNetwork = voice.isNetworkConnectionRequired
            )
        }

        val activeVoiceId = engine.voice?.name?.takeIf { id -> voices.any { it.name == id } }

        _uiState.update {
            it.copy(
                voices = options,
                selectedVoiceId = activeVoiceId
            )
        }
    }

    private fun Voice.isMissingData(): Boolean =
        features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true

    private fun displayNameFor(voice: Voice, allVoices: List<Voice>): String {
        // getDisplayName ya devuelve el nombre del idioma en el idioma de la app
        // ("español (México)", "Spanish (Mexico)"…), sin listas hardcodeadas.
        val localeName = voice.locale.getDisplayName(appLocale)
            .replaceFirstChar { it.uppercase(appLocale) }

        val sameLocale = allVoices.filter { it.locale.country == voice.locale.country }
        return if (sameLocale.size > 1) {
            appContext.getString(R.string.voice_voice_number, localeName, sameLocale.indexOf(voice) + 1)
        } else {
            localeName
        }
    }

    private fun qualityLabel(quality: Int): String = when {
        quality >= Voice.QUALITY_VERY_HIGH -> appContext.getString(R.string.voice_quality_max)
        quality >= Voice.QUALITY_HIGH -> appContext.getString(R.string.voice_quality_high)
        quality >= Voice.QUALITY_NORMAL -> appContext.getString(R.string.voice_quality_normal)
        else -> appContext.getString(R.string.voice_quality_basic)
    }

    // ---------------------------------------------------------------------------------
    // Configuración expuesta a la interfaz
    // ---------------------------------------------------------------------------------

    fun toggleVoice() {
        val newState = !_uiState.value.isEnabled
        _uiState.update { it.copy(isEnabled = newState) }
        prefs.edit().putBoolean(KEY_ENABLED, newState).apply()

        if (!newState) {
            stop()
        } else {
            speak(appContext.getString(R.string.voice_speak_enabled), force = true)
        }
    }

    fun selectVoice(voiceId: String) {
        val engine = tts ?: return
        val voice = engine.voices?.firstOrNull { it.name == voiceId } ?: return

        if (engine.setVoice(voice) == TextToSpeech.SUCCESS) {
            prefs.edit().putString(KEY_VOICE_ID, voiceId).apply()
            networkFallbackDone = false
            _uiState.update {
                it.copy(
                    selectedVoiceId = voiceId,
                    needsVoiceData = false,
                    errorMessage = null
                )
            }
            previewVoice()
        }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.7f, 1.4f)
        tts?.setSpeechRate(clamped)
        prefs.edit().putFloat(KEY_RATE, clamped).apply()
        _uiState.update { it.copy(speechRate = clamped) }
        previewVoice()
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.6f, 1.4f)
        tts?.setPitch(clamped)
        prefs.edit().putFloat(KEY_PITCH, clamped).apply()
        _uiState.update { it.copy(speechPitch = clamped) }
        previewVoice()
    }

    fun previewVoice() {
        if (!isInitialized) {
            pendingPreview = true
            return
        }
        speakInternal(appContext.getString(R.string.voice_preview_phrase), force = true, showSubtitle = false)
    }

    // ---------------------------------------------------------------------------------
    // Reproducción de voz
    // ---------------------------------------------------------------------------------

    fun speak(text: String, force: Boolean = false) {
        speakInternal(text, force = force)
    }

    private fun speakInternal(
        text: String,
        force: Boolean = false,
        showSubtitle: Boolean = true,
        optional: Boolean = false
    ) {
        if (!force && !_uiState.value.isEnabled) return

        val engine = tts
        if (!isInitialized || engine == null) return

        // Un aviso opcional (mitad del ejercicio, "diez segundos") no debe cortar
        // lo que el entrenador esté diciendo: si ya está hablando, se omite.
        if (optional && isTalking()) return

        // Con las voces de red el motor a veces informa de que no habla mientras
        // aún suena: tampoco se permite pisar un aviso recién empezado.
        if (optional && System.currentTimeMillis() - lastCueAtMillis < OPTIONAL_CUE_GAP_MS) {
            return
        }

        val utteranceId = "fitcasa_${utteranceCounter.incrementAndGet()}"
        currentUtteranceId = utteranceId

        if (showSubtitle) {
            _currentVoiceMessage.value = text
        }

        requestAudioFocus()
        lastCueAtMillis = System.currentTimeMillis()

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
        }

        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (result == TextToSpeech.ERROR && utteranceId == currentUtteranceId) {
            currentUtteranceId = null
            _currentVoiceMessage.value = null
            abandonAudioFocus()
        }
    }

    fun speakExerciseStart(exerciseName: String, durationOrReps: String) {
        speakInternal(
            appContext.getString(
                R.string.voice_speak_start,
                nameForSpeech(exerciseName),
                durationOrReps,
                nextEncouragement()
            )
        )
    }

    fun speakHalfway() {
        speakInternal(appContext.getString(R.string.voice_speak_halfway), optional = true)
    }

    fun speakTenSecondsLeft() {
        speakInternal(appContext.getString(R.string.voice_speak_ten_left), optional = true)
    }

    /**
     * Un número por segundo, para que "tres… dos… uno…" suene al ritmo de la
     * pantalla. Cada número es tan corto que le da tiempo a terminar antes de que
     * llegue el siguiente, aunque la voz sea de red.
     */
    fun speakCountdownNumber(number: Int) {
        val res = when (number) {
            3 -> R.string.voice_countdown_3
            2 -> R.string.voice_countdown_2
            1 -> R.string.voice_countdown_1
            else -> return
        }
        speakInternal(appContext.getString(res))
    }

    fun speakRestStart(restSeconds: Int, nextExerciseName: String) {
        speakInternal(
            appContext.getString(
                R.string.voice_speak_rest,
                restSeconds,
                nameForSpeech(nextExerciseName)
            )
        )
    }

    fun speakPrepareNext(nextExerciseName: String) {
        speakInternal(
            appContext.getString(R.string.voice_speak_prepare, nameForSpeech(nextExerciseName))
        )
    }

    fun speakWorkoutCompleted(calories: Int, minutes: Int) {
        speakInternal(
            appContext.getString(R.string.voice_speak_completed, calories, minutes)
        )
    }

    fun speakPaused() {
        speakInternal(appContext.getString(R.string.voice_speak_paused))
    }

    fun speakResumed() {
        speakInternal(appContext.getString(R.string.voice_speak_resumed))
    }

    /**
     * Alterna los ánimos para que el entrenador no repita la misma frase en cada
     * ejercicio, que es lo que más delata a una voz sintética.
     */
    private fun nextEncouragement(): String {
        val phrases = appContext.resources.getStringArray(R.array.voice_encouragements)
        if (phrases.isEmpty()) return ""
        val phrase = phrases[encouragementIndex % phrases.size]
        encouragementIndex += 1
        return phrase
    }

    /**
     * Los nombres del catálogo incluyen aclaraciones entre paréntesis
     * ("Saltos de Tijera (Jumping Jacks)"). Al hablar suenan mejor sin ellas.
     */
    private fun nameForSpeech(name: String): String =
        name.substringBefore("(").trim().ifBlank { name }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) {
            finishUtterance(utteranceId)
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            finishUtterance(utteranceId)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            finishUtterance(utteranceId)
            fallbackToLocalVoice()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            finishUtterance(utteranceId)
        }
    }

    private fun finishUtterance(utteranceId: String?) {
        // Si una frase nueva ya tomó el relevo, la anterior no debe borrar los subtítulos.
        if (utteranceId != currentUtteranceId) return

        currentUtteranceId = null
        _currentVoiceMessage.value = null
        abandonAudioFocus()
    }

    /**
     * Hay una locución en curso (o esperando síntesis, que en las voces de red
     * tarda un poco en arrancar).
     */
    private fun isTalking(): Boolean =
        currentUtteranceId != null || tts?.isSpeaking == true

    /**
     * Si la voz elegida necesita internet y falla (sin datos, servidor caído),
     * cambiamos una sola vez a la mejor voz local para que el entrenador no se
     * quede mudo a mitad de la rutina.
     */
    private fun fallbackToLocalVoice() {
        if (networkFallbackDone) return
        val engine = tts ?: return
        val current = engine.voice ?: return
        if (!current.isNetworkConnectionRequired) return

        val localVoice = engine.voices.orEmpty()
            .filter { it.locale.language.equals(appLocale.language, ignoreCase = true) }
            .filterNot { it.isNetworkConnectionRequired || it.isMissingData() }
            .sortedWith(voiceComparator)
            .firstOrNull() ?: return

        if (engine.setVoice(localVoice) == TextToSpeech.SUCCESS) {
            networkFallbackDone = true
            prefs.edit().putString(KEY_VOICE_ID, localVoice.name).apply()
            _uiState.update { it.copy(selectedVoiceId = localVoice.name) }
            _currentVoiceMessage.value = appContext.getString(R.string.voice_speak_fallback)
        }
    }

    // Se crea al final para que todos los campos de la clase ya estén inicializados
    // cuando el motor invoque la devolución de llamada onInit.
    init {
        createEngine(preferGoogle = isGoogleEngineInstalled())
    }

    // ---------------------------------------------------------------------------------
    // Foco de audio: baja el volumen de la música mientras habla el entrenador
    // ---------------------------------------------------------------------------------

    private fun requestAudioFocus() {
        if (hasAudioFocus) return
        val manager = audioManager ?: return

        hasAudioFocus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = audioFocusRequest ?: AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { }
                .build()
                .also { audioFocusRequest = it }

            manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        hasAudioFocus = false

        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { manager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(null)
        }
    }

    // ---------------------------------------------------------------------------------
    // Ciclo de vida
    // ---------------------------------------------------------------------------------

    fun stop() {
        currentUtteranceId = null
        tts?.stop()
        _currentVoiceMessage.value = null
        abandonAudioFocus()
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private companion object {
        const val TAG = "WorkoutVoiceCoach"
        const val PREFS_NAME = "fitcasa_voice_coach"
        const val KEY_ENABLED = "voice_enabled"
        const val KEY_VOICE_ID = "voice_id"
        const val KEY_RATE = "voice_rate"
        const val KEY_PITCH = "voice_pitch"
        const val GOOGLE_TTS_ENGINE = "com.google.android.tts"
        const val OPTIONAL_CUE_GAP_MS = 5_000L
    }
}
