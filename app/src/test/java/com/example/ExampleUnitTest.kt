package com.example

import com.example.data.model.BodyPart
import com.example.data.model.IntensityLevel
import com.example.data.repository.ExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun exerciseCatalog_containsAllBodyParts() {
        assertFalse(ExerciseCatalog.allExercises.isEmpty())
        
        val bodyPartsInCatalog = ExerciseCatalog.allExercises.map { it.bodyPart }.toSet()
        assertTrue(bodyPartsInCatalog.contains(BodyPart.CUERPO_COMPLETO))
        assertTrue(bodyPartsInCatalog.contains(BodyPart.PECHO))
        assertTrue(bodyPartsInCatalog.contains(BodyPart.ABDOMEN))
        assertTrue(bodyPartsInCatalog.contains(BodyPart.PIERNAS))
        assertTrue(bodyPartsInCatalog.contains(BodyPart.BRAZOS))
        assertTrue(bodyPartsInCatalog.contains(BodyPart.ESPALDA))
    }

    @Test
    fun exerciseCatalog_filterByBodyPart_returnsMatchingExercises() {
        val chestExercises = ExerciseCatalog.getExercisesByBodyPart(BodyPart.PECHO)
        assertTrue(chestExercises.isNotEmpty())
        assertTrue(chestExercises.any { it.bodyPart == BodyPart.PECHO })
    }
}
