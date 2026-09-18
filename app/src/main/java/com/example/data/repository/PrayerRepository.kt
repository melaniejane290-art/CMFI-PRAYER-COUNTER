package com.example.data.repository

import com.example.data.local.PrayerSessionDao
import com.example.data.local.PrayerSessionRecord
import com.example.data.local.SavedProclamation
import com.example.data.local.SavedProclamationDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing access to saved prayer proclamations and session history logs.
 */
class PrayerRepository(
    private val proclamationDao: SavedProclamationDao,
    private val sessionDao: PrayerSessionDao
) {

    // Saved Proclamations
    val allSavedProclamations: Flow<List<SavedProclamation>> = proclamationDao.getAllSavedProclamations()
    val totalLifetimeProclamations: Flow<Int?> = proclamationDao.getTotalLifetimeProclamations()
    val totalLifetimePrayerDuration: Flow<Long?> = proclamationDao.getTotalLifetimePrayerDuration()
    val totalSessionCount: Flow<Int> = proclamationDao.getTotalSessionCount()

    // Prayer Session History Logs
    val allSessionLogs: Flow<List<PrayerSessionRecord>> = sessionDao.getAllSessions()
    val totalHistoryProclamations: Flow<Int?> = sessionDao.getTotalProclamations()
    val totalHistoryDuration: Flow<Long?> = sessionDao.getTotalDurationSeconds()
    val totalHistorySessionsCount: Flow<Int> = sessionDao.getTotalSessionCount()
    val averageProclamationsPerSession: Flow<Float?> = sessionDao.getAverageProclamationsPerSession()

    fun searchProclamations(query: String): Flow<List<SavedProclamation>> {
        return proclamationDao.searchProclamationsByTopic(query)
    }

    suspend fun getProclamationById(id: Long): SavedProclamation? {
        return proclamationDao.getProclamationById(id)
    }

    suspend fun saveProclamation(proclamation: SavedProclamation): Long {
        return proclamationDao.insertProclamation(proclamation)
    }

    suspend fun updateProclamation(proclamation: SavedProclamation) {
        proclamationDao.updateProclamation(proclamation)
    }

    suspend fun deleteProclamation(proclamation: SavedProclamation) {
        proclamationDao.deleteProclamation(proclamation)
    }

    suspend fun deleteProclamationById(id: Long) {
        proclamationDao.deleteProclamationById(id)
    }

    suspend fun clearAllProclamations() {
        proclamationDao.clearAllSavedProclamations()
    }

    // Session History Methods
    suspend fun recordSession(session: PrayerSessionRecord): Long {
        return sessionDao.insertSession(session)
    }

    suspend fun updateSessionRecord(session: PrayerSessionRecord) {
        sessionDao.updateSession(session)
    }

    suspend fun deleteSessionRecord(session: PrayerSessionRecord) {
        sessionDao.deleteSession(session)
    }

    suspend fun deleteSessionRecordById(id: Long) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAllSessionHistory() {
        sessionDao.clearAllHistory()
    }
}
