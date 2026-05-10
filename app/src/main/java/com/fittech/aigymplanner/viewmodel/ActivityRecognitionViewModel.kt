package com.fittech.aigymplanner.viewmodel

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.fittech.aigymplanner.activity.ActivityRecognitionReceiver
import com.google.android.gms.location.ActivityRecognition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActivityRecognitionViewModel : ViewModel() {
    companion object {
        private val _currentStatus = MutableStateFlow("Stationary")
        val currentStatus = _currentStatus.asStateFlow()

        fun updateStatus(status: String) {
            _currentStatus.value = status
        }
    }

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startTracking(context: Context) {
        val intent = Intent(context, ActivityRecognitionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        ActivityRecognition.getClient(context)
            .requestActivityUpdates(3000, pendingIntent)
            .addOnSuccessListener {
                _isTracking.value = true
                Toast.makeText(context, "Activity Tracking Started", Toast.LENGTH_SHORT).show()
                Log.d("ActivityRecognition", "Successfully started tracking")
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("ActivityRecognition", "Failed to start tracking", it)
            }
    }

    fun stopTracking(context: Context) {
        val intent = Intent(context, ActivityRecognitionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        ActivityRecognition.getClient(context)
            .removeActivityUpdates(pendingIntent)
            .addOnSuccessListener {
                _isTracking.value = false
            }
    }
}
