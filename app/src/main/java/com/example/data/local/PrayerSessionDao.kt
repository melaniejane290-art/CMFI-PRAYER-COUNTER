package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Prayer Session History.
 */
@Dao
interface PrayerSessionDao {

    @Query("SELECT * FROM prayer_session_history ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PrayerSessionRecord>>

    @Query("SELECT * FROM prayer_session_history WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSessionsSince(startTime: Long): Flow<List<PrayerSessionRecord>>

    @Query("SELECT * FROM prayer_session_history WHERE id = :id")
    suspend fun getSessionById(id: Long): PrayerSessionRecord?

    @Query("SELECT SUM(sessionCount) FROM prayer_session_history")
    fun getTotalProclamations(): Flow<Int?>

    @Query("SELECT SUM(durationSeconds) FROM prayer_session_history")
    fun getTotalDurationSeconds(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM prayer_session_history")
    fun getTotalSessionCount(): Flow<Int>

    @Query("SELECT AVG(sessionCount) FROM prayer_session_history WHERE sessionCount > 0")
    fun getAverageProclamationsPerSession(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PrayerSessionRecord): Long

    @Update
    suspend fun updateSession(session: PrayerSessionRecord)

    @Delete
    suspend fun deleteSession(session: PrayerSessionRecord)

    @Query("DELETE FROM prayer_session_history WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM prayer_session_history")
    suspend fun clearAllHistory()
}
