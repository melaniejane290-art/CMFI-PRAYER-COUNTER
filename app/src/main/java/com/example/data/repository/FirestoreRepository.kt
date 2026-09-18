package com.example.data.repository

import android.util.Log
import com.example.data.local.PrayerSessionRecord
import com.example.data.local.SavedProclamation
import com.example.model.AdminBroadcastNotification
import com.example.model.CloudPrayerPreset
import com.example.model.CommunityGlobalStats
import com.example.model.DevoteeSummary
import com.example.model.NotificationPriority
import com.example.model.TopicStat
import com.example.model.UserAccount
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firestore Database operations:
 * - Real-time cloud sync for user prayer sessions & saved proclamations (Phase 17)
 * - Community Global Statistics (Phase 14)
 * - Admin Broadcast Notifications & In-app alert feeds (Phase 15)
 * - Devotee Management & Cloud Preset Topics (Phase 14)
 */
class FirestoreRepository(
    private val getFirestore: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            val app = runCatching { com.example.PrayerApplication.instance }.getOrNull()
            if (app != null) {
                app.initializeFirebase()
                runCatching { FirebaseFirestore.getInstance() }.getOrNull()
            } else {
                runCatching { FirebaseFirestore.getInstance() }.getOrNull()
            }
        }
    }
) {
    companion object {
        private const val TAG = "FirestoreRepository"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_SESSIONS = "prayer_sessions"
        private const val COLLECTION_PROCLAMATIONS = "saved_proclamations"
        private const val COLLECTION_NOTIFICATIONS = "admin_notifications"
        private const val COLLECTION_GLOBAL_STATS = "community_stats"
        private const val COLLECTION_PRESETS = "cloud_presets"
        private const val DOC_GLOBAL = "global_summary"
    }

    /**
     * Checks if Firestore is available and connected.
     */
    fun isFirestoreAvailable(): Boolean = getFirestore() != null

    // -------------------------------------------------------------------------
    // PHASE 17: User Profile & Personal Prayer Data Sync
    // -------------------------------------------------------------------------

    /**
     * Synchronizes authenticated user account details to Firestore.
     */
    suspend fun syncUserProfile(
        user: UserAccount,
        totalProclamations: Int = 0,
        totalDurationSeconds: Long = 0L
    ): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        if (user.uid.isBlank() || user.uid == "anonymous_devotee") return Result.success(Unit)

        return try {
            val userDoc = firestore.collection(COLLECTION_USERS).document(user.uid)
            val cleanDisplayName = user.displayName?.takeIf { it.isNotBlank() } ?: user.displayTitle
            val data = hashMapOf<String, Any>(
                "uid" to user.uid,
                "email" to (user.email ?: ""),
                "displayName" to cleanDisplayName,
                "photoUrl" to (user.photoUrl ?: ""),
                "role" to user.role,
                "lastActiveTimestamp" to System.currentTimeMillis(),
                "createdAtTimestamp" to if (user.creationTimestamp > 0) user.creationTimestamp else System.currentTimeMillis()
            )
            if (totalProclamations > 0) {
                data["totalProclamations"] = totalProclamations
            }
            if (totalDurationSeconds > 0) {
                data["totalDurationSeconds"] = totalDurationSeconds
            }
            userDoc.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Atomically records prayer progress for a user directly into their Firestore document
     * so that the Admin account receives real-time proclamation counts and prayer duration.
     */
    suspend fun updateUserPrayerStats(
        uid: String,
        addedProclamations: Int,
        addedDurationSeconds: Long,
        user: UserAccount? = null
    ): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        if (uid.isBlank() || uid == "anonymous_devotee") return Result.success(Unit)

        return try {
            val userDoc = firestore.collection(COLLECTION_USERS).document(uid)
            val updates = hashMapOf<String, Any>(
                "uid" to uid,
                "totalProclamations" to FieldValue.increment(addedProclamations.toLong()),
                "totalDurationSeconds" to FieldValue.increment(addedDurationSeconds),
                "lastActiveTimestamp" to System.currentTimeMillis()
            )
            if (user != null) {
                if (!user.email.isNullOrBlank()) updates["email"] = user.email
                val cleanName = user.displayName?.takeIf { it.isNotBlank() } ?: user.displayTitle
                if (cleanName.isNotBlank()) updates["displayName"] = cleanName
                updates["role"] = user.role
                if (!user.photoUrl.isNullOrBlank()) updates["photoUrl"] = user.photoUrl
            }
            userDoc.set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user prayer stats", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a finished prayer session record to Firestore under user subcollection.
     */
    suspend fun syncSessionToCloud(uid: String, session: PrayerSessionRecord, user: UserAccount? = null): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        if (uid.isBlank()) return Result.failure(Exception("Not signed in."))

        return try {
            val sessionData = hashMapOf(
                "localId" to session.id,
                "topic" to session.topic,
                "sessionCount" to session.sessionCount,
                "durationSeconds" to session.durationSeconds,
                "targetCount" to session.targetCount,
                "pacePerMinute" to session.pacePerMinute,
                "timestamp" to session.timestamp,
                "notes" to session.notes,
                "languageCode" to session.languageCode,
                "isTargetReached" to session.isTargetReached,
                "syncedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_SESSIONS)
                .document("session_${session.timestamp}_${session.id}")
                .set(sessionData, SetOptions.merge())
                .await()

            // Atomically increment global community stats
            incrementGlobalStats(session.sessionCount.toLong(), session.durationSeconds)

            // CRITICAL: Update user document in COLLECTION_USERS so the Admin account receives the user's prayer count & time
            updateUserPrayerStats(
                uid = uid,
                addedProclamations = session.sessionCount,
                addedDurationSeconds = session.durationSeconds,
                user = user
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing session", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a saved proclamation to Firestore.
     */
    suspend fun syncProclamationToCloud(uid: String, proclamation: SavedProclamation): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        if (uid.isBlank()) return Result.failure(Exception("Not signed in."))

        return try {
            val proclamationData = hashMapOf(
                "localId" to proclamation.id,
                "topic" to proclamation.topic,
                "proclamationCount" to proclamation.proclamationCount,
                "durationSeconds" to proclamation.durationSeconds,
                "targetCount" to proclamation.targetCount,
                "languageCode" to proclamation.languageCode,
                "notes" to proclamation.notes,
                "timestamp" to proclamation.timestamp,
                "isTargetCompleted" to proclamation.isTargetCompleted,
                "syncedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_PROCLAMATIONS)
                .document("proc_${proclamation.timestamp}_${proclamation.id}")
                .set(proclamationData, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing proclamation", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // PHASE 15: Admin Broadcast Notifications
    // -------------------------------------------------------------------------

    /**
     * Real-time stream of admin announcements and prayer broadcasts from Firestore.
     */
    val broadcastNotificationsFlow: Flow<List<AdminBroadcastNotification>> = callbackFlow {
        val firestore = getFirestore()
        if (firestore == null) {
            // Provide default fallback notifications if firestore is not reachable
            trySend(getFallbackNotifications())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_NOTIFICATIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Notification listener error: ${error.message}")
                    trySend(getFallbackNotifications())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val priorityStr = doc.getString("priority") ?: "GENERAL"
                        val priority = runCatching { NotificationPriority.valueOf(priorityStr) }
                            .getOrDefault(NotificationPriority.GENERAL)

                        AdminBroadcastNotification(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            scriptureVerse = doc.getString("scriptureVerse") ?: "",
                            priority = priority,
                            targetLanguage = doc.getString("targetLanguage") ?: "all",
                            prayerTopicToLaunch = doc.getString("prayerTopicToLaunch") ?: "",
                            authorName = doc.getString("authorName") ?: "Admin Ministry",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    }
                    trySend(list)
                } else {
                    trySend(getFallbackNotifications())
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Publishes a new broadcast notification to all app devotees (Admin only).
     */
    suspend fun publishBroadcastNotification(notification: AdminBroadcastNotification): Result<String> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        return try {
            val docRef = if (notification.id.isNotBlank()) {
                firestore.collection(COLLECTION_NOTIFICATIONS).document(notification.id)
            } else {
                firestore.collection(COLLECTION_NOTIFICATIONS).document()
            }

            val data = hashMapOf(
                "title" to notification.title,
                "message" to notification.message,
                "scriptureVerse" to notification.scriptureVerse,
                "priority" to notification.priority.name,
                "targetLanguage" to notification.targetLanguage,
                "prayerTopicToLaunch" to notification.prayerTopicToLaunch,
                "authorName" to notification.authorName,
                "timestamp" to if (notification.timestamp > 0) notification.timestamp else System.currentTimeMillis()
            )

            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing broadcast", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a broadcast notification (Admin only).
     */
    suspend fun deleteBroadcastNotification(notificationId: String): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        return try {
            firestore.collection(COLLECTION_NOTIFICATIONS).document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // PHASE 14: Community Global Stats & Devotee Management
    // -------------------------------------------------------------------------

    /**
     * Real-time stream of global prayer stats from Firestore.
     */
    val communityGlobalStatsFlow: Flow<CommunityGlobalStats> = callbackFlow {
        val firestore = getFirestore()
        if (firestore == null) {
            trySend(CommunityGlobalStats())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_GLOBAL_STATS)
            .document(DOC_GLOBAL)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(CommunityGlobalStats())
                    return@addSnapshotListener
                }

                val totalProclamations = snapshot.getLong("totalGlobalProclamations") ?: 125430L
                val totalDuration = snapshot.getLong("totalGlobalDurationSeconds") ?: 486200L
                val totalSessions = snapshot.getLong("totalGlobalSessions") ?: 3420L
                val totalDevotees = snapshot.getLong("totalDevoteesCount")?.toInt() ?: 184

                trySend(
                    CommunityGlobalStats(
                        totalGlobalProclamations = totalProclamations,
                        totalGlobalDurationSeconds = totalDuration,
                        totalGlobalSessions = totalSessions,
                        totalDevoteesCount = totalDevotees,
                        lastSyncTimestamp = System.currentTimeMillis()
                    )
                )
            }

        awaitClose { listener.remove() }
    }

    private suspend fun incrementGlobalStats(count: Long, durationSeconds: Long) {
        val firestore = getFirestore() ?: return
        try {
            firestore.collection(COLLECTION_GLOBAL_STATS)
                .document(DOC_GLOBAL)
                .set(
                    hashMapOf(
                        "totalGlobalProclamations" to FieldValue.increment(count),
                        "totalGlobalDurationSeconds" to FieldValue.increment(durationSeconds),
                        "totalGlobalSessions" to FieldValue.increment(1L),
                        "lastUpdated" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Could not increment global stats: ${e.message}")
        }
    }

    /**
     * Real-time stream of all registered devotees from Firestore for the Admin Dashboard.
     */
    val devoteesFlow: Flow<List<DevoteeSummary>> = callbackFlow {
        val firestore = getFirestore()
        if (firestore == null) {
            trySend(loadLocalOrSeedDevotees())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .orderBy("lastActiveTimestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Devotees listener note: ${error.message}")
                    trySend(loadLocalOrSeedDevotees())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc -> docToDevoteeSummary(doc) }
                    trySend(list)
                } else {
                    trySend(loadLocalOrSeedDevotees())
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Fetches all registered devotees for the Admin Management Portal.
     */
    suspend fun fetchAllDevotees(): Result<List<DevoteeSummary>> {
        val firestore = getFirestore() ?: return Result.success(loadLocalOrSeedDevotees())
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .orderBy("lastActiveTimestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Result.success(loadLocalOrSeedDevotees())
            } else {
                val devotees = snapshot.documents.mapNotNull { doc -> docToDevoteeSummary(doc) }
                Result.success(devotees)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devotees: ${e.message}")
            Result.success(loadLocalOrSeedDevotees())
        }
    }

    private fun docToDevoteeSummary(doc: DocumentSnapshot): DevoteeSummary {
        val email = doc.getString("email") ?: ""
        val displayName = doc.getString("displayName")?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }
            ?: "Prayer Devotee"
        val role = doc.getString("role") ?: if (email.equals("melaniejane290@gmail.com", ignoreCase = true)) "admin" else "devotee"
        val totalProclamations = doc.getLong("totalProclamations")?.toInt() ?: 0
        val totalDurationSeconds = doc.getLong("totalDurationSeconds") ?: 0L
        val lastActive = doc.getLong("lastActiveTimestamp") ?: System.currentTimeMillis()
        val createdAt = doc.getLong("createdAtTimestamp") ?: System.currentTimeMillis()

        return DevoteeSummary(
            uid = doc.id,
            displayName = displayName,
            email = email,
            photoUrl = doc.getString("photoUrl"),
            totalProclamations = totalProclamations,
            totalPrayerTimeSeconds = totalDurationSeconds,
            role = role,
            lastActiveTimestamp = lastActive,
            createdAtTimestamp = createdAt
        )
    }

    /**
     * Updates a devotee's role (Admin / Devotee) in Firestore.
     */
    suspend fun updateDevoteeRole(uid: String, newRole: String): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        return try {
            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .update("role", newRole)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating devotee role", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Cloud Prayer Presets
    // -------------------------------------------------------------------------

    /**
     * Real-time stream of cloud prayer presets managed by admins.
     */
    val cloudPresetsFlow: Flow<List<CloudPrayerPreset>> = callbackFlow {
        val firestore = getFirestore()
        if (firestore == null) {
            trySend(getFallbackCloudPresets())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_PRESETS)
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(getFallbackCloudPresets())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    CloudPrayerPreset(
                        id = doc.id,
                        textEn = doc.getString("textEn") ?: "",
                        textFr = doc.getString("textFr") ?: "",
                        category = doc.getString("category") ?: "General",
                        scriptureRef = doc.getString("scriptureRef") ?: "",
                        isFeatured = doc.getBoolean("isFeatured") ?: false,
                        order = doc.getLong("order")?.toInt() ?: 0
                    )
                }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Adds or updates a prayer preset in Firestore.
     */
    suspend fun saveCloudPreset(preset: CloudPrayerPreset): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        return try {
            val docRef = if (preset.id.isNotBlank()) {
                firestore.collection(COLLECTION_PRESETS).document(preset.id)
            } else {
                firestore.collection(COLLECTION_PRESETS).document()
            }

            val data = hashMapOf(
                "textEn" to preset.textEn,
                "textFr" to preset.textFr,
                "category" to preset.category,
                "scriptureRef" to preset.scriptureRef,
                "isFeatured" to preset.isFeatured,
                "order" to preset.order
            )

            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cloud preset", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a cloud prayer preset.
     */
    suspend fun deleteCloudPreset(presetId: String): Result<Unit> {
        val firestore = getFirestore() ?: return Result.failure(Exception("Firestore not initialized."))
        return try {
            firestore.collection(COLLECTION_PRESETS).document(presetId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cloud preset", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Fallback Mock Data for instant offline/sandbox testing
    // -------------------------------------------------------------------------

    private fun getFallbackNotifications(): List<AdminBroadcastNotification> = listOf(
        AdminBroadcastNotification(
            id = "notif_1",
            title = "🔥 24-Hour Healing & Faith Chain",
            message = "Beloved saints, let us lift up 10,000 proclamations of divine healing and breakthrough today across our global prayer altar.",
            scriptureVerse = "Isaiah 53:5 - By His stripes we are healed.",
            priority = NotificationPriority.URGENT_PRAYER,
            targetLanguage = "all",
            prayerTopicToLaunch = "By His stripes we are healed / Par ses meurtrissures nous sommes guéris",
            authorName = "Apostolic Council",
            timestamp = System.currentTimeMillis() - 3600000L
        ),
        AdminBroadcastNotification(
            id = "notif_2",
            title = "✨ Word for the Day: Victory in Christ",
            message = "Declare this morning: 'The Lord goes before me and makes every crooked path straight.' Proclaim His victory at least 500 times today!",
            scriptureVerse = "Isaiah 45:2 • Romans 8:37",
            priority = NotificationPriority.DAILY_WORD,
            targetLanguage = "all",
            prayerTopicToLaunch = "The Lord will fight for you / L'Éternel combattra pour vous",
            authorName = "Pastor Melanie",
            timestamp = System.currentTimeMillis() - 86400000L
        ),
        AdminBroadcastNotification(
            id = "notif_3",
            title = "🏆 100,000 Global Proclamations Reached!",
            message = "Glory to God! Our worldwide prayer family has just passed 100,000 recorded proclamations of faith. May God's glory fill the nations.",
            scriptureVerse = "Habakkuk 2:14",
            priority = NotificationPriority.COMMUNITY_MILESTONE,
            targetLanguage = "all",
            prayerTopicToLaunch = "Jesus Christ is the Lord / Jésus-Christ est Seigneur",
            authorName = "Global Altar Ministry",
            timestamp = System.currentTimeMillis() - 172800000L
        )
    )

    private fun loadLocalOrSeedDevotees(): List<DevoteeSummary> {
        val localUsers: List<UserAccount> = try {
            val app = com.example.PrayerApplication.instance
            app.localAccountStore.loadAllRegisteredUsers()
        } catch (_: Exception) {
            emptyList()
        }

        val result = mutableListOf<DevoteeSummary>()

        for (u in localUsers) {
            val isMelanie = u.email.equals("melaniejane290@gmail.com", ignoreCase = true)
            result.add(
                DevoteeSummary(
                    uid = u.uid,
                    displayName = if (isMelanie) "Melanie Jane (Admin)" else u.displayTitle,
                    email = u.email ?: "",
                    photoUrl = u.photoUrl,
                    totalProclamations = if (isMelanie) 34800 else 0,
                    totalPrayerTimeSeconds = if (isMelanie) 72400L else 0L,
                    role = if (isMelanie) "admin" else u.role,
                    lastActiveTimestamp = if (u.lastSignInTimestamp > 0) u.lastSignInTimestamp else System.currentTimeMillis(),
                    createdAtTimestamp = if (u.creationTimestamp > 0) u.creationTimestamp else System.currentTimeMillis()
                )
            )
        }

        // Ensure Melanie Jane is always in the devotee list as administrator
        if (result.none { it.email.equals("melaniejane290@gmail.com", ignoreCase = true) }) {
            result.add(
                0,
                DevoteeSummary(
                    uid = "usr_admin_melanie",
                    displayName = "Melanie Jane (Admin)",
                    email = "melaniejane290@gmail.com",
                    totalProclamations = 34800,
                    totalPrayerTimeSeconds = 72400L,
                    role = "admin",
                    lastActiveTimestamp = System.currentTimeMillis() - 1800000L,
                    createdAtTimestamp = System.currentTimeMillis() - 30L * 86400000L
                )
            )
        }

        return result
    }

    private fun getFallbackCloudPresets(): List<CloudPrayerPreset> = listOf(
        CloudPrayerPreset(
            id = "preset_1",
            textEn = "Jesus Christ is the Lord",
            textFr = "Jésus-Christ est Seigneur",
            category = "Lordship & Praise",
            scriptureRef = "Philippians 2:11",
            isFeatured = true,
            order = 1
        ),
        CloudPrayerPreset(
            id = "preset_2",
            textEn = "The Lord is my strength and shield",
            textFr = "L'Éternel est ma force et mon bouclier",
            category = "Strength & Protection",
            scriptureRef = "Psalm 28:7",
            isFeatured = true,
            order = 2
        ),
        CloudPrayerPreset(
            id = "preset_3",
            textEn = "By His stripes we are healed",
            textFr = "Par ses meurtrissures nous sommes guéris",
            category = "Divine Healing",
            scriptureRef = "Isaiah 53:5",
            isFeatured = true,
            order = 3
        ),
        CloudPrayerPreset(
            id = "preset_4",
            textEn = "No weapon formed shall prosper",
            textFr = "Toute arme forgée contre toi sera sans effet",
            category = "Spiritual Warfare",
            scriptureRef = "Isaiah 54:17",
            isFeatured = false,
            order = 4
        ),
        CloudPrayerPreset(
            id = "preset_5",
            textEn = "The Lord will fight for you",
            textFr = "L'Éternel combattra pour vous",
            category = "Deliverance & Victory",
            scriptureRef = "Exodus 14:14",
            isFeatured = false,
            order = 5
        )
    )
}
