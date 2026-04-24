package com.civiciq.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiciq.app.data.local.DataStoreManager
import com.civiciq.app.data.model.QuizRecord
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    dataStoreManager: DataStoreManager,
    onBack: () -> Unit
) {
    val quizHistory by dataStoreManager.quizHistory.collectAsStateWithLifecycle(emptyList())
    val flashcardProgress by dataStoreManager.flashcardProgress.collectAsStateWithLifecycle(emptyMap())
    val totalQuizzes by dataStoreManager.totalQuizzes.collectAsStateWithLifecycle(0)
    val streak by dataStoreManager.currentStreak.collectAsStateWithLifecycle(0)

    val avgScore = if (quizHistory.isNotEmpty())
        quizHistory.map { it.percentage }.average().toInt() else 0

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CivicTopAppBar(
                title = "Your Progress",
                onNavigationClick = onBack,
                navigationIcon = Icons.Default.ArrowBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.md)
            ) {
                Spacer(Modifier.height(AppSpacing.sm))

                // Hero stats
                ProgressHeroSection(
                    totalQuizzes = totalQuizzes,
                    streak = streak,
                    avgScore = avgScore
                )

                Spacer(Modifier.height(AppSpacing.lg))

                // Flashcard progress section
                SectionHeader(title = "Flashcard Progress", icon = Icons.Default.Style)
                Spacer(Modifier.height(AppSpacing.sm))
                FlashcardProgressSection(flashcardProgress = flashcardProgress)

                Spacer(Modifier.height(AppSpacing.lg))

                // Quiz history section
                SectionHeader(title = "Quiz History", icon = Icons.Default.History)
                Spacer(Modifier.height(AppSpacing.sm))

                if (quizHistory.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Quiz,
                        title = "No Quizzes Yet",
                        message = "Take a quiz to see your history here",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    quizHistory.take(10).forEachIndexed { index, record ->
                        QuizHistoryCard(
                            record = record,
                            index = index + 1
                        )
                        Spacer(Modifier.height(AppSpacing.sm))
                    }
                }

                Spacer(Modifier.height(AppSpacing.xxl))
            }
        }
    }
}

@Composable
private fun ProgressHeroSection(
    totalQuizzes: Int,
    streak: Int,
    avgScore: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593),
                        Color(0xFF1565C0)
                    )
                )
            )
            .padding(AppSpacing.lg)
    ) {
        // Background deco
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 60.dp)
                .clip(CircleShape)
                .background(Color(0x15FFFFFF))
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Performance Overview",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xCCFFFFFF)
                )
            }
            Spacer(Modifier.height(AppSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeroStat("$totalQuizzes", "Total Quizzes", ElectricBlue)
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0x30FFFFFF))
                HeroStat("${streak}🔥", "Day Streak", SunsetOrange)
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0x30FFFFFF))
                HeroStat("$avgScore%", "Avg Score", EmeraldGreen)
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xAAFFFFFF),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FlashcardProgressSection(flashcardProgress: Map<String, Int>) {
    val categories = listOf(
        Triple("legislature", "Legislature", LegislatureColor),
        Triple("executive", "Executive", ExecutiveColor),
        Triple("judiciary", "Judiciary", JudiciaryColor)
    )
    val totalPerCategory = 6 // matches JSON

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        categories.forEach { (key, name, color) ->
            val seen = flashcardProgress[key] ?: 0
            val progress = seen.toFloat() / totalPerCategory.toFloat()

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Style, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(AppSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("$seen / $totalPerCategory", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        AnimatedProgressBar(
                            progress = progress.coerceIn(0f, 1f),
                            progressColor = color,
                            trackColor = color.copy(alpha = 0.2f),
                            height = 6.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizHistoryCard(record: QuizRecord, index: Int) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val date = dateFormatter.format(Date(record.timestamp))

    val scoreColor = when {
        record.percentage >= 80 -> SuccessColor
        record.percentage >= 60 -> WarningColor
        else -> ErrorColor
    }

    val categoryColor = when (record.category) {
        "legislature" -> LegislatureColor
        "executive" -> ExecutiveColor
        "judiciary" -> JudiciaryColor
        else -> ElectricBlue
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$index",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(AppSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$date",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${record.score} / ${record.total} correct",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            ScoreBadge(percentage = record.percentage)
        }
    }
}
