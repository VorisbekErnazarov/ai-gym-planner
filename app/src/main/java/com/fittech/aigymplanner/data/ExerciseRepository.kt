package com.fittech.aigymplanner.data

import com.fittech.aigymplanner.model.Exercise
import com.fittech.aigymplanner.model.FitnessLevel
import com.fittech.aigymplanner.model.WorkoutDay
import kotlinx.coroutines.flow.Flow
class ExerciseRepository(
    private val api: ExerciseApiService,
    private val dao: ExerciseDao
) {
    suspend fun fetchExercises(
        muscle: String? = null,
        type: String? = null,
        difficulty: String? = null
    ): Result<List<Exercise>> {
        return try {
            val response = api.getExercises(muscle, type, difficulty)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.success(getMockExercises())
            }
        } catch (e: Exception) {
            Result.success(getMockExercises())
        }
    }

    // ─── Local Database (Room) ────────────────────────────────────────────────

    fun getSavedExercises(): Flow<List<Exercise>> = dao.getAllSavedExercises()

    suspend fun saveExercise(exercise: Exercise) = dao.saveExercise(exercise.copy(isSaved = true))

    suspend fun deleteExercise(name: String) = dao.deleteExercise(name)

    // ─── Workout Plan Logic ───────────────────────────────────────────────────

    /**
     * Generates a weekly workout plan based on the user's fitness level.
     *   Beginner     -> 3 days/week  (Full body focus)
     *   Intermediate -> 4 days/week  (Upper / Lower split)
     *   Advanced     -> 5 days/week  (Push / Pull / Legs)
     */
    fun generateWorkoutPlan(level: FitnessLevel, allExercises: List<Exercise>): List<WorkoutDay> {
        return when (level) {
            FitnessLevel.BEGINNER     -> buildBeginnerPlan(allExercises)
            FitnessLevel.INTERMEDIATE -> buildIntermediatePlan(allExercises)
            FitnessLevel.ADVANCED     -> buildAdvancedPlan(allExercises)
        }
    }

    // 3-day full-body plan
    private fun buildBeginnerPlan(exercises: List<Exercise>): List<WorkoutDay> {
        val days = listOf("Monday", "Wednesday", "Friday")
        val splits = listOf(
            listOf("chest", "triceps"),
            listOf("back", "biceps"),
            listOf("legs", "shoulders")
        )
        return splits.mapIndexed { i, muscles ->
            WorkoutDay(
                dayNumber = i + 1,
                dayName = "Day ${i + 1} - ${days[i]}",
                muscleGroups = muscles,
                exercises = exercises.filter { it.muscle in muscles }.take(4)
                    .ifEmpty { getMockExercisesForMuscles(muscles) }
            )
        }
    }

    // 4-day upper/lower split
    private fun buildIntermediatePlan(exercises: List<Exercise>): List<WorkoutDay> {
        val config = listOf(
            Triple("Day 1 - Monday",    listOf("chest", "shoulders", "triceps"), 5),
            Triple("Day 2 - Tuesday",   listOf("back", "biceps"),                5),
            Triple("Day 3 - Thursday",  listOf("legs", "glutes"),                5),
            Triple("Day 4 - Friday",    listOf("chest", "back", "shoulders"),    5)
        )
        return config.mapIndexed { i, (name, muscles, take) ->
            WorkoutDay(
                dayNumber = i + 1,
                dayName = name,
                muscleGroups = muscles,
                exercises = exercises.filter { it.muscle in muscles }.take(take)
                    .ifEmpty { getMockExercisesForMuscles(muscles) }
            )
        }
    }

    // 5-day push/pull/legs split
    private fun buildAdvancedPlan(exercises: List<Exercise>): List<WorkoutDay> {
        val config = listOf(
            Triple("Day 1 - Monday",    listOf("chest", "shoulders", "triceps"), 6),
            Triple("Day 2 - Tuesday",   listOf("back", "biceps", "lats"),        6),
            Triple("Day 3 - Wednesday", listOf("legs", "glutes", "hamstrings"),  6),
            Triple("Day 4 - Friday",    listOf("chest", "triceps"),              6),
            Triple("Day 5 - Saturday",  listOf("back", "shoulders", "biceps"),  6)
        )
        return config.mapIndexed { i, (name, muscles, take) ->
            WorkoutDay(
                dayNumber = i + 1,
                dayName = name,
                muscleGroups = muscles,
                exercises = exercises.filter { it.muscle in muscles }.take(take)
                    .ifEmpty { getMockExercisesForMuscles(muscles) }
            )
        }
    }

    // ─── Mock / Fallback Data ─────────────────────────────────────────────────
    // Used when the API key is not set or there is no internet connection.

    fun getMockExercises(): List<Exercise> = listOf(
        Exercise("Barbell Bench Press",    "strength", "chest",      "barbell",  "beginner",     "Lie on bench, lower bar to chest, press up."),
        Exercise("Push-Up",                "strength", "chest",      "body_only","beginner",     "Keep body straight, lower chest to floor, push up."),
        Exercise("Incline Dumbbell Press", "strength", "chest",      "dumbbell", "intermediate", "On incline bench, press dumbbells up and together."),
        Exercise("Pull-Up",                "strength", "back",       "body_only","intermediate", "Hang from bar, pull chin above bar, lower slowly."),
        Exercise("Barbell Row",            "strength", "back",       "barbell",  "intermediate", "Hinge forward, row bar to lower chest."),
        Exercise("Lat Pulldown",           "strength", "lats",       "cable",    "beginner",     "Sit at cable machine, pull bar to upper chest."),
        Exercise("Barbell Squat",          "strength", "legs",       "barbell",  "beginner",     "Bar on traps, squat to parallel, drive up."),
        Exercise("Romanian Deadlift",      "strength", "hamstrings", "barbell",  "intermediate", "Hinge at hips, lower bar down legs, return."),
        Exercise("Leg Press",              "strength", "legs",       "machine",  "beginner",     "Push platform away with both feet."),
        Exercise("Dumbbell Curl",          "strength", "biceps",     "dumbbell", "beginner",     "Curl dumbbells to shoulders, squeeze biceps."),
        Exercise("Tricep Dip",             "strength", "triceps",    "body_only","beginner",     "Lower body between bars until arms at 90 degrees, push up."),
        Exercise("Overhead Press",         "strength", "shoulders",  "barbell",  "intermediate", "Press bar overhead from shoulder height."),
        Exercise("Lateral Raise",          "strength", "shoulders",  "dumbbell", "beginner",     "Raise dumbbells to sides until shoulder height."),
        Exercise("Glute Bridge",           "strength", "glutes",     "body_only","beginner",     "Drive hips up, squeeze glutes at top."),
        Exercise("Plank",                  "strength", "abdominals", "body_only","beginner",     "Hold push-up position with forearms on floor."),
        Exercise("Cable Row",              "strength", "back",       "cable",    "beginner",     "Sit at cable row, pull handle to abdomen."),
        Exercise("Face Pull",              "strength", "shoulders",  "cable",    "beginner",     "Pull rope attachment to face level, elbows high."),
        Exercise("Leg Curl",               "strength", "hamstrings", "machine",  "beginner",     "Curl heels toward glutes on the machine."),
        Exercise("Calf Raise",             "strength", "calves",     "body_only","beginner",     "Rise up on toes, hold, lower slowly."),
        Exercise("Hammer Curl",            "strength", "biceps",     "dumbbell", "beginner",     "Curl with neutral grip (thumbs up).")
    )

    private fun getMockExercisesForMuscles(muscles: List<String>): List<Exercise> =
        getMockExercises().filter { it.muscle in muscles }
}
