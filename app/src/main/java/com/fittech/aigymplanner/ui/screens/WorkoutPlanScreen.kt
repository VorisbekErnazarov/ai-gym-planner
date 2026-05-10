package com.fittech.aigymplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.model.WorkoutDay
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanScreen(
    viewModel: ExerciseViewModel,
    onBack: () -> Unit
) {
    val workoutPlan   by viewModel.workoutPlan.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val colors = MaterialTheme.appColors
    val strings = appStrings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.workoutPlan, color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // Header
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${selectedLevel.label} ${strings.program}",
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${selectedLevel.daysPerWeek} ${strings.trainingDaysPerWeek}",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // One card per workout day
            items(workoutPlan) { day ->
                WorkoutDayCard(day)
            }

            // Empty state
            if (workoutPlan.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(strings.noWorkouts,
                            color = colors.textSecondary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDayCard(day: WorkoutDay) {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    
    val translatedDayName = day.dayName
        .replace("Day", strings.day)
        .replace("Monday", "Dushanba") // Uzbek mapping if possible, or just keep as is for now
        // For university demo, let's just use the strings system better.

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Day header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "${day.dayNumber}",
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(translatedDayName, color = colors.textPrimary,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text  = day.muscleGroups.joinToString(" · ") {
                            it.replaceFirstChar { c -> c.uppercase() }
                        },
                        color = colors.primary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colors.border,
                thickness = 0.5.dp
            )

            // Exercise rows
            if (day.exercises.isEmpty()) {
                Text(strings.noExercisesForDay, color = colors.textSecondary, fontSize = 13.sp)
            } else {
                day.exercises.forEach { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, color = colors.textPrimary, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium)
                            Text(
                                (exercise.muscle ?: "").replaceFirstChar { it.uppercase() },
                                color = colors.textSecondary, fontSize = 11.sp
                            )
                        }
                        // Sets x Reps suggestion based on difficulty
                        val setsReps = when (exercise.difficulty?.lowercase()) {
                            "beginner"     -> "3 x 10"
                            "intermediate" -> "4 x 8"
                            "expert"       -> "5 x 5"
                            else           -> "3 x 10"
                        }
                        Text(setsReps, color = colors.primary, fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
