package com.civiciq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.civiciq.app.auth.AuthViewModel
import com.civiciq.app.navigation.CivicIQNavGraph
import com.civiciq.app.ui.theme.CivicIQTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ native splash screen
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CivicIQTheme(darkTheme = true) {
                val navController = rememberNavController()
                CivicIQNavGraph(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
