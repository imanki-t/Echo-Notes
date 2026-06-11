package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatar_state WHERE id = 1 LIMIT 1")
    fun getAvatarState(): Flow<AvatarState?>

    @Query("SELECT * FROM avatar_state WHERE id = 1 LIMIT 1")
    suspend fun getAvatarStateDirect(): AvatarState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAvatarState(state: AvatarState)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: Int)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)

    @Query("SELECT SUM(durationSeconds) FROM study_sessions")
    fun getTotalStudyTimeSeconds(): Flow<Long?>
}

@Dao
interface SpacedCardDao {
    @Query("SELECT * FROM spaced_cards ORDER BY nextReviewTimestamp ASC")
    fun getAllCards(): Flow<List<SpacedCard>>

    @Query("SELECT * FROM spaced_cards WHERE nextReviewTimestamp <= :currentTimestamp ORDER BY nextReviewTimestamp ASC")
    fun getDueCards(currentTimestamp: Long): Flow<List<SpacedCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SpacedCard)

    @Update
    suspend fun updateCard(card: SpacedCard)

    @Delete
    suspend fun deleteCard(card: SpacedCard)
}

@Dao
interface RecurrentTaskDao {
    @Query("SELECT * FROM recurrent_tasks ORDER BY nextDueTimestamp ASC")
    fun getAllRecurrentTasks(): Flow<List<RecurrentTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: RecurrentTask)

    @Update
    suspend fun updateTask(task: RecurrentTask)

    @Delete
    suspend fun deleteTask(task: RecurrentTask)
}

@Dao
interface StudyLogDao {
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<StudyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudyLog)

    @Query("DELETE FROM study_logs")
    suspend fun clearLogs()
}
