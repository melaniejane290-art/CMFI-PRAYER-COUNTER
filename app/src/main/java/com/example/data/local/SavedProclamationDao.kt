package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Saved Prayer Proclamations.
 */
@Dao
interface SavedProclamationDao {

    @Query("SELECT * FROM saved_proclamations ORDER BY timestamp DESC")
    fun getAllSavedProclamations(): Flow<List<SavedProclamation>>

    @Query("SELECT * FROM saved_proclamations WHERE id = :id")
    suspend fun getProclamationById(id: Long): SavedProclamation?

    @Query("SELECT * FROM saved_proclamations WHERE topic LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchProclamationsByTopic(query: String): Flow<List<SavedProclamation>>

    @Query("SELECT SUM(proclamationCount) FROM saved_proclamations")
    fun getTotalLifetimeProclamations(): Flow<Int?>

    @Query("SELECT SUM(durationSeconds) FROM saved_proclamations")
    fun getTotalLifetimePrayerDuration(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM saved_proclamations")
    fun getTotalSessionCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProclamation(proclamation: SavedProclamation): Long

    @Update
    suspend fun updateProclamation(proclamation: SavedProclamation)

    @Delete
    suspend fun deleteProclamation(proclamation: SavedProclamation)

    @Query("DELETE FROM saved_proclamations WHERE id = :id")
    suspend fun deleteProclamationById(id: Long)

    @Query("DELETE FROM saved_proclamations")
    suspend fun clearAllSavedProclamations()
}
