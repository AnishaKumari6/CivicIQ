package com.civiciq.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.civiciq.app.data.local.ContentRepository
import com.civiciq.app.data.local.DataStoreManager
import com.civiciq.app.data.model.*
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    category: String,
    repository: ContentRepository,
    dataStoreManager: DataStoreManager,
    onBack: () -> Unit,
    onOpenArticle: (String, String) -> Unit
) {
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val categoryColor = when (category) {
        "legislature" -> LegislatureColor
        "executive" -> ExecutiveColor
        "judiciary" -> JudiciaryColor
        else -> ElectricBlue
    }

    LaunchedEffect(category) {
        isLoading = true
        repository.getFlashcards(category).onSuccess { cards ->
            flashcards = cards
            isLoading = false
        }
    }

    // Save progress when index changes
    LaunchedEffect(currentIndex, flashcards.size) {
        if (flashcards.isNotEmpty()) {
            dataStoreManager.updateFlashcardProgress(category, currentIndex + 1)
        }
    }

    // Flip rotation animation
    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500, easing = EaseInOutCubic),
        label = "flip"
    )

    // Swipe drag animation
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "drag"
    )

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            CivicTopAppBar(
                title = "${category.replaceFirstChar { it.uppercase() }} Flashcards",
                onNavigationClick = onBack,
                navigationIcon = Icons.Default.ArrowBack
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = categoryColor)
                }
            } else if (flashcards.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Style,
                    title = "No Flashcards",
                    message = "Flashcards for this category will appear here",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val card = flashcards.getOrNull(currentIndex)
                val progress = (currentIndex + 1).toFloat() / flashcards.size.toFloat()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.md)
                ) {
                    // Progress bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${currentIndex + 1} / ${flashcards.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = categoryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        AnimatedProgressBar(
                            progress = progress,
                            progressColor = categoryColor,
                            trackColor = categoryColor.copy(alpha = 0.2f),
                            height = 8.dp
                        )
                    }

                    Spacer(Modifier.height(AppSpacing.lg))

                    // Swipe hint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SwipeLeft, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Swipe to navigate", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.SwipeRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    Spacer(Modifier.height(AppSpacing.sm))

                    // Flashcard
                    if (card != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            FlashcardView(
                                card = card,
                                isFlipped = isFlipped,
                                flipRotation = flipRotation,
                                dragOffset = animatedDragOffset,
                                categoryColor = categoryColor,
                                onFlip = { isFlipped = !isFlipped },
                                onDrag = { delta ->
                                    if (!isAnimating) dragOffset += delta
                                },
                                onDragEnd = {
                                    if (dragOffset < -150f && currentIndex < flashcards.size - 1) {
                                        isAnimating = true
                                        isFlipped = false
                                        currentIndex++
                                    } else if (dragOffset > 150f && currentIndex > 0) {
                                        isAnimating = true
                                        isFlipped = false
                                        currentIndex--
                                    }
                                    dragOffset = 0f
                                    scope.launch {
                                        kotlinx.coroutines.delay(200)
                                        isAnimating = false
                                    }
                                }
                            )
                        }
                    }

                    // Navigation buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.lg),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous
                        FilledTonalIconButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    isFlipped = false
                                    currentIndex--
                                }
                            },
                            enabled = currentIndex > 0,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = categoryColor.copy(alpha = 0.2f),
                                contentColor = categoryColor
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, "Previous", modifier = Modifier.size(24.dp))
                        }

                        // Flip button
                        Button(
                            onClick = { isFlipped = !isFlipped },
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Flip, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isFlipped) "See Question" else "Flip Card",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Next
                        FilledTonalIconButton(
                            onClick = {
                                if (currentIndex < flashcards.size - 1) {
                                    isFlipped = false
                                    currentIndex++
                                }
                            },
                            enabled = currentIndex < flashcards.size - 1,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = categoryColor.copy(alpha = 0.2f),
                                contentColor = categoryColor
                            )
                        ) {
                            Icon(Icons.Default.ArrowForward, "Next", modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardView(
    card: Flashcard,
    isFlipped: Boolean,
    flipRotation: Float,
    dragOffset: Float,
    categoryColor: Color,
    onFlip: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .graphicsLayer {
                rotationX = 0f
                translationX = dragOffset
                rotationZ = dragOffset / 30f
                alpha = 1f - (kotlin.math.abs(dragOffset) / 800f)
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onHorizontalDrag = { _, dragAmount -> onDrag(dragAmount) }
                )
            }
            .clickable { onFlip() }
    ) {
        // Front face
        if (flipRotation <= 90f) {
            CardFace(
                card = card,
                isFront = true,
                categoryColor = categoryColor,
                rotation = flipRotation,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Back face
        if (flipRotation > 90f) {
            CardFace(
                card = card,
                isFront = false,
                categoryColor = categoryColor,
                rotation = flipRotation - 180f,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CardFace(
    card: Flashcard,
    isFront: Boolean,
    categoryColor: Color,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (isFront) {
        listOf(NavyCard, Color(0xFF1E2B5E), NavyCard)
    } else {
        listOf(Color(0xFF1A2040), categoryColor.copy(alpha = 0.3f), Color(0xFF1A2040))
    }

    Box(
        modifier = modifier
            .graphicsLayer { rotationY = rotation }
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .border(
                1.dp,
                if (isFront) Color(0x30FFFFFF) else categoryColor.copy(alpha = 0.4f),
                RoundedCornerShape(24.dp)
            )
    ) {
        // Side indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(AppSpacing.md)
                .clip(RoundedCornerShape(50))
                .background(
                    if (isFront) Color(0x20FFFFFF) else categoryColor.copy(alpha = 0.3f)
                )
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = if (isFront) "Question" else "Answer",
                style = MaterialTheme.typography.labelSmall,
                color = if (isFront) TextSecondary else categoryColor,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFront) {
                // Front content
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📖", fontSize = 32.sp)
                }
                Spacer(Modifier.height(AppSpacing.lg))
                Text(
                    text = card.frontTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.md))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x15FFFFFF))
                        .padding(AppSpacing.md)
                ) {
                    Text(
                        text = card.frontSummary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(AppSpacing.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TouchApp, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tap to reveal answer", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            } else {
                // Back content
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 32.sp)
                }
                Spacer(Modifier.height(AppSpacing.md))
                Text(
                    text = "Explanation",
                    style = MaterialTheme.typography.labelMedium,
                    color = categoryColor,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    text = card.backExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(AppSpacing.md))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryColor.copy(alpha = 0.1f))
                        .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(AppSpacing.md)
                ) {
                    Column {
                        Text(
                            "Real-world Example:",
                            style = MaterialTheme.typography.labelMedium,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = card.backExample,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
