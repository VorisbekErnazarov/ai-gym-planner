package com.fittech.aigymplanner.data

import android.content.Context
import androidx.room.*
import com.fittech.aigymplanner.model.Exercise
import com.fittech.aigymplanner.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

// ─── DAO (Data Access Object) ─────────────────────────────────────────────────
// Defines all SQL operations for the saved_exercises table.

@Dao
interface ExerciseDao {

    /** Stream all saved exercises — updates automatically when data changes. */
    @Query("SELECT * FROM saved_exercises")
    fun getAllSavedExercises(): Flow<List<Exercise>>

    /** Insert or replace an exercise (used when user saves one). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveExercise(exercise: Exercise)

    /** Remove a saved exercise by name. */
    @Query("DELETE FROM saved_exercises WHERE name = :name")
    suspend fun deleteExercise(name: String)

    /** Check if a specific exercise is already saved. */
    @Query("SELECT COUNT(*) FROM saved_exercises WHERE name = :name")
    suspend fun isSaved(name: String): Int
}

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLog)

    @Query("SELECT * FROM workout_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: String): WorkoutLog?

    @Delete
    suspend fun deleteLog(log: WorkoutLog)

    @Update
    suspend fun updateLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs")
    suspend fun clearLogs()
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [Exercise::class, WorkoutLog::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         * Creates it if it doesn't already exist (thread-safe).
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_planner_database"
                )
                .fallbackToDestructiveMigration() // Allow data loss on schema change for this fix
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
