package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Priority categories for Admin Broadcast Notifications.
 */
enum class NotificationPriority(
    val labelEn: String,
    val labelFr: String,
    val iconEmoji: String
) {
    URGENT_PRAYER("Urgent Prayer Request", "Demande de Prière Urgente", "🔥"),
    DAILY_WORD("Daily Exhortation", "Exhortation du Jour", "✨"),
    COMMUNITY_MILESTONE("Milestone Celebration", "Célébration de Victoire", "🏆"),
    GENERAL("Announcement", "Annonce Générale", "📢")
}

/**
 * Broadcast Notification sent by Administrators to all app users via Firestore.
 */
data class AdminBroadcastNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val scriptureVerse: String = "",
    val priority: NotificationPriority = NotificationPriority.GENERAL,
    val targetLanguage: String = "all", // "all", "en", "fr"
    val prayerTopicToLaunch: String = "",
    val authorName: String = "Admin Ministry",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val expirationTimestamp: Long = 0L
) {
    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val isUrgent: Boolean
        get() = priority == NotificationPriority.URGENT_PRAYER
}
