package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Alarm
import com.example.data.Reminder
import com.example.ui.ClockViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CampClockDashboard(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CampClockDashboard(
    modifier: Modifier = Modifier,
    clockViewModel: ClockViewModel = viewModel()
) {
    val currentTime by clockViewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val alarms by clockViewModel.alarms.collectAsStateWithLifecycle()
    val reminders by clockViewModel.reminders.collectAsStateWithLifecycle()
    val activeAlarm by clockViewModel.activeRingingAlarm.collectAsStateWithLifecycle()

    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    val formattedTime = clockViewModel.getFormattedTime(currentTime)
    val formattedDate = clockViewModel.getFormattedDate(currentTime)
    val campQuote = clockViewModel.getCampQuote(currentTime)

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = if (MaterialTheme.colorScheme.background == Color(0xFF102115)) {
                        listOf(Color(0xFF0F2617), Color(0xFF0C1910), Color(0xFF050D08))
                    } else {
                        listOf(Color(0xFFFDFCF5), Color(0xFFF5F1E2))
                    }
                )
            )
    ) {
        // Main Scrollable Area containing all widgets in a single screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Main Header Badge in Artistic Style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Camp Buddy 🏕️",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary, // #4C7C3C
                        modifier = Modifier.testTag("app_title"),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "ADVENTURE AWAITS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C7A5B), // #8C7A5B
                        letterSpacing = 1.2.sp
                    )
                }

                // Decorative orange scout badge element with forest icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE67E22)) // #E67E22
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forest,
                        contentDescription = "Forest icon",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Real-Time Camp Buddy Dynamic speech bubble Widget
            CampBuddyBubbleCard(
                character = campQuote.character,
                message = campQuote.message,
                avatarColor = Color(campQuote.avatarColorHex)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Digital Log Clock Widget in Artistic Theme styling
            LogDigitalClock(
                timeString = formattedTime,
                dateString = formattedDate
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Alarms
            WidgetHeaderRow(
                title = "Alarm Perkemahan",
                icon = Icons.Default.Alarm,
                actionLabel = "Tambah",
                actionTag = "add_alarm_button",
                onActionClicked = { showAddAlarmDialog = true }
            )

            if (alarms.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE6E2D3),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Snooze",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tidak ada alarm terpasang.\nTidur nyenyak di kantong tidurmu!",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    alarms.forEach { alarm ->
                        AlarmRowItem(
                            alarm = alarm,
                            onToggle = { clockViewModel.toggleAlarm(alarm) },
                            onDelete = { clockViewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }

            // Section Reminders / Camp Quests
            WidgetHeaderRow(
                title = "Misi & Kegiatan Harian",
                icon = Icons.Default.CheckCircle,
                actionLabel = "Misi Baru",
                actionTag = "add_reminder_button",
                onActionClicked = { showAddReminderDialog = true }
            )

            if (reminders.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE6E2D3),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Task,
                                contentDescription = "Bebas tugas",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Semua kegiatan selesai!\nAyo rileks di pondokan.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    reminders.forEach { reminder ->
                        ReminderItemRow(
                            reminder = reminder,
                            onCheckedChange = { clockViewModel.toggleReminderCompletion(reminder) },
                            onDelete = { clockViewModel.deleteReminder(reminder) }
                        )
                    }

                    if (reminders.any { it.isCompleted }) {
                        TextButton(
                            onClick = { clockViewModel.clearCompletedReminders() },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp)
                                .testTag("clear_completed_button")
                        ) {
                            Text(
                                "Bersihkan Kegiatan Selesai 🧹",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Ringing Alarm Intercepting Alert overlay
        if (activeAlarm != null) {
            AlarmActiveOverlay(
                alarm = activeAlarm!!,
                formattedTime = formattedTime,
                onDismiss = { clockViewModel.dismissAlarm() },
                onSnooze = { clockViewModel.snoozeAlarm() }
            )
        }

        // Add Alarm Form Dialog
        if (showAddAlarmDialog) {
            AddAlarmDialog(
                onDismiss = { showAddAlarmDialog = false },
                onSave = { h, m, label, isDaily ->
                    clockViewModel.addAlarm(h, m, label, isDaily)
                    showAddAlarmDialog = false
                }
            )
        }

        // Add Reminder Quest Form Dialog
        if (showAddReminderDialog) {
            AddReminderDialog(
                onDismiss = { showAddReminderDialog = false },
                onSave = { title, character, tm, note, cat ->
                    clockViewModel.addReminder(title, character, tm, note, cat)
                    showAddReminderDialog = false
                }
            )
        }
    }
}

@Composable
fun LogDigitalClock(
    timeString: String,
    dateString: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF102115)
    
    // Artistic design theme colors for the container
    val containerBg = if (isDark) {
        Color(0xFF192C1D)
    } else {
        Color(0xFF4C7C3C).copy(alpha = 0.05f)
    }
    val borderColor = if (isDark) {
        Color(0xFF4C7C3C).copy(alpha = 0.25f)
    } else {
        Color(0xFF4C7C3C).copy(alpha = 0.12f)
    }
    
    val timeColor = if (isDark) {
        Color(0xFFFAB123)
    } else {
        Color(0xFF2D4522) // ArtisticDeepGreen
    }
    
    val dateColor = if (isDark) {
        Color(0xFFEDE4D5)
    } else {
        Color(0xFF4C7C3C) // ArtisticGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.3.dp,
                color = borderColor,
                shape = RoundedCornerShape(40.dp)
            ),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campfire canvas vector drawing above the clock
            CampfireDrawing(modifier = Modifier.size(64.dp))

            Spacer(modifier = Modifier.height(14.dp))

            // Beautiful tabular mono LCD display
            Text(
                text = timeString,
                fontSize = 58.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = timeColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_clock_display"),
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Local Date
            Text(
                text = dateString,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = dateColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Pill-shaped badge under the clock for "Misi Aktif" status
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF4C7C3C))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Campaign Logo",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MISI AKTIF • BONFIRE HOUR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CampfireDrawing(modifier: Modifier = Modifier) {
    // A cute dynamic drawing of fire log & flames
    val infiniteTransition = rememberInfiniteTransition(label = "campfire_flame")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw logs (crossing brown logs)
        // Log 1
        drawRect(
            color = Color(0xFF5C3317),
            topLeft = Offset(w * 0.15f, h * 0.75f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.12f)
        )
        // Log 2 (angled shadow)
        drawRect(
            color = Color(0xFF4A2A14),
            topLeft = Offset(w * 0.3f, h * 0.65f),
            size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.12f)
        )

        // 2. Draw campfire stones (little grey circles at bottom)
        drawCircle(color = Color.Gray, radius = w * 0.08f, center = Offset(w * 0.2f, h * 0.85f))
        drawCircle(color = Color.DarkGray, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.88f))
        drawCircle(color = Color.Gray, radius = w * 0.08f, center = Offset(w * 0.8f, h * 0.85f))

        // 3. Draw multiple burning flames layers
        val outerFlame = Path().apply {
            moveTo(w * 0.5f, h * 0.15f * flamePulse)
            cubicTo(
                w * 0.25f, h * 0.45f,
                w * 0.2f, h * 0.75f,
                w * 0.5f, h * 0.75f
            )
            cubicTo(
                w * 0.8f, h * 0.75f,
                w * 0.75f, h * 0.45f,
                w * 0.5f, h * 0.15f * flamePulse
            )
            close()
        }
        drawPath(path = outerFlame, color = Color(0xFFE65C00)) // Bonfire Orange

        val innerFlame = Path().apply {
            moveTo(w * 0.5f, h * 0.3f * flamePulse)
            cubicTo(
                w * 0.35f, h * 0.5f,
                w * 0.32f, h * 0.72f,
                w * 0.5f, h * 0.72f
            )
            cubicTo(
                w * 0.68f, h * 0.72f,
                w * 0.65f, h * 0.5f,
                w * 0.5f, h * 0.3f * flamePulse
            )
            close()
        }
        drawPath(path = innerFlame, color = Color(0xFFFAB123)) // Golden Core

        // Small ember spark
        drawCircle(
            color = Color(0xFFFFEB3B),
            radius = w * 0.04f * flamePulse,
            center = Offset(w * 0.48f, h * 0.45f)
        )
    }
}

@Composable
fun CampBuddyBubbleCard(
    character: String,
    message: String,
    avatarColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                width = 1.5.dp,
                color = avatarColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character Avatar Stamp
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = character.firstOrNull()?.toString() ?: "C",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$character 🏕️",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = avatarColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun WidgetHeaderRow(
    title: String,
    icon: ImageVector,
    actionLabel: String,
    actionTag: String,
    onActionClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Button(
            onClick = onActionClicked,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag(actionTag)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = actionLabel,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AlarmRowItem(
    alarm: Alarm,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE6E2D3), // ArtisticBorder
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("alarm_item_${alarm.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle orange status indicator badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE67E22).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (alarm.isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Alarm status",
                        tint = Color(0xFFE67E22),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    val formattedHour = String.format("%02d", alarm.hour)
                    val formattedMinute = String.format("%02d", alarm.minute)
                    Text(
                        text = "$formattedHour:$formattedMinute",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = alarm.label,
                        fontSize = 13.sp,
                        color = Color(0xFF8C7A5B), // #8C7A5B
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier
                        .scale(0.85f)
                        .testTag("alarm_switch_${alarm.id}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("alarm_delete_${alarm.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus alarm",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun ReminderItemRow(
    reminder: Reminder,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (reminder.category) {
        "Inspection" -> Color(0xFFF57C00)
        "Cooking" -> Color(0xFF4CAF50)
        "Survival" -> Color(0xFFFAB123)
        "Friendship" -> Color(0xFF29B6F6)
        else -> Color(0xFFAB47BC)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE6E2D3), // ArtisticBorder
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("reminder_item_${reminder.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle checklist decorative icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF4C7C3C).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.Checklist,
                        contentDescription = "Checklist logo",
                        tint = Color(0xFF4C7C3C),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reminder.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor,
                            letterSpacing = 0.5.sp
                        )
                        if (reminder.targetTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⏰ ${reminder.targetTime}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (reminder.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            null
                        }
                    )

                    if (reminder.note.isNotEmpty()) {
                        Text(
                            text = reminder.note,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (reminder.isCompleted) 0.35f else 0.7f)
                        )
                    }

                    if (reminder.characterName.isNotEmpty() && !reminder.isCompleted) {
                        Text(
                            text = "Teman Misi: ${reminder.characterName} 🪵",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = { onCheckedChange() },
                    modifier = Modifier.testTag("reminder_check_${reminder.id}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reminder_delete_${reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus misi",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmActiveOverlay(
    alarm: Alarm,
    formattedTime: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A dramatic full screen dialog that simulates an active ringer overlay!
    Dialog(
        onDismissRequest = {}, // Cannot dismiss by tapping outside, must force action!
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_alert")
        val alertColorMultiplier by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alert"
        )

        val overlayBg = Color(0xFFC0392B).copy(alpha = alertColorMultiplier)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF101C11)) // Dark woodsy cabin background
                .background(overlayBg), // Animated red flashing hazard glow
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large rotating scout badge emblem
                val rotatingBadge = rememberInfiniteTransition(label = "scout_badge_rot")
                val rotationAngle by rotatingBadge.animateFloat(
                    initialValue = -15f,
                    targetValue = 15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )

                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Siren",
                    tint = Color(0xFFFAB123),
                    modifier = Modifier
                        .size(112.dp)
                        .testTag("alarm_ringing_dialog")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ALARM PERKEMAHAN!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ringing time displays in giant fonts
                Text(
                    text = formattedTime,
                    fontSize = 54.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFEB3B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2112)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFFAF3E0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔔 Label Alarm:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5D3B3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = alarm.label,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💬 Yoichi says: \"Ayo, Campers! Jangan malas-malasan di balik kantong tidur, matahari pagi sudah membakar tenda kita!\"",
                            fontSize = 12.sp,
                            color = Color(0xFF8FA88B),
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Actions buttons
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234F32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("alarm_dismiss_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LightMode, contentDescription = "Bangun")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SAYA SUDAH BANGUN! 🎒",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("alarm_snooze_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Snooze, contentDescription = "Snooze")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SNOOZE 1 MENIT 🪵",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, label: String, isDaily: Boolean) -> Unit
) {
    var hourString by remember { mutableStateOf("07") }
    var minuteString by remember { mutableStateOf("00") }
    var label by remember { mutableStateOf("") }
    var isRepeatDaily by remember { mutableStateOf(true) }

    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Pasang Alarm Camp 🏕️",
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Time pick slots input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hourString,
                        onValueChange = {
                            if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                hourString = it
                            }
                        },
                        label = { Text("Jam") },
                        modifier = Modifier.width(70.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = minuteString,
                        onValueChange = {
                            if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                minuteString = it
                            }
                        },
                        label = { Text("Menit") },
                        modifier = Modifier.width(70.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label Alarm (contoh: Makan Kare)") },
                    placeholder = { Text("Bangun Pagi Keitaro") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRepeatDaily,
                        onCheckedChange = { isRepeatDaily = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ulangi Setiap Hari", style = MaterialTheme.typography.bodyMedium)
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Format jam atau menit tidak valid! (Jam: 0-23, Menit: 0-59)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hourString.toIntOrNull()
                    val m = minuteString.toIntOrNull()

                    if (h != null && m != null && h in 0..23 && m in 0..59) {
                        val finalLabel = label.ifEmpty { "Alarm Perkemahan" }
                        onSave(h, m, finalLabel, isRepeatDaily)
                    } else {
                        hasError = true
                    }
                }
            ) {
                Text("Simpan 🪵")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, characterName: String, targetTime: String, note: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var characterName by remember { mutableStateOf("Keitaro") }
    var targetTime by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Survival") }

    val characters = listOf("Keitaro", "Hiro", "Natsumi", "Yoichi", "Taiga", "Goro", "General")
    val categories = listOf("Inspection", "Cooking", "Survival", "Friendship", "Night")

    var showCharDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Misi Kegiatan Baru 🎒",
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Misi Kegiatan") },
                    placeholder = { Text("Mencari kayu bakar di tepi danau") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Character picker drop down custom
                Text("Teman Karakter Camp Buddy:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCharDropdown = true }
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(characterName, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                }

                DropdownMenu(
                    expanded = showCharDropdown,
                    onDismissRequest = { showCharDropdown = false }
                ) {
                    characters.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                characterName = name
                                showCharDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category selection custom
                Text("Kategori Kegiatan:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDropdown = true }
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                }

                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                showCategoryDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetTime,
                    onValueChange = { targetTime = it },
                    label = { Text("Waktu Kegiatan (opsional)") },
                    placeholder = { Text("Contoh: 08:30 AM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Deskripsi / Catatan") },
                    placeholder = { Text("Bawa botol minum dan ransel!") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        onSave(title, characterName, targetTime, note, category)
                    }
                },
                enabled = title.isNotEmpty()
            ) {
                Text("Tambah Misi 🏕️")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
