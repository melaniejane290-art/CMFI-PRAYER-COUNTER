package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminBroadcastNotification
import com.example.model.AdminDashboardTab
import com.example.model.AppLanguage
import com.example.model.CloudPrayerPreset
import com.example.model.CommunityGlobalStats
import com.example.model.DevoteeSummary
import com.example.model.NotificationPriority
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

/**
 * Phase 14 & 15 & 17: Administrative Control Center & Live Firestore Community Management.
 * Comprehensive portal for:
 * 1. Global Analytics & Prayer Impact
 * 2. Real-time Admin Broadcast Publisher (Push notifications to all users via Firestore)
 * 3. Community Devotee Directory & Role Assignments
 * 4. Cloud Prayer Presets Synchronization
 * 5. Firestore Database Health & Full Cloud Backup Status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: PrayerCounterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val globalStats by viewModel.communityGlobalStats.collectAsState()
    val notifications by viewModel.broadcastNotifications.collectAsState()
    val devotees by viewModel.devoteesList.collectAsState()
    val cloudPresets by viewModel.cloudPresets.collectAsState()
    val isFirestoreConnected by viewModel.isFirestoreConnected.collectAsState()

    var selectedTab by remember { mutableStateOf(AdminDashboardTab.OVERVIEW) }
    var showCreateBroadcastDialog by remember { mutableStateOf(false) }
    var showCreatePresetDialog by remember { mutableStateOf(false) }
    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val isFr = state.language == AppLanguage.FRENCH
    val screenTitle = if (isFr) "Tableau de Bord Admin" else "Admin Dashboard"

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MidnightBlue,
                            border = BorderStroke(1.dp, SoftGold),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = if (isFr) "Gestion Cloud & Communauté" else "Live Cloud & Ministry Hub",
                                style = MaterialTheme.typography.labelSmall.copy(color = SoftGold)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                actions = {
                    // Firestore Sync Indicator & Refresh Action
                    IconButton(
                        onClick = {
                            viewModel.syncAllLocalDataToFirestore()
                            syncFeedbackMessage = if (isFr) "Base Firestore synchronisée !" else "Firestore database synchronized!"
                        },
                        modifier = Modifier.testTag("admin_btn_sync_now")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Sync Cloud",
                            tint = SoftGold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepNavy
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == AdminDashboardTab.BROADCASTS) {
                FloatingActionButton(
                    onClick = { showCreateBroadcastDialog = true },
                    containerColor = SoftGold,
                    contentColor = DeepNavy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_create_broadcast")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFr) "Nouvelle Annonce" else "New Broadcast",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else if (selectedTab == AdminDashboardTab.PRESET_TOPICS) {
                FloatingActionButton(
                    onClick = { showCreatePresetDialog = true },
                    containerColor = SoftGold,
                    contentColor = DeepNavy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_create_preset")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFr) "Ajouter Sujet" else "Add Topic",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceVariantBg)
                .padding(innerPadding)
        ) {
            // Live Cloud Connection Banner
            Surface(
                color = if (isFirestoreConnected) Color(0xFF064E3B) else Color(0xFF78350F),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFirestoreConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFirestoreConnected) {
                                if (isFr) "Base Firestore : Connectée & Synchronisation Active" else "Firestore: Connected & Real-time Sync Active"
                            } else {
                                if (isFr) "Base Firestore : Mode Local / En attente" else "Firestore: Local Standby Mode"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = if (isFr) "Rôle : ADMIN" else "Role: ADMIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SoftGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Sync Feedback Banner if active
            AnimatedVisibility(visible = syncFeedbackMessage != null) {
                syncFeedbackMessage?.let { msg ->
                    Surface(
                        color = GoldContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { syncFeedbackMessage = null }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DeepNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Admin Navigation Tabs
            Surface(
                color = PureWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminDashboardTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val tabTitle = when (tab) {
                            AdminDashboardTab.OVERVIEW -> if (isFr) "📊 Statistiques" else "📊 Overview"
                            AdminDashboardTab.BROADCASTS -> if (isFr) "📢 Annonces (${notifications.size})" else "📢 Broadcasts (${notifications.size})"
                            AdminDashboardTab.DEVOTEES -> if (isFr) "👥 Fidèles (${devotees.size})" else "👥 Devotees (${devotees.size})"
                            AdminDashboardTab.PRESET_TOPICS -> if (isFr) "📜 Sujets Cloud (${cloudPresets.size})" else "📜 Cloud Topics (${cloudPresets.size})"
                            AdminDashboardTab.FIRESTORE_STATUS -> if (isFr) "☁️ Base Firestore" else "☁️ Firestore Status"
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) DeepNavy else SurfaceVariantBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SoftGold else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTab = tab }
                                .testTag("admin_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = tabTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) PureWhite else TextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Tab Content Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    AdminDashboardTab.OVERVIEW -> {
                        AdminOverviewTabContent(
                            globalStats = globalStats,
                            isFr = isFr
                        )
                    }
                    AdminDashboardTab.BROADCASTS -> {
                        AdminBroadcastsTabContent(
                            notifications = notifications,
                            isFr = isFr,
                            onDeleteNotification = { viewModel.deleteBroadcastNotification(it) },
                            onCreateNew = { showCreateBroadcastDialog = true }
                        )
                    }
                    AdminDashboardTab.DEVOTEES -> {
                        AdminDevoteesTabContent(
                            devotees = devotees,
                            isFr = isFr,
                            onToggleRole = { uid, newRole -> viewModel.updateDevoteeRole(uid, newRole) },
                            onRefresh = { viewModel.loadDevotees() }
                        )
                    }
                    AdminDashboardTab.PRESET_TOPICS -> {
                        AdminPresetsTabContent(
                            presets = cloudPresets,
                            isFr = isFr,
                            onDeletePreset = { viewModel.deleteCloudPreset(it) },
                            onCreateNew = { showCreatePresetDialog = true }
                        )
                    }
                    AdminDashboardTab.FIRESTORE_STATUS -> {
                        AdminFirestoreStatusTabContent(
                            isFirestoreConnected = isFirestoreConnected,
                            globalStats = globalStats,
                            isFr = isFr,
                            onSyncAll = {
                                viewModel.syncAllLocalDataToFirestore()
                                syncFeedbackMessage = if (isFr) "Synchronisation Cloud Firestore effectuée !" else "Cloud Firestore sync completed!"
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Broadcast Dialog
    if (showCreateBroadcastDialog) {
        CreateBroadcastDialog(
            isFr = isFr,
            onPublish = { notification ->
                viewModel.publishBroadcastNotification(notification)
                showCreateBroadcastDialog = false
                syncFeedbackMessage = if (isFr) "Annonce diffusée en direct à tous les fidèles !" else "Broadcast published live to all devotees!"
            },
            onDismissRequest = { showCreateBroadcastDialog = false }
        )
    }

    // Create Cloud Preset Dialog
    if (showCreatePresetDialog) {
        CreateCloudPresetDialog(
            isFr = isFr,
            onSave = { preset ->
                viewModel.saveCloudPreset(preset)
                showCreatePresetDialog = false
                syncFeedbackMessage = if (isFr) "Nouveau sujet de prière enregistré dans Firestore !" else "New prayer topic saved to Firestore!"
            },
            onDismissRequest = { showCreatePresetDialog = false }
        )
    }
}

// -----------------------------------------------------------------------------
// 1. OVERVIEW & ANALYTICS TAB
// -----------------------------------------------------------------------------
@Composable
private fun AdminOverviewTabContent(
    globalStats: CommunityGlobalStats,
    isFr: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Grid Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminKpiCard(
                title = if (isFr) "Proclamations Globales" else "Global Proclamations",
                value = String.format("%,d", globalStats.totalGlobalProclamations),
                iconEmoji = "🌍",
                accentColor = SoftGold,
                modifier = Modifier.weight(1f)
            )
            AdminKpiCard(
                title = if (isFr) "Temps Total de Prière" else "Total Prayer Time",
                value = globalStats.formattedGlobalDuration,
                iconEmoji = "⏱️",
                accentColor = Color(0xFF60A5FA),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminKpiCard(
                title = if (isFr) "Sessions de Prière" else "Prayer Sessions",
                value = String.format("%,d", globalStats.totalGlobalSessions),
                iconEmoji = "🕊️",
                accentColor = AccentGreen,
                modifier = Modifier.weight(1f)
            )
            AdminKpiCard(
                title = if (isFr) "Fidèles Connectés" else "Registered Devotees",
                value = String.format("%,d", globalStats.totalDevoteesCount),
                iconEmoji = "👥",
                accentColor = Color(0xFFA78BFA),
                modifier = Modifier.weight(1f)
            )
        }

        // Top Proclaimed Topics Breakdown
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFr) "🔥 Sujets de Prière les Plus Proclamés" else "🔥 Top Proclaimed Topics (Global)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = SoftGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                globalStats.topTopics.forEach { topicStat ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = topicStat.topic,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format("%,d", topicStat.count)} (${(topicStat.percentage * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { topicStat.percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SoftGold,
                            trackColor = GoldContainer
                        )
                    }
                }
            }
        }

        // Language & Demographics Card
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isFr) "🌐 Répartition Linguistique & Communauté" else "🌐 Language & Ministry Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceVariantBg,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "🇺🇸 English Devotees", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                            Text(text = "58%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepNavy))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceVariantBg,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "🇫🇷 Fidèles Francophones", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                            Text(text = "42%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepNavy))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    iconEmoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = iconEmoji, fontSize = 22.sp)
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(8.dp)
                ) {}
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 2. BROADCASTS MANAGEMENT TAB
// -----------------------------------------------------------------------------
@Composable
private fun AdminBroadcastsTabContent(
    notifications: List<AdminBroadcastNotification>,
    isFr: Boolean,
    onDeleteNotification: (String) -> Unit,
    onCreateNew: () -> Unit
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = SoftGold,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isFr) "Aucune annonce active." else "No active broadcasts.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextDark, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCreateNew,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (isFr) "Diffuser une Annonce" else "Publish Broadcast")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications, key = { it.id }) { item ->
                AdminNotificationItemRow(
                    notification = item,
                    isFr = isFr,
                    onDelete = { onDeleteNotification(item.id) }
                )
            }
        }
    }
}

@Composable
private fun AdminNotificationItemRow(
    notification: AdminBroadcastNotification,
    isFr: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = notification.priority.iconEmoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFr) notification.priority.labelFr else notification.priority.labelEn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.formattedDateTime,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
            )

            if (notification.scriptureVerse.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📖 ${notification.scriptureVerse}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = RoyalNavy
                    )
                )
            }

            if (notification.prayerTopicToLaunch.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "🎯 Sujet lié : ${notification.prayerTopicToLaunch}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DeepNavy,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isFr) "Supprimer l'annonce ?" else "Delete Broadcast?") },
            text = { Text(if (isFr) "Cette annonce sera retirée de la base Firestore et du centre de notifications de tous les fidèles." else "This announcement will be removed from Firestore and all devotee devices.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text(if (isFr) "Supprimer" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isFr) "Annuler" else "Cancel")
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 3. DEVOTEES MANAGEMENT TAB
// -----------------------------------------------------------------------------
@Composable
private fun AdminDevoteesTabContent(
    devotees: List<DevoteeSummary>,
    isFr: Boolean,
    onToggleRole: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = if (searchQuery.isBlank()) {
        devotees
    } else {
        devotees.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isFr) "Rechercher un fidèle par nom ou email..." else "Search devotee by name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SoftGold)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite,
                focusedBorderColor = SoftGold,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFr) "Aucun fidèle trouvé." else "No devotees found.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.uid }) { devotee ->
                    DevoteeListItemCard(
                        devotee = devotee,
                        isFr = isFr,
                        onToggleRole = { newRole -> onToggleRole(devotee.uid, newRole) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DevoteeListItemCard(
    devotee: DevoteeSummary,
    isFr: Boolean,
    onToggleRole: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (devotee.isAdmin) SoftGold else MidnightBlue,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = devotee.displayName.take(2).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (devotee.isAdmin) DeepNavy else PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = devotee.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (devotee.isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldContainer
                            ) {
                                Text(
                                    text = "ADMIN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepNavy,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (devotee.email.isNotBlank()) {
                        Text(
                            text = devotee.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "🔥 ${String.format("%,d", devotee.totalProclamations)} ${if (isFr) "proclamations" else "prayers"} • ⏱️ ${devotee.formattedPrayerTime}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Role toggle button
            Button(
                onClick = {
                    val targetRole = if (devotee.isAdmin) "devotee" else "admin"
                    onToggleRole(targetRole)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (devotee.isAdmin) SurfaceVariantBg else SoftGold,
                    contentColor = DeepNavy
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                )
            ) {
                Text(
                    text = if (devotee.isAdmin) {
                        if (isFr) "Rétrograder" else "Demote"
                    } else {
                        if (isFr) "Promouvoir Admin" else "Promote Admin"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. CLOUD PRESETS TAB
// -----------------------------------------------------------------------------
@Composable
private fun AdminPresetsTabContent(
    presets: List<CloudPrayerPreset>,
    isFr: Boolean,
    onDeletePreset: (String) -> Unit,
    onCreateNew: () -> Unit
) {
    if (presets.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = SoftGold, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (isFr) "Aucun sujet cloud enregistré." else "No cloud topics registered.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCreateNew) {
                    Text(text = if (isFr) "Ajouter un Sujet" else "Add Prayer Topic")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(presets, key = { it.id }) { preset ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldContainer
                            ) {
                                Text(
                                    text = preset.category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepNavy,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeletePreset(preset.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = AccentRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🇺🇸 ${preset.textEn}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        )
                        Text(
                            text = "🇫🇷 ${preset.textFr}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDark,
                                fontStyle = FontStyle.Italic
                            )
                        )

                        if (preset.scriptureRef.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📖 ${preset.scriptureRef}",
                                style = MaterialTheme.typography.labelSmall.copy(color = SoftGold)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. FIRESTORE STATUS TAB
// -----------------------------------------------------------------------------
@Composable
private fun AdminFirestoreStatusTabContent(
    isFirestoreConnected: Boolean,
    globalStats: CommunityGlobalStats,
    isFr: Boolean,
    onSyncAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFr) "État de la Base de Données Firestore" else "Firestore Database Health & Sync",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = SoftGold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isFr) "Statut de Connexion" else "Connection Status", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    Text(
                        text = if (isFirestoreConnected) "🟢 CONNECTED (LIVE)" else "🟡 OFFLINE / STANDBY",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isFirestoreConnected) AccentGreen else Color(0xFFD97706),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isFr) "Persistance Hors-Ligne" else "Offline Cache Persistence", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    Text(text = "ENABLED (Room + Firestore)", style = MaterialTheme.typography.bodySmall.copy(color = DeepNavy, fontWeight = FontWeight.SemiBold))
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isFr) "Collections Cloud" else "Active Collections", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    Text(text = "users, prayer_sessions, admin_notifications, presets", style = MaterialTheme.typography.bodySmall.copy(color = DeepNavy, fontWeight = FontWeight.SemiBold))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSyncAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepNavy,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = SoftGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFr) "Forcer la Synchronisation Complète" else "Force Full Cloud Sync",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Dialogs: Create Broadcast & Create Preset
// -----------------------------------------------------------------------------
@Composable
private fun CreateBroadcastDialog(
    isFr: Boolean,
    onPublish: (AdminBroadcastNotification) -> Unit,
    onDismissRequest: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var scripture by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(NotificationPriority.URGENT_PRAYER) }
    var targetTopic by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Admin Ministry") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (isFr) "📢 Publier une Annonce / Chaîne de Prière" else "📢 Broadcast Announcement to Devotees",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepNavy)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Priority pills
                Text(
                    text = if (isFr) "Type d'Annonce :" else "Priority Type:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NotificationPriority.values().forEach { prio ->
                        val isSelected = priority == prio
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) DeepNavy else SurfaceVariantBg,
                            modifier = Modifier.clickable { priority = prio }
                        ) {
                            Text(
                                text = "${prio.iconEmoji} ${if (isFr) prio.labelFr else prio.labelEn}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) PureWhite else TextDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isFr) "Titre de l'annonce *" else "Announcement Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(if (isFr) "Message / Exhortation *" else "Message / Exhortation *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = scripture,
                    onValueChange = { scripture = it },
                    label = { Text(if (isFr) "Verset Biblique (ex: Ésaïe 53:5)" else "Scripture Verse (e.g. Isaiah 53:5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetTopic,
                    onValueChange = { targetTopic = it },
                    label = { Text(if (isFr) "Sujet de prière à lancer (Optionnel)" else "Prayer Topic to launch (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(if (isFr) "Auteur / Ministère" else "Author / Ministry Signature") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onPublish(
                            AdminBroadcastNotification(
                                title = title.trim(),
                                message = message.trim(),
                                scriptureVerse = scripture.trim(),
                                priority = priority,
                                prayerTopicToLaunch = targetTopic.trim(),
                                authorName = author.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = DeepNavy)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isFr) "Diffuser en Direct" else "Broadcast Live")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(if (isFr) "Annuler" else "Cancel")
            }
        }
    )
}

@Composable
private fun CreateCloudPresetDialog(
    isFr: Boolean,
    onSave: (CloudPrayerPreset) -> Unit,
    onDismissRequest: () -> Unit
) {
    var textEn by remember { mutableStateOf("") }
    var textFr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Faith & Victory") }
    var scripture by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (isFr) "Ajouter un Sujet Cloud" else "Add Cloud Prayer Topic",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepNavy)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = textEn,
                    onValueChange = { textEn = it },
                    label = { Text("English Proclamation Topic *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = textFr,
                    onValueChange = { textFr = it },
                    label = { Text("Sujet de Proclamation en Français *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(if (isFr) "Catégorie" else "Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = scripture,
                    onValueChange = { scripture = it },
                    label = { Text(if (isFr) "Référence Biblique" else "Scripture Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textEn.isNotBlank() || textFr.isNotBlank()) {
                        onSave(
                            CloudPrayerPreset(
                                textEn = textEn.trim().ifBlank { textFr.trim() },
                                textFr = textFr.trim().ifBlank { textEn.trim() },
                                category = category.trim(),
                                scriptureRef = scripture.trim()
                            )
                        )
                    }
                },
                enabled = textEn.isNotBlank() || textFr.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = DeepNavy)
            ) {
                Text(if (isFr) "Enregistrer dans Firestore" else "Save to Firestore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(if (isFr) "Annuler" else "Cancel")
            }
        }
    )
}
