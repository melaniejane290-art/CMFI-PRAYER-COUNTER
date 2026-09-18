package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.AppLanguage
import com.example.model.UserAccount
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.NavyLight
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.PrayerCounterViewModel

/**
 * User Profile Dialog displaying account details, provider, lifetime prayer metrics, and sign out options.
 */
@Composable
fun UserProfileDialog(
    user: UserAccount,
    viewModel: PrayerCounterViewModel,
    onOpenAdminDashboard: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val totalProclamations by viewModel.totalLifetimeProclamations.collectAsState()
    val totalDuration by viewModel.totalHistoryDuration.collectAsState()
    val dailyStreak by viewModel.currentDailyStreak.collectAsState()

    val isFr = state.language == AppLanguage.FRENCH
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showAdminPasscodeDialog by remember { mutableStateOf(false) }
    var adminPasscodeInput by remember { mutableStateOf("") }
    var adminPasscodeError by remember { mutableStateOf(false) }

    val formattedTotalTime = remember(totalDuration) {
        val totalSec = totalDuration ?: 0L
        val hours = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(1.5.dp, SoftGold.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("user_profile_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFr) "Profil de Prière" else "Prayer Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepNavy
                        )
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("btn_close_profile_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Avatar with Gold Halo
                Surface(
                    shape = CircleShape,
                    color = MidnightBlue,
                    border = BorderStroke(2.5.dp, SoftGold),
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("profile_avatar_badge")
                ) {
                    if (!user.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.background(MidnightBlue)
                        ) {
                            Text(
                                text = user.initials,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SoftGold,
                                    fontSize = 24.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Display Name
                Text(
                    text = user.displayTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    ),
                    textAlign = TextAlign.Center
                )

                // Email
                if (!user.email.isNullOrBlank()) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Badge & Cloud Sync Status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RoyalNavy.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, RoyalNavy.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (user.providerId.contains("google", ignoreCase = true)) Icons.Default.Shield else Icons.Default.Email,
                                contentDescription = null,
                                tint = RoyalNavy,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (user.providerId.contains("google", ignoreCase = true)) "Google" else "Email",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RoyalNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFr) "Cloud Actif" else "Cloud Synced",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = SoftGold.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(16.dp))

                // Devotion Stats Overview
                Text(
                    text = if (isFr) "Bilan Spirituel Personnel" else "Personal Devotion Summary",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Proclamations
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OffWhite,
                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RoyalNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${totalProclamations ?: 0}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = if (isFr) "Proclamations" else "Proclamations",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Streak
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OffWhite,
                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = SoftGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$dailyStreak ${if (isFr) "J" else "D"}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = if (isFr) "Série de Prière" else "Daily Streak",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Prayer Time
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OffWhite,
                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedTotalTime,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = if (isFr) "Temps Total" else "Prayer Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Admin Dashboard Entry Button
                Button(
                    onClick = {
                        if (user.isAdmin || viewModel.isAdminUnlocked.value) {
                            onDismissRequest()
                            onOpenAdminDashboard()
                        } else {
                            showAdminPasscodeDialog = true
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepNavy,
                        contentColor = SoftGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_open_admin_dashboard")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Admin Dashboard",
                        tint = SoftGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFr) "Tableau de Bord Admin" else "Admin Dashboard",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftGold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sign Out Button
                OutlinedButton(
                    onClick = { showSignOutConfirm = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PureWhite,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_sign_out")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFr) "Se Déconnecter" else "Sign Out",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }

    // Admin Passcode Dialog (for quick admin authentication)
    if (showAdminPasscodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminPasscodeDialog = false
                adminPasscodeError = false
                adminPasscodeInput = ""
            },
            title = {
                Text(
                    text = if (isFr) "🔐 Accès Administrateur" else "🔐 Admin Access",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isFr) "Saisissez le mot de passe administrateur pour accéder à la gestion globale :" else "Enter administrator passcode to access live ministry controls:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = adminPasscodeInput,
                        onValueChange = {
                            adminPasscodeInput = it
                            adminPasscodeError = false
                        },
                        label = { Text(if (isFr) "Code secret admin" else "Admin Passcode") },
                        singleLine = true,
                        isError = adminPasscodeError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (adminPasscodeError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFr) "Code d'accès incorrect." else "Incorrect passcode.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.unlockAdminMode(adminPasscodeInput)
                        if (success) {
                            showAdminPasscodeDialog = false
                            adminPasscodeInput = ""
                            onDismissRequest()
                            onOpenAdminDashboard()
                        } else {
                            adminPasscodeError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = SoftGold)
                ) {
                    Text(if (isFr) "Déverrouiller" else "Unlock")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminPasscodeDialog = false
                        adminPasscodeError = false
                        adminPasscodeInput = ""
                    }
                ) {
                    Text(if (isFr) "Annuler" else "Cancel", color = TextDark)
                }
            }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = {
                Text(
                    text = if (isFr) "Confirmer la déconnexion ?" else "Confirm Sign Out?",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Text(
                    text = if (isFr) {
                        "Vous serez déconnecté de votre compte. Vos données locales resteront sécurisées sur cet appareil."
                    } else {
                        "You will be signed out of your account. Your local prayer records will remain safely on this device."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        showSignOutConfirm = false
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = PureWhite
                    )
                ) {
                    Text(if (isFr) "Se déconnecter" else "Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(if (isFr) "Annuler" else "Cancel", color = TextDark)
                }
            }
        )
    }
}
