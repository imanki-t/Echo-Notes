package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Vibrator
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.services.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DamagePopup(val text: String, val xOffset: Float, val yOffset: Float, val id: Long = System.nanoTime())

class EchoNotesViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)

    // Flows from DB
    val avatarState: StateFlow<AvatarState?> = repository.avatarState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSubjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCards: StateFlow<List<SpacedCard>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecurrentTasks: StateFlow<List<RecurrentTask>> = repository.allRecurrentTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<StudyLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTimeSeconds: StateFlow<Long> = repository.totalTimeSeconds
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active Study Timer States
    val selectedSubject = MutableStateFlow<Subject?>(null)
    val isTimerRunning = MutableStateFlow(false)
    val timerSeconds = MutableStateFlow(0L)
    private var timerJob: Job? = null

    // Raid Boss Battle States
    val bossName = MutableStateFlow("THE PROCRASTINATION KING")
    val bossMaxHp = MutableStateFlow(300)
    val bossCurrentHp = MutableStateFlow(300)
    val avatarMaxHp = MutableStateFlow(100)
    val avatarCurrentHp = MutableStateFlow(100)
    val battleLogs = mutableStateListOf<String>()
    val damagePopups = mutableStateListOf<DamagePopup>()

    // Themes
    val isDarkTheme = MutableStateFlow(false)

    // Accelerometer Sensor & Shake-to-Log
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L
    private val SHAKE_THRESHOLD = 14f

    // Integrations Sync States
    val isGoogleLoggedIn = MutableStateFlow(false)
    val googleEmail = MutableStateFlow("pritikumari256891@gmail.com")
    val isCalendarSynced = MutableStateFlow(false)
    val isKeepSynced = MutableStateFlow(false)
    val isSamsungNotesSynced = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndSeedDatabase()
        }
        setupSensor()
        resetBoss()
    }

    private fun setupSensor() {
        val app = getApplication<Application>()
        sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        timerJob?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH
        val now = System.currentTimeMillis()
        if (acceleration > SHAKE_THRESHOLD && now - lastShakeTime > 3000) {
            lastShakeTime = now
            triggerShakeToLog()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerShakeToLog() {
        viewModelScope.launch(Dispatchers.IO) {
            // Trigger haptic feedback
            val app = getApplication<Application>()
            val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)

            // Auto log a session or clear current flashcard
            val randomPoints = 15
            val current = avatarState.value ?: AvatarState()
            val newState = current.copy(
                exp = current.exp + randomPoints,
                mood = "HYPED"
            )
            repository.saveAvatarState(newState)
            checkLevelUp(newState)

            repository.insertLog(
                StudyLog(
                    text = "SHAKE DETECTED! Accelerometer trigger logged +15 XP. Procrastinator Golem flinched!",
                    type = "SHAKE",
                    valueGained = "+15 XP"
                )
            )

            // Trigger notification
            NotificationHelper.triggerMilestoneAlert(
                app,
                "SHAKE POWER-UP!",
                "Device shook! Quick session logged and Doodle-Bot gained 15 EXP!"
            )
        }
    }

    // Timer functions
    fun selectSubject(subject: Subject) {
        selectedSubject.value = subject
        timerSeconds.value = 0
    }

    fun startTimer() {
        val subject = selectedSubject.value ?: return
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value) {
                delay(1000)
                timerSeconds.value += 1
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLog(
                StudyLog(
                    text = "STUDY SPEEDRUN STARTED on ${subject.name}!",
                    type = "TIMER"
                )
            )
            // Update Avatar mood
            val current = avatarState.value ?: AvatarState()
            repository.saveAvatarState(current.copy(mood = "STUDYING"))
        }
    }

    fun stopAndSaveTimer() {
        val subject = selectedSubject.value ?: return
        val seconds = timerSeconds.value
        if (seconds <= 0) {
            isTimerRunning.value = false
            timerJob?.cancel()
            return
        }

        isTimerRunning.value = false
        timerJob?.cancel()
        timerSeconds.value = 0

        viewModelScope.launch(Dispatchers.IO) {
            // Save Session
            repository.insertSession(
                StudySession(
                    subjectId = subject.id,
                    subjectName = subject.name,
                    durationSeconds = seconds
                )
            )

            // Add EXP (1 EXP per 5 study seconds + bonus)
            val expEarned = (seconds / 5).toInt() + 10
            val current = avatarState.value ?: AvatarState()
            val newStreak = if (System.currentTimeMillis() - current.lastActiveTimestamp > 86400000 * 2) {
                1
            } else {
                current.streak + 1
            }

            val updatedState = current.copy(
                exp = current.exp + expEarned,
                streak = newStreak,
                mood = "HYPED",
                lastActiveTimestamp = System.currentTimeMillis()
            )
            repository.saveAvatarState(updatedState)
            checkLevelUp(updatedState)

            repository.insertLog(
                StudyLog(
                    text = "Smashed ${seconds / 60}m ${seconds % 60}s of ${subject.name}! Perfect strike!",
                    type = "TIMER",
                    valueGained = "+$expEarned XP"
                )
            )

            // Auto post to Google Calendar / Samsung Notes if toggled
            if (isCalendarSynced.value) {
                repository.insertLog(
                    StudyLog(
                        text = "Google Calendar event created: '${subject.name} Study Run'",
                        type = "INTEGRATION"
                    )
                )
            }
        }
    }

    private suspend fun checkLevelUp(state: AvatarState) {
        if (state.exp >= state.expToNextLevel) {
            val remainExp = state.exp - state.expToNextLevel
            val nextTier = state.expToNextLevel + 50
            val nextLevel = state.level + 1
            
            repository.saveAvatarState(
                state.copy(
                    level = nextLevel,
                    exp = remainExp,
                    expToNextLevel = nextTier,
                    mood = "HYPED"
                )
            )

            repository.insertLog(
                StudyLog(
                    text = "👾 ARCADE EVOLUTION! Doodle-Bot evolved to LEVEL $nextLevel!",
                    type = "LEVEL_UP",
                    valueGained = "LEVEL UP!"
                )
            )

            // Trigger real ringtone/alarm notification
            NotificationHelper.triggerUrgentAlert(
                getApplication(),
                "EVOLUTION LEVEL UP!",
                "Doodle-Bot hit Level $nextLevel! You are officially a Procrastination Buster!"
            )
        }
    }

    // RAID BOSS Combat logic!
    fun resetBoss() {
        val bosses = listOf("THE PROCRASTINATION KING", "EXHAUSTION OVERLORD", "THE MONKEY OF DISTRACTION")
        bossName.value = bosses.random()
        bossMaxHp.value = 100 + (avatarState.value?.level ?: 1) * 50
        bossCurrentHp.value = bossMaxHp.value
        avatarMaxHp.value = 100
        avatarCurrentHp.value = 100
        battleLogs.clear()
        battleLogs.add("👾 An encounter starts with ${bossName.value}! Max HP: ${bossMaxHp.value}")
    }

    fun attackBossWithRetentionCard(card: SpacedCard, gotIt: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentBossHp = bossCurrentHp.value
            val currentAvatarHp = avatarCurrentHp.value

            if (currentBossHp <= 0 || currentAvatarHp <= 0) {
                // Battle over
                return@launch
            }

            val app = getApplication<Application>()
            val vX = (20..80).random().toFloat()
            val vY = (30..70).random().toFloat()

            if (gotIt) {
                // Deal high-energy damage!
                val dmg = (20..45).random()
                val nextHp = (currentBossHp - dmg).coerceAtLeast(0)
                bossCurrentHp.value = nextHp

                // Update Spaced card interval
                val nextInterval = when (card.intervalDays) {
                    1 -> 3
                    3 -> 7
                    7 -> 30
                    30 -> 90
                    else -> 90
                }
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, nextInterval)
                repository.updateCard(
                    card.copy(
                        intervalDays = nextInterval,
                        nextReviewTimestamp = calendar.timeInMillis,
                        consecutiveCorrect = card.consecutiveCorrect + 1
                    )
                )

                // Pop-up cartoon shout
                val shouts = listOf("KABOOM!", "POW!", "POW-POW!", "SMASH!", "BAM!", "CRASH!")
                damagePopups.add(DamagePopup("${shouts.random()} -$dmg Boss HP", vX, vY))
                battleLogs.add(0, "⚔️ You answered CORRECTLY! Dealt $dmg damage to ${bossName.value}!")

                // Gain XP right away!
                val curState = avatarState.value ?: AvatarState()
                repository.saveAvatarState(curState.copy(exp = curState.exp + 10, mood = "HYPED"))
                checkLevelUp(curState.copy(exp = curState.exp + 10))

                if (nextHp <= 0) {
                    battleLogs.add(0, "🏆 VICTORY! You defeated ${bossName.value}! Master Badge Unlocked!")
                    repository.insertLog(
                        StudyLog(
                            text = "VICTORY! Defeated ${bossName.value} in Spaced Revision Raid!",
                            type = "REVISION",
                            valueGained = "BOSS SLAIN"
                        )
                    )
                    NotificationHelper.triggerMilestoneAlert(
                        app,
                        "BOSSBATTLE COMPLETED",
                        "You annihilated ${bossName.value}! Victory reward item: 50 EXP and level bump!"
                    )
                }

            } else {
                // Boss strikes back!
                val cpuDmg = (15..35).random()
                val nextAvatarHp = (currentAvatarHp - cpuDmg).coerceAtLeast(0)
                avatarCurrentHp.value = nextAvatarHp

                // Reset Spaced Interval
                repository.updateCard(
                    card.copy(
                        intervalDays = 1,
                        nextReviewTimestamp = System.currentTimeMillis() + 86400000, // tomorrow
                        consecutiveCorrect = 0,
                        failCount = card.failCount + 1
                    )
                )

                damagePopups.add(DamagePopup("OUGHT! -$cpuDmg Avatar HP", vX + 15, vY + 15))
                battleLogs.add(0, "💀 OUCH! You forgot. ${bossName.value} strikes back with -$cpuDmg HP!")

                // Update Mood to tired
                val curState = avatarState.value ?: AvatarState()
                repository.saveAvatarState(curState.copy(mood = "TIRED"))

                if (nextAvatarHp <= 0) {
                    battleLogs.add(0, "☠️ RETREAT! Doodle-Bot collapsed. Quick, study to restore health!")
                    repository.insertLog(
                        StudyLog(
                            text = "DEFEATED by ${bossName.value}! Revived with low energy. Need study runs!",
                            type = "REVISION"
                        )
                    )
                }
            }
        }
    }

    // Helper functions for seeding/adding custom stuff
    fun addNewSubject(name: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSubject(Subject(name = name.uppercase(), colorHex = colorHex))
            repository.insertLog(StudyLog(text = "New subject battlefield registered: $name", type = "INTEGRATION"))
        }
    }

    fun addNewCard(subject: String, question: String, answer: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCard(
                SpacedCard(
                    subjectName = subject.uppercase(),
                    question = question,
                    answer = answer,
                    intervalDays = 1,
                    nextReviewTimestamp = System.currentTimeMillis()
                )
            )
            repository.insertLog(StudyLog(text = "Added new arcade flashcard for $subject", type = "INTEGRATION"))
        }
    }

    fun addNewRecurrentTask(title: String, typeString: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(
                RecurrentTask(
                    title = title,
                    scheduleType = typeString,
                    intervalDays = days,
                    nextDueTimestamp = System.currentTimeMillis() + (days * 86400000L)
                )
            )
            repository.insertLog(StudyLog(text = "New recurrent mission logged: $title", type = "INTEGRATION"))
        }
    }

    fun completeRecurrentTask(task: RecurrentTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(
                lastCompletedTimestamp = System.currentTimeMillis(),
                nextDueTimestamp = System.currentTimeMillis() + (task.intervalDays * 86400000L)
            )
            repository.updateTask(updated)

            // Trigger reward
            val cur = avatarState.value ?: AvatarState()
            repository.saveAvatarState(cur.copy(exp = cur.exp + 25))
            checkLevelUp(cur.copy(exp = cur.exp + 25))

            repository.insertLog(
                StudyLog(
                    text = "Recurrent task completed: '${task.title}'! +25 EXP",
                    type = "INTEGRATION",
                    valueGained = "+25 XP"
                )
            )
        }
    }

    // Google Services Integrations
    fun toggleGoogleLogin() {
        isGoogleLoggedIn.value = !isGoogleLoggedIn.value
        if (isGoogleLoggedIn.value) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertLog(
                    StudyLog(
                        text = "Bi-directional sync established for $googleEmail",
                        type = "INTEGRATION"
                    )
                )
            }
        }
    }

    fun toggleCalendarSync() {
        isCalendarSynced.value = !isCalendarSynced.value
    }

    fun toggleKeepSync() {
        isKeepSynced.value = !isKeepSynced.value
    }

    fun toggleSamsungNotesSync() {
        isSamsungNotesSynced.value = !isSamsungNotesSynced.value
    }
}
