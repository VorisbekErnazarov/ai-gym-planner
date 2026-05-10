package com.fittech.aigymplanner.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import java.util.UUID

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isSmartActivity")
    @set:PropertyName("isSmartActivity")
    @get:JvmName("isSmartActivity")
    @set:JvmName("setSmartActivity")
    var isSmartActivity: Boolean = false,
    
    val activityType: String? = null,
    val calories: Int = 0,
    val durationMinutes: Int = 0
)
