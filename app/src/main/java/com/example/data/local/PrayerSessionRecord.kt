package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Entity representing a granular prayer session log for history & analytics.
 */
@Entity(tableName = "prayer_session_history")
data class PrayerSessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val sessionCount: Int,
    val durationSeconds: Long,
    val targetCount: Int = 0,
    val pacePerMinute: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val languageCode: String = "en",
    val isTargetReached: Boolean = false
) {
    /**
     * Formatted duration string (e.g. 05:30 or 01:15:00).
     */
    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    /**
     * Detailed human-readable date & time (e.g., Aug 28, 2026 • 15:30).
     */
    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    /**
     * Short time only (e.g. 15:30).
     */
    val formattedTimeOnly: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    /**
     * Short day format (e.g. Aug 28).
     */
    val formattedDateOnly: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    /**
     * Proclamation pace formatted (e.g. 24.5 / min).
     */
    val formattedPace: String
        get() {
            return if (pacePerMinute > 0) {
                String.format(Locale.getDefault(), "%.1f / min", pacePerMinute)
            } else if (durationSeconds > 0 && sessionCount > 0) {
                val calculated = (sessionCount.toFloat() / durationSeconds.toFloat()) * 60f
                String.format(Locale.getDefault(), "%.1f / min", calculated)
            } else {
                "--"
            }
        }
}
