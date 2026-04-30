package com.civiciq.app.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val applicationContext: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(applicationContext)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // --- GOOGLE SIGN IN ---
    suspend fun signInWithGoogle(uiContext: Context) {
        _authState.value = AuthState.Loading
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("320269285643-nd9jea5p6s9emlhrr6cg9hv88vcul5gq.apps.googleusercontent.com") 
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Uses the UI context passed from the button click
            val result = credentialManager.getCredential(uiContext, request)
            val credential = result.credential

            if (credential is GoogleIdTokenCredential) {
                val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                
                authResult.user?.let {
                    _authState.value = AuthState.Authenticated(it)
                }
            } else {
                _authState.value = AuthState.Error("Invalid credential type received.")
            }

        // THESE ARE THE BLOCKS THAT WENT MISSING:
        } catch (e: GetCredentialCancellationException) {
            _authState.value = AuthState.Unauthenticated // User closed the bottom sheet
        } catch (e: Exception) {
            Log.e("AuthRepo", "Google Sign In Failed", e)
            _authState.value = AuthState.Error(e.message ?: "Google Sign In Failed")
        }
    }

    // --- EMAIL / PASSWORD ---
    suspend fun signUpWithEmail(email: String, pass: String) {
        _authState.value = AuthState.Loading
        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            result.user?.let { _authState.value = AuthState.Authenticated(it) }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signInWithEmail(email: String, pass: String) {
        _authState.value = AuthState.Loading
        try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user?.let { _authState.value = AuthState.Authenticated(it) }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
        }
    }

    // --- LOGOUT ---
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}