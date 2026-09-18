package com.example.model

/**
 * Supported application languages.
 */
enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇺🇸"),
    FRENCH("fr", "Français", "🇫🇷")
}

/**
 * Represents the state of the active prayer proclamation and counter.
 */
data class PrayerCounterState(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val prayerTopic: String = "Jesus Christ is the Lord",
    val targetCount: Int = 1000,
    val savedCumulativeCount: Int = 0,
    val currentSessionCount: Int = 0,
    val sessionDurationSeconds: Long = 0L,
    val isTimerRunning: Boolean = false,
    val showSaveSuccessBanner: Boolean = false,
    val saveMessage: String = "",
    val showResetConfirmation: Boolean = false
) {
    /**
     * Total cumulative proclamation count across all sessions.
     */
    val totalCount: Int
        get() = savedCumulativeCount + currentSessionCount

    /**
     * Progress percentage from 0.0 to 1.0.
     */
    val progressFraction: Float
        get() = if (targetCount > 0) {
            (totalCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
        } else 0f

    /**
     * Progress percentage formatted as an integer (e.g., 75%).
     */
    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()

    /**
     * Whether the user has reached or exceeded their proclamation target.
     */
    val isTargetReached: Boolean
        get() = targetCount > 0 && totalCount >= targetCount

    /**
     * Formats session seconds into HH:MM:SS format.
     */
    val formattedSessionTime: String
        get() {
            val hours = sessionDurationSeconds / 3600
            val minutes = (sessionDurationSeconds % 3600) / 60
            val seconds = sessionDurationSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
}

object PrayerPresets {
    val englishTopics = listOf(
        "Jesus Christ is the Lord",
        "The Lord is my strength and shield",
        "By His stripes we are healed",
        "No weapon formed shall prosper",
        "The Lord will fight for you"
    )

    val frenchTopics = listOf(
        "Jésus-Christ est Seigneur",
        "L'Éternel est ma force et mon bouclier",
        "Par ses meurtrissures nous sommes guéris",
        "Toute arme forgée contre toi sera sans effet",
        "L'Éternel combattra pour vous"
    )

    fun getTopicsFor(language: AppLanguage): List<String> {
        return when (language) {
            AppLanguage.ENGLISH -> englishTopics
            AppLanguage.FRENCH -> frenchTopics
        }
    }

    val targets = listOf(50, 100, 500, 1000, 2500, 5000, 10000)
}
