package com.example.model

/**
 * Filter options for viewing prayer session history.
 */
enum class HistoryTimeFilter {
    ALL_TIME,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}

/**
 * Data model for daily activity bar chart representation in prayer history.
 */
data class DayPrayerActivity(
    val dayLabel: String,
    val dateString: String,
    val proclamationCount: Int,
    val durationSeconds: Long,
    val isToday: Boolean = false
) {
    val durationMinutes: Int
        get() = (durationSeconds / 60).toInt()
}
