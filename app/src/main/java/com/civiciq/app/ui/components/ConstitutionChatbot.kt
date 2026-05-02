package com.civiciq.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

// --- DATA MODELS ---
data class ChatMessage(val text: String, val isUser: Boolean)
data class FAQEntry(val answer: String, val followUps: List<String> = emptyList())

// --- THEME PALETTE ---
val GoldSaffron = Color(0xFFFFAB40)
val DeepSaffron = Color(0xFFFF9100)
val TirangaGreen = Color(0xFF138808)
val ChakraBlue = Color(0xFF000080)
val GlassSurface = Color(0xFF1A1A2E)
val AiSpark = Color(0xFF00E5FF)

@Composable
fun ConstitutionChatbot() {
    var isChatOpen by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }
    var isActiveInteraction by remember { mutableStateOf(false) } // True if hovering OR pressing

    val welcomeMessage = "Namaste! I am your CivicIQ Guide. Select a topic to explore the Indian Constitution."
    var chatHistory by remember { mutableStateOf(listOf(ChatMessage(welcomeMessage, false))) }

    val initialQuestions = listOf(
        "What is the Preamble?", "What are Fundamental Rights?", "What are Directive Principles?",
        "How is the President elected?", "What are Emergency Provisions?", "How can the Constitution be amended?",
        "What is the Role of the Judiciary?", "What is the Parliament structure?", "What are Fundamental Duties?",
        "Who is the Father of the Constitution?"
    )

    var currentSuggestions by remember { mutableStateOf(initialQuestions) }
    val scrollState = rememberLazyListState()
    val knowledgeBase = getKnowledgeBase()

    // TOOLTIP TIMER: Only runs if user isn't touching/hovering
    LaunchedEffect(isChatOpen, isActiveInteraction) {
        if (!isChatOpen && !isActiveInteraction) {
            while(true) {
                delay(3000)
                showTooltip = true
                delay(5000)
                showTooltip = false
                delay(10000)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) scrollState.animateScrollToItem(chatHistory.size - 1)
    }

    Box(modifier = Modifier.fillMaxSize().zIndex(999f)) {

        if (!isChatOpen) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Tooltip logic: show if timer says so OR user is hovering/pressing
                AnimatedVisibility(
                    visible = showTooltip || isActiveInteraction,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Surface(
                        modifier = Modifier.padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                        color = ChakraBlue,
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            "Your Assistant is here ✨",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .scale(fabScale)
                        .size(68.dp)
                        .background(Brush.linearGradient(listOf(GoldSaffron, DeepSaffron)), CircleShape)
                        .border(2.dp, Color.White.copy(0.4f), CircleShape)
                        // 1. Change cursor for mouse users
                        .pointerHoverIcon(PointerIcon.Hand)
                        // 2. Detect BOTH Hover (Mouse) and Press (Touch)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Enter, PointerEventType.Press -> isActiveInteraction = true
                                        PointerEventType.Exit, PointerEventType.Release -> isActiveInteraction = false
                                    }
                                }
                            }
                        }
                        .clickable { isChatOpen = true; isActiveInteraction = false; showTooltip = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 18.dp).size(10.dp).background(AiSpark, CircleShape).border(1.5.dp, Color.White, CircleShape))
                }
            }
        }

        // --- CHAT WINDOW ---
        AnimatedVisibility(
            visible = isChatOpen,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            val windowModifier = if (isFullScreen) Modifier.fillMaxSize()
            else Modifier.padding(16.dp).fillMaxWidth(0.94f).fillMaxHeight(0.85f)

            ElevatedCard(
                modifier = windowModifier,
                shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = GlassSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ImprovedHeader(isFullScreen, { isFullScreen = !isFullScreen }, { isChatOpen = false }) {
                        chatHistory = listOf(ChatMessage(welcomeMessage, false))
                        currentSuggestions = initialQuestions
                    }

                    Box(modifier = Modifier.weight(1f).background(Brush.verticalGradient(listOf(GlassSurface, Color(0xFF0F0F1E))))) {
                        LazyColumn(state = scrollState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(chatHistory) { msg -> ImprovedChatBubble(msg) }
                        }
                    }

                    ImprovedSuggestionFooter(currentSuggestions, initialQuestions, isFullScreen, { currentSuggestions = initialQuestions }) { clicked ->
                        chatHistory = chatHistory + ChatMessage(clicked, true)
                        val entry = knowledgeBase[clicked]
                        currentSuggestions = currentSuggestions.filter { it != clicked }
                        if (entry != null) {
                            chatHistory = chatHistory + ChatMessage(entry.answer, false)
                            currentSuggestions = (entry.followUps + currentSuggestions).distinct()
                        }
                    }
                }
            }
        }
    }
}

// --- SUPPORTING COMPONENTS (UNCHANGED) ---

@Composable
fun ImprovedHeader(isFullScreen: Boolean, onToggleExpand: () -> Unit, onClose: () -> Unit, onReset: () -> Unit) {
    val topPadding = if (isFullScreen) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp
    Surface(color = GoldSaffron, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(top = topPadding, start = 8.dp, end = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(0.2f), modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Gavel, null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("CivicIQ Guide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onReset) { Icon(Icons.Default.DeleteSweep, null, tint = Color.White) }
            IconButton(onClick = onToggleExpand) { Icon(if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.OpenInFull, null, tint = Color.White) }
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
    }
}

@Composable
fun ImprovedChatBubble(message: ChatMessage) {
    val align = if (message.isUser) Alignment.End else Alignment.Start
    val bg = if (message.isUser) GoldSaffron else TirangaGreen.copy(0.15f)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Surface(color = bg, shape = RoundedCornerShape(16.dp), modifier = Modifier.widthIn(max = 280.dp).border(1.dp, if (message.isUser) Color.Transparent else TirangaGreen.copy(0.3f), RoundedCornerShape(16.dp))) {
            Text(message.text, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun ImprovedSuggestionFooter(suggestions: List<String>, initialQuestions: List<String>, isFullScreen: Boolean, onReset: () -> Unit, onClick: (String) -> Unit) {
    var dragHeight by remember { mutableStateOf(220.dp) }
    val density = LocalDensity.current

    Column(modifier = Modifier.background(Color(0xFF0F0F1E)).animateContentSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(24.dp).background(Color(0xFF1A1A2E)).pointerInput(Unit) {
            detectVerticalDragGestures { change, dragAmount ->
                change.consume()
                dragHeight = (dragHeight - with(density) { dragAmount.toDp() }).coerceIn(120.dp, 500.dp)
            }
        }.pointerHoverIcon(PointerIcon.Hand), contentAlignment = Alignment.Center) {
            Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.Gray.copy(0.4f)))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("EXPLORE TOPICS", style = MaterialTheme.typography.labelSmall, color = GoldSaffron, fontWeight = FontWeight.ExtraBold)
                Text("RESET TOPICS", modifier = Modifier.clickable { onReset() }.pointerHoverIcon(PointerIcon.Hand), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.heightIn(max = dragHeight), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestions) { q ->
                    val isFollowUp = !initialQuestions.contains(q)
                    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick(q) }.pointerHoverIcon(PointerIcon.Hand), shape = RoundedCornerShape(12.dp), color = if (isFollowUp) ChakraBlue.copy(0.3f) else Color.White.copy(0.05f), border = androidx.compose.foundation.BorderStroke(1.dp, if (isFollowUp) ChakraBlue else TirangaGreen.copy(0.3f))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isFollowUp) Icons.Default.AutoAwesome else Icons.Default.ArrowForwardIos, null, tint = if (isFollowUp) GoldSaffron else TirangaGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(q, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

fun getKnowledgeBase() = mapOf(
    "What is the Preamble?" to FAQEntry("The Preamble is the soul of the Constitution, declaring India a Sovereign, Socialist, Secular, Democratic Republic.", listOf("What does 'Secular' mean?", "What does 'Sovereign' mean?")),
    "What does 'Secular' mean?" to FAQEntry("It means the State has no official religion and treats all faiths equally."),
    "What does 'Sovereign' mean?" to FAQEntry("It means India is an independent nation, free from any external control."),
    "What are Fundamental Rights?" to FAQEntry("Part III (Art 12-35) protects citizens from arbitrary state power.", listOf("How many Fundamental Rights?", "Can these rights be suspended?")),
    "How many Fundamental Rights?" to FAQEntry("There are 6: Equality, Freedom, Against Exploitation, Religion, Cultural & Educational, and Constitutional Remedies."),
    "Can these rights be suspended?" to FAQEntry("Yes, during a National Emergency, except for Articles 20 and 21."),
    "What are Directive Principles?" to FAQEntry("Part IV (DPSP) are guidelines for the State to ensure social and economic justice.", listOf("Are DPSP justiciable?", "Examples of DPSP?")),
    "Are DPSP justiciable?" to FAQEntry("No, they are not enforceable by courts, but are fundamental to the governance of India."),
    "Examples of DPSP?" to FAQEntry("Uniform Civil Code, promotion of village panchayats, and equal pay for equal work."),
    "How is the President elected?" to FAQEntry("Elected indirectly by an electoral college of elected members of Parliament and State Assemblies.", listOf("What is the term of the President?", "Can the President be impeached?")),
    "What is the term of the President?" to FAQEntry("The President serves a 5-year term."),
    "Can the President be impeached?" to FAQEntry("Yes, under Article 61 for 'violation of the Constitution'."),
    "What are Emergency Provisions?" to FAQEntry("Part XVIII allows the President to declare emergencies during crises.", listOf("Who declares an emergency?", "Types of emergencies?")),
    "Who declares an emergency?" to FAQEntry("The President, on the written advice of the Union Cabinet."),
    "Types of emergencies?" to FAQEntry("National (Art 352), State (Art 356), and Financial (Art 360)."),
    "How can the Constitution be amended?" to FAQEntry("Under Article 368, provided it doesn't alter the 'Basic Structure'.", listOf("What is the Basic Structure?", "Can Parliament amend anything?")),
    "What is the Basic Structure?" to FAQEntry("Core features like Secularism, Federalism, and Democracy that cannot be removed."),
    "Can Parliament amend anything?" to FAQEntry("No, the Judiciary can strike down amendments that violate the Basic Structure."),
    "What is the Role of the Judiciary?" to FAQEntry("The protector and final interpreter of the Constitution.", listOf("What is Judicial Review?", "How are judges appointed?")),
    "What is Judicial Review?" to FAQEntry("The power of the courts to invalidate laws that are unconstitutional."),
    "How are judges appointed?" to FAQEntry("Via the Collegium System of senior judges."),
    "What is the Parliament structure?" to FAQEntry("Bicameral: Lok Sabha (House of People) and Rajya Sabha (Council of States).", listOf("Difference between Lok Sabha & Rajya Sabha?", "How is a bill passed?")),
    "Difference between Lok Sabha & Rajya Sabha?" to FAQEntry("Lok Sabha is directly elected; Rajya Sabha represents the states."),
    "How is a bill passed?" to FAQEntry("A bill must be passed by both houses and receive Presidential assent."),
    "What are Fundamental Duties?" to FAQEntry("Part IV-A (Art 51A) lists 11 moral obligations of citizens.", listOf("Are duties mandatory?", "When were they added?")),
    "Are duties mandatory?" to FAQEntry("They are not legally enforceable but are moral imperatives for all citizens."),
    "When were they added?" to FAQEntry("Added by the 42nd Amendment in 1976."),
    "Who is the Father of the Constitution?" to FAQEntry("Dr. B.R. Ambedkar, Chairman of the Drafting Committee.", listOf("What was the Drafting Committee?", "When was the Constitution adopted?")),
    "What was the Drafting Committee?" to FAQEntry("A committee set up in 1947 to prepare the draft of the new Constitution."),
    "When was the Constitution adopted?" to FAQEntry("Adopted on Nov 26, 1949; came into force on Jan 26, 1950.")
)