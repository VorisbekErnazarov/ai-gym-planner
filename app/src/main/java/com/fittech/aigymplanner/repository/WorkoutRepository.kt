package com.fittech.aigymplanner.repository

import com.fittech.aigymplanner.data.ExerciseDao
import com.fittech.aigymplanner.data.WorkoutLogDao
import com.fittech.aigymplanner.data.remote.FirebaseManager
import com.fittech.aigymplanner.model.WorkoutLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class WorkoutRepository(
    private val logDao: WorkoutLogDao,
    private val exerciseDao: ExerciseDao,
    private val firebaseManager: FirebaseManager
) {
    val allLogs: Flow<List<WorkoutLog>> = logDao.getAllLogs()

    suspend fun addLog(log: WorkoutLog) {
        logDao.insertLog(log)
        updateStatsLocally()
        val localLogs = logDao.getAllLogs().first()
        firebaseManager.syncLogsToCloud(localLogs)
    }

    suspend fun updateLog(log: WorkoutLog) {
        logDao.updateLog(log)
        updateStatsLocally()
        val localLogs = logDao.getAllLogs().first()
        firebaseManager.syncLogsToCloud(localLogs)
    }

    suspend fun deleteLog(log: WorkoutLog) {
        logDao.deleteLog(log)
        updateStatsLocally()
        firebaseManager.deleteLogFromCloud(log.id)
    }

    suspend fun updateStatsLocally() {
        val allLogs = logDao.getAllLogs().first()
        val savedExercises = exerciseDao.getAllSavedExercises().first()
        
        val totalExercises = allLogs.size
        val totalCalories = allLogs.sumOf { it.calories }
        
        // Count unique days in current week for daysPerWeek
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)
        
        val daysThisWeek = allLogs.filter { log ->
            val logCal = Calendar.getInstance()
            logCal.timeInMillis = log.timestamp
            logCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && logCal.get(Calendar.YEAR) == currentYear
        }.map { log ->
            val logCal = Calendar.getInstance()
            logCal.timeInMillis = log.timestamp
            logCal.get(Calendar.DAY_OF_YEAR)
        }.distinct().size

        val newStats = com.fittech.aigymplanner.model.UserStats(
            daysPerWeek = daysThisWeek,
            completedExercises = totalExercises,
            caloriesBurned = totalCalories,
            savedExercisesCount = savedExercises.size, 
            currentStreak = 5 // Simplified streak logic
        )
        firebaseManager.saveUserStats(newStats)
    }

    suspend fun syncWithCloud() {
        // Upload first
        val localLogs = logDao.getAllLogs().first()
        firebaseManager.syncLogsToCloud(localLogs)
        
        // Then download
        val cloudLogs = firebaseManager.fetchLogsFromCloud()
        cloudLogs.forEach { log ->
            logDao.insertLog(log)
        }
        updateStatsLocally()
    }
}
