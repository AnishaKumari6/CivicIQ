package com.civiciq.app.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.civiciq.app.data.local.ContentRepository
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinWheelScreen(
    category: String,
    repository: ContentRepository,
    onBack: () -> Unit
) {
    var topics by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSpinning by remember { mutableStateOf(false) }
    var targetAngle by remember { mutableFloatStateOf(0f) }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }

    val categoryColor = when (category) {
        "legislature" -> LegislatureColor
        "executive"   -> ExecutiveColor
        "judiciary"   -> JudiciaryColor
        else          -> ElectricBlue
    }

    LaunchedEffect(category) {
        repository.getSpinTopics(category).onSuccess { t ->
            topics = t
            isLoading = false
        }
    }

    // Smooth deceleration spin animation
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(
            durationMillis = 4500,
            easing = CubicBezierEasing(0.15f, 0.85f, 0.25f, 1.0f)
        ),
        finishedListener = {
            if (isSpinning) {
                isSpinning = false
                val normalised = ((it % 360f) + 360f) % 360f
                val segmentAngle = if (topics.isNotEmpty()) 360f / topics.size else 360f
                // Pointer is at top (270°), calculate which segment is there
                val pointerPos = (270f - normalised + 360f) % 360f
                val idx = (pointerPos / segmentAngle).toInt().coerceIn(0, topics.size - 1)
                selectedTopic = topics.getOrNull(idx)
                showResult = true
            }
        },
        label = "spin_angle"
    )

    fun spin() {
        if (!isSpinning && topics.isNotEmpty()) {
            isSpinning = true
            showResult  = false
            val spins       = (6..10).random() * 360f
            val randomExtra = (0..359).random().toFloat()
            targetAngle = animatedAngle + spins + randomExtra
        }
    }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            CivicTopAppBar(
                title = "Spin the Wheel",
                onNavigationClick = onBack,
                navigationIcon = Icons.Default.ArrowBack
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = categoryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(AppSpacing.sm))

                    CategoryChip(
                        text = "📚 ${category.replaceFirstChar { it.uppercase() }}",
                        color = categoryColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Spin to pick a random topic!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(AppSpacing.lg))

                    // ── Wheel + pointer ──────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(320.dp)
                    ) {
                        // Outer glow ring
                        Box(
                            modifier = Modifier
                                .size(316.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            categoryColor.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        if (topics.isNotEmpty()) {
                            WheelCanvas(
                                topics      = topics,
                                rotation    = animatedAngle,
                                modifier    = Modifier.size(290.dp)
                            )
                        }

                        // Centre hub
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF3A4480), NavyDeep)
                                    )
                                )
                                .border(3.dp, categoryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 20.sp)
                        }

                        // ── Pointer arrow at TOP ──
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 2.dp)
                                .size(28.dp, 36.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                // Drop shadow
                                drawPath(
                                    path = Path().apply {
                                        moveTo(w / 2f, h - 2f)
                                        lineTo(2f, 2f)
                                        lineTo(w - 2f, 2f)
                                        close()
                                    },
                                    color = Color.Black.copy(alpha = 0.4f)
                                )
                                // White arrow
                                drawPath(
                                    path = Path().apply {
                                        moveTo(w / 2f, h)
                                        lineTo(0f, 0f)
                                        lineTo(w, 0f)
                                        close()
                                    },
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(AppSpacing.xl))

                    // ── Spin button ──────────────────────────────────
                    Button(
                        onClick  = { spin() },
                        enabled  = !isSpinning,
                        modifier = Modifier
                            .width(220.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = categoryColor,
                            disabledContainerColor = categoryColor.copy(alpha = 0.45f)
                        ),
                        shape     = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                    ) {
                        if (isSpinning) {
                            CircularProgressIndicator(
                                color      = Color.White,
                                modifier   = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Spinning…", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Text("🎯", fontSize = 22.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("SPIN!", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }

                    Spacer(Modifier.height(AppSpacing.xl))

                    // ── Topics pill list ────────────────────────────
                    Text(
                        "Topics on the wheel",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = TextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(AppSpacing.sm))
                    TopicsPills(topics = topics, categoryColor = categoryColor)

                    Spacer(Modifier.height(AppSpacing.xxl))
                }
            }
        }
    }

    // ── Result bottom sheet ──────────────────────────────────────────
    if (showResult && selectedTopic != null) {
        SpinResultSheet(
            topic         = selectedTopic!!,
            categoryColor = categoryColor,
            onDismiss     = { showResult = false },
            onSpin        = { showResult = false; spin() }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  WHEEL CANVAS  — clearly readable text on every segment
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun WheelCanvas(
    topics   : List<String>,
    rotation : Float,
    modifier : Modifier = Modifier
) {
    val textMeasurer  = rememberTextMeasurer()
    val segCount      = topics.size
    val segAngleDeg   = 360f / segCount

    Canvas(modifier = modifier) {
        val cx     = size.width  / 2f
        val cy     = size.height / 2f
        val radius = size.minDimension / 2f

        rotate(degrees = rotation, pivot = Offset(cx, cy)) {

            topics.forEachIndexed { i, topic ->
                val startDeg = i * segAngleDeg
                val midDeg   = startDeg + segAngleDeg / 2f
                val baseColor = WheelColors[i % WheelColors.size]

                // ── 1. Filled arc ──────────────────────────────────
                drawArc(
                    color      = baseColor,
                    startAngle = startDeg,
                    sweepAngle = segAngleDeg,
                    useCenter  = true,
                    topLeft    = Offset(cx - radius, cy - radius),
                    size       = Size(radius * 2, radius * 2)
                )

                // ── 2. Lighter radial highlight (inner half) ───────
                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = radius * 0.75f
                    ),
                    startAngle = startDeg,
                    sweepAngle = segAngleDeg,
                    useCenter  = true,
                    topLeft    = Offset(cx - radius, cy - radius),
                    size       = Size(radius * 2, radius * 2)
                )

                // ── 3. Divider line ────────────────────────────────
                val lineRad = startDeg * PI.toFloat() / 180f
                drawLine(
                    color       = Color.White.copy(alpha = 0.55f),
                    start       = Offset(cx, cy),
                    end         = Offset(
                        cx + radius * cos(lineRad),
                        cy + radius * sin(lineRad)
                    ),
                    strokeWidth = 2.5f
                )

                // ── 4. Text — ROTATED along radius ────────────────
                //    Place at 68 % of radius so it's centred in segment
                val midRad   = midDeg * PI.toFloat() / 180f
                val textDist = radius * 0.62f
                val textX    = cx + textDist * cos(midRad)
                val textY    = cy + textDist * sin(midRad)

                // Shorten if too long
                val label = if (topic.length > 10) topic.take(9) + "…" else topic

                val measured = textMeasurer.measure(
                    text  = label,
                    style = TextStyle(
                        color      = Color.White,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Rotate canvas so text reads outward from centre
                withTransform({
                    rotate(
                        degrees = midDeg + 90f,   // +90 so text is perpendicular to radius
                        pivot   = Offset(textX, textY)
                    )
                }) {
                    // Dark pill background — CRITICAL for readability
                    drawRoundRect(
                        color       = Color.Black.copy(alpha = 0.45f),
                        topLeft     = Offset(
                            textX - measured.size.width  / 2f - 6f,
                            textY - measured.size.height / 2f - 3f
                        ),
                        size        = Size(
                            measured.size.width  + 12f,
                            measured.size.height + 6f
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )

                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            textX - measured.size.width  / 2f,
                            textY - measured.size.height / 2f
                        )
                    )
                }
            }

            // ── 5. Outer border ring ───────────────────────────────
            drawCircle(
                color  = Color.White.copy(alpha = 0.35f),
                radius = radius,
                style  = Stroke(width = 3.5f)
            )

            // ── 6. Inner dark hub mask (so text doesn't overlap) ───
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NavyDeep, NavyDeep, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius * 0.15f
                ),
                radius = radius * 0.15f
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  TOPICS PILLS
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun TopicsPills(topics: List<String>, categoryColor: Color) {
    val rows = topics.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { topic ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(categoryColor.copy(alpha = 0.12f))
                            .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text  = topic,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  RESULT BOTTOM SHEET
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpinResultSheet(
    topic         : String,
    categoryColor : Color,
    onDismiss     : () -> Unit,
    onSpin        : () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceDefault,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier             = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            // Confetti emoji
            Text("🎯", fontSize = 60.sp)
            Spacer(Modifier.height(AppSpacing.md))

            Text(
                "Your topic is:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(AppSpacing.sm))

            // Highlighted topic box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                categoryColor.copy(alpha = 0.35f),
                                categoryColor.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(1.5.dp, categoryColor, RoundedCornerShape(18.dp))
                    .padding(vertical = AppSpacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = topic,
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = Color.White,
                    fontWeight = FontWeight.Black,
                    textAlign  = TextAlign.Center
                )
            }

            Spacer(Modifier.height(AppSpacing.md))

            Text(
                "Open Flashcards or Quiz from Home to study this topic!",
                style     = MaterialTheme.typography.bodySmall,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(AppSpacing.xl))

            Row(
                modifier                = Modifier.fillMaxWidth(),
                horizontalArrangement   = Arrangement.spacedBy(AppSpacing.md)
            ) {
                OutlinedButton(
                    onClick   = onDismiss,
                    modifier  = Modifier.weight(1f).height(52.dp),
                    shape     = RoundedCornerShape(14.dp),
                    border    = BorderStroke(1.dp, categoryColor.copy(0.5f))
                ) {
                    Text("Close", color = categoryColor, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick  = onSpin,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = categoryColor),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Spin Again", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(AppSpacing.xl))
        }
    }
}
