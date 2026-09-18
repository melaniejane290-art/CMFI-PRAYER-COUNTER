package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.SetTargetDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.PrayerPresets
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPrayerCounterScreen(
    viewModel: PrayerCounterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val isFr = state.language == AppLanguage.FRENCH

    var isEditingTopic by remember { mutableStateOf(false) }
    var tempTopicInput by remember(state.prayerTopic) { mutableStateOf(state.prayerTopic) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val customTargets by viewModel.customTargets.collectAsState()

    val presetTopics = PrayerPresets.getTopicsFor(state.language)
    val presetTargets = PrayerPresets.targets

    // Localized Strings
    val appTitle = if (isFr) "Compteur de Proclamations" else "Prayer Proclamation"
    val topicLabel = if (isFr) "SUJET DE PRIÈRE / PROCLAMATION" else "PRAYER TOPIC / PROCLAMATION"
    val topicHint = if (isFr) "ex. Jésus-Christ est Seigneur" else "e.g. Jesus Christ is the Lord"
    val targetLabel = if (isFr) "OBJECTIF DE PROCLAMATIONS" else "TARGET PROCLAMATIONS"
    val targetSuffix = if (isFr) "proclamations" else "proclamations"
    val currentCountLabel = if (isFr) "COMPTE ACTUEL" else "CURRENT COUNT"
    val targetRatioLabel = if (isFr) "Objectif : " else "Target: "
    val progressLabel = if (isFr) "Progression" else "Progress"
    val savedBaseLabel = if (isFr) "Base enregistrée" else "Saved Base"
    val thisSessionLabel = if (isFr) "Cette session" else "This Session"
    val btnCountPlusOne = if (isFr) "COMPTER +1" else "COUNT +1"
    val sessionControlsLabel = if (isFr) "CONTRÔLES DE LA SESSION" else "SESSION CONTROLS"
    val btnStart = if (isFr) "DÉMARRER" else "START"
    val btnPause = if (isFr) "PAUSE" else "PAUSE"
    val btnSave = if (isFr) "ENREGISTRER" else "SAVE"
    val btnResetSession = if (isFr) "RÉINITIALISER LA SESSION" else "RESET SESSION"

    val statusText = when {
        state.isTargetReached -> if (isFr) "OBJECTIF ATTEINT" else "TARGET ACHIEVED"
        state.isTimerRunning -> if (isFr) "EN PRIÈRE" else "PRAYING NOW"
        else -> if (isFr) "SESSION EN PAUSE" else "SESSION PAUSED"
    }

    val defaultSaveBannerText = if (isFr) "Progression de la session enregistrée avec succès !" else "Session progress saved successfully!"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("main_prayer_counter_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Save Notification Banner
                AnimatedVisibility(
                    visible = state.showSaveSuccessBanner,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GoldContainer),
                        border = BorderStroke(1.dp, GoldOutline),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("save_success_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = state.saveMessage.ifEmpty { defaultSaveBannerText },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissSaveBanner() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 1. PRAYER TOPIC & PROCLAMATION CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("prayer_topic_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = RoyalNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = topicLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            IconButton(
                                onClick = { isEditingTopic = !isEditingTopic },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_edit_topic")
                            ) {
                                Icon(
                                    imageVector = if (isEditingTopic) Icons.Default.Check else Icons.Default.Edit,
                                    contentDescription = "Edit Topic",
                                    tint = RoyalNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditingTopic) {
                            OutlinedTextField(
                                value = tempTopicInput,
                                onValueChange = {
                                    tempTopicInput = it
                                    viewModel.updatePrayerTopic(it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_prayer_topic"),
                                placeholder = { Text(topicHint) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftGold,
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark
                                ),
                                singleLine = false,
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    isEditingTopic = false
                                })
                            )
                        } else {
                            val defaultDisplay = if (isFr) "Jésus-Christ est Seigneur" else "Jesus Christ is the Lord"
                            Text(
                                text = "“${state.prayerTopic.ifEmpty { defaultDisplay }}”",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    lineHeight = 28.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isEditingTopic = true }
                                    .testTag("display_prayer_topic")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick suggestion pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetTopics.forEach { topic ->
                                val isSelected = state.prayerTopic == topic
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) GoldContainer else SurfaceVariantBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SoftGold else Color.Transparent
                                    ),
                                    modifier = Modifier.clickable {
                                        tempTopicInput = topic
                                        viewModel.updatePrayerTopic(topic)
                                        isEditingTopic = false
                                    }
                                ) {
                                    Text(
                                        text = topic,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) DeepNavy else TextDark,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. TARGET SELECTION BAR (PHASE 6: User-Defined Target)
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("target_selection_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = targetLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showTargetDialog = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("btn_open_custom_target")
                            ) {
                                Text(
                                    text = String.format("%,d", state.targetCount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RoyalNavy
                                    ),
                                    modifier = Modifier.testTag("display_target_count")
                                )
                                Text(
                                    text = " $targetSuffix",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Target",
                                    tint = SoftGold,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .testTag("btn_edit_target")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target preset & custom pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dedicated Custom Target Button Pill
                            val isCurrentPreset = presetTargets.contains(state.targetCount)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (!isCurrentPreset) SoftGold else GoldContainer,
                                border = BorderStroke(1.dp, if (!isCurrentPreset) DeepNavy else GoldOutline),
                                modifier = Modifier
                                    .clickable { showTargetDialog = true }
                                    .testTag("btn_custom_target_pill")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = DeepNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (!isCurrentPreset) {
                                            "🎯 ${String.format("%,d", state.targetCount)}"
                                        } else {
                                            if (isFr) "Personnalisé..." else "Custom..."
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = DeepNavy,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Custom targets saved by user (if any)
                            customTargets.filter { it != state.targetCount }.forEach { customTgt ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceVariantBg,
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        viewModel.updateTargetCount(customTgt)
                                    }
                                ) {
                                    Text(
                                        text = String.format("%,d", customTgt),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = RoyalNavy,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            // Standard Preset Pills
                            presetTargets.forEach { preset ->
                                val isSelected = state.targetCount == preset
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) RoyalNavy else SurfaceVariantBg,
                                    modifier = Modifier.clickable {
                                        viewModel.updateTargetCount(preset)
                                    }
                                ) {
                                    Text(
                                        text = String.format("%,d", preset),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isSelected) PureWhite else TextDark,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Steppers Bar (-100, +100, +500, +1000)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFr) "Ajuster :" else "Adjust:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                            )
                            listOf(-100, 100, 500, 1000).forEach { step ->
                                val isPos = step > 0
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPos) GoldContainer.copy(alpha = 0.5f) else SurfaceVariantBg,
                                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable { viewModel.adjustTargetBy(step) }
                                ) {
                                    Text(
                                        text = if (isPos) "+$step" else "$step",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isPos) DeepNavy else TextDark,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. MAIN DIGITAL COUNTER & ACTIVE SESSION HERO
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("main_counter_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Session Status & Timer Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active status chip
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (state.isTimerRunning) AccentGreen.copy(alpha = 0.2f) else MidnightBlue,
                                border = BorderStroke(
                                    1.dp,
                                    if (state.isTimerRunning) AccentGreen else SoftGold.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (state.isTimerRunning) AccentGreen else TextSubtle)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (state.isTimerRunning) AccentGreen else PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }

                            // Session Timer
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MidnightBlue
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = state.formattedSessionTime,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        modifier = Modifier.testTag("session_timer_display")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Large Digital Counter Display
                        Text(
                            text = currentCountLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SoftGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Giant Counter Number
                        Text(
                            text = String.format("%,d", state.totalCount),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 72.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite,
                                letterSpacing = (-1).sp
                            ),
                            modifier = Modifier.testTag("current_count_number")
                        )

                        // Target Ratio: / Target
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showTargetDialog = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = targetRatioLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PureWhite.copy(alpha = 0.7f)
                                )
                            )
                            Text(
                                text = "/ ${String.format("%,d", state.targetCount)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = SoftGold,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("target_ratio_display")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Target",
                                tint = SoftGold.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Progress Indicator & Percentage
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = progressLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PureWhite.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "${state.progressPercentage}%",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SoftGold,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("progress_percentage_text")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { state.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .testTag("progress_bar"),
                                color = SoftGold,
                                trackColor = MidnightBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Cumulative vs Active Session Summary
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MidnightBlue.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = savedBaseLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                                    )
                                    Text(
                                        text = String.format("%,d", state.savedCumulativeCount),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.testTag("saved_cumulative_count_text")
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(1.dp)
                                        .background(Color(0xFF334155))
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = thisSessionLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                                    )
                                    Text(
                                        text = "+${state.currentSessionCount}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = BrightGold,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.testTag("session_count_text")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // 4. LARGE PRIMARY COUNT +1 BUTTON
                        Button(
                            onClick = { viewModel.incrementCount() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftGold,
                                contentColor = DeepNavy
                            ),
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .shadow(12.dp, RoundedCornerShape(32.dp))
                                .testTag("btn_count_plus_one"),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = DeepNavy
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = btnCountPlusOne,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.5.sp,
                                        color = DeepNavy
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. SESSION CONTROLS: START, PAUSE, SAVE, RESET SESSION
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("session_controls_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = sessionControlsLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 1: START / PAUSE & SAVE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start or Pause button
                            if (state.isTimerRunning) {
                                Button(
                                    onClick = { viewModel.pauseTimer() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MidnightBlue,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("btn_pause")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = BrightGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = btnPause,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.startTimer() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalNavy,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("btn_start")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = SoftGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = btnStart,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // SAVE button
                            Button(
                                onClick = { viewModel.saveProgress() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftGold,
                                    contentColor = DeepNavy
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("btn_save")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = DeepNavy
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = btnSave,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // RESET SESSION button
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AccentRed
                            ),
                            border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_reset_session")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AccentRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = btnResetSession,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRed
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

    // Set Custom Target Dialog (Phase 6)
    if (showTargetDialog) {
        SetTargetDialog(
            currentTarget = state.targetCount,
            currentTotalCount = state.totalCount,
            language = state.language,
            onTargetConfirmed = { newTarget ->
                viewModel.updateTargetCount(newTarget)
            },
            onDismissRequest = { showTargetDialog = false }
        )
    }

    // Reset Session confirmation dialog
    if (showResetDialog) {
        val dialogTitle = if (isFr) "Réinitialiser la session en cours ?" else "Reset Current Session?"
        val dialogMessage = if (isFr) {
            "Cela réinitialisera le chronomètre de la session et le compte de session (+${state.currentSessionCount}) à zéro.\n\nVotre total cumulé sauvegardé (${state.savedCumulativeCount}) NE SERA PAS supprimé."
        } else {
            "This will reset the current session timer and session count (+${state.currentSessionCount}) back to zero.\n\nYour saved cumulative proclamation count (${state.savedCumulativeCount}) will NOT be deleted."
        }
        val dialogResetBtn = if (isFr) "Réinitialiser la session" else "Reset Session"
        val dialogCancelBtn = if (isFr) "Annuler" else "Cancel"

        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSession()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text(dialogResetBtn, color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(dialogCancelBtn, color = TextDark)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
