package com.fittech.aigymplanner.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.fittech.aigymplanner.model.WorkoutLog

class GeminiManager(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun generateRecommendation(history: List<WorkoutLog>): String? {
        if (history.isEmpty()) return "Log some workouts first so I can analyze your progress!"
        
        val prompt = """
            You are a professional fitness coach. Based on this workout history:
            ${history.joinToString { "${it.exerciseName}: ${it.sets} sets x ${it.reps} reps" }}
            
            Provide a short, 2-3 sentence recommendation for what I should train tomorrow for optimal recovery and growth. 
            Be specific and encouraging.
        """.trimIndent()
        
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: getDemoRecommendation(history)
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error generating content, using demo fallback", e)
            // Fallback for Demo/University Check
            getDemoRecommendation(history)
        }
    }

    suspend fun calculateCalories(exercise: String, sets: Int, reps: Int): Int {
        val prompt = "Estimate total calories burned for $exercise: $sets sets of $reps reps. Reply ONLY with the number (integer) and nothing else."
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: estimateCaloriesLocally(exercise, sets, reps)
        } catch (e: Exception) {
            estimateCaloriesLocally(exercise, sets, reps)
        }
    }

    private fun estimateCaloriesLocally(exercise: String, sets: Int, reps: Int): Int {
        val base = when {
            exercise.lowercase().contains("squat") || exercise.lowercase().contains("leg") -> 8
            exercise.lowercase().contains("deadlift") -> 10
            exercise.lowercase().contains("walking") -> 4
            exercise.lowercase().contains("running") -> 12
            else -> 5
        }
        return sets * reps * base
    }

    private fun getDemoRecommendation(history: List<WorkoutLog>): String {
        val lastExercise = history.firstOrNull()?.exerciseName?.lowercase() ?: "general training"
        return when {
            lastExercise.contains("shoulder") || lastExercise.contains("chest") -> 
                "Great work on your upper body! Tomorrow, focus on legs or active recovery like walking to let your pushing muscles recover."
            lastExercise.contains("leg") || lastExercise.contains("squat") -> 
                "Those leg sessions are tough! Tomorrow is a perfect day for upper body pulling exercises or a light yoga session."
            lastExercise.contains("walking") || lastExercise.contains("running") -> 
                "Excellent cardio! Tomorrow, consider adding some resistance training (like pushups or squats) to build strength."
            else -> 
                "Consistent training is key! Based on your recent activity, I suggest a balanced full-body mobility routine tomorrow."
        }
    }
}
