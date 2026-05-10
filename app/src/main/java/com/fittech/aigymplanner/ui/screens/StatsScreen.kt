package com.fittech.aigymplanner.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.model.WorkoutLog
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: WorkoutViewModel) {
    val logs by viewModel.allLogs.collectAsState(initial = emptyList())
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    
    val walkRunLogs = remember(logs) {
        logs.filter { 
            val name = it.exerciseName.lowercase()
            val type = it.activityType?.lowercase() ?: ""
            name.contains("walk") || name.contains("run") || 
            type.contains("walk") || type.contains("run")
        }
    }
    
    val totalMins = walkRunLogs.sumOf { it.durationMinutes }
    val walkingLogs = walkRunLogs.filter { it.exerciseName.lowercase().contains("walk") || it.activityType?.lowercase()?.contains("walk") == true }
    val runningLogs = walkRunLogs.filter { it.exerciseName.lowercase().contains("run") || it.activityType?.lowercase()?.contains("run") == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.smartDashboard, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(strings.totalTrackedTime, color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("${totalMins / 60}${strings.hoursShort} ${totalMins % 60}${strings.minsShort}", color = colors.primary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Personal activity overview", color = colors.textSecondary, fontSize = 11.sp)
                        
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                        Spacer(Modifier.height(20.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(strings.walking.uppercase(), walkingLogs.size.toString(), Icons.AutoMirrored.Filled.DirectionsWalk)
                            StatItem(strings.running.uppercase(), runningLogs.size.toString(), Icons.AutoMirrored.Filled.DirectionsRun)
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                SectionLabel(strings.activityHistory)
            }

            if (walkRunLogs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(strings.noSmartActivity, color = colors.textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                items(walkRunLogs) { log ->
                    SmartHistoryRow(log)
                    Spacer(Modifier.height(8.dp))
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val colors = MaterialTheme.appColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = colors.textSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SmartHistoryRow(log: WorkoutLog) {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    val translatedType = when (log.activityType?.lowercase()) {
        "walking" -> strings.walking
        "running" -> strings.running
        else -> log.activityType ?: strings.walking
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(0.5.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (log.activityType?.lowercase()?.contains("run") == true || log.exerciseName.lowercase().contains("run")) 
                Icons.AutoMirrored.Filled.DirectionsRun else Icons.AutoMirrored.Filled.DirectionsWalk
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(translatedType, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(dateFormat.format(Date(log.timestamp)), color = colors.textSecondary, fontSize = 10.sp)
            }
            Text("${log.durationMinutes}${strings.minsShort}", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = MaterialTheme.appColors
    Text(
        text = text,
        color = colors.textSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
