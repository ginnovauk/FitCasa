package com.example

import com.example.data.model.Sex
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    @Test
    fun `el perfil por defecto no cambia el gasto`() {
        assertEquals(1f, UserProfile().calorieFactor, 0.0001f)
    }

    @Test
    fun `mas peso sube el gasto`() {
        val ligero = UserProfile(weightKg = 60f)
        val pesado = UserProfile(weightKg = 90f)

        assertTrue(
            "El perfil pesado (${pesado.calorieFactor}) no gasta más que el ligero (${ligero.calorieFactor})",
            pesado.calorieFactor > ligero.calorieFactor
        )
    }

    @Test
    fun `mas estatura sube el gasto`() {
        val bajo = UserProfile(heightCm = 160)
        val alto = UserProfile(heightCm = 190)

        assertTrue(
            "La estatura no influye: ${alto.calorieFactor} vs ${bajo.calorieFactor}",
            alto.calorieFactor > bajo.calorieFactor
        )
    }

    @Test
    fun `mas edad baja el gasto`() {
        val joven = UserProfile(age = 20)
        val mayor = UserProfile(age = 60)

        assertTrue(
            "La edad no influye: ${mayor.calorieFactor} vs ${joven.calorieFactor}",
            mayor.calorieFactor < joven.calorieFactor
        )
    }

    @Test
    fun `hombre y mujer difieren con el mismo cuerpo`() {
        val hombre = UserProfile(sex = Sex.HOMBRE)
        val mujer = UserProfile(sex = Sex.MUJER)
        val neutro = UserProfile(sex = Sex.SIN_ESPECIFICAR)

        assertTrue(hombre.calorieFactor > neutro.calorieFactor)
        assertTrue(neutro.calorieFactor > mujer.calorieFactor)
    }

    @Test
    fun `el imc usa peso y estatura`() {
        val profile = UserProfile(weightKg = 70f, heightCm = 175)

        assertEquals(22.86f, profile.bmi, 0.01f)
    }

    @Test
    fun `sanitized acota los valores fuera de rango`() {
        val clean = UserProfile(weightKg = 10f, heightCm = 300, age = 5).sanitized()

        assertEquals(UserProfile.MIN_WEIGHT_KG, clean.weightKg, 0.001f)
        assertEquals(UserProfile.MAX_HEIGHT_CM, clean.heightCm)
        assertEquals(UserProfile.MIN_AGE, clean.age)
    }
}
