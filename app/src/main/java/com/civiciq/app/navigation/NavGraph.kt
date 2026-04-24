package com.civiciq.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.civiciq.app.data.local.ContentRepository
import com.civiciq.app.data.local.DataStoreManager
import com.civiciq.app.ui.screens.*

@Composable
fun CivicIQNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val repository = ContentRepository(context)
    val dataStoreManager = DataStoreManager(context)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = EaseOutCubic)
            ) + fadeIn(tween(350))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(350, easing = EaseInCubic)
            ) + fadeOut(tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(350, easing = EaseOutCubic)
            ) + fadeIn(tween(350))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(350, easing = EaseInCubic)
            ) + fadeOut(tween(200))
        }
    ) {

        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(tween(500)) },
            exitTransition = { fadeOut(tween(500)) }
        ) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                dataStoreManager = dataStoreManager
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(
                dataStoreManager = dataStoreManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                dataStoreManager = dataStoreManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Flashcard.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "legislature"
            FlashcardScreen(
                category = category,
                repository = repository,
                dataStoreManager = dataStoreManager,
                onBack = { navController.popBackStack() },
                onOpenArticle = { cat, id ->
                    navController.navigate(Screen.ArticleDetail.createRoute(cat, id))
                }
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "legislature"
            QuizScreen(
                category = category,
                repository = repository,
                dataStoreManager = dataStoreManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SpinWheel.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "legislature"
            SpinWheelScreen(
                category = category,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("flashcardId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "legislature"
            val flashcardId = backStackEntry.arguments?.getString("flashcardId") ?: ""
            ArticleDetailScreen(
                category = category,
                flashcardId = flashcardId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
