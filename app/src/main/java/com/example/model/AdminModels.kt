package com.example.model

/**
 * Community and Global Aggregate Statistics synchronized via Firestore.
 */
data class CommunityGlobalStats(
    val totalGlobalProclamations: Long = 125430L,
    val totalGlobalDurationSeconds: Long = 486200L,
    val totalGlobalSessions: Long = 3420L,
    val totalDevoteesCount: Int = 184,
    val topTopics: List<TopicStat> = listOf(
        TopicStat("Jesus Christ is the Lord / Jésus-Christ est Seigneur", 48200L, 0.38f),
        TopicStat("The Lord is my strength and shield", 32150L, 0.25f),
        TopicStat("By His stripes we are healed", 24300L, 0.19f),
        TopicStat("No weapon formed shall prosper", 12400L, 0.10f),
        TopicStat("The Lord will fight for you", 8380L, 0.08f)
    ),
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    val formattedGlobalDuration: String
        get() {
            val hours = totalGlobalDurationSeconds / 3600
            val minutes = (totalGlobalDurationSeconds % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

/**
 * Statistics per prayer topic.
 */
data class TopicStat(
    val topic: String,
    val count: Long,
    val percentage: Float
)

/**
 * Devotee User Profile summary shown in the Admin Management Portal.
 */
data class DevoteeSummary(
    val uid: String = "",
    val displayName: String = "Prayer Devotee",
    val email: String = "",
    val photoUrl: String? = null,
    val totalProclamations: Int = 0,
    val totalPrayerTimeSeconds: Long = 0L,
    val role: String = "devotee", // "admin" or "devotee"
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean get() = role.equals("admin", ignoreCase = true)

    val formattedPrayerTime: String
        get() {
            val hours = totalPrayerTimeSeconds / 3600
            val minutes = (totalPrayerTimeSeconds % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

/**
 * Tab Navigation within the Admin Dashboard.
 */
enum class AdminDashboardTab {
    OVERVIEW,
    BROADCASTS,
    DEVOTEES,
    PRESET_TOPICS,
    FIRESTORE_STATUS
}

/**
 * Cloud Managed Prayer Topic Preset item in Firestore.
 */
data class CloudPrayerPreset(
    val id: String = "",
    val textEn: String = "",
    val textFr: String = "",
    val category: String = "General",
    val scriptureRef: String = "",
    val isFeatured: Boolean = false,
    val order: Int = 0
)
