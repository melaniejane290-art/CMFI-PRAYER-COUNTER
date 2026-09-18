package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.ui.theme.BrightGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftGold

@Composable
fun SplashScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val isFr = language == AppLanguage.FRENCH

    val titleProclamation = if (isFr) "PROCLAMATION DE PRIÈRE" else "PRAYER PROCLAMATION"
    val titleCounter = if (isFr) "COMPTEUR" else "COUNTER"
    val subtitle = if (isFr) "Proclamez. Persévérez. Triomphez." else "Proclaim. Persevere. Prevail."
    val verse = if (isFr) "« La mort et la vie sont au pouvoir de la langue »\n— Proverbes 18:21" else "“Death and life are in the power of the tongue”\n— Proverbs 18:21"
    val buttonText = if (isFr) "COMMENCER LA PRIÈRE" else "START PRAYER COUNTER"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepNavy,
                        MidnightBlue,
                        RoyalNavy
                    )
                )
            )
            .testTag("splash_screen")
    ) {
        // Serene background image with overlay
        Image(
            painter = painterResource(id = R.drawable.prayer_bg_hands_1787928573868),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Language Selector (English / Français)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MidnightBlue.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("splash_language_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (language == AppLanguage.ENGLISH) SoftGold else Color.Transparent,
                            modifier = Modifier.clickable { onLanguageChange(AppLanguage.ENGLISH) }
                        ) {
                            Text(
                                text = "🇺🇸 EN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (language == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal,
                                    color = if (language == AppLanguage.ENGLISH) DeepNavy else PureWhite
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (language == AppLanguage.FRENCH) SoftGold else Color.Transparent,
                            modifier = Modifier.clickable { onLanguageChange(AppLanguage.FRENCH) }
                        ) {
                            Text(
                                text = "🇫🇷 FR",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (language == AppLanguage.FRENCH) FontWeight.Bold else FontWeight.Normal,
                                    color = if (language == AppLanguage.FRENCH) DeepNavy else PureWhite
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Main Hero Emblem & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Radiant glow ring behind emblem
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(glowScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        BrightGold.copy(alpha = glowAlpha),
                                        SoftGold.copy(alpha = glowAlpha * 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Surface(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        color = MidnightBlue,
                        shadowElevation = 12.dp,
                        tonalElevation = 6.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.praying_hands_logo_1787928549167),
                                contentDescription = "Praying Hands Logo",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Title with Gold Accent
                Text(
                    text = titleProclamation,
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = titleCounter,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = PureWhite
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Peace & Scripture Tagline Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MidnightBlue.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GoldContainer,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            // Bottom Action Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = verse,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PureWhite.copy(alpha = 0.75f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToMain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftGold,
                        contentColor = DeepNavy
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_begin_prayer"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
