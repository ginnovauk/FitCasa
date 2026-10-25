package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_records ORDER BY dateMillis DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_records WHERE id = :id LIMIT 1")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workout_records WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("DELETE FROM workout_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM workout_records")
    fun getWorkoutCount(): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM workout_records")
    fun getTotalDurationSeconds(): Flow<Int?>

    @Query("SELECT SUM(caloriesBurned) FROM workout_records")
    fun getTotalCaloriesBurned(): Flow<Int?>
}
