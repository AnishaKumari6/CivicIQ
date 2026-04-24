package com.civiciq.app.data.local

import android.content.Context
import com.civiciq.app.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentRepository(private val context: Context) {

    private val gson = Gson()
    private var cachedContent: CivicContent? = null

    suspend fun loadContent(): Result<CivicContent> = withContext(Dispatchers.IO) {
        try {
            cachedContent?.let { return@withContext Result.success(it) }
            val json = context.assets.open("civic_content.json")
                .bufferedReader()
                .use { it.readText() }
            val content = gson.fromJson(json, CivicContent::class.java)
            cachedContent = content
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFlashcards(category: String): Result<List<Flashcard>> {
        return loadContent().map { content ->
            when (category.lowercase()) {
                "legislature" -> content.legislature.flashcards
                "executive" -> content.executive.flashcards
                "judiciary" -> content.judiciary.flashcards
                else -> content.legislature.flashcards
            }
        }
    }

    suspend fun getQuizQuestions(category: String): Result<List<QuizQuestion>> {
        return loadContent().map { content ->
            when (category.lowercase()) {
                "legislature" -> content.legislature.quiz
                "executive" -> content.executive.quiz
                "judiciary" -> content.judiciary.quiz
                else -> content.legislature.quiz
            }
        }
    }

    suspend fun getSpinTopics(category: String): Result<List<String>> {
        return loadContent().map { content ->
            when (category.lowercase()) {
                "legislature" -> content.legislature.spinTopics
                "executive" -> content.executive.spinTopics
                "judiciary" -> content.judiciary.spinTopics
                else -> content.legislature.spinTopics
            }
        }
    }
}
