package com.civiciq.app.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize the repository with the application context
    private val repository = AuthRepository(application)
    
    // The Compose UI will listen to this stream of data
    val authState = repository.authState

   fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch { 
           
            repository.signInWithGoogle(context) 
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch { 
            repository.signInWithEmail(email, pass) 
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch { 
            repository.signUpWithEmail(email, pass) 
        }
    }

    fun signOut() {
        repository.signOut()
    }
}
