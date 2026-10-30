package com.example.data.local

import android.content.Context
import com.example.data.model.Sex
import com.example.data.model.UserProfile

/**
 * Guarda el perfil en SharedPreferences, igual que hace el entrenador por voz.
 * Al ser un dato pequeño y local no hace falta una tabla de Room.
 */
class UserProfileStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): UserProfile {
        val defaults = UserProfile()
        val storedSex = prefs.getString(KEY_SEX, defaults.sex.id)
        return UserProfile(
            weightKg = prefs.getFloat(KEY_WEIGHT_KG, defaults.weightKg),
            heightCm = prefs.getInt(KEY_HEIGHT_CM, defaults.heightCm),
            age = prefs.getInt(KEY_AGE, defaults.age),
            sex = Sex.values().firstOrNull { it.id == storedSex } ?: defaults.sex
        ).sanitized()
    }

    fun save(profile: UserProfile) {
        val clean = profile.sanitized()
        prefs.edit()
            .putFloat(KEY_WEIGHT_KG, clean.weightKg)
            .putInt(KEY_HEIGHT_CM, clean.heightCm)
            .putInt(KEY_AGE, clean.age)
            .putString(KEY_SEX, clean.sex.id)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "fitcasa_profile"
        const val KEY_WEIGHT_KG = "weight_kg"
        const val KEY_HEIGHT_CM = "height_cm"
        const val KEY_AGE = "age"
        const val KEY_SEX = "sex"
    }
}
