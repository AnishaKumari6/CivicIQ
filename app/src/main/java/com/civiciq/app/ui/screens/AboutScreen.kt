package com.civiciq.app.ui.screens

import androidx.compose.animation.*
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
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*

@Composable
fun AboutScreen(onBack: () -> Unit) {
    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CivicTopAppBar(
                title = "About CivicIQ",
                onNavigationClick = onBack,
                navigationIcon = Icons.Default.ArrowBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(AppSpacing.lg))

                // App logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricBlue, PurpleAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x30FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚖", fontSize = 36.sp)
                    }
                }

                Spacer(Modifier.height(AppSpacing.md))
                Text(
                    "CivicIQ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    "Learn the Indian Constitution through\ninteractive flashcards, quizzes & spin wheel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(AppSpacing.xl))

                // Feature highlights
                Text(
                    "What's Inside",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm))

                val features = listOf(
                    Triple(Icons.Default.Style, "Flashcards", "Swipe-based learning cards on Legislature, Executive & Judiciary"),
                    Triple(Icons.Default.Quiz, "Quizzes", "MCQ quizzes with timed questions, scoring & explanations"),
                    Triple(Icons.Default.Casino, "Spin Wheel", "Randomly pick a civic topic to study next"),
                    Triple(Icons.Default.BarChart, "Progress Tracking", "Track your quiz scores and flashcard progress"),
                    Triple(Icons.Default.Notifications, "Daily Reminders", "Set daily notifications to build a study habit")
                )

                features.forEach { (icon, title, desc) ->
                    Spacer(Modifier.height(AppSpacing.sm))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(AppSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElectricBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = ElectricBlue, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(AppSpacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.xl))

                // Content coverage
                Text(
                    "Content Coverage",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AppSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    listOf(
                        Triple("Legislature", "6 cards\n5 questions", LegislatureColor),
                        Triple("Executive", "6 cards\n5 questions", ExecutiveColor),
                        Triple("Judiciary", "6 cards\n5 questions", JudiciaryColor)
                    ).forEach { (name, stats, color) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(color.copy(alpha = 0.12f))
                                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(AppSpacing.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(name, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(4.dp))
                                Text(stats, style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.xl))

                // Disclaimer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GoldAccent.copy(alpha = 0.08f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(AppSpacing.md)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "This app is for educational purposes only. Content is based on the Indian Constitution and standard civics references. Always verify with official sources for legal matters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg))

                Text(
                    "Made with ❤️ in India",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    "© 2024 CivicIQ. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(AppSpacing.xxl))
            }
        }
    }
}
