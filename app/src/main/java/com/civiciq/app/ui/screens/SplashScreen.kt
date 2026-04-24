package com.civiciq.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civiciq.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {

    var visible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(400)
        subtitleVisible = true
        delay(300)
        taglineVisible = true
        delay(1500)
        onNavigateToHome()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A2D6E),
                        NavyDeep,
                        Color(0xFF050A1C)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(400.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PurpleAccent.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(tween(600))
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricBlue, PurpleAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner decorative shape
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x30FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚖",
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(600, delayMillis = 200)
                ) + fadeIn(tween(600, delayMillis = 200))
            ) {
                Text(
                    text = "CivicIQ",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            AnimatedVisibility(
                visible = subtitleVisible,
                enter = slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(500)
                ) + fadeIn(tween(500))
            ) {
                Text(
                    text = "KNOW YOUR CONSTITUTION",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricBlue,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Loading dots
            AnimatedVisibility(
                visible = taglineVisible,
                enter = fadeIn(tween(400))
            ) {
                LoadingDots()
            }
        }

        // Version text at bottom
        AnimatedVisibility(
            visible = taglineVisible,
            enter = fadeIn(tween(600)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "v1.0 • Made with ❤️ in India",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue.copy(alpha = alpha))
            )
        }
    }
}
