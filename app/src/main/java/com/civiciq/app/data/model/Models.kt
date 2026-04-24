package com.civiciq.app.data.model

import com.google.gson.annotations.SerializedName

// ==================== CONTENT MODELS ====================

data class CivicContent(
    @SerializedName("legislature") val legislature: CategoryContent,
    @SerializedName("executive") val executive: CategoryContent,
    @SerializedName("judiciary") val judiciary: CategoryContent
)

data class CategoryContent(
    @SerializedName("flashcards") val flashcards: List<Flashcard>,
    @SerializedName("quiz") val quiz: List<QuizQuestion>,
    @SerializedName("spin_topics") val spinTopics: List<String>
)

data class Flashcard(
    @SerializedName("id") val id: String,
    @SerializedName("front_title") val frontTitle: String,
    @SerializedName("front_summary") val frontSummary: String,
    @SerializedName("back_explanation") val backExplanation: String,
    @SerializedName("back_example") val backExample: String,
    @SerializedName("category") val category: String
)

data class QuizQuestion(
    @SerializedName("id") val id: String,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("correct_answer") val correctAnswer: Int,
    @SerializedName("explanation") val explanation: String
)

// ==================== UI STATE MODELS ====================

data class FlashcardUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val progress: Float get() = if (flashcards.isEmpty()) 0f
        else (currentIndex + 1).toFloat() / flashcards.size.toFloat()
    val currentCard: Flashcard? get() = flashcards.getOrNull(currentIndex)
    val isFirst: Boolean get() = currentIndex == 0
    val isLast: Boolean get() = currentIndex == flashcards.size - 1
}

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val score: Int = 0,
    val isQuizComplete: Boolean = false,
    val isLoading: Boolean = true,
    val timeRemaining: Int = 20,
    val isTimerRunning: Boolean = false,
    val answerRecord: List<Boolean> = emptyList(),
    val error: String? = null
) {
    val progress: Float get() = if (questions.isEmpty()) 0f
        else (currentQuestionIndex).toFloat() / questions.size.toFloat()
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentQuestionIndex)
    val percentageScore: Int get() = if (questions.isEmpty()) 0
        else ((score.toFloat() / questions.size.toFloat()) * 100).toInt()
}

data class SpinWheelUiState(
    val topics: List<String> = emptyList(),
    val isSpinning: Boolean = false,
    val selectedTopic: String? = null,
    val showResult: Boolean = false,
    val currentAngle: Float = 0f,
    val isLoading: Boolean = true
)

data class ProgressUiState(
    val quizHistory: List<QuizRecord> = emptyList(),
    val flashcardProgress: Map<String, Int> = emptyMap(),
    val totalQuizzesTaken: Int = 0,
    val averageScore: Float = 0f,
    val streak: Int = 0
)

data class QuizRecord(
    val category: String,
    val score: Int,
    val total: Int,
    val timestamp: Long,
    val percentage: Int = ((score.toFloat() / total.toFloat()) * 100).toInt()
)

// ==================== NAVIGATION MODELS ====================

enum class Category(val displayName: String, val emoji: String) {
    LEGISLATURE("Legislature", "🏛"),
    EXECUTIVE("Executive", "🏛"),
    JUDICIARY("Judiciary", "⚖️")
}

enum class FeatureType(val displayName: String) {
    FLASHCARDS("Flashcards"),
    QUIZ("Quiz"),
    SPIN_WHEEL("Spin Wheel")
}

data class DrawerItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String,
    val badgeCount: Int? = null
)
