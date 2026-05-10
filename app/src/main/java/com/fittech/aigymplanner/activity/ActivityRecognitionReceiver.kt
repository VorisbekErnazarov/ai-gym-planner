package com.fittech.aigymplanner.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.fittech.aigymplanner.MainActivity
import com.fittech.aigymplanner.data.AppDatabase
import com.fittech.aigymplanner.model.WorkoutLog
import com.fittech.aigymplanner.ui.theme.getAppStrings
import com.fittech.aigymplanner.viewmodel.ActivityRecognitionViewModel
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent) ?: return
            val activities = result.probableActivities
            
            var detectedType: String? = null

            // Prioritize RUNNING > WALKING > ON_FOOT
            val running = activities.find { it.type == DetectedActivity.RUNNING }
            val walking = activities.find { it.type == DetectedActivity.WALKING }
            val onFoot = activities.find { it.type == DetectedActivity.ON_FOOT }

            detectedType = when {
                running != null && running.confidence > 10 -> "Running" 
                walking != null && walking.confidence > 15 -> "Walking"
                onFoot != null && onFoot.confidence > 20 -> "Walking"
                else -> null
            }

            if (detectedType != null) {
                Log.d("ActivityRecognition", "Detected: $detectedType (R:${running?.confidence} W:${walking?.confidence})")
                ActivityRecognitionViewModel.updateStatus("$detectedType detected")
                saveOrUpdateActivity(context, detectedType)
            }
        }
    }

    private fun saveOrUpdateActivity(context: Context, type: String) {
        val prefs = context.getSharedPreferences("activity_prefs", Context.MODE_PRIVATE)
        val lastType = prefs.getString("last_type", null)
        val lastId = prefs.getString("last_session_id", null)
        val currentTime = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            
            // If it's the same activity within 10 minutes, update the existing session
            if (type == lastType && lastId != null) {
                val existingLog = db.workoutLogDao().getLogById(lastId)
                if (existingLog != null && (currentTime - existingLog.timestamp) < 600000) {
                    val durationMins = ((currentTime - existingLog.timestamp) / 60000).toInt().coerceAtLeast(1)
                    val caloriesPerMin = if (type == "Running") 15 else 6
                    
                    Log.d("ActivityRecognition", "Updating $type duration: $durationMins mins")
                    
                    db.workoutLogDao().updateLog(
                        existingLog.copy(
                            durationMinutes = durationMins,
                            calories = durationMins * caloriesPerMin
                        )
                    )
                    return@launch
                }
            }

            // Create new session if type changed or too much time passed
            createNewSession(context, db, type, currentTime, prefs)
        }
    }

    private suspend fun createNewSession(context: Context, db: AppDatabase, type: String, time: Long, prefs: android.content.SharedPreferences) {
        val id = java.util.UUID.randomUUID().toString()
        val calories = if (type == "Running") 15 else 6
        
        Log.d("ActivityRecognition", "Starting new $type session: $id")
        
        db.workoutLogDao().insertLog(
            WorkoutLog(
                id = id,
                exerciseName = type,
                isSmartActivity = true,
                activityType = type,
                timestamp = time,
                calories = calories, // Start with 1 min worth
                durationMinutes = 1
            )
        )
        
        prefs.edit().apply {
            putString("last_type", type)
            putString("last_session_id", id)
            apply()
        }
        
        val lang = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("lang", "en") ?: "en"
        val strings = getAppStrings(lang)
        val translatedType = if (type == "Running") strings.running else strings.walking

        MainScope().launch {
            Toast.makeText(context, "${strings.smartActivity}: $translatedType", Toast.LENGTH_SHORT).show()
        }
        showNotification(context, translatedType, strings)
    }

    private fun showNotification(context: Context, typeLabel: String, strings: com.fittech.aigymplanner.ui.theme.AppStrings) {
        val channelId = "activity_recognition_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Activity Detection", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(strings.smartDashboard)
            .setContentText("${strings.duration}: $typeLabel")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
    }
}
