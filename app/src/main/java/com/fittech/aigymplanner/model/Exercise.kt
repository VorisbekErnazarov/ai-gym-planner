package com.fittech.aigymplanner.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Exercise data model.
 * Used both as a Retrofit response model (from API)
 * and as a Room entity (for local storage).
 */
@Entity(tableName = "saved_exercises")
data class Exercise(
    @PrimaryKey
    val name: String,
    val type: String? = "",          // e.g. "strength", "cardio"
    val muscle: String? = "",        // e.g. "chest", "biceps"
    val equipment: String? = "",     // e.g. "barbell", "dumbbell"
    val difficulty: String? = "",    // e.g. "beginner", "intermediate", "expert"
    val instructions: String? = "",
    val isSaved: Boolean = false,    // local flag for Room (not from API)

    @SerializedName("equipments")
    @Ignore
    val equipments: List<String>? = null // Some API responses return this
) {
    // Required by Room if there are multiple constructors or ignored fields in primary
    constructor(
        name: String,
        type: String?,
        muscle: String?,
        equipment: String?,
        difficulty: String?,
        instructions: String?,
        isSaved: Boolean
    ) : this(name, type, muscle, equipment, difficulty, instructions, isSaved, null)

    /**
     * Safely returns the equipment string, prioritizing 'equipment' then 'equipments' list.
     */
    fun getDisplayEquipment(): String {
        val singular = equipment
        if (!singular.isNullOrBlank()) return singular
        return equipments?.joinToString(", ") ?: ""
    }
}

/**
 * User fitness level — drives the workout plan logic.
 */
enum class FitnessLevel(val label: String, val daysPerWeek: Int) {
    BEGINNER("Beginner", 3),
    INTERMEDIATE("Intermediate", 4),
    ADVANCED("Advanced", 5)
}

/**
 * A single day in a weekly workout plan.
 */
data class WorkoutDay(
    val dayNumber: Int,
    val dayName: String,         // e.g. "Day 1 – Monday"
    val muscleGroups: List<String>,
    val exercises: List<Exercise>
)
