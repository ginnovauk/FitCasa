package com.example.data.model

data class WorkoutPlan(
    val bodyPart: BodyPart,
    val intensity: IntensityLevel,
    val targetDurationMinutes: Int,
    val exercises: List<Exercise>,
    val estimatedCalories: Int,
    val restBetweenSeconds: Int
)
