package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Entity representing a saved prayer proclamation session/record.
 */
@Entity(tableName = "saved_proclamations")
data class SavedProclamation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val proclamationCount: Int,
    val durationSeconds: Long,
    val targetCount: Int,
    val languageCode: String = "en",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isTargetCompleted: Boolean = false
) {
    /**
     * Formatted duration in HH:MM:SS or MM:SS.
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
     * Formatted date string (e.g., Aug 28, 2026 - 14:45).
     */
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    /**
     * Short date format for statistics cards.
     */
    val shortDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
