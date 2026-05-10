package com.fittech.aigymplanner.navigation

/**
 * All navigation destinations in the app.
 * Using a sealed class keeps route strings type-safe and in one place.
 */
sealed class Screen(val route: String) {
    object Auth           : Screen("auth")
    object Register       : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home           : Screen("home")
    object ExerciseList : Screen("exercise_list")
    object WorkoutPlan  : Screen("workout_plan")
    object Saved        : Screen("saved")
    object History      : Screen("history")
    object Stats        : Screen("stats")
    object Profile      : Screen("profile")
}
