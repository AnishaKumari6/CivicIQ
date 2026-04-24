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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.civiciq.app.data.local.ContentRepository
import com.civiciq.app.data.local.DataStoreManager
import com.civiciq.app.data.model.*
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val QUESTION_TIME = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    category: String,
    repository: ContentRepository,
    dataStoreManager: DataStoreManager,
    onBack: () -> Unit
) {
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswerRevealed by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var isQuizComplete by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var timeRemaining by remember { mutableIntStateOf(QUESTION_TIME) }
    var isTimerRunning by remember { mutableStateOf(false) }
    val answerRecord = remember { mutableStateListOf<Boolean>() }
    val scope = rememberCoroutineScope()

    val categoryColor = when (category) {
        "legislature" -> LegislatureColor
        "executive" -> ExecutiveColor
        "judiciary" -> JudiciaryColor
        else -> ElectricBlue
    }

    LaunchedEffect(category) {
        repository.getQuizQuestions(category).onSuccess { qs ->
            questions = qs
            isLoading = false
            isTimerRunning = true
        }
    }

    // Timer logic
    LaunchedEffect(currentIndex, isTimerRunning) {
        if (!isTimerRunning || isAnswerRevealed) return@LaunchedEffect
        timeRemaining = QUESTION_TIME
        while (timeRemaining > 0 && !isAnswerRevealed) {
            delay(1000)
            timeRemaining--
        }
        if (timeRemaining == 0 && !isAnswerRevealed) {
            isAnswerRevealed = true
            answerRecord.add(false)
        }
    }

    // Save result on completion
    LaunchedEffect(isQuizComplete) {
        if (isQuizComplete && questions.isNotEmpty()) {
            dataStoreManager.saveQuizResult(
                QuizRecord(
                    category = category,
                    score = score,
                    total = questions.size,
                    timestamp = System.currentTimeMillis()
                )
            )
            dataStoreManager.updateStreak()
        }
    }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CivicTopAppBar(
                title = "${category.replaceFirstChar { it.uppercase() }} Quiz",
                onNavigationClick = onBack,
                navigationIcon = Icons.Default.ArrowBack,
                actions = {
                    if (!isQuizComplete && !isLoading) {
                        Box(
                            modifier = Modifier
                                .padding(end = AppSpacing.md)
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularTimer(
                                timeRemaining = timeRemaining,
                                totalTime = QUESTION_TIME,
                                color = categoryColor
                            )
                        }
                    }
                }
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = categoryColor)
                }
                isQuizComplete -> QuizResultScreen(
                    score = score,
                    total = questions.size,
                    answerRecord = answerRecord.toList(),
                    categoryColor = categoryColor,
                    category = category,
                    onRetry = {
                        currentIndex = 0
                        score = 0
                        selectedAnswer = null
                        isAnswerRevealed = false
                        isQuizComplete = false
                        answerRecord.clear()
                        isTimerRunning = true
                        timeRemaining = QUESTION_TIME
                    },
                    onBack = onBack
                )
                questions.isEmpty() -> EmptyState(
                    icon = Icons.Default.Quiz,
                    title = "No Questions",
                    message = "Quiz questions for this category will appear here",
                    modifier = Modifier.fillMaxSize()
                )
                else -> {
                    val question = questions.getOrNull(currentIndex)
                    if (question != null) {
                        QuizContent(
                            question = question,
                            questionNumber = currentIndex + 1,
                            total = questions.size,
                            selectedAnswer = selectedAnswer,
                            isAnswerRevealed = isAnswerRevealed,
                            categoryColor = categoryColor,
                            onAnswerSelected = { answerIndex ->
                                if (!isAnswerRevealed) {
                                    selectedAnswer = answerIndex
                                    isAnswerRevealed = true
                                    isTimerRunning = false
                                    val isCorrect = answerIndex == question.correctAnswer
                                    if (isCorrect) score++
                                    answerRecord.add(isCorrect)
                                }
                            },
                            onNext = {
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    selectedAnswer = null
                                    isAnswerRevealed = false
                                    isTimerRunning = true
                                } else {
                                    isQuizComplete = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==================== QUIZ CONTENT ====================

@Composable
private fun QuizContent(
    question: QuizQuestion,
    questionNumber: Int,
    total: Int,
    selectedAnswer: Int?,
    isAnswerRevealed: Boolean,
    categoryColor: Color,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
            .verticalScroll(rememberScrollState())
    ) {
        // Progress
        AnimatedProgressBar(
            progress = (questionNumber - 1).toFloat() / total.toFloat(),
            progressColor = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.2f),
            height = 6.dp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Question $questionNumber of $total",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(Modifier.height(AppSpacing.lg))

        // Question card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(NavyCard, Color(0xFF1E2B5E))
                    )
                )
                .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(AppSpacing.lg)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Q$questionNumber",
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.md))

        // Options
        question.options.forEachIndexed { index, option ->
            Spacer(Modifier.height(AppSpacing.sm))
            AnswerOption(
                option = option,
                index = index,
                isSelected = selectedAnswer == index,
                isRevealed = isAnswerRevealed,
                isCorrect = index == question.correctAnswer,
                onClick = { onAnswerSelected(index) }
            )
        }

        // Explanation (after answer revealed)
        AnimatedVisibility(
            visible = isAnswerRevealed,
            enter = slideInVertically(animationSpec = tween(400)) + fadeIn(tween(400))
        ) {
            Column {
                Spacer(Modifier.height(AppSpacing.md))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(16.dp))
                        .padding(AppSpacing.md)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Explanation",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(AppSpacing.lg))

        // Next button
        AnimatedVisibility(visible = isAnswerRevealed) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (questionNumber < total) "Next Question →" else "See Results",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.xl))
    }
}

// ==================== ANSWER OPTION ====================

@Composable
private fun AnswerOption(
    option: String,
    index: Int,
    isSelected: Boolean,
    isRevealed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val optionLabels = listOf("A", "B", "C", "D")

    val bgColor by animateColorAsState(
        targetValue = when {
            isRevealed && isCorrect -> SuccessColor.copy(alpha = 0.2f)
            isRevealed && isSelected && !isCorrect -> ErrorColor.copy(alpha = 0.2f)
            isSelected -> ElectricBlue.copy(alpha = 0.2f)
            else -> Color(0x15FFFFFF)
        },
        animationSpec = tween(300),
        label = "option_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isRevealed && isCorrect -> SuccessColor
            isRevealed && isSelected && !isCorrect -> ErrorColor
            isSelected -> ElectricBlue
            else -> Color(0x20FFFFFF)
        },
        animationSpec = tween(300),
        label = "option_border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !isRevealed) { onClick() }
            .padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Option label
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(borderColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                optionLabels[index],
                style = MaterialTheme.typography.labelMedium,
                color = borderColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(AppSpacing.md))

        Text(
            text = option,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Correct/wrong icon
        if (isRevealed && (isCorrect || isSelected)) {
            Icon(
                if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isCorrect) SuccessColor else ErrorColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==================== CIRCULAR TIMER ====================

@Composable
private fun CircularTimer(
    timeRemaining: Int,
    totalTime: Int,
    color: Color
) {
    val progress = timeRemaining.toFloat() / totalTime.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "timer"
    )
    val timerColor = when {
        progress > 0.5f -> color
        progress > 0.25f -> WarningColor
        else -> ErrorColor
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .drawBehind {
                val strokeWidth = 3.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2, size.height / 2)

                drawCircle(
                    color = timerColor.copy(alpha = 0.2f),
                    radius = radius,
                    style = Stroke(strokeWidth)
                )
                drawArc(
                    color = timerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$timeRemaining",
            style = MaterialTheme.typography.labelSmall,
            color = timerColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== RESULT SCREEN ====================

@Composable
private fun QuizResultScreen(
    score: Int,
    total: Int,
    answerRecord: List<Boolean>,
    categoryColor: Color,
    category: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val percentage = if (total > 0) ((score.toFloat() / total.toFloat()) * 100).toInt() else 0
    val isExcellent = percentage >= 80
    val isGood = percentage >= 60

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(AppSpacing.xl))

            // Trophy
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(categoryColor.copy(0.3f), categoryColor.copy(0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isExcellent) "🏆" else if (isGood) "🥈" else "📚",
                    fontSize = 48.sp
                )
            }

            Spacer(Modifier.height(AppSpacing.lg))

            Text(
                text = if (isExcellent) "Outstanding!" else if (isGood) "Well Done!" else "Keep Learning!",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(AppSpacing.sm))

            Text(
                text = "You scored $score out of $total",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(Modifier.height(AppSpacing.xl))

            // Score circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(categoryColor.copy(0.2f), Color.Transparent)
                        )
                    )
                    .border(3.dp, categoryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$percentage%",
                        style = MaterialTheme.typography.displaySmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Black
                    )
                    Text("Score", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(AppSpacing.xl))

            // Answer grid
            Text(
                "Answer Summary",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(AppSpacing.sm))
            LazyAnswerGrid(answerRecord = answerRecord)

            Spacer(Modifier.height(AppSpacing.xl))

            // Actions
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(AppSpacing.sm))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, categoryColor.copy(0.5f))
            ) {
                Icon(Icons.Default.Home, null, tint = categoryColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Back to Home", color = categoryColor, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(AppSpacing.xxl))
        }
    }
}

@Composable
private fun LazyAnswerGrid(answerRecord: List<Boolean>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        answerRecord.forEachIndexed { index, correct ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (correct) SuccessColor.copy(0.2f) else ErrorColor.copy(0.2f)
                    )
                    .border(1.dp, if (correct) SuccessColor else ErrorColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (correct) SuccessColor else ErrorColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
