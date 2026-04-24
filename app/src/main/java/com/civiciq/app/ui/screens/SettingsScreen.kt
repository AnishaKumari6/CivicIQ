package com.civiciq.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiciq.app.data.local.DataStoreManager
import com.civiciq.app.ui.components.*
import com.civiciq.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    dataStoreManager: DataStoreManager,
    onBack: () -> Unit
) {
    val isDailyReminder by dataStoreManager.isDailyReminderEnabled.collectAsStateWithLifecycle(false)
    val preferredCategory by dataStoreManager.preferredCategory.collectAsStateWithLifecycle("Legislature")
    val notificationHour by dataStoreManager.notificationHour.collectAsStateWithLifecycle(9)

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTimeDropdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val categories = listOf("Legislature", "Executive", "Judiciary")
    val hours = (6..22).map { h ->
        val amPm = if (h < 12) "AM" else "PM"
        val hour12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        "$hour12:00 $amPm" to h
    }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CivicTopAppBar(
                title = "Settings",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.md)
            ) {
                Spacer(Modifier.height(AppSpacing.sm))

                // NOTIFICATIONS SECTION
                SettingsSectionHeader(title = "Notifications", icon = Icons.Default.Notifications)
                Spacer(Modifier.height(AppSpacing.sm))

                SettingsToggleItem(
                    icon = Icons.Default.NotificationsActive,
                    iconColor = ElectricBlue,
                    title = "Daily Reminder",
                    subtitle = "Get a daily nudge to study civic topics",
                    isChecked = isDailyReminder,
                    onCheckedChange = { enabled ->
                        kotlinx.coroutines.GlobalScope.launch {
                            dataStoreManager.setDailyReminder(enabled)
                        }
                    }
                )

                AnimatedVisibility(
                    visible = isDailyReminder,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(Modifier.height(AppSpacing.sm))
                        SettingsDropdownItem(
                            icon = Icons.Default.Schedule,
                            iconColor = PurpleAccent,
                            title = "Reminder Time",
                            subtitle = "When to send daily notification",
                            value = hours.find { it.second == notificationHour }?.first ?: "9:00 AM",
                            expanded = showTimeDropdown,
                            onExpandToggle = { showTimeDropdown = !showTimeDropdown },
                            dropdownContent = {
                                hours.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (value == notificationHour) ElectricBlue else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            kotlinx.coroutines.GlobalScope.launch {
                                                dataStoreManager.setNotificationHour(value)
                                            }
                                            showTimeDropdown = false
                                        },
                                        leadingIcon = if (value == notificationHour) {
                                            { Icon(Icons.Default.Check, null, tint = ElectricBlue, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg))

                // STUDY PREFERENCES
                SettingsSectionHeader(title = "Study Preferences", icon = Icons.Default.MenuBook)
                Spacer(Modifier.height(AppSpacing.sm))

                SettingsDropdownItem(
                    icon = Icons.Default.Category,
                    iconColor = EmeraldGreen,
                    title = "Default Category",
                    subtitle = "Opens this category on app launch",
                    value = preferredCategory,
                    expanded = showCategoryDropdown,
                    onExpandToggle = { showCategoryDropdown = !showCategoryDropdown },
                    dropdownContent = {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (category == preferredCategory) ElectricBlue else TextPrimary
                                    )
                                },
                                onClick = {
                                    kotlinx.coroutines.GlobalScope.launch {
                                        dataStoreManager.setPreferredCategory(category)
                                    }
                                    showCategoryDropdown = false
                                },
                                leadingIcon = if (category == preferredCategory) {
                                    { Icon(Icons.Default.Check, null, tint = ElectricBlue, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                )

                Spacer(Modifier.height(AppSpacing.lg))

                // APP SECTION
                SettingsSectionHeader(title = "App", icon = Icons.Default.PhoneAndroid)
                Spacer(Modifier.height(AppSpacing.sm))

                SettingsNavItem(
                    icon = Icons.Default.DeleteSweep,
                    iconColor = ErrorColor,
                    title = "Clear All Data",
                    subtitle = "Reset quiz history and progress",
                    onClick = {
                        kotlinx.coroutines.GlobalScope.launch {
                            dataStoreManager.clearAllData()
                        }
                    }
                )

                Spacer(Modifier.height(AppSpacing.sm))

                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    iconColor = GoldAccent,
                    title = "App Version",
                    value = "1.0.0"
                )

                Spacer(Modifier.height(AppSpacing.xxl))
            }
        }
    }
}

// ==================== SETTINGS COMPONENTS ====================

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = AppSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = ElectricBlue, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ElectricBlue,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconColor,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = NavyBorder
                )
            )
        }
    }
}

@Composable
private fun SettingsDropdownItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    value: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    dropdownContent: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onExpandToggle
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
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(AppSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(value, style = MaterialTheme.typography.labelSmall, color = iconColor, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDeep.copy(alpha = 0.5f))
                ) {
                    HorizontalDivider(color = NavyBorder)
                    dropdownContent()
                }
            }
        }
    }
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = iconColor, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(AppSpacing.md))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
