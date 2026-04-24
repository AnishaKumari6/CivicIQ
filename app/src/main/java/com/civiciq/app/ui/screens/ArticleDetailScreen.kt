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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.civiciq.app.data.local.ContentRepository
import com.civiciq.app.data.model.Flashcard
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*

@Composable
fun ArticleDetailScreen(
    category: String,
    flashcardId: String,
    repository: ContentRepository,
    onBack: () -> Unit
) {
    var flashcard by remember { mutableStateOf<Flashcard?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isOriginalExpanded by remember { mutableStateOf(false) }
    var isExampleExpanded by remember { mutableStateOf(true) }

    val categoryColor = when (category) {
        "legislature" -> LegislatureColor
        "executive" -> ExecutiveColor
        "judiciary" -> JudiciaryColor
        else -> ElectricBlue
    }

    LaunchedEffect(category, flashcardId) {
        repository.getFlashcards(category).onSuccess { cards ->
            flashcard = cards.find { it.id == flashcardId } ?: cards.firstOrNull()
            isLoading = false
        }
    }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CivicTopAppBar(
                title = "Article Detail",
                onNavigationClick = onBack,
                navigationIcon = Icons.Default.ArrowBack,
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Default.Share, null, tint = TextSecondary)
                    }
                    IconButton(onClick = { /* bookmark */ }) {
                        Icon(Icons.Default.BookmarkBorder, null, tint = TextSecondary)
                    }
                }
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = categoryColor)
                }
            } else if (flashcard == null) {
                EmptyState(
                    icon = Icons.Default.Article,
                    title = "Article Not Found",
                    message = "This article could not be loaded",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val card = flashcard!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.md)
                ) {
                    Spacer(Modifier.height(AppSpacing.sm))

                    // Hero
                    ArticleHero(card = card, categoryColor = categoryColor, category = category)

                    Spacer(Modifier.height(AppSpacing.lg))

                    // Summary section
                    ArticleSection(
                        title = "Summary",
                        icon = Icons.Default.ShortText,
                        iconColor = categoryColor,
                        isExpandable = false,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Text(
                            text = card.frontSummary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            lineHeight = 26.sp
                        )
                    }

                    Spacer(Modifier.height(AppSpacing.md))

                    // Explanation section
                    ArticleSection(
                        title = "Detailed Explanation",
                        icon = Icons.Default.MenuBook,
                        iconColor = PurpleAccent,
                        isExpandable = false,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Text(
                            text = card.backExplanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 24.sp
                        )
                    }

                    Spacer(Modifier.height(AppSpacing.md))

                    // Example section (expandable)
                    ArticleSection(
                        title = "Real-world Example",
                        icon = Icons.Default.Lightbulb,
                        iconColor = GoldAccent,
                        isExpandable = true,
                        isExpanded = isExampleExpanded,
                        onToggle = { isExampleExpanded = !isExampleExpanded }
                    ) {
                        Text(
                            text = card.backExample,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(Modifier.height(AppSpacing.md))

                    // Key facts tags
                    KeyFactsSection(card = card, categoryColor = categoryColor)

                    Spacer(Modifier.height(AppSpacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun ArticleHero(card: Flashcard, categoryColor: Color, category: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = 0.4f),
                        categoryColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
            .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(AppSpacing.lg)
    ) {
        // Background deco
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 50.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.1f))
        )

        Column {
            CategoryChip(
                text = category.replaceFirstChar { it.uppercase() },
                color = categoryColor
            )
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                text = card.frontTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, null, tint = categoryColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Full Article",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Timer, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("2 min read", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
private fun ArticleSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isExpandable: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (isExpandable) onToggle else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isExpandable) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "expand_rotate"
                    )
                    Icon(
                        Icons.Default.ExpandMore,
                        null,
                        tint = TextMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }

            AnimatedVisibility(
                visible = !isExpandable || isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.md)
                ) {
                    HorizontalDivider(
                        color = Color(0x15FFFFFF),
                        modifier = Modifier.padding(bottom = AppSpacing.sm)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun KeyFactsSection(card: Flashcard, categoryColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tag, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Related Tags",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            listOf(card.category, "Constitution", "Civics").forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(categoryColor.copy(alpha = 0.1f))
                        .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        "#$tag",
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor
                    )
                }
            }
        }
    }
}
