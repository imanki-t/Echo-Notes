package com.example

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.services.ClickSoundPlayer
import com.example.services.FocusBubbleService
import com.example.services.NotificationHelper
import com.example.ui.theme.*
import com.example.ui.viewmodel.DamagePopup
import com.example.ui.viewmodel.EchoNotesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create user-defined notification channels
        NotificationHelper.createNotificationChannels(this)

        setContent {
            val context = LocalContext.current
            val vm: EchoNotesViewModel = viewModel()
            val isDark by vm.isDarkTheme.collectAsStateWithLifecycle()

            // Handle parameters from widgets & tile
            LaunchedEffect(intent) {
                if (intent?.getBooleanExtra("START_TIMER_FORCE", false) == true ||
                    intent?.getBooleanExtra("FORCE_TIMER_START", false) == true) {
                    vm.isTimerRunning.value = true
                    vm.startTimer()
                }
            }

            // Centralized Dynamic Comic Theme Colors matching ComicStyles
            val bgColor = if (isDark) NeonDarkBg else ComicBgLight
            val cardColor = if (isDark) NeonCardDark else Color.White
            val textColor = if (isDark) NeonOffWhite else ComicBlack
            val primaryColor = if (isDark) NeonPink else ComicPink
            val secondaryColor = if (isDark) NeonCyan else ComicCyan
            val tertiaryColor = if (isDark) NeonGreen else ComicGreen

            Surface(modifier = Modifier.fillMaxSize(), color = bgColor) {
                EchoNotesMainScreen(
                    vm = vm,
                    isDark = isDark,
                    bgColor = bgColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    tertiaryColor = tertiaryColor
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EchoNotesMainScreen(
    vm: EchoNotesViewModel,
    isDark: Boolean,
    bgColor: Color,
    cardColor: Color,
    textColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("DASHBOARD") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            // Retro 8-bit comic banner top-header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .neoBrutalist(
                        backgroundColor = if (isDark) ComicPurple else ComicYellow,
                        cornerRadius = 8.dp,
                        shadowOffset = 5.dp
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ECHO NOTES!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ComicBlack
                )

                // High intensity toggle button switch
                Row(
                    modifier = Modifier
                        .neoBrutalist(
                            backgroundColor = if (isDark) NeonCardDark else Color.White,
                            borderWidth = 2.dp,
                            cornerRadius = 16.dp,
                            shadowOffset = 3.dp
                        )
                        .clickable {
                            ClickSoundPlayer.playMechanicalClick(context)
                            vm.isDarkTheme.value = !isDark
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDark) "NEON NIGHT 🌙" else "COMIC DAY ☀️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) NeonOffWhite else ComicBlack
                    )
                }
            }
        },
        bottomBar = {
            // High-energy tactile pill navigation tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    "DASHBOARD" to "🕹️ DASH",
                    "REVISION" to "👾 RAID",
                    "MISSIONS" to "⏰ CHORES",
                    "HEATMAPS" to "📊 DATA",
                    "SYNC" to "🌐 SYNC"
                )

                tabs.forEach { (tabId, label) ->
                    val isSelected = currentTab == tabId
                    val buttonBg = if (isSelected) {
                        if (isDark) NeonCyan else ComicCyan
                    } else {
                        if (isDark) NeonCardDark else Color.White
                    }

                    Box(
                        modifier = Modifier
                            .testTag("tab_$tabId")
                            .padding(horizontal = 4.dp)
                            .neoBrutalist(
                                backgroundColor = buttonBg,
                                borderWidth = 3.dp,
                                cornerRadius = 12.dp,
                                shadowOffset = if (isSelected) 2.dp else 4.dp
                            )
                            .clickable {
                                ClickSoundPlayer.playMechanicalClick(context)
                                currentTab = tabId
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = ComicBlack
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
        ) {
            // Evolving Study Avatar Header Panel (Renders always for active presence!)
            StudyAvatarHeader(vm = vm, isDark = isDark)

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic view loading depending on selected state tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (currentTab) {
                    "DASHBOARD" -> DashboardScreen(vm = vm, isDark = isDark, cardColor = cardColor, textColor = textColor)
                    "REVISION" -> SpacedRevisionCombatScreen(vm = vm, isDark = isDark, cardColor = cardColor, textColor = textColor)
                    "MISSIONS" -> RecurrentTasksAndLogsScreen(vm = vm, isDark = isDark, cardColor = cardColor, textColor = textColor)
                    "HEATMAPS" -> AnalyticalHeatmapScreen(vm = vm, isDark = isDark, cardColor = cardColor, textColor = textColor)
                    "SYNC" -> CloudSyncSettingsScreen(vm = vm, isDark = isDark, cardColor = cardColor, textColor = textColor)
                }
            }
        }
    }
}

@Composable
fun StudyAvatarHeader(vm: EchoNotesViewModel, isDark: Boolean) {
    val avatar by vm.avatarState.collectAsStateWithLifecycle()
    val state = avatar ?: AvatarState()

    // Render evolving Doodle-Bot drawing on Canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .neoBrutalist(
                backgroundColor = if (isDark) NeonCardDark else Color.White,
                cornerRadius = 12.dp,
                shadowOffset = 5.dp
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Doodle Bot Face drawing
        Box(
            modifier = Modifier
                .size(75.dp)
                .neoBrutalist(
                    backgroundColor = when (state.mood) {
                        "HAPPY" -> ComicGreen
                        "HYPED" -> ComicPink
                        "STUDYING" -> ComicCyan
                        "TIRED" -> ComicYellow
                        else -> ComicPurple
                    },
                    borderWidth = 3.dp,
                    cornerRadius = 35.dp,
                    shadowOffset = 3.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(50.dp)) {
                // Background Bot head
                val strokeWidth = 3.dp.toPx()

                // Render dynamic mood eyes
                when (state.mood) {
                    "HAPPY" -> {
                        // Wide happy arcs
                        drawArc(
                            color = Color.Black,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(5.dp.toPx(), 15.dp.toPx()),
                            size = Size(14.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(strokeWidth)
                        )
                        drawArc(
                            color = Color.Black,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(30.dp.toPx(), 15.dp.toPx()),
                            size = Size(14.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(strokeWidth)
                        )
                        // Big smile
                        drawArc(
                            color = Color.Black,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(15.dp.toPx(), 26.dp.toPx()),
                            size = Size(20.dp.toPx(), 14.dp.toPx()),
                            style = Stroke(strokeWidth)
                        )
                    }
                    "HYPED" -> {
                        // Fire slashes/stars
                        drawLine(Color.Black, Offset(5.dp.toPx(), 15.dp.toPx()), Offset(16.dp.toPx(), 25.dp.toPx()), strokeWidth)
                        drawLine(Color.Black, Offset(16.dp.toPx(), 15.dp.toPx()), Offset(5.dp.toPx(), 25.dp.toPx()), strokeWidth)

                        drawLine(Color.Black, Offset(30.dp.toPx(), 15.dp.toPx()), Offset(41.dp.toPx(), 25.dp.toPx()), strokeWidth)
                        drawLine(Color.Black, Offset(41.dp.toPx(), 15.dp.toPx()), Offset(30.dp.toPx(), 25.dp.toPx()), strokeWidth)

                        // Open screaming triangle
                        val mouthPath = Path().apply {
                            moveTo(18.dp.toPx(), 28.dp.toPx())
                            lineTo(32.dp.toPx(), 28.dp.toPx())
                            lineTo(25.dp.toPx(), 36.dp.toPx())
                            close()
                        }
                        drawPath(mouthPath, Color.Black, style = Fill)
                    }
                    "STUDYING" -> {
                        // Matrix style laser sunglasses/visor
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(4.dp.toPx(), 14.dp.toPx()),
                            size = Size(42.dp.toPx(), 10.dp.toPx()),
                            style = Fill
                        )
                        // Laser light inside visor
                        drawRect(
                            color = Color(0xFF00FF00),
                            topLeft = Offset(8.dp.toPx(), 17.dp.toPx()),
                            size = Size(34.dp.toPx(), 4.dp.toPx()),
                            style = Fill
                        )
                        // Concentrated flat mouth
                        drawLine(Color.Black, Offset(18.dp.toPx(), 32.dp.toPx()), Offset(32.dp.toPx(), 32.dp.toPx()), strokeWidth)
                    }
                    "TIRED" -> {
                        // Downward dizzy narrow slopes
                        drawLine(Color.Black, Offset(6.dp.toPx(), 24.dp.toPx()), Offset(18.dp.toPx(), 16.dp.toPx()), strokeWidth)
                        drawLine(Color.Black, Offset(32.dp.toPx(), 16.dp.toPx()), Offset(44.dp.toPx(), 24.dp.toPx()), strokeWidth)
                        // Wobbly worried outline mouth
                        val wobblyPath = Path().apply {
                            moveTo(18.dp.toPx(), 30.dp.toPx())
                            quadraticTo(21.dp.toPx(), 33.dp.toPx(), 24.dp.toPx(), 30.dp.toPx())
                            quadraticTo(27.dp.toPx(), 27.dp.toPx(), 30.dp.toPx(), 30.dp.toPx())
                        }
                        drawPath(wobblyPath, Color.Black, style = Stroke(strokeWidth))
                    }
                    else -> { // SLEEPY
                        // Flat sleepers Zzz
                        drawLine(Color.Black, Offset(6.dp.toPx(), 20.dp.toPx()), Offset(18.dp.toPx(), 20.dp.toPx()), strokeWidth)
                        drawLine(Color.Black, Offset(32.dp.toPx(), 20.dp.toPx()), Offset(44.dp.toPx(), 20.dp.toPx()), strokeWidth)
                        // Simple small oval snoozing mouth
                        drawOval(
                            color = Color.Black,
                            topLeft = Offset(21.dp.toPx(), 27.dp.toPx()),
                            size = Size(8.dp.toPx(), 11.dp.toPx()),
                            style = Stroke(strokeWidth)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Avatar stats, streak status and dialog box
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.name} [Lvl ${state.level}]",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDark) NeonOffWhite else ComicBlack
                )

                // Streak Banner
                Box(
                    modifier = Modifier
                        .neoBrutalist(
                            backgroundColor = ComicYellow,
                            borderWidth = 1.dp,
                            cornerRadius = 8.dp,
                            shadowOffset = 2.dp
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔥 ${state.streak} DAY STREAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ComicBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bulletproof XP Progress Bar
            val expProgress = state.exp.toFloat() / state.expToNextLevel.coerceAtLeast(1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    .background(Color.White, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(expProgress.coerceIn(0f, 1f))
                        .background(if (isDark) NeonGreen else ComicGreen)
                )
            }

            Text(
                text = "${state.exp}/${state.expToNextLevel} EXP TO LVL ${state.level + 1}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) NeonOffWhite else ComicBlack,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Cartoon Bubble Dialogue
            val comment = when (state.mood) {
                "HAPPY" -> "Who needs sleep when we have GRIT?"
                "HYPED" -> "FEELING THE FIRE! BAM!"
                "STUDYING" -> "ENGAGING HYPERDRIVE FOCUS!"
                "TIRED" -> "Need reading juice and math run victories!"
                else -> "Yawwn... Zzz... 5 more mins of study?"
            }
            Text(
                text = "💬 \"$comment\"",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) NeonOffWhite.copy(alpha = 0.85f) else ComicBlack.copy(alpha = 0.8f)
            )
        }
    }
}

// 1. DASHBOARD OVERVIEW: STUDY TIMER & SUBJECT CREATION
@Composable
fun DashboardScreen(vm: EchoNotesViewModel, isDark: Boolean, cardColor: Color, textColor: Color) {
    val context = LocalContext.current
    val subjects by vm.allSubjects.collectAsStateWithLifecycle()
    val activeSubject by vm.selectedSubject.collectAsStateWithLifecycle()
    val isRunning by vm.isTimerRunning.collectAsStateWithLifecycle()
    val seconds by vm.timerSeconds.collectAsStateWithLifecycle()

    var showAddSubjectDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Arcade Cabinet Study Clock!
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (isDark) ComicPurple else ComicYellow,
                        cornerRadius = 16.dp,
                        shadowOffset = 7.dp
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ARCADE STUDY CLOCK ⏱️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = ComicBlack
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = activeSubject?.name ?: "CHOOSE YOUR BATTLEFIELD BELOW!",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ComicBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Standard visual timer format MM:SS
                val min = seconds / 60
                val sec = seconds % 60
                val timerString = String.format("%02d:%02d", min, sec)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .neoBrutalist(
                            backgroundColor = Color.Black,
                            cornerRadius = 10.dp,
                            shadowOffset = 2.dp
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timerString,
                        fontSize = 42.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isRunning) ComicGreen else ComicPink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer controller buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!isRunning) {
                        Button(
                            onClick = {
                                if (activeSubject == null) {
                                    NotificationHelper.triggerMilestoneAlert(context, "CHOOSE SUBJECT First!", "You need to select a study arena before tapping!")
                                    return@Button
                                }
                                ClickSoundPlayer.playMechanicalClick(context)
                                vm.startTimer()
                            },
                            modifier = Modifier
                                .testTag("start_timer_btn")
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .border(3.dp, Color.Black, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = ComicGreen, contentColor = ComicBlack)
                        ) {
                            Text("START RUN ⚔️", fontWeight = FontWeight.Black)
                        }
                    } else {
                        Button(
                            onClick = {
                                ClickSoundPlayer.playMechanicalClick(context)
                                vm.stopAndSaveTimer()
                            },
                            modifier = Modifier
                                .testTag("stop_timer_btn")
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .border(3.dp, Color.Black, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = ComicPink, contentColor = ComicBlack)
                        ) {
                            Text("STOP & REWARD 🏆", fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Focus Floating bubble launcher toggle
                Row(
                    modifier = Modifier
                        .neoBrutalist(
                            backgroundColor = Color.White,
                            borderWidth = 2.dp,
                            cornerRadius = 12.dp,
                            shadowOffset = 3.dp
                        )
                        .clickable {
                            ClickSoundPlayer.playMechanicalClick(context)
                            val token = true // simulate permissions or call service directly
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                val launchServiceIntent = Intent(context, FocusBubbleService::class.java)
                                context.startService(launchServiceIntent)
                                NotificationHelper.triggerMilestoneAlert(context, "Focus Bubble Active!", "Floating overlay unlocked to deter distraction!")
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Focus Bubble", tint = ComicBlack)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LAUNCH FOCUS BUBBLE OVERLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ComicBlack)
                }
            }
        }

        // Subjects list and Subject battlefield registration form
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDY BATTLEFIELDS",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = textColor
                )

                Box(
                    modifier = Modifier
                        .neoBrutalist(
                            backgroundColor = ComicCyan,
                            borderWidth = 2.dp,
                            cornerRadius = 8.dp,
                            shadowOffset = 3.dp
                        )
                        .clickable {
                            showAddSubjectDialog = true
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("+ ADD ARENA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ComicBlack)
                }
            }
        }

        // Horizontal Grid of customizable subjects
        item {
            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Battlefields. Tap Add Arena!", color = textColor, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    subjects.forEach { sub ->
                        val isSelected = activeSubject?.id == sub.id
                        val colorParsed = try {
                            Color(android.graphics.Color.parseColor(sub.colorHex))
                        } catch (e: Exception) {
                            ComicPink
                        }

                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .neoBrutalist(
                                    backgroundColor = if (isSelected) colorParsed else cardColor,
                                    borderWidth = if (isSelected) 4.dp else 2.dp,
                                    cornerRadius = 12.dp,
                                    shadowOffset = if (isSelected) 2.dp else 5.dp
                                )
                                .clickable {
                                    ClickSoundPlayer.playMechanicalClick(context)
                                    vm.selectSubject(sub)
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = sub.name,
                                tint = if (isSelected) ComicBlack else colorParsed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sub.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) ComicBlack else textColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Mastery Badges Panel
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = cardColor,
                        cornerRadius = 14.dp,
                        shadowOffset = 5.dp
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "🏆 REVEALED MASTERY BADGES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val badges = listOf(
                        Triple("MATH OVERLORD", "Complete 1 math battle", ComicPink),
                        Triple("CODE NINJA", "Study coding for 10s", ComicCyan),
                        Triple("HISTORY QUEST", "Complete 1 card review", ComicYellow),
                        Triple("NO SLEEP CHAMPION", "Active Study Session", ComicGreen)
                    )

                    badges.forEach { (title, req, color) ->
                        Column(
                            modifier = Modifier
                                .width(110.dp)
                                .neoBrutalist(
                                    backgroundColor = color,
                                    borderWidth = 2.5.dp,
                                    cornerRadius = 8.dp,
                                    shadowOffset = 3.dp
                                )
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = ComicBlack, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Unlocked!", fontSize = 8.sp, color = ComicBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to create study arena / subject
    if (showAddSubjectDialog) {
        var arenaName by remember { mutableStateOf("") }
        val selectableColors = listOf("#FF4081", "#00E5FF", "#FFD600", "#00E676", "#D500F9")
        var selectedColorHex by remember { mutableStateOf(selectableColors.first()) }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("REGISTER STUDY ARENA ⚔️", fontWeight = FontWeight.Black, color = ComicBlack) },
            text = {
                Column {
                    OutlinedTextField(
                        value = arenaName,
                        onValueChange = { arenaName = it },
                        label = { Text("Arena Title (e.g. CHEMISTRY RAID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Battlefield Paint Color:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ComicBlack)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        selectableColors.forEach { colorStr ->
                            val parsed = Color(android.graphics.Color.parseColor(colorStr))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (selectedColorHex == colorStr) 4.dp else 1.5.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColorHex = colorStr
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (arenaName.isNotBlank()) {
                            vm.addNewSubject(arenaName, selectedColorHex)
                        }
                        showAddSubjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ComicGreen, contentColor = ComicBlack),
                    modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                ) {
                    Text("REGISTER", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("CANCEL", color = ComicBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// 2. RAID BOSS SPARED-REPETITION COMBAT ARENA
@Composable
fun SpacedRevisionCombatScreen(vm: EchoNotesViewModel, isDark: Boolean, cardColor: Color, textColor: Color) {
    val context = LocalContext.current
    val cards by vm.allCards.collectAsStateWithLifecycle()
    val dueCards = cards.filter { it.nextReviewTimestamp <= System.currentTimeMillis() }

    val bossNameStr by vm.bossName.collectAsStateWithLifecycle()
    val bossMaxHpVal by vm.bossMaxHp.collectAsStateWithLifecycle()
    val bossCurHpVal by vm.bossCurrentHp.collectAsStateWithLifecycle()
    val avatarMaxHpVal by vm.avatarMaxHp.collectAsStateWithLifecycle()
    val avatarCurHpVal by vm.avatarCurrentHp.collectAsStateWithLifecycle()

    var activeCardIndex by remember { mutableStateOf(0) }
    var isAnswerRevealed by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // RAID BOSS STRIKE ARENA CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (isDark) NeonCardDark else Color.White,
                        cornerRadius = 16.dp,
                        shadowOffset = 6.dp
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👾 RAID BOSS COMPACT BATTLE!",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = textColor
                    )

                    Button(
                        onClick = { vm.resetBoss() },
                        colors = ButtonDefaults.buttonColors(containerColor = ComicYellow, contentColor = ComicBlack),
                        modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    ) {
                        Text("SUMMON", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BOSS HP METRIC
                Text(
                    text = "${bossNameStr.uppercase()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = ComicPink,
                    fontFamily = FontFamily.Monospace
                )

                val bossProgress = bossCurHpVal.toFloat() / bossMaxHpVal.coerceAtLeast(1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(bossProgress.coerceIn(0f, 1f))
                            .background(ComicPink)
                    )
                    Text(
                        text = "BOSS HP: $bossCurHpVal / $bossMaxHpVal",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AVATAR HP METRIC
                Text(
                    text = "DOODLE-BOT HEALTH",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = ComicCyan,
                    fontFamily = FontFamily.Monospace
                )

                val avatarProgress = avatarCurHpVal.toFloat() / avatarMaxHpVal.coerceAtLeast(1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(avatarProgress.coerceIn(0f, 1f))
                            .background(ComicCyan)
                    )
                    Text(
                        text = "AVATAR HP: $avatarCurHpVal / $avatarMaxHpVal",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // ACTIVE CARD CLASH ARENA
        item {
            if (dueCards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = ComicGreen,
                            cornerRadius = 12.dp,
                            shadowOffset = 5.dp
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏆 ALL BATTLEFIELDS ARE CLEAN!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = ComicBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No spaced repetition cards due for Day (1, 3, 7, 30, 90) curves. Tap add card below to plant more monsters!",
                        fontSize = 12.sp,
                        color = ComicBlack,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val activeCard = dueCards.getOrNull(activeCardIndex)
                if (activeCard != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoBrutalist(
                                backgroundColor = if (isDark) NeonCardDark else Color.White,
                                borderWidth = 3.dp,
                                cornerRadius = 14.dp,
                                shadowOffset = 6.dp
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BATTLE CARD [${activeCard.subjectName}]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ComicPink
                            )
                            Text(
                                text = "Day Interval: ${activeCard.intervalDays}D",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large question board
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalist(
                                    backgroundColor = ComicYellow,
                                    borderWidth = 2.dp,
                                    cornerRadius = 8.dp,
                                    shadowOffset = 3.dp
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = activeCard.question,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = ComicBlack,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reveal Answer box
                        if (isAnswerRevealed) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neoBrutalist(
                                        backgroundColor = ComicCyan,
                                        borderWidth = 2.dp,
                                        cornerRadius = 8.dp,
                                        shadowOffset = 3.dp
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = activeCard.answer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ComicBlack,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action hit buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        ClickSoundPlayer.playMechanicalClick(context)
                                        vm.attackBossWithRetentionCard(activeCard, gotIt = true)
                                        isAnswerRevealed = false
                                        if (activeCardIndex >= dueCards.size - 1) {
                                            activeCardIndex = 0
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = ComicGreen, contentColor = ComicBlack)
                                ) {
                                    Text("BAM! GOT IT! 🎯", fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        ClickSoundPlayer.playMechanicalClick(context)
                                        vm.attackBossWithRetentionCard(activeCard, gotIt = false)
                                        isAnswerRevealed = false
                                        if (activeCardIndex >= dueCards.size - 1) {
                                            activeCardIndex = 0
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = ComicPink, contentColor = ComicBlack)
                                ) {
                                    Text("OUGH! FORGOT 💀", fontWeight = FontWeight.Black)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    ClickSoundPlayer.playMechanicalClick(context)
                                    isAnswerRevealed = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = ComicPink, contentColor = ComicBlack)
                            ) {
                                Text("REVEAL REVISION ANSWER 🔍", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // BATTLE LOGS POP-UP FEED
        item {
            Text(
                text = "BATTLE RECAP COMBAT LOGS",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = textColor
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 130.dp)
                    .neoBrutalist(
                        backgroundColor = Color.Black,
                        cornerRadius = 10.dp,
                        shadowOffset = 2.dp
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (vm.battleLogs.isEmpty()) {
                    Text("Combats idle. Engage cards!", color = ComicGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                } else {
                    vm.battleLogs.take(5).forEach { log ->
                        Text(log, color = ComicGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Form registration trigger
        item {
            Button(
                onClick = { showAddCardDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Color.Black, RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = ComicCyan, contentColor = ComicBlack)
            ) {
                Text("+ CREATE NEW RETENTION FLASHCARD", fontWeight = FontWeight.Black)
            }
        }
    }

    if (showAddCardDialog) {
        var cardSub by remember { mutableStateOf("") }
        var cardQuest by remember { mutableStateOf("") }
        var cardAns by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("MINT STUDY MONSTER CARD 👾", fontWeight = FontWeight.Black, color = ComicBlack) },
            text = {
                Column {
                    OutlinedTextField(
                        value = cardSub,
                        onValueChange = { cardSub = it },
                        label = { Text("Subject (e.g. MATH RAID)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardQuest,
                        onValueChange = { cardQuest = it },
                        label = { Text("Question Board") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardAns,
                        onValueChange = { cardAns = it },
                        label = { Text("Annihilation Secrets / Answer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardSub.isNotBlank() && cardQuest.isNotBlank() && cardAns.isNotBlank()) {
                            vm.addNewCard(cardSub, cardQuest, cardAns)
                        }
                        showAddCardDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ComicGreen, contentColor = ComicBlack),
                    modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                ) {
                    Text("MINT CARD", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("CANCEL", color = ComicBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// 3. RECURRENT CHORES & LOG HISTORIES
@Composable
fun RecurrentTasksAndLogsScreen(vm: EchoNotesViewModel, isDark: Boolean, cardColor: Color, textColor: Color) {
    val context = LocalContext.current
    val tasks by vm.allRecurrentTasks.collectAsStateWithLifecycle()
    val logs by vm.recentLogs.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECURRENT STUDY CHORES",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = textColor
                )

                Box(
                    modifier = Modifier
                        .neoBrutalist(
                            backgroundColor = ComicYellow,
                            borderWidth = 2.dp,
                            cornerRadius = 8.dp,
                            shadowOffset = 3.dp
                        )
                        .clickable { showAddTaskDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+ MISSION", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ComicBlack)
                }
            }
        }

        // Display recurrent items (e.g. weekly review writing or monthly essay sweeps)
        if (tasks.isEmpty()) {
            item {
                Text("No study chores planned. Hit + MISSION!", color = textColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
            }
        } else {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = cardColor,
                            cornerRadius = 10.dp,
                            shadowOffset = 4.dp
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Text(
                            text = "Cooldown Interval: ${task.intervalDays} Days [${task.scheduleType}]",
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.75f)
                        )
                    }

                    Button(
                        onClick = {
                            ClickSoundPlayer.playMechanicalClick(context)
                            ClickSoundPlayer.playLevelUpSound()
                            vm.completeRecurrentTask(task)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ComicCyan, contentColor = ComicBlack),
                        modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    ) {
                        Text("COMPLETED!", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // LOG HISTORY RECORDS
        item {
            Text(
                text = "STUDY JOURNALS & LOGS 📜",
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = textColor
            )
        }

        if (logs.isEmpty()) {
            item {
                Text("No chronicles recorded yet.", color = textColor, modifier = Modifier.padding(12.dp))
            }
        } else {
            items(logs) { lg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = when (lg.type) {
                                "LEVEL_UP" -> ComicYellow
                                "TIMER" -> ComicCyan
                                "SHAKE" -> ComicPink
                                else -> cardColor
                            },
                            cornerRadius = 8.dp,
                            shadowOffset = 3.dp
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lg.text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ComicBlack
                        )
                    }
                    if (lg.valueGained.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(ComicBlack, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(lg.valueGained, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        var missionTitle by remember { mutableStateOf("") }
        var scheduleTypeString by remember { mutableStateOf("WEEKLY") }
        var intervalValDays by remember { mutableStateOf(7) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("PLAN CHORE MISSION 📋", fontWeight = FontWeight.Black, color = ComicBlack) },
            text = {
                Column {
                    OutlinedTextField(
                        value = missionTitle,
                        onValueChange = { missionTitle = it },
                        label = { Text("Chore Title (e.g. Weekly Letter sweep)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Cadence Interval:", fontWeight = FontWeight.Bold, color = ComicBlack)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WEEKLY" to 7, "MONTHLY" to 30).forEach { (label, duration) ->
                            val isSelected = scheduleTypeString == label
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .neoBrutalist(
                                        backgroundColor = if (isSelected) ComicPink else Color.White,
                                        borderWidth = 2.dp,
                                        cornerRadius = 6.dp,
                                        shadowOffset = 2.dp
                                    )
                                    .clickable {
                                        scheduleTypeString = label
                                        intervalValDays = duration
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = ComicBlack)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (missionTitle.isNotBlank()) {
                            vm.addNewRecurrentTask(missionTitle, scheduleTypeString, intervalValDays)
                        }
                        showAddTaskDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ComicGreen, contentColor = ComicBlack),
                    modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                ) {
                    Text("DECREE CHORE", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("CANCEL", color = ComicBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// 4. ANALYTICAL HEATMAPS & PERFORMANCE GRID
@Composable
fun AnalyticalHeatmapScreen(vm: EchoNotesViewModel, isDark: Boolean, cardColor: Color, textColor: Color) {
    val context = LocalContext.current
    val subjects by vm.allSubjects.collectAsStateWithLifecycle()
    val cards by vm.allCards.collectAsStateWithLifecycle()
    
    // Sort weak flashcard targets (fail count > 0)
    val weakSpots = cards.filter { it.failCount > 0 }.sortedByDescending { it.failCount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "WEEKLY STUDY HEATMAPS 📊",
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = textColor
            )
        }

        // Render 8-bit visual heatmap grid (Days of week Sun-Sat vs Subject)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (isDark) NeonCardDark else Color.White,
                        cornerRadius = 12.dp,
                        shadowOffset = 5.dp
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "COMMITTED BATTLE ACTIVITY RANGE (Pixel Heatmap)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComicPink
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Headers
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(60.dp))
                    daysOfWeek.forEach { dayLetter ->
                        Text(
                            text = dayLetter,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            modifier = Modifier.width(22.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Heatmaps rows per subject
                if (subjects.isEmpty()) {
                    Text("No Subject battles logged to compute ranges.", fontSize = 11.sp, color = textColor)
                } else {
                    subjects.forEachIndexed { sIndex, sub ->
                        val subColor = try {
                            Color(android.graphics.Color.parseColor(sub.colorHex))
                        } catch (e: Exception) {
                            ComicPink
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.name.take(10),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.width(60.dp),
                                maxLines = 1
                            )

                            // 7 cell pixels
                            daysOfWeek.forEachIndexed { dIndex, _ ->
                                // Compute simulated activity density grid based on indices
                                val activeIntensity = (sIndex + dIndex) % 4
                                val cellBg = when (activeIntensity) {
                                    1 -> subColor.copy(alpha = 0.35f)
                                    2 -> subColor.copy(alpha = 0.65f)
                                    3 -> subColor
                                    else -> if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
                                        .background(cellBg)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Analytical weakness spot heatmap target list
        item {
            Text(
                text = "⚡ SPOTLIGHTED WEAK SPOTS",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = textColor
            )
        }

        if (weakSpots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = ComicCyan,
                            cornerRadius = 10.dp,
                            shadowOffset = 3.dp
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Clean scan! No flashcard marked with forgot state yet. Keep doing raid runs to populate analytics!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComicBlack
                    )
                }
            }
        } else {
            items(weakSpots) { card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = cardColor,
                            cornerRadius = 10.dp,
                            shadowOffset = 4.dp
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.question,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = textColor
                        )
                        Text(
                            text = "Subject Battlefield: ${card.subjectName}",
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .neoBrutalist(
                                backgroundColor = ComicPink,
                                borderWidth = 1.5.dp,
                                cornerRadius = 6.dp,
                                shadowOffset = 2.dp
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("FAILED ${card.failCount}X", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ComicBlack)
                    }
                }
            }
        }
    }
}

// 5. CLOUD SYNC & GOOGLE CREDENTIAL SYSTEM TAB
@Composable
fun CloudSyncSettingsScreen(vm: EchoNotesViewModel, isDark: Boolean, cardColor: Color, textColor: Color) {
    val context = LocalContext.current
    val loggedIn by vm.isGoogleLoggedIn.collectAsStateWithLifecycle()
    val emailString by vm.googleEmail.collectAsStateWithLifecycle()

    val calSync by vm.isCalendarSynced.collectAsStateWithLifecycle()
    val keepSync by vm.isKeepSynced.collectAsStateWithLifecycle()
    val samSync by vm.isSamsungNotesSynced.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "BI-DIRECTIONAL CLOUD SYNCS 🌐",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = textColor
            )
        }

        // Google Account auth login frame
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (loggedIn) ComicGreen else ComicYellow,
                        cornerRadius = 12.dp,
                        shadowOffset = 6.dp
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "GOOGLE CLOUD INTEGRATION SYSTEM",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = ComicBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (loggedIn) {
                    Text(
                        text = "Status: ACTIVE & SYNCED!",
                        fontWeight = FontWeight.ExtraBold,
                        color = ComicBlack,
                        fontSize = 14.sp
                    )
                    Text("Linked: $emailString", fontSize = 11.sp, color = ComicBlack)
                } else {
                    Text(
                        text = "Status: OFFLINE-ONLY MODE",
                        fontWeight = FontWeight.ExtraBold,
                        color = ComicBlack,
                        fontSize = 14.sp
                    )
                    Text("Instant local persistence acts as high performance safety card.", fontSize = 11.sp, color = ComicBlack)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleGoogleLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (loggedIn) "DISCONNECT SECURE GOOGLE CLIENT" else "CONNECT ME TO GOOGLE ACCOUNT 🔑",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Individual Sync switches (Calendar, Keep, Samsung notes)
        item {
            Text(
                text = "INDIVIDUAL SYNC BATTLE CRADLES",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = textColor
            )
        }

        // Calendar sync switch
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (calSync) ComicCyan else cardColor,
                        cornerRadius = 10.dp,
                        shadowOffset = 4.dp
                    )
                    .clickable {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleCalendarSync()
                    }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🗓️ GOOGLE CALENDAR SYNC", fontWeight = FontWeight.Black, color = ComicBlack, fontSize = 14.sp)
                    Text("Auto exports Study Stopwatch logs as event milestones.", fontSize = 11.sp, color = ComicBlack.copy(alpha = 0.75f))
                }
                Switch(
                    checked = calSync,
                    onCheckedChange = {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleCalendarSync()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ComicBlack, checkedTrackColor = ComicGreen)
                )
            }
        }

        // Keep Sync switch
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (keepSync) ComicYellow else cardColor,
                        cornerRadius = 10.dp,
                        shadowOffset = 4.dp
                    )
                    .clickable {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleKeepSync()
                    }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("💡 GOOGLE KEEP INTEGRATION", fontWeight = FontWeight.Black, color = ComicBlack, fontSize = 14.sp)
                    Text("Generates quick-flash revision links sync to Keep cards.", fontSize = 11.sp, color = ComicBlack.copy(alpha = 0.75f))
                }
                Switch(
                    checked = keepSync,
                    onCheckedChange = {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleKeepSync()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ComicBlack, checkedTrackColor = ComicGreen)
                )
            }
        }

        // Samsung Notes Sync switch
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalist(
                        backgroundColor = if (samSync) ComicPink else cardColor,
                        cornerRadius = 10.dp,
                        shadowOffset = 4.dp
                    )
                    .clickable {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleSamsungNotesSync()
                    }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📱 SAMSUNG NOTES EXPORTER", fontWeight = FontWeight.Black, color = ComicBlack, fontSize = 14.sp)
                    Text("Flares notes directly as revision battles here.", fontSize = 11.sp, color = ComicBlack.copy(alpha = 0.75f))
                }
                Switch(
                    checked = samSync,
                    onCheckedChange = {
                        ClickSoundPlayer.playMechanicalClick(context)
                        vm.toggleSamsungNotesSync()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ComicBlack, checkedTrackColor = ComicGreen)
                )
            }
        }
    }
}
