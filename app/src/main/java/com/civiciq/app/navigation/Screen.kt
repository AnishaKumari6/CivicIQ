package com.civiciq.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Login : Screen("login")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object About : Screen("about")

    object Flashcard : Screen("flashcard/{category}") {
        fun createRoute(category: String) = "flashcard/$category"
    }

    object Quiz : Screen("quiz/{category}") {
        fun createRoute(category: String) = "quiz/$category"
    }

    object SpinWheel : Screen("spin/{category}") {
        fun createRoute(category: String) = "spin/$category"
    }

    object ArticleDetail : Screen("article/{category}/{flashcardId}") {
        fun createRoute(category: String, flashcardId: String) = "article/$category/$flashcardId"
    }
}
