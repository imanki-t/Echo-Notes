package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avatar_state")
data class AvatarState(
    @PrimaryKey val id: Int = 1,
    val name: String = "DOODLE-BOT",
    val level: Int = 1,
    val exp: Int = 0,
    val expToNextLevel: Int = 100,
    val mood: String = "HAPPY", // HAPPY, HYPED, SLEEPY, STUDYING, TIRED
    val streak: Int = 1,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String, // Neon brutalist color hex
    val totalSeconds: Long = 0
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val subjectName: String,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "spaced_cards")
data class SpacedCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val question: String,
    val answer: String,
    val intervalDays: Int = 1, // 1, 3, 7, 30, 90
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val difficulty: String = "MEDIUM",
    val consecutiveCorrect: Int = 0,
    val failCount: Int = 0
)

@Entity(tableName = "recurrent_tasks")
data class RecurrentTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val scheduleType: String, // WEEKLY, MONTHLY, CUSTOM
    val intervalDays: Int = 7,
    val nextDueTimestamp: Long = System.currentTimeMillis(),
    val lastCompletedTimestamp: Long = 0
)

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val type: String, // REVISION, TIMER, STREAK, LEVEL_UP, SHAKE, INTEGRATION
    val timestamp: Long = System.currentTimeMillis(),
    val valueGained: String = ""
)
