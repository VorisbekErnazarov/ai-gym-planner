package com.fittech.aigymplanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fittech.aigymplanner.model.FitnessLevel
import com.fittech.aigymplanner.ui.components.formatWithDigits
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.AuthViewModel
import com.fittech.aigymplanner.viewmodel.ExerciseViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExerciseViewModel,
    authViewModel: AuthViewModel,
    onNavigateToExercises: () -> Unit,
    onNavigateToWorkoutPlan: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val userStats by authViewModel.userStats.collectAsState()
    val isSyncing by authViewModel.isSyncing.collectAsState()
    
    val colors = MaterialTheme.appColors
    val strings = appStrings()

    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
    val context = LocalContext.current
    
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchUserStats()
        authViewModel.startStatsObservation()
    }

    if (userStats.completedExercises == 0 && isSyncing) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About AI Gym Planner", color = colors.primary) },
            text = { Text("Your intelligent companion for gym routines and activity tracking. Powered by Gemini AI. Done by Voris", color = colors.textPrimary) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it", color = colors.primary)
                }
            },
            containerColor = colors.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = if (colors.primary == Color.White) Color.Black else colors.background,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            strings.appName,
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.cardBg),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    AppBarIconButton(Icons.Default.Info) {
                        showInfoDialog = true
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Hero card - Slim horizontal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(colors.surface, colors.heroEnd)))
                        .padding(16.dp)
                ) {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val greeting = when (hour) {
                        in 0..11 -> strings.goodMorning
                        in 12..16 -> strings.goodAfternoon
                        else -> strings.goodEvening
                    }
                    Column {
                        Text("$greeting, $userName", color = colors.textSecondary, fontSize = 12.sp)
                        Text(strings.readyToTrain, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(colors.primary))
                            Spacer(Modifier.width(6.dp))
                            Text("${strings.streak}: ${userStats.currentStreak} days", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Select Level
            SectionLabel(strings.selectLevel)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FitnessLevel.entries.forEach { level ->
                    val label = when(level) {
                        FitnessLevel.BEGINNER -> strings.beginner
                        FitnessLevel.INTERMEDIATE -> strings.intermediate
                        FitnessLevel.ADVANCED -> strings.advanced
                    }
                    LevelPill(
                        label = label,
                        isSelected = level == selectedLevel,
                        onClick = { viewModel.setFitnessLevel(level) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Your Stats
            SectionLabel(strings.yourStats)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeStatCard(strings.daysWeek, "${userStats.daysPerWeek}/7", Modifier.weight(1f), isSyncing)
                HomeStatCard(strings.exercises, "${userStats.completedExercises}", Modifier.weight(1f), isSyncing)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeStatCard(strings.saved, "${userStats.savedExercisesCount}", Modifier.weight(1f).clickable { onNavigateToSaved() }, isSyncing)
                val calorieDisplay = if (userStats.caloriesBurned >= 1000) "${(userStats.caloriesBurned / 1000f).formatWithDigits(1)}k" else "${userStats.caloriesBurned}"
                HomeStatCard(strings.calories, calorieDisplay, Modifier.weight(1f), isSyncing)
            }

            Spacer(Modifier.height(16.dp))

            // Explore
            SectionLabel(strings.explore)
            Column {
                HomeNavCard(Icons.Default.Menu, strings.exerciseLibrary, "${exercises.size} exercises", onNavigateToExercises)
                HomeNavCard(Icons.Default.CalendarToday, strings.workoutPlan, "3-day beginner", onNavigateToWorkoutPlan)
                HomeNavCard(Icons.Default.History, strings.workoutHistory, "Weekly calendar", onNavigateToHistory)
                HomeNavCard(Icons.Default.Shield, strings.smartDashboard, "Live activity + AI", onNavigateToStats)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AppBarIconButton(icon: ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.cardBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = MaterialTheme.appColors
    Text(
        text = text,
        color = colors.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun LevelPill(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(17.dp),
        color = if (isSelected) colors.primary else colors.cardBg,
        border = if (isSelected) null else BorderStroke(1.dp, colors.border)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) (if (colors.primary == Color.White) Color.Black else Color.Black) else colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HomeStatCard(label: String, value: String, modifier: Modifier = Modifier, isSyncing: Boolean = false) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBg)
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Normal)
                if (isSyncing) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Sync, null, tint = colors.primary.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(value, color = colors.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeNavCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = colors.textSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
