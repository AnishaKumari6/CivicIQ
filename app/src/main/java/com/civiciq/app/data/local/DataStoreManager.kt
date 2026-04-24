package com.civiciq.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.civiciq.app.data.model.QuizRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "civiciq_prefs")

class DataStoreManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        // Keys
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val PREFERRED_CATEGORY = stringPreferencesKey("preferred_category")
        val QUIZ_HISTORY_JSON = stringPreferencesKey("quiz_history_json")
        val FLASHCARD_PROGRESS_JSON = stringPreferencesKey("flashcard_progress_json")
        val TOTAL_QUIZZES = intPreferencesKey("total_quizzes")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_SESSION_DATE = longPreferencesKey("last_session_date")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val THEME_DARK = booleanPreferencesKey("theme_dark")
    }

    // ==================== READ FLOWS ====================

    val isDailyReminderEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[DAILY_REMINDER_ENABLED] ?: false }

    val preferredCategory: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[PREFERRED_CATEGORY] ?: "Legislature" }

    val quizHistory: Flow<List<QuizRecord>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[QUIZ_HISTORY_JSON] ?: return@map emptyList()
            try {
                val type = object : TypeToken<List<QuizRecord>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    val flashcardProgress: Flow<Map<String, Int>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[FLASHCARD_PROGRESS_JSON] ?: return@map emptyMap()
            try {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                gson.fromJson(json, type) ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }
        }

    val totalQuizzes: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[TOTAL_QUIZZES] ?: 0 }

    val currentStreak: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[CURRENT_STREAK] ?: 0 }

    val notificationHour: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[NOTIFICATION_HOUR] ?: 9 }

    // ==================== WRITE OPERATIONS ====================

    suspend fun setDailyReminder(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setPreferredCategory(category: String) {
        context.dataStore.edit { prefs ->
            prefs[PREFERRED_CATEGORY] = category
        }
    }

    suspend fun saveQuizResult(record: QuizRecord) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[QUIZ_HISTORY_JSON] ?: "[]"
            val type = object : TypeToken<MutableList<QuizRecord>>() {}.type
            val history: MutableList<QuizRecord> = try {
                gson.fromJson(currentJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
            history.add(0, record) // Most recent first
            // Keep only last 50 records
            if (history.size > 50) history.subList(50, history.size).clear()
            prefs[QUIZ_HISTORY_JSON] = gson.toJson(history)
            prefs[TOTAL_QUIZZES] = (prefs[TOTAL_QUIZZES] ?: 0) + 1
        }
    }

    suspend fun updateFlashcardProgress(category: String, seenCount: Int) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[FLASHCARD_PROGRESS_JSON] ?: "{}"
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            val progress: MutableMap<String, Int> = try {
                gson.fromJson(currentJson, type) ?: mutableMapOf()
            } catch (e: Exception) {
                mutableMapOf()
            }
            val current = progress[category] ?: 0
            progress[category] = maxOf(current, seenCount)
            prefs[FLASHCARD_PROGRESS_JSON] = gson.toJson(progress)
        }
    }

    suspend fun updateStreak() {
        context.dataStore.edit { prefs ->
            val lastSession = prefs[LAST_SESSION_DATE] ?: 0L
            val now = System.currentTimeMillis()
            val daysDiff = (now - lastSession) / (1000 * 60 * 60 * 24)
            val currentStreak = prefs[CURRENT_STREAK] ?: 0

            prefs[CURRENT_STREAK] = when {
                daysDiff <= 1L -> currentStreak + 1
                else -> 1
            }
            prefs[LAST_SESSION_DATE] = now
        }
    }

    suspend fun setNotificationHour(hour: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_HOUR] = hour
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
