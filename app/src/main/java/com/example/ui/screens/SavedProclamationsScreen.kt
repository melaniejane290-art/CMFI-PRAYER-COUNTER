package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SavedProclamation
import com.example.model.AppLanguage
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrightGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOutline
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurfaceVariantBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.viewmodel.PrayerCounterViewModel

@Composable
fun SavedProclamationsScreen(
    viewModel: PrayerCounterViewModel,
    onNavigateToCounter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val savedList by viewModel.allSavedProclamations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalProclamations by viewModel.totalLifetimeProclamations.collectAsState()
    val totalPrayerTime by viewModel.totalLifetimePrayerDuration.collectAsState()
    val totalSessions by viewModel.totalSessionCount.collectAsState()

    val isFr = state.language == AppLanguage.FRENCH

    var itemToDelete by remember { mutableStateOf<SavedProclamation?>(null) }
    var itemToEditNotes by remember { mutableStateOf<SavedProclamation?>(null) }
    var tempNoteText by remember { mutableStateOf("") }
    var filterOnlyCompleted by remember { mutableStateOf(false) }

    val displayedList = remember(savedList, filterOnlyCompleted) {
        if (filterOnlyCompleted) {
            savedList.filter { it.isTargetCompleted }
        } else {
            savedList
        }
    }

    // Localized Strings
    val screenTitle = if (isFr) "Proclamations Enregistrées" else "Saved Proclamations"
    val screenSubtitle = if (isFr) "Vos étapes spirituelles et bilans de prière" else "Your spiritual prayer milestones and records"
    val statProclamationsLabel = if (isFr) "Proclamations" else "Proclamations"
    val statSessionsLabel = if (isFr) "Sessions" else "Sessions"
    val statTimeLabel = if (isFr) "Temps de Prière" else "Prayer Time"
    val searchHint = if (isFr) "Rechercher par sujet..." else "Search by topic..."
    val emptyTitle = if (isFr) "Aucune proclamation enregistrée" else "No Saved Proclamations Yet"
    val emptyDesc = if (isFr) {
        "Vos sessions de prière enregistrées depuis le compteur apparaîtront ici avec vos totaux et statistiques."
    } else {
        "Your saved prayer sessions from the counter will appear here with cumulative totals and statistics."
    }
    val btnStartPraying = if (isFr) "OUVRIR LE COMPTEUR" else "OPEN PRAYER COUNTER"

    // Format total prayer time
    val formattedTotalTime = remember(totalPrayerTime) {
        val totalSecs = totalPrayerTime ?: 0L
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("saved_proclamations_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Stats Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("saved_stats_header_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MidnightBlue,
                                border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.6f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.praying_hands_logo_1787928549167),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = screenTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                )
                                Text(
                                    text = screenSubtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PureWhite.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 3 Column Stats
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MidnightBlue.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Total Proclamations
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = statProclamationsLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSubtle,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format("%,d", totalProclamations ?: 0),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = BrightGold,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        modifier = Modifier.testTag("stat_total_proclamations")
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(1.dp)
                                        .background(Color(0xFF334155))
                                )

                                // 2. Total Sessions
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = statSessionsLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSubtle,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$totalSessions",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        modifier = Modifier.testTag("stat_total_sessions")
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(1.dp)
                                        .background(Color(0xFF334155))
                                )

                                // 3. Total Prayer Time
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = statTimeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSubtle,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formattedTotalTime,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        modifier = Modifier.testTag("stat_total_time")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Chip
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text(searchHint, color = TextMuted) },
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
                                        contentDescription = "Clear search",
                                        tint = TextMuted
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite,
                            focusedBorderColor = SoftGold,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_saved")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (!filterOnlyCompleted) RoyalNavy else PureWhite,
                            border = BorderStroke(1.dp, if (!filterOnlyCompleted) RoyalNavy else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { filterOnlyCompleted = false }
                        ) {
                            Text(
                                text = if (isFr) "Toutes (${savedList.size})" else "All (${savedList.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (!filterOnlyCompleted) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!filterOnlyCompleted) PureWhite else TextDark
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (filterOnlyCompleted) GoldContainer else PureWhite,
                            border = BorderStroke(1.dp, if (filterOnlyCompleted) SoftGold else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { filterOnlyCompleted = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFr) "Objectifs atteints" else "Completed Goals",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (filterOnlyCompleted) FontWeight.Bold else FontWeight.Normal,
                                        color = if (filterOnlyCompleted) DeepNavy else TextDark
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // List or Empty State
            if (displayedList.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(vertical = 24.dp)
                            .testTag("empty_saved_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GoldContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = emptyTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = emptyDesc,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMuted,
                                    lineHeight = 20.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onNavigateToCounter,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftGold,
                                    contentColor = DeepNavy
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("btn_empty_to_counter")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = btnStartPraying,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(displayedList, key = { it.id }) { item ->
                    SavedProclamationCard(
                        item = item,
                        isFr = isFr,
                        onResumeInCounter = {
                            viewModel.resumeProclamationInCounter(item)
                            onNavigateToCounter()
                        },
                        onEditNotes = {
                            itemToEditNotes = item
                            tempNoteText = item.notes
                        },
                        onDelete = {
                            itemToDelete = item
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { proclamation ->
        val dialogTitle = if (isFr) "Supprimer la proclamation ?" else "Delete Saved Proclamation?"
        val dialogMsg = if (isFr) {
            "Êtes-vous certain de vouloir supprimer l'enregistrement « ${proclamation.topic} » (${proclamation.proclamationCount} proclamations) ?"
        } else {
            "Are you sure you want to delete the record “${proclamation.topic}” (${proclamation.proclamationCount} proclamations)?"
        }
        val btnDeleteText = if (isFr) "Supprimer" else "Delete"
        val btnCancelText = if (isFr) "Annuler" else "Cancel"

        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = dialogMsg,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSavedProclamation(proclamation.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        contentColor = PureWhite
                    )
                ) {
                    Text(btnDeleteText)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(btnCancelText, color = TextMuted)
                }
            }
        )
    }

    // Edit Notes Dialog
    itemToEditNotes?.let { proclamation ->
        val noteDialogTitle = if (isFr) "Notes de Proclamation" else "Prayer Proclamation Notes"
        val noteDialogHint = if (isFr) {
            "Ajouter des versets, témoignages ou réflexions spirituelles..."
        } else {
            "Add scriptures, testimonies, or spiritual reflections..."
        }
        val btnSaveNoteText = if (isFr) "Enregistrer la note" else "Save Note"
        val btnCancelNoteText = if (isFr) "Annuler" else "Cancel"

        AlertDialog(
            onDismissRequest = { itemToEditNotes = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = SoftGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = noteDialogTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "“${proclamation.topic}”",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempNoteText,
                        onValueChange = { tempNoteText = it },
                        placeholder = { Text(noteDialogHint, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("input_proclamation_notes"),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftGold,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProclamationNotes(proclamation, tempNoteText)
                        itemToEditNotes = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftGold,
                        contentColor = DeepNavy
                    )
                ) {
                    Text(btnSaveNoteText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEditNotes = null }) {
                    Text(btnCancelNoteText, color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun SavedProclamationCard(
    item: SavedProclamation,
    isFr: Boolean,
    onResumeInCounter: () -> Unit,
    onEditNotes: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val btnResumeText = if (isFr) "Reprendre" else "Resume"
    val countLabel = if (isFr) "proclamations" else "proclamations"
    val completedLabel = if (isFr) "Objectif atteint" else "Target Reached"

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("saved_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Date & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                if (item.isTargetCompleted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SoftGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = completedLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DeepNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prayer Proclamation Topic
            Text(
                text = "“${item.topic}”",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    lineHeight = 22.sp
                )
            )

            // Optional User Notes
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDark,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row: Proclamation Count & Session Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Count Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RoyalNavy
                    ) {
                        Text(
                            text = String.format("%,d", item.proclamationCount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (item.targetCount > 0) "/ ${String.format("%,d", item.targetCount)} $countLabel" else countLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Duration Badge
                if (item.durationSeconds > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.formattedDuration,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DeepNavy,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Actions: Resume in counter, Edit Notes, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit note icon button
                    IconButton(
                        onClick = onEditNotes,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Edit Notes",
                            tint = RoyalNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete icon button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = AccentRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Resume in Counter Button
                Button(
                    onClick = onResumeInCounter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldContainer,
                        contentColor = DeepNavy
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = btnResumeText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
