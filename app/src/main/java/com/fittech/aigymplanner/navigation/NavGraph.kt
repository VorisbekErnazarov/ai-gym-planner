package com.fittech.aigymplanner.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fittech.aigymplanner.ui.screens.*
import com.fittech.aigymplanner.viewmodel.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    languageViewModel: LanguageViewModel
) {
    val exerciseViewModel: ExerciseViewModel = viewModel()
    val workoutViewModel: WorkoutViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = exerciseViewModel,
                authViewModel = authViewModel,
                onNavigateToExercises = { navController.navigate(Screen.ExerciseList.route) },
                onNavigateToWorkoutPlan = { navController.navigate(Screen.WorkoutPlan.route) },
                onNavigateToSaved = { navController.navigate(Screen.Saved.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) }
            )
        }

        composable(Screen.ExerciseList.route) {
            ExerciseListScreen(
                viewModel = exerciseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.WorkoutPlan.route) {
            WorkoutPlanScreen(
                viewModel = exerciseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Saved.route) {
            SavedScreen(
                viewModel = exerciseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = workoutViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(viewModel = workoutViewModel)
        }

        composable(Screen.Profile.route) {
            SettingsScreen(
                themeViewModel = themeViewModel,
                languageViewModel = languageViewModel,
                onBack = { navController.popBackStack() },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
