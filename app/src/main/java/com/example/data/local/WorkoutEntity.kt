package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_records")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val workoutTitle: String,
    val bodyPartId: String,
    val intensityId: String,
    val durationSeconds: Int,
    val caloriesBurned: Int,
    val exercisesCompleted: Int,
    val totalExercises: Int
)
