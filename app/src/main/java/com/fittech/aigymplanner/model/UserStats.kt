package com.fittech.aigymplanner.model

data class UserStats(
    val daysPerWeek: Int = 0,
    val completedExercises: Int = 0,
    val caloriesBurned: Int = 0,
    val savedExercisesCount: Int = 0,
    val currentStreak: Int = 0
)
