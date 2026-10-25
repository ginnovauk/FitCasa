package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.WorkoutDatabase
import com.example.data.local.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: WorkoutDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FitCasa", appName)
    }

    @Test
    fun insertAndReadWorkoutRecord() = runBlocking {
        val record = WorkoutEntity(
            dateMillis = System.currentTimeMillis(),
            workoutTitle = "Rutina Pecho Exprés",
            bodyPartId = "pecho",
            intensityId = "principiante",
            durationSeconds = 600,
            caloriesBurned = 75,
            exercisesCompleted = 6,
            totalExercises = 6
        )
        val id = db.workoutDao().insertWorkout(record)
        assertEquals(1L, id)

        val list = db.workoutDao().getAllWorkouts().first()
        assertEquals(1, list.size)
        assertEquals("Rutina Pecho Exprés", list[0].workoutTitle)
        assertEquals(75, list[0].caloriesBurned)
    }
}
