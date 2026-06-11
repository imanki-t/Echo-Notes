package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val db: AppDatabase) {
    val avatarState: Flow<AvatarState?> = db.avatarDao().getAvatarState()
    val allSubjects: Flow<List<Subject>> = db.subjectDao().getAllSubjects()
    val allSessions: Flow<List<StudySession>> = db.studySessionDao().getAllSessions()
    val totalTimeSeconds: Flow<Long?> = db.studySessionDao().getTotalStudyTimeSeconds()
    val allCards: Flow<List<SpacedCard>> = db.spacedCardDao().getAllCards()
    val allRecurrentTasks: Flow<List<RecurrentTask>> = db.recurrentTaskDao().getAllRecurrentTasks()
    val recentLogs: Flow<List<StudyLog>> = db.studyLogDao().getRecentLogs()

    suspend fun checkAndSeedDatabase() {
        // Seed default avatar if empty
        val currentAvatar = db.avatarDao().getAvatarStateDirect()
        if (currentAvatar == null) {
            db.avatarDao().saveAvatarState(AvatarState())
        }

        // Seed default subjects if empty
        val defaultSubjects = listOf(
            Subject(name = "MATH RAID", colorHex = "#FF4081"), // Hot Pink
            Subject(name = "CODE SLAYER", colorHex = "#00E5FF"), // Neon Cyan
            Subject(name = "HISTORY QUEST", colorHex = "#FFD600"), // Yellow
            Subject(name = "SCI-FI LAB", colorHex = "#00E676")  // Neon Green
        )
        // Check if subjects are empty
        val subjects = db.subjectDao().getAllSubjects().firstOrNull() ?: emptyList()
        if (subjects.isEmpty()) {
            for (sub in defaultSubjects) {
                db.subjectDao().insertSubject(sub)
            }

            // Seed default cards
            db.spacedCardDao().insertCard(
                SpacedCard(
                    subjectName = "MATH RAID",
                    question = "What is the Euler's formula representation?",
                    answer = "e^(i*x) = cos(x) + i*sin(x). Absolute masterpiece!",
                    difficulty = "HARD",
                    intervalDays = 1
                )
            )
            db.spacedCardDao().insertCard(
                SpacedCard(
                    subjectName = "CODE SLAYER",
                    question = "Explain SOLID principles in 5 letters",
                    answer = "S: Single Responsibility, O: Open/Closed, L: Liskov design, I: Interface segregation, D: Dependency inversions.",
                    difficulty = "MEDIUM",
                    intervalDays = 3
                )
            )
            db.spacedCardDao().insertCard(
                SpacedCard(
                    subjectName = "HISTORY QUEST",
                    question = "Who designed the neon signs of classic arcades?",
                    answer = "Georges Claude originally patented neon lighting in 1910!",
                    difficulty = "EASY",
                    intervalDays = 7
                )
            )

            // Seed recurrent tasks
            db.recurrentTaskDao().insertTask(
                RecurrentTask(
                    title = "Weekly Comic Scrapbook Drawing",
                    scheduleType = "WEEKLY",
                    intervalDays = 7
                )
            )
            db.recurrentTaskDao().insertTask(
                RecurrentTask(
                    title = "Monthly Spaced Revision Cleanout",
                    scheduleType = "MONTHLY",
                    intervalDays = 30
                )
            )

            // Log first event
            db.studyLogDao().insertLog(
                StudyLog(
                    text = "ECHO NOTES initialized. Ready to SMASH procrastination!",
                    type = "INTEGRATION"
                )
            )
        }
    }

    suspend fun saveAvatarState(state: AvatarState) {
        db.avatarDao().saveAvatarState(state)
    }

    suspend fun insertSubject(subject: Subject) {
        db.subjectDao().insertSubject(subject)
    }

    suspend fun deleteSubject(id: Int) {
        db.subjectDao().deleteSubject(id)
    }

    suspend fun insertSession(session: StudySession) {
        db.studySessionDao().insertSession(session)
    }

    suspend fun insertCard(card: SpacedCard) {
        db.spacedCardDao().insertCard(card)
    }

    suspend fun updateCard(card: SpacedCard) {
        db.spacedCardDao().updateCard(card)
    }

    suspend fun deleteCard(card: SpacedCard) {
        db.spacedCardDao().deleteCard(card)
    }

    suspend fun insertTask(task: RecurrentTask) {
        db.recurrentTaskDao().insertTask(task)
    }

    suspend fun updateTask(task: RecurrentTask) {
        db.recurrentTaskDao().updateTask(task)
    }

    suspend fun deleteTask(task: RecurrentTask) {
        db.recurrentTaskDao().deleteTask(task)
    }

    suspend fun insertLog(log: StudyLog) {
        db.studyLogDao().insertLog(log)
    }

    suspend fun clearLogs() {
        db.studyLogDao().clearLogs()
    }
}
