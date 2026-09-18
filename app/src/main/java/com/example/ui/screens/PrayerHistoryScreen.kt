package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PrayerSessionRecord
import com.example.model.AppLanguage
import com.example.model.DayPrayerActivity
import com.example.model.HistoryTimeFilter
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BrightGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyLighter
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.PrayerCounterViewModel

/**
 * Phase 3: Prayer Session History Screen.
 * Displays chronological prayer logs, pacing analytics, devotion streaks, and weekly trends.
 */
@Composable
fun PrayerHistoryScreen(
    viewModel: PrayerCounterViewModel,
    onNavigateToCounter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val sessionLogs by viewModel.allSessionLogs.collectAsState()
    val totalSessions by viewModel.totalHistorySessionsCount.collectAsState()
    val totalDuration by viewModel.totalHistoryDuration.collectAsState()
    val dailyStreak by viewModel.currentDailyStreak.collectAsState()
    val weeklyActivity by viewModel.weeklyActivity.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val isFr = state.language == AppLanguage.FRENCH

    // Dialog States
    var sessionToDelete by remember { mutableStateOf<PrayerSessionRecord?>(null) }
    var sessionToEditNote by remember { mutableStateOf<PrayerSessionRecord?>(null) }
    var noteText by remember { mutableStateOf("") }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val formattedTotalTime = remember(totalDuration) {
        val totalSec = totalDuration ?: 0L
        val hours = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("prayer_history_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFr) "Historique des Sessions" else "Prayer Session History",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepNavy
                            )
                        )
                        Text(
                            text = if (isFr) "Journaux chronologiques, cadence et séries" else "Chronological prayer logs, pace, and devotion streaks",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted
                            )
                        )
                    }

                    if (sessionLogs.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllDialog = true },
                            modifier = Modifier.testTag("btn_clear_all_history")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = if (isFr) "Effacer tout" else "Clear All",
                                tint = TextMuted
                            )
                        }
                    }
                }
            }

            // Hero Devotion Streak & Lifetime Stats Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_stats_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Devotion Streak Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrightGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, SoftGold),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = "Streak Fire",
                                            tint = SoftGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isFr) "Série de Prière" else "Devotion Streak",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PureWhite.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = if (isFr) "$dailyStreak ${if (dailyStreak > 1) "Jours" else "Jour"}" else "$dailyStreak ${if (dailyStreak == 1) "Day" else "Days"}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = SoftGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Total Time Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MidnightBlue,
                                border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f)),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formattedTotalTime,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Horizontal Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(SoftGold.copy(alpha = 0.2f))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Weekly Breakdown Bar Chart
                        Text(
                            text = if (isFr) "Activité des 7 derniers jours" else "Last 7 Days Prayer Activity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        WeeklyActivityChart(
                            activityList = weeklyActivity,
                            isFr = isFr
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = if (isFr) "Rechercher par sujet ou notes…" else "Search sessions by topic or notes…",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = RoyalNavy
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite,
                            focusedBorderColor = RoyalNavy,
                            unfocusedBorderColor = SoftGold.copy(alpha = 0.5f),
                            focusedTextColor = DeepNavy,
                            unfocusedTextColor = DeepNavy
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Range Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HistoryTimeFilter.values()) { filterOption ->
                            val label = when (filterOption) {
                                HistoryTimeFilter.ALL_TIME -> if (isFr) "Tout" else "All Time"
                                HistoryTimeFilter.TODAY -> if (isFr) "Aujourd'hui" else "Today"
                                HistoryTimeFilter.THIS_WEEK -> if (isFr) "Cette semaine" else "This Week"
                                HistoryTimeFilter.THIS_MONTH -> if (isFr) "Ce mois-ci" else "This Month"
                            }
                            val isSelected = currentFilter == filterOption

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setHistoryFilter(filterOption) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepNavy,
                                    selectedLabelColor = SoftGold,
                                    containerColor = PureWhite,
                                    labelColor = TextDark
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SoftGold else SoftGold.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("filter_chip_${filterOption.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Session Logs List or Empty State
            if (sessionLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("history_empty_state_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = DeepNavy,
                                border = BorderStroke(1.dp, SoftGold),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isFr) "Aucune session dans l'historique" else "No Prayer Sessions Logged",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isFr) {
                                    "Priez sur l'onglet Compteur et enregistrez vos proclamations pour visualiser ici votre historique complet avec chronométrage et cadence."
                                } else {
                                    "Pray on the Counter tab and tap Save to record your proclamation history with exact timestamps, duration, and cadence."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = onNavigateToCounter,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepNavy,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SoftGold),
                                modifier = Modifier.testTag("btn_go_to_counter")
                            ) {
                                Text(
                                    text = if (isFr) "Aller au Compteur" else "Go to Prayer Counter",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = sessionLogs,
                    key = { it.id }
                ) { session ->
                    PrayerSessionHistoryCard(
                        session = session,
                        isFr = isFr,
                        onShare = {
                            val shareText = viewModel.formatSessionShareText(session, isFr)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(
                                sendIntent,
                                if (isFr) "Partager la session de prière" else "Share Prayer Session"
                            )
                            context.startActivity(shareIntent)
                        },
                        onEditNote = {
                            sessionToEditNote = session
                            noteText = session.notes
                        },
                        onDelete = {
                            sessionToDelete = session
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Delete Single Session Confirmation Dialog
    if (sessionToDelete != null) {
        val target = sessionToDelete!!
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = if (isFr) "Supprimer cette session ?" else "Delete Session Log?",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Text(
                    text = if (isFr) {
                        "Voulez-vous supprimer l'historique de la session du ${target.formattedDateTime} (+${target.sessionCount} proclamations) ?"
                    } else {
                        "Are you sure you want to delete the session log from ${target.formattedDateTime} (+${target.sessionCount} proclamations)?"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSessionLog(target.id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = PureWhite
                    )
                ) {
                    Text(if (isFr) "Supprimer" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(if (isFr) "Annuler" else "Cancel", color = TextDark)
                }
            }
        )
    }

    // Clear All Session History Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text(
                    text = if (isFr) "Effacer tout l'historique ?" else "Clear All Prayer History?",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Text(
                    text = if (isFr) {
                        "Cette action supprimera définitivement tous les journaux de session de prière enregistrés. Les totaux cumulés ne seront pas affectés."
                    } else {
                        "This will permanently delete all recorded prayer session logs. Your cumulative saved counts will remain safe."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllSessionHistory()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = PureWhite
                    )
                ) {
                    Text(if (isFr) "Tout effacer" else "Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(if (isFr) "Annuler" else "Cancel", color = TextDark)
                }
            }
        )
    }

    // Edit Spiritual Notes Dialog
    if (sessionToEditNote != null) {
        val target = sessionToEditNote!!
        AlertDialog(
            onDismissRequest = { sessionToEditNote = null },
            title = {
                Text(
                    text = if (isFr) "Notes de la Session" else "Prayer Session Notes",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = target.topic,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RoyalNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = {
                            Text(if (isFr) "Réflexions, versets bibliques ou témoignages" else "Reflections, Bible verses, or testimonies")
                        },
                        placeholder = {
                            Text(if (isFr) "Entrez vos notes spirituelles…" else "Enter spiritual notes…")
                        },
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_history_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSessionLogNotes(target, noteText)
                        sessionToEditNote = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepNavy,
                        contentColor = PureWhite
                    )
                ) {
                    Text(if (isFr) "Enregistrer la note" else "Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToEditNote = null }) {
                    Text(if (isFr) "Annuler" else "Cancel", color = TextDark)
                }
            }
        )
    }
}

/**
 * Individual Prayer Session History Card displaying detailed metrics, pace, and action buttons.
 */
@Composable
fun PrayerSessionHistoryCard(
    session: PrayerSessionRecord,
    isFr: Boolean,
    onShare: () -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Date / Time + Target Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = RoyalNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = session.formattedDateTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                if (session.isTargetReached) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFr) "Objectif atteint" else "Target Met",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Proclamation Topic
            Text(
                text = session.topic,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Badges Row: Count, Duration, Pace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Count Badge
                MetricPill(
                    icon = Icons.Filled.CheckCircle,
                    label = if (isFr) "Proclamations" else "Count",
                    value = "+${session.sessionCount}",
                    containerColor = MidnightBlue,
                    contentColor = SoftGold
                )

                // Duration Badge
                MetricPill(
                    icon = Icons.Filled.Timer,
                    label = if (isFr) "Durée" else "Duration",
                    value = session.formattedDuration,
                    containerColor = NavyLight.copy(alpha = 0.12f),
                    contentColor = DeepNavy
                )

                // Pace Badge
                MetricPill(
                    icon = Icons.Filled.Speed,
                    label = if (isFr) "Rythme" else "Pace",
                    value = session.formattedPace,
                    containerColor = BrightGold.copy(alpha = 0.12f),
                    contentColor = RoyalNavy
                )
            }

            // Attached Spiritual Notes (if present)
            if (session.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OffWhite,
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = session.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDark,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Share, Note, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("btn_share_session_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = RoyalNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFr) "Partager" else "Share",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalNavy,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                TextButton(
                    onClick = onEditNote,
                    modifier = Modifier.testTag("btn_edit_session_note_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Note",
                        tint = TextDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (session.notes.isBlank()) {
                            if (isFr) "Ajouter note" else "Add Note"
                        } else {
                            if (isFr) "Modifier note" else "Edit Note"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("btn_delete_session_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Metric pill badge for session stats.
 */
@Composable
private fun MetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Custom Compose 7-day activity bar visualizer.
 */
@Composable
fun WeeklyActivityChart(
    activityList: List<DayPrayerActivity>,
    isFr: Boolean,
    modifier: Modifier = Modifier
) {
    val maxCount = remember(activityList) {
        activityList.maxOfOrNull { it.proclamationCount }?.coerceAtLeast(1) ?: 1
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        activityList.forEach { day ->
            val ratio = if (maxCount > 0) (day.proclamationCount.toFloat() / maxCount.toFloat()).coerceIn(0.1f, 1f) else 0.1f
            val hasActivity = day.proclamationCount > 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Count Tooltip / Label
                if (hasActivity) {
                    Text(
                        text = "${day.proclamationCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = if (day.isToday) SoftGold else PureWhite.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Bar
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(if (hasActivity) ratio * 0.7f else 0.08f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (day.isToday) {
                                Brush.verticalGradient(listOf(BrightGold, SoftGold))
                            } else if (hasActivity) {
                                Brush.verticalGradient(listOf(NavyLight, RoyalNavy))
                            } else {
                                Brush.verticalGradient(listOf(MidnightBlue, MidnightBlue))
                            }
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Day Label
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = if (day.isToday) SoftGold else PureWhite.copy(alpha = 0.6f),
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}
