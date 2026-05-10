package com.fittech.aigymplanner.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fittech.aigymplanner.ui.components.ActivityRecognitionPermissionHandler
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.ActivityRecognitionViewModel
import com.fittech.aigymplanner.viewmodel.AppLanguage
import com.fittech.aigymplanner.viewmodel.LanguageViewModel
import com.fittech.aigymplanner.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    languageViewModel: LanguageViewModel,
    onBack: () -> Unit, 
    onSignOut: () -> Unit
) {
    val activityViewModel: ActivityRecognitionViewModel = viewModel()
    val isTracking by activityViewModel.isTracking.collectAsState()
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    val context = LocalContext.current
    var showPermissionHandler by remember { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
    val userEmail = user?.email ?: ""
    val userInitials = userName.take(2).uppercase()

    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var cloudSyncEnabled by remember { mutableStateOf(prefs.getBoolean("cloud_sync", true)) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(userName) }
    val focusRequester = remember { FocusRequester() }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(strings.editWorkout.replace("Workout", strings.profile), color = colors.primary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it },
                        label = { Text(strings.exerciseName.replace("Exercise", strings.profile)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
                LaunchedEffect(Unit) {
                    delay(100)
                    focusRequester.requestFocus()
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                            displayName = newDisplayName
                        }
                        user?.updateProfile(profileUpdates)?.addOnSuccessListener {
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(strings.save, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(strings.cancel, color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBg
        )
    }

    if (showPermissionHandler) {
        ActivityRecognitionPermissionHandler(
            onPermissionGranted = {
                showPermissionHandler = false
                activityViewModel.startTracking(context)
            },
            onPermissionDenied = {
                showPermissionHandler = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                
                // Profile Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.7f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userInitials, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("$userEmail • ${strings.beginner}", color = colors.textSecondary, fontSize = 10.sp)
                    }
                    IconButton(onClick = { 
                        newDisplayName = user?.displayName ?: ""
                        showEditProfileDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel(strings.appearance)
                
                // Theme Grid
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeCard("Dark Green", Color(0xFF0F1923), Color(0xFF2ECC71), currentTheme == AppTheme.DarkGreen, Modifier.weight(1f)) { 
                            themeViewModel.setTheme(AppTheme.DarkGreen) 
                            prefs.edit().putString("theme", AppTheme.DarkGreen.name).apply()
                        }
                        ThemeCard("Dark Blue", Color(0xFF1A1A2E), Color(0xFF7C83FD), currentTheme == AppTheme.DarkBlue, Modifier.weight(1f)) { 
                            themeViewModel.setTheme(AppTheme.DarkBlue) 
                            prefs.edit().putString("theme", AppTheme.DarkBlue.name).apply()
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeCard("Light", Color.White, Color(0xFF2ECC71), currentTheme == AppTheme.Light, Modifier.weight(1f)) { 
                            themeViewModel.setTheme(AppTheme.Light) 
                            prefs.edit().putString("theme", AppTheme.Light.name).apply()
                        }
                        ThemeCard("Dark Amber", Color(0xFF1A1200), Color(0xFFF1C40F), currentTheme == AppTheme.DarkAmber, Modifier.weight(1f)) { 
                            themeViewModel.setTheme(AppTheme.DarkAmber) 
                            prefs.edit().putString("theme", AppTheme.DarkAmber.name).apply()
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel(strings.language)
                
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column {
                        LanguageRow("🇺🇿", "Uzbek", "O'zbekcha", currentLanguage == AppLanguage.Uzbek) { 
                            languageViewModel.setLanguage(AppLanguage.Uzbek) 
                            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putString("lang", "uz").apply()
                        }
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        LanguageRow("🇬🇧", "English", "English", currentLanguage == AppLanguage.English) { 
                            languageViewModel.setLanguage(AppLanguage.English) 
                            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putString("lang", "en").apply()
                        }
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        LanguageRow("🇷🇺", "Russian", "Русский", currentLanguage == AppLanguage.Russian) { 
                            languageViewModel.setLanguage(AppLanguage.Russian) 
                            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putString("lang", "ru").apply()
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel(strings.preferences)
                
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column {
                        ToggleRow(strings.notifications, strings.workoutReminders, notificationsEnabled) { 
                            notificationsEnabled = it
                            prefs.edit().putBoolean("notifications", it).apply()
                        }
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        ToggleRow(strings.autoDetect, strings.walking + " & " + strings.running, isTracking) { checked ->
                            if (checked) {
                                showPermissionHandler = true
                            } else {
                                activityViewModel.stopTracking(context)
                            }
                        }
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                        ToggleRow(strings.cloudSync, strings.syncWithFirebase, cloudSyncEnabled) { 
                            cloudSyncEnabled = it
                            prefs.edit().putBoolean("cloud_sync", it).apply()
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        strings.signOut,
                        color = Color(0xFFE74C3C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onSignOut() }
                            .padding(vertical = 12.dp)
                    )
                }
                
                Spacer(Modifier.height(24.dp))
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

@Composable
private fun ThemeCard(label: String, bgColor: Color, accentColor: Color, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.cardBg)
            .border(1.5.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(bgColor)
                .padding(4.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor).align(Alignment.BottomEnd))
        }
        Text(
            text = label,
            color = if (isSelected) colors.primary else colors.textSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(8.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LanguageRow(flag: String, name: String, nativeName: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(nativeName, color = colors.textSecondary, fontSize = 10.sp)
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primary else Color.Transparent)
                .border(1.dp, if (isSelected) colors.primary else colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = colors.textSecondary, fontSize = 10.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.border
            )
        )
    }
}
