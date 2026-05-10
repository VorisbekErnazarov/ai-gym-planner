package com.fittech.aigymplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fittech.aigymplanner.data.AppDatabase
import com.fittech.aigymplanner.data.remote.FirebaseManager
import com.fittech.aigymplanner.data.remote.GeminiManager
import com.fittech.aigymplanner.model.WorkoutLog
import com.fittech.aigymplanner.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = WorkoutRepository(
        logDao = db.workoutLogDao(),
        exerciseDao = db.exerciseDao(),
        firebaseManager = FirebaseManager()
    )
    private val geminiManager = GeminiManager("AIzaSyAEGcFKaBFMUG4fT3Z_uJpRGS2-HDTRkq4")

    val allLogs = repository.allLogs

    private val _aiRecommendation = MutableStateFlow<String?>(null)
    val aiRecommendation = _aiRecommendation.asStateFlow()

    fun logWorkout(
        exerciseName: String, 
        sets: Int, 
        reps: Int, 
        isSmart: Boolean = false,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val calories = if (isSmart) {
                if (exerciseName.lowercase().contains("running")) 300 else 100
            } else {
                geminiManager.calculateCalories(exerciseName, sets, reps)
            }
            
            repository.addLog(
                WorkoutLog(
                    exerciseName = exerciseName,
                    sets = sets,
                    reps = reps,
                    timestamp = timestamp,
                    isSmartActivity = isSmart,
                    activityType = if (isSmart) exerciseName else null,
                    calories = calories
                )
            )
        }
    }

    fun updateWorkout(log: WorkoutLog) {
        viewModelScope.launch {
            repository.updateLog(log)
        }
    }

    fun deleteWorkout(log: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun generateAIRecommendation(history: List<WorkoutLog>) {
        viewModelScope.launch {
            _aiRecommendation.value = geminiManager.generateRecommendation(history)
        }
    }

    fun syncData() {
        viewModelScope.launch {
            repository.syncWithCloud()
        }
    }
}
