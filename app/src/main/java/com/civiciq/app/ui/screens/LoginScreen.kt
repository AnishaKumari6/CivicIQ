package com.civiciq.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiciq.app.auth.AuthState
import com.civiciq.app.auth.AuthViewModel

// Extracted colors matching your Home Screen aesthetic
val PrimaryBlue = Color(0xFF5B81FF)
val CardDark = Color(0xFF1E2038)
val SurfacePurple = Color(0xFF382F5C)
val TextGray = Color(0xFFA0A3BD)
val BackgroundDark = Color(0xFF0B0D1B) // Deep navy background

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark) // Forces the deep navy background
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo/Icon (Matching the home screen's CivicIQ icon)
        Icon(
            imageVector = Icons.Filled.AccountBalance,
            contentDescription = "CivicIQ Logo",
            tint = PrimaryBlue,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "CivicIQ",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = if (isLoginMode) "Welcome back, future leader" else "Start your journey today",
            color = TextGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                Text(
                    text = errorMessage, 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                AuthForm(
                    email = email,
                    password = password,
                    isLoginMode = isLoginMode,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onModeToggle = { isLoginMode = !isLoginMode },
                    viewModel = viewModel
                )
            }
            else -> {
                AuthForm(
                    email = email,
                    password = password,
                    isLoginMode = isLoginMode,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onModeToggle = { isLoginMode = !isLoginMode },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun AuthForm(
    email: String,
    password: String,
    isLoginMode: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    viewModel: AuthViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current 
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    // Custom sleek TextField for Email
    TextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = { Text("Email", color = TextGray) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CardDark,
            unfocusedContainerColor = CardDark,
            focusedIndicatorColor = Color.Transparent, // Removes the harsh bottom line
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Custom sleek TextField for Password
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text("Password", color = TextGray) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CardDark,
            unfocusedContainerColor = CardDark,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Primary Action Button
    Button(
        onClick = { 
            if (isLoginMode) viewModel.signInWithEmail(email, password)
            else viewModel.signUpWithEmail(email, password)
        },
        enabled = isFormValid,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue,
            disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Taller button for a more premium feel
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Sign Up",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Google Sign In Button
    Button(
        onClick = { viewModel.signInWithGoogle(context) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SurfacePurple),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Continue with Google",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Mode Toggle (Clean text at the bottom)
    TextButton(
        onClick = onModeToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login",
            color = PrimaryBlue,
            fontWeight = FontWeight.Medium
        )
    }
}