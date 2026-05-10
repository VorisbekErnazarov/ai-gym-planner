package com.fittech.aigymplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.ui.components.ExerciseCard
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: ExerciseViewModel,
    onBack: () -> Unit
) {
    val savedExercises by viewModel.savedExercises.collectAsState()
    val colors = MaterialTheme.appColors
    val strings = appStrings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.savedExercises, color = colors.textPrimary, fontWeight = FontWeight.Bold) },
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

        if (savedExercises.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkRemove,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        strings.noExercisesFound,
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                item {
                    Text(
                        "${savedExercises.size} ${strings.exerciseCountSuffix}",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(savedExercises, key = { it.name }) { exercise ->
                    ExerciseCard(
                        exercise     = exercise,
                        isSaved      = true,
                        onSaveToggle = { viewModel.removeSavedExercise(it.name) }
                    )
                }
            }
        }
    }
}
