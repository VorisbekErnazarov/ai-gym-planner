package com.fittech.aigymplanner.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.model.WorkoutLog
import com.fittech.aigymplanner.ui.components.formatWithDigits
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: WorkoutViewModel, onBack: () -> Unit) {
    val logs by viewModel.allLogs.collectAsState(initial = emptyList())
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    
    var viewWeekTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    val selectedDate = remember(selectedTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedTimeMillis }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<WorkoutLog?>(null) }
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayNameFormat = SimpleDateFormat("E", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    val dailyLogsForSummary = remember(logs, selectedTimeMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimeMillis }
        val day = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        logs.filter {
            val logCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            logCal.get(Calendar.DAY_OF_YEAR) == day && logCal.get(Calendar.YEAR) == year
        }
    }
    
    val totalWorkouts = dailyLogsForSummary.size
    val totalCalories = dailyLogsForSummary.sumOf { it.calories }
    val totalTimeMins = dailyLogsForSummary.sumOf { (it.sets * 5) + it.durationMinutes }
    val timeDisplay = "${totalTimeMins / 60}${strings.hoursShort} ${totalTimeMins % 60}${strings.minsShort}"

    LaunchedEffect(logs) {
        if (logs.isNotEmpty() && aiRecommendation == null) {
            viewModel.generateAIRecommendation(logs)
        }
    }

    if (showAddDialog || editingLog != null) {
        AddWorkoutDialog(
            existingLog = editingLog,
            onDismiss = { 
                showAddDialog = false
                editingLog = null
            },
            onSave = { name, sets, reps ->
                if (editingLog != null) {
                    viewModel.updateWorkout(editingLog!!.copy(exerciseName = name, sets = sets, reps = reps))
                } else {
                    viewModel.logWorkout(
                        exerciseName = name, 
                        sets = sets, 
                        reps = reps,
                        timestamp = selectedTimeMillis
                    )
                }
                showAddDialog = false
                editingLog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.workoutHistory, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.generateAIRecommendation(logs) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Tip", tint = colors.primary)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = colors.primary)
                    }
                },
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

                // AI Coach Card at the top of Log
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(colors.surface, colors.background.copy(alpha = 0.3f))))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("AI PERSONAL COACH", color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        if (aiRecommendation == null) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(2.dp),
                                color = colors.primary,
                                trackColor = colors.border
                            )
                        } else {
                            Text(
                                text = aiRecommendation!!,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = monthFormat.format(Date(viewWeekTimeMillis)).uppercase(),
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                Icon(
                                    Icons.Default.ChevronLeft, 
                                    null, 
                                    tint = colors.textSecondary, 
                                    modifier = Modifier.size(20.dp).clickable {
                                        val cal = Calendar.getInstance().apply { 
                                            timeInMillis = viewWeekTimeMillis
                                            add(Calendar.WEEK_OF_YEAR, -1)
                                        }
                                        viewWeekTimeMillis = cal.timeInMillis
                                    }
                                )
                                Spacer(Modifier.width(16.dp))
                                Icon(
                                    Icons.Default.ChevronRight, 
                                    null, 
                                    tint = colors.textSecondary, 
                                    modifier = Modifier.size(20.dp).clickable {
                                        val cal = Calendar.getInstance().apply { 
                                            timeInMillis = viewWeekTimeMillis
                                            add(Calendar.WEEK_OF_YEAR, 1)
                                        }
                                        viewWeekTimeMillis = cal.timeInMillis
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val cal = Calendar.getInstance().apply { 
                                timeInMillis = viewWeekTimeMillis
                                firstDayOfWeek = Calendar.MONDAY
                                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            }
                            repeat(7) {
                                val cellTime = cal.timeInMillis
                                val isSelected = Calendar.getInstance().apply { timeInMillis = cellTime }.get(Calendar.DAY_OF_YEAR) == 
                                               selectedDate.get(Calendar.DAY_OF_YEAR) && 
                                               Calendar.getInstance().apply { timeInMillis = cellTime }.get(Calendar.YEAR) == 
                                               selectedDate.get(Calendar.YEAR)
                                               
                                val hasLogs = logs.any { 
                                    val logCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                                    logCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().apply { timeInMillis = cellTime }.get(Calendar.DAY_OF_YEAR) && 
                                    logCal.get(Calendar.YEAR) == Calendar.getInstance().apply { timeInMillis = cellTime }.get(Calendar.YEAR)
                                }
                                
                                DayCell(
                                    dayLetter = dayNameFormat.format(cal.time).first().toString(),
                                    dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString(),
                                    isSelected = isSelected,
                                    hasLogs = hasLogs,
                                    onClick = { selectedTimeMillis = cellTime }
                                )
                                cal.add(Calendar.DAY_OF_MONTH, 1)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                        Spacer(Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryItem(strings.log.uppercase(), totalWorkouts.toString(), Modifier.weight(1f))
                            SummaryItem(strings.calories.uppercase(), if (totalCalories >= 1000) "${(totalCalories / 1000f).formatWithDigits(1)}k" else totalCalories.toString(), Modifier.weight(1f))
                            SummaryItem(strings.duration.uppercase(), timeDisplay, Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = fullDateFormat.format(selectedDate.time).uppercase(),
                    color = colors.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column {
                        val displayLogs = dailyLogsForSummary.filter { !it.isSmartActivity }
                            .sortedByDescending { it.timestamp }

                        if (displayLogs.isEmpty()) {
                            Text(
                                strings.noWorkouts,
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        } else {
                            displayLogs.forEachIndexed { index, log ->
                                HistoryRow(
                                    log = log,
                                    onEdit = { editingLog = log },
                                    onDelete = { viewModel.deleteWorkout(log) }
                                )
                                if (index < displayLogs.lastIndex) {
                                    HorizontalDivider(color = colors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                SectionLabel(strings.smartActivity)
                
                val smartLogs = dailyLogsForSummary.filter { it.isSmartActivity }.sortedByDescending { it.timestamp }

                if (smartLogs.isEmpty()) {
                     Text(
                        strings.noSmartActivity,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    smartLogs.forEach { log ->
                        SmartActivityRow(
                            log = log,
                            onDelete = { viewModel.deleteWorkout(log) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AddWorkoutDialog(
    existingLog: WorkoutLog? = null,
    onDismiss: () -> Unit, 
    onSave: (String, Int, Int) -> Unit
) {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    var name by remember { mutableStateOf(existingLog?.exerciseName ?: "") }
    var sets by remember { mutableStateOf(existingLog?.sets?.toString() ?: "") }
    var reps by remember { mutableStateOf(existingLog?.reps?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingLog != null) strings.editWorkout else strings.addWorkout, color = colors.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.exerciseName) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary, 
                        focusedTextColor = colors.textPrimary, 
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text(strings.sets) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary, 
                            focusedTextColor = colors.textPrimary, 
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text(strings.reps) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary, 
                            focusedTextColor = colors.textPrimary, 
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, sets.toIntOrNull() ?: 0, reps.toIntOrNull() ?: 0) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(if (existingLog != null) strings.update else strings.add, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = colors.textSecondary)
            }
        },
        containerColor = colors.cardBg
    )
}

@Composable
private fun DayCell(
    dayLetter: String,
    dayNumber: String,
    isSelected: Boolean,
    hasLogs: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .width(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayLetter,
                color = if (isSelected) Color.Black else colors.textSecondary,
                fontSize = 8.sp
            )
            Text(
                text = dayNumber,
                color = if (isSelected) Color.Black else colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.Black 
                        else if (hasLogs) colors.primary 
                        else Color.Transparent
                    )
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColors
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = colors.textSecondary, fontSize = 8.sp)
    }
}

@Composable
private fun HistoryRow(
    log: WorkoutLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FitnessCenter, null, tint = colors.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(log.exerciseName, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "${log.sets} ${strings.sets.lowercase()} • ${log.reps} ${strings.reps.lowercase()} • ${dateFormat.format(Date(log.timestamp))}", 
                color = colors.textSecondary, 
                fontSize = 10.sp
            )
        }
        
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(colors.cardBg)
            ) {
                DropdownMenuItem(
                    text = { Text(strings.add, color = colors.textPrimary) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = colors.primary) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFE74C3C)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFE74C3C)) }
                )
            }
        }
    }
}

@Composable
private fun SmartActivityRow(
    log: WorkoutLog,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    
    val translatedType = when (log.activityType?.lowercase()) {
        "walking" -> strings.walking
        "running" -> strings.running
        else -> log.activityType ?: strings.walking
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = colors.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(translatedType, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${strings.duration}: ${log.durationMinutes}${strings.minsShort} • ${dateFormat.format(Date(log.timestamp))}", color = colors.textSecondary, fontSize = 10.sp)
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(colors.cardBg)
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFE74C3C)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFE74C3C)) }
                    )
                }
            }
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
