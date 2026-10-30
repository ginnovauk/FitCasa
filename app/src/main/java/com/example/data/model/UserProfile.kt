package com.example.data.model

/** Sexo biológico usado por la fórmula de metabolismo basal. */
enum class Sex(val id: String, val displayName: String) {
    MUJER("mujer", "Mujer"),
    HOMBRE("hombre", "Hombre"),
    SIN_ESPECIFICAR("sin_especificar", "Sin especificar")
}

/**
 * Datos del usuario que ajustan el gasto calórico.
 *
 * El catálogo de ejercicios está calibrado para un cuerpo de referencia
 * (70 kg, 170 cm, 30 años, sexo sin especificar). El perfil no cambia los MET:
 * calcula la tasa metabólica basal con Mifflin-St Jeor y escala el gasto del
 * catálogo respecto a esa referencia. Con el perfil por defecto el factor es 1,
 * así que las estimaciones históricas se mantienen.
 */
data class UserProfile(
    val weightKg: Float = DEFAULT_WEIGHT_KG,
    val heightCm: Int = DEFAULT_HEIGHT_CM,
    val age: Int = DEFAULT_AGE,
    val sex: Sex = Sex.SIN_ESPECIFICAR
) {
    /** Metabolismo basal estimado (Mifflin-St Jeor), kcal/día. */
    val basalMetabolicRate: Float
        get() {
            val base = 10f * weightKg + 6.25f * heightCm - 5f * age
            val adjustment = when (sex) {
                Sex.HOMBRE -> 5f
                Sex.MUJER -> -161f
                // Sin el dato, usamos el punto medio en vez de inventar uno de los dos.
                Sex.SIN_ESPECIFICAR -> -78f
            }
            return base + adjustment
        }

    /**
     * Gasto de este cuerpo respecto al cuerpo de referencia del catálogo.
     * Sube con el peso y la estatura, y baja con la edad.
     */
    val calorieFactor: Float
        get() = basalMetabolicRate / REFERENCE_BMR

    /** Ajusta una tasa del catálogo (kcal/min a 70 kg) a este perfil. */
    fun adjustCaloriesPerMinute(catalogKcalPerMinute: Float): Float =
        catalogKcalPerMinute * calorieFactor

    /** Índice de masa corporal, solo informativo en la hoja de perfil. */
    val bmi: Float
        get() {
            val meters = heightCm / 100f
            return weightKg / (meters * meters)
        }

    /** Acota los valores a rangos razonables antes de guardarlos o usarlos. */
    fun sanitized(): UserProfile = copy(
        weightKg = weightKg.coerceIn(MIN_WEIGHT_KG, MAX_WEIGHT_KG),
        heightCm = heightCm.coerceIn(MIN_HEIGHT_CM, MAX_HEIGHT_CM),
        age = age.coerceIn(MIN_AGE, MAX_AGE)
    )

    companion object {
        const val DEFAULT_WEIGHT_KG = 70f
        const val DEFAULT_HEIGHT_CM = 170
        const val DEFAULT_AGE = 30

        const val MIN_WEIGHT_KG = 35f
        const val MAX_WEIGHT_KG = 200f
        const val MIN_HEIGHT_CM = 120
        const val MAX_HEIGHT_CM = 230
        const val MIN_AGE = 14
        const val MAX_AGE = 100

        /** BMR del perfil por defecto, tomado como referencia de calibración. */
        private val REFERENCE_BMR = UserProfile().basalMetabolicRate
    }
}
