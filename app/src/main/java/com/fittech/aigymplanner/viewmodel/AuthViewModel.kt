package com.fittech.aigymplanner.viewmodel

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittech.aigymplanner.data.remote.FirebaseManager
import com.fittech.aigymplanner.model.UserStats
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _userStats = MutableStateFlow<UserStats>(UserStats())
    val userStats = _userStats.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun resetMessage() {
        _message.value = null
    }

    fun startStatsObservation() {
        firebaseManager.observeUserStats { stats ->
            _userStats.value = stats
        }
    }

    fun fetchUserStats() {
        viewModelScope.launch {
            _isSyncing.value = true
            val stats = firebaseManager.fetchUserStats()
            if (stats != null) {
                _userStats.value = stats
            }
            _isSyncing.value = false
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun signUp(email: String, password: String, username: String, profilePicUri: Uri?) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    var downloadUrl: String? = null
                    if (profilePicUri != null) {
                        downloadUrl = firebaseManager.uploadProfilePicture(user.uid, profilePicUri)
                    }
                    
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                        photoUri = downloadUrl?.let { Uri.parse(it) }
                    }
                    user.updateProfile(profileUpdates).await()
                    
                    firebaseManager.saveUserProfile(user.uid, email, username, downloadUrl)
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Sign up failed")
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Please enter your email address")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = firebaseManager.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _authState.value = AuthState.Idle
                _message.value = "Reset link sent to your email!"
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)
        
        // IMPORTANT: Replace this with your real Web Client ID from Firebase Console
        // You can find it in your updated google-services.json (client_type: 3)
        val webClientId = "141608464545-9pg7m0ajo0mjupnigqg36m5lh1c3queo.apps.googleusercontent.com"

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                handleGoogleSignIn(result)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    private suspend fun handleGoogleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val googleIdToken = credential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseManager.signInWithCredential(firebaseCredential)
            if (authResult.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(authResult.exceptionOrNull()?.message ?: "Firebase login failed")
            }
        } else {
            _authState.value = AuthState.Error("Unexpected credential type")
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            auth.signOut()
        }
    }
}
