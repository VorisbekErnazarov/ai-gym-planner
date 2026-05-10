package com.fittech.aigymplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.ui.components.ExerciseCard
import com.fittech.aigymplanner.ui.components.MuscleFilterChip
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    viewModel: ExerciseViewModel,
    onBack: () -> Unit
) {
    val exercises      by viewModel.filteredExercises.collectAsState()
    val savedExercises by viewModel.savedExercises.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val selectedMuscle by viewModel.selectedMuscle.collectAsState()
    val muscleGroups      = viewModel.getMuscleGroups()
    val savedNames        = savedExercises.map { it.name }.toSet()
    
    val colors = MaterialTheme.appColors
    val strings = appStrings()

    // Local search state — filters the already-filtered list
    var searchQuery by remember { mutableStateOf("") }
    val displayList = if (searchQuery.isBlank()) exercises
                      else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.exerciseLibrary, color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchExercises, color = colors.textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor     = colors.textPrimary,
                    unfocusedTextColor   = colors.textPrimary,
                    cursorColor          = colors.primary
                )
            )

            // Muscle group filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(muscleGroups) { muscle ->
                    MuscleFilterChip(
                        label    = muscle,
                        selected = muscle == selectedMuscle,
                        onClick  = { viewModel.setMuscleFilter(muscle) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Result count
            Text(
                text = "${displayList.size} ${strings.exerciseCountSuffix}",
                color = colors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Loading spinner
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            // Exercise list
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = displayList,
                    key   = { it.name }
                ) { exercise ->
                    ExerciseCard(
                        exercise    = exercise,
                        isSaved     = exercise.name in savedNames,
                        onSaveToggle = { ex ->
                            if (ex.name in savedNames) viewModel.removeSavedExercise(ex.name)
                            else viewModel.saveExercise(ex)
                        }
                    )
                }

                // Empty state
                if (!isLoading && displayList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(strings.noExercisesFound, color = colors.textSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
