package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlin.math.max

/**
 * Phase 6: Interactive Custom Target Proclamation Dialog.
 * Allows users to set any custom proclamation goal with numeric keyboard input,
 * quick preset chips, stepper adjustments (+/- 100, 500, 1000, 5000), and live progress preview.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetTargetDialog(
    currentTarget: Int,
    currentTotalCount: Int,
    language: AppLanguage,
    onTargetConfirmed: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val isFr = language == AppLanguage.FRENCH
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var inputString by remember { mutableStateOf(currentTarget.toString()) }
    var selectedPreset by remember { mutableStateOf(currentTarget) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Parse target safely
    val parsedTarget = inputString.toIntOrNull() ?: 0

    // Localized Strings
    val dialogTitle = if (isFr) "Définir l'Objectif" else "Set Proclamation Target"
    val dialogSubtitle = if (isFr) {
        "Fixez un objectif personnel de proclamations pour votre moment de prière"
    } else {
        "Set a personal target number of proclamations for your devotion"
    }
    val targetInputLabel = if (isFr) "Nombre de proclamations" else "Target Proclamations"
    val targetInputPlaceholder = if (isFr) "ex. 1000" else "e.g. 1000"
    val presetsHeader = if (isFr) "OBJECTIFS RECOMMANDÉS" else "RECOMMENDED PRESETS"
    val steppersHeader = if (isFr) "AJUSTEMENT RAPIDE" else "QUICK ADJUSTMENT"
    val goalSummaryHeader = if (isFr) "APERÇU DE LA PROGRESSION" else "PROGRESSION PREVIEW"
    val currentLabel = if (isFr) "Compte actuel :" else "Current Count:"
    val newGoalLabel = if (isFr) "Nouvel objectif :" else "New Target:"
    val remainingLabel = if (isFr) "Restant à faire :" else "Remaining:"
    val completedGoalText = if (isFr) "🎉 Objectif déjà atteint avec ce total !" else "🎉 Target already achieved with your current count!"
    val confirmBtnText = if (isFr) "Appliquer l'objectif" else "Set Target"
    val cancelBtnText = if (isFr) "Annuler" else "Cancel"
    val customTagLabel = if (isFr) "Personnalisé" else "Custom"

    val standardPresets = listOf(50, 100, 250, 500, 1000, 2000, 2500, 5000, 10000, 25000, 50000)

    fun applyTargetValue(value: Int) {
        val clamped = value.coerceIn(1, 1_000_000)
        inputString = clamped.toString()
        selectedPreset = clamped
        errorMessage = null
    }

    fun adjustTarget(delta: Int) {
        val current = inputString.toIntOrNull() ?: currentTarget
        applyTargetValue(current + delta)
    }

    Dialog(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismissRequest()
        },
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
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .testTag("dialog_set_target")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
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
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = dialogTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = if (standardPresets.contains(parsedTarget)) "Preset" else customTagLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (standardPresets.contains(parsedTarget)) RoyalNavy else SoftGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_target_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = dialogSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, lineHeight = 20.sp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Input Field with clear and formatting
                OutlinedTextField(
                    value = inputString,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.filter { it.isDigit() }.take(7)
                        inputString = digitsOnly
                        val num = digitsOnly.toIntOrNull()
                        if (num != null) {
                            selectedPreset = num
                            errorMessage = when {
                                num < 1 -> if (isFr) "L'objectif doit être d'au moins 1" else "Target must be at least 1"
                                num > 1_000_000 -> if (isFr) "Maximum 1 000 000" else "Maximum 1,000,000"
                                else -> null
                            }
                        } else if (digitsOnly.isEmpty()) {
                            errorMessage = if (isFr) "Veuillez entrer un nombre" else "Please enter a number"
                        }
                    },
                    label = { Text(targetInputLabel) },
                    placeholder = { Text(targetInputPlaceholder) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = SoftGold
                        )
                    },
                    trailingIcon = {
                        if (inputString.isNotEmpty()) {
                            IconButton(onClick = {
                                inputString = ""
                                errorMessage = if (isFr) "Veuillez entrer un nombre" else "Please enter a number"
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = AccentRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = if (parsedTarget > 0) String.format("%,d %s", parsedTarget, if (isFr) "proclamations" else "proclamations") else "",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftGold,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = SurfaceVariantBg,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_target")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stepper Buttons (-500, -100, +100, +500, +1,000, +5,000)
                Text(
                    text = steppersHeader,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stepperSteps = listOf(-1000, -500, -100, 100, 500, 1000, 5000)
                    stepperSteps.forEach { step ->
                        val isPositive = step > 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPositive) GoldContainer else SurfaceVariantBg,
                            border = BorderStroke(
                                1.dp,
                                if (isPositive) GoldOutline else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .clickable {
                                    adjustTarget(step)
                                }
                                .testTag("btn_step_${if (isPositive) "plus" else "minus"}_${kotlin.math.abs(step)}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.Add else Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = if (isPositive) DeepNavy else TextDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%,d", kotlin.math.abs(step)),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isPositive) DeepNavy else TextDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Recommended Presets FlowRow
                Text(
                    text = presetsHeader,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    standardPresets.forEach { preset ->
                        val isSelected = parsedTarget == preset
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) RoyalNavy else SurfaceVariantBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) RoyalNavy else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable {
                                    applyTargetValue(preset)
                                }
                                .testTag("preset_target_$preset")
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

                Spacer(modifier = Modifier.height(20.dp))

                // Live Progression Preview Card
                if (parsedTarget > 0) {
                    val remaining = max(0, parsedTarget - currentTotalCount)
                    val progressFraction = (currentTotalCount.toFloat() / parsedTarget.toFloat()).coerceIn(0f, 1f)
                    val percentage = (progressFraction * 100).toInt()
                    val isAchieved = currentTotalCount >= parsedTarget

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAchieved) Color(0xFFF0FDF4) else SurfaceVariantBg
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isAchieved) AccentGreen.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goalSummaryHeader,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isAchieved) AccentGreen else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )

                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isAchieved) AccentGreen else RoyalNavy,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isAchieved) AccentGreen else SoftGold,
                                trackColor = Color(0xFFCBD5E1)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = currentLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                    )
                                    Text(
                                        text = String.format("%,d", currentTotalCount),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = newGoalLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                    )
                                    Text(
                                        text = String.format("%,d", parsedTarget),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalNavy
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = remainingLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                    )
                                    Text(
                                        text = if (remaining == 0) "0" else String.format("%,d", remaining),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (remaining == 0) AccentGreen else TextDark
                                        )
                                    )
                                }
                            }

                            if (isAchieved) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = completedGoalText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Confirm & Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_cancel_target")
                    ) {
                        Text(
                            text = cancelBtnText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (parsedTarget > 0 && errorMessage == null) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onTargetConfirmed(parsedTarget)
                                onDismissRequest()
                            }
                        },
                        enabled = parsedTarget > 0 && errorMessage == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftGold,
                            contentColor = DeepNavy,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = TextSubtle
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_confirm_target")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = confirmBtnText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        )
                    }
                }
            }
        }
    }
}
