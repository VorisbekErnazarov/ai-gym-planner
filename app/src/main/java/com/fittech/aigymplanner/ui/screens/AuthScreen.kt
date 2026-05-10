package com.fittech.aigymplanner.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittech.aigymplanner.ui.theme.*
import com.fittech.aigymplanner.viewmodel.AuthState
import com.fittech.aigymplanner.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.appColors
    val strings = appStrings()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val successMessage by viewModel.message.collectAsState()

    // Handle Success State
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    // Show Success Toast
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.surface, colors.background))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(strings.appName, color = colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(strings.fitnessCoach, color = colors.textSecondary, fontSize = 12.sp)

                Spacer(Modifier.height(32.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(strings.emailAddress, color = colors.textSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = authState !is AuthState.Loading
                )

                Spacer(Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(strings.password, color = colors.textSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true,
                    enabled = authState !is AuthState.Loading
                )

                // Error Message Animation
                AnimatedVisibility(
                    visible = authState is AuthState.Error,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color(0xFFE74C3C),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Main Action Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(strings.signIn, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Toggle Mode Button
                OutlinedButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.primary),
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(
                        strings.newHere,
                        color = colors.primary,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(strings.forgotPassword, color = colors.textSecondary, fontSize = 11.sp)
                }

                Spacer(Modifier.height(16.dp))
                
                // Divider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border, thickness = 0.5.dp)
                    Text(strings.orContinueWith, color = colors.textSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 12.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border, thickness = 0.5.dp)
                }

                Spacer(Modifier.height(16.dp))

                // Social Buttons
                SocialButton(strings.continueWithGoogle, Modifier.fillMaxWidth()) {
                    viewModel.signInWithGoogle(context)
                }
            }
        }
    }
}

@Composable
private fun SocialButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = colors.textSecondary, fontSize = 11.sp)
    }
}
