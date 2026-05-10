package com.fittech.aigymplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.model.Exercise
import com.fittech.aigymplanner.ui.theme.*

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isSaved: Boolean = false,
    onSaveToggle: (Exercise) -> Unit = {}
) {
    val colors = MaterialTheme.appColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon on the left
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            // Name + details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = (exercise.muscle ?: "Unknown").replace("_", " ").replaceFirstChar { it.uppercase() },
                    color = colors.primary,
                    fontSize = 12.sp
                )
                val equipmentText = exercise.getDisplayEquipment()
                if (equipmentText.isNotBlank()) {
                    Text(
                        text = equipmentText.replace("_", " "),
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Difficulty badge
            DifficultyBadge(difficulty = exercise.difficulty ?: "Unknown")

            Spacer(Modifier.width(8.dp))

            // Save bookmark icon
            IconButton(onClick = { onSaveToggle(exercise) }) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "Unsave" else "Save",
                    tint = if (isSaved) colors.primary else colors.textSecondary
                )
            }
        }
    }
}

/**
 * A small coloured badge showing the exercise difficulty.
 */
@Composable
fun DifficultyBadge(difficulty: String) {
    val colors = MaterialTheme.appColors
    val (bgColor, label) = when (difficulty.lowercase()) {
        "beginner"     -> Color(0xFF1A6B3A) to "Beginner"
        "intermediate" -> Color(0xFF7B5A00) to "Inter"
        "expert"       -> Color(0xFF7B1A1A) to "Expert"
        else           -> colors.surface   to difficulty.replaceFirstChar { it.uppercase() }
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val textColor = if (bgColor == colors.surface && colors.background == Color.White) Color.Black else Color.White
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * A filter chip for muscle group selection.
 */
@Composable
fun MuscleFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val bg = if (selected) colors.primary else colors.cardBg
    val textColor = if (selected) Color.Black else colors.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = colors.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = colors.textSecondary, fontSize = 11.sp)
        }
    }
}

fun Float.formatWithDigits(digits: Int) = "%.${digits}f".format(this)
