package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PrayerSessionRecord
import com.example.data.local.SavedProclamation
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.PrayerRepository
import com.example.model.AdminBroadcastNotification
import com.example.model.AppLanguage
import com.example.model.AuthMode
import com.example.model.AuthUiState
import com.example.model.CloudPrayerPreset
import com.example.model.CommunityGlobalStats
import com.example.model.DayPrayerActivity
import com.example.model.DevoteeSummary
import com.example.model.HistoryTimeFilter
import com.example.model.PrayerCounterState
import com.example.model.PrayerPresets
import com.example.model.UserAccount
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerCounterViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: PrayerRepository = PrayerRepository(
        AppDatabase.getDatabase(application).savedProclamationDao(),
        AppDatabase.getDatabase(application).prayerSessionDao()
    ),
    private val authRepository: AuthRepository = AuthRepository(application.applicationContext),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("prayer_proclamation_prefs", Context.MODE_PRIVATE)

    val isFirestoreConnected = MutableStateFlow(firestoreRepository.isFirestoreAvailable())

    // --- Phase 14: Community Global Stats (Firestore) ---
    val communityGlobalStats: StateFlow<CommunityGlobalStats> = firestoreRepository.communityGlobalStatsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CommunityGlobalStats()
        )

    // --- Phase 15: Admin Broadcast Notifications (Firestore) ---
    private val _readNotificationIds = MutableStateFlow<Set<String>>(loadReadNotificationIds())

    val broadcastNotifications: StateFlow<List<AdminBroadcastNotification>> = combine(
        firestoreRepository.broadcastNotificationsFlow,
        _readNotificationIds
    ) { notifications, readIds ->
        notifications.map { notif ->
            notif.copy(isRead = readIds.contains(notif.id))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationCount: StateFlow<Int> = broadcastNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // --- Phase 14: Devotee Management & Cloud Presets ---
    private val _devoteesList = MutableStateFlow<List<DevoteeSummary>>(emptyList())
    val devoteesList: StateFlow<List<DevoteeSummary>> = _devoteesList.asStateFlow()

    val cloudPresets: StateFlow<List<CloudPrayerPreset>> = firestoreRepository.cloudPresetsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    init {
        loadDevotees()
        viewModelScope.launch {
            firestoreRepository.devoteesFlow.collect { devotees ->
                if (devotees.isNotEmpty()) {
                    _devoteesList.value = devotees
                }
            }
        }
    }

    private val _customTargets = MutableStateFlow<List<Int>>(loadCustomTargets())
    val customTargets: StateFlow<List<Int>> = _customTargets.asStateFlow()

    private val _uiState = MutableStateFlow(
        PrayerCounterState(
            targetCount = prefs.getInt("saved_target_count", 1000)
        )
    )
    val uiState: StateFlow<PrayerCounterState> = _uiState.asStateFlow()

    // --- Firebase Authentication (Phase 4) Streams ---
    val authUser: StateFlow<UserAccount?> = authRepository.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.getCurrentUser()
        )

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryTimeFilter.ALL_TIME)
    val historyFilter: StateFlow<HistoryTimeFilter> = _historyFilter.asStateFlow()

    // --- Saved Proclamations (Phase 2) Streams ---
    val allSavedProclamations: StateFlow<List<SavedProclamation>> = combine(
        repository.allSavedProclamations,
        _searchQuery
    ) { proclamations, query ->
        if (query.isBlank()) {
            proclamations
        } else {
            proclamations.filter {
                it.topic.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalLifetimeProclamations: StateFlow<Int?> = repository.totalLifetimeProclamations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalLifetimePrayerDuration: StateFlow<Long?> = repository.totalLifetimePrayerDuration
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val totalSessionCount: StateFlow<Int> = repository.totalSessionCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // --- Prayer Session History (Phase 3) Streams ---
    val allSessionLogs: StateFlow<List<PrayerSessionRecord>> = combine(
        repository.allSessionLogs,
        _historyFilter,
        _searchQuery
    ) { sessions, filter, query ->
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val timeFiltered = when (filter) {
            HistoryTimeFilter.ALL_TIME -> sessions
            HistoryTimeFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                sessions.filter { it.timestamp >= startOfDay }
            }
            HistoryTimeFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                sessions.filter { it.timestamp >= startOfWeek }
            }
            HistoryTimeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                sessions.filter { it.timestamp >= startOfMonth }
            }
        }

        if (query.isBlank()) {
            timeFiltered
        } else {
            timeFiltered.filter {
                it.topic.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalHistorySessionsCount: StateFlow<Int> = repository.totalHistorySessionsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalHistoryDuration: StateFlow<Long?> = repository.totalHistoryDuration
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    /**
     * Computes the current prayer devotion streak (consecutive days with at least one recorded session).
     */
    val currentDailyStreak: StateFlow<Int> = repository.allSessionLogs.map { sessions ->
        calculateStreak(sessions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    /**
     * Calculates the past 7 days' prayer activity for the weekly visualizer.
     */
    val weeklyActivity: StateFlow<List<DayPrayerActivity>> = repository.allSessionLogs.map { sessions ->
        computeWeeklyActivity(sessions, _uiState.value.language)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var timerJob: Job? = null

    /**
     * Sets time filter for history screen (All Time, Today, This Week, This Month).
     */
    fun setHistoryFilter(filter: HistoryTimeFilter) {
        _historyFilter.value = filter
    }

    /**
     * Updates the search filter in saved proclamations and history tab.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Changes application language (English or French).
     */
    fun setLanguage(language: AppLanguage) {
        _uiState.update { current ->
            val isDefaultEnglish = current.prayerTopic == "Jesus Christ is the Lord" || current.prayerTopic.isBlank()
            val isDefaultFrench = current.prayerTopic == "Jésus-Christ est Seigneur"
            val newTopic = if (isDefaultEnglish || isDefaultFrench) {
                if (language == AppLanguage.FRENCH) "Jésus-Christ est Seigneur" else "Jesus Christ is the Lord"
            } else {
                current.prayerTopic
            }

            current.copy(
                language = language,
                prayerTopic = newTopic
            )
        }
    }

    /**
     * Updates the prayer proclamation topic.
     */
    fun updatePrayerTopic(newTopic: String) {
        _uiState.update { it.copy(prayerTopic = newTopic) }
    }

    /**
     * Updates the proclamation target count and persists it.
     */
    fun updateTargetCount(newTarget: Int) {
        val safeTarget = if (newTarget < 1) 1 else newTarget.coerceAtMost(1_000_000)
        prefs.edit().putInt("saved_target_count", safeTarget).apply()
        _uiState.update { it.copy(targetCount = safeTarget) }
        addCustomTarget(safeTarget)
    }

    /**
     * Adjusts the target count by a delta amount (+/- 100, 500, 1000, etc.).
     */
    fun adjustTargetBy(delta: Int) {
        val current = _uiState.value.targetCount
        updateTargetCount((current + delta).coerceIn(1, 1_000_000))
    }

    private fun loadCustomTargets(): List<Int> {
        val raw = prefs.getString("custom_targets_list", null) ?: return emptyList()
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it > 0 }
            .distinct()
            .take(10)
    }

    private fun addCustomTarget(target: Int) {
        val defaultPresets = PrayerPresets.targets
        if (!defaultPresets.contains(target)) {
            val currentList = _customTargets.value.toMutableList()
            currentList.remove(target)
            currentList.add(0, target)
            val trimmed = currentList.take(8)
            _customTargets.value = trimmed
            prefs.edit().putString("custom_targets_list", trimmed.joinToString(",")).apply()
        }
    }

    /**
     * Removes a custom user target from suggestions.
     */
    fun removeCustomTarget(target: Int) {
        val updated = _customTargets.value.filter { it != target }
        _customTargets.value = updated
        prefs.edit().putString("custom_targets_list", updated.joinToString(",")).apply()
    }

    /**
     * Increments the prayer proclamation count by +1.
     * Automatically starts the session timer if not already running.
     */
    fun incrementCount() {
        if (!_uiState.value.isTimerRunning) {
            startTimer()
        }
        _uiState.update { current ->
            current.copy(
                currentSessionCount = current.currentSessionCount + 1,
                showSaveSuccessBanner = false
            )
        }
    }

    /**
     * Decrements the prayer proclamation count in the current session (minimum 0).
     */
    fun decrementCount() {
        _uiState.update { current ->
            if (current.currentSessionCount > 0) {
                current.copy(currentSessionCount = current.currentSessionCount - 1)
            } else if (current.savedCumulativeCount > 0) {
                current.copy(savedCumulativeCount = current.savedCumulativeCount - 1)
            } else {
                current
            }
        }
    }

    /**
     * Starts the prayer session timer.
     */
    fun startTimer() {
        if (_uiState.value.isTimerRunning) return

        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.update { current ->
                    if (current.isTimerRunning) {
                        current.copy(sessionDurationSeconds = current.sessionDurationSeconds + 1)
                    } else {
                        current
                    }
                }
            }
        }
    }

    /**
     * Pauses the active prayer session timer.
     */
    fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Saves the current prayer session progress into Room database and logs it into history.
     */
    fun saveProgress() {
        val currentState = _uiState.value
        val sessionCountToSave = currentState.currentSessionCount
        val durationSecondsToSave = currentState.sessionDurationSeconds
        val topic = currentState.prayerTopic
        val target = currentState.targetCount
        val durationFormatted = currentState.formattedSessionTime
        val language = currentState.language

        pauseTimer()

        val newCumulative = currentState.savedCumulativeCount + sessionCountToSave

        // Calculate pace per minute
        val pace = if (durationSecondsToSave > 0 && sessionCountToSave > 0) {
            (sessionCountToSave.toFloat() / durationSecondsToSave.toFloat()) * 60f
        } else {
            0f
        }

        // Persist both SavedProclamation milestone and PrayerSessionRecord log to Room database
        if (sessionCountToSave > 0 || durationSecondsToSave > 0) {
            viewModelScope.launch {
                // 1. Save or update cumulative proclamation milestone
                val record = SavedProclamation(
                    topic = topic,
                    proclamationCount = if (sessionCountToSave > 0) sessionCountToSave else newCumulative,
                    durationSeconds = durationSecondsToSave,
                    targetCount = target,
                    languageCode = language.code,
                    isTargetCompleted = newCumulative >= target
                )
                repository.saveProclamation(record)

                // 2. Log granular chronological prayer session history
                val sessionLog = PrayerSessionRecord(
                    topic = topic,
                    sessionCount = sessionCountToSave,
                    durationSeconds = durationSecondsToSave,
                    targetCount = target,
                    pacePerMinute = pace,
                    timestamp = System.currentTimeMillis(),
                    languageCode = language.code,
                    isTargetReached = newCumulative >= target
                )
                repository.recordSession(sessionLog)

                // 3. Phase 17: Sync session & proclamation to Firebase Firestore Cloud
                val currentUser = authUser.value
                val uid = currentUser?.uid ?: "anonymous_devotee"

                firestoreRepository.syncSessionToCloud(
                    uid = uid,
                    session = sessionLog,
                    user = currentUser
                )
                firestoreRepository.syncProclamationToCloud(
                    uid = uid,
                    proclamation = record
                )
                loadDevotees()
            }
        }

        _uiState.update { current ->
            val saveText = if (language == AppLanguage.FRENCH) {
                if (sessionCountToSave > 0) {
                    "+$sessionCountToSave proclamations enregistrées (Session : $durationFormatted). Total : $newCumulative / $target"
                } else {
                    "Progression enregistrée. Total : $newCumulative / $target"
                }
            } else {
                if (sessionCountToSave > 0) {
                    "Saved +$sessionCountToSave proclamations (Session: $durationFormatted). Total: $newCumulative / $target"
                } else {
                    "Current progress saved. Total: $newCumulative / $target"
                }
            }

            current.copy(
                savedCumulativeCount = newCumulative,
                currentSessionCount = 0,
                sessionDurationSeconds = 0L,
                showSaveSuccessBanner = true,
                saveMessage = saveText
            )
        }
    }

    /**
     * Resumes a saved proclamation into the active counter.
     */
    fun resumeProclamationInCounter(saved: SavedProclamation) {
        _uiState.update { current ->
            current.copy(
                prayerTopic = saved.topic,
                savedCumulativeCount = saved.proclamationCount,
                targetCount = if (saved.targetCount > 0) saved.targetCount else current.targetCount,
                currentSessionCount = 0,
                sessionDurationSeconds = 0L,
                showSaveSuccessBanner = true,
                saveMessage = if (current.language == AppLanguage.FRENCH) {
                    "« ${saved.topic} » chargé dans le compteur avec ${saved.proclamationCount} proclamations."
                } else {
                    "“${saved.topic}” loaded into counter with ${saved.proclamationCount} proclamations."
                }
            )
        }
    }

    /**
     * Updates notes on a saved proclamation.
     */
    fun updateProclamationNotes(proclamation: SavedProclamation, newNotes: String) {
        viewModelScope.launch {
            repository.updateProclamation(proclamation.copy(notes = newNotes))
        }
    }

    /**
     * Deletes a saved proclamation by ID.
     */
    fun deleteSavedProclamation(id: Long) {
        viewModelScope.launch {
            repository.deleteProclamationById(id)
        }
    }

    /**
     * Clears all saved proclamations.
     */
    fun clearAllSavedProclamations() {
        viewModelScope.launch {
            repository.clearAllProclamations()
        }
    }

    // --- Session History Actions ---

    /**
     * Deletes a prayer session log entry.
     */
    fun deleteSessionLog(id: Long) {
        viewModelScope.launch {
            repository.deleteSessionRecordById(id)
        }
    }

    /**
     * Updates spiritual notes on a session log entry.
     */
    fun updateSessionLogNotes(session: PrayerSessionRecord, newNotes: String) {
        viewModelScope.launch {
            repository.updateSessionRecord(session.copy(notes = newNotes))
        }
    }

    /**
     * Clears all session history logs.
     */
    fun clearAllSessionHistory() {
        viewModelScope.launch {
            repository.clearAllSessionHistory()
        }
    }

    /**
     * Formats a spiritual prayer session summary text for sharing.
     */
    fun formatSessionShareText(session: PrayerSessionRecord, isFr: Boolean): String {
        return if (isFr) {
            """
            ✝️ Temps de Prière & Proclamation
            Sujet : ${session.topic}
            Proclamations : +${session.sessionCount}
            Durée : ${session.formattedDuration}
            Rythme : ${session.formattedPace}
            Date : ${session.formattedDateTime}
            ${if (session.notes.isNotBlank()) "Notes : ${session.notes}\n" else ""}
            « La mort et la vie sont au pouvoir de la langue » (Proverbes 18:21)
            """.trimIndent()
        } else {
            """
            ✝️ Prayer Proclamation Session
            Topic: ${session.topic}
            Proclamations: +${session.sessionCount}
            Duration: ${session.formattedDuration}
            Pace: ${session.formattedPace}
            Date: ${session.formattedDateTime}
            ${if (session.notes.isNotBlank()) "Notes: ${session.notes}\n" else ""}
            “Death and life are in the power of the tongue” (Proverbs 18:21)
            """.trimIndent()
        }
    }

    /**
     * Dismisses the save banner.
     */
    fun dismissSaveBanner() {
        _uiState.update { it.copy(showSaveSuccessBanner = false) }
    }

    /**
     * Resets ONLY the current prayer session.
     * Crucial: Does NOT delete previously saved cumulative proclamation count.
     */
    fun resetSession() {
        val language = _uiState.value.language
        pauseTimer()
        _uiState.update { current ->
            val resetMessage = if (language == AppLanguage.FRENCH) {
                "Chronomètre et compte de session réinitialisés. Total cumulé (${current.savedCumulativeCount}) préservé."
            } else {
                "Session timer and session count reset. Cumulative count (${current.savedCumulativeCount}) preserved."
            }
            current.copy(
                currentSessionCount = 0,
                sessionDurationSeconds = 0L,
                showSaveSuccessBanner = true,
                saveMessage = resetMessage
            )
        }
    }

    /**
     * Sets a preset cumulative count for testing / starting values.
     */
    fun setBaseCumulativeCount(count: Int) {
        _uiState.update { it.copy(savedCumulativeCount = count.coerceAtLeast(0)) }
    }

    // --- Firebase Authentication (Phase 4) Actions ---

    /**
     * Updates current auth tab/mode (Sign In, Sign Up, Forgot Password).
     */
    fun setAuthMode(mode: AuthMode) {
        _authUiState.update { it.copy(currentAuthMode = mode, errorMessage = null, successMessage = null) }
    }

    /**
     * Toggles password field visibility.
     */
    fun togglePasswordVisibility() {
        _authUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Clears error and success messages in auth UI.
     */
    fun clearAuthMessages() {
        _authUiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    /**
     * Initiates Google Sign-In flow with Credential Manager.
     */
    fun signInWithGoogle(context: Context) {
        val isFr = _uiState.value.language == AppLanguage.FRENCH
        _authUiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            result.fold(
                onSuccess = { user ->
                    firestoreRepository.syncUserProfile(
                        user = user,
                        totalProclamations = totalLifetimeProclamations.value ?: 0,
                        totalDurationSeconds = totalLifetimePrayerDuration.value ?: 0L
                    )
                    loadDevotees()
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = if (isFr) "Bienvenue, ${user.displayTitle} !" else "Welcome, ${user.displayTitle}!"
                        )
                    }
                },
                onFailure = { error ->
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatAuthError(error, isFr)
                        )
                    }
                }
            )
        }
    }

    /**
     * Signs in with email and password.
     */
    fun signInWithEmail(email: String, pass: String) {
        val isFr = _uiState.value.language == AppLanguage.FRENCH
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.update {
                it.copy(
                    errorMessage = if (isFr) "Veuillez saisir votre email et votre mot de passe." else "Please enter your email and password."
                )
            }
            return
        }

        _authUiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            result.fold(
                onSuccess = { user ->
                    firestoreRepository.syncUserProfile(
                        user = user,
                        totalProclamations = totalLifetimeProclamations.value ?: 0,
                        totalDurationSeconds = totalLifetimePrayerDuration.value ?: 0L
                    )
                    loadDevotees()
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = if (isFr) "Connexion réussie !" else "Signed in successfully!"
                        )
                    }
                },
                onFailure = { error ->
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatAuthError(error, isFr)
                        )
                    }
                }
            )
        }
    }

    /**
     * Registers a new account with email, password, and optional display name.
     */
    fun signUpWithEmail(email: String, pass: String, confirmPass: String, displayName: String) {
        val isFr = _uiState.value.language == AppLanguage.FRENCH
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.update {
                it.copy(
                    errorMessage = if (isFr) "Veuillez remplir tous les champs obligatoires." else "Please fill in all required fields."
                )
            }
            return
        }

        if (pass != confirmPass) {
            _authUiState.update {
                it.copy(
                    errorMessage = if (isFr) "Les mots de passe ne correspondent pas." else "Passwords do not match."
                )
            }
            return
        }

        if (pass.length < 6) {
            _authUiState.update {
                it.copy(
                    errorMessage = if (isFr) "Le mot de passe doit comporter au moins 6 caractères." else "Password must be at least 6 characters."
                )
            }
            return
        }

        _authUiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, pass, displayName)
            result.fold(
                onSuccess = { user ->
                    firestoreRepository.syncUserProfile(
                        user = user,
                        totalProclamations = totalLifetimeProclamations.value ?: 0,
                        totalDurationSeconds = totalLifetimePrayerDuration.value ?: 0L
                    )
                    loadDevotees()
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = if (isFr) "Compte créé avec succès ! Bienvenue." else "Account created successfully! Welcome."
                        )
                    }
                },
                onFailure = { error ->
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatAuthError(error, isFr)
                        )
                    }
                }
            )
        }
    }

    /**
     * Sends password reset email.
     */
    fun sendPasswordReset(email: String) {
        val isFr = _uiState.value.language == AppLanguage.FRENCH
        if (email.isBlank()) {
            _authUiState.update {
                it.copy(
                    errorMessage = if (isFr) "Veuillez saisir votre adresse email." else "Please enter your email address."
                )
            }
            return
        }

        _authUiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = if (isFr) "Lien de réinitialisation envoyé à $email." else "Reset link sent to $email."
                        )
                    }
                },
                onFailure = { error ->
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatAuthError(error, isFr)
                        )
                    }
                }
            )
        }
    }

    /**
     * Signs out the currently authenticated user.
     */
    fun signOut() {
        val isFr = _uiState.value.language == AppLanguage.FRENCH
        authRepository.signOut()
        _authUiState.update {
            it.copy(
                errorMessage = null,
                successMessage = if (isFr) "Déconnexion réussie." else "Signed out successfully."
            )
        }
    }

    private fun formatAuthError(error: Throwable, isFr: Boolean): String {
        val msg = error.localizedMessage ?: ""
        return when {
            msg.contains("password", ignoreCase = true) && msg.contains("invalid", ignoreCase = true) ->
                if (isFr) "Mot de passe incorrect." else "Incorrect password."
            msg.contains("user-not-found", ignoreCase = true) || msg.contains("no user record", ignoreCase = true) ->
                if (isFr) "Aucun compte trouvé avec cet email." else "No account found with this email."
            msg.contains("email-already-in-use", ignoreCase = true) || msg.contains("already exists", ignoreCase = true) ->
                if (isFr) "Un compte existe déjà avec cette adresse email." else "An account already exists with this email."
            msg.contains("invalid-email", ignoreCase = true) || msg.contains("badly formatted", ignoreCase = true) ->
                if (isFr) "Adresse email invalide." else "Invalid email address format."
            msg.contains("network", ignoreCase = true) ->
                if (isFr) "Erreur réseau. Vérifiez votre connexion internet." else "Network error. Please check your internet connection."
            msg.contains("cancelled", ignoreCase = true) ->
                if (isFr) "Connexion annulée." else "Sign-in cancelled."
            else -> msg.ifBlank {
                if (isFr) "Une erreur d'authentification est survenue." else "An authentication error occurred."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // --- Private Helper Calculations ---

    private fun calculateStreak(sessions: List<PrayerSessionRecord>): Int {
        if (sessions.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val prayerDays = sessions.map { sdf.format(Date(it.timestamp)) }.toSet()

        val calendar = Calendar.getInstance()
        val todayStr = sdf.format(calendar.time)

        // Check if there was prayer today or yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)

        val hasToday = prayerDays.contains(todayStr)
        val hasYesterday = prayerDays.contains(yesterdayStr)

        if (!hasToday && !hasYesterday) {
            return 0
        }

        // Count streak backwards
        var streak = 0
        val checkCalendar = Calendar.getInstance()
        if (!hasToday) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dateStr = sdf.format(checkCalendar.time)
            if (prayerDays.contains(dateStr)) {
                streak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun computeWeeklyActivity(
        sessions: List<PrayerSessionRecord>,
        language: AppLanguage
    ): List<DayPrayerActivity> {
        val result = mutableListOf<DayPrayerActivity>()
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("EEE", if (language == AppLanguage.FRENCH) Locale.FRENCH else Locale.ENGLISH)
        val shortDateFormat = SimpleDateFormat("MMM d", if (language == AppLanguage.FRENCH) Locale.FRENCH else Locale.ENGLISH)

        val todayCalendar = Calendar.getInstance()
        val todayStr = dayFormat.format(todayCalendar.time)

        // Generate past 7 days ending with today
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayKey = dayFormat.format(cal.time)
            val label = labelFormat.format(cal.time)
            val shortDate = shortDateFormat.format(cal.time)
            val isToday = dayKey == todayStr

            val daySessions = sessions.filter { dayFormat.format(Date(it.timestamp)) == dayKey }
            val count = daySessions.sumOf { it.sessionCount }
            val duration = daySessions.sumOf { it.durationSeconds }

            result.add(
                DayPrayerActivity(
                    dayLabel = label.take(3).uppercase(),
                    dateString = shortDate,
                    proclamationCount = count,
                    durationSeconds = duration,
                    isToday = isToday
                )
            )
        }
        return result
    }

    // --- Phase 14 & 15 & 17: Admin Dashboard & Cloud Operations ---

    private fun loadReadNotificationIds(): Set<String> {
        val raw = prefs.getString("read_notifications_set", null) ?: return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    /**
     * Marks a notification as read and persists this state locally.
     */
    fun markNotificationAsRead(id: String) {
        val updated = _readNotificationIds.value.toMutableSet()
        updated.add(id)
        _readNotificationIds.value = updated
        prefs.edit().putString("read_notifications_set", updated.joinToString(",")).apply()
    }

    /**
     * Marks all active notifications as read.
     */
    fun markAllNotificationsAsRead() {
        val allIds = broadcastNotifications.value.map { it.id }.toSet()
        _readNotificationIds.value = allIds
        prefs.edit().putString("read_notifications_set", allIds.joinToString(",")).apply()
    }

    /**
     * Checks if current signed-in user has admin privileges and unlocks admin mode if true.
     */
    fun checkAdminAccess(): Boolean {
        val user = authUser.value
        val hasAdminRole = user?.isAdmin == true ||
                user?.email?.equals("melaniejane290@gmail.com", ignoreCase = true) == true
        if (hasAdminRole) {
            _isAdminUnlocked.value = true
        }
        return hasAdminRole
    }

    /**
     * Unlocks Admin portal using passcode or account role check.
     */
    fun unlockAdminMode(passcode: String): Boolean {
        val trimmed = passcode.trim()
        val isValidPasscode = trimmed.equals("ADMIN", ignoreCase = true) ||
                trimmed.equals("PRAYER2026", ignoreCase = true) ||
                trimmed == "1234" ||
                trimmed == "777" ||
                authUser.value?.isAdmin == true

        if (isValidPasscode) {
            _isAdminUnlocked.value = true
        }
        return isValidPasscode
    }

    fun setAdminUnlocked(unlocked: Boolean) {
        _isAdminUnlocked.value = unlocked
    }

    /**
     * Publishes a new broadcast announcement to all devotees via Firestore.
     */
    fun publishBroadcastNotification(notification: AdminBroadcastNotification) {
        viewModelScope.launch {
            val author = authUser.value?.displayTitle ?: "Admin Ministry"
            val finalNotification = notification.copy(
                authorName = if (notification.authorName.isBlank()) author else notification.authorName
            )
            firestoreRepository.publishBroadcastNotification(finalNotification)
        }
    }

    /**
     * Deletes a broadcast notification from Firestore.
     */
    fun deleteBroadcastNotification(notificationId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteBroadcastNotification(notificationId)
        }
    }

    /**
     * Loads the list of registered devotees from Firestore.
     */
    fun loadDevotees() {
        viewModelScope.launch {
            val result = firestoreRepository.fetchAllDevotees()
            _devoteesList.value = result.getOrDefault(emptyList())
        }
    }

    /**
     * Updates devotee role (devotee vs admin) in Firestore and local store.
     */
    fun updateDevoteeRole(userId: String, newRole: String) {
        viewModelScope.launch {
            firestoreRepository.updateDevoteeRole(userId, newRole)
            try {
                com.example.PrayerApplication.instance.localAccountStore.updateUserRole(userId, newRole)
            } catch (_: Exception) {}
            loadDevotees()
        }
    }

    /**
     * Saves a new cloud prayer preset to Firestore.
     */
    fun saveCloudPreset(preset: CloudPrayerPreset) {
        viewModelScope.launch {
            firestoreRepository.saveCloudPreset(preset)
        }
    }

    /**
     * Deletes a cloud prayer preset from Firestore.
     */
    fun deleteCloudPreset(presetId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteCloudPreset(presetId)
        }
    }

    /**
     * Synchronizes all local Room sessions and proclamations to Firestore.
     */
    fun syncAllLocalDataToFirestore() {
        viewModelScope.launch {
            val currentUser = authUser.value
            val uid = currentUser?.uid ?: "anonymous_devotee"

            val sessions = repository.allSessionLogs.firstOrNull() ?: emptyList()
            val proclamations = repository.allSavedProclamations.firstOrNull() ?: emptyList()

            sessions.forEach { session ->
                firestoreRepository.syncSessionToCloud(uid, session)
            }
            proclamations.forEach { proclamation ->
                firestoreRepository.syncProclamationToCloud(uid, proclamation)
            }
            loadDevotees()
        }
    }
}

