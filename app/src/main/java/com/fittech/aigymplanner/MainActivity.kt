package com.fittech.aigymplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fittech.aigymplanner.navigation.NavGraph
import com.fittech.aigymplanner.navigation.Screen
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.LanguageViewModel
import com.fittech.aigymplanner.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val languageViewModel: LanguageViewModel = viewModel()
            
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
                val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                
                val savedLang = prefs.getString("lang", "en") ?: "en"
                val appLang = when(savedLang) {
                    "uz" -> com.fittech.aigymplanner.viewmodel.AppLanguage.Uzbek
                    "ru" -> com.fittech.aigymplanner.viewmodel.AppLanguage.Russian
                    else -> com.fittech.aigymplanner.viewmodel.AppLanguage.English
                }
                languageViewModel.setLanguage(appLang)

                val savedTheme = prefs.getString("theme", "DarkGreen") ?: "DarkGreen"
                val appTheme = try { com.fittech.aigymplanner.ui.theme.AppTheme.valueOf(savedTheme) } catch(e: Exception) { com.fittech.aigymplanner.ui.theme.AppTheme.DarkGreen }
                themeViewModel.setTheme(appTheme)
            }
            
            val currentAppTheme by themeViewModel.currentTheme.collectAsState()
            val currentLanguage by languageViewModel.currentLanguage.collectAsState()

            AIGymPlannerTheme(theme = currentAppTheme, language = currentLanguage) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavScreens = listOf(
                    Screen.Home.route,
                    Screen.History.route,
                    Screen.Stats.route,
                    Screen.Profile.route,
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomNavScreens) {
                            AppBottomNavigation(navController, currentRoute)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(
                            navController = navController, 
                            themeViewModel = themeViewModel,
                            languageViewModel = languageViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigation(navController: NavController, currentRoute: String?) {
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    
    val items = listOf(
        BottomNavItem(strings.home, Screen.Home.route, Icons.Outlined.Home, Icons.Rounded.Home),
        BottomNavItem(strings.log, Screen.History.route, Icons.Outlined.CalendarToday, Icons.Rounded.CalendarToday),
        BottomNavItem(strings.stats, Screen.Stats.route, Icons.Outlined.BarChart, Icons.Rounded.BarChart),
        BottomNavItem(strings.profile, Screen.Profile.route, Icons.Outlined.Person, Icons.Rounded.Person)
    )

    NavigationBar(
        containerColor = colors.surface,
        modifier = Modifier.drawBehind {
            val strokeWidth = 0.5.dp.toPx()
            drawLine(
                color = colors.border,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = strokeWidth
            )
        },
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = colors.textSecondary,
                    selectedTextColor = colors.primary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = colors.primary
                )
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)
