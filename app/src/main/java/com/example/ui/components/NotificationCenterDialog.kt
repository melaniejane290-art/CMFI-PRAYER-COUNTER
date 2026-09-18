package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AdminBroadcastNotification
import com.example.model.AppLanguage
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

/**
 * Phase 15: In-App Notification Center & Broadcast Announcements Feed.
 * Displays real-time prayer requests, daily exhortations, and ministry updates pushed via Firestore.
 */
@Composable
fun NotificationCenterDialog(
    notifications: List<AdminBroadcastNotification>,
    unreadCount: Int,
    language: AppLanguage,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onLaunchPrayer: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val isFr = language == AppLanguage.FRENCH
    var selectedFilter by remember { mutableStateOf<NotificationPriority?>(null) }

    val filteredList = if (selectedFilter == null) {
        notifications
    } else {
        notifications.filter { it.priority == selectedFilter }
    }

    val dialogTitle = if (isFr) "Centre de Notifications" else "Notification Center"
    val dialogSubtitle = if (isFr) "Annonces & Chaînes de Prière en direct" else "Live Prayer Chains & Broadcasts"
    val markAllText = if (isFr) "Tout marquer comme lu" else "Mark all as read"
    val allFilterText = if (isFr) "Toutes" else "All"
    val prayNowButtonText = if (isFr) "Prier ce sujet maintenant" else "Pray this topic now"
    val emptyStateText = if (isFr) "Aucune notification pour le moment." else "No announcements at this time."

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = PureWhite,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 560.dp)
                .testTag("dialog_notification_center")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(GoldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dialogTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                )
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = AccentRed,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = PureWhite,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = dialogSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp).testTag("btn_close_notifications")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions & Filter Chips Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Chips horizontal scroll
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // All Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedFilter == null) RoyalNavy else SurfaceVariantBg,
                            modifier = Modifier.clickable { selectedFilter = null }
                        ) {
                            Text(
                                text = allFilterText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selectedFilter == null) PureWhite else TextDark,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Priority filter chips
                        NotificationPriority.values().forEach { priority ->
                            val isSelected = selectedFilter == priority
                            val label = if (isFr) priority.labelFr else priority.labelEn
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) RoyalNavy else SurfaceVariantBg,
                                modifier = Modifier.clickable { selectedFilter = priority }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = priority.iconEmoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) PureWhite else TextDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (unreadCount > 0) {
                        TextButton(
                            onClick = onMarkAllAsRead,
                            modifier = Modifier.padding(start = 4.dp).testTag("btn_mark_all_read")
                        ) {
                            Text(
                                text = markAllText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RoyalNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Notifications List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = SoftGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = emptyStateText,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            NotificationItemCard(
                                notification = item,
                                language = language,
                                onMarkAsRead = { onMarkAsRead(item.id) },
                                onLaunchPrayer = { topic ->
                                    onMarkAsRead(item.id)
                                    onLaunchPrayer(topic)
                                    onDismissRequest()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Dismiss Button
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepNavy,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_notif_dialog_bottom")
                ) {
                    Text(
                        text = if (isFr) "Fermer" else "Close",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: AdminBroadcastNotification,
    language: AppLanguage,
    onMarkAsRead: () -> Unit,
    onLaunchPrayer: (String) -> Unit
) {
    val isFr = language == AppLanguage.FRENCH
    val isUrgent = notification.priority == NotificationPriority.URGENT_PRAYER
    val priorityLabel = if (isFr) notification.priority.labelFr else notification.priority.labelEn

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) Color(0xFFFEF2F2) else PureWhite
        ),
        border = BorderStroke(
            1.dp,
            if (isUrgent) AccentRed.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkAsRead() }
            .testTag("notif_card_${notification.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Priority Badge + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (notification.priority) {
                        NotificationPriority.URGENT_PRAYER -> AccentRed.copy(alpha = 0.15f)
                        NotificationPriority.DAILY_WORD -> GoldContainer
                        NotificationPriority.COMMUNITY_MILESTONE -> Color(0xFFF0FDF4)
                        NotificationPriority.GENERAL -> SurfaceVariantBg
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = notification.priority.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (notification.priority) {
                                    NotificationPriority.URGENT_PRAYER -> AccentRed
                                    NotificationPriority.DAILY_WORD -> DeepNavy
                                    NotificationPriority.COMMUNITY_MILESTONE -> AccentGreen
                                    NotificationPriority.GENERAL -> TextDark
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Text(
                    text = notification.formattedDateTime,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Message Body
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextDark,
                    lineHeight = 18.sp
                )
            )

            // Scripture Verse if present
            if (notification.scriptureVerse.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldContainer.copy(alpha = 0.4f),
                    border = BorderStroke(0.5.dp, GoldOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notification.scriptureVerse,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = DeepNavy,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Author & Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✍️ ${notification.authorName}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                // Launch Prayer Action (if linked topic present)
                if (notification.prayerTopicToLaunch.isNotBlank()) {
                    Button(
                        onClick = { onLaunchPrayer(notification.prayerTopicToLaunch) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUrgent) AccentRed else SoftGold,
                            contentColor = if (isUrgent) PureWhite else DeepNavy
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        modifier = Modifier.testTag("btn_launch_prayer_${notification.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFr) "Prier" else "Pray Now",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
