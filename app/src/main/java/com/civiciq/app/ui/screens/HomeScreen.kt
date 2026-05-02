package com.civiciq.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.civiciq.app.data.local.DataStoreManager
import com.civiciq.app.navigation.Screen
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*
import kotlinx.coroutines.launch

// ==================== DATA ====================

private data class TabInfo(
    val title: String,
    val emoji: String,
    val color: Color,
    val categoryKey: String
)

private data class FeatureInfo(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val route: (String) -> String
)

private val tabs = listOf(
    TabInfo("Legislature", "🏛", LegislatureColor, "legislature"),
    TabInfo("Executive", "⚡", ExecutiveColor, "executive"),
    TabInfo("Judiciary", "⚖️", JudiciaryColor, "judiciary")
)

private val features = listOf(
    FeatureInfo(
        title = "Flashcards",
        subtitle = "Swipe to learn",
        icon = Icons.Filled.Style,
        gradient = listOf(Color(0xFF1A2980), Color(0xFF26D0CE)),
        route = { cat -> Screen.Flashcard.createRoute(cat) }
    ),
    FeatureInfo(
        title = "Quiz",
        subtitle = "Test your knowledge",
        icon = Icons.Filled.Quiz,
        gradient = listOf(Color(0xFF6A3DE8), Color(0xFFC850C0)),
        route = { cat -> Screen.Quiz.createRoute(cat) }
    ),
    FeatureInfo(
        title = "Spin Wheel",
        subtitle = "Random topics",
        icon = Icons.Filled.Casino,
        gradient = listOf(Color(0xFFFF8008), Color(0xFFFFC837)),
        route = { cat -> Screen.SpinWheel.createRoute(cat) }
    )
)

// ==================== MAIN SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTab by remember { derivedStateOf { pagerState.currentPage } }

    val totalQuizzes by dataStoreManager.totalQuizzes.collectAsStateWithLifecycle(0)
    val streak by dataStoreManager.currentStreak.collectAsStateWithLifecycle(0)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CivicDrawerContent(
                navController = navController,
                drawerState = drawerState,
                totalQuizzes = totalQuizzes,
                streak = streak
            )
        },
        scrimColor = Color(0x99000000)
    ) {
        Scaffold(
            topBar = {
                HomeTopAppBar(
                    selectedTab = tabs[selectedTab],
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onProgressClick = { navController.navigate(Screen.Progress.route) }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            GradientBackground(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Header stats strip
                    StatsStrip(
                        totalQuizzes = totalQuizzes,
                        streak = streak,
                        modifier = Modifier.padding(horizontal = AppSpacing.md)
                    )

                    Spacer(Modifier.height(AppSpacing.md))

                    // Tabs
                    TabSection(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )

                    Spacer(Modifier.height(AppSpacing.md))

                    // Pager Content
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        TabContent(
                            tab = tabs[page],
                            features = features,
                            navController = navController
                        )
                    }
                }
            }
            ConstitutionChatbot()
        }
    }
}

// ==================== TOP APP BAR ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    selectedTab: TabInfo,
    onMenuClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "CivicIQ",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = selectedTab.emoji + " " + selectedTab.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = selectedTab.color
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onProgressClick) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = "Progress",
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(NavyDeep, Color.Transparent)
            )
        )
    )
}

// ==================== STATS STRIP ====================

@Composable
private fun StatsStrip(
    totalQuizzes: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x20FFFFFF),
                        Color(0x10FFFFFF)
                    )
                )
            )
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(16.dp))
            .padding(AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniStat(value = totalQuizzes.toString(), label = "Quizzes", icon = Icons.Default.Quiz, color = ElectricBlue)
        VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0x30FFFFFF))
        MiniStat(value = "${streak}🔥", label = "Streak", icon = Icons.Default.LocalFireDepartment, color = SunsetOrange)
        VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0x30FFFFFF))
        MiniStat(value = "3", label = "Chapters", icon = Icons.Default.MenuBook, color = EmeraldGreen)
    }
}

@Composable
private fun MiniStat(value: String, label: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

// ==================== TAB SECTION ====================

@Composable
private fun TabSection(
    tabs: List<TabInfo>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        tabs.forEachIndexed { index, tab ->
            CustomTab(
                tab = tab,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CustomTab(
    tab: TabInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "tab_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextMuted,
        animationSpec = tween(300),
        label = "tab_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) tab.color.copy(alpha = bgAlpha)
                else Color(0x15FFFFFF)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color(0x20FFFFFF),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = tab.emoji, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = tab.title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== TAB CONTENT ====================

@Composable
private fun TabContent(
    tab: TabInfo,
    features: List<FeatureInfo>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        // Hero section
        HeroCard(tab = tab)

        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Study Modes",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            CategoryChip(text = tab.title, color = tab.color)
        }

        // Feature cards
        features.forEach { feature ->
            AnimatedFeatureCard(
                feature = feature,
                tab = tab,
                onClick = {
                    navController.navigate(feature.route(tab.categoryKey))
                }
            )
        }

        // Bottom tip card
        TipCard(tab = tab)

        Spacer(Modifier.height(AppSpacing.lg))
    }
}

// ==================== HERO CARD ====================

@Composable
private fun HeroCard(tab: TabInfo) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(tab.categoryKey) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -20 },
            animationSpec = tween(400)
        ) + fadeIn(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            tab.color.copy(alpha = 0.8f),
                            tab.color.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.dp, tab.color.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            // Background pattern
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 180.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(tab.color.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 220.dp, y = 80.dp)
                    .clip(CircleShape)
                    .background(tab.color.copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.lg)
            ) {
                Text(
                    text = tab.emoji,
                    fontSize = 36.sp
                )
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Learn • Test • Master",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ==================== ANIMATED FEATURE CARD ====================

@Composable
private fun AnimatedFeatureCard(
    feature: FeatureInfo,
    tab: TabInfo,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { pressed = false },
        label = "card_press"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(colors = feature.gradient)
            )
            .clickable {
                pressed = true
                onClick()
            }
    ) {
        // Background decoration
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp)
                .clip(CircleShape)
                .background(Color(0x20FFFFFF))
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 10.dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color(0x15FFFFFF))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x30FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(AppSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xCCFFFFFF)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x30FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==================== TIP CARD ====================

@Composable
private fun TipCard(tab: TabInfo) {
    val tips = mapOf(
        "legislature" to "💡 Tip: The Lok Sabha can be dissolved by the President on the advice of the PM.",
        "executive" to "💡 Tip: India follows parliamentary executive — President is nominal, PM is real head.",
        "judiciary" to "💡 Tip: Only the Supreme Court can enforce Fundamental Rights under Article 32."
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💡", fontSize = 18.sp)
            }
            Spacer(Modifier.width(AppSpacing.sm))
            Text(
                text = tips[tab.categoryKey] ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==================== DRAWER CONTENT ====================

@Composable
private fun CivicDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    totalQuizzes: Int,
    streak: Int
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        drawerContainerColor = SurfaceDefault,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Drawer Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF283593)
                            )
                        )
                    )
            ) {
                // Background circles
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = 200.dp, y = (-30).dp)
                        .clip(CircleShape)
                        .background(Color(0x20FFFFFF))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppSpacing.lg),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x30FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚖", fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text(
                        text = "CivicIQ",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$totalQuizzes quizzes • ${streak}🔥 streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.md))

            // Main Nav Items
            val navItems = listOf(
                Triple(Icons.Default.Home, "Home", Screen.Home.route),
                Triple(Icons.Default.BarChart, "Progress", Screen.Progress.route),
                Triple(Icons.Default.Settings, "Settings", Screen.Settings.route),
                Triple(Icons.Default.Info, "About", Screen.About.route)
            )

            Text(
                text = "NAVIGATION",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            )

            navItems.forEach { (icon, label, route) ->
                val isCurrentRoute = navController.currentDestination?.route == route
                NavigationDrawerItem(
                    label = {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrentRoute) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = isCurrentRoute,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (route != navController.currentDestination?.route) {
                            navController.navigate(route) {
                                if (route != Screen.Home.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    icon = { Icon(icon, contentDescription = label) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = ElectricBlue.copy(alpha = 0.15f),
                        selectedIconColor = ElectricBlue,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.padding(horizontal = AppSpacing.sm)
                )
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Divider(color = NavyBorder, modifier = Modifier.padding(horizontal = AppSpacing.md))
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                text = "CivicIQ v1.0 • Indian Constitution",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.lg),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(AppSpacing.md))
        }
    }
}
