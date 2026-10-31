package com.example.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Idioma elegido dentro de FitCasa.
 *
 * En Android 13+ delega en [LocaleManager], el mismo mecanismo que el selector
 * del sistema (Ajustes > Aplicaciones > FitCasa > Idioma), así que ambos siguen
 * sincronizados. En versiones anteriores se guarda en SharedPreferences y se
 * aplica al arrancar desde [wrap].
 *
 * No necesita appcompat ni ninguna dependencia nueva.
 */
object AppLanguage {

    /** Idiomas que la app ofrece dentro de sí misma, además de "sistema". */
    val options = listOf(
        Option(tag = "es", label = "Español"),
        Option(tag = "en", label = "English")
    )

    /**
     * El nombre del idioma se muestra en su propio idioma ("Español", "English")
     * y por eso no pasa por recursos: así se encuentra siempre, se lea como se lea.
     */
    data class Option(val tag: String, val label: String)

    /** Idioma activo, o null si la app sigue el idioma del sistema. */
    fun current(context: Context): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales
                ?.takeIf { !it.isEmpty }
                ?.get(0)
                ?.language
        } else {
            prefs(context).getString(KEY_LANGUAGE, null)
        }

    /** Aplica el idioma. Con null se vuelve al idioma del sistema. */
    fun set(context: Context, language: String?) {
        val app = context.applicationContext
        prefs(app).edit().putString(KEY_LANGUAGE, language).apply()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            app.getSystemService(LocaleManager::class.java)?.applicationLocales =
                if (language == null) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(language)
                }
        }
    }

    /**
     * Envuelve el contexto base con el idioma guardado. En Android 13+ el
     * sistema ya lo aplica solo, así que no hay que tocar nada.
     */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val language = prefs(base).getString(KEY_LANGUAGE, null) ?: return base
        val locale = Locale(language)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
        }
        return base.createConfigurationContext(config)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private const val PREFS_NAME = "fitcasa_language"
    private const val KEY_LANGUAGE = "language"
}
