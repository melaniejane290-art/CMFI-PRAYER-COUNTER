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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppLanguage
import com.example.ui.components.AuthDialog
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.PrayerCounterViewModel

/**
 * Container screen with top bar, 3-tab bottom navigation + Firebase Authentication:
 * Tab 0: Counter (Phase 1)
 * Tab 1: Saved Proclamations (Phase 2)
 * Tab 2: Prayer Session History (Phase 3)
 * Phase 4: Firebase Auth (Account Profile & Sign-in)
 * Phase 14 & 17: Admin Control Center & Firestore Cloud DB
 * Phase 15: In-App Notification Center & Live Prayer Requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainerScreen(
    viewModel: PrayerCounterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val authUser by viewModel.authUser.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotificationCount.collectAsState()
    val broadcastNotifs by viewModel.broadcastNotifications.collectAsState()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Counter, 1 = Saved, 2 = History
    var showAuthDialog by remember { mutableStateOf(false) }
    var showUserProfileDialog by remember { mutableStateOf(false) }
    var showNotificationCenterDialog by remember { mutableStateOf(false) }
    var isViewingAdminDashboard by remember { mutableStateOf(false) }

    val isFr = state.language == AppLanguage.FRENCH
    val appTitle = if (isFr) "Compteur de Prière" else "Prayer Proclamation"
    val tabCounterTitle = if (isFr) "Compteur" else "Counter"
    val tabSavedTitle = if (isFr) "Proclamations" else "Saved"
    val tabHistoryTitle = if (isFr) "Historique" else "History"

    if (isViewingAdminDashboard) {
        AdminDashboardScreen(
            viewModel = viewModel,
            onNavigateBack = { isViewingAdminDashboard = false },
            modifier = modifier
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MidnightBlue,
                            border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.6f)),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.praying_hands_logo_1787928549167),
                                contentDescription = "Prayer Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = appTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Phase 15: Notification Center Bell Icon with Badge
                        IconButton(
                            onClick = { showNotificationCenterDialog = true },
                            modifier = Modifier.testTag("topbar_notif_bell_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifsCount > 0) {
                                        Badge(
                                            containerColor = AccentRed,
                                            contentColor = PureWhite
                                        ) {
                                            Text(
                                                text = unreadNotifsCount.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadNotifsCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (unreadNotifsCount > 0) SoftGold else PureWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Phase 14: Direct Admin Portal Shortcut (if admin or unlocked)
                        if (authUser?.isAdmin == true || isAdminUnlocked) {
                            IconButton(
                                onClick = { isViewingAdminDashboard = true },
                                modifier = Modifier.testTag("topbar_admin_shortcut_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Dashboard",
                                    tint = SoftGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // User Profile / Sign In Button (Phase 4 Firebase Auth)
                        if (authUser != null) {
                            Surface(
                                shape = CircleShape,
                                color = MidnightBlue,
                                border = BorderStroke(1.5.dp, SoftGold),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { showUserProfileDialog = true }
                                    .testTag("topbar_user_profile_btn")
                            ) {
                                if (!authUser?.photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = authUser?.photoUrl,
                                        contentDescription = "User Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = authUser?.initials ?: "✝️",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SoftGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MidnightBlue,
                                border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clickable { showAuthDialog = true }
                                    .testTag("topbar_signin_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Sign In",
                                        tint = SoftGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isFr) "Compte" else "Sign In",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Bilingual Switcher in Top Bar
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MidnightBlue,
                            border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("topbar_language_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (state.language == AppLanguage.ENGLISH) SoftGold else Color.Transparent,
                                    modifier = Modifier.clickable { viewModel.setLanguage(AppLanguage.ENGLISH) }
                                ) {
                                    Text(
                                        text = "EN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (state.language == AppLanguage.ENGLISH) DeepNavy else PureWhite
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (state.language == AppLanguage.FRENCH) SoftGold else Color.Transparent,
                                    modifier = Modifier.clickable { viewModel.setLanguage(AppLanguage.FRENCH) }
                                ) {
                                    Text(
                                        text = "FR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (state.language == AppLanguage.FRENCH) DeepNavy else PureWhite
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepNavy
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Tab 0: Counter
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Timer else Icons.Outlined.Timer,
                            contentDescription = tabCounterTitle
                        )
                    },
                    label = {
                        Text(
                            text = tabCounterTitle,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepNavy,
                        selectedTextColor = DeepNavy,
                        indicatorColor = SoftGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_prayer_counter")
                )

                // Tab 1: Saved Proclamations
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = tabSavedTitle
                        )
                    },
                    label = {
                        Text(
                            text = tabSavedTitle,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepNavy,
                        selectedTextColor = DeepNavy,
                        indicatorColor = SoftGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_saved_proclamations")
                )

                // Tab 2: Session History
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = tabHistoryTitle
                        )
                    },
                    label = {
                        Text(
                            text = tabHistoryTitle,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepNavy,
                        selectedTextColor = DeepNavy,
                        indicatorColor = SoftGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_prayer_history")
                )
            }
        },
        containerColor = OffWhite,
        modifier = modifier.testTag("main_app_container")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    MainPrayerCounterScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    SavedProclamationsScreen(
                        viewModel = viewModel,
                        onNavigateToCounter = { selectedTab = 0 },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                2 -> {
                    PrayerHistoryScreen(
                        viewModel = viewModel,
                        onNavigateToCounter = { selectedTab = 0 },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Firebase Auth Dialog
    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            onDismissRequest = { showAuthDialog = false }
        )
    }

    // User Profile Dialog
    if (showUserProfileDialog && authUser != null) {
        UserProfileDialog(
            user = authUser!!,
            viewModel = viewModel,
            onOpenAdminDashboard = {
                showUserProfileDialog = false
                isViewingAdminDashboard = true
            },
            onDismissRequest = { showUserProfileDialog = false }
        )
    }

    // Phase 15: Notification Center Dialog
    if (showNotificationCenterDialog) {
        NotificationCenterDialog(
            notifications = broadcastNotifs,
            unreadCount = unreadNotifsCount,
            language = state.language,
            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
            onLaunchPrayer = { topic ->
                viewModel.updatePrayerTopic(topic)
                selectedTab = 0
            },
            onDismissRequest = { showNotificationCenterDialog = false }
        )
    }
}

